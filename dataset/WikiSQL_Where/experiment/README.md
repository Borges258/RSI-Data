# WikiSQL WHERE formal 2×2 experiment

The runner is **offline by default**. Only `--execute` contacts Spring Boot and may spend model tokens. It does not schedule itself.

## Arms

| Arm | Prior knowledge | WHERE Skill Flow | Execution |
| --- | --- | --- | --- |
| E0 | No | No | Reuse frozen E4 predictions; no new model call |
| E1 | Canvas knowledge | No | `where-prior /parse` |
| E2 | Empty prior | Yes | `WIKISQL_WHERE_GENERATION` |
| E3 | Canvas knowledge | Yes | `WIKISQL_WHERE_GENERATION` |

The valid cohort is `../data/test.jsonl` (410 rows); 186 excluded audit rows are never in an evaluation denominator. `../data/test.tables.jsonl` and `../data/test.db` provide tables and execution data. E0 source predictions are frozen at `../../WikiSQL/results/formal_test/test/E4/evaluated.jsonl`. The runner aligns original IDs with the current cohort and compares every repaired Gold entry before any live call.

## Manual use (PowerShell, from `F:\Agent_LAB`)

```powershell
python dataset/WikiSQL_Where/experiment/run_ablation.py --run-id formal_where_v1
```

This performs the full offline preflight, prints the sample count, and writes no result. Start the existing where-prior service with its `.env` model configuration and LLM enabled; start the existing FlowEngine service. Then, only when ready to spend tokens:

```powershell
python dataset/WikiSQL_Where/experiment/run_ablation.py --run-id formal_where_v1 --execute
```

The default is two concurrent sample requests and zero automatic retries. Override conservatively with `--concurrency 1` through `4`. Use the same run ID and arguments to resume; completed sample files are skipped. For a deliberately separate configuration, use a new run ID. Endpoint overrides: `--prior-url http://127.0.0.1:18086/api/where-prior` and `--flow-url http://127.0.0.1:30845/api/engine/flow`.

`--execute` first checks where-prior `/health` for READY, enabled and configured. It stops if a completed response reports a model other than `gpt-5.6-sol`. A timeout can occur after the provider has already received a request; the default of zero retries avoids automatic duplicate spend.

## Reproducibility and caching

`result/<run-id>/manifest.json` freezes the data, Gold audits, E4 source, canvas fixture, Flow JSON, Java Skill sources, script hashes, cohort IDs, model, URLs and execution settings. Each arm gets one raw JSON per sample plus `evaluated.jsonl` and `metrics.json`. Reports are written under `reports/<run-id>/`.

Canvas knowledge and structure are prepared once per distinct table, stored in `knowledge_cache.json` with a hash in the manifest, and reused across samples and resumptions. The existing where-prior `/skill` endpoint sends a stable question/table/knowledge prefix before stage-specific data, so the provider can cache repeated prompt prefixes within a sample. The runner reports provider-returned cached token counts; it never assumes a cache hit or estimates token savings. E1 uses one model request per sample and cannot guarantee a shared provider cache across different questions.

SCR is successful generation with valid WikiSQL `conds` divided by **all 410 valid samples**. E0 also requires the old E4 JSON and structural validation flags; E1 requires a successful `/parse` response and local condition validation; E2/E3 additionally require completed Flow execution and `WHERE_CONDITION_VALIDATE.valid=true`. This gives the same denominator but different available validation evidence, which is retained in raw records. Unsuccessful, malformed, or model-mismatched responses count as non-completions.

Strict structure preserves condition order and case. Normalized structure uses Unicode `casefold()` and treats AND conditions as a multiset, retaining duplicates. WHERE execution agreement compares selected row IDs in the supplied SQLite database, with both-empty agreement listed separately. E1/E2/E3 do not predict `sel` or `agg`; therefore official full-SQL EX is unavailable and is not replaced by WHERE execution agreement.

## Offline checks

```powershell
python -m unittest discover -s dataset/WikiSQL_Where/experiment -p 'test_*.py' -v
```

These checks do not contact the model endpoint.
