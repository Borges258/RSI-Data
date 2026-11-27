"""Focused regression tests for the supplemental E3 semantic evaluator."""

from pathlib import Path
import sqlite3
import tempfile
import unittest

from evaluate_semantic import WhereEngine, classify, condition_key, multiset, summarize


ROOT = Path(__file__).resolve().parent


class SemanticEvaluationTest(unittest.TestCase):
    def test_unicode_casefold_and_duplicate_multiset(self):
        self.assertEqual(condition_key([0, 0, "Straße"]), condition_key([0, 0, "STRASSE"]))
        self.assertNotEqual(multiset([[0, 0, "A"], [0, 0, "A"]], True),
                            multiset([[0, 0, "a"]], True))
        self.assertNotEqual(condition_key([0, 0, "A B"]), condition_key([0, 0, "A  B"]))

    def test_order_and_empty_set_are_not_redundancy_proof(self):
        with tempfile.TemporaryDirectory() as directory:
            db_path = Path(directory) / "test.db"
            db = sqlite3.connect(db_path)
            db.execute('CREATE TABLE "table_t" (col0 text, col1 text)')
            db.executemany('INSERT INTO "table_t" VALUES (?, ?)',
                           [("a", "yes"), ("b", "no")])
            db.commit()
            db.close()
            engine = WhereEngine(db_path)
            try:
                base = {"record_number": 1, "example_id": "fixture", "table_id": "t",
                        "status": "SUCCEEDED", "completed": True, "exact": False}
                ordered = classify({**base, "gold": [[0, 0, "A"], [1, 0, "Yes"]],
                                    "predicted": [[1, 0, "Yes"], [0, 0, "A"]]}, engine)
                self.assertEqual(ordered["classification"], "NORMALIZED_MATCH")
                redundant = classify({**base, "gold": [[0, 0, "A"]],
                                      "predicted": [[0, 0, "A"], [1, 0, "Yes"]]}, engine)
                self.assertEqual(redundant["classification"], "REDUNDANT_BUT_EQUIVALENT")
                self.assertEqual(redundant["redundancy_evidence"]["common_row_count"], 1)
                empty = classify({**base, "gold": [[0, 0, "Missing"]],
                                  "predicted": [[0, 0, "Missing"], [1, 0, "Yes"]]}, engine)
                self.assertEqual(empty["classification"], "DENOTATION_MATCH_ONLY")
                self.assertFalse(empty["redundancy_proven_on_current_table"])
            finally:
                engine.close()

    def test_category_summary(self):
        with tempfile.TemporaryDirectory() as directory:
            db_path = Path(directory) / "test.db"
            db = sqlite3.connect(db_path)
            db.execute('CREATE TABLE "table_t" (col0 text, col1 text)')
            db.execute('INSERT INTO "table_t" VALUES (?, ?)', ("a", "yes"))
            db.commit()
            db.close()
            engine = WhereEngine(db_path)
            try:
                base = {"record_number": 1, "example_id": "fixture", "category": "W01",
                        "table_id": "t", "status": "SUCCEEDED", "completed": True,
                        "gold": [[0, 0, "a"]], "predicted": [[0, 0, "A"]], "exact": False}
                normalized = classify(base, engine)
                redundant = classify({**base, "record_number": 2,
                                      "predicted": [[0, 0, "A"], [1, 0, "Yes"]]}, engine)
            finally:
                engine.close()
            metrics = summarize([normalized, redundant])
            self.assertEqual(metrics["by_category"]["W01"]["count"], 2)
            self.assertEqual(metrics["by_category"]["W01"]["normalized_structure"], 1)
            self.assertEqual(metrics["proven_redundant_condition_count"], 1)


if __name__ == "__main__":
    unittest.main()
