// WHERE-only comparison using the same SQLite operator and value rules as the E4 evaluator.
const fs = require('fs');
const path = require('path');
const { DatabaseSync } = require('node:sqlite');
const base = __dirname;
const outputDir = process.env.E3_RESULT_DIR
  ? path.resolve(process.env.E3_RESULT_DIR) : path.join(base, 'results/e3_testline');
const rows = fs.readFileSync(path.join(outputDir, 'evaluated.jsonl'), 'utf8')
  .trim().split(/\r?\n/).map(JSON.parse);
const db = new DatabaseSync(path.join(base, 'data/test.db'), { readOnly: true });
const ops = ['=', '>', '<'];
const quote = x => '"' + x.replaceAll('"', '""') + '"';
function wikiNumber(value) {
  if (typeof value === 'number') return value;
  const clean = String(value).trim().replaceAll(',', '');
  const number = Number(clean);
  if (Number.isFinite(number) && clean !== '') return number;
  const match = clean.match(/[-+]?\d*\.?\d+/);
  if (!match) throw Error(`cannot parse numeric value: ${value}`);
  return Number(match[0]);
}
function selected(tableId, conds) {
  const name = tableId.startsWith('table') ? tableId : 'table_' + tableId.replaceAll('-', '_');
  const schema = db.prepare(`PRAGMA table_info(${quote(name)})`).all().map(x => x.type.toLowerCase());
  const clauses = [], values = [];
  for (const [column, operator, raw] of conds) {
    if (!Number.isInteger(column) || column < 0 || column >= schema.length || !ops[operator]) throw Error('invalid condition');
    clauses.push(`col${column} ${ops[operator]} ?`);
    values.push(schema[column] === 'real' ? wikiNumber(raw) : String(raw).toLowerCase());
  }
  return db.prepare(`SELECT rowid FROM ${quote(name)}${clauses.length ? ' WHERE ' + clauses.join(' AND ') : ''}`)
    .all(...values).map(x => Number(x.rowid)).sort((a, b) => a - b);
}
const norm = x => JSON.stringify(x.map(([c, op, value]) => [c, op, String(value).toLowerCase()]).map(JSON.stringify).sort());
const byCategory = {};
for (const row of rows) {
  const category = byCategory[row.category] ??= { n: 0, completed: 0, logicalForm: 0, whereRows: 0, errors: 0 };
  category.n++;
  category.completed += Number(row.completed);
  row.logicalForm = row.completed && norm(row.predicted) === norm(row.gold);
  category.logicalForm += Number(row.logicalForm);
  try {
    row.goldRows = selected(row.table_id, row.gold);
    row.predictedRows = row.completed ? selected(row.table_id, row.predicted) : null;
    row.whereRowsEqual = row.completed && JSON.stringify(row.goldRows) === JSON.stringify(row.predictedRows);
    category.whereRows += Number(row.whereRowsEqual);
  } catch (error) { row.executionError = String(error); category.errors++; }
}
db.close();
fs.writeFileSync(path.join(outputDir, 'compared.jsonl'), rows.map(JSON.stringify).join('\n') + '\n');
console.log(JSON.stringify({ total: rows.length, completed: rows.filter(x => x.completed).length,
  logicalForm: rows.filter(x => x.logicalForm).length, whereRows: rows.filter(x => x.whereRowsEqual).length,
  byCategory }, null, 2));
