# E3 Pilot: per-sample execution evidence

Read-only diagnosis source snapshot. Stage output is the merged child Flow result captured in raw.jsonl; it is not the raw provider response. The exact HTTP prompt body and the provider raw reply were not persisted. No missing stage is reconstructed.

## Source versions

- dataset/WikiSQL_Where_Testline/results/e3_testline/raw.jsonl SHA-256: ed3f68b62e2cba0e981221a64731cfed78bb4434ebd2a0c3de067809edf989f7
- dataset/WikiSQL_Where_Testline/results/e3_testline/evaluated.jsonl SHA-256: 423384780f081a30931fd8a84a32f610aee10f9b56b2f4514609a54103542d6f
- dataset/WikiSQL_Where_Testline/results/e3_testline/compared.jsonl SHA-256: 1bd972ae1a7ac7194ee05bc34a6d97f27fa12f85a2e0855e5467d7c8ac054cee
- dataset/WikiSQL_Where_Testline/sample_manifest.json SHA-256: b8dd17029941d0fc3b1ecf2e0a6890364b5d00885327c2f9215167f55f8504fe
- RSI/rsi-data-spring-boot/core-flow/src/main/resources/flow/Where/flow-wikisql-where-generation.json SHA-256: 50a5a1e24cf601b588cb381ed9bb31ae212abd877670e532bcc177d1130c1091
- RSI/rsi-data-spring-boot/where-prior/target/where-prior-0.1.0.jar SHA-256: 6b6bd1726a2a8d933af3075ab37a0117bf2f64bbcc838b8fdf836c88f0fbae91
- RSI/rsi-data-platform/src/xpages/Login/test/wikisql_where_knowledge.tsx SHA-256: a3856011b1e56bb37fb1d54d7c927b9b7c02a1e6ccff235fc90de24c26371302

- dataset/WikiSQL_Where_Testline/run_e3.cjs SHA-256: 3f0ce440551369e415ed4e1533a7e3c7d0d84b0bf3806ca4aea322e4f9a04064
- dataset/WikiSQL_Where_Testline/evaluate_e3.cjs SHA-256: c9c2cc0a4fd8c7a3b2591ee1b33db603f1719d3ca1dc69cbd1036e7cc6573951
- dataset/WikiSQL/results/formal_test/test/E4/manifest.json SHA-256: 58b597423a5b228dbeca999fc69396897476648b16bd51393b34e97b366d8c4f

## Sample traces

### 01 W01 — test:12826:2-17817954-1

- Question: When Ilie Năstase beat runner-up Peter Fleming what year was it?
- Table: 2-17817954-1; headers: ["Location","Year","Champion","Runner-up","Score"]; types: ["text","real","text","text","text"]; rows: 5. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[2,0,"ilie năstase"],[3,0,"peter fleming"]]
- E4 predicted conds: [[2,0,"Ilie Năstase"],[3,0,"Peter Fleming"]]; E4 SELECT/agg: [1,0].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[2,0,"Ilie Năstase"],[3,0,"Peter Fleming"]].
- Comparison: logical-form WHERE true; SQLite WHERE row equality true (gold 1 rows, E3 1 rows).
- Knowledge context: 16 atoms, 10 patterns, 30 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 2,
  "conditions": [
    {
      "surface": "Ilie Năstase",
      "value": "Ilie Năstase",
      "start": 5,
      "end": 17
    },
    {
      "surface": "Peter Fleming",
      "value": "Peter Fleming",
      "start": 33,
      "end": 46
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "0079d15ee986b26c6fbe156495af15404404618528b588a2fdb369b1b9432398",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-17817954-1-field-0",
      "t-2-17817954-1-field-1",
      "t-2-17817954-1-field-2",
      "t-2-17817954-1-field-3",
      "t-2-17817954-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9569,
      "completion_tokens": 37,
      "total_tokens": 9606,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Ilie Năstase",
      "value": "Ilie Năstase",
      "start": 5,
      "end": 17,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?"
    },
    {
      "surface": "Peter Fleming",
      "value": "Peter Fleming",
      "start": 33,
      "end": 46,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?"
    }
  ],
  "count": 2
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "Ilie Năstase",
      "value": "Ilie Năstase",
      "start": 5,
      "end": 17,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?"
    },
    {
      "surface": "Peter Fleming",
      "value": "Peter Fleming",
      "start": 33,
      "end": 46,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Ilie Năstase",
      "value": "Ilie Năstase",
      "start": 5,
      "end": 17,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?",
      "columnIndex": 2
    },
    {
      "surface": "Peter Fleming",
      "value": "Peter Fleming",
      "start": 33,
      "end": 46,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?",
      "columnIndex": 3
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "77afa8cee8b36e8eeca088c346eaa42ec7f9c00f96b3430d0bd7fc11a456d03f",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-17817954-1-field-0",
      "t-2-17817954-1-field-1",
      "t-2-17817954-1-field-2",
      "t-2-17817954-1-field-3",
      "t-2-17817954-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10144,
      "completion_tokens": 11,
      "total_tokens": 10155,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Ilie Năstase",
      "value": "Ilie Năstase",
      "start": 5,
      "end": 17,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?",
      "columnIndex": 2,
      "operator": "="
    },
    {
      "surface": "Peter Fleming",
      "value": "Peter Fleming",
      "start": 33,
      "end": 46,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?",
      "columnIndex": 3,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "380f862c1f7f152c08b3e9359f17b3d6b5a7357637b062e5545d18edb0b50223",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-17817954-1-field-0",
      "t-2-17817954-1-field-1",
      "t-2-17817954-1-field-2",
      "t-2-17817954-1-field-3",
      "t-2-17817954-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10530,
      "completion_tokens": 11,
      "total_tokens": 10541,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Ilie Năstase",
      "value": "Ilie Năstase",
      "start": 5,
      "end": 17,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?",
      "columnIndex": 2,
      "operator": "="
    },
    {
      "surface": "Peter Fleming",
      "value": "Peter Fleming",
      "start": 33,
      "end": 46,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?",
      "columnIndex": 3,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "3b185dfa58566b559cc9c604617414d06a1245e543d7b874336159c2924d0669",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-17817954-1-field-0",
      "t-2-17817954-1-field-1",
      "t-2-17817954-1-field-2",
      "t-2-17817954-1-field-3",
      "t-2-17817954-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10888,
      "completion_tokens": 18,
      "total_tokens": 10906,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Ilie Năstase",
      "value": "Ilie Năstase",
      "start": 5,
      "end": 17,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "Peter Fleming",
      "value": "Peter Fleming",
      "start": 33,
      "end": 46,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?",
      "columnIndex": 3,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Ilie Năstase",
      "value": "Ilie Năstase",
      "start": 5,
      "end": 17,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "Peter Fleming",
      "value": "Peter Fleming",
      "start": 33,
      "end": 46,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?",
      "columnIndex": 3,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "Ilie Năstase",
      "value": "Ilie Năstase",
      "start": 5,
      "end": 17,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "Peter Fleming",
      "value": "Peter Fleming",
      "start": 33,
      "end": 46,
      "clause": "When Ilie Năstase beat runner-up Peter Fleming what year was it?",
      "columnIndex": 3,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      2,
      0,
      "Ilie Năstase"
    ],
    [
      3,
      0,
      "Peter Fleming"
    ]
  ]
}
```

### 02 W01 — test:13532:2-18403356-1

- Question: what is the silver when the nation is syria (syr) and gold is more than 0?
- Table: 2-18403356-1; headers: ["Rank","Nation","Gold","Silver","Bronze","Total"]; types: ["text","text","real","real","real","real"]; rows: 21. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[1,0,"syria (syr)"],[2,1,0]]
- E4 predicted conds: [[1,0,"Syria (SYR)"],[2,1,0]]; E4 SELECT/agg: [3,0].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[1,0,"Syria (SYR)"],[2,1,0]].
- Comparison: logical-form WHERE true; SQLite WHERE row equality true (gold 0 rows, E3 0 rows).
- Knowledge context: 17 atoms, 10 patterns, 32 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 2,
  "conditions": [
    {
      "surface": "syria (syr)",
      "value": "Syria (SYR)",
      "start": 38,
      "end": 49
    },
    {
      "surface": "0",
      "value": 0,
      "start": 72,
      "end": 73
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "a51934c81f46675aabca558b5b045250e4909fdc3fffee0d1e31bc1e31e2f62b",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-18403356-1-field-0",
      "t-2-18403356-1-field-1",
      "t-2-18403356-1-field-2",
      "t-2-18403356-1-field-3",
      "t-2-18403356-1-field-4",
      "t-2-18403356-1-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10034,
      "completion_tokens": 35,
      "total_tokens": 10069,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "syria (syr)",
      "value": "Syria (SYR)",
      "start": 38,
      "end": 49,
      "clause": "what is the silver when the nation is syria (syr)"
    },
    {
      "surface": "0",
      "value": 0,
      "start": 72,
      "end": 73,
      "clause": "and gold is more than 0?"
    }
  ],
  "count": 2
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "syria (syr)",
      "value": "Syria (SYR)",
      "start": 38,
      "end": 49,
      "clause": "what is the silver when the nation is syria (syr)"
    },
    {
      "surface": "0",
      "value": 0,
      "start": 72,
      "end": 73,
      "clause": "and gold is more than 0?"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "syria (syr)",
      "value": "Syria (SYR)",
      "start": 38,
      "end": 49,
      "clause": "what is the silver when the nation is syria (syr)",
      "columnIndex": 1
    },
    {
      "surface": "0",
      "value": 0,
      "start": 72,
      "end": 73,
      "clause": "and gold is more than 0?",
      "columnIndex": 2
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "444c1784f12bf379ac685b6dd4441ea2f40e5c08b929611cb563a9c1273bdd7a",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-18403356-1-field-0",
      "t-2-18403356-1-field-1",
      "t-2-18403356-1-field-2",
      "t-2-18403356-1-field-3",
      "t-2-18403356-1-field-4",
      "t-2-18403356-1-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10584,
      "completion_tokens": 11,
      "total_tokens": 10595,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "syria (syr)",
      "value": "Syria (SYR)",
      "start": 38,
      "end": 49,
      "clause": "what is the silver when the nation is syria (syr)",
      "columnIndex": 1,
      "operator": "="
    },
    {
      "surface": "0",
      "value": 0,
      "start": 72,
      "end": 73,
      "clause": "and gold is more than 0?",
      "columnIndex": 2,
      "operator": ">"
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "82fd80a72adbb097a192d55f2b9e86adb195b52eb7f4bf3bf55a1f5b2a592037",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-18403356-1-field-0",
      "t-2-18403356-1-field-1",
      "t-2-18403356-1-field-2",
      "t-2-18403356-1-field-3",
      "t-2-18403356-1-field-4",
      "t-2-18403356-1-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10966,
      "completion_tokens": 11,
      "total_tokens": 10977,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "syria (syr)",
      "value": "Syria (SYR)",
      "start": 38,
      "end": 49,
      "clause": "what is the silver when the nation is syria (syr)",
      "columnIndex": 1,
      "operator": "="
    },
    {
      "surface": "0",
      "value": 0,
      "start": 72,
      "end": 73,
      "clause": "and gold is more than 0?",
      "columnIndex": 2,
      "operator": ">"
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "8596a43069c60f52132b3fac600c82c5f8f6cd71d54b4eb312a38f1004ace60a",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-18403356-1-field-0",
      "t-2-18403356-1-field-1",
      "t-2-18403356-1-field-2",
      "t-2-18403356-1-field-3",
      "t-2-18403356-1-field-4",
      "t-2-18403356-1-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 11321,
      "completion_tokens": 15,
      "total_tokens": 11336,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "syria (syr)",
      "value": "Syria (SYR)",
      "start": 38,
      "end": 49,
      "clause": "what is the silver when the nation is syria (syr)",
      "columnIndex": 1,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "0",
      "value": 0,
      "start": 72,
      "end": 73,
      "clause": "and gold is more than 0?",
      "columnIndex": 2,
      "operator": ">",
      "grounded": true
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "syria (syr)",
      "value": "Syria (SYR)",
      "start": 38,
      "end": 49,
      "clause": "what is the silver when the nation is syria (syr)",
      "columnIndex": 1,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "0",
      "value": 0,
      "start": 72,
      "end": 73,
      "clause": "and gold is more than 0?",
      "columnIndex": 2,
      "operator": ">",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "syria (syr)",
      "value": "Syria (SYR)",
      "start": 38,
      "end": 49,
      "clause": "what is the silver when the nation is syria (syr)",
      "columnIndex": 1,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "0",
      "value": 0,
      "start": 72,
      "end": 73,
      "clause": "and gold is more than 0?",
      "columnIndex": 2,
      "operator": ">",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      1,
      0,
      "Syria (SYR)"
    ],
    [
      2,
      1,
      0
    ]
  ]
}
```

### 03 W02 — test:469:1-12226390-7

- Question: How many episodes had a title "the great mayoralty campaign"?
- Table: 1-12226390-7; headers: ["No. in series","No. in season","Title","Directed by","Written by","Original air date","Production code"]; types: ["real","real","text","text","text","text","real"]; rows: 25. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[2,0,"\"The Great Mayoralty Campaign\""]]
- E4 predicted conds: [[2,0,"the great mayoralty campaign"]]; E4 SELECT/agg: [2,3].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[2,0,"\"The Great Mayoralty Campaign\""]].
- Comparison: logical-form WHERE true; SQLite WHERE row equality true (gold 1 rows, E3 1 rows).
- Knowledge context: 18 atoms, 10 patterns, 34 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 1,
  "conditions": [
    {
      "surface": "\"the great mayoralty campaign\"",
      "value": "\"The Great Mayoralty Campaign\"",
      "start": 30,
      "end": 60
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "f9d9a4d4f6e0aed4814910a40c6599d74e8545e9a3f534cf27d9fbe208829ebb",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-1-12226390-7-field-0",
      "t-1-12226390-7-field-1",
      "t-1-12226390-7-field-2",
      "t-1-12226390-7-field-3",
      "t-1-12226390-7-field-4",
      "t-1-12226390-7-field-5",
      "t-1-12226390-7-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 11064,
      "completion_tokens": 30,
      "total_tokens": 11094,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "\"the great mayoralty campaign\"",
      "value": "\"The Great Mayoralty Campaign\"",
      "start": 30,
      "end": 60,
      "clause": "How many episodes had a title \"the great mayoralty campaign\"?"
    }
  ],
  "count": 1
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "\"the great mayoralty campaign\"",
      "value": "\"The Great Mayoralty Campaign\"",
      "start": 30,
      "end": 60,
      "clause": "How many episodes had a title \"the great mayoralty campaign\"?"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "\"the great mayoralty campaign\"",
      "value": "\"The Great Mayoralty Campaign\"",
      "start": 30,
      "end": 60,
      "clause": "How many episodes had a title \"the great mayoralty campaign\"?",
      "columnIndex": 2
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "dabeb3d758815fc361c73a46a35cbffb3eba909a62a214cc421c50d3f912ec74",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-1-12226390-7-field-0",
      "t-1-12226390-7-field-1",
      "t-1-12226390-7-field-2",
      "t-1-12226390-7-field-3",
      "t-1-12226390-7-field-4",
      "t-1-12226390-7-field-5",
      "t-1-12226390-7-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 11541,
      "completion_tokens": 9,
      "total_tokens": 11550,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "\"the great mayoralty campaign\"",
      "value": "\"The Great Mayoralty Campaign\"",
      "start": 30,
      "end": 60,
      "clause": "How many episodes had a title \"the great mayoralty campaign\"?",
      "columnIndex": 2,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "3901ac19b5e4e0c5dd98e5449bc85bdca8a7973ffb4ebc1a113eefbef7b5d590",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-1-12226390-7-field-0",
      "t-1-12226390-7-field-1",
      "t-1-12226390-7-field-2",
      "t-1-12226390-7-field-3",
      "t-1-12226390-7-field-4",
      "t-1-12226390-7-field-5",
      "t-1-12226390-7-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 11899,
      "completion_tokens": 9,
      "total_tokens": 11908,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "\"the great mayoralty campaign\"",
      "value": "\"The Great Mayoralty Campaign\"",
      "start": 30,
      "end": 60,
      "clause": "How many episodes had a title \"the great mayoralty campaign\"?",
      "columnIndex": 2,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "cce4ac3e2ef4281fc85408c7e9101b73c54a072e0da7e0a4294c8f0ee2f9f95a",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-1-12226390-7-field-0",
      "t-1-12226390-7-field-1",
      "t-1-12226390-7-field-2",
      "t-1-12226390-7-field-3",
      "t-1-12226390-7-field-4",
      "t-1-12226390-7-field-5",
      "t-1-12226390-7-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 12230,
      "completion_tokens": 17,
      "total_tokens": 12247,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "\"the great mayoralty campaign\"",
      "value": "\"The Great Mayoralty Campaign\"",
      "start": 30,
      "end": 60,
      "clause": "How many episodes had a title \"the great mayoralty campaign\"?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "\"the great mayoralty campaign\"",
      "value": "\"The Great Mayoralty Campaign\"",
      "start": 30,
      "end": 60,
      "clause": "How many episodes had a title \"the great mayoralty campaign\"?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "\"the great mayoralty campaign\"",
      "value": "\"The Great Mayoralty Campaign\"",
      "start": 30,
      "end": 60,
      "clause": "How many episodes had a title \"the great mayoralty campaign\"?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      2,
      0,
      "\"The Great Mayoralty Campaign\""
    ]
  ]
}
```

### 04 W02 — test:8998:2-1726637-1

- Question: What is the area (in km2) for theewaterskloof, whose population in 2011 was less than 108,790?
- Table: 2-1726637-1; headers: ["Name","Seat","Population (2011)","Area (km 2 )","Density (inhabitants/km 2 )"]; types: ["text","text","real","real","real"]; rows: 5. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[0,0,"theewaterskloof"],[2,2,"108,790"]]
- E4 predicted conds: [[0,0,"Theewaterskloof"],[2,2,108790]]; E4 SELECT/agg: [3,0].
- E3 final: status INCOMPLETE, stage WHERE_COMPLETENESS_CHECK, completed false, conds [].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 0 rows, E3 not reached rows).
- Knowledge context: 16 atoms, 10 patterns, 30 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 2,
  "conditions": [
    {
      "surface": "theewaterskloof",
      "value": "Theewaterskloof",
      "start": 30,
      "end": 45
    },
    {
      "surface": "108,790",
      "value": 108790,
      "start": 86,
      "end": 93
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "7da093604dbe013dca5411b04d2ac1ef50646f89b64e159f67500fbb790964c8",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-1726637-1-field-0",
      "t-2-1726637-1-field-1",
      "t-2-1726637-1-field-2",
      "t-2-1726637-1-field-3",
      "t-2-1726637-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9505,
      "completion_tokens": 38,
      "total_tokens": 9543,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "theewaterskloof",
      "value": "Theewaterskloof",
      "start": 30,
      "end": 45,
      "clause": "What is the area (in km2) for theewaterskloof"
    },
    {
      "surface": "108,790",
      "value": 108790,
      "start": 86,
      "end": 93,
      "clause": "whose population in 2011 was less than 108,790?"
    }
  ],
  "count": 2
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "INCOMPLETE",
  "complete": false,
  "missing": [
    {
      "surface": "2011",
      "value": "2011",
      "start": 67,
      "end": 71
    },
    {
      "surface": "108,790",
      "value": "108,790",
      "columnIndex": 2,
      "start": 86,
      "end": 93
    }
  ],
  "extra": [
    "108790"
  ],
  "conditions": [
    {
      "surface": "theewaterskloof",
      "value": "Theewaterskloof",
      "start": 30,
      "end": 45,
      "clause": "What is the area (in km2) for theewaterskloof"
    },
    {
      "surface": "108,790",
      "value": 108790,
      "start": 86,
      "end": 93,
      "clause": "whose population in 2011 was less than 108,790?"
    }
  ]
}
```

Not executed after termination: WHERE_COLUMN_MATCH, WHERE_OPERATOR_RECOGNIZE, WHERE_VALUE_EXTRACT, WHERE_SCHEMA_VALUE_MATCH, WHERE_VALUE_NORMALIZE, WHERE_CONDITION_VALIDATE, WHERE_CONDITION_ASSEMBLE.

### 05 W03 — test:5053:2-14176339-4

- Question: What city had a score of 20 in aigburth after 1907?
- Table: 2-14176339-4; headers: ["Score","Opposition","Venue","City","Year"]; types: ["real","text","text","text","real"]; rows: 7. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[4,1,1907],[0,1,20],[2,0,"aigburth"]]
- E4 predicted conds: [[0,0,20],[2,0,"aigburth"],[4,1,1907]]; E4 SELECT/agg: [3,0].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[0,0,20],[2,0,"Aigburth"],[4,1,1907]].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 1 rows, E3 0 rows).
- Knowledge context: 16 atoms, 10 patterns, 30 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 3,
  "conditions": [
    {
      "surface": "20",
      "value": "20",
      "start": 25,
      "end": 27
    },
    {
      "surface": "aigburth",
      "value": "Aigburth",
      "start": 31,
      "end": 39
    },
    {
      "surface": "1907",
      "value": "1907",
      "start": 46,
      "end": 50
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "c6a0f8a220c7e5c7263121b5bc633ccdf0a59c3ceb40cf3657bdc57b7325fa60",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-14176339-4-field-0",
      "t-2-14176339-4-field-1",
      "t-2-14176339-4-field-2",
      "t-2-14176339-4-field-3",
      "t-2-14176339-4-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9417,
      "completion_tokens": 41,
      "total_tokens": 9458,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "20",
      "value": "20",
      "start": 25,
      "end": 27,
      "clause": "What city had a score of 20 in aigburth after 1907?"
    },
    {
      "surface": "aigburth",
      "value": "Aigburth",
      "start": 31,
      "end": 39,
      "clause": "What city had a score of 20 in aigburth after 1907?"
    },
    {
      "surface": "1907",
      "value": "1907",
      "start": 46,
      "end": 50,
      "clause": "What city had a score of 20 in aigburth after 1907?"
    }
  ],
  "count": 3
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "20",
      "value": "20",
      "start": 25,
      "end": 27,
      "clause": "What city had a score of 20 in aigburth after 1907?"
    },
    {
      "surface": "aigburth",
      "value": "Aigburth",
      "start": 31,
      "end": 39,
      "clause": "What city had a score of 20 in aigburth after 1907?"
    },
    {
      "surface": "1907",
      "value": "1907",
      "start": 46,
      "end": 50,
      "clause": "What city had a score of 20 in aigburth after 1907?"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "20",
      "value": "20",
      "start": 25,
      "end": 27,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 0
    },
    {
      "surface": "aigburth",
      "value": "Aigburth",
      "start": 31,
      "end": 39,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 2
    },
    {
      "surface": "1907",
      "value": "1907",
      "start": 46,
      "end": 50,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 4
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "2a97ae9292bc1227c1a42200835bb62b6bfb7811bea77bfca6a4a95487f1e5c6",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-14176339-4-field-0",
      "t-2-14176339-4-field-1",
      "t-2-14176339-4-field-2",
      "t-2-14176339-4-field-3",
      "t-2-14176339-4-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10110,
      "completion_tokens": 13,
      "total_tokens": 10123,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "20",
      "value": "20",
      "start": 25,
      "end": 27,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 0,
      "operator": "="
    },
    {
      "surface": "aigburth",
      "value": "Aigburth",
      "start": 31,
      "end": 39,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 2,
      "operator": "="
    },
    {
      "surface": "1907",
      "value": "1907",
      "start": 46,
      "end": 50,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 4,
      "operator": ">"
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "699f9111f0a3a28d1edd76bb364edaebd16f457fcae5be1825420e2b3ece7eac",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-14176339-4-field-0",
      "t-2-14176339-4-field-1",
      "t-2-14176339-4-field-2",
      "t-2-14176339-4-field-3",
      "t-2-14176339-4-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10539,
      "completion_tokens": 13,
      "total_tokens": 10552,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "20",
      "value": 20,
      "start": 25,
      "end": 27,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 0,
      "operator": "="
    },
    {
      "surface": "aigburth",
      "value": "Aigburth",
      "start": 31,
      "end": 39,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 2,
      "operator": "="
    },
    {
      "surface": "1907",
      "value": 1907,
      "start": 46,
      "end": 50,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 4,
      "operator": ">"
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "1bb8d88ebd7ed7b0ca90d31cc502e633ac143b2b4e20e22d7c960baf51d78b48",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-14176339-4-field-0",
      "t-2-14176339-4-field-1",
      "t-2-14176339-4-field-2",
      "t-2-14176339-4-field-3",
      "t-2-14176339-4-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10940,
      "completion_tokens": 17,
      "total_tokens": 10957,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "20",
      "value": 20,
      "start": 25,
      "end": 27,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 0,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "aigburth",
      "value": "Aigburth",
      "start": 31,
      "end": 39,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "1907",
      "value": 1907,
      "start": 46,
      "end": 50,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 4,
      "operator": ">",
      "grounded": true
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "20",
      "value": 20,
      "start": 25,
      "end": 27,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 0,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "aigburth",
      "value": "Aigburth",
      "start": 31,
      "end": 39,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "1907",
      "value": 1907,
      "start": 46,
      "end": 50,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 4,
      "operator": ">",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "20",
      "value": 20,
      "start": 25,
      "end": 27,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 0,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "aigburth",
      "value": "Aigburth",
      "start": 31,
      "end": 39,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "1907",
      "value": 1907,
      "start": 46,
      "end": 50,
      "clause": "What city had a score of 20 in aigburth after 1907?",
      "columnIndex": 4,
      "operator": ">",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      0,
      0,
      20
    ],
    [
      2,
      0,
      "Aigburth"
    ],
    [
      4,
      1,
      1907
    ]
  ]
}
```

### 06 W03 — test:15139:2-12556269-1

- Question: What is the lowest number of bronze medals for a country with less than 1 gold and 0 silver?
- Table: 2-12556269-1; headers: ["Nation","Gold","Silver","Bronze","Total"]; types: ["text","real","real","real","real"]; rows: 6. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[1,0,1],[2,2,0]]
- E4 predicted conds: [[1,2,1],[2,0,0]]; E4 SELECT/agg: [3,2].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[1,2,1],[2,0,0]].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 0 rows, E3 2 rows).
- Knowledge context: 16 atoms, 10 patterns, 30 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 2,
  "conditions": [
    {
      "surface": "1",
      "value": "1",
      "start": 72,
      "end": 73
    },
    {
      "surface": "0",
      "value": "0",
      "start": 83,
      "end": 84
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "4c9c58540ecb6156a2a50df4fbf0ca83ec79178c1f116c3ae3c8d07dd2fc9446",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-12556269-1-field-0",
      "t-2-12556269-1-field-1",
      "t-2-12556269-1-field-2",
      "t-2-12556269-1-field-3",
      "t-2-12556269-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9354,
      "completion_tokens": 25,
      "total_tokens": 9379,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "1",
      "value": "1",
      "start": 72,
      "end": 73,
      "clause": "What is the lowest number of bronze medals for a country with less than 1 gold"
    },
    {
      "surface": "0",
      "value": "0",
      "start": 83,
      "end": 84,
      "clause": "and 0 silver?"
    }
  ],
  "count": 2
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "1",
      "value": "1",
      "start": 72,
      "end": 73,
      "clause": "What is the lowest number of bronze medals for a country with less than 1 gold"
    },
    {
      "surface": "0",
      "value": "0",
      "start": 83,
      "end": 84,
      "clause": "and 0 silver?"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "1",
      "value": "1",
      "start": 72,
      "end": 73,
      "clause": "What is the lowest number of bronze medals for a country with less than 1 gold",
      "columnIndex": 1
    },
    {
      "surface": "0",
      "value": "0",
      "start": 83,
      "end": 84,
      "clause": "and 0 silver?",
      "columnIndex": 2
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "8ce8ac4775a216978e4dbdb278a2ce2611657be26064d16f4c94eb5d5c7190a6",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-12556269-1-field-0",
      "t-2-12556269-1-field-1",
      "t-2-12556269-1-field-2",
      "t-2-12556269-1-field-3",
      "t-2-12556269-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9853,
      "completion_tokens": 11,
      "total_tokens": 9864,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "1",
      "value": "1",
      "start": 72,
      "end": 73,
      "clause": "What is the lowest number of bronze medals for a country with less than 1 gold",
      "columnIndex": 1,
      "operator": "<"
    },
    {
      "surface": "0",
      "value": "0",
      "start": 83,
      "end": 84,
      "clause": "and 0 silver?",
      "columnIndex": 2,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "a042221926e6cf0128b2c59b3e428f26ee426e8665feac422e1b980879443eda",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-12556269-1-field-0",
      "t-2-12556269-1-field-1",
      "t-2-12556269-1-field-2",
      "t-2-12556269-1-field-3",
      "t-2-12556269-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10214,
      "completion_tokens": 11,
      "total_tokens": 10225,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "1",
      "value": 1,
      "start": 72,
      "end": 73,
      "clause": "What is the lowest number of bronze medals for a country with less than 1 gold",
      "columnIndex": 1,
      "operator": "<"
    },
    {
      "surface": "0",
      "value": 0,
      "start": 83,
      "end": 84,
      "clause": "and 0 silver?",
      "columnIndex": 2,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "91e0d41223e7cb405564eccdac65e7a7cedde6aeaebc78bab8ccbdd08864eaca",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-12556269-1-field-0",
      "t-2-12556269-1-field-1",
      "t-2-12556269-1-field-2",
      "t-2-12556269-1-field-3",
      "t-2-12556269-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10541,
      "completion_tokens": 11,
      "total_tokens": 10552,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "1",
      "value": 1,
      "start": 72,
      "end": 73,
      "clause": "What is the lowest number of bronze medals for a country with less than 1 gold",
      "columnIndex": 1,
      "operator": "<",
      "grounded": true
    },
    {
      "surface": "0",
      "value": 0,
      "start": 83,
      "end": 84,
      "clause": "and 0 silver?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "1",
      "value": 1,
      "start": 72,
      "end": 73,
      "clause": "What is the lowest number of bronze medals for a country with less than 1 gold",
      "columnIndex": 1,
      "operator": "<",
      "grounded": true
    },
    {
      "surface": "0",
      "value": 0,
      "start": 83,
      "end": 84,
      "clause": "and 0 silver?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "1",
      "value": 1,
      "start": 72,
      "end": 73,
      "clause": "What is the lowest number of bronze medals for a country with less than 1 gold",
      "columnIndex": 1,
      "operator": "<",
      "grounded": true
    },
    {
      "surface": "0",
      "value": 0,
      "start": 83,
      "end": 84,
      "clause": "and 0 silver?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      1,
      2,
      1
    ],
    [
      2,
      0,
      0
    ]
  ]
}
```

### 07 W04 — test:4192:1-291768-1

- Question: what project was initially started before september 20, 1968
- Table: 1-291768-1; headers: ["#","Project","Shipyard","Laid down","Launched","Commissioned","Status"]; types: ["text","text","text","text","text","text","text"]; rows: 33. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[4,0,"September 20, 1968"]]
- E4 predicted conds: [[3,2,"september 20, 1968"]]; E4 SELECT/agg: [1,0].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[3,2,"September 20, 1968"]].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 1 rows, E3 31 rows).
- Knowledge context: 18 atoms, 10 patterns, 34 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 1,
  "conditions": [
    {
      "surface": "september 20, 1968",
      "value": "September 20, 1968",
      "start": 42,
      "end": 60
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "16894d8409277866fe65d9895046611f58f1b467f7728dc73ec37477824d0ab0",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-1-291768-1-field-0",
      "t-1-291768-1-field-1",
      "t-1-291768-1-field-2",
      "t-1-291768-1-field-3",
      "t-1-291768-1-field-4",
      "t-1-291768-1-field-5",
      "t-1-291768-1-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 12822,
      "completion_tokens": 30,
      "total_tokens": 12852,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "september 20, 1968",
      "value": "September 20, 1968",
      "start": 42,
      "end": 60,
      "clause": "what project was initially started before september 20, 1968"
    }
  ],
  "count": 1
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "september 20, 1968",
      "value": "September 20, 1968",
      "start": 42,
      "end": 60,
      "clause": "what project was initially started before september 20, 1968"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "september 20, 1968",
      "value": "September 20, 1968",
      "start": 42,
      "end": 60,
      "clause": "what project was initially started before september 20, 1968",
      "columnIndex": 3
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "93c34573905cf5aa9009dbf5d026cfce95775b5e816b587c09af60c53045c95a",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-1-291768-1-field-0",
      "t-1-291768-1-field-1",
      "t-1-291768-1-field-2",
      "t-1-291768-1-field-3",
      "t-1-291768-1-field-4",
      "t-1-291768-1-field-5",
      "t-1-291768-1-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 13282,
      "completion_tokens": 9,
      "total_tokens": 13291,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "september 20, 1968",
      "value": "September 20, 1968",
      "start": 42,
      "end": 60,
      "clause": "what project was initially started before september 20, 1968",
      "columnIndex": 3,
      "operator": "<"
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "3a3f6efa4540391605213e135299a672bdf70c73b7a4f34e92659096264f1469",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-1-291768-1-field-0",
      "t-1-291768-1-field-1",
      "t-1-291768-1-field-2",
      "t-1-291768-1-field-3",
      "t-1-291768-1-field-4",
      "t-1-291768-1-field-5",
      "t-1-291768-1-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 13632,
      "completion_tokens": 10,
      "total_tokens": 13642,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "september 20, 1968",
      "value": "September 20, 1968",
      "start": 42,
      "end": 60,
      "clause": "what project was initially started before september 20, 1968",
      "columnIndex": 3,
      "operator": "<"
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "a576be1338723b8b46ad06cd637d342ae800f89b1c2ecea63417f49b824e90c2",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-1-291768-1-field-0",
      "t-1-291768-1-field-1",
      "t-1-291768-1-field-2",
      "t-1-291768-1-field-3",
      "t-1-291768-1-field-4",
      "t-1-291768-1-field-5",
      "t-1-291768-1-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 13950,
      "completion_tokens": 16,
      "total_tokens": 13966,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "september 20, 1968",
      "value": "September 20, 1968",
      "start": 42,
      "end": 60,
      "clause": "what project was initially started before september 20, 1968",
      "columnIndex": 3,
      "operator": "<",
      "grounded": false
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "september 20, 1968",
      "value": "September 20, 1968",
      "start": 42,
      "end": 60,
      "clause": "what project was initially started before september 20, 1968",
      "columnIndex": 3,
      "operator": "<",
      "grounded": false
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "september 20, 1968",
      "value": "September 20, 1968",
      "start": 42,
      "end": 60,
      "clause": "what project was initially started before september 20, 1968",
      "columnIndex": 3,
      "operator": "<",
      "grounded": false
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      3,
      2,
      "September 20, 1968"
    ]
  ]
}
```

### 08 W04 — test:4746:2-10746200-9

- Question: What was the score when geelong was the away team?
- Table: 2-10746200-9; headers: ["Home team","Home team score","Away team","Away team score","Venue","Crowd","Date"]; types: ["text","text","text","text","text","real","text"]; rows: 6. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[0,0,"geelong"]]
- E4 predicted conds: [[2,0,"geelong"]]; E4 SELECT/agg: [3,0].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[2,0,"Geelong"]].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 1 rows, E3 0 rows).
- Knowledge context: 18 atoms, 10 patterns, 34 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 1,
  "conditions": [
    {
      "surface": "geelong",
      "value": "Geelong",
      "start": 24,
      "end": 31
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "5968ff371b5e4ff89246e363b4fb4d51d7206f9f60dce4e8ed18112877e95a35",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-10746200-9-field-0",
      "t-2-10746200-9-field-1",
      "t-2-10746200-9-field-2",
      "t-2-10746200-9-field-3",
      "t-2-10746200-9-field-4",
      "t-2-10746200-9-field-5",
      "t-2-10746200-9-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10160,
      "completion_tokens": 19,
      "total_tokens": 10179,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "geelong",
      "value": "Geelong",
      "start": 24,
      "end": 31,
      "clause": "What was the score when geelong was the away team?"
    }
  ],
  "count": 1
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "geelong",
      "value": "Geelong",
      "start": 24,
      "end": 31,
      "clause": "What was the score when geelong was the away team?"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "geelong",
      "value": "Geelong",
      "start": 24,
      "end": 31,
      "clause": "What was the score when geelong was the away team?",
      "columnIndex": 2
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "65b66b8cd1eaa6ac0263ad99c8b9aeb0193c46de6a35847d437105c1c38413c0",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-10746200-9-field-0",
      "t-2-10746200-9-field-1",
      "t-2-10746200-9-field-2",
      "t-2-10746200-9-field-3",
      "t-2-10746200-9-field-4",
      "t-2-10746200-9-field-5",
      "t-2-10746200-9-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10585,
      "completion_tokens": 9,
      "total_tokens": 10594,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "geelong",
      "value": "Geelong",
      "start": 24,
      "end": 31,
      "clause": "What was the score when geelong was the away team?",
      "columnIndex": 2,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "373089bdbd36a8e7eea8e9dee829c4ff3a4c0541abdf6b74aff89747d76e196b",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-10746200-9-field-0",
      "t-2-10746200-9-field-1",
      "t-2-10746200-9-field-2",
      "t-2-10746200-9-field-3",
      "t-2-10746200-9-field-4",
      "t-2-10746200-9-field-5",
      "t-2-10746200-9-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10936,
      "completion_tokens": 9,
      "total_tokens": 10945,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "geelong",
      "value": "Geelong",
      "start": 24,
      "end": 31,
      "clause": "What was the score when geelong was the away team?",
      "columnIndex": 2,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "080f378c882769131f620df1c1cc8bd9c94aa5e1da3846167c88033497ceccbb",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-10746200-9-field-0",
      "t-2-10746200-9-field-1",
      "t-2-10746200-9-field-2",
      "t-2-10746200-9-field-3",
      "t-2-10746200-9-field-4",
      "t-2-10746200-9-field-5",
      "t-2-10746200-9-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 11253,
      "completion_tokens": 11,
      "total_tokens": 11264,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "geelong",
      "value": "Geelong",
      "start": 24,
      "end": 31,
      "clause": "What was the score when geelong was the away team?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": false
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "geelong",
      "value": "Geelong",
      "start": 24,
      "end": 31,
      "clause": "What was the score when geelong was the away team?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": false
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "geelong",
      "value": "Geelong",
      "start": 24,
      "end": 31,
      "clause": "What was the score when geelong was the away team?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": false
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      2,
      0,
      "Geelong"
    ]
  ]
}
```

### 09 W05 — test:4931:2-10783853-16

- Question: What is the smallest crowd with fitzroy as the home team?
- Table: 2-10783853-16; headers: ["Home team","Home team score","Away team","Away team score","Venue","Crowd","Date"]; types: ["text","text","text","text","text","real","text"]; rows: 6. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[2,0,"fitzroy"]]
- E4 predicted conds: [[0,0,"fitzroy"]]; E4 SELECT/agg: [5,2].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[0,0,"Fitzroy"]].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 1 rows, E3 0 rows).
- Knowledge context: 18 atoms, 10 patterns, 34 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 1,
  "conditions": [
    {
      "surface": "fitzroy",
      "value": "Fitzroy",
      "start": 32,
      "end": 39
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "f8e437ecd42b3a8d7a4bab087e66823a105b3ea2b12f685949e146b7915b0fcd",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-10783853-16-field-0",
      "t-2-10783853-16-field-1",
      "t-2-10783853-16-field-2",
      "t-2-10783853-16-field-3",
      "t-2-10783853-16-field-4",
      "t-2-10783853-16-field-5",
      "t-2-10783853-16-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10176,
      "completion_tokens": 21,
      "total_tokens": 10197,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "fitzroy",
      "value": "Fitzroy",
      "start": 32,
      "end": 39,
      "clause": "What is the smallest crowd with fitzroy as the home team?"
    }
  ],
  "count": 1
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "fitzroy",
      "value": "Fitzroy",
      "start": 32,
      "end": 39,
      "clause": "What is the smallest crowd with fitzroy as the home team?"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "fitzroy",
      "value": "Fitzroy",
      "start": 32,
      "end": 39,
      "clause": "What is the smallest crowd with fitzroy as the home team?",
      "columnIndex": 0
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "46dc3046592b40a1710fdc37b5c636963a173ed498e3510657c76bbe0bb6d970",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-10783853-16-field-0",
      "t-2-10783853-16-field-1",
      "t-2-10783853-16-field-2",
      "t-2-10783853-16-field-3",
      "t-2-10783853-16-field-4",
      "t-2-10783853-16-field-5",
      "t-2-10783853-16-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10615,
      "completion_tokens": 9,
      "total_tokens": 10624,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "fitzroy",
      "value": "Fitzroy",
      "start": 32,
      "end": 39,
      "clause": "What is the smallest crowd with fitzroy as the home team?",
      "columnIndex": 0,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "c946de769e3bf4740312d8c3cad32a13d0f3a2c5b6fdbd451a8f3ae78e063e69",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-10783853-16-field-0",
      "t-2-10783853-16-field-1",
      "t-2-10783853-16-field-2",
      "t-2-10783853-16-field-3",
      "t-2-10783853-16-field-4",
      "t-2-10783853-16-field-5",
      "t-2-10783853-16-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10964,
      "completion_tokens": 9,
      "total_tokens": 10973,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "fitzroy",
      "value": "Fitzroy",
      "start": 32,
      "end": 39,
      "clause": "What is the smallest crowd with fitzroy as the home team?",
      "columnIndex": 0,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "a3c583883f45434ed0168c4b0d49c255bdee167b5215ea8e3904966be90e4d18",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-10783853-16-field-0",
      "t-2-10783853-16-field-1",
      "t-2-10783853-16-field-2",
      "t-2-10783853-16-field-3",
      "t-2-10783853-16-field-4",
      "t-2-10783853-16-field-5",
      "t-2-10783853-16-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 11288,
      "completion_tokens": 12,
      "total_tokens": 11300,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "fitzroy",
      "value": "Fitzroy",
      "start": 32,
      "end": 39,
      "clause": "What is the smallest crowd with fitzroy as the home team?",
      "columnIndex": 0,
      "operator": "=",
      "grounded": false
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "fitzroy",
      "value": "Fitzroy",
      "start": 32,
      "end": 39,
      "clause": "What is the smallest crowd with fitzroy as the home team?",
      "columnIndex": 0,
      "operator": "=",
      "grounded": false
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "fitzroy",
      "value": "Fitzroy",
      "start": 32,
      "end": 39,
      "clause": "What is the smallest crowd with fitzroy as the home team?",
      "columnIndex": 0,
      "operator": "=",
      "grounded": false
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      0,
      0,
      "Fitzroy"
    ]
  ]
}
```

### 10 W05 — test:15152:2-13065827-1

- Question: With a Division II of Portsmouth, what is the Division IV?
- Table: 2-13065827-1; headers: ["Year","Division I","Division II","Division III","Division IV","Division V"]; types: ["real","text","text","text","text","text"]; rows: 8. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[3,0,"portsmouth"]]
- E4 predicted conds: [[2,0,"Portsmouth"]]; E4 SELECT/agg: [4,0].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[3,0,"Portsmouth"]].
- Comparison: logical-form WHERE true; SQLite WHERE row equality true (gold 1 rows, E3 1 rows).
- Knowledge context: 17 atoms, 10 patterns, 32 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 1,
  "conditions": [
    {
      "surface": "Portsmouth",
      "value": "Portsmouth",
      "start": 22,
      "end": 32
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "8bc6d5252a99f144c852effbbe6d9a90264f126c88011418729105adb367521b",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-13065827-1-field-0",
      "t-2-13065827-1-field-1",
      "t-2-13065827-1-field-2",
      "t-2-13065827-1-field-3",
      "t-2-13065827-1-field-4",
      "t-2-13065827-1-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9794,
      "completion_tokens": 19,
      "total_tokens": 9813,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Portsmouth",
      "value": "Portsmouth",
      "start": 22,
      "end": 32,
      "clause": "With a Division II of Portsmouth"
    }
  ],
  "count": 1
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "Portsmouth",
      "value": "Portsmouth",
      "start": 22,
      "end": 32,
      "clause": "With a Division II of Portsmouth"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Portsmouth",
      "value": "Portsmouth",
      "start": 22,
      "end": 32,
      "clause": "With a Division II of Portsmouth",
      "columnIndex": 3
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "9d8d27df3595de034d9a587136c8689ee905910d44562b0c6db3b900859799f1",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-13065827-1-field-0",
      "t-2-13065827-1-field-1",
      "t-2-13065827-1-field-2",
      "t-2-13065827-1-field-3",
      "t-2-13065827-1-field-4",
      "t-2-13065827-1-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10182,
      "completion_tokens": 9,
      "total_tokens": 10191,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Portsmouth",
      "value": "Portsmouth",
      "start": 22,
      "end": 32,
      "clause": "With a Division II of Portsmouth",
      "columnIndex": 3,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "bded90d1939040dc3cfdea9c97c95455c030bc4560fe3a4260d97fd153d15995",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-13065827-1-field-0",
      "t-2-13065827-1-field-1",
      "t-2-13065827-1-field-2",
      "t-2-13065827-1-field-3",
      "t-2-13065827-1-field-4",
      "t-2-13065827-1-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10509,
      "completion_tokens": 9,
      "total_tokens": 10518,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Portsmouth",
      "value": "Portsmouth",
      "start": 22,
      "end": 32,
      "clause": "With a Division II of Portsmouth",
      "columnIndex": 3,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "5cfb01a28a3b929ab4237174857ace59c8cf51be4ec4b8f0e89bfad49c68f78c",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-13065827-1-field-0",
      "t-2-13065827-1-field-1",
      "t-2-13065827-1-field-2",
      "t-2-13065827-1-field-3",
      "t-2-13065827-1-field-4",
      "t-2-13065827-1-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10802,
      "completion_tokens": 11,
      "total_tokens": 10813,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Portsmouth",
      "value": "Portsmouth",
      "start": 22,
      "end": 32,
      "clause": "With a Division II of Portsmouth",
      "columnIndex": 3,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Portsmouth",
      "value": "Portsmouth",
      "start": 22,
      "end": 32,
      "clause": "With a Division II of Portsmouth",
      "columnIndex": 3,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "Portsmouth",
      "value": "Portsmouth",
      "start": 22,
      "end": 32,
      "clause": "With a Division II of Portsmouth",
      "columnIndex": 3,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      3,
      0,
      "Portsmouth"
    ]
  ]
}
```

### 11 W06 — test:10989:2-11086216-2

- Question: In what category was character Jacqui Mcqueen nominated before 2011?
- Table: 2-11086216-2; headers: ["Year","Result","Award","Category","Film or series","Character"]; types: ["real","text","text","text","text","text"]; rows: 18. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[5,0,"jacqui mcqueen"],[0,2,2011]]
- E4 predicted conds: [[5,0,"Jacqui McQueen"],[1,0,"Nominated"],[0,2,2011]]; E4 SELECT/agg: [3,0].
- E3 final: status INCOMPLETE, stage WHERE_COMPLETENESS_CHECK, completed false, conds [].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 4 rows, E3 not reached rows).
- Knowledge context: 17 atoms, 10 patterns, 32 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 2,
  "conditions": [
    {
      "surface": "Jacqui Mcqueen",
      "value": "Jacqui McQueen",
      "start": 31,
      "end": 45
    },
    {
      "surface": "2011",
      "value": 2011,
      "start": 63,
      "end": 67
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "4f874d7d9017777a29a8fc995d610c1fc462efc3bb9214bd20d1179b2afc6b24",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-11086216-2-field-0",
      "t-2-11086216-2-field-1",
      "t-2-11086216-2-field-2",
      "t-2-11086216-2-field-3",
      "t-2-11086216-2-field-4",
      "t-2-11086216-2-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10078,
      "completion_tokens": 33,
      "total_tokens": 10111,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Jacqui Mcqueen",
      "value": "Jacqui McQueen",
      "start": 31,
      "end": 45,
      "clause": "In what category was character Jacqui Mcqueen nominated before 2011?"
    },
    {
      "surface": "2011",
      "value": 2011,
      "start": 63,
      "end": 67,
      "clause": "In what category was character Jacqui Mcqueen nominated before 2011?"
    }
  ],
  "count": 2
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "INCOMPLETE",
  "complete": false,
  "missing": [
    {
      "surface": "nominated",
      "value": "Nominated",
      "columnIndex": 1,
      "start": 46,
      "end": 55
    }
  ],
  "extra": [],
  "conditions": [
    {
      "surface": "Jacqui Mcqueen",
      "value": "Jacqui McQueen",
      "start": 31,
      "end": 45,
      "clause": "In what category was character Jacqui Mcqueen nominated before 2011?"
    },
    {
      "surface": "2011",
      "value": 2011,
      "start": 63,
      "end": 67,
      "clause": "In what category was character Jacqui Mcqueen nominated before 2011?"
    }
  ]
}
```

Not executed after termination: WHERE_COLUMN_MATCH, WHERE_OPERATOR_RECOGNIZE, WHERE_VALUE_EXTRACT, WHERE_SCHEMA_VALUE_MATCH, WHERE_VALUE_NORMALIZE, WHERE_CONDITION_VALIDATE, WHERE_CONDITION_ASSEMBLE.

### 12 W06 — test:15233:2-12378197-1

- Question: Who is retired with eyes of dark brown, and black hair?
- Table: 2-12378197-1; headers: ["Name","Hair Color:","Eye Color:","Release Date","Retired?"]; types: ["text","text","text","text","text"]; rows: 34. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[2,0,"dark brown"],[1,0,"black"]]
- E4 predicted conds: [[1,0,"black"],[2,0,"dark brown"],[4,0,"yes"]]; E4 SELECT/agg: [0,0].
- E3 final: status INCOMPLETE, stage WHERE_COMPLETENESS_CHECK, completed false, conds [].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 2 rows, E3 not reached rows).
- Knowledge context: 16 atoms, 10 patterns, 30 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 3,
  "conditions": [
    {
      "surface": "retired",
      "value": "Yes",
      "start": 7,
      "end": 14
    },
    {
      "surface": "dark brown",
      "value": "Dark Brown",
      "start": 28,
      "end": 38
    },
    {
      "surface": "black",
      "value": "Black",
      "start": 44,
      "end": 49
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "51f9fc7bdbb14337e8a339f5cac4c1e49b854bf08c75332bb684f36d5830df5b",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-12378197-1-field-0",
      "t-2-12378197-1-field-1",
      "t-2-12378197-1-field-2",
      "t-2-12378197-1-field-3",
      "t-2-12378197-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10341,
      "completion_tokens": 36,
      "total_tokens": 10377,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "retired",
      "value": "Yes",
      "start": 7,
      "end": 14,
      "clause": "Who is retired with eyes of dark brown,"
    },
    {
      "surface": "dark brown",
      "value": "Dark Brown",
      "start": 28,
      "end": 38,
      "clause": "Who is retired with eyes of dark brown,"
    },
    {
      "surface": "black",
      "value": "Black",
      "start": 44,
      "end": 49,
      "clause": "and black hair?"
    }
  ],
  "count": 3
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "INCOMPLETE",
  "complete": false,
  "missing": [],
  "extra": [
    "yes"
  ],
  "conditions": [
    {
      "surface": "retired",
      "value": "Yes",
      "start": 7,
      "end": 14,
      "clause": "Who is retired with eyes of dark brown,"
    },
    {
      "surface": "dark brown",
      "value": "Dark Brown",
      "start": 28,
      "end": 38,
      "clause": "Who is retired with eyes of dark brown,"
    },
    {
      "surface": "black",
      "value": "Black",
      "start": 44,
      "end": 49,
      "clause": "and black hair?"
    }
  ]
}
```

Not executed after termination: WHERE_COLUMN_MATCH, WHERE_OPERATOR_RECOGNIZE, WHERE_VALUE_EXTRACT, WHERE_SCHEMA_VALUE_MATCH, WHERE_VALUE_NORMALIZE, WHERE_CONDITION_VALIDATE, WHERE_CONDITION_ASSEMBLE.

### 13 W07 — test:2697:1-22670216-1

- Question: If the location is Brooklyn, Michigan and the pole position is Bobby Unser, what is the RND total number?
- Table: 1-22670216-1; headers: ["Rnd","Date","Race Name","Length","Track","Location","Pole Position","Winning Driver"]; types: ["real","text","text","text","text","text","text","text"]; rows: 15. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[5,0,"Brooklyn, Michigan"],[6,0,"Bobby Unser"]]
- E4 predicted conds: [[5,0,"Brooklyn , Michigan"],[6,0,"Bobby Unser"]]; E4 SELECT/agg: [0,4].
- E3 final: status INCOMPLETE, stage WHERE_COMPLETENESS_CHECK, completed false, conds [].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 0 rows, E3 not reached rows).
- Knowledge context: 19 atoms, 10 patterns, 36 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 2,
  "conditions": [
    {
      "surface": "Brooklyn, Michigan",
      "value": "Brooklyn , Michigan",
      "start": 19,
      "end": 37
    },
    {
      "surface": "Bobby Unser",
      "value": "Bobby Unser",
      "start": 63,
      "end": 74
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "68d1ad292eef9fe18842c2c9bc200b4e539b07e9a71c5b98b1d1b1edebe9fa7f",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-1-22670216-1-field-0",
      "t-1-22670216-1-field-1",
      "t-1-22670216-1-field-2",
      "t-1-22670216-1-field-3",
      "t-1-22670216-1-field-4",
      "t-1-22670216-1-field-5",
      "t-1-22670216-1-field-6",
      "t-1-22670216-1-field-7",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10945,
      "completion_tokens": 35,
      "total_tokens": 10980,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Brooklyn, Michigan",
      "value": "Brooklyn , Michigan",
      "start": 19,
      "end": 37,
      "clause": "If the location is Brooklyn, Michigan"
    },
    {
      "surface": "Bobby Unser",
      "value": "Bobby Unser",
      "start": 63,
      "end": 74,
      "clause": "and the pole position is Bobby Unser"
    }
  ],
  "count": 2
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "INCOMPLETE",
  "complete": false,
  "missing": [],
  "extra": [
    "brooklyn , michigan"
  ],
  "conditions": [
    {
      "surface": "Brooklyn, Michigan",
      "value": "Brooklyn , Michigan",
      "start": 19,
      "end": 37,
      "clause": "If the location is Brooklyn, Michigan"
    },
    {
      "surface": "Bobby Unser",
      "value": "Bobby Unser",
      "start": 63,
      "end": 74,
      "clause": "and the pole position is Bobby Unser"
    }
  ]
}
```

Not executed after termination: WHERE_COLUMN_MATCH, WHERE_OPERATOR_RECOGNIZE, WHERE_VALUE_EXTRACT, WHERE_SCHEMA_VALUE_MATCH, WHERE_VALUE_NORMALIZE, WHERE_CONDITION_VALIDATE, WHERE_CONDITION_ASSEMBLE.

### 14 W07 — test:9646:2-17589865-2

- Question: What is the number of people in attendance when venue shows A, and Di Matteo, M. Hughes were the scorers?
- Table: 2-17589865-2; headers: ["Date","Round","Opponent","Venue","Result","Attendance","Scorers"]; types: ["text","text","text","text","text","real","text"]; rows: 7. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[3,0,"a"],[6,0,"di matteo, m. hughes"]]
- E4 predicted conds: [[3,0,"A"],[6,0,"Di Matteo , M. Hughes"]]; E4 SELECT/agg: [5,0].
- E3 final: status INCOMPLETE, stage WHERE_COMPLETENESS_CHECK, completed false, conds [].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 0 rows, E3 not reached rows).
- Knowledge context: 18 atoms, 10 patterns, 34 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 2,
  "conditions": [
    {
      "surface": "a",
      "value": "A",
      "start": 2,
      "end": 3
    },
    {
      "surface": "Di Matteo, M. Hughes",
      "value": "Di Matteo , M. Hughes",
      "start": 67,
      "end": 87
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "f9726d038fed62a2dec90fa13775fdad2cf96f4297dc0fbd3f86123145a05bbf",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-17589865-2-field-0",
      "t-2-17589865-2-field-1",
      "t-2-17589865-2-field-2",
      "t-2-17589865-2-field-3",
      "t-2-17589865-2-field-4",
      "t-2-17589865-2-field-5",
      "t-2-17589865-2-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10217,
      "completion_tokens": 35,
      "total_tokens": 10252,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "a",
      "value": "A",
      "start": 2,
      "end": 3,
      "clause": "What is the number of people in attendance when venue shows A,"
    },
    {
      "surface": "Di Matteo, M. Hughes",
      "value": "Di Matteo , M. Hughes",
      "start": 67,
      "end": 87,
      "clause": "and Di Matteo, M. Hughes were the scorers?"
    }
  ],
  "count": 2
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "INCOMPLETE",
  "complete": false,
  "missing": [],
  "extra": [
    "a",
    "di matteo , m. hughes"
  ],
  "conditions": [
    {
      "surface": "a",
      "value": "A",
      "start": 2,
      "end": 3,
      "clause": "What is the number of people in attendance when venue shows A,"
    },
    {
      "surface": "Di Matteo, M. Hughes",
      "value": "Di Matteo , M. Hughes",
      "start": 67,
      "end": 87,
      "clause": "and Di Matteo, M. Hughes were the scorers?"
    }
  ]
}
```

Not executed after termination: WHERE_COLUMN_MATCH, WHERE_OPERATOR_RECOGNIZE, WHERE_VALUE_EXTRACT, WHERE_SCHEMA_VALUE_MATCH, WHERE_VALUE_NORMALIZE, WHERE_CONDITION_VALIDATE, WHERE_CONDITION_ASSEMBLE.

### 15 W08 — test:10323:2-11051308-1

- Question: Which number of New Councils had a previous council number of 19 and the seats up for election were bigger than 6?
- Table: 2-11051308-1; headers: ["Party","Previous council","Staying councillors","Seats up for election","Election result","New council"]; types: ["text","real","real","real","real","real"]; rows: 9. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[1,0,19],[3,2,6]]
- E4 predicted conds: [[1,0,19],[3,1,6]]; E4 SELECT/agg: [5,0].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[1,0,19],[3,1,6]].
- Comparison: logical-form WHERE false; SQLite WHERE row equality true (gold 0 rows, E3 0 rows).
- Knowledge context: 17 atoms, 10 patterns, 32 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 2,
  "conditions": [
    {
      "surface": "19",
      "value": "19",
      "start": 62,
      "end": 64
    },
    {
      "surface": "6",
      "value": "6",
      "start": 112,
      "end": 113
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "549973eacec52a73ab7d7933b231e96ed0527630bfcd0187f92d355e75cb46c2",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-11051308-1-field-0",
      "t-2-11051308-1-field-1",
      "t-2-11051308-1-field-2",
      "t-2-11051308-1-field-3",
      "t-2-11051308-1-field-4",
      "t-2-11051308-1-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9717,
      "completion_tokens": 25,
      "total_tokens": 9742,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "19",
      "value": "19",
      "start": 62,
      "end": 64,
      "clause": "Which number of New Councils had a previous council number of 19"
    },
    {
      "surface": "6",
      "value": "6",
      "start": 112,
      "end": 113,
      "clause": "and the seats up for election were bigger than 6?"
    }
  ],
  "count": 2
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "19",
      "value": "19",
      "start": 62,
      "end": 64,
      "clause": "Which number of New Councils had a previous council number of 19"
    },
    {
      "surface": "6",
      "value": "6",
      "start": 112,
      "end": 113,
      "clause": "and the seats up for election were bigger than 6?"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "19",
      "value": "19",
      "start": 62,
      "end": 64,
      "clause": "Which number of New Councils had a previous council number of 19",
      "columnIndex": 1
    },
    {
      "surface": "6",
      "value": "6",
      "start": 112,
      "end": 113,
      "clause": "and the seats up for election were bigger than 6?",
      "columnIndex": 3
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "0615e665e67440eeb7a73cd68646a70e170e0dd4d9417fab5a8a3bee7729c5cf",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-11051308-1-field-0",
      "t-2-11051308-1-field-1",
      "t-2-11051308-1-field-2",
      "t-2-11051308-1-field-3",
      "t-2-11051308-1-field-4",
      "t-2-11051308-1-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10235,
      "completion_tokens": 11,
      "total_tokens": 10246,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "19",
      "value": "19",
      "start": 62,
      "end": 64,
      "clause": "Which number of New Councils had a previous council number of 19",
      "columnIndex": 1,
      "operator": "="
    },
    {
      "surface": "6",
      "value": "6",
      "start": 112,
      "end": 113,
      "clause": "and the seats up for election were bigger than 6?",
      "columnIndex": 3,
      "operator": ">"
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "f056aa8e75170e2f084026d40d7dfaeded7ac2137bd9760ace58a7c1096ced22",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-11051308-1-field-0",
      "t-2-11051308-1-field-1",
      "t-2-11051308-1-field-2",
      "t-2-11051308-1-field-3",
      "t-2-11051308-1-field-4",
      "t-2-11051308-1-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10613,
      "completion_tokens": 11,
      "total_tokens": 10624,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "19",
      "value": 19,
      "start": 62,
      "end": 64,
      "clause": "Which number of New Councils had a previous council number of 19",
      "columnIndex": 1,
      "operator": "="
    },
    {
      "surface": "6",
      "value": 6,
      "start": 112,
      "end": 113,
      "clause": "and the seats up for election were bigger than 6?",
      "columnIndex": 3,
      "operator": ">"
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "d28a779bf5885b110fc7898dde28f5edfab8d45a2b91ab72a3cdfe48299d602b",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-11051308-1-field-0",
      "t-2-11051308-1-field-1",
      "t-2-11051308-1-field-2",
      "t-2-11051308-1-field-3",
      "t-2-11051308-1-field-4",
      "t-2-11051308-1-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10957,
      "completion_tokens": 11,
      "total_tokens": 10968,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "19",
      "value": 19,
      "start": 62,
      "end": 64,
      "clause": "Which number of New Councils had a previous council number of 19",
      "columnIndex": 1,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "6",
      "value": 6,
      "start": 112,
      "end": 113,
      "clause": "and the seats up for election were bigger than 6?",
      "columnIndex": 3,
      "operator": ">",
      "grounded": true
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "19",
      "value": 19,
      "start": 62,
      "end": 64,
      "clause": "Which number of New Councils had a previous council number of 19",
      "columnIndex": 1,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "6",
      "value": 6,
      "start": 112,
      "end": 113,
      "clause": "and the seats up for election were bigger than 6?",
      "columnIndex": 3,
      "operator": ">",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "19",
      "value": 19,
      "start": 62,
      "end": 64,
      "clause": "Which number of New Councils had a previous council number of 19",
      "columnIndex": 1,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "6",
      "value": 6,
      "start": 112,
      "end": 113,
      "clause": "and the seats up for election were bigger than 6?",
      "columnIndex": 3,
      "operator": ">",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      1,
      0,
      19
    ],
    [
      3,
      1,
      6
    ]
  ]
}
```

### 16 W08 — test:10905:2-11399628-28

- Question: How many appearances for psv eindhoven ranked above 11?
- Table: 2-11399628-28; headers: ["Rank","Name","Team","Goals","Appearances","Minutes played"]; types: ["real","text","text","real","real","text"]; rows: 20. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[2,0,"psv eindhoven"],[0,2,11]]
- E4 predicted conds: [[2,0,"PSV Eindhoven"],[0,1,11]]; E4 SELECT/agg: [4,3].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[2,0,"PSV Eindhoven"],[0,1,11]].
- Comparison: logical-form WHERE false; SQLite WHERE row equality true (gold 0 rows, E3 0 rows).
- Knowledge context: 17 atoms, 10 patterns, 32 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 2,
  "conditions": [
    {
      "surface": "psv eindhoven",
      "value": "PSV Eindhoven",
      "start": 25,
      "end": 38
    },
    {
      "surface": "11",
      "value": 11,
      "start": 52,
      "end": 54
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "8a2406d5693bd39629901c5b5d8aa797fa1c4d6a8082a7acef6dc0d8a3070c39",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-11399628-28-field-0",
      "t-2-11399628-28-field-1",
      "t-2-11399628-28-field-2",
      "t-2-11399628-28-field-3",
      "t-2-11399628-28-field-4",
      "t-2-11399628-28-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10174,
      "completion_tokens": 30,
      "total_tokens": 10204,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "psv eindhoven",
      "value": "PSV Eindhoven",
      "start": 25,
      "end": 38,
      "clause": "How many appearances for psv eindhoven ranked above 11?"
    },
    {
      "surface": "11",
      "value": 11,
      "start": 52,
      "end": 54,
      "clause": "How many appearances for psv eindhoven ranked above 11?"
    }
  ],
  "count": 2
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "psv eindhoven",
      "value": "PSV Eindhoven",
      "start": 25,
      "end": 38,
      "clause": "How many appearances for psv eindhoven ranked above 11?"
    },
    {
      "surface": "11",
      "value": 11,
      "start": 52,
      "end": 54,
      "clause": "How many appearances for psv eindhoven ranked above 11?"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "psv eindhoven",
      "value": "PSV Eindhoven",
      "start": 25,
      "end": 38,
      "clause": "How many appearances for psv eindhoven ranked above 11?",
      "columnIndex": 2
    },
    {
      "surface": "11",
      "value": 11,
      "start": 52,
      "end": 54,
      "clause": "How many appearances for psv eindhoven ranked above 11?",
      "columnIndex": 0
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "42948ad77ffe314a9a23d144b3ce860d47149655604a6be53e72fd004eb875e4",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-11399628-28-field-0",
      "t-2-11399628-28-field-1",
      "t-2-11399628-28-field-2",
      "t-2-11399628-28-field-3",
      "t-2-11399628-28-field-4",
      "t-2-11399628-28-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10719,
      "completion_tokens": 11,
      "total_tokens": 10730,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "psv eindhoven",
      "value": "PSV Eindhoven",
      "start": 25,
      "end": 38,
      "clause": "How many appearances for psv eindhoven ranked above 11?",
      "columnIndex": 2,
      "operator": "="
    },
    {
      "surface": "11",
      "value": 11,
      "start": 52,
      "end": 54,
      "clause": "How many appearances for psv eindhoven ranked above 11?",
      "columnIndex": 0,
      "operator": ">"
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "a4755187b0da70968ab0477f8221951a3afb68a2e6b55c58c0f0f79f1655e605",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-11399628-28-field-0",
      "t-2-11399628-28-field-1",
      "t-2-11399628-28-field-2",
      "t-2-11399628-28-field-3",
      "t-2-11399628-28-field-4",
      "t-2-11399628-28-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 11097,
      "completion_tokens": 11,
      "total_tokens": 11108,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "psv eindhoven",
      "value": "PSV Eindhoven",
      "start": 25,
      "end": 38,
      "clause": "How many appearances for psv eindhoven ranked above 11?",
      "columnIndex": 2,
      "operator": "="
    },
    {
      "surface": "11",
      "value": 11,
      "start": 52,
      "end": 54,
      "clause": "How many appearances for psv eindhoven ranked above 11?",
      "columnIndex": 0,
      "operator": ">"
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "3497836debd978bc04fa9e3d9fb0055a1786b29df01e30cf04c3ccc5be6cd796",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-11399628-28-field-0",
      "t-2-11399628-28-field-1",
      "t-2-11399628-28-field-2",
      "t-2-11399628-28-field-3",
      "t-2-11399628-28-field-4",
      "t-2-11399628-28-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 11451,
      "completion_tokens": 13,
      "total_tokens": 11464,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "psv eindhoven",
      "value": "PSV Eindhoven",
      "start": 25,
      "end": 38,
      "clause": "How many appearances for psv eindhoven ranked above 11?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "11",
      "value": 11,
      "start": 52,
      "end": 54,
      "clause": "How many appearances for psv eindhoven ranked above 11?",
      "columnIndex": 0,
      "operator": ">",
      "grounded": true
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "psv eindhoven",
      "value": "PSV Eindhoven",
      "start": 25,
      "end": 38,
      "clause": "How many appearances for psv eindhoven ranked above 11?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "11",
      "value": 11,
      "start": 52,
      "end": 54,
      "clause": "How many appearances for psv eindhoven ranked above 11?",
      "columnIndex": 0,
      "operator": ">",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "psv eindhoven",
      "value": "PSV Eindhoven",
      "start": 25,
      "end": 38,
      "clause": "How many appearances for psv eindhoven ranked above 11?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "11",
      "value": 11,
      "start": 52,
      "end": 54,
      "clause": "How many appearances for psv eindhoven ranked above 11?",
      "columnIndex": 0,
      "operator": ">",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      2,
      0,
      "PSV Eindhoven"
    ],
    [
      0,
      1,
      11
    ]
  ]
}
```

### 17 W09 — test:9273:2-171222-1

- Question: What is the area (in km2) for the village of Paquetville, with a population over 706?
- Table: 2-171222-1; headers: ["Official Name","Status","Area km 2","Population","Census Ranking"]; types: ["text","text","real","real","text"]; rows: 18. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[1,0,"village"],[0,0,"paquetville"],[3,1,706]]
- E4 predicted conds: [[0,0,"Paquetville"],[3,1,706]]; E4 SELECT/agg: [2,0].
- E3 final: status INCOMPLETE, stage WHERE_COMPLETENESS_CHECK, completed false, conds [].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 0 rows, E3 not reached rows).
- Knowledge context: 16 atoms, 10 patterns, 30 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 2,
  "conditions": [
    {
      "surface": "Paquetville",
      "value": "Paquetville",
      "start": 45,
      "end": 56
    },
    {
      "surface": "706",
      "value": 706,
      "start": 81,
      "end": 84
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "4e60b3e9721c624bc392e35db325edec7a6a8f5057906a3db7ec5491a75b137f",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-171222-1-field-0",
      "t-2-171222-1-field-1",
      "t-2-171222-1-field-2",
      "t-2-171222-1-field-3",
      "t-2-171222-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9988,
      "completion_tokens": 29,
      "total_tokens": 10017,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Paquetville",
      "value": "Paquetville",
      "start": 45,
      "end": 56,
      "clause": "What is the area (in km2) for the village of Paquetville"
    },
    {
      "surface": "706",
      "value": 706,
      "start": 81,
      "end": 84,
      "clause": "with a population over 706?"
    }
  ],
  "count": 2
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "INCOMPLETE",
  "complete": false,
  "missing": [
    {
      "surface": "village",
      "value": "Village",
      "columnIndex": 1,
      "start": 34,
      "end": 41
    }
  ],
  "extra": [],
  "conditions": [
    {
      "surface": "Paquetville",
      "value": "Paquetville",
      "start": 45,
      "end": 56,
      "clause": "What is the area (in km2) for the village of Paquetville"
    },
    {
      "surface": "706",
      "value": 706,
      "start": 81,
      "end": 84,
      "clause": "with a population over 706?"
    }
  ]
}
```

Not executed after termination: WHERE_COLUMN_MATCH, WHERE_OPERATOR_RECOGNIZE, WHERE_VALUE_EXTRACT, WHERE_SCHEMA_VALUE_MATCH, WHERE_VALUE_NORMALIZE, WHERE_CONDITION_VALIDATE, WHERE_CONDITION_ASSEMBLE.

### 18 W09 — test:14505:2-12755231-1

- Question: What is the height of the fort peck dam which was built before 1968, is located in the united states and has a dam type of TE ?
- Table: 2-12755231-1; headers: ["Name","Country","Year","Structure Height [m]","Structure Volume [10 6 m 3 ]","Reservoir Volume [10 9 m 3 ]","Type"]; types: ["text","text","real","text","text","real","text"]; rows: 21. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[2,2,1968],[6,0,"te"],[1,0,"united states"],[0,0,"fort peck dam"]]
- E4 predicted conds: [[2,2,1968],[1,0,"united states"],[6,0,"TE"]]; E4 SELECT/agg: [3,0].
- E3 final: status INCOMPLETE, stage WHERE_COMPLETENESS_CHECK, completed false, conds [].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 1 rows, E3 not reached rows).
- Knowledge context: 18 atoms, 10 patterns, 34 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 4,
  "conditions": [
    {
      "surface": "fort peck dam",
      "value": "Fort Peck Dam",
      "start": 26,
      "end": 39
    },
    {
      "surface": "1968",
      "value": 1968,
      "start": 63,
      "end": 67
    },
    {
      "surface": "united states",
      "value": "United States",
      "start": 87,
      "end": 100
    },
    {
      "surface": "TE",
      "value": "TE",
      "start": 123,
      "end": 125
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "ce083664656d21a44b2cbc408a8686ef7054c1633d53ebeeb66a6afd61ee0362",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-12755231-1-field-0",
      "t-2-12755231-1-field-1",
      "t-2-12755231-1-field-2",
      "t-2-12755231-1-field-3",
      "t-2-12755231-1-field-4",
      "t-2-12755231-1-field-5",
      "t-2-12755231-1-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10838,
      "completion_tokens": 51,
      "total_tokens": 10889,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "fort peck dam",
      "value": "Fort Peck Dam",
      "start": 26,
      "end": 39,
      "clause": "What is the height of the fort peck dam which was built before 1968, is located in the united states"
    },
    {
      "surface": "1968",
      "value": 1968,
      "start": 63,
      "end": 67,
      "clause": "What is the height of the fort peck dam which was built before 1968, is located in the united states"
    },
    {
      "surface": "united states",
      "value": "United States",
      "start": 87,
      "end": 100,
      "clause": "is located in the united states"
    },
    {
      "surface": "TE",
      "value": "TE",
      "start": 123,
      "end": 125,
      "clause": "and has a dam type of TE ?"
    }
  ],
  "count": 4
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "INCOMPLETE",
  "complete": false,
  "missing": [],
  "extra": [
    "te"
  ],
  "conditions": [
    {
      "surface": "fort peck dam",
      "value": "Fort Peck Dam",
      "start": 26,
      "end": 39,
      "clause": "What is the height of the fort peck dam which was built before 1968, is located in the united states"
    },
    {
      "surface": "1968",
      "value": 1968,
      "start": 63,
      "end": 67,
      "clause": "What is the height of the fort peck dam which was built before 1968, is located in the united states"
    },
    {
      "surface": "united states",
      "value": "United States",
      "start": 87,
      "end": 100,
      "clause": "is located in the united states"
    },
    {
      "surface": "TE",
      "value": "TE",
      "start": 123,
      "end": 125,
      "clause": "and has a dam type of TE ?"
    }
  ]
}
```

Not executed after termination: WHERE_COLUMN_MATCH, WHERE_OPERATOR_RECOGNIZE, WHERE_VALUE_EXTRACT, WHERE_SCHEMA_VALUE_MATCH, WHERE_VALUE_NORMALIZE, WHERE_CONDITION_VALIDATE, WHERE_CONDITION_ASSEMBLE.

### 19 W10 — test:12664:2-18036506-11

- Question: What was the lowest against when the draw was at least 0?
- Table: 2-18036506-11; headers: ["Golden Rivers","Wins","Byes","Losses","Draws","Against"]; types: ["text","real","real","real","real","real"]; rows: 9. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[4,1,0]]
- E4 predicted conds: [[4,3,0]]; E4 SELECT/agg: [5,2].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[4,1,-5e-324]].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 0 rows, E3 9 rows).
- Knowledge context: 17 atoms, 10 patterns, 32 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 1,
  "conditions": [
    {
      "surface": "0",
      "value": "0",
      "start": 55,
      "end": 56
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "29ba3fe9b745490aa80c707a71918c77742307470dc55c4711771d2b21d4f674",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-18036506-11-field-0",
      "t-2-18036506-11-field-1",
      "t-2-18036506-11-field-2",
      "t-2-18036506-11-field-3",
      "t-2-18036506-11-field-4",
      "t-2-18036506-11-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9702,
      "completion_tokens": 17,
      "total_tokens": 9719,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "0",
      "value": "0",
      "start": 55,
      "end": 56,
      "clause": "What was the lowest against when the draw was at least 0?"
    }
  ],
  "count": 1
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "0",
      "value": "0",
      "start": 55,
      "end": 56,
      "clause": "What was the lowest against when the draw was at least 0?"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "0",
      "value": "0",
      "start": 55,
      "end": 56,
      "clause": "What was the lowest against when the draw was at least 0?",
      "columnIndex": 4
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "ab4bb82884bcb6429629ade7f61ee1ffee6145aa7299d3551a154ca1f41da17f",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-18036506-11-field-0",
      "t-2-18036506-11-field-1",
      "t-2-18036506-11-field-2",
      "t-2-18036506-11-field-3",
      "t-2-18036506-11-field-4",
      "t-2-18036506-11-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10107,
      "completion_tokens": 9,
      "total_tokens": 10116,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "0",
      "value": "0",
      "start": 55,
      "end": 56,
      "clause": "What was the lowest against when the draw was at least 0?",
      "columnIndex": 4,
      "operator": ">="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "c27dc3eed9010fa1b0443a35c1112e0b390a6a6f01d074d012ded0b3215f4a55",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-18036506-11-field-0",
      "t-2-18036506-11-field-1",
      "t-2-18036506-11-field-2",
      "t-2-18036506-11-field-3",
      "t-2-18036506-11-field-4",
      "t-2-18036506-11-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10439,
      "completion_tokens": 10,
      "total_tokens": 10449,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "0",
      "value": 0,
      "start": 55,
      "end": 56,
      "clause": "What was the lowest against when the draw was at least 0?",
      "columnIndex": 4,
      "operator": ">="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "3b5075bccc8e64b0d2daaf5d851149bfa99f5a9c343dbb6f43ed2f32534323f6",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-18036506-11-field-0",
      "t-2-18036506-11-field-1",
      "t-2-18036506-11-field-2",
      "t-2-18036506-11-field-3",
      "t-2-18036506-11-field-4",
      "t-2-18036506-11-field-5",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10746,
      "completion_tokens": 9,
      "total_tokens": 10755,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "0",
      "value": 0,
      "start": 55,
      "end": 56,
      "clause": "What was the lowest against when the draw was at least 0?",
      "columnIndex": 4,
      "operator": ">=",
      "grounded": true
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "0",
      "value": 0,
      "start": 55,
      "end": 56,
      "clause": "What was the lowest against when the draw was at least 0?",
      "columnIndex": 4,
      "operator": ">=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "0",
      "value": 0,
      "start": 55,
      "end": 56,
      "clause": "What was the lowest against when the draw was at least 0?",
      "columnIndex": 4,
      "operator": ">=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      4,
      1,
      -5e-324
    ]
  ]
}
```

### 20 W10 — test:15074:2-1252109-3

- Question: What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?
- Table: 2-1252109-3; headers: ["Year","Entrant","Chassis","Engine","Points"]; types: ["real","text","text","text","real"]; rows: 10. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[2,0,"kurtis kraft 500c"],[1,0,"bardahl"],[4,2,0]]
- E4 predicted conds: [[1,0,"Bardahl"],[2,0,"Kurtis Kraft 500C"],[4,3,0]]; E4 SELECT/agg: [0,1].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[1,0,"Bardahl"],[2,0,"Kurtis Kraft 500C"],[4,2,"2"]].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 0 rows, E3 1 rows).
- Knowledge context: 16 atoms, 10 patterns, 30 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 3,
  "conditions": [
    {
      "surface": "Bardahl",
      "value": "Bardahl",
      "start": 32,
      "end": 39
    },
    {
      "surface": "Kurtis Kraft 500c",
      "value": "Kurtis Kraft 500C",
      "start": 54,
      "end": 71
    },
    {
      "surface": "0",
      "value": 0,
      "start": 98,
      "end": 99
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "25f5152b3b4353cfd9b2cc9284f321c9a482afc0192f325f48a3a118f175965c",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-1252109-3-field-0",
      "t-2-1252109-3-field-1",
      "t-2-1252109-3-field-2",
      "t-2-1252109-3-field-3",
      "t-2-1252109-3-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9603,
      "completion_tokens": 49,
      "total_tokens": 9652,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Bardahl",
      "value": "Bardahl",
      "start": 32,
      "end": 39,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?"
    },
    {
      "surface": "Kurtis Kraft 500c",
      "value": "Kurtis Kraft 500C",
      "start": 54,
      "end": 71,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?"
    },
    {
      "surface": "0",
      "value": 0,
      "start": 98,
      "end": 99,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?"
    }
  ],
  "count": 3
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "Bardahl",
      "value": "Bardahl",
      "start": 32,
      "end": 39,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?"
    },
    {
      "surface": "Kurtis Kraft 500c",
      "value": "Kurtis Kraft 500C",
      "start": 54,
      "end": 71,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?"
    },
    {
      "surface": "0",
      "value": 0,
      "start": 98,
      "end": 99,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Bardahl",
      "value": "Bardahl",
      "start": 32,
      "end": 39,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 1
    },
    {
      "surface": "Kurtis Kraft 500c",
      "value": "Kurtis Kraft 500C",
      "start": 54,
      "end": 71,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 2
    },
    {
      "surface": "0",
      "value": 0,
      "start": 98,
      "end": 99,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 4
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "eed47a978bf4b0553b0f49ae74ee0de10619bc3001df49ebcff2a78378249816",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-1252109-3-field-0",
      "t-2-1252109-3-field-1",
      "t-2-1252109-3-field-2",
      "t-2-1252109-3-field-3",
      "t-2-1252109-3-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10417,
      "completion_tokens": 13,
      "total_tokens": 10430,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Bardahl",
      "value": "Bardahl",
      "start": 32,
      "end": 39,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 1,
      "operator": "="
    },
    {
      "surface": "Kurtis Kraft 500c",
      "value": "Kurtis Kraft 500C",
      "start": 54,
      "end": 71,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 2,
      "operator": "="
    },
    {
      "surface": "0",
      "value": 0,
      "start": 98,
      "end": 99,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 4,
      "operator": "<="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "bbf62e7d68df3c69911ee51166aed6cf864d67a8778082bd2f9962196ffe128d",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-1252109-3-field-0",
      "t-2-1252109-3-field-1",
      "t-2-1252109-3-field-2",
      "t-2-1252109-3-field-3",
      "t-2-1252109-3-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10878,
      "completion_tokens": 14,
      "total_tokens": 10892,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Bardahl",
      "value": "Bardahl",
      "start": 32,
      "end": 39,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 1,
      "operator": "="
    },
    {
      "surface": "Kurtis Kraft 500c",
      "value": "Kurtis Kraft 500C",
      "start": 54,
      "end": 71,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 2,
      "operator": "="
    },
    {
      "surface": "0",
      "value": 0,
      "start": 98,
      "end": 99,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 4,
      "operator": "<="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "25bd7c1485684602e6f44fd852ca0b45d7a1ee1c8b9f1414cd66019c40417904",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-1252109-3-field-0",
      "t-2-1252109-3-field-1",
      "t-2-1252109-3-field-2",
      "t-2-1252109-3-field-3",
      "t-2-1252109-3-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 11315,
      "completion_tokens": 21,
      "total_tokens": 11336,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Bardahl",
      "value": "Bardahl",
      "start": 32,
      "end": 39,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 1,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "Kurtis Kraft 500c",
      "value": "Kurtis Kraft 500C",
      "start": 54,
      "end": 71,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "0",
      "value": 0,
      "start": 98,
      "end": 99,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 4,
      "operator": "<=",
      "grounded": true
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Bardahl",
      "value": "Bardahl",
      "start": 32,
      "end": 39,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 1,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "Kurtis Kraft 500c",
      "value": "Kurtis Kraft 500C",
      "start": 54,
      "end": 71,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "0",
      "value": 0,
      "start": 98,
      "end": 99,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 4,
      "operator": "<=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "Bardahl",
      "value": "Bardahl",
      "start": 32,
      "end": 39,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 1,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "Kurtis Kraft 500c",
      "value": "Kurtis Kraft 500C",
      "start": 54,
      "end": 71,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "0",
      "value": 0,
      "start": 98,
      "end": 99,
      "clause": "What was the latest year that a Bardahl entrant had a Kurtis Kraft 500c chassis with no more than 0 points?",
      "columnIndex": 4,
      "operator": "<=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      1,
      0,
      "Bardahl"
    ],
    [
      2,
      0,
      "Kurtis Kraft 500C"
    ],
    [
      4,
      2,
      "2"
    ]
  ]
}
```

### 21 W11 — test:5120:2-13564637-3

- Question: what team lost the most points
- Table: 2-13564637-3; headers: ["Club","Played","Drawn","Lost","Points for","Points against","Tries for","Tries against","Try bonus","Losing bonus","Points"]; types: ["text","text","text","text","text","text","text","text","text","text","text"]; rows: 14. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[10,0,"points"]]
- E4 predicted conds: []; E4 SELECT/agg: [10,1].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 1 rows, E3 14 rows).
- Knowledge context: 22 atoms, 10 patterns, 42 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 0,
  "conditions": [],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "55ca4ede8bb4a7ea66aba1aa042a567a0b4e169b1cee49fdfd5ee54c399f4642",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-13564637-3-field-0",
      "t-2-13564637-3-field-1",
      "t-2-13564637-3-field-2",
      "t-2-13564637-3-field-3",
      "t-2-13564637-3-field-4",
      "t-2-13564637-3-field-5",
      "t-2-13564637-3-field-6",
      "t-2-13564637-3-field-7",
      "t-2-13564637-3-field-8",
      "t-2-13564637-3-field-9",
      "t-2-13564637-3-field-10",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 11682,
      "completion_tokens": 8,
      "total_tokens": 11690,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [],
  "count": 0
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": []
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "ad35946d7882c6dc5b5f0e2174751a9490cb596ab0c5d2b0ab08c08ab5d4f82a",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-13564637-3-field-0",
      "t-2-13564637-3-field-1",
      "t-2-13564637-3-field-2",
      "t-2-13564637-3-field-3",
      "t-2-13564637-3-field-4",
      "t-2-13564637-3-field-5",
      "t-2-13564637-3-field-6",
      "t-2-13564637-3-field-7",
      "t-2-13564637-3-field-8",
      "t-2-13564637-3-field-9",
      "t-2-13564637-3-field-10",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 12034,
      "completion_tokens": 8,
      "total_tokens": 12042,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "9b7a26cb77ed4e350a29239fe35fd74456f727342c53d3cf17475111c20e3fb8",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-13564637-3-field-0",
      "t-2-13564637-3-field-1",
      "t-2-13564637-3-field-2",
      "t-2-13564637-3-field-3",
      "t-2-13564637-3-field-4",
      "t-2-13564637-3-field-5",
      "t-2-13564637-3-field-6",
      "t-2-13564637-3-field-7",
      "t-2-13564637-3-field-8",
      "t-2-13564637-3-field-9",
      "t-2-13564637-3-field-10",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 12394,
      "completion_tokens": 8,
      "total_tokens": 12402,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "27850c1e4d4eb506fa49f71e377e8a0cd28043d727a1df891adcbffb800768ad",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-13564637-3-field-0",
      "t-2-13564637-3-field-1",
      "t-2-13564637-3-field-2",
      "t-2-13564637-3-field-3",
      "t-2-13564637-3-field-4",
      "t-2-13564637-3-field-5",
      "t-2-13564637-3-field-6",
      "t-2-13564637-3-field-7",
      "t-2-13564637-3-field-8",
      "t-2-13564637-3-field-9",
      "t-2-13564637-3-field-10",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 12712,
      "completion_tokens": 8,
      "total_tokens": 12720,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": []
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": []
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": []
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": []
}
```

### 22 W11 — test:9053:2-16713759-18

- Question: What was the total effic for the quarterbacks?
- Table: 2-16713759-18; headers: ["Name","GP-GS","Effic","Att-Cmp-Int","Avg/G"]; types: ["text","text","real","text","real"]; rows: 7. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[0,0,"total"]]
- E4 predicted conds: []; E4 SELECT/agg: [2,4].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[0,0,"Total"]].
- Comparison: logical-form WHERE true; SQLite WHERE row equality true (gold 1 rows, E3 1 rows).
- Knowledge context: 16 atoms, 10 patterns, 30 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 1,
  "conditions": [
    {
      "surface": "total",
      "value": "Total",
      "start": 13,
      "end": 18
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "e462143ebd7533698920cb4aa11c4a5b718b96cbea511e491d4ba2fb2ef72027",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-16713759-18-field-0",
      "t-2-16713759-18-field-1",
      "t-2-16713759-18-field-2",
      "t-2-16713759-18-field-3",
      "t-2-16713759-18-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9567,
      "completion_tokens": 17,
      "total_tokens": 9584,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "total",
      "value": "Total",
      "start": 13,
      "end": 18,
      "clause": "What was the total effic for the quarterbacks?"
    }
  ],
  "count": 1
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "total",
      "value": "Total",
      "start": 13,
      "end": 18,
      "clause": "What was the total effic for the quarterbacks?"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "total",
      "value": "Total",
      "start": 13,
      "end": 18,
      "clause": "What was the total effic for the quarterbacks?",
      "columnIndex": 0
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "cb0f702b608590240180e110ef926b43e8454337c1648ce80b5dd39c5a704dbb",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-16713759-18-field-0",
      "t-2-16713759-18-field-1",
      "t-2-16713759-18-field-2",
      "t-2-16713759-18-field-3",
      "t-2-16713759-18-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9945,
      "completion_tokens": 9,
      "total_tokens": 9954,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "total",
      "value": "Total",
      "start": 13,
      "end": 18,
      "clause": "What was the total effic for the quarterbacks?",
      "columnIndex": 0,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "6e7154eab6bd069d1ef52ad4bbeba9e2aa309cb909f3133e60c1cf6bb6cbf02a",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-16713759-18-field-0",
      "t-2-16713759-18-field-1",
      "t-2-16713759-18-field-2",
      "t-2-16713759-18-field-3",
      "t-2-16713759-18-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10259,
      "completion_tokens": 9,
      "total_tokens": 10268,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "total",
      "value": "Total",
      "start": 13,
      "end": 18,
      "clause": "What was the total effic for the quarterbacks?",
      "columnIndex": 0,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "b8626220de7db4c13e8563fe71a81e450524e53123f225e4c2b2dc224e37b34a",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-16713759-18-field-0",
      "t-2-16713759-18-field-1",
      "t-2-16713759-18-field-2",
      "t-2-16713759-18-field-3",
      "t-2-16713759-18-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10545,
      "completion_tokens": 10,
      "total_tokens": 10555,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "total",
      "value": "Total",
      "start": 13,
      "end": 18,
      "clause": "What was the total effic for the quarterbacks?",
      "columnIndex": 0,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "total",
      "value": "Total",
      "start": 13,
      "end": 18,
      "clause": "What was the total effic for the quarterbacks?",
      "columnIndex": 0,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "total",
      "value": "Total",
      "start": 13,
      "end": 18,
      "clause": "What was the total effic for the quarterbacks?",
      "columnIndex": 0,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      0,
      0,
      "Total"
    ]
  ]
}
```

### 23 W12 — test:10140:2-11108194-2

- Question: Which TV network is a spanish commercial network with greater than 53,674,000 households viewable and place lower than 1?
- Table: 2-11108194-2; headers: ["Place","TV network","% of US households reached","Number of households viewable","Type of Television Network"]; types: ["real","text","text","real","text"]; rows: 8. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[4,0,"spanish commercial"],[3,1,"53,674,000"],[0,1,1]]
- E4 predicted conds: [[4,0,"spanish commercial"],[3,1,53674000],[0,2,1]]; E4 SELECT/agg: [1,0].
- E3 final: status INCOMPLETE, stage WHERE_COMPLETENESS_CHECK, completed false, conds [].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 6 rows, E3 not reached rows).
- Knowledge context: 16 atoms, 10 patterns, 30 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 3,
  "conditions": [
    {
      "surface": "spanish commercial",
      "value": "Spanish Commercial",
      "start": 22,
      "end": 40
    },
    {
      "surface": "53,674,000",
      "value": 53674000,
      "start": 67,
      "end": 77
    },
    {
      "surface": "1",
      "value": 1,
      "start": 119,
      "end": 120
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "2da01329535635c591ade147497b3ba6f9d10b7d7c2614e1b908275ac9ac4b71",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-11108194-2-field-0",
      "t-2-11108194-2-field-1",
      "t-2-11108194-2-field-2",
      "t-2-11108194-2-field-3",
      "t-2-11108194-2-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9540,
      "completion_tokens": 42,
      "total_tokens": 9582,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "spanish commercial",
      "value": "Spanish Commercial",
      "start": 22,
      "end": 40,
      "clause": "Which TV network is a spanish commercial network with greater than 53,674,000 households viewable"
    },
    {
      "surface": "53,674,000",
      "value": 53674000,
      "start": 67,
      "end": 77,
      "clause": "Which TV network is a spanish commercial network with greater than 53,674,000 households viewable"
    },
    {
      "surface": "1",
      "value": 1,
      "start": 119,
      "end": 120,
      "clause": "and place lower than 1?"
    }
  ],
  "count": 3
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "INCOMPLETE",
  "complete": false,
  "missing": [
    {
      "surface": "53,674,000",
      "value": "53,674,000",
      "columnIndex": 3,
      "start": 67,
      "end": 77
    }
  ],
  "extra": [
    "53674000"
  ],
  "conditions": [
    {
      "surface": "spanish commercial",
      "value": "Spanish Commercial",
      "start": 22,
      "end": 40,
      "clause": "Which TV network is a spanish commercial network with greater than 53,674,000 households viewable"
    },
    {
      "surface": "53,674,000",
      "value": 53674000,
      "start": 67,
      "end": 77,
      "clause": "Which TV network is a spanish commercial network with greater than 53,674,000 households viewable"
    },
    {
      "surface": "1",
      "value": 1,
      "start": 119,
      "end": 120,
      "clause": "and place lower than 1?"
    }
  ]
}
```

Not executed after termination: WHERE_COLUMN_MATCH, WHERE_OPERATOR_RECOGNIZE, WHERE_VALUE_EXTRACT, WHERE_SCHEMA_VALUE_MATCH, WHERE_VALUE_NORMALIZE, WHERE_CONDITION_VALIDATE, WHERE_CONDITION_ASSEMBLE.

### 24 W12 — test:12112:2-11467154-1

- Question: The song "Before the Clock Strikes 12" has 55 points. What number did the song place?
- Table: 2-11467154-1; headers: ["Draw","Artist","Song","Points","Place"]; types: ["real","text","text","real","real"]; rows: 10. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[2,0,"\"before the clock strikes 12\""],[3,1,55]]
- E4 predicted conds: [[2,0,"Before The Clock Strikes 12"],[3,0,55]]; E4 SELECT/agg: [4,0].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[2,0,"\"Before The Clock Strikes 12\""],[3,0,55]].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 0 rows, E3 1 rows).
- Knowledge context: 16 atoms, 10 patterns, 30 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 2,
  "conditions": [
    {
      "surface": "Before the Clock Strikes 12",
      "value": "\"Before The Clock Strikes 12\"",
      "start": 10,
      "end": 37
    },
    {
      "surface": "55",
      "value": 55,
      "start": 43,
      "end": 45
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "98c9f82bbc91860af0e7fefa22a94d7df013f03269e9fb051dc3993f41fe6103",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-11467154-1-field-0",
      "t-2-11467154-1-field-1",
      "t-2-11467154-1-field-2",
      "t-2-11467154-1-field-3",
      "t-2-11467154-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9536,
      "completion_tokens": 39,
      "total_tokens": 9575,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Before the Clock Strikes 12",
      "value": "\"Before The Clock Strikes 12\"",
      "start": 10,
      "end": 37,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?"
    },
    {
      "surface": "55",
      "value": 55,
      "start": 43,
      "end": 45,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?"
    }
  ],
  "count": 2
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "Before the Clock Strikes 12",
      "value": "\"Before The Clock Strikes 12\"",
      "start": 10,
      "end": 37,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?"
    },
    {
      "surface": "55",
      "value": 55,
      "start": 43,
      "end": 45,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Before the Clock Strikes 12",
      "value": "\"Before The Clock Strikes 12\"",
      "start": 10,
      "end": 37,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?",
      "columnIndex": 2
    },
    {
      "surface": "55",
      "value": 55,
      "start": 43,
      "end": 45,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?",
      "columnIndex": 3
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "1d7519f628b90a4ec1962a7c8d2bd7ad8c8440e059bd127114f30c0d656f478e",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-11467154-1-field-0",
      "t-2-11467154-1-field-1",
      "t-2-11467154-1-field-2",
      "t-2-11467154-1-field-3",
      "t-2-11467154-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10158,
      "completion_tokens": 11,
      "total_tokens": 10169,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Before the Clock Strikes 12",
      "value": "\"Before The Clock Strikes 12\"",
      "start": 10,
      "end": 37,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?",
      "columnIndex": 2,
      "operator": "="
    },
    {
      "surface": "55",
      "value": 55,
      "start": 43,
      "end": 45,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?",
      "columnIndex": 3,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "a8f5fc9080139f8befe83b085280c1dc1b9eb470db5477b4886b5ba7d50ab9ee",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-11467154-1-field-0",
      "t-2-11467154-1-field-1",
      "t-2-11467154-1-field-2",
      "t-2-11467154-1-field-3",
      "t-2-11467154-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10559,
      "completion_tokens": 11,
      "total_tokens": 10570,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Before the Clock Strikes 12",
      "value": "\"Before The Clock Strikes 12\"",
      "start": 10,
      "end": 37,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?",
      "columnIndex": 2,
      "operator": "="
    },
    {
      "surface": "55",
      "value": 55,
      "start": 43,
      "end": 45,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?",
      "columnIndex": 3,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "dd58994929773e8c1402a92b60fd56775c5df9cf7d87184f5bf73eff38f37389",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-11467154-1-field-0",
      "t-2-11467154-1-field-1",
      "t-2-11467154-1-field-2",
      "t-2-11467154-1-field-3",
      "t-2-11467154-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10929,
      "completion_tokens": 18,
      "total_tokens": 10947,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Before the Clock Strikes 12",
      "value": "\"Before The Clock Strikes 12\"",
      "start": 10,
      "end": 37,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "55",
      "value": 55,
      "start": 43,
      "end": 45,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?",
      "columnIndex": 3,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "Before the Clock Strikes 12",
      "value": "\"Before The Clock Strikes 12\"",
      "start": 10,
      "end": 37,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "55",
      "value": 55,
      "start": 43,
      "end": 45,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?",
      "columnIndex": 3,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "Before the Clock Strikes 12",
      "value": "\"Before The Clock Strikes 12\"",
      "start": 10,
      "end": 37,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    },
    {
      "surface": "55",
      "value": 55,
      "start": 43,
      "end": 45,
      "clause": "The song \"Before the Clock Strikes 12\" has 55 points. What number did the song place?",
      "columnIndex": 3,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      2,
      0,
      "\"Before The Clock Strikes 12\""
    ],
    [
      3,
      0,
      55
    ]
  ]
}
```

### 25 W13 — test:4612:2-10701914-11

- Question: How many attended the game with north melbourne as the away side?
- Table: 2-10701914-11; headers: ["Home team","Home team score","Away team","Away team score","Venue","Crowd","Date"]; types: ["text","text","text","text","text","real","text"]; rows: 6. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[2,0,"melbourne"]]
- E4 predicted conds: [[2,0,"north melbourne"]]; E4 SELECT/agg: [5,3].
- E3 final: status SUCCEEDED, stage WHERE_CONDITION_ASSEMBLE, completed true, conds [[2,0,"North Melbourne"]].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 1 rows, E3 1 rows).
- Knowledge context: 18 atoms, 10 patterns, 34 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 1,
  "conditions": [
    {
      "surface": "north melbourne",
      "value": "North Melbourne",
      "start": 32,
      "end": 47
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "1c4e33fc4cc3e11c6bc1f12cf6825fa558412ee92b240d0b39c2270396e7b238",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-10701914-11-field-0",
      "t-2-10701914-11-field-1",
      "t-2-10701914-11-field-2",
      "t-2-10701914-11-field-3",
      "t-2-10701914-11-field-4",
      "t-2-10701914-11-field-5",
      "t-2-10701914-11-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10174,
      "completion_tokens": 20,
      "total_tokens": 10194,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "north melbourne",
      "value": "North Melbourne",
      "start": 32,
      "end": 47,
      "clause": "How many attended the game with north melbourne as the away side?"
    }
  ],
  "count": 1
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "SUCCEEDED",
  "complete": true,
  "missing": [],
  "extra": [],
  "conditions": [
    {
      "surface": "north melbourne",
      "value": "North Melbourne",
      "start": 32,
      "end": 47,
      "clause": "How many attended the game with north melbourne as the away side?"
    }
  ]
}
```

#### WHERE_COLUMN_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "north melbourne",
      "value": "North Melbourne",
      "start": 32,
      "end": 47,
      "clause": "How many attended the game with north melbourne as the away side?",
      "columnIndex": 2
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "a338dec88e6f4ecd7880f5d4a32119faab6b00f17399315e10e99a08a7655a3d",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-10701914-11-field-0",
      "t-2-10701914-11-field-1",
      "t-2-10701914-11-field-2",
      "t-2-10701914-11-field-3",
      "t-2-10701914-11-field-4",
      "t-2-10701914-11-field-5",
      "t-2-10701914-11-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10609,
      "completion_tokens": 9,
      "total_tokens": 10618,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_OPERATOR_RECOGNIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "north melbourne",
      "value": "North Melbourne",
      "start": 32,
      "end": 47,
      "clause": "How many attended the game with north melbourne as the away side?",
      "columnIndex": 2,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "f09dacadf013de87fda7b4c678d31527d8064b0e65b38c569be15f54b3ddc3f7",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-10701914-11-field-0",
      "t-2-10701914-11-field-1",
      "t-2-10701914-11-field-2",
      "t-2-10701914-11-field-3",
      "t-2-10701914-11-field-4",
      "t-2-10701914-11-field-5",
      "t-2-10701914-11-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 10962,
      "completion_tokens": 9,
      "total_tokens": 10971,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_VALUE_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "north melbourne",
      "value": "North Melbourne",
      "start": 32,
      "end": 47,
      "clause": "How many attended the game with north melbourne as the away side?",
      "columnIndex": 2,
      "operator": "="
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "daf4ae3dffc54743f5f19385b1ab6c67a9abb3ddc01d5da3bfdb51d0d248f8d5",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-10701914-11-field-0",
      "t-2-10701914-11-field-1",
      "t-2-10701914-11-field-2",
      "t-2-10701914-11-field-3",
      "t-2-10701914-11-field-4",
      "t-2-10701914-11-field-5",
      "t-2-10701914-11-field-6",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 11283,
      "completion_tokens": 11,
      "total_tokens": 11294,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_SCHEMA_VALUE_MATCH

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "north melbourne",
      "value": "North Melbourne",
      "start": 32,
      "end": 47,
      "clause": "How many attended the game with north melbourne as the away side?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_VALUE_NORMALIZE

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "north melbourne",
      "value": "North Melbourne",
      "start": 32,
      "end": 47,
      "clause": "How many attended the game with north melbourne as the away side?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_VALIDATE

```json
{
  "status": "SUCCEEDED",
  "valid": true,
  "errors": [],
  "conditions": [
    {
      "surface": "north melbourne",
      "value": "North Melbourne",
      "start": 32,
      "end": 47,
      "clause": "How many attended the game with north melbourne as the away side?",
      "columnIndex": 2,
      "operator": "=",
      "grounded": true
    }
  ]
}
```

#### WHERE_CONDITION_ASSEMBLE

```json
{
  "status": "SUCCEEDED",
  "conds": [
    [
      2,
      0,
      "North Melbourne"
    ]
  ]
}
```

### 26 W14 — test:14569:2-1247786-1

- Question: What is the greatest Born in other EU state (1000) with more than 1,380 foreign-born from Italy and a born in a non EU state (1000) less than 3,205?
- Table: 2-1247786-1; headers: ["Country","Total population (1000)","Total Foreign-born (1000)","Born in other EU state (1000)","Born in a non EU state (1000)"]; types: ["text","real","real","real","real"]; rows: 13. Full rows are in data/test.tables.jsonl and E3 raw.jsonl.
- Gold conds: [[2,1,"1,380"],[0,0,"italy"],[4,2,"3,205"]]
- E4 predicted conds: [[2,1,1380],[4,2,3205]]; E4 SELECT/agg: [3,1].
- E3 final: status INCOMPLETE, stage WHERE_COMPLETENESS_CHECK, completed false, conds [].
- Comparison: logical-form WHERE false; SQLite WHERE row equality false (gold 0 rows, E3 not reached rows).
- Knowledge context: 16 atoms, 10 patterns, 30 relations. E3 request retains question, full table and canvas structure.

#### WHERE_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "count": 3,
  "conditions": [
    {
      "surface": "1,380",
      "value": "1380",
      "start": 66,
      "end": 71
    },
    {
      "surface": "Italy",
      "value": "Italy",
      "start": 90,
      "end": 95
    },
    {
      "surface": "3,205",
      "value": "3205",
      "start": 142,
      "end": 147
    }
  ],
  "modelCall": {
    "model": "gpt-5.6-sol",
    "promptSha256": "df4d4cde0f30ff9d6aac4c1295ebc300ac239a1c3188422d210793a9da6d34fa",
    "knowledgeIds": [
      "g-eq",
      "g-gt",
      "g-lt",
      "g-inclusive",
      "g-literal",
      "g-threshold",
      "g-and",
      "g-or",
      "g-column",
      "g-count",
      "g-ambiguous",
      "t-2-1247786-1-field-0",
      "t-2-1247786-1-field-1",
      "t-2-1247786-1-field-2",
      "t-2-1247786-1-field-3",
      "t-2-1247786-1-field-4",
      "d-implicit-eq",
      "d-explicit-eq",
      "d-gt",
      "d-lt-and-gt",
      "d-with-two",
      "d-literal-form",
      "p-text",
      "p-number",
      "p-multi",
      "p-unsupported"
    ],
    "usage": {
      "prompt_tokens": 9823,
      "completion_tokens": 39,
      "total_tokens": 9862,
      "prompt_tokens_details": {
        "cached_tokens": 4224
      }
    }
  }
}
```

#### WHERE_MULTI_CONDITION_EXTRACT

```json
{
  "status": "SUCCEEDED",
  "conditions": [
    {
      "surface": "1,380",
      "value": "1380",
      "start": 66,
      "end": 71,
      "clause": "What is the greatest Born in other EU state (1000) with more than 1,380 foreign-born from Italy"
    },
    {
      "surface": "Italy",
      "value": "Italy",
      "start": 90,
      "end": 95,
      "clause": "380 foreign-born from Italy"
    },
    {
      "surface": "3,205",
      "value": "3205",
      "start": 142,
      "end": 147,
      "clause": "and a born in a non EU state (1000) less than 3,205?"
    }
  ],
  "count": 3
}
```

#### WHERE_COMPLETENESS_CHECK

```json
{
  "status": "INCOMPLETE",
  "complete": false,
  "missing": [
    {
      "surface": "1,380",
      "value": "1,380",
      "columnIndex": 2,
      "start": 66,
      "end": 71
    },
    {
      "surface": "3,205",
      "value": "3,205",
      "columnIndex": 4,
      "start": 142,
      "end": 147
    }
  ],
  "extra": [
    "3205",
    "1380"
  ],
  "conditions": [
    {
      "surface": "1,380",
      "value": "1380",
      "start": 66,
      "end": 71,
      "clause": "What is the greatest Born in other EU state (1000) with more than 1,380 foreign-born from Italy"
    },
    {
      "surface": "Italy",
      "value": "Italy",
      "start": 90,
      "end": 95,
      "clause": "380 foreign-born from Italy"
    },
    {
      "surface": "3,205",
      "value": "3205",
      "start": 142,
      "end": 147,
      "clause": "and a born in a non EU state (1000) less than 3,205?"
    }
  ]
}
```

Not executed after termination: WHERE_COLUMN_MATCH, WHERE_OPERATOR_RECOGNIZE, WHERE_VALUE_EXTRACT, WHERE_SCHEMA_VALUE_MATCH, WHERE_VALUE_NORMALIZE, WHERE_CONDITION_VALIDATE, WHERE_CONDITION_ASSEMBLE.
