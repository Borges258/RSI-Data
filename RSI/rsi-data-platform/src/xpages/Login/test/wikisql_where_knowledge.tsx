/**
 * WikiSQL WHERE prior-knowledge view. This is a knowledge graph, not an Agent DAG.
 * It follows the elements/links shape of the other Login/test canvas fixtures.
 * TABLE knowledge is generated only for a supplied table and never reused across tables.
 */

type CanvasPendantData = { type: string; detail: unknown };

export type WhereKnowledgeScope = 'GLOBAL' | 'DATASET' | 'TABLE';
export type WhereKnowledgeSource = 'HUMAN' | 'WIKISQL_TRAIN' | 'CURRENT_SCHEMA';
export type WhereKnowledgeTrust = 'EXPLICIT_RULE' | 'STATISTICAL_REFERENCE' | 'CURRENT_TABLE_FACT';
export type WhereKnowledgeKind = 'OPERATOR' | 'VALUE' | 'LOGIC' | 'DISAMBIGUATION' | 'COLUMN' | 'PATTERN';
export type WhereSupport = 'SUPPORTED' | 'CLARIFY';

export interface WhereKnowledge {
  id: string;
  title: string;
  kind: WhereKnowledgeKind;
  source: WhereKnowledgeSource;
  scope: WhereKnowledgeScope;
  trust: WhereKnowledgeTrust;
  support: WhereSupport;
  statement: string;
  cues?: string[];
  structured: Record<string, unknown>;
  tableId?: string;
  evidence?: Array<{ trainLine: number; question: string; conds: Array<[number, number, string | number]> }>;
}

export interface WhereKnowledgeRelation {
  id: string;
  from: string;
  to: string;
  relation: 'SUPPORTS' | 'COMBINES_WITH' | 'CONSTRAINS' | 'DISAMBIGUATES' | 'ILLUSTRATES';
  statement: string;
}

export interface WhereKnowledgeTable {
  id: string;
  headers: string[];
  types: string[];
  rows: Array<Array<string | number | boolean | null>>;
  /** Optional author-provided aliases, keyed by zero-based column index. */
  columnAliases?: Record<number, string[]>;
  /** Optional author-provided value aliases; canonical must occur in this table. */
  valueAliases?: Array<{ column: number; alias: string; canonical: string | number | boolean }>;
}

export interface WhereKnowledgeView {
  id: string;
  elements: Array<{
    id: string; name: string; type: 'model'; color: string; x: number; y: number;
    select: boolean; locked: boolean;
    pendant: { data: { type: 'model'; detail: Record<string, unknown> }; onDataChange: (data: CanvasPendantData) => void };
  }>;
  links: Array<{
    id: string; name: string; type: 'relation'; color: string;
    points: Array<{ x: number; y: number }>;
    fromElementId: string; toElementId: string;
    pendant: { data: { type: 'relation'; detail: Record<string, unknown> }; onDataChange: (data: CanvasPendantData) => void };
  }>;
}

const human = (id: string, title: string, kind: WhereKnowledgeKind, support: WhereSupport,
  statement: string, cues: string[], structured: Record<string, unknown>): WhereKnowledge => ({
  id, title, kind, source: 'HUMAN', scope: 'GLOBAL', trust: 'EXPLICIT_RULE',
  support, statement, cues, structured,
});

const train = (id: string, title: string, statement: string, cues: string[],
  structured: Record<string, unknown>, trainLine: number, question: string,
  conds: Array<[number, number, string | number]>): WhereKnowledge => ({
  id, title, kind: 'PATTERN', source: 'WIKISQL_TRAIN', scope: 'DATASET',
  trust: 'STATISTICAL_REFERENCE', support: 'SUPPORTED', statement, cues, structured,
  evidence: [{ trainLine, question, conds }],
});

/** Descriptive counts computed from train.jsonl SQL labels; not per-pattern probabilities. */
export const whereTrainSummary = {
  source: 'dataset/WikiSQL/data/train.jsonl',
  questions: 56355,
  questionsWithWhere: 55932,
  questionsWithMultipleConditions: 16836,
  conditionOperatorCounts: { equals: 63880, greaterThan: 6606, lessThan: 6243 },
  maximumObservedConditions: 4,
  interpretation: 'Descriptive training-set counts only; do not override an explicit question or current table.',
} as const;

/** Train-line references are 1-based lines of dataset/WikiSQL/data/train.jsonl. */
export const whereKnowledgeAtoms: WhereKnowledge[] = [
  human('g-eq', '等值比较 =', 'OPERATOR', 'SUPPORTED',
    '明确的属性等值表达可映射到 WikiSQL 运算符 0；不得只凭介词猜测字段。',
    ['is', 'equals', 'equal to'], { operator: 0, sql: '=', requires: ['column', 'value'] }),
  human('g-gt', '严格大于 >', 'OPERATOR', 'SUPPORTED',
    'more than、greater than、bigger than 表示严格大于，不含边界。',
    ['more than', 'greater than', 'bigger than'], { operator: 1, sql: '>', inclusive: false }),
  human('g-lt', '严格小于 <', 'OPERATOR', 'SUPPORTED',
    'less than、smaller than 表示严格小于，不含边界。',
    ['less than', 'smaller than'], { operator: 2, sql: '<', inclusive: false }),
  human('g-inclusive', '含边界比较', 'OPERATOR', 'SUPPORTED',
    'at least、no less than、at most 等含边界；保留 >= 或 <= 的原始语义，最终仅在当前表上存在等价谓词时转换为 WikiSQL 标准编码。',
    ['at least', 'no less than', 'at most', 'no more than'],
    { semanticOperators: ['>=', '<='], output: 'standardWikiSqlWhenEquivalent', otherwise: 'UNREPRESENTABLE_OPERATOR' }),
  human('g-literal', '精确保留条件值', 'VALUE', 'SUPPORTED',
    '表格中的文本取值优先采用当前表内原样字面量，包括大小写、标点、连字符与括号。',
    ['named', 'for', 'with'], { source: 'current_table_cell', preserveSurfaceForm: true }),
  human('g-threshold', '问题中的数值阈值', 'VALUE', 'SUPPORTED',
    '比较阈值可直接来自问题，不要求出现在表格样本值中；先确认所属字段和单位。',
    ['than', 'above', 'below'], { source: 'question', valueMayBeAbsentFromRows: true }),
  human('g-and', '合取 AND', 'LOGIC', 'SUPPORTED',
    '两个独立 WHERE 限定需要各自保留并按 AND 组合；不能把一个条件的值绑定到另一字段。',
    ['and', 'with', 'where'], { connective: 'AND', preserveAllPredicates: true }),
  human('g-or', '析取 OR', 'LOGIC', 'CLARIFY',
    '明确的 or/任一条件是析取；WikiSQL 当前 WHERE 结构只有合取，不能擅自改成 AND。',
    ['or', 'either'], { connective: 'OR', unsupportedInWikiSql: true, action: 'CLARIFY' }),
  human('g-column', '限定字段与输出字段分离', 'DISAMBIGUATION', 'SUPPORTED',
    'WHERE 条件列须由限定短语、表头、类型和值共同确定；被询问的输出列不必是过滤列。',
    ['where', 'for', 'with'], { bind: 'filter_attribute', distinguishFrom: 'requested_output' }),
  human('g-count', '条件逐项核对', 'DISAMBIGUATION', 'SUPPORTED',
    '将问题中的每个独立限定归为一条谓词，并检查遗漏、重复和把输出描述误作过滤的情况。',
    ['where', 'with', 'and'], { check: ['missing', 'duplicate', 'output_as_filter'], maxWikiSqlConditions: 4 }),
  human('g-ambiguous', '极值语义消歧', 'DISAMBIGUATION', 'CLARIFY',
    'highest/lowest 可能描述被查询结果，也可能限定某行；缺少明确限定对象时不臆造 WHERE 比较。',
    ['highest', 'lowest', 'maximum', 'minimum'], { action: 'CLARIFY', avoidInventedPredicate: true }),
];

/** Illustrative train observations, never absolute rules or gold labels for another table. */
export const whereKnowledgePatterns: WhereKnowledge[] = [
  train('d-implicit-eq', '省略 where 的实体限定',
    '“for South Australia”在训练样例中对应一条等值条件；具体列必须按当前表重新绑定。',
    ['for <entity>'], { shape: 'one_equality_predicate', columnIsTableDependent: true },
    1, 'Tell me what the notes are for South Australia ', [[3, 0, 'SOUTH AUSTRALIA']]),
  train('d-explicit-eq', '显式属性等值',
    '“where/when <field> is <value>”是等值表达的一种观察模式，不保证所有 is 均为 WHERE。',
    ['where <field> is <value>', 'when <field> is <value>'],
    { shape: 'one_equality_predicate', requireFilterAttribute: true },
    10, 'what is the powertrain (engine/transmission) when the order year is 2000?', [[0, 0, '2000']]),
  train('d-gt', '训练集严格大于表达',
    '“bigger than 51092.0”对应严格大于；训练中的阈值可归一为数值。',
    ['bigger than <number>'], { shape: 'numeric_comparison', operator: 1 },
    135, "What's the 2001 census of the region of Abruzzo where the 1871 census is bigger than 51092.0?",
    [[6, 0, 'Abruzzo'], [2, 1, 51092]]),
  train('d-lt-and-gt', '双比较条件',
    '同句的 less than 与 bigger than 可约束不同字段，并以 AND 连接。',
    ['<fieldA> less than <a> and <fieldB> bigger than <b>'],
    { shape: 'two_conjunctive_comparisons', operators: [2, 1] },
    649, 'What is the english title where the premiere is less than 30.0 and the finale is bigger than 36.0?',
    [[5, 2, 30], [6, 1, 36]]),
  train('d-with-two', 'with 引出的第二限定',
    'with 短语可以补充第二个等值限定；是否真的构成条件须依当前问题和 Schema 判断。',
    ['for <entity> with <field> <value>'], { shape: 'two_conjunctive_equalities', needsColumnDisambiguation: true },
    87, 'What is the population for County Mayo with the English Name Carrowteige?',
    [[0, 0, 'County Mayo'], [1, 0, 'Carrowteige']]),
  train('d-literal-form', '复杂文本值原样匹配',
    '带破折号与括号的表格值可能是一整个等值字面量，不应按数字或连词拆解。',
    ['0–0', '3–1'], { shape: 'text_equality', preservePunctuation: true },
    52, "what's the total number of singles w-l with doubles w-l of 0–0 and total w-l of 3–1",
    [[4, 0, '0–0'], [2, 0, '3–1']]),
];

export const whereKnowledgeCombinations: WhereKnowledge[] = [
  human('p-text', '文本等值 WHERE', 'PATTERN', 'SUPPORTED',
    '先定位限定字段，再以当前表的精确取值形成单条等值条件。',
    ['for <entity>', 'where <field> is <text>'],
    { slots: ['filter_column', 'exact_table_value'], operator: 0, conditionCount: 1 }),
  human('p-number', '数值比较 WHERE', 'PATTERN', 'SUPPORTED',
    '从限定短语绑定数值字段，区分严格比较方向，再提取问题中的阈值。',
    ['<field> greater than <number>', '<field> less than <number>'],
    { slots: ['numeric_filter_column', 'comparison_operator', 'question_threshold'], conditionCount: 1 }),
  human('p-multi', '多条件合取 WHERE', 'PATTERN', 'SUPPORTED',
    '逐项识别独立条件，保持每个条件的字段、运算符、值配对，再作 AND 组合。',
    ['<conditionA> and <conditionB>', '<entity> with <attribute> <value>'],
    { slots: ['predicate_1', 'predicate_2'], connective: 'AND', maximumConditions: 4 }),
  human('p-unsupported', '不可表达条件识别', 'PATTERN', 'CLARIFY',
    '析取意图无法直接表达；含边界比较仅在当前表不存在等价标准谓词时停止生成，不能做语义近似。',
    ['at least', 'at most', 'or'], { action: 'CLARIFY_IF_UNREPRESENTABLE', neverApproximate: true }),
];

const relation = (from: string, to: string, type: WhereKnowledgeRelation['relation'], statement: string): WhereKnowledgeRelation => ({
  id: `where-rel-${from}-${to}`, from, to, relation: type, statement,
});

export const whereKnowledgeRelations: WhereKnowledgeRelation[] = [
  relation('g-column', 'p-text', 'CONSTRAINS', '等值条件必须先确认过滤字段'),
  relation('g-eq', 'p-text', 'COMBINES_WITH', '等值运算符参与文本条件'),
  relation('g-literal', 'p-text', 'COMBINES_WITH', '采用当前表的精确取值'),
  relation('g-column', 'p-number', 'CONSTRAINS', '比较条件绑定数值字段'),
  relation('g-gt', 'p-number', 'COMBINES_WITH', '严格大于分支'),
  relation('g-lt', 'p-number', 'COMBINES_WITH', '严格小于分支'),
  relation('g-threshold', 'p-number', 'COMBINES_WITH', '阈值来自问题'),
  relation('g-and', 'p-multi', 'COMBINES_WITH', '独立谓词以 AND 组合'),
  relation('g-count', 'p-multi', 'CONSTRAINS', '检查遗漏与重复'),
  relation('p-text', 'p-multi', 'COMBINES_WITH', '文本条件可参与组合'),
  relation('p-number', 'p-multi', 'COMBINES_WITH', '数值条件可参与组合'),
  relation('g-inclusive', 'p-unsupported', 'CONSTRAINS', '含边界比较仅在无法等价转换时停止'),
  relation('g-or', 'p-unsupported', 'SUPPORTS', '析取不可直接表达'),
  relation('d-implicit-eq', 'p-text', 'ILLUSTRATES', '训练集中的隐式等值样例'),
  relation('d-explicit-eq', 'p-text', 'ILLUSTRATES', '训练集中的显式等值样例'),
  relation('d-gt', 'p-number', 'ILLUSTRATES', '训练集中的严格大于样例'),
  relation('d-lt-and-gt', 'p-multi', 'ILLUSTRATES', '训练集中的双比较样例'),
  relation('d-with-two', 'p-multi', 'ILLUSTRATES', '训练集中的 with 双限定样例'),
  relation('d-literal-form', 'g-literal', 'ILLUSTRATES', '训练集中的复杂文本值样例'),
  relation('g-ambiguous', 'g-count', 'DISAMBIGUATES', '避免凭极值词臆造过滤条件'),
];

function tableKnowledge(table: WhereKnowledgeTable): { nodes: WhereKnowledge[]; links: WhereKnowledgeRelation[] } {
  if (!table.id?.trim()) throw new Error('TABLE knowledge requires a nonempty table id');
  if (table.headers.length !== table.types.length || table.rows.some(row => row.length !== table.headers.length)) {
    throw new Error('Table headers, types and every row must have matching widths');
  }
  if (Object.keys(table.columnAliases ?? {}).some(key => !Number.isInteger(Number(key)) ||
      Number(key) < 0 || Number(key) >= table.headers.length)) {
    throw new Error('Column aliases must reference existing columns in the current table');
  }
  const scopedId = `t-${encodeURIComponent(table.id)}`;
  const nodes: WhereKnowledge[] = [];
  const links: WhereKnowledgeRelation[] = [];
  table.headers.forEach((header, index) => {
    const values = Array.from(new Set(table.rows.map(row => row[index]).filter(value => value !== null)));
    const numeric = /^(real|number|numeric|integer|int|float|double|decimal)$/i.test(table.types[index]);
    const fieldId = `${scopedId}-field-${index}`;
    nodes.push({
      id: fieldId, title: `字段 ${index}: ${header}`, kind: 'COLUMN', source: 'CURRENT_SCHEMA',
      scope: 'TABLE', trust: 'CURRENT_TABLE_FACT', support: 'SUPPORTED', tableId: table.id,
      statement: `当前表字段 ${header} 的类型与取值证据；只在表 ${table.id} 内有效。`,
      structured: { column: index, header, type: table.types[index],
        distinctValues: values, valueEvidenceScope: 'supplied_rows_only',
        operatorGuidance: { equality: 'SUPPORTED', strictNumericComparison: numeric ? 'SUPPORTED' : 'CLARIFY_TYPE_MISMATCH',
          inclusiveComparison: 'CLARIFY_UNSUPPORTED' } },
    });
    links.push(relation(fieldId, 'g-column', 'SUPPORTS', '当前表字段用于过滤列绑定'));
    links.push(relation(fieldId, 'g-literal', 'SUPPORTS', '当前表取值用于精确值匹配'));
  });
  Object.entries(table.columnAliases ?? {}).forEach(([column, aliases]) => {
    aliases.forEach((alias, index) => {
      const id = `${scopedId}-column-alias-${column}-${index}`;
      nodes.push({ id, title: `字段别名: ${alias}`, kind: 'COLUMN', source: 'HUMAN',
        scope: 'TABLE', trust: 'EXPLICIT_RULE', support: 'SUPPORTED', tableId: table.id,
        statement: `人工声明：仅在表 ${table.id} 内，${alias} 可指向字段 ${table.headers[Number(column)]}。`,
        cues: [alias], structured: { column: Number(column), alias, canonicalHeader: table.headers[Number(column)] } });
      links.push(relation(id, `${scopedId}-field-${column}`, 'SUPPORTS', '人工字段别名绑定到当前表字段'));
    });
  });
  table.valueAliases?.forEach((item, index) => {
    const values = table.rows.map(row => row[item.column]);
    if (!Number.isInteger(item.column) || item.column < 0 || item.column >= table.headers.length ||
        !values.some(value => value === item.canonical)) {
      throw new Error(`Value alias ${index} must reference an existing column and cell in the current table`);
    }
    const id = `${scopedId}-alias-${index}`;
    nodes.push({ id, title: `取值别名: ${item.alias}`, kind: 'VALUE', source: 'HUMAN',
      scope: 'TABLE', trust: 'EXPLICIT_RULE', support: 'SUPPORTED', tableId: table.id,
      statement: `人工声明：仅在表 ${table.id} 中，将 ${item.alias} 对齐到字段 ${table.headers[item.column]} 的取值。`,
      cues: [item.alias], structured: { column: item.column, alias: item.alias, canonical: item.canonical } });
    links.push(relation(id, `${scopedId}-field-${item.column}`, 'SUPPORTS', '别名只属于该字段'));
  });
  return { nodes, links };
}

function nodeElement(node: WhereKnowledge, x: number, y: number): WhereKnowledgeView['elements'][number] {
  const color = node.scope === 'TABLE' ? '#13a8a8' : node.scope === 'DATASET' ? '#faad14' :
    node.kind === 'PATTERN' ? '#597ef7' : node.support === 'CLARIFY' ? '#ff7875' : '#73d13d';
  const fields = [
    { name: 'statement', fieldTitle: '知识内容', notes: node.statement, type: 'String' },
    { name: 'scope', fieldTitle: '作用域', notes: node.scope, type: 'String' },
  ];
  return {
    id: node.id, name: node.title, type: 'model', color, x, y, select: false, locked: false,
    pendant: { onDataChange: () => {}, data: { type: 'model', detail: {
      id: node.id, knowledge: node,
      model: { module: 'where_knowledge', modelTitle: node.title, name: node.id, id: node.id, pk: ['id'], fields },
      component: { id: node.id, modelId: node.id, isCollapse: false, x, y },
      fields: fields.map(field => ({ field })), familyId: 'wikisql.where', thirdParty: false,
      size: { width: 200, height: 80 },
    } } },
  };
}

function linkElement(link: WhereKnowledgeRelation): WhereKnowledgeView['links'][number] {
  return {
    id: link.id, name: link.relation, type: 'relation', color: '#8c8c8c', points: [],
    fromElementId: link.from, toElementId: link.to,
    pendant: { onDataChange: () => {}, data: { type: 'relation', detail: { id: link.id, knowledgeRelation: link,
      link: { id: link.id, relationId: link.id, color: '#8c8c8c', points: [] }, thirdParty: false } } },
  };
}

/** Pass the result to VisualDiagram.setView; omit table until a specific table is selected. */
export function createWikiSqlWhereKnowledgeView(table?: WhereKnowledgeTable): WhereKnowledgeView {
  const scoped = table ? tableKnowledge(table) : { nodes: [], links: [] };
  const global = [...whereKnowledgeAtoms, ...whereKnowledgePatterns, ...whereKnowledgeCombinations];
  const nodes = [...global, ...scoped.nodes];
  const elements = nodes.map((node, index) => {
    let x: number; let y: number;
    if (node.scope === 'GLOBAL' && node.kind !== 'PATTERN') {
      const position = whereKnowledgeAtoms.findIndex(item => item.id === node.id);
      x = 80 + (position % 3) * 280; y = 80 + Math.floor(position / 3) * 145;
    } else if (node.scope === 'DATASET') {
      const position = whereKnowledgePatterns.findIndex(item => item.id === node.id);
      x = 950 + (position % 2) * 280; y = 80 + Math.floor(position / 2) * 145;
    } else if (node.scope === 'GLOBAL') {
      const position = whereKnowledgeCombinations.findIndex(item => item.id === node.id);
      x = 1530; y = 80 + position * 180;
    } else {
      x = 1830 + ((index - global.length) % 2) * 280;
      y = 80 + Math.floor((index - global.length) / 2) * 145;
    }
    return nodeElement(node, x, y);
  });
  return { id: table ? `wikisql-where-${table.id}` : 'wikisql-where-global',
    elements, links: [...whereKnowledgeRelations, ...scoped.links].map(linkElement) };
}

/** Ready-to-load global/DATASET canvas fixture, in the same style as user_favorites_display. */
export const wikisql_where_knowledge = createWikiSqlWhereKnowledgeView();

/**
 * Extract structured knowledge from a canvas snapshot after manual repositioning/link editing.
 * TABLE nodes from other tables are excluded; cross-table TABLE relations are rejected.
 */
export function extractWikiSqlWhereKnowledge(view: WhereKnowledgeView, activeTableId?: string) {
  const all = new Map<string, WhereKnowledge>();
  for (const element of view.elements) {
    const node = element.pendant?.data?.detail?.knowledge as WhereKnowledge | undefined;
    if (!node || node.id !== element.id || all.has(node.id)) throw new Error(`Invalid or duplicate knowledge node: ${element.id}`);
    if ((node.scope === 'GLOBAL' && (node.source !== 'HUMAN' || node.trust !== 'EXPLICIT_RULE')) ||
        (node.scope === 'DATASET' && (node.source !== 'WIKISQL_TRAIN' || node.trust !== 'STATISTICAL_REFERENCE')) ||
        (node.scope === 'TABLE' && (!node.tableId || !((node.source === 'CURRENT_SCHEMA' && node.trust === 'CURRENT_TABLE_FACT') ||
          (node.source === 'HUMAN' && node.trust === 'EXPLICIT_RULE'))))) {
      throw new Error(`Invalid provenance/scope contract: ${node.id}`);
    }
    all.set(node.id, node);
  }
  const selected = new Map(Array.from(all).filter(([, node]) => node.scope !== 'TABLE' || node.tableId === activeTableId));
  const relations: WhereKnowledgeRelation[] = [];
  const relationIds = new Set<string>();
  for (const edge of view.links) {
    const item = edge.pendant?.data?.detail?.knowledgeRelation as WhereKnowledgeRelation | undefined;
    if (!item || relationIds.has(edge.id) || item.id !== edge.id || item.from !== edge.fromElementId || item.to !== edge.toElementId ||
        !all.has(item.from) || !all.has(item.to)) throw new Error(`Invalid knowledge relation: ${edge.id}`);
    relationIds.add(edge.id);
    const from = all.get(item.from)!; const to = all.get(item.to)!;
    if (from.scope === 'TABLE' && to.scope === 'TABLE' && from.tableId !== to.tableId) {
      throw new Error(`Cross-table knowledge relation is forbidden: ${edge.id}`);
    }
    if (selected.has(item.from) && selected.has(item.to)) relations.push(item);
  }
  return {
    version: 'wikisql-where-prior-v1', activeTableId: activeTableId ?? null,
    atoms: Array.from(selected.values()).filter(node => node.kind !== 'PATTERN'),
    patterns: Array.from(selected.values()).filter(node => node.kind === 'PATTERN'),
    relations,
    datasetSummary: whereTrainSummary,
    precedence: ['CURRENT_TABLE_FACT', 'EXPLICIT_RULE', 'STATISTICAL_REFERENCE'] as const,
    note: 'Trust precedence applies within the same fact type: current table facts ground fields/values; explicit rules govern operator meaning; train patterns are references, not rules. CLARIFY nodes identify unsupported WikiSQL WHERE semantics.',
  };
}
