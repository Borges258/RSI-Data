# WHERE 先验知识链路：基座改动与回档边界

本记录只涉及 `RSI` 下重新克隆的三个项目，未读取或修改旧 `repos` 项目。

| 项目 | 基座原有文件改动 | 作用 | 回档注意 |
| --- | --- | --- | --- |
| `rsi-data-platform` | `src/xpages/Login/index.tsx` | 已恢复原有“右上角运行按钮 + 下方画布”布局、原有 `flowRun` 入口及基座渲染组件；画布载入 WHERE 知识图 | 修改前此文件已有用户未提交的知识画布入口改动；不可盲目 `git checkout`，需人工按差异恢复 |
| `rsi-data-spring-boot` | 无 | WHERE 服务是独立新增的 `where-prior/` Maven 模块，不加入原基座父 POM，不扫描原业务 Bean | 若不再需要，可单独移除该模块；不会影响原 Spring 服务 |
| `rsi-data-visual` | 无 | 复用其现有 `VisualDiagram` API 和打包产物 | 无需回档 |

本次新增而非覆盖基座的前端文件：`src/xpages/Login/test/WherePriorWorkbench.tsx`、`WhereKnowledgeNode.tsx`、`where-prior-smoke.cjs`、`where-prior-browser-smoke.cjs`。前两个文件现已不再由首页引用，保留作此前链路验证代码；浏览器脚本已调整为检查恢复后的首页。`wikisql_where_knowledge.tsx` 在本轮开始前已经是未跟踪文件，属于先前的知识图设计；本轮未改它。其他既有未跟踪文件（如 `.idea/`、`pnpm-lock.yaml`、`pnpm-workspace.yaml`）未触碰。

回档时先恢复 `index.tsx` 所需的用户原有知识画布入口，再移除本轮新文件；不要对整个仓库做重置。`where-prior` 服务的输出和配置与原 Spring 基座隔离。
