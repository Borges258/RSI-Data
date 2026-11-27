"""Supplemental, read-only semantic comparison for the fixed E3 WHERE run.

This does not change the official labels, predictions, or the existing evaluator.
E3 predicts only WHERE conditions, so official full-SQL EX is unavailable.
"""

import argparse
from collections import Counter
import hashlib
import json
from pathlib import Path
import re
import sqlite3


ROOT = Path(__file__).resolve().parent
DEFAULT_RUN = ROOT / "results" / "e3_v3_20261126"
OPERATORS = ("=", ">", "<")


def read_jsonl(path):
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line]


def value_key(value):
    # No trimming, quote removal, punctuation rewriting, or number coercion:
    # those transformations could make different literals appear equivalent.
    return value.casefold() if isinstance(value, str) else value


def condition_key(condition):
    if not isinstance(condition, list) or len(condition) != 3:
        raise ValueError(f"invalid condition: {condition!r}")
    column, operator, value = condition
    if type(column) is not int or type(operator) is not int or operator not in range(3):
        raise ValueError(f"invalid condition: {condition!r}")
    return column, operator, value_key(value)


def multiset(conditions, normalize=False):
    if normalize:
        return Counter(condition_key(c) for c in conditions)
    return Counter((c[0], c[1], c[2]) for c in conditions)


def subtract_conditions(source, matched):
    """Return original condition triples, preserving duplicate multiplicity."""
    remaining = matched.copy()
    result = []
    for condition in source:
        key = condition_key(condition)
        if remaining[key]:
            remaining[key] -= 1
        else:
            result.append(condition)
    return result


def table_name(table_id):
    return table_id if table_id.startswith("table") else "table_" + table_id.replace("-", "_")


def quote(identifier):
    return '"' + identifier.replace('"', '""') + '"'


def wiki_number(value):
    if isinstance(value, (int, float)) and not isinstance(value, bool):
        return value
    text = str(value).strip().replace(",", "")
    try:
        return float(text)
    except ValueError:
        match = re.search(r"[-+]?\d*\.?\d+", text)
        if not match:
            raise ValueError(f"cannot parse numeric value {value!r}")
        return float(match.group())


class WhereEngine:
    def __init__(self, db_path):
        self.db = sqlite3.connect(f"file:{db_path.as_posix()}?mode=ro", uri=True)

    def close(self):
        self.db.close()

    def selected(self, table_id, conditions):
        name = table_name(table_id)
        schema = [str(row[2]).lower() for row in self.db.execute(f"PRAGMA table_info({quote(name)})")]
        if not schema:
            raise ValueError(f"missing table: {name}")
        clauses, values = [], []
        for column, operator, raw in conditions:
            condition_key([column, operator, raw])
            if column < 0 or column >= len(schema):
                raise ValueError(f"invalid column: {column}")
            clauses.append(f"col{column} {OPERATORS[operator]} ?")
            values.append(wiki_number(raw) if schema[column] == "real" else str(raw).lower())
        where = " WHERE " + " AND ".join(clauses) if clauses else ""
        return sorted(row[0] for row in self.db.execute(
            f"SELECT rowid FROM {quote(name)}{where}", values))


def classify(row, engine):
    result = {
        "record_number": row["record_number"], "example_id": row["example_id"],
        "category": row.get("category"), "table_id": row["table_id"], "status": row["status"],
        "gold": row["gold"], "predicted": row["predicted"],
        "completed": row["completed"], "existing_exact_cond_match": row["exact"],
        "official_ex": None,
        "official_ex_note": "E3 outputs WHERE conds only; predicted sel/agg unavailable",
        "classification": "MISMATCH", "extra_conditions": None,
        "missing_conditions": None, "redundancy_proven_on_current_table": False,
        "redundancy_evidence": None,
    }
    gold = row["gold"]
    predicted = row["predicted"] if row["completed"] and isinstance(row["predicted"], list) else None
    try:
        gold_rows = engine.selected(row["table_id"], gold)
        predicted_rows = engine.selected(row["table_id"], predicted) if predicted is not None else None
        result["gold_rowids"] = gold_rows
        result["predicted_rowids"] = predicted_rows
        result["where_execution_match"] = predicted_rows is not None and gold_rows == predicted_rows
        result["both_empty"] = result["where_execution_match"] and len(gold_rows) == 0

        if predicted is None:
            return result
        gold_keys, predicted_keys = multiset(gold, True), multiset(predicted, True)
        common = gold_keys & predicted_keys
        extra = subtract_conditions(predicted, common)
        missing = subtract_conditions(gold, common)
        result["extra_conditions"] = extra
        result["missing_conditions"] = missing
        result["raw_cond_multiset_match"] = multiset(gold) == multiset(predicted)
        result["normalized_structure_match"] = gold_keys == predicted_keys

        if result["raw_cond_multiset_match"]:
            # The existing row.exact ignores order; this classification does not.
            result["classification"] = "EXACT_MATCH" if gold == predicted else "NORMALIZED_MATCH"
        elif result["normalized_structure_match"]:
            result["classification"] = "NORMALIZED_MATCH"
        elif extra and not missing:
            # With no missing Gold condition, the shared condition multiset is Gold.
            common_rows = engine.selected(row["table_id"], gold)
            extended_rows = engine.selected(row["table_id"], gold + extra)
            result["redundancy_evidence"] = {
                "common_row_count": len(common_rows),
                "common_rowids": common_rows,
                "rows_after_extra": extended_rows,
                "all_common_rows_satisfy_extra": bool(common_rows) and common_rows == extended_rows,
            }
            if common_rows and common_rows == extended_rows and result["where_execution_match"]:
                result["redundancy_proven_on_current_table"] = True
                result["classification"] = "REDUNDANT_BUT_EQUIVALENT"

        if result["classification"] == "MISMATCH" and result["where_execution_match"]:
            result["classification"] = "DENOTATION_MATCH_ONLY"
    except (ValueError, sqlite3.Error, TypeError) as error:
        result["execution_error"] = str(error)
    return result


def summarize(rows):
    n = len(rows)
    counts = Counter(row["classification"] for row in rows)
    structure = counts["EXACT_MATCH"] + counts["NORMALIZED_MATCH"]
    executed = sum(row.get("where_execution_match", False) for row in rows)
    both_empty = sum(row.get("both_empty", False) for row in rows)
    nonempty_gold = sum(bool(row.get("gold_rowids")) for row in rows)
    nonempty_match = sum(row.get("where_execution_match", False)
                         and bool(row.get("gold_rowids")) for row in rows)
    by_category = {}
    for category in sorted({row["category"] for row in rows}):
        group = [row for row in rows if row["category"] == category]
        by_category[category] = {
            "count": len(group),
            "completed": sum(row["completed"] for row in group),
            "exact": sum(row["classification"] == "EXACT_MATCH" for row in group),
            "normalized_structure": sum(row["classification"] in ("EXACT_MATCH", "NORMALIZED_MATCH") for row in group),
            "where_execution_match": sum(row.get("where_execution_match", False) for row in group),
            "nonempty_where_execution_match": sum(row.get("where_execution_match", False)
                                                    and bool(row.get("gold_rowids")) for row in group),
            "both_empty": sum(row.get("both_empty", False) for row in group),
            "classifications": dict(Counter(row["classification"] for row in group)),
        }
    return {
        "sample_count": n,
        "official_ex": None,
        "official_ex_reason": "E3 has no predicted SELECT column or aggregation; full WikiSQL EX cannot be computed",
        "existing_exact_cond_match": sum(row["existing_exact_cond_match"] for row in rows),
        "normalized_structure_match": structure,
        "normalized_structure_rate": structure / n,
        "unmatched_predicted_condition_count": sum(len(row["extra_conditions"] or []) for row in rows),
        "pure_extra_condition_count": sum(len(row["extra_conditions"] or []) for row in rows
                                          if row["extra_conditions"] and not row["missing_conditions"]),
        "samples_with_pure_extra_conditions": sum(bool(row["extra_conditions"]) and not row["missing_conditions"]
                                                  for row in rows),
        "proven_redundant_condition_count": sum(len(row["extra_conditions"] or [])
                                                  for row in rows if row["redundancy_proven_on_current_table"]),
        "where_execution_match": executed,
        "where_execution_match_rate": executed / n,
        "nonempty_where_execution_match": nonempty_match,
        "nonempty_where_execution_match_denominator": nonempty_gold,
        "nonempty_where_execution_match_rate": nonempty_match / nonempty_gold if nonempty_gold else None,
        "both_empty_match": both_empty,
        "classifications": dict(counts),
        "by_category": by_category,
    }


def report_text(rows, metrics, source_hash, db_hash):
    fmt = lambda count: f"{count}/26 ({count / 26:.1%})"
    lines = [
        "# E3-v3 WHERE 语义评估（26 条）", "",
        "本报告复算已保存的 E3-v3 输出；Gold 和预测均未改动，未调用 LLM。", "",
        f"- 输入 `evaluated.jsonl` SHA-256：`{source_hash}`",
        f"- 输入 `test.db` SHA-256：`{db_hash}`",
        "- 官方完整 SQL EX：**无法计算**。E3 只输出 `conds`，缺少预测的 `sel` 与 `agg`。原记录中的 `exact` 是 WHERE 条件比较，不能冒充官方 EX。",
        f"- 原评估器严格条件多重集匹配：{fmt(metrics['existing_exact_cond_match'])}",
        f"- Unicode casefold 与 AND 顺序归一后的结构匹配：{fmt(metrics['normalized_structure_match'])}",
        f"- 真正多出的条件：{metrics['pure_extra_condition_count']} 个；在当前表上证明冗余：{metrics['proven_redundant_condition_count']} 个",
        f"- WHERE 筛选行一致：{fmt(metrics['where_execution_match'])}",
        f"- 非空 WHERE 一致：{metrics['nonempty_where_execution_match']}/{metrics['nonempty_where_execution_match_denominator']} ({metrics['nonempty_where_execution_match_rate']:.1%})；双方空集：{metrics['both_empty_match']} 条",
        "- 结构匹配与 WHERE 执行一致分别统计；双方空集单列。", "",
        "## W01–W14 分类统计", "",
        "每类仅 1–2 条，百分比是小样本描述，不能视为该类总体准确率。", "",
        "| 类别 | 样本 | 完成 | 严格结构 | 归一化结构 | WHERE 行一致 | 其中双方空集 |",
        "|---|---:|---:|---:|---:|---:|---:|",
    ]
    for category, stats in metrics["by_category"].items():
        lines.append(f"| {category} | {stats['count']} | {stats['completed']} | {stats['exact']} | "
                     f"{stats['normalized_structure']} | {stats['where_execution_match']} | {stats['both_empty']} |")
    lines += [
        "",
        "## 逐条分类", "",
        "序号按已保存 `evaluated.jsonl` 的顺序；`example_id` 为稳定样本标识。中途终止的样本没有最终预测，额外和缺失条件记作不可判定。", "",
        "| 序号 | W 类 | example_id | 结果类别 | 状态 | 未匹配预测条件 | 缺失 Gold 条件 | Gold 行数 | 预测行数 |",
        "|---:|---|---|---|---|---:|---:|---:|---:|",
    ]
    for row in rows:
        gold_count = len(row.get("gold_rowids", []))
        predicted_count = len(row["predicted_rowids"]) if row.get("predicted_rowids") is not None else "—"
        extra_count = len(row["extra_conditions"]) if row["extra_conditions"] is not None else "—"
        missing_count = len(row["missing_conditions"]) if row["missing_conditions"] is not None else "—"
        lines.append(f"| {row['record_number']:02d} | {row['category']} | `{row['example_id']}` | {row['classification']} | {row['status']} | "
                     f"{extra_count} | {missing_count} | {gold_count} | {predicted_count} |")
    lines += [
        "", "## 未匹配与异常明细", "",
    ]
    for row in rows:
        if row["classification"] in ("EXACT_MATCH", "NORMALIZED_MATCH"):
            continue
        evidence = f"Gold={row['gold']!r}；预测={row['predicted']!r}"
        if row["redundancy_evidence"]:
            evidence += f"；公共条件行数={row['redundancy_evidence']['common_row_count']}"
        if row.get("both_empty"):
            evidence += "；双方空集"
        lines.append(f"- #{row['record_number']:02d} {row['category']} {row['classification']}（{row['status']}）：{evidence}")
    lines += [
        "",
        "## 比较口径", "",
        "仅在比较键中对文本值执行 Python Unicode `casefold()`。WHERE 条件按多重集比较，忽略 AND 顺序但保留重复次数；列、运算符和值类型均保留。不消除标点、空格或引号，也不改写数字。只有公共 Gold 条件筛出至少一行，且额外条件在这些行上全部为真，才判当前表上的冗余。这不是普遍逻辑等价证明。", "",
    ]
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--run-dir", type=Path, default=DEFAULT_RUN)
    parser.add_argument("--output-dir", type=Path)
    args = parser.parse_args()
    run_dir = args.run_dir.resolve()
    output_dir = (args.output_dir or run_dir / "semantic_evaluation").resolve()
    source = read_jsonl(run_dir / "evaluated.jsonl")
    if len(source) != 26:
        raise ValueError(f"expected fixed 26 records, found {len(source)}")
    engine = WhereEngine(ROOT / "data" / "test.db")
    try:
        rows = [classify({**record, "record_number": index}, engine)
                for index, record in enumerate(source, 1)]
    finally:
        engine.close()
    metrics = summarize(rows)
    source_hash = hashlib.sha256((run_dir / "evaluated.jsonl").read_bytes()).hexdigest()
    db_hash = hashlib.sha256((ROOT / "data" / "test.db").read_bytes()).hexdigest()
    output_dir.mkdir(parents=True, exist_ok=True)
    (output_dir / "classified.jsonl").write_text(
        "".join(json.dumps(row, ensure_ascii=False) + "\n" for row in rows), encoding="utf-8")
    (output_dir / "metrics.json").write_text(
        json.dumps(metrics, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (output_dir / "report.md").write_text(
        report_text(rows, metrics, source_hash, db_hash), encoding="utf-8")
    print(json.dumps(metrics, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
