#!/usr/bin/env python3
"""Offline integrity check and descriptive summary of the completed retention run."""

from __future__ import annotations

from collections import Counter
import json
from pathlib import Path
import sys

import run_regression as regression


def checked_arm(arm, selected, tables, run_dir):
    directory = run_dir / arm
    files = {p.name for p in directory.glob("[0-9][0-9][0-9].json")}
    expected = {f"{row['position']:03d}.json" for row in selected}
    if files != expected:
        raise ValueError(f"{arm}: missing/extra raw records: {len(expected - files)}/{len(files - expected)}")
    records = []
    for row in selected:
        raw = json.loads((directory / f"{row['position']:03d}.json").read_text(encoding="utf-8"))
        if (raw.get("arm") != arm or raw.get("position") != row["position"]
                or raw.get("example_id") != row["example_id"]
                or raw.get("question") != row["sample"]["question"]
                or raw.get("gold_sql") != row["sample"]["sql"]):
            raise ValueError(f"{arm}: raw identity/Gold mismatch at {row['position']}")
        records.append(raw)
    evaluated, metrics = regression.formal.evaluate(arm, records, selected, tables)
    saved_metrics = json.loads((directory / "metrics.json").read_text(encoding="utf-8"))
    saved_evaluated = list(regression.formal.lines(directory / "evaluated.jsonl"))
    if metrics != saved_metrics or evaluated != saved_evaluated:
        raise ValueError(f"{arm}: saved evaluation differs from recomputation")
    return records, evaluated, metrics


def paired(left, right, key):
    before = [row[key] for row in left]
    after = [row[key] for row in right]
    return {"gained": sum(not x and y for x, y in zip(before, after)),
            "lost": sum(x and not y for x, y in zip(before, after)),
            "net": sum(after) - sum(before)}


def main():
    run_id = sys.argv[1] if len(sys.argv) == 2 else "retention_return_20261127"
    selected = regression.cohort()
    tables = {t["id"]: t for t in regression.formal.lines(regression.formal.DATA / "test.tables.jsonl")}
    run_dir = regression.formal.RESULT / run_id
    manifest = json.loads((run_dir / "manifest.json").read_text(encoding="utf-8"))
    if (manifest["sample_count"] != 400 or manifest["arms"] != ["E1", "E2", "E3"]
            or manifest["cohort"] != [{k: r[k] for k in ("position", "original_index", "example_id")}
                                      for r in selected]):
        raise ValueError("run manifest cohort/arms mismatch")
    for name, expected in manifest["sha256"].items():
        current = regression.formal.digest(regression.LAB / name)
        if current != expected:
            raise ValueError(f"frozen source changed: {name}")
    cache = run_dir / "knowledge_cache.json"
    if regression.formal.digest(cache) != manifest["knowledge_cache_sha256"]:
        raise ValueError("knowledge cache hash mismatch")
    records_by_arm = {}
    evaluated_by_arm = {}
    metrics_by_arm = {}
    for arm in manifest["arms"]:
        records, evaluated, metrics = checked_arm(arm, selected, tables, run_dir)
        records_by_arm[arm] = records
        evaluated_by_arm[arm] = evaluated
        metrics_by_arm[arm] = metrics

    # E4 was not run again. Re-evaluate its frozen WHERE conditions using the
    # identical local metric code to distinguish SQL EX from WHERE agreement.
    frozen = regression.formal.baseline(selected)
    e4_records = [regression.formal.run_one("E0", row, tables, {}, frozen, manifest_args(manifest))
                  for row in selected]
    e4_evaluated, e4_metrics = regression.formal.evaluate("E0", e4_records, selected, tables)
    groups = {"0": [], "1": [], "2": [], ">=3": []}
    for row in selected:
        count = len(row["sample"]["sql"]["conds"])
        groups[str(count) if count < 3 else ">=3"].append(row["position"])
    by_stratum = {}
    for stratum, positions in groups.items():
        by_stratum[stratum] = {"n": len(positions)}
        for arm, evaluated in evaluated_by_arm.items():
            by_stratum[stratum][arm] = {
                "safe_completed": sum(evaluated[i]["safe_completed"] for i in positions),
                "normalized": sum(evaluated[i]["normalized"] for i in positions),
                "where_rows": sum(evaluated[i]["where_execution_match"] for i in positions)}
    paired_results = {}
    for left_arm, right_arm, left, right in [
        ("E4_frozen", "E1", e4_evaluated, evaluated_by_arm["E1"]),
        ("E4_frozen", "E2", e4_evaluated, evaluated_by_arm["E2"]),
        ("E4_frozen", "E3", e4_evaluated, evaluated_by_arm["E3"]),
        ("E1", "E3", evaluated_by_arm["E1"], evaluated_by_arm["E3"]),
        ("E2", "E3", evaluated_by_arm["E2"], evaluated_by_arm["E3"]),
    ]:
        paired_results[f"{left_arm}->{right_arm}"] = {
            key: paired(left, right, key)
            for key in ("safe_completed", "normalized", "where_execution_match")}
    status_stages = {}
    for arm, evaluated in evaluated_by_arm.items():
        status_stages[arm] = {
            "status": dict(Counter(row["status"] for row in evaluated)),
            "failed_stage": dict(Counter(row["stage"] or "UNKNOWN" for row in evaluated
                                         if not row["safe_completed"])),
            "failed_example_ids": [row["example_id"] for row in evaluated if not row["safe_completed"]]}
    output = {"run_id": run_id, "verified": "400 unique, aligned raw/evaluated records per arm; recomputed metrics match; frozen source and cache hashes match",
              "cohort_count": 400, "e4_source": "frozen ex_correct=true and api_success=true",
              "e4_where_reference": e4_metrics, "arms": metrics_by_arm,
              "by_gold_condition_count": by_stratum, "paired": paired_results,
              "failures": status_stages,
              "recovery_events": manifest.get("recovery_events", [])}
    report_dir = regression.formal.REPORTS / run_id
    report_dir.mkdir(parents=True, exist_ok=True)
    regression.formal.atomic_json(report_dir / "regression_summary.json", output)
    print(json.dumps({"verified": output["verified"], "E4_reference": e4_metrics,
                      "arms": metrics_by_arm, "by_stratum": by_stratum,
                      "paired": paired_results, "failures": status_stages}, ensure_ascii=False))


def manifest_args(manifest):
    from types import SimpleNamespace
    return SimpleNamespace(model=manifest["model"])


if __name__ == "__main__":
    main()
