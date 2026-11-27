// Run only the fixed 26-example Testline stratum through the real E3 Flow API.
const fs = require('fs');
const path = require('path');
const Module = require('module');
const ts = require('../../RSI/rsi-data-platform/node_modules/typescript');

const root = __dirname;
const data = path.join(root, 'data');
const lines = name => fs.readFileSync(path.join(data, name), 'utf8').trim().split(/\r?\n/).map(JSON.parse);
const samples = lines('test.jsonl');
const tables = Object.fromEntries(lines('test.tables.jsonl').map(table => [table.id, table]));
const manifest = JSON.parse(fs.readFileSync(path.join(root, 'sample_manifest.json'), 'utf8'));
const source = path.resolve(root, '../../RSI/rsi-data-platform/src/xpages/Login/test/wikisql_where_knowledge.tsx');
const compiled = ts.transpileModule(fs.readFileSync(source, 'utf8'), {
  compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 },
}).outputText;
const knowledgeModule = new Module(source, module);
knowledgeModule.filename = source;
knowledgeModule.paths = Module._nodeModulePaths(path.dirname(source));
knowledgeModule._compile(compiled, source);
const { createWikiSqlWhereKnowledgeView, extractWikiSqlWhereKnowledge } = knowledgeModule.exports;

if (samples.length !== 26 || manifest.samples.length !== samples.length) throw Error('Testline manifest mismatch');
const outputDir = process.env.E3_RESULT_DIR
  ? path.resolve(process.env.E3_RESULT_DIR) : path.join(root, 'results', 'e3_testline');
fs.mkdirSync(outputDir, { recursive: true });
const rawPath = path.join(outputDir, 'raw.jsonl');
const evaluatedPath = path.join(outputDir, 'evaluated.jsonl');
const completed = new Set();
if (fs.existsSync(evaluatedPath)) for (const line of fs.readFileSync(evaluatedPath, 'utf8').split(/\r?\n/)) {
  if (line.trim()) completed.add(JSON.parse(line).example_id);
}
const endpoint = process.env.WHERE_FLOW_API_URL || 'http://127.0.0.1:30845/api/engine/flow';
const flowId = '@com.horsecoder.common.whuTalent.dataTesting.core/WIKISQL_WHERE_GENERATION';
const same = (a, b) => JSON.stringify(a.map(JSON.stringify).sort()) === JSON.stringify(b.map(JSON.stringify).sort());

async function main() {
  const selected = process.env.E3_SAMPLE_INDEX === undefined ? samples.map((_, i) => i) : [Number(process.env.E3_SAMPLE_INDEX)];
  for (const i of selected) {
    const sample = samples[i], meta = manifest.samples[i];
    if (!sample || !meta || sample.table_id !== meta.table_id) throw Error(`sample mismatch at ${i}`);
    if (completed.has(meta.example_id)) continue;
    const table = tables[sample.table_id];
    if (!table) throw Error(`missing table for ${meta.example_id}`);
    const flowTable = { ...table, headers: table.header };
    const view = createWikiSqlWhereKnowledgeView(flowTable);
    const knowledge = extractWikiSqlWhereKnowledge(view, table.id);
    // JSON round trip matches the canvas snapshot sent by the homepage.
    const request = { question: sample.question, table: flowTable,
      structure: JSON.stringify(view), knowledge };
    const started = Date.now();
    let parsed, transportError;
    try {
      const response = await fetch(endpoint, { method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ flowId, params: { request: JSON.stringify(request) } }),
        signal: AbortSignal.timeout(600000) });
      const body = await response.json();
      if (!response.ok) throw Error(`Flow HTTP ${response.status}: ${JSON.stringify(body).slice(0, 400)}`);
      const value = body?.data?.value ?? body?.data ?? body?.value ?? body;
      const result = value?.result ?? value;
      parsed = typeof result === 'string' ? JSON.parse(result) : result;
    } catch (error) { transportError = String(error); }
    const row = { index: i, ...meta, question: sample.question, gold: sample.sql.conds,
      predicted: Array.isArray(parsed?.conds) ? parsed.conds : null,
      status: parsed?.status ?? 'TRANSPORT_ERROR', stage: parsed?.stage ?? null,
      completed: parsed?.completed === true, exact: parsed?.completed === true && same(parsed.conds, sample.sql.conds),
      elapsedMs: Date.now() - started, errors: parsed?.errors ?? (transportError ? [transportError] : []),
      modelCalls: Object.fromEntries(Object.entries(parsed?.request?.stageResults ?? {}).filter(([, v]) => v?.modelCall)
        .map(([stage, v]) => [stage, v.modelCall])),
    };
    fs.appendFileSync(rawPath, JSON.stringify({ index: i, ...meta, response: parsed ?? null, transportError }) + '\n');
    fs.appendFileSync(evaluatedPath, JSON.stringify(row) + '\n');
    console.log(`${i + 1}/${samples.length} ${meta.category} ${row.status} ${row.stage ?? ''} exact=${row.exact} modelCalls=${Object.keys(row.modelCalls).length} ${row.elapsedMs}ms`);
  }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
