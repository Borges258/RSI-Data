"""Offline contract tests. No model endpoint is contacted."""

import json
from pathlib import Path
import subprocess
import sys
import tempfile
from types import SimpleNamespace
import unittest

import run_ablation as experiment


class ExperimentTests(unittest.TestCase):
    def setUp(self):
        self.table = {"id": "fixture", "header": ["Name", "Score"],
                      "types": ["text", "real"], "rows": [["Alice", 2]]}
        self.sample = {"table_id": "fixture", "question": "Who scored 2?",
                       "sql": {"sel": 0, "agg": 0, "conds": [[1, 0, 2]]}}
        self.graph = {"structure": "{\"elements\":[],\"links\":[]}",
                      "knowledge": {"atoms": [{"id": "a"}], "patterns": [], "relations": []}}
        self.args = SimpleNamespace(prior_url="http://localhost:18086/api/where-prior",
                                    flow_url="http://localhost:30845/api/engine/flow",
                                    model="gpt-5.6-sol", concurrency=2)

    def test_ablation_request_contract(self):
        url, e1 = experiment.build_request("E1", self.sample, self.table, self.graph, self.args)
        self.assertTrue(url.endswith("/parse"))
        self.assertEqual(e1["knowledge"], self.graph["knowledge"])
        self.assertEqual(e1["question"], self.sample["question"])
        _, e2 = experiment.build_request("E2", self.sample, self.table, self.graph, self.args)
        _, e3 = experiment.build_request("E3", self.sample, self.table, self.graph, self.args)
        r2 = json.loads(e2["params"]["request"])
        r3 = json.loads(e3["params"]["request"])
        self.assertEqual(e2["flowId"], experiment.FLOW_ID)
        self.assertEqual(r2["knowledgeContext"]["atoms"], [])
        self.assertNotIn("structure", r2)
        self.assertEqual(r3["knowledge"], self.graph["knowledge"])
        self.assertEqual(r3["structure"], self.graph["structure"])
        for request in (r2, r3):
            self.assertEqual(request["question"], self.sample["question"])
            self.assertEqual(request["table"]["header"], self.table["header"])

    def test_baseline_safe_completion_requires_validation(self):
        row = {"position": 0, "original_index": 5, "example_id": "test:5:fixture", "sample": self.sample}
        valid = {"api_success": True, "json_valid": True, "structural_valid": True,
                 "validation_errors": [], "prediction_sql": {"conds": [[1, 0, 2]]},
                 "request": {"model": "gpt-5.6-sol"}}
        record = experiment.run_one("E0", row, {"fixture": self.table}, {}, {5: valid}, self.args)
        self.assertTrue(record["safe_completed"])
        broken = dict(valid, structural_valid=False)
        record = experiment.run_one("E0", row, {"fixture": self.table}, {}, {5: broken}, self.args)
        self.assertFalse(record["safe_completed"])
        broken = dict(valid, prediction_sql={"conds": [[1, 3, 2]]})
        record = experiment.run_one("E0", row, {"fixture": self.table}, {}, {5: broken}, self.args)
        self.assertFalse(record["safe_completed"])

    def test_order_casefold_and_duplicate_conditions(self):
        a = [[0, 0, "ALICE"], [1, 0, 2], [0, 0, "ALICE"]]
        b = [[1, 0, 2], [0, 0, "alice"], [0, 0, "alice"]]
        self.assertEqual(experiment.keys(a, True), experiment.keys(b, True))
        self.assertNotEqual(experiment.keys(a, True), experiment.keys(b[:-1], True))
        self.assertNotEqual(a, b)

    def test_flow_response_envelope(self):
        wrapped = {"data": {"value": {"result": '{"status":"SUCCEEDED","conds":[]}'}}}
        self.assertEqual(experiment.decode_flow(wrapped)["conds"], [])

    def test_bounded_runner_writes_only_given_local_rows(self):
        rows = [{"position": i, "original_index": i + 5,
                 "example_id": f"test:{i + 5}:fixture", "sample": self.sample} for i in range(2)]
        prior = {"api_success": True, "json_valid": True, "structural_valid": True,
                 "validation_errors": [], "prediction_sql": {"conds": [[1, 0, 2]]},
                 "request": {"model": "gpt-5.6-sol"}}
        with tempfile.TemporaryDirectory(dir=experiment.ROOT / "experiment") as path:
            directory = Path(path)
            experiment.run_pending("E0", rows, directory, {"fixture": self.table}, {},
                                   {5: prior, 6: prior}, self.args)
            self.assertEqual(sorted(p.name for p in directory.glob("*.json")), ["000.json", "001.json"])
            self.assertTrue(all(json.loads(p.read_text(encoding="utf-8"))["safe_completed"]
                                for p in directory.glob("*.json")))

    def test_cohort_and_canvas_cache(self):
        selected = experiment.cohort()
        self.assertEqual(len(selected), 410)
        table_id = selected[0]["sample"]["table_id"]
        table = next(t for t in experiment.lines(experiment.DATA / "test.tables.jsonl") if t["id"] == table_id)
        prepared = experiment.knowledge_cache({table_id: table}, "node")
        self.assertEqual(prepared[table_id]["knowledge"]["activeTableId"], table_id)

    def test_default_is_offline_and_creates_no_run(self):
        run_id = "offline_contract_test_no_execute"
        self.assertFalse((experiment.RESULT / run_id).exists())
        result = subprocess.run([sys.executable, str(Path(experiment.__file__)), "--run-id", run_id],
                                capture_output=True, text=True, encoding="utf-8", timeout=60, check=True)
        self.assertEqual(json.loads(result.stdout)["calls_made"], 0)
        self.assertFalse((experiment.RESULT / run_id).exists())


if __name__ == "__main__":
    unittest.main()
