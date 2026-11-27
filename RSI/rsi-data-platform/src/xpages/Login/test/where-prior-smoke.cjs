// Reuses the exact canvas fixture/extractor used by the UI; no browser secrets or WikiSQL test data.
const fs = require('node:fs');
const path = require('node:path');
const Module = require('node:module');
const ts = require('typescript');

const source = path.join(__dirname, 'wikisql_where_knowledge.tsx');
const compiled = ts.transpileModule(fs.readFileSync(source, 'utf8'), {
  compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 },
}).outputText;
const fixture = new Module(source, module);
fixture.filename = source;
fixture.paths = Module._nodeModulePaths(__dirname);
fixture._compile(compiled, source);

const table = {
  id: 'smoke-cities',
  headers: ['City', 'Population', 'Country'],
  types: ['text', 'real', 'text'],
  rows: [['Toronto', 120, 'Canada'], ['Vancouver', 90, 'Canada'], ['Seattle', 110, 'USA']],
};
const view = fixture.exports.createWikiSqlWhereKnowledgeView(table);
const knowledge = fixture.exports.extractWikiSqlWhereKnowledge(view, table.id);
const payload = {
  question: 'Which cities have population greater than 100 and country is Canada?',
  table, knowledge,
};

(async () => {
  const mode = process.argv.includes('--llm') ? 'parse' : 'prepare';
  const response = await fetch(`http://127.0.0.1:18086/api/where-prior/${mode}`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload),
  });
  const result = await response.json();
  console.log(JSON.stringify({ httpStatus: response.status, result }, null, 2));
  if (response.status !== 200 || (mode === 'parse' && result.status !== 'SUCCEEDED')) process.exitCode = 1;
})().catch(error => { console.error(error.message); process.exitCode = 1; });
