# 重新抽样 Testline 26 条：E3-v3 失败复核

本轮使用 `sample_manifest.json` 中的新抽样与当前 `data/test.jsonl` Gold，执行真实 GPT-5.6-sol E3 Flow。未修改模型、Prompt、Skill 或 Gold；未运行 596 条正式实验。以下准确率仅指 WHERE 条件的 Unicode casefold 与 AND 多重集归一比较，非完整 SQL EX。每类只有 1–2 条。

| E4 历史类别 | 本轮归一化结构匹配 | 本轮失败记录 |
|---|---:|---|
| W01 值大小写不一致 | 2/2 | 无 |
| W02 值不在表内/引号差异 | 2/2 | 无 |
| W03 等值与范围混淆 | 2/2 | 无 |
| W04 列映射错且伴随选列/聚合错 | 0/2 | #07、#08 |
| W05 仅列映射错 | 2/2 | 无 |
| W06 多出条件 | 1/2 | #12 |
| W07 表内值选错 | 2/2 | 无 |
| W08 比较方向反转 | 1/2 | #15 |
| W09 遗漏部分条件 | 2/2 | 无 |
| W10 运算符越界 | 1/2 | #20 |
| W11 WHERE 整体丢失 | 2/2 | 无 |
| W12 多条件复合错误 | 2/2 | 无 |
| W13 值截断或扩写 | 1/1 | 无 |
| W14 条件全错且数量不足 | 0/1 | #26 |

## 六条未匹配记录

- **#07 W04** `test:6006:2-13669614-12`：问句问“谁对阵 Milwaukee Braves”。Gold 用 `Team=Milwaukee Braves` 筛选，预测在字段匹配阶段改用 `Opponent=Milwaukee Braves`，导致筛选行为第 7 行与第 9 行不同。属于列语义方向错误。
- **#08 W04** `test:2990:1-2409041-4`：Gold 用 `No. in season=11`；模型误以为该列是输出、把筛选指向 `No. in series`。后续 Grounding 发现 11 不在该列，局部复核拒绝切到 Gold 列，最终 `GROUNDING_FAILED`。原问句“number of number in the season for 11”表述不清，须结合目标列人工复核。
- **#12 W06** `test:10989:2-11086216-2`：条件提取额外加入 `Result=Nominated*`，Gold 只有人物和年份条件。Gold 筛出 4 行、预测筛出 3 行，该额外条件**不是**当前表上的冗余条件；“nominated”一词是否应单独成条件仍有问句与标注解释空间。
- **#15 W08** `test:7876:2-17128242-4`：提取到 65 和 Rocco Mediate，但“65 or better”在多条件边界识别处触发 `CLARIFY`，未形成 `conds`。Gold 使用 `Score>65`；高尔夫分数“better”的方向及边界与该标注存在争议，不能简单按 Gold 判模型方向错误。
- **#20 W10** `test:3389:1-25696729-8`：问句提到 `blocked shots (points) >=5`，表头没有这一字段。字段匹配保留未决，Grounding 复核没有证据将其强行映射到 `Double double`，校验以 `GROUNDING_FAILED` 结束。Gold 为 `Double double=5`，与 `>=5` 的问句语义也不相同；该样本需要人工重审标注和可表达性。
- **#26 W14** `test:12072:2-11827596-4`：问句是 **1972** 年、Hindi、Ravindra Jain 担任**音乐导演**。预测据此输出 `Year=1972, Language=Hindi, Music director=Ravindra Jain`；Gold 却使用 `Year>1972`，还额外要求 `Lyricist=Ravindra Jain`。两者筛选行显著不同（36 对 1），但 Gold 与问句存在明确冲突，不宜直接归为模型语义失败。

本轮 23/26 条完成 Flow，归一化结构匹配 20/26，WHERE 执行行一致 20/26，且无双方空集。六条比较失败中，#07 是较明确的列映射错误；#08 是字段解释与 Grounding 终止；#12 是非冗余额外条件；#15、#20 受不可安全表达的条件和标注争议影响；#26 的 Gold 与问句冲突。完整逐条预测、Gold、状态和行集合见 `semantic_evaluation/classified.jsonl`。

## Gold 人工审计结论（#08、#12、#15、#20、#26）

审计对象为 `data/test.jsonl` 当前 Gold（`sel`、`conds`、`agg`）与问句、表头及表内行值的一致性。结论类别为“有效 Gold / 歧义 / 不可表达 / 标注错误”。#07 不在本批，其 Gold 与问句一致，失败归因于模型列映射。

| 序号 | W 类 | example_id | 结论 | 关键依据 | 建议 |
|---:|---|---|---|---|---|
| 08 | W04 | `test:2990:1-2409041-4` | 标注错误 | 条件列与目标列可解释，但 `agg=COUNT` 与问句不符 | 修 `agg=COUNT→NONE` 后保留，或剔除 |
| 12 | W06 | `test:10989:2-11086216-2` | 歧义 | “nominated”是否应作为 `Result` 条件存在两种自洽读法 | 剔除或人工改写问句后再用 |
| 15 | W08 | `test:7876:2-17128242-4` | 标注错误 | 高尔夫“65 or better”应为 ≤65，Gold 用 `Score>65` 方向相反 | 剔除 |
| 20 | W10 | `test:3389:1-25696729-8` | 不可表达 | 问句要求 `>=5` 且引用表中不存在的 blocked shots 列 | 剔除或重写问句 |
| 26 | W14 | `test:12072:2-11827596-4` | 标注错误 | `Year>1972` 应为 `Year=1972`，且多出问句未要求的 `Lyricist` 条件 | 修正后可保留，或剔除 |

### #08 W04

- 问句：`Name the number of number in the season for 11`；表头含 `No. in series`（48–71）与 `No. in season`（1–24）。
- Gold：`sel=No. in series`，`conds=[[No. in season, =, 11]]`，`agg=COUNT`；命中 1 行（`No. in series=58`），返回 **1**。
- 判定：在“No. in season=11 的那一集的 No. in series”这一自然读法下，条件列与目标列都正确，答案应为 **58**，因此 `agg=COUNT` 是标注错误；“把 11 当作 No. in series”不可行，因为该列范围为 48–71，模型 `GROUNDING_FAILED` 属于可理解的误读。
- 结论：**标注错误**（`agg` 应为 `NONE`；条件与 `sel` 无需改动）。若需保留该样本，建议将 `agg` 修正为 `NONE` 并复核答案 58。

### #12 W06

- 问句：`In what category was character Jacqui Mcqueen nominated before 2011?`
- Gold：`Character='jacqui mcqueen'`、`Year<2011`，命中 4 行，其中第 3 行为 `Result=Won`（Best Family）；模型额外加 `Result='Nominated*'`，命中 3 行。
- 判定：读法一，“nominated”是对 `Result` 的筛选，则 Gold 漏条件，模型更贴近问句；读法二，获奖记录本身蕴含被提名，则 Gold 的 4 行也自洽。表中 `Result` 同时存在 `Nominated*`、`Nominated`、`Won`、`Shortlisted*`，单个等值条件无法覆盖全部“提名”拼写，进一步放大歧义。
- 结论：**歧义**（Gold 是否应包含 `Result` 条件无唯一解，不能据此判定模型语义错误）。建议剔除或人工改写问句后重标。

### #15 W08

- 问句：`WHAT IS THE PLACE THAT HAS SCORE OF 65 OR BETTER, FOR ROCCO MEDIATE?`
- Gold：`Score>65`、`Player='rocco mediate'`，命中 1 行（Rocco Mediate，Score=69）。
- 判定：高尔夫中“65 or better”表示 **≤65**（越低越好），而 Rocco 的 Score=69，按问句应无结果；Gold 的 `>65` 方向相反且排除了 65 本身。即使把方向改为 `<`，闭区间“or better”（≤65）仍无法用 0/1/2 精确表达（需要 `<66` 或 `≤65`）。
- 结论：**标注错误**（方向反转为主）；同时“or better”的边界条件不可由现有算子精确表达。建议剔除。

### #20 W10

- 问句：`What is the highest rebound Dennis Rodman obtained within the 20 rebounds category given two of the following: points, rebounds, assists, steals, and blocked shots (points) greater equal to 5`。
- Gold：`Double double=5`，`sel=20 rebounds`，`agg=MAX`，命中第 10、12 行，返回 16。
- 判定：问句要求 `blocked shots (points) >= 5`，但表中没有 blocked shots 列，且 `>=5` 不能被 0/1/2 表达；Gold 用 `Double double=5` 是另一派生指标的近似，与 `>=5` 语义不同。
- 结论：**不可表达**（同时存在列引用歧义）。建议剔除或重写问句。

### #26 W14

- 问句：`What 1972 Hindi film had Ravindra Jain directing the music?`
- Gold：`Year>1972`、`Language='hindi'`、`Lyricist='ravindra jain'`、`Music director='ravindra jain'`，命中 36 行（多为 1973–1980 年影片）。
- 判定：问句限定 1972 年，`Year>1972` 应为 `Year=1972`；“directing the music”对应 `Music director`，Gold 额外加入的 `Lyricist` 条件并非问句要求。按问句三条件（`Year=1972`、`Language='Hindi'`、`Music director='Ravindra Jain'`）应命中 1 行（`Kaanch Aur Heera`），与模型预测一致。
- 结论：**标注错误**（`Year` 运算符错误 + 多出 `Lyricist` 条件）。建议修正后保留，或剔除。

### 26 条汇总

- 有效 Gold：21 条（含 #07；其余 20 条归一化结构与 WHERE 行均一致，未发现问句—Gold 冲突）。
- 歧义：1 条（#12）。
- 不可表达：1 条（#20）。
- 标注错误：3 条（#08、#15、#26）。
- 建议：严格准确率统计时剔除 #12、#15、#20、#26；#08 修正 `agg` 后可保留，未修正前同样剔除。
