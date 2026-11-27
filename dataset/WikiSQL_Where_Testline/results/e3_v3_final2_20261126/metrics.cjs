const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const dir = __dirname;
const testline = path.resolve(dir, '../..');
const lab = path.resolve(testline, '../..');
const baselineDir = path.resolve(dir, '../e3_testline_round2_final_20261126');
const lines = file => fs.readFileSync(file, 'utf8').trim().split(/\r?\n/).map(JSON.parse);
const sha = data => crypto.createHash('sha256').update(data).digest('hex');

// A comparison key only. Predicted literals and gold labels are never rewritten.
function valueKey(value) {
  let text = String(value).normalize('NFKC').trim().toLowerCase().replace(/\s+/g, ' ');
  if ((text.startsWith('"') && text.endsWith('"')) || (text.startsWith("'") && text.endsWith("'"))
      || (text.startsWith('“') && text.endsWith('”'))) text = text.slice(1, -1).trim();
  if (/^[+-]?(?:\d{1,3}(?:,\d{3})+|\d+)(?:\.\d+)?$/.test(text))
    return String(Number(text.replaceAll(',', '')));
  return text.replace(/\s*([,.;:!?])\s*/g, '$1');
}

function recall(rows) {
  let gold = 0, fieldAndValue = 0, full = 0;
  for (const row of rows) {
    const used = new Set();
    for (const expected of row.gold) {
      gold++;
      const hit = (row.predicted || []).findIndex((actual, index) => !used.has(index)
        && actual[0] === expected[0] && valueKey(actual[2]) === valueKey(expected[2]));
      if (hit < 0) continue;
      used.add(hit);
      fieldAndValue++;
      if (row.predicted[hit][1] === expected[1]) full++;
    }
  }
  return { gold, fieldAndValue, full, rate: fieldAndValue / gold, fullRate: full / gold };
}

function summarize(folder) {
  const compared = lines(path.join(folder, 'compared.jsonl'));
  const raw = lines(path.join(folder, 'raw.jsonl'));
  if (compared.length !== 26 || raw.length !== 26) throw Error('Expected exactly 26 records');
  let calls = 0, promptTokens = 0, cachedTokens = 0, completionTokens = 0;
  let reviewCalls = 0;
  for (const record of raw) for (const [stage, result] of Object.entries(record.response?.request?.stageResults || {})) {
    const modelCalls = Array.isArray(result.reviewModelCalls) ? result.reviewModelCalls
      : result.modelCall ? [result.modelCall] : [];
    for (const call of modelCalls) {
      calls++;
      if (stage === 'WHERE_COMPLETENESS_CHECK' || stage === 'WHERE_SCHEMA_VALUE_MATCH') reviewCalls++;
      promptTokens += call.usage?.prompt_tokens || 0;
      cachedTokens += call.usage?.prompt_tokens_details?.cached_tokens || 0;
      completionTokens += call.usage?.completion_tokens || 0;
    }
  }
  const statuses = {};
  for (const row of compared) statuses[row.status] = (statuses[row.status] || 0) + 1;
  return {
    completed: compared.filter(row => row.completed).length,
    strict: compared.filter(row => row.exact).length,
    caseNormalized: compared.filter(row => row.logicalForm).length,
    whereRowsEqual: compared.filter(row => row.whereRowsEqual).length,
    nonemptyWhereRowsEqual: compared.filter(row => row.whereRowsEqual
      && row.goldRows?.length > 0 && row.predictedRows?.length > 0).length,
    bothEmptyRows: compared.filter(row => row.whereRowsEqual && row.goldRows?.length === 0).length,
    goldNonemptyRows: compared.filter(row => row.goldRows?.length > 0).length,
    conditionRecallGoldProxy: recall(compared),
    modelCallsObserved: calls, conditionalReviewCallsObserved: reviewCalls,
    usage: { promptTokens, cachedTokens, uncachedPromptTokens: promptTokens - cachedTokens,
      completionTokens, totalTokens: promptTokens + completionTokens },
    statuses,
  };
}

const service = path.join(lab, 'RSI/rsi-data-spring-boot/where-prior/src/main/java/com/horsecoder/agentlab/whereprior/WherePriorController.java');
const source = fs.readFileSync(service, 'utf8');
const promptBlocks = [
  source.slice(source.indexOf('private static final String SKILL_SYSTEM'), source.indexOf('private final ObjectMapper')),
  source.slice(source.indexOf('switch (task)'), source.indexOf('JsonNode request = input.path("request")')),
];
if (promptBlocks.some(block => block.length < 100)) throw Error('Prompt source boundaries missing');
const sourcePaths = [
  'RSI/rsi-data-spring-boot/where-prior/src/main/java/com/horsecoder/agentlab/whereprior/WherePriorController.java',
  ...fs.readdirSync(path.join(lab, 'RSI/rsi-data-spring-boot/core-flow/src/main/java/com/horsecoder/common/whuTalent/core/flow/Skill/Where'))
    .filter(name => name.endsWith('.java')).map(name => 'RSI/rsi-data-spring-boot/core-flow/src/main/java/com/horsecoder/common/whuTalent/core/flow/Skill/Where/' + name),
  'RSI/rsi-data-spring-boot/core-flow/src/main/resources/flow/Where/flow-wikisql-where-generation.json',
  'dataset/WikiSQL_Where_Testline/run_e3.cjs',
  'dataset/WikiSQL_Where_Testline/evaluate_e3.cjs',
  'dataset/WikiSQL_Where_Testline/sample_manifest.json',
  'dataset/WikiSQL_Where_Testline/data/test.jsonl',
  'dataset/WikiSQL_Where_Testline/data/test.tables.jsonl',
  'dataset/WikiSQL_Where_Testline/data/test.db',
];
const sourceSha256 = {};
for (const relative of sourcePaths) {
  const file = path.join(lab, relative);
  if (!fs.existsSync(file)) throw Error('Missing source: ' + relative);
  sourceSha256[relative] = sha(fs.readFileSync(file));
}
const current = summarize(dir), baseline = summarize(baselineDir);
const currentRows = lines(path.join(dir, 'compared.jsonl'));
const baselineRows = new Map(lines(path.join(baselineDir, 'compared.jsonl')).map(row => [row.example_id, row]));
const sampleChanges = currentRows.map(row => {
  const old = baselineRows.get(row.example_id);
  return { exampleId: row.example_id, category: row.category,
    before: { status: old.status, predicted: old.predicted, strict: old.exact,
      caseNormalized: old.logicalForm, rowsEqual: old.whereRowsEqual },
    after: { status: row.status, predicted: row.predicted, strict: row.exact,
      caseNormalized: row.logicalForm, rowsEqual: row.whereRowsEqual } };
});
const manifest = {
  version: 'E3-v3', sampleCount: 26, model: 'gpt-5.6-sol',
  experiment: 'fixed Testline 26, real E3 flow; not the 596-example formal run',
  promptDefinitionSha256: sha(promptBlocks.join('\n')),
  sourceSha256,
  recordSha256: Object.fromEntries(['raw.jsonl', 'evaluated.jsonl', 'compared.jsonl']
    .map(name => [name, sha(fs.readFileSync(path.join(dir, name)))])),
  baseline,
  current,
  metricNotes: {
    caseNormalized: 'Existing evaluator lowercases values, ignores condition order; does not remove punctuation or spaces.',
    conditionRecallGoldProxy: 'Micro recall of column and safe-normalized value against gold; ignores operator. Conflicted gold labels limit interpretation.',
    modelCallsObserved: 'Count of persisted successful provider responses in stageResults. No model calls are inferred from errors.',
    nonemptyWhereRowsEqual: 'Both row sets equal and nonempty, reported out of all 26 and 14 gold-nonempty cases.',
    falseHalts: 'No halt proven erroneous from logs; four grounding conflicts remain unresolved, not counted as model errors without label review.',
  },
  sampleChanges,
};
fs.writeFileSync(path.join(dir, 'run_manifest.json'), JSON.stringify(manifest, null, 2) + '\n');
console.log(JSON.stringify({ baseline, current, promptDefinitionSha256: manifest.promptDefinitionSha256 }, null, 2));
