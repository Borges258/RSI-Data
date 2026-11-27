# E3-v3 WHERE 语义评估（26 条）

本报告复算已保存的 E3-v3 输出；Gold 和预测均未改动，未调用 LLM。

- 输入 `evaluated.jsonl` SHA-256：`ff2a125f59ace26298cdfc188b8c1f0c26fc3fcf864bf62e035187a803f0a84f`
- 输入 `test.db` SHA-256：`a8eaaa562743bde57ea68e4dbf6996a7908b32fd66197f91de8f03715f40738c`
- 官方完整 SQL EX：**无法计算**。E3 只输出 `conds`，缺少预测的 `sel` 与 `agg`。原记录中的 `exact` 是 WHERE 条件比较，不能冒充官方 EX。
- 原评估器严格条件多重集匹配：11/26 (42.3%)
- Unicode casefold 与 AND 顺序归一后的结构匹配：20/26 (76.9%)
- 真正多出的条件：1 个；在当前表上证明冗余：0 个
- WHERE 筛选行一致：20/26 (76.9%)
- 非空 WHERE 一致：20/26 (76.9%)；双方空集：0 条
- 结构匹配与 WHERE 执行一致分别统计；双方空集单列。

## W01–W14 分类统计

每类仅 1–2 条，百分比是小样本描述，不能视为该类总体准确率。

| 类别 | 样本 | 完成 | 严格结构 | 归一化结构 | WHERE 行一致 | 其中双方空集 |
|---|---:|---:|---:|---:|---:|---:|
| W01 | 2 | 2 | 0 | 2 | 2 | 0 |
| W02 | 2 | 2 | 2 | 2 | 2 | 0 |
| W03 | 2 | 2 | 2 | 2 | 2 | 0 |
| W04 | 2 | 1 | 0 | 0 | 0 | 0 |
| W05 | 2 | 2 | 0 | 2 | 2 | 0 |
| W06 | 2 | 2 | 1 | 1 | 1 | 0 |
| W07 | 2 | 2 | 2 | 2 | 2 | 0 |
| W08 | 2 | 1 | 1 | 1 | 1 | 0 |
| W09 | 2 | 2 | 0 | 2 | 2 | 0 |
| W10 | 2 | 1 | 0 | 1 | 1 | 0 |
| W11 | 2 | 2 | 1 | 2 | 2 | 0 |
| W12 | 2 | 2 | 1 | 2 | 2 | 0 |
| W13 | 1 | 1 | 1 | 1 | 1 | 0 |
| W14 | 1 | 1 | 0 | 0 | 0 | 0 |

## 逐条分类

序号按已保存 `evaluated.jsonl` 的顺序；`example_id` 为稳定样本标识。中途终止的样本没有最终预测，额外和缺失条件记作不可判定。

| 序号 | W 类 | example_id | 结果类别 | 状态 | 未匹配预测条件 | 缺失 Gold 条件 | Gold 行数 | 预测行数 |
|---:|---|---|---|---|---:|---:|---:|---:|
| 01 | W01 | `test:12152:2-18228282-6` | NORMALIZED_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 02 | W01 | `test:13980:2-18602446-15` | NORMALIZED_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 03 | W02 | `test:1494:1-16581695-2` | EXACT_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 04 | W02 | `test:3416:1-25737761-4` | EXACT_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 05 | W03 | `test:9286:2-169523-2` | EXACT_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 06 | W03 | `test:15522:2-12898083-1` | EXACT_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 07 | W04 | `test:6006:2-13669614-12` | MISMATCH | SUCCEEDED | 1 | 1 | 1 | 1 |
| 08 | W04 | `test:2990:1-2409041-4` | MISMATCH | GROUNDING_FAILED | — | — | 1 | — |
| 09 | W05 | `test:6870:2-15679259-3` | NORMALIZED_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 10 | W05 | `test:8373:2-1620220-3` | NORMALIZED_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 11 | W06 | `test:1102:1-1473672-7` | EXACT_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 12 | W06 | `test:10989:2-11086216-2` | MISMATCH | SUCCEEDED | 1 | 0 | 4 | 3 |
| 13 | W07 | `test:11779:2-11163017-5` | EXACT_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 14 | W07 | `test:13611:2-1859914-1` | EXACT_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 15 | W08 | `test:7876:2-17128242-4` | MISMATCH | CLARIFY | — | — | 1 | — |
| 16 | W08 | `test:10943:2-11516217-44` | EXACT_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 17 | W09 | `test:14505:2-12755231-1` | NORMALIZED_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 18 | W09 | `test:12332:2-1794747-6` | NORMALIZED_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 19 | W10 | `test:14626:2-13023925-2` | NORMALIZED_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 20 | W10 | `test:3389:1-25696729-8` | MISMATCH | GROUNDING_FAILED | — | — | 2 | — |
| 21 | W11 | `test:574:1-12828723-5` | EXACT_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 22 | W11 | `test:9053:2-16713759-18` | NORMALIZED_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 23 | W12 | `test:12112:2-11467154-1` | NORMALIZED_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 24 | W12 | `test:15220:2-12538628-1` | EXACT_MATCH | SUCCEEDED | 0 | 0 | 4 | 4 |
| 25 | W13 | `test:4612:2-10701914-11` | EXACT_MATCH | SUCCEEDED | 0 | 0 | 1 | 1 |
| 26 | W14 | `test:12072:2-11827596-4` | MISMATCH | SUCCEEDED | 1 | 2 | 36 | 1 |

## 未匹配与异常明细

- #07 W04 MISMATCH（SUCCEEDED）：Gold=[[1, 0, 'milwaukee braves']]；预测=[[3, 0, 'Milwaukee Braves']]
- #08 W04 MISMATCH（GROUNDING_FAILED）：Gold=[[1, 0, 11]]；预测=[]
- #12 W06 MISMATCH（SUCCEEDED）：Gold=[[5, 0, 'jacqui mcqueen'], [0, 2, 2011]]；预测=[[5, 0, 'Jacqui McQueen'], [1, 0, 'Nominated*'], [0, 2, 2011]]；公共条件行数=4
- #15 W08 MISMATCH（CLARIFY）：Gold=[[3, 1, 65], [1, 0, 'rocco mediate']]；预测=[]
- #20 W10 MISMATCH（GROUNDING_FAILED）：Gold=[[5, 0, 5]]；预测=[]
- #26 W14 MISMATCH（SUCCEEDED）：Gold=[[0, 1, 1972], [4, 0, 'hindi'], [3, 0, 'ravindra jain'], [2, 0, 'ravindra jain']]；预测=[[0, 0, 1972], [4, 0, 'Hindi'], [2, 0, 'Ravindra Jain']]

## 比较口径

仅在比较键中对文本值执行 Python Unicode `casefold()`。WHERE 条件按多重集比较，忽略 AND 顺序但保留重复次数；列、运算符和值类型均保留。不消除标点、空格或引号，也不改写数字。只有公共 Gold 条件筛出至少一行，且额外条件在这些行上全部为真，才判当前表上的冗余。这不是普遍逻辑等价证明。

## Gold 人工审计补充（5 条争议样本）

本节的 Gold 结论不改变上文任何预测或指标；完整论证见 `../failure_review.md` 的“Gold 人工审计结论”一节。

| 序号 | example_id | 结论 | 要点 |
|---:|---|---|---|
| 08 | `test:2990:1-2409041-4` | 标注错误 | `agg=COUNT` 应为 `NONE`；自然读法答案 58，COUNT 返回 1 |
| 12 | `test:10989:2-11086216-2` | 歧义 | “nominated”是否构成 `Result` 条件存在两种自洽读法 |
| 15 | `test:7876:2-17128242-4` | 标注错误 | 高尔夫“65 or better”应为 ≤65，Gold 用 `Score>65` 方向相反；闭区间不可表达 |
| 20 | `test:3389:1-25696729-8` | 不可表达 | 问句要求 `>=5` 且引用不存在的 blocked shots 列 |
| 26 | `test:12072:2-11827596-4` | 标注错误 | `Year>1972` 应为 `=1972`，并多出 `Lyricist` 条件 |

汇总：26 条中有效 Gold 21 条（含 #07）、歧义 1 条、不可表达 1 条、标注错误 3 条。严格准确率统计建议剔除 #12、#15、#20、#26；#08 修正 `agg` 后可保留，未修正前同样剔除。因此上文 20/26 的结构匹配与 WHERE 行一致率不应直接当作对“有效 Gold”的准确率。
