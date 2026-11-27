#!/usr/bin/env python3
"""Run the 400-sample E1/E2/E3 retention regression with the formal evaluator.

Without --execute this performs an offline provenance, Gold, and canvas preflight.
"""

from __future__ import annotations

import argparse
from datetime import datetime, timezone
import importlib.util
import json
from pathlib import Path
import re
import sys


ROOT = Path(__file__).resolve().parents[1]
LAB = ROOT.parents[1]
FORMAL_RUNNER = LAB / "dataset/WikiSQL_Where/experiment/run_ablation.py"
spec = importlib.util.spec_from_file_location("formal_where_runner", FORMAL_RUNNER)
formal = importlib.util.module_from_spec(spec)
spec.loader.exec_module(formal)
formal.ROOT = ROOT
formal.DATA = ROOT / "data"
formal.RESULT = ROOT / "result"
formal.REPORTS = ROOT / "reports"


def config():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--run-id", required=True)
    parser.add_argument("--execute", action="store_true")
    parser.add_argument("--arms", nargs="+", choices=("E1", "E2", "E3"),
                        default=["E1", "E2", "E3"])
    parser.add_argument("--concurrency", type=int, default=2)
    parser.add_argument("--timeout", type=float, default=600.0)
    parser.add_argument("--retries", type=int, default=0)
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
    manifest = json.loads((ROOT / "sample_manifest.json").read_text(encoding="utf-8"))
    samples = list(formal.lines(formal.DATA / "test.jsonl"))
    source = list(formal.lines(formal.ORIGINAL))
    baseline = {r["index"]: r for r in formal.lines(formal.E4)}
    entries = manifest["samples"]
    if manifest["total"] != 400 or len(entries) != 400 or len(samples) != 400:
        raise ValueError("regression cohort must contain exactly 400 rows")
    if len(source) != 15878 or len(baseline) != len(source):
        raise ValueError("original WikiSQL/E4 source count changed")
    selected = []
    seen = set()
    for position, (entry, sample) in enumerate(zip(entries, samples)):
        index = entry["index"]
        identity = f"test:{index}:{sample['table_id']}"
        if index in seen or not 0 <= index < len(source):
            raise ValueError(f"duplicate or invalid source index at slot {position}")
        seen.add(index)
        if (entry["example_id"] != identity or entry["table_id"] != sample["table_id"]
                or entry["question"] != sample["question"] or source[index] != sample):
            raise ValueError(f"sample/source identity mismatch at slot {position}")
        expected_stratum = str(min(len(sample["sql"]["conds"]), 3))
        if entry["stratum"] != expected_stratum:
            raise ValueError(f"stratum mismatch at slot {position}")
        baseline_row = baseline.get(index)
        if (baseline_row is None or baseline_row.get("example_id") != identity
                or baseline_row.get("question") != sample["question"]
                or baseline_row.get("ex_correct") is not True
                or baseline_row.get("api_success") is not True):
            raise ValueError(f"E4 success/provenance mismatch at slot {position}")
        selected.append({"position": position, "original_index": index,
                         "example_id": identity, "sample": sample})
    return selected


def comparison_text(metrics_by_arm):
    lines = ["# WikiSQL WHERE 原能力回归：400 条 E4 成功样本", "",
             "E1 为先验知识；E2 为 WHERE 原子 Skill；E3 同时使用两者。",
             "SCR = 成功生成且通过条件校验的样本数 / 400。", "",
             "| 组别 | SCR | 严格 WHERE 结构 | 归一化 WHERE 结构 | WHERE 行一致 | 非空 WHERE 行一致 | 双方空结果 | 新模型返回 | 输入 / 缓存 / 输出 Token |",
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
    lines += ["", "E1/E2/E3 仅生成 WHERE conds；WHERE 行一致不能冒充完整 SQL 官方 EX。",
              "双方空结果单列，不作为有效回归成功证据。", ""]
    return "\n".join(lines)


def run_pending(arm, pending, arm_dir, tables, prepared, args):
    # Reuse the formal runner's HTTP, model-evidence, and safety validation logic.
    from concurrent.futures import FIRST_COMPLETED, ThreadPoolExecutor, wait

    rows = iter(pending)
    failures = 0
    with ThreadPoolExecutor(max_workers=args.concurrency) as pool:
        running = {}
        for _ in range(args.concurrency):
            row = next(rows, None)
            if row is not None:
                running[pool.submit(formal.run_one, arm, row, tables, prepared, {}, args)] = row
        while running:
            done, _ = wait(running, return_when=FIRST_COMPLETED)
            for future in done:
                row = running.pop(future)
                record = future.result()
                formal.atomic_json(arm_dir / f"{row['position']:03d}.json", record)
                print(f"{arm} {row['position']:03d} {record['status']} safe={record['safe_completed']}", flush=True)
                if record["status"] == "MODEL_MISMATCH":
                    raise ValueError("wrong model observed; experiment stopped")
                if record.get("error") in ("HTTP 404", "HTTP 422"):
                    raise ValueError(f"endpoint/contract failure: {record['error']}; experiment stopped")
                failures = failures + 1 if record["status"] == "TRANSPORT_ERROR" else 0
                if failures >= 5:
                    raise ValueError("five consecutive transport failures; experiment stopped")
                following = next(rows, None)
                if following is not None:
                    running[pool.submit(formal.run_one, arm, following, tables, prepared, {}, args)] = following


def main():
    args = config()
    selected = cohort()
    tables = {t["id"]: t for t in formal.lines(formal.DATA / "test.tables.jsonl")}
    used = {r["sample"]["table_id"] for r in selected}
    if not used <= tables.keys():
        raise ValueError("regression table missing")
    prepared_tables = {table_id: tables[table_id] for table_id in used}
    nonempty_gold = formal.validate_gold(selected, tables)
    sources = [formal.DATA / "test.jsonl", formal.DATA / "test.tables.jsonl",
               formal.DATA / "test.db", ROOT / "sample_manifest.json", formal.E4,
               formal.ORIGINAL, FORMAL_RUNNER, Path(__file__),
               FORMAL_RUNNER.with_name("prepare_knowledge.cjs"), formal.PRIOR_SOURCE,
               formal.CANVAS_SOURCE]
    sources += sorted(formal.FLOW_SOURCE.parent.glob("*.json"))
    sources += sorted((LAB / "RSI/rsi-data-spring-boot/core-flow/src/main/java/com/horsecoder/common/whuTalent/core/flow/Skill/Where").glob("*.java"))
    sources.append(LAB / "RSI/rsi-data-spring-boot/core-flow/src/main/java/com/horsecoder/common/whuTalent/core/flow/FlowEngineService.java")
    sources.append(LAB / "RSI/rsi-data-platform/node_modules/typescript/package.json")
    frozen = {str(p.relative_to(LAB)): formal.digest(p) for p in sources}
    plan = {"run_id": args.run_id, "sample_count": len(selected), "arms": args.arms,
            "concurrency": args.concurrency,
            "mode": "execute" if args.execute else "offline_preflight",
            "model": args.model, "flow_url": args.flow_url, "prior_url": args.prior_url,
            "node": args.node, "timeout_seconds": args.timeout, "retries": args.retries,
            "sha256": frozen,
            "cohort": [{k: r[k] for k in ("position", "original_index", "example_id")}
                       for r in selected],
            "SCR": "safe_completed / 400 E4-success regression samples; E2/E3 also require Flow condition validation",
            "gold_nonempty_where_count": nonempty_gold}
    if not args.execute:
        prepared = formal.knowledge_cache(prepared_tables, args.node)
        print(json.dumps({"preflight": "OK", "count": len(selected), "arms": args.arms,
                          "gold_nonempty_where_count": nonempty_gold,
                          "canvas_tables_prepared": len(prepared), "calls_made": 0},
                         ensure_ascii=False, indent=2))
        return
    health = formal.service_health(args.prior_url, min(args.timeout, 15))
    run_dir = formal.RESULT / args.run_id
    run_dir.mkdir(parents=True, exist_ok=True)
    manifest_file = run_dir / "manifest.json"
    if not manifest_file.exists() and any(run_dir.iterdir()):
        raise ValueError("run directory is not empty but has no manifest")
    if manifest_file.exists():
        previous = json.loads(manifest_file.read_text(encoding="utf-8"))
        for field in ("sample_count", "sha256", "cohort", "arms", "model",
                      "flow_url", "prior_url", "timeout_seconds", "retries",
                      "concurrency", "node"):
            if previous[field] != plan[field]:
                raise ValueError(f"resume rejected: {field} changed")
    else:
        plan["created_at"] = datetime.now(timezone.utc).isoformat()
        plan["where_prior_health"] = health
        formal.atomic_json(manifest_file, plan)
    cache_file = run_dir / "knowledge_cache.json"
    if cache_file.exists():
        prepared = json.loads(cache_file.read_text(encoding="utf-8"))
    else:
        prepared = formal.knowledge_cache(prepared_tables, args.node)
        formal.atomic_json(cache_file, prepared)
    if set(prepared) != used or any(prepared[t]["knowledge"]["activeTableId"] != t for t in used):
        raise ValueError("canvas knowledge cache/table mismatch")
    cache_hash = formal.digest(cache_file)
    frozen_manifest = json.loads(manifest_file.read_text(encoding="utf-8"))
    if frozen_manifest.get("knowledge_cache_sha256") not in (None, cache_hash):
        raise ValueError("canvas knowledge cache hash changed")
    if frozen_manifest.get("knowledge_cache_sha256") is None:
        frozen_manifest["knowledge_cache_sha256"] = cache_hash
        formal.atomic_json(manifest_file, frozen_manifest)
    metrics_by_arm = {}
    for arm in args.arms:
        arm_dir = run_dir / arm
        arm_dir.mkdir(exist_ok=True)
        pending = [row for row in selected if not (arm_dir / f"{row['position']:03d}.json").exists()]
        print(f"{arm}: {len(pending)} pending / {len(selected)}", flush=True)
        run_pending(arm, pending, arm_dir, tables, prepared, args)
        records = []
        for row in selected:
            record = json.loads((arm_dir / f"{row['position']:03d}.json").read_text(encoding="utf-8"))
            if record["example_id"] != row["example_id"]:
                raise ValueError("resume result identity mismatch")
            records.append(record)
        evaluated, metrics = formal.evaluate(arm, records, selected, tables)
        metrics_by_arm[arm] = metrics
        formal.atomic_json(arm_dir / "metrics.json", metrics)
        (arm_dir / "evaluated.jsonl").write_text(
            "".join(json.dumps(r, ensure_ascii=False) + "\n" for r in evaluated), encoding="utf-8")
        report_dir = formal.REPORTS / args.run_id
        report_dir.mkdir(parents=True, exist_ok=True)
        (report_dir / f"{arm}.md").write_text(
            formal.report_text(metrics).replace("formal experiment", "retention regression")
            .replace("E0 replays prior E4 results and makes no new model calls. ", ""),
            encoding="utf-8")
        (report_dir / "comparison.md").write_text(comparison_text(metrics_by_arm), encoding="utf-8")
        print(json.dumps(metrics, ensure_ascii=False), flush=True)


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        sys.exit(1)
