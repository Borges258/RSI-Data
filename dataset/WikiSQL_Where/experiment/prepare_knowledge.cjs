// Compile the existing canvas fixture once and prepare one immutable graph per table.
const fs = require('fs');
const path = require('path');
const Module = require('module');
const ts = require('../../../RSI/rsi-data-platform/node_modules/typescript');
const source = path.resolve(__dirname, '../../../RSI/rsi-data-platform/src/xpages/Login/test/wikisql_where_knowledge.tsx');
const compiled = ts.transpileModule(fs.readFileSync(source, 'utf8'), {
  compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 },
}).outputText;
const mod = new Module(source, module);
mod.filename = source;
mod.paths = Module._nodeModulePaths(path.dirname(source));
mod._compile(compiled, source);
const { createWikiSqlWhereKnowledgeView, extractWikiSqlWhereKnowledge } = mod.exports;
const tables = JSON.parse(fs.readFileSync(0, 'utf8'));
const result = {};
for (const table of tables) {
  if (result[table.id]) continue;
  const flowTable = { ...table, headers: table.header };
  const view = createWikiSqlWhereKnowledgeView(flowTable);
  result[table.id] = {
    structure: JSON.stringify(view),
    knowledge: extractWikiSqlWhereKnowledge(view, table.id),
  };
}
process.stdout.write(JSON.stringify(result));
