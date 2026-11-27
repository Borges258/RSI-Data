#!/usr/bin/env python3
"""Verify a completed ablation run and write an offline, reproducible summary."""

from __future__ import annotations

import argparse
from collections import Counter
import json
from pathlib import Path
import sys

from run_ablation import REPORTS, RESULT, atomic_json, cohort


ARMS = ("E0", "E1", "E2", "E3")
DESIGN = {"E0": ("No", "No"), "E1": ("Yes", "No"),
          "E2": ("No", "Yes"), "E3": ("Yes", "Yes")}


def load_json(path):
    return json.loads(path.read_text(encoding="utf-8"))


def load_jsonl(path):
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]


def verify(run_dir):
    manifest = load_json(run_dir / "manifest.json")
    selected = cohort()
    expected = {r["position"]: r["example_id"] for r in selected}
    if manifest["sample_count"] != 410 or manifest["arms"] != list(ARMS):
        raise ValueError("run does not contain the full 2×2 cohort")
    if {r["position"]: r["example_id"] for r in manifest["cohort"]} != expected:
        raise ValueError("manifest cohort identity mismatch")
    results = {}
    for arm in ARMS:
        arm_dir = run_dir / arm
        raw_paths = list(arm_dir.glob("[0-9][0-9][0-9].json"))
        if len(raw_paths) != 410:
            raise ValueError(f"{arm}: expected 410 raw sample files, got {len(raw_paths)}")
        raw = {int(path.stem): load_json(path) for path in raw_paths}
        if set(raw) != set(expected):
            raise ValueError(f"{arm}: sample slots differ from frozen cohort")
        evaluated = load_jsonl(arm_dir / "evaluated.jsonl")
        if len(evaluated) != 410 or len({r["position"] for r in evaluated}) != 410:
            raise ValueError(f"{arm}: evaluated JSONL count/identity mismatch")
        evaluation = {r["position"]: r for r in evaluated}
        for position, example_id in expected.items():
            if raw[position]["example_id"] != example_id or evaluation[position]["example_id"] != example_id:
                raise ValueError(f"{arm}: wrong example at slot {position}")
            if raw[position]["safe_completed"] != evaluation[position]["safe_completed"]:
                raise ValueError(f"{arm}: SCR raw/evaluated disagreement at slot {position}")
        metrics = load_json(arm_dir / "metrics.json")
        checks = {"safe_completed": "safe_completed", "strict_where_match": "exact",
                  "normalized_where_match": "normalized", "where_execution_match": "where_execution_match",
                  "nonempty_where_execution_match": "nonempty_where_execution_match",
                  "both_empty_match": "both_empty"}
        if metrics["denominator"] != 410:
            raise ValueError(f"{arm}: SCR denominator changed")
        for metric, field in checks.items():
            if metrics[metric] != sum(bool(row[field]) for row in evaluated):
                raise ValueError(f"{arm}: {metric} does not match evaluated records")
        results[arm] = {"metrics": metrics, "raw": raw, "evaluated": evaluation}
    return manifest, results


def percent(numerator, denominator):
    return f"{100 * numerator / denominator:.2f}%"


def transitions(left, right, field):
    before = left["evaluated"]
    after = right["evaluated"]
    wins = sum(not before[p][field] and after[p][field] for p in before)
    losses = sum(before[p][field] and not after[p][field] for p in before)
    return {"gained": wins, "lost": losses, "net": wins - losses}


def summary(manifest, results):
    entries = {}
    for arm, data in results.items():
        m = data["metrics"]
        failures = [r for r in data["raw"].values() if not r["safe_completed"]]
        entries[arm] = {
            "metrics": m,
            "status_counts": dict(Counter(r["status"] for r in data["raw"].values())),
            "failed_stage_counts": dict(Counter(r.get("response", {}).get("stage", "UNKNOWN")
                                                for r in failures)),
            "failed_example_ids": [r["example_id"] for r in failures],
        }
    pairs = {f"{a}_to_{b}": {field: transitions(results[a], results[b], field)
                              for field in ("safe_completed", "normalized", "where_execution_match")}
             for a, b in (("E0", "E1"), ("E0", "E2"), ("E0", "E3"), ("E1", "E3"), ("E2", "E3"))}
    return {"run_id": manifest["run_id"], "sample_count": 410,
            "gold_nonempty_where_count": manifest["gold_nonempty_where_count"],
            "model": manifest["model"], "concurrency": manifest["concurrency"],
            "arms": entries, "paired_changes": pairs,
            "verification": "All four arms have 410 unique aligned raw/evaluated samples and matching metrics."}


def render(report):
    lines = [f"# WikiSQL WHERE 正式 2×2 消融实验：{report['run_id']}", "",
             "## 数据与口径", "",
             "有效样本 410 条；另有 186 条 Gold 审计排除样本，不进入分母。"
             "其中 405 条 Gold WHERE 筛选结果非空，5 条为空。E0 直接复用先前 E4 预测；"
             "E1 仅使用先验知识，E2 仅使用原子 Skill，E3 同时使用两者。",
             "", "SCR = 成功生成且通过条件校验的样本数 / 410。"
             "E0 依据冻结记录的 JSON/结构校验及本地 conds 校验；"
             "E1 依据 /parse 成功状态及本地 conds 校验；"
             "E2/E3 还要求 Flow 完成且 WHERE_CONDITION_VALIDATE.valid=true。",
             "", "## 实验表", "",
             "| 组别 | 先验知识 | 原子 Skill | SCR | 严格结构匹配 | 归一化结构匹配 | WHERE 行一致 | 非空 WHERE 行一致 | 双方空结果 |",
             "| --- | --- | --- | ---: | ---: | ---: | ---: | ---: | ---: |"]
    for arm in ARMS:
        m = report["arms"][arm]["metrics"]
        prior, skill = DESIGN[arm]
        lines.append(f"| {arm} | {prior} | {skill} | {m['safe_completed']}/410 ({percent(m['safe_completed'], 410)}) | "
                     f"{m['strict_where_match']}/410 ({percent(m['strict_where_match'], 410)}) | "
                     f"{m['normalized_where_match']}/410 ({percent(m['normalized_where_match'], 410)}) | "
                     f"{m['where_execution_match']}/410 ({percent(m['where_execution_match'], 410)}) | "
                     f"{m['nonempty_where_execution_match']}/405 ({percent(m['nonempty_where_execution_match'], 405)}) | "
                     f"{m['both_empty_match']} |")
    lines += ["", "严格结构匹配保留大小写及条件顺序；归一化结构匹配对值执行 Unicode casefold，"
              "并按保留重复项的 AND 条件多重集比较。WHERE 行一致仅比较当前 SQLite 表筛选出的行。"
              "双方空结果 5 条单列，不视为有效修复证据。", "",
              "## 失败状态与阶段", "",
              "| 组别 | 状态分布 | 未安全完成数 | 未安全完成阶段 |",
              "| --- | --- | ---: | --- |"]
    for arm in ARMS:
        entry = report["arms"][arm]
        status = ", ".join(f"{k} {v}" for k, v in sorted(entry["status_counts"].items()))
        stages = ", ".join(f"{k} {v}" for k, v in sorted(entry["failed_stage_counts"].items()))
        lines.append(f"| {arm} | {status} | {410-entry['metrics']['safe_completed']} | {stages or '—'} |")
    lines += ["", "## 本轮新增模型开销", "",
              "| 组别 | 观测成功模型返回 | 输入 Token | 其中缓存输入 Token | 缓存占比 | 输出 Token |",
              "| --- | ---: | ---: | ---: | ---: | ---: |"]
    for arm in ARMS:
        m = report["arms"][arm]["metrics"]
        prompt = m["new_prompt_tokens_observed"]
        cached = m["new_cached_prompt_tokens_observed"]
        ratio = percent(cached, prompt) if prompt else "—"
        lines.append(f"| {arm} | {m['new_model_calls_observed']} | {prompt:,} | {cached:,} | {ratio} | "
                     f"{m['new_completion_tokens_observed']:,} |")
    e1 = report["paired_changes"]["E1_to_E3"]
    e2 = report["paired_changes"]["E2_to_E3"]
    lines += ["", "调用次数仅统计原始阶段结果中可恢复的成功模型返回；供应商已处理但请求随后失败的调用可能未计入。"
              "缓存输入 Token 包含在输入 Token 内，不应相加。E0 本轮没有新增模型调用。", "",
              "## 同一样本配对变化", "",
              "| 对比 | SCR 新增 / 失去 / 净变化 | 归一化结构新增 / 失去 / 净变化 | WHERE 行一致新增 / 失去 / 净变化 |",
              "| --- | ---: | ---: | ---: |"]
    for key, name in (("E0_to_E1", "E0 → E1"), ("E0_to_E2", "E0 → E2"),
                      ("E0_to_E3", "E0 → E3"), ("E1_to_E3", "E1 → E3"),
                      ("E2_to_E3", "E2 → E3")):
        pair = report["paired_changes"][key]
        cells = [f"{pair[field]['gained']} / {pair[field]['lost']} / {pair[field]['net']:+d}"
                 for field in ("safe_completed", "normalized", "where_execution_match")]
        lines.append(f"| {name} | {' | '.join(cells)} |")
    lines += ["", "E3 相对 E2 的 SCR 和结构匹配上升；相对 E1 的归一化结构匹配上升，"
              f"但 SCR 净变化 {e1['safe_completed']['net']:+d} 条，WHERE 行一致净变化 "
              f"{e1['where_execution_match']['net']:+d} 条。先验知识使 E3 输入 Token 显著高于 E2；"
              "本表不以缓存命中推断实际费用。", "",
              "## 解释限制", "",
              "E1/E2/E3 只预测 WHERE conds，不能计算与 E0 可比的完整 SQL 官方 EX。"
              "Gold 曾按人工审计修订，因此历史 E4 的官方得分也不能直接转抄为此 410 条结果。"
              "四臂入口及可获得的校验证据不同，特别是 E0 为历史完整 SQL 基线；"
              "这些数值描述本次固定数据集上的观测表现，不单独证明因果收益或统计显著性。", "",
              f"结果核对：{report['verification']}", ""]
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("run_id")
    args = parser.parse_args()
    if args.run_id != Path(args.run_id).name:
        parser.error("run_id must not contain a path")
    manifest, results = verify(RESULT / args.run_id)
    report = summary(manifest, results)
    destination = REPORTS / args.run_id
    destination.mkdir(parents=True, exist_ok=True)
    atomic_json(destination / "formal_summary.json", report)
    (destination / "formal_summary.md").write_text(render(report), encoding="utf-8")
    print(json.dumps({"verified": report["verification"],
                      "report": str(destination / "formal_summary.md")}, ensure_ascii=False))


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        sys.exit(1)
