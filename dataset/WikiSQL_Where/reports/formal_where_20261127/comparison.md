# WikiSQL WHERE 2×2 ablation

SCR = successful generation with validated conditions / all 410 valid samples.
E0 reuses frozen E4 predictions; E1 uses canvas prior only; E2 uses WHERE Skill Flow with empty prior; E3 uses both.

| Arm | SCR | Strict WHERE | Normalized WHERE | WHERE rows | Nonempty WHERE rows | Both empty | New model calls | Input / cached / output tokens |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| E0 | 406/410 (99.02%) | 50/410 | 228/410 | 243/410 | 238/405 | 5 | 0 | 0 / 0 / 0 |
| E1 | 392/410 (95.61%) | 141/410 | 321/410 | 343/410 | 338/405 | 5 | 410 | 4280336 / 1742080 / 21695 |
| E2 | 368/410 (89.76%) | 141/410 | 316/410 | 319/410 | 314/405 | 5 | 1660 | 8698191 / 7282944 / 53533 |
| E3 | 391/410 (95.37%) | 146/410 | 337/410 | 341/410 | 336/405 | 5 | 1648 | 17313814 / 10095104 / 47473 |

Official full-SQL EX is unavailable for E1/E2/E3 WHERE-only outputs. WHERE selected-row agreement is a separate metric; both-empty agreement is not evidence of repair.
