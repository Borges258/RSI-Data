# WikiSQL WHERE 原能力回归

本目录复用 `dataset/WikiSQL_Where/experiment/run_ablation.py` 的请求、条件校验、SQLite 执行比较、模型证据和 Token 统计。数据为 E4 成功案例中抽取的 400 条样本；仅运行 E1、E2、E3，样本身份逐条与 `sample_manifest.json`、官方 WikiSQL test 和 E4 评估记录核对。

```powershell
# 从 F:\Agent_LAB 运行；不加 --execute 时只做离线预检。
python dataset/WikiSQL_Where_return/experiment/run_regression.py --run-id return_regression_20261126
python dataset/WikiSQL_Where_return/experiment/run_regression.py --run-id return_regression_20261126 --execute
```

默认并发为 2，超时 600 秒，不自动重试。相同参数与 run ID 可跳过已完成的逐样本文件并续跑。服务地址、模型和 Node 路径可用 `--prior-url`、`--flow-url`、`--model`、`--node` 指定。

输出位于本数据集的 `result/<run-id>/` 和 `reports/<run-id>/`。运行清单冻结数据、抽样名单、脚本、Flow、Java 实现等 SHA-256。SCR、严格结构、Unicode casefold 加 AND 顺序归一化结构、WHERE 行一致及非空行一致均沿用正式实验口径。E1/E2/E3 仅产生 WHERE 条件，不能据此报告完整 SQL 官方 EX。
