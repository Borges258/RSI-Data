# WHERE 先验知识最小链路

这是独立于原 Spring 基座业务 Bean 的本地验证服务，只处理 WikiSQL 风格的 `WHERE` 条件，不执行 SQL、不输出 `SELECT`，也不读取或评测 WikiSQL Dev/Test 集。

## 启动

从 `F:\Agent_LAB` 根目录的 `.env` 读取 `base_url`、`api_key`、`model`，仅映射到后端进程环境变量。密钥不会发给浏览器。

```powershell
cd F:\Agent_LAB\RSI\rsi-data-spring-boot\where-prior
.\run-where-prior.ps1
```

默认监听 `127.0.0.1:18086`，且 `WHERE_PRIOR_ALLOW_LLM=false`。仅在准备好做一次真实模型调用时，显式使用 `./run-where-prior.ps1 -EnableLlm`。已有最新 fat jar 时可加 `-SkipBuild`。构建脚本执行测试并打包；Maven 缺失时使用 `F:\Agent_LAB\归档\tools\apache-maven-3.9.9`，Java 缺失时使用 `F:\Agent_LAB\tools\jdk8`。

另开终端启动前端：

```powershell
cd F:\Agent_LAB\RSI\rsi-data-platform
npm run dev
```

打开 `http://localhost:3000/`。首页已按用户要求恢复为“右上角运行按钮 + 下方 WHERE 知识图”，不展示此前验证用的表格、问题和结果面板。该“运行”按钮沿用 platform 基座原有的 `flowRun`，**不调用本模块的 `/parse`**。要复核本模块链路，使用下方独立烟测脚本；`/parse` 在默认配置下返回 503 `LLM_DISABLED`。调用供应商的凭据只能放在后端，不能放进 `NEXT_PUBLIC_*` 变量。

旧工作台组件保留在 `src/xpages/Login/test/WherePriorWorkbench.tsx`，但当前首页不引用它；首页只展示知识图和原有运行按钮。训练知识仍作为参考，不应人工伪装成当前表事实。

## 可复核的最小检查

```powershell
Invoke-RestMethod http://127.0.0.1:18086/api/where-prior/health
cd F:\Agent_LAB\RSI\rsi-data-platform
node src/xpages/Login/test/where-prior-smoke.cjs
node src/xpages/Login/test/where-prior-browser-smoke.cjs
```

浏览器脚本使用本机 Edge 无界面模式，检查首页只显示运行按钮和知识图，不点击按钮、不会调用供应商。仅在服务以 `-EnableLlm` 启动后，执行 `node src/xpages/Login/test/where-prior-smoke.cjs --llm` 做一次自造小表真实调用。预期条件为 `[[1,1,100],[2,0,"Canada"]]`；模型输出仍可能有随机性，因此脚本只检验接口状态与结构，不把单次输出当成准确率结论。

本服务的知识契约是 `wikisql-where-prior-v1`。`TABLE` 知识必须与当前表 ID 一致，图中不存在的节点不能被关系引用；超长 Prompt 被拒绝而非截断。支持 WikiSQL `=`、`>`、`<` 与最多四条 AND 条件；`OR`、`>=`、`<=` 等不可表示意图要求模型返回 `CLARIFY`。后续正式实验仍需另行设计固定数据、输出审计及评价器。本模块没有替代原始 Baseline。

前端基座当前的 `next.config.ts` 设置了 `typescript.ignoreBuildErrors=true`；`npm run build` 可以通过，但独立的 `tsc --noEmit` 仍会报告多处原基座缺失模块/类型错误。本轮没有修改这些无关业务组件。静态导出的 `rewrites` 警告也来自基座配置；WHERE 工作台直接访问本地服务，不依赖该 rewrite。

## 回档

基座文件变更和新文件的边界见 `F:\Agent_LAB\RSI\rsi-data-platform\WHERE_PRIOR_BASE_CHANGELOG.md`。不要对用户先前未提交的 `index.tsx` 改动直接执行 `git checkout`。
