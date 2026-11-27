# WHERE 先验知识链路最小验证记录

本轮仅验证工程链路，没有执行 WikiSQL 数据集实验、准确率对比或消融实验，也没有修改原始 Baseline。用的是 3 行自造城市表；Prompt 内原有的 Train 知识举例是参考知识，不是本轮测试问题或测试标签。

## 实际执行结果

| 检查项 | 结果 | 证据 |
| --- | --- | --- |
| Spring Controller 自动化测试 | 通过 | 5 项，Failures 0 / Errors 0 / Skipped 0；报告在 `target/surefire-reports` |
| 默认禁用、零供应商请求 | 通过 | Controller 测试计数为 0；实际 fat jar 的 `/health` 显示 `llmEnabled=false`，实际 `/parse` 返回 503 `LLM_DISABLED` |
| 来源/表级作用域校验 | 通过 | 作用域不一致、统计来源伪装人工规则均在模型调用前拒绝 |
| 供应商正常/非法响应处理 | 通过（单元测试桩） | 正常 WHERE 接受；非 JSON 返回 502 `LLM_OUTPUT_INVALID`；非法 URL 返回 503 配置错误且零调用。测试桩不代表真实模型 |
| fat jar 启动 | 通过 | 实际运行 `target/where-prior-0.1.0.jar`，未使用 exploded classpath |
| 画布 → 提取 → Prepare | 通过 | 24 个知识节点，HTTP 200，`tableId=smoke-cities` |
| 浏览器页面点击 Prepare | 通过 | Edge headless 实际渲染表级节点并点击；接口返回 200 |
| 画布修改影响知识输入 | 通过 | 浏览器移除 `g-eq` 后节点数 24 → 23，Prompt 哈希变化 |
| 知识快照往返恢复 | 通过 | 浏览器导出、删节点、载入快照，再 Prepare；恢复到 24 个节点及相同 Prompt 哈希 |
| 新增链路前端代码类型检查 | 通过 | 对 `index.tsx`、`WherePriorWorkbench.tsx`、`WhereKnowledgeNode.tsx`、知识 fixture 单独执行严格 tsc |
| 前端生产构建 | 通过 | `npm run build` 成功；基座默认跳过全量类型检查，不能据此声称整个基座类型检查通过 |
| 真正 LLM 最小调用 | 通过 | 使用 `.env` 模型与地址，真实返回如下；不是 Mock |

## 真实 LLM 最小调用

问题：`Which cities have population greater than 100 and country is Canada?`

表头：`City, Population, Country`；行：`Toronto/120/Canada`、`Vancouver/90/Canada`、`Seattle/110/USA`。

```json
{
  "httpStatus": 200,
  "status": "SUCCEEDED",
  "conds": [[1, 1, 100], [2, 0, "Canada"]],
  "model": "gpt-5.6-sol",
  "usage": {
    "prompt_tokens": 8600,
    "completion_tokens": 46,
    "total_tokens": 8646,
    "prompt_tokens_details": {"cached_tokens": 4224}
  },
  "latencyMs": 3271,
  "promptSha256": "efe5617823b2ef1d14858c7f9ef8606d1b56f5747301bc6f8fcd9a3ee7e16c04"
}
```

首次沙箱内真实请求因网络权限失败（502 `ResourceAccessException`），随后经联网审批，以相同接口和配置重试成功。验证完成已停止显式开启 LLM 的进程，恢复默认关闭服务。未记录密钥或供应商主机信息。

本结果只证明先验知识进入了请求、人工编辑会改变请求、供应商可被真实调用并返回合法条件。它不证明知识提升准确率，也不代表后续正式实验已完成。页面新增节点和关系的所有组合未做穷举交互测试。

随后首页按用户要求恢复为“运行按钮 + WHERE 知识图”；上表中的浏览器点击 Prepare、编辑、快照恢复是恢复前工作台的历史执行结果，不再描述当前首页的可见功能。恢复后另经 Edge 检查：按钮 1 个、知识图可见、旧面板未显示。当前首页的“运行”仍为基座原有 `flowRun`，不调用 WHERE `/parse`；WHERE API 复核需使用独立脚本。

## 仍需区分的范围

当前工作台完成 WHERE-only 链路，不承担 SELECT/聚合、完整 SQL 执行或 WikiSQL 批量评测。后续正式实验还须固定知识快照、模型采样参数、数据采样与评价器，并配置持久化逐请求审计；本轮未擅自启动这些实验。全项目 `tsc --noEmit` 仍存在原基座无关业务模块的缺失类型/导入错误，未标记为通过。

构建曾分别遇到沙箱禁止下载字体、用户 Maven 配置离线、Windows 正运行 jar 锁定文件，均按原因处理后重跑，最终构建通过。没有跳过后端测试来规避错误。
