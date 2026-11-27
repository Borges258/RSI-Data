#!/usr/bin/env python3
"""Reproducible WikiSQL WHERE 2x2 run. Default is offline preflight; --execute calls LLM.

E0 reuses frozen E4 predictions. E1 uses where-prior /parse (knowledge only).
E2/E3 use WIKISQL_WHERE_GENERATION (empty/canvas prior respectively).
"""

from __future__ import annotations

import argparse
from collections import Counter
from concurrent.futures import FIRST_COMPLETED, ThreadPoolExecutor, wait
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import re
import sqlite3
import subprocess
import sys
import time
import urllib.error
import urllib.request


ROOT = Path(__file__).resolve().parents[1]
LAB = ROOT.parents[1]
DATA = ROOT / "data"
RESULT = ROOT / "result"
REPORTS = ROOT / "reports"
E4 = LAB / "dataset/WikiSQL/results/formal_test/test/E4/evaluated.jsonl"
ORIGINAL = LAB / "dataset/WikiSQL/data/test.jsonl"
FLOW_ID = "@com.horsecoder.common.whuTalent.dataTesting.core/WIKISQL_WHERE_GENERATION"
FLOW_SOURCE = LAB / "RSI/rsi-data-spring-boot/core-flow/src/main/resources/flow/Where/flow-wikisql-where-generation.json"
PRIOR_SOURCE = LAB / "RSI/rsi-data-spring-boot/where-prior/src/main/java/com/horsecoder/agentlab/whereprior/WherePriorController.java"
CANVAS_SOURCE = LAB / "RSI/rsi-data-platform/src/xpages/Login/test/wikisql_where_knowledge.tsx"
OPS = ("=", ">", "<")


def lines(file):
    with file.open(encoding="utf-8") as stream:
        for number, line in enumerate(stream, 1):
            if line.strip():
                try:
                    yield json.loads(line)
                except json.JSONDecodeError as exc:
                    raise ValueError(f"invalid JSONL {file}:{number}: {exc}") from exc


def digest(file):
    h = hashlib.sha256()
    with file.open("rb") as stream:
        for block in iter(lambda: stream.read(1 << 20), b""):
            h.update(block)
    return h.hexdigest()


def atomic_json(file, value):
    temporary = file.with_suffix(file.suffix + ".tmp")
    temporary.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    temporary.replace(file)


def config():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--run-id", required=True, help="Unique output name; reuse the same name to resume")
    parser.add_argument("--execute", action="store_true", help="Allow E1/E2/E3 HTTP calls; omitted = offline preflight")
    parser.add_argument("--arms", nargs="+", choices=("E0", "E1", "E2", "E3"), default=["E0", "E1", "E2", "E3"])
    parser.add_argument("--concurrency", type=int, default=2)
    parser.add_argument("--timeout", type=float, default=600.0)
    parser.add_argument("--retries", type=int, default=0,
                        help="Explicit opt-in; a timed-out request may already have charged model tokens")
    parser.add_argument("--model", default="gpt-5.6-sol")
    parser.add_argument("--flow-url", default="http://127.0.0.1:30845/api/engine/flow")
    parser.add_argument("--prior-url", default="http://127.0.0.1:18086/api/where-prior")
    parser.add_argument("--node", default="node")
    args = parser.parse_args()
    if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9_.-]{0,79}", args.run_id):
        parser.error("run-id must be a short path-safe name")
    if not 1 <= args.concurrency <= 4 or args.timeout <= 0 or args.retries < 0:
        parser.error("concurrency must be 1..4; timeout must be positive; retries nonnegative")
    args.arms = list(dict.fromkeys(args.arms))
    return args


def cohort():
    samples = list(lines(DATA / "test.jsonl"))
    excluded = list(lines(ROOT / "gold_excluded.jsonl"))
    if len(samples) != 410 or len(excluded) != 186:
        raise ValueError(f"expected 410 valid + 186 excluded; got {len(samples)} + {len(excluded)}")
    original = list(lines(ORIGINAL))
    if len(original) != 15878:
        raise ValueError("original WikiSQL test count changed")
    excluded_positions = {row["local_index"] for row in excluded}
    if len(excluded_positions) != len(excluded) or any(not 0 <= p < 596 for p in excluded_positions):
        raise ValueError("excluded cohort indices are invalid")
    valid_positions = [p for p in range(596) if p not in excluded_positions]
    if len(valid_positions) != len(samples):
        raise ValueError("valid/excluded cohort alignment failed")
    # The local cohort is sorted by gold agg and original index. The excluded
    # audit freezes the missing positions. Resolve duplicate questions by slot.
    original_lookup = {}
    for i, row in enumerate(original):
        original_lookup.setdefault((row["table_id"], row["question"]), []).append(i)
    full = [None] * 596
    for row in excluded:
        full[row["local_index"]] = row["example_id"]
        index = int(row["example_id"].split(":")[1])
        expected = original[index]
        if row["table_id"] != expected["table_id"]:
            raise ValueError("excluded audit does not match original WikiSQL")
    assigned = set()
    selected = []
    for position, sample in zip(valid_positions, samples):
        candidates = original_lookup.get((sample["table_id"], sample["question"]), [])
        candidates = [i for i in candidates if f"test:{i}:{sample['table_id']}" not in full]
        if len(candidates) > 1:
            exact = [i for i in candidates if original[i]["sql"]["agg"] == sample["sql"]["agg"]]
            if len(exact) == 1:
                candidates = exact
        candidates = [i for i in candidates if i not in assigned]
        if len(candidates) > 1 and all(original[i] == original[candidates[0]] for i in candidates):
            # Identical source duplicates retain their original file order.
            candidates = [min(candidates)]
        if len(candidates) != 1:
            raise ValueError(f"expected unique original E4 row at cohort position {position}; got {candidates}")
        index = candidates[0]
        assigned.add(index)
        example_id = f"test:{index}:{sample['table_id']}"
        full[position] = example_id
        selected.append({"position": position, "original_index": index,
                         "example_id": example_id, "sample": sample})
    if len(set(full)) != 596:
        raise ValueError("cohort identities are not unique")
    by_position = {row["position"]: row for row in selected}
    for fix in lines(ROOT / "gold_fixes_410.jsonl"):
        row = by_position.get(fix["local_index"])
        if not row or row["sample"]["question"] != fix["question"] or row["sample"]["sql"] != fix["after"]:
            raise ValueError(f"repaired Gold does not match cohort slot {fix['local_index']}")
    for audited in lines(ROOT / "gold_defect_samples.jsonl"):
        row = by_position.get(audited["local_index"])
        if row and row["example_id"] != audited["example_id"]:
            raise ValueError(f"audit identity does not match cohort slot {audited['local_index']}")
    return selected


def baseline(selected):
    wanted = {row["original_index"] for row in selected}
    found = {}
    for record in lines(E4):
        index = record.get("index")
        if index in wanted:
            found[index] = record
    if len(found) != len(wanted):
        raise ValueError("E4 evaluated records missing")
    for row in selected:
        record = found[row["original_index"]]
        if record.get("example_id") != row["example_id"] or record.get("question") != row["sample"]["question"]:
            raise ValueError(f"E4 identity mismatch: {row['example_id']}")
    return found


def knowledge_cache(tables, node):
    helper = Path(__file__).with_name("prepare_knowledge.cjs")
    result = subprocess.run([node, str(helper)], input=json.dumps(list(tables.values()), ensure_ascii=False),
                            text=True, encoding="utf-8", capture_output=True, check=True, timeout=120)
    prepared = json.loads(result.stdout)
    if set(prepared) != set(tables):
        raise ValueError("knowledge cache table coverage mismatch")
    return prepared


def request_json(url, body, timeout):
    data = json.dumps(body, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(url, data=data, method="POST",
                                 headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=timeout) as response:
        result = json.loads(response.read())
    if not isinstance(result, dict):
        raise ValueError("service returned non-object JSON")
    return result


def service_health(prior_url, timeout):
    with urllib.request.urlopen(prior_url.rstrip("/") + "/health", timeout=timeout) as response:
        health = json.loads(response.read())
    if (not isinstance(health, dict) or health.get("status") != "READY"
            or health.get("llmEnabled") is not True or health.get("llmConfigured") is not True):
        raise ValueError("where-prior health requires READY, llmEnabled and llmConfigured")
    return health


def decode_flow(body):
    data = body.get("data")
    value = data.get("value", data) if isinstance(data, dict) else body.get("value", body)
    value = value.get("result", value) if isinstance(value, dict) else value
    return json.loads(value) if isinstance(value, str) else value


def validate_conditions(conditions, table):
    errors = []
    if not isinstance(conditions, list) or len(conditions) > 4:
        return ["conds must be an array of at most 4 conditions"]
    for i, condition in enumerate(conditions):
        if not isinstance(condition, list) or len(condition) != 3:
            errors.append(f"condition {i}: expected triple")
            continue
        column, operator, value = condition
        if type(column) is not int or not 0 <= column < len(table["header"]):
            errors.append(f"condition {i}: invalid column")
        if type(operator) is not int or operator not in range(3):
            errors.append(f"condition {i}: invalid WikiSQL operator")
        if isinstance(value, bool) or not isinstance(value, (str, int, float)):
            errors.append(f"condition {i}: invalid value")
        elif isinstance(value, str) and not value.strip():
            errors.append(f"condition {i}: empty value")
        elif (type(column) is int and 0 <= column < len(table["header"])
              and table["types"][column].lower() == "real" and isinstance(value, str)
              and not re.fullmatch(r"-?\d[\d,]*(?:\.\d+)?%?", value)):
            errors.append(f"condition {i}: nonnumeric value for real column")
    return errors


def build_request(arm, sample, table, graph, args):
    flow_table = {**table, "headers": table["header"]}
    if arm == "E1":
        return args.prior_url.rstrip("/") + "/parse", {
            "question": sample["question"], "table": flow_table, "knowledge": graph["knowledge"]}
    if arm not in ("E2", "E3"):
        raise ValueError(f"no live request for {arm}")
    context = {"question": sample["question"], "table": flow_table}
    if arm == "E3":
        context.update(structure=graph["structure"], knowledge=graph["knowledge"])
    else:
        context["knowledgeContext"] = {"version": "wikisql-where-prior-v1",
                                       "activeTableId": table["id"],
                                       "atoms": [], "patterns": [], "relations": []}
    return args.flow_url, {"flowId": FLOW_ID,
                           "params": {"request": json.dumps(context, ensure_ascii=False)}}


def run_one(arm, row, tables, prepared, base, args):
    started = time.monotonic()
    sample = row["sample"]
    table = tables[sample["table_id"]]
    result = {"arm": arm, "position": row["position"], "example_id": row["example_id"],
              "original_index": row["original_index"], "table_id": sample["table_id"],
              "question": sample["question"], "gold_sql": sample["sql"],
              "prediction_conds": None, "status": "TRANSPORT_ERROR", "attempts": 0,
              "model": None, "usage": None, "validation_errors": []}
    if arm == "E0":
        record = base[row["original_index"]]
        prediction = record.get("prediction_sql")
        prior_valid = (record.get("api_success") is True and record.get("json_valid") is True
                       and record.get("structural_valid") is True
                       and not record.get("validation_errors"))
        result.update(status="SUCCEEDED" if prior_valid and isinstance(prediction, dict) else "BASELINE_FAILED",
                      prediction_conds=prediction.get("conds") if isinstance(prediction, dict) else None,
                      prediction_sql=prediction, model=record.get("request", {}).get("model"),
                      usage=record.get("usage"), attempts=0, source="frozen_E4",
                      baseline_validation={"json_valid": record.get("json_valid"),
                                           "structural_valid": record.get("structural_valid"),
                                           "validation_errors": record.get("validation_errors")})
    else:
        graph = prepared[sample["table_id"]]
        url, body = build_request(arm, sample, table, graph, args)
        for attempt in range(args.retries + 1):
            result["attempts"] = attempt + 1
            try:
                response = request_json(url, body, args.timeout)
                parsed = response if arm == "E1" else decode_flow(response)
                if not isinstance(parsed, dict):
                    raise ValueError("invalid Flow response")
                result["response"] = parsed
                result["status"] = parsed.get("status", "UNKNOWN")
                result["prediction_conds"] = parsed.get("conds")
                result["model"] = parsed.get("model") if arm == "E1" else None
                result["usage"] = parsed.get("usage") if arm == "E1" else None
                result["source"] = "where_prior_parse" if arm == "E1" else FLOW_ID
                break
            except urllib.error.HTTPError as exc:
                result["error"] = f"HTTP {exc.code}"
                if exc.code not in (408, 429, 500, 502, 503, 504):
                    break
            except (urllib.error.URLError, TimeoutError, OSError, ValueError) as exc:
                result["error"] = f"{type(exc).__name__}: {exc}"
            if attempt < args.retries:
                time.sleep(min(2 ** attempt, 8))
    result["validation_errors"] = validate_conditions(result["prediction_conds"], table)
    model_calls = []
    if arm == "E1" and result.get("model"):
        model_calls = [result["model"]]
    elif arm in ("E2", "E3"):
        for stage in result.get("response", {}).get("request", {}).get("stageResults", {}).values():
            if isinstance(stage, dict):
                model_calls.extend(call.get("model") for call in
                                   (stage.get("reviewModelCalls") or ([stage["modelCall"]] if "modelCall" in stage else [])))
    if any(model != args.model for model in model_calls) or (arm == "E0" and result["model"] != args.model):
        result["status"] = "MODEL_MISMATCH"
        result["validation_errors"].append(f"expected model {args.model}; observed {model_calls or result['model']}")
    if arm in ("E1", "E2", "E3") and result["status"] == "SUCCEEDED" and not model_calls:
        result["status"] = "MISSING_MODEL_EVIDENCE"
        result["validation_errors"].append("successful response has no persisted model call")
    flow_validated = arm not in ("E2", "E3") or (result.get("response", {}).get("completed") is True
                      and result.get("response", {}).get("request", {}).get("stageResults", {})
                      .get("WHERE_CONDITION_VALIDATE", {}).get("status") == "SUCCEEDED"
                      and result.get("response", {}).get("request", {}).get("stageResults", {})
                      .get("WHERE_CONDITION_VALIDATE", {}).get("valid") is True)
    result["safe_completed"] = (result["status"] == "SUCCEEDED" and not result["validation_errors"]
                                and flow_validated and isinstance(result["prediction_conds"], list))
    result["elapsed_ms"] = round((time.monotonic() - started) * 1000)
    return result


def where_rows(db, table, conditions):
    name = table["id"] if table["id"].startswith("table") else "table_" + table["id"].replace("-", "_")
    quoted = '"' + name.replace('"', '""') + '"'
    schema = [str(r[2]).lower() for r in db.execute(f"PRAGMA table_info({quoted})")]
    if not schema:
        raise ValueError(f"missing table: {name}")
    clauses, values = [], []
    for column, operator, value in conditions:
        if type(column) is not int or not 0 <= column < len(schema) or type(operator) is not int or operator not in range(3):
            raise ValueError("invalid condition column/operator")
        clauses.append(f"col{column} {OPS[operator]} ?")
        if schema[column] == "real":
            text = str(value).strip().replace(",", "")
            try:
                number = float(text)
            except ValueError:
                match = re.search(r"[-+]?\d*\.?\d+", text)
                if not match:
                    raise ValueError(f"numeric condition cannot be parsed: {value!r}")
                number = float(match.group())
            values.append(number)
        else:
            values.append(str(value).lower())
    suffix = " WHERE " + " AND ".join(clauses) if clauses else ""
    return sorted(r[0] for r in db.execute(f"SELECT rowid FROM {quoted}{suffix}", values))


def validate_gold(selected, tables):
    db = sqlite3.connect(f"file:{(DATA / 'test.db').as_posix()}?mode=ro", uri=True)
    nonempty = 0
    try:
        for row in selected:
            sample = row["sample"]
            table = tables[sample["table_id"]]
            errors = validate_conditions(sample["sql"]["conds"], table)
            if errors:
                raise ValueError(f"invalid Gold at slot {row['position']}: {errors}")
            try:
                nonempty += bool(where_rows(db, table, sample["sql"]["conds"]))
            except (ValueError, sqlite3.Error) as exc:
                raise ValueError(f"Gold execution failed at slot {row['position']}: {exc}") from exc
    finally:
        db.close()
    return nonempty


def keys(conditions, casefold=False):
    return Counter((c, op, v.casefold() if casefold and isinstance(v, str) else v)
                   for c, op, v in conditions)


def usage(record):
    if record["arm"] == "E0":
        u = record.get("usage") or {}
        return 0, 0, 0, 0  # Reused E4; no new token charge in this run.
    if record["arm"] == "E1":
        calls = [record.get("usage") or {}] if record.get("usage") else []
    else:
        calls = []
        stages = record.get("response", {}).get("request", {}).get("stageResults", {})
        for value in stages.values():
            calls.extend(value.get("reviewModelCalls") or ([value["modelCall"]] if "modelCall" in value else []))
        calls = [call.get("usage") or {} for call in calls]
    return (len(calls), sum(u.get("prompt_tokens", 0) for u in calls),
            sum(u.get("prompt_tokens_details", {}).get("cached_tokens", 0) for u in calls),
            sum(u.get("completion_tokens", 0) for u in calls))


def evaluate(arm, records, selected, tables):
    db = sqlite3.connect(f"file:{(DATA / 'test.db').as_posix()}?mode=ro", uri=True)
    evaluated = []
    lookup = {r["example_id"]: r for r in selected}
    try:
        for record in records:
            sample = lookup[record["example_id"]]["sample"]
            table = tables[sample["table_id"]]
            predicted = record["prediction_conds"] if record["safe_completed"] else None
            exact = predicted is not None and predicted == sample["sql"]["conds"]
            normalized = predicted is not None and keys(predicted, True) == keys(sample["sql"]["conds"], True)
            try:
                gold_rows = where_rows(db, table, sample["sql"]["conds"])
                pred_rows = where_rows(db, table, predicted) if predicted is not None else None
                execution = pred_rows is not None and pred_rows == gold_rows
                execution_error = None
            except (ValueError, sqlite3.Error) as exc:
                gold_rows = pred_rows = None
                execution = False
                execution_error = str(exc)
            evaluated.append({"arm": arm, "example_id": record["example_id"],
                              "position": record["position"], "status": record["status"],
                              "stage": record.get("response", {}).get("stage"),
                              "validation_errors": record["validation_errors"],
                              "error": record.get("error"),
                              "safe_completed": record["safe_completed"], "exact": exact,
                              "normalized": normalized, "where_execution_match": execution,
                              "both_empty": execution and not gold_rows,
                              "nonempty_where_execution_match": execution and bool(gold_rows),
                              "gold_row_count": len(gold_rows) if gold_rows is not None else None,
                              "prediction_row_count": len(pred_rows) if pred_rows is not None else None,
                              "execution_error": execution_error})
    finally:
        db.close()
    n = len(evaluated)
    sums = [usage(r) for r in records]
    metrics = {"arm": arm, "denominator": n,
               "safe_completed": sum(x["safe_completed"] for x in evaluated),
               "safe_completion_rate": sum(x["safe_completed"] for x in evaluated) / n,
               "strict_where_match": sum(x["exact"] for x in evaluated),
               "normalized_where_match": sum(x["normalized"] for x in evaluated),
               "where_execution_match": sum(x["where_execution_match"] for x in evaluated),
               "nonempty_where_execution_match": sum(x["nonempty_where_execution_match"] for x in evaluated),
               "nonempty_gold_count": sum(bool(x["gold_row_count"]) for x in evaluated),
               "both_empty_match": sum(x["both_empty"] for x in evaluated),
               "status_counts": dict(Counter(x["status"] for x in evaluated)),
               "validation_error_counts": dict(Counter(e for r in records for e in r["validation_errors"])),
               "new_model_calls_observed": sum(x[0] for x in sums),
               "new_prompt_tokens_observed": sum(x[1] for x in sums),
               "new_cached_prompt_tokens_observed": sum(x[2] for x in sums),
               "new_completion_tokens_observed": sum(x[3] for x in sums),
               "official_full_sql_ex": None,
               "official_ex_note": "E1/E2/E3 predict only WHERE; full SQL EX is not comparable across arms."}
    return evaluated, metrics


def run_pending(arm, pending, arm_dir, tables, prepared, base, args):
    limit = 1 if arm == "E0" else args.concurrency
    rows = iter(pending)
    pool = ThreadPoolExecutor(max_workers=limit)
    running = {}
    transport_failures = 0
    try:
        for _ in range(limit):
            row = next(rows, None)
            if row is not None:
                running[pool.submit(run_one, arm, row, tables, prepared, base, args)] = row
        while running:
            done, _ = wait(running, return_when=FIRST_COMPLETED)
            for future in done:
                row = running.pop(future)
                record = future.result()
                atomic_json(arm_dir / f"{row['position']:03d}.json", record)
                print(f"{arm} {row['position']:03d} {record['status']} safe={record['safe_completed']}", flush=True)
                if record["status"] == "MODEL_MISMATCH":
                    raise ValueError("wrong model observed; experiment stopped")
                if record.get("error") in ("HTTP 404", "HTTP 422"):
                    raise ValueError(f"endpoint/contract failure: {record['error']}; experiment stopped")
                transport_failures = transport_failures + 1 if record["status"] == "TRANSPORT_ERROR" else 0
                if transport_failures >= 5:
                    raise ValueError("five consecutive transport failures; experiment stopped")
                following = next(rows, None)
                if following is not None:
                    running[pool.submit(run_one, arm, following, tables, prepared, base, args)] = following
    finally:
        pool.shutdown(wait=True, cancel_futures=True)


def report_text(metrics):
    n = metrics["denominator"]
    nonempty = metrics["nonempty_gold_count"]
    lines = [f"# {metrics['arm']} WikiSQL WHERE formal experiment", "",
             f"SCR: {metrics['safe_completed']}/{n} = {metrics['safe_completion_rate']:.2%}",
             f"Strict ordered WHERE structure: {metrics['strict_where_match']}/{n}",
             f"Casefold and AND-order normalized structure: {metrics['normalized_where_match']}/{n}",
             f"WHERE selected-row agreement: {metrics['where_execution_match']}/{n}",
             f"Nonempty WHERE selected-row agreement: {metrics['nonempty_where_execution_match']}/{nonempty}",
             f"Both-empty agreement (reported separately): {metrics['both_empty_match']}",
             "", "## Status", ""]
    lines.extend(f"- {key}: {value}" for key, value in sorted(metrics["status_counts"].items()))
    lines += ["", "## Observed new model usage", "",
              f"Calls: {metrics['new_model_calls_observed']}; input tokens: {metrics['new_prompt_tokens_observed']}; "
              f"cached input tokens: {metrics['new_cached_prompt_tokens_observed']}; "
              f"output tokens: {metrics['new_completion_tokens_observed']}.",
              "", "E0 replays prior E4 results and makes no new model calls. E1/E2/E3 output WHERE only, "
              "so official full-SQL EX is unavailable and is never substituted with selected-row agreement.",
              ""]
    return "\n".join(lines)


def comparison_text(metrics_by_arm):
    lines = ["# WikiSQL WHERE 2×2 ablation", "",
             "SCR = successful generation with validated conditions / all 410 valid samples.",
             "E0 reuses frozen E4 predictions; E1 uses canvas prior only; E2 uses WHERE Skill Flow with empty prior; E3 uses both.",
             "", "| Arm | SCR | Strict WHERE | Normalized WHERE | WHERE rows | Nonempty WHERE rows | Both empty | New model calls | Input / cached / output tokens |",
             "| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |"]
    for arm, m in metrics_by_arm.items():
        n = m["denominator"]
        lines.append(f"| {arm} | {m['safe_completed']}/{n} ({m['safe_completion_rate']:.2%}) | "
                     f"{m['strict_where_match']}/{n} | {m['normalized_where_match']}/{n} | "
                     f"{m['where_execution_match']}/{n} | "
                     f"{m['nonempty_where_execution_match']}/{m['nonempty_gold_count']} | "
                     f"{m['both_empty_match']} | {m['new_model_calls_observed']} | "
                     f"{m['new_prompt_tokens_observed']} / {m['new_cached_prompt_tokens_observed']} / "
                     f"{m['new_completion_tokens_observed']} |")
    lines += ["", "Official full-SQL EX is unavailable for E1/E2/E3 WHERE-only outputs. "
              "WHERE selected-row agreement is a separate metric; both-empty agreement is not evidence of repair.", ""]
    return "\n".join(lines)


def main():
    args = config()
    selected = cohort()
    tables = {t["id"]: t for t in lines(DATA / "test.tables.jsonl")}
    if any(row["sample"]["table_id"] not in tables for row in selected):
        raise ValueError("missing table in current dataset")
    used_tables = {table_id: tables[table_id] for table_id in {r["sample"]["table_id"] for r in selected}}
    nonempty_gold = validate_gold(selected, tables)
    base = baseline(selected) if "E0" in args.arms else {}
    sources = [DATA / "test.jsonl", DATA / "test.tables.jsonl", DATA / "test.db",
               ROOT / "gold_excluded.jsonl", ROOT / "gold_fixes_410.jsonl",
               ROOT / "gold_defect_samples.jsonl", E4, ORIGINAL,
               PRIOR_SOURCE, CANVAS_SOURCE, Path(__file__), Path(__file__).with_name("prepare_knowledge.cjs")]
    sources += sorted(FLOW_SOURCE.parent.glob("*.json"))
    sources += sorted((LAB / "RSI/rsi-data-spring-boot/core-flow/src/main/java/com/horsecoder/common/whuTalent/core/flow/Skill/Where").glob("*.java"))
    sources.append(LAB / "RSI/rsi-data-spring-boot/core-flow/src/main/java/com/horsecoder/common/whuTalent/core/flow/FlowEngineService.java")
    sources.append(LAB / "RSI/rsi-data-platform/node_modules/typescript/package.json")
    frozen = {str(p.relative_to(LAB)): digest(p) for p in sources}
    plan = {"run_id": args.run_id, "sample_count": len(selected), "arms": args.arms,
            "concurrency": args.concurrency, "mode": "execute" if args.execute else "offline_preflight",
            "model": args.model, "flow_url": args.flow_url, "prior_url": args.prior_url,
            "node": args.node,
            "timeout_seconds": args.timeout, "retries": args.retries,
            "sha256": frozen, "cohort": [{k: r[k] for k in ("position", "original_index", "example_id")}
                                       for r in selected],
            "SCR": "safe_completed / 410 valid evaluation samples; successful status + valid WikiSQL cond triples; E2/E3 additionally require Flow condition validation",
            "gold_nonempty_where_count": nonempty_gold}
    if not args.execute:
        if any(a != "E0" for a in args.arms):
            knowledge_cache(used_tables, args.node)
        print(json.dumps({"preflight": "OK", "run_id": args.run_id, "count": len(selected),
                          "arms": args.arms, "gold_nonempty_where_count": nonempty_gold,
                          "canvas_tables_prepared": len(used_tables) if any(a != "E0" for a in args.arms) else 0,
                          "calls_made": 0,
                          "next": f"python dataset/WikiSQL_Where/experiment/run_ablation.py --run-id {args.run_id} --execute"}, ensure_ascii=False, indent=2))
        return
    health = service_health(args.prior_url, min(args.timeout, 15)) if any(a != "E0" for a in args.arms) else None
    run_dir = RESULT / args.run_id
    run_dir.mkdir(parents=True, exist_ok=True)
    manifest = run_dir / "manifest.json"
    if not manifest.exists() and any(run_dir.iterdir()):
        raise ValueError("run directory is not empty but has no manifest")
    if manifest.exists():
        existing = json.loads(manifest.read_text(encoding="utf-8"))
        for field in ("sample_count", "sha256", "cohort", "arms", "model", "flow_url",
                      "prior_url", "timeout_seconds", "retries", "concurrency", "node"):
            if existing[field] != plan[field]:
                raise ValueError(f"resume rejected: {field} changed")
    else:
        plan["created_at"] = datetime.now(timezone.utc).isoformat()
        plan["where_prior_health"] = health
        atomic_json(manifest, plan)
    prepared = {}
    if any(a != "E0" for a in args.arms):
        cache_file = run_dir / "knowledge_cache.json"
        if cache_file.exists():
            prepared = json.loads(cache_file.read_text(encoding="utf-8"))
        else:
            prepared = knowledge_cache(used_tables, args.node)
            atomic_json(cache_file, prepared)
        if set(prepared) != set(used_tables) or any(
                prepared[table_id]["knowledge"]["activeTableId"] != table_id
                for table_id in used_tables):
            raise ValueError("cached canvas knowledge does not match cohort tables")
        cache_hash = digest(cache_file)
        cache_manifest = json.loads(manifest.read_text(encoding="utf-8"))
        if cache_manifest.get("knowledge_cache_sha256") not in (None, cache_hash):
            raise ValueError("cached canvas knowledge hash changed")
        if cache_manifest.get("knowledge_cache_sha256") is None:
            cache_manifest["knowledge_cache_sha256"] = cache_hash
            atomic_json(manifest, cache_manifest)
    metrics_by_arm = {}
    for arm in args.arms:
        arm_dir = run_dir / arm
        arm_dir.mkdir(exist_ok=True)
        pending = []
        for row in selected:
            file = arm_dir / f"{row['position']:03d}.json"
            if not file.exists():
                pending.append(row)
        print(f"{arm}: {len(pending)} pending / 410", flush=True)
        run_pending(arm, pending, arm_dir, tables, prepared, base, args)
        records = []
        for row in selected:
            record = json.loads((arm_dir / f"{row['position']:03d}.json").read_text(encoding="utf-8"))
            if record["example_id"] != row["example_id"]:
                raise ValueError("resume result identity mismatch")
            records.append(record)
        evaluated, metrics = evaluate(arm, records, selected, tables)
        metrics_by_arm[arm] = metrics
        atomic_json(arm_dir / "metrics.json", metrics)
        (arm_dir / "evaluated.jsonl").write_text("".join(json.dumps(r, ensure_ascii=False) + "\n" for r in evaluated), encoding="utf-8")
        REPORTS.mkdir(parents=True, exist_ok=True)
        report_dir = REPORTS / args.run_id
        report_dir.mkdir(exist_ok=True)
        (report_dir / f"{arm}.md").write_text(report_text(metrics), encoding="utf-8")
        (report_dir / "comparison.md").write_text(comparison_text(metrics_by_arm), encoding="utf-8")
        print(json.dumps(metrics, ensure_ascii=False), flush=True)


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        sys.exit(1)
