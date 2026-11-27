'use client';

import React, { useEffect, useState } from 'react';
import {
  createWikiSqlWhereKnowledgeView,
  extractWikiSqlWhereKnowledge,
  type WhereKnowledgeTable,
  type WhereKnowledgeView,
  type WhereKnowledge,
  type WhereKnowledgeRelation,
} from './wikisql_where_knowledge';

/** Self-contained smoke table. No WikiSQL dataset examples or labels are sent as test questions. */
export const wherePriorDemoTable: WhereKnowledgeTable = {
  id: 'smoke-cities',
  headers: ['City', 'Population', 'Country'],
  types: ['text', 'real', 'text'],
  rows: [['Toronto', 120, 'Canada'], ['Vancouver', 90, 'Canada'], ['Seattle', 110, 'USA']],
};

const endpoint = process.env.NEXT_PUBLIC_WHERE_PRIOR_API_URL ?? 'http://127.0.0.1:18086/api/where-prior';

export default function WherePriorWorkbench({ diagramRef }: { diagramRef: React.RefObject<any> }) {
  const [tableText, setTableText] = useState(JSON.stringify(wherePriorDemoTable, null, 2));
  const [table, setTable] = useState<WhereKnowledgeTable>(wherePriorDemoTable);
  const [question, setQuestion] = useState('Which cities have population greater than 100 and country is Canada?');
  const [result, setResult] = useState('');
  const [busy, setBusy] = useState(false);
  const [nodeIds, setNodeIds] = useState<string[]>([]);
  const [selectedId, setSelectedId] = useState('');
  const [statement, setStatement] = useState('');
  const [newTitle, setNewTitle] = useState('');
  const [newStatement, setNewStatement] = useState('');
  const [newScope, setNewScope] = useState<'GLOBAL' | 'TABLE'>('GLOBAL');
  const [linkFrom, setLinkFrom] = useState('');
  const [linkTo, setLinkTo] = useState('');
  const [linkType, setLinkType] = useState<WhereKnowledgeRelation['relation']>('SUPPORTS');
  const [linkStatement, setLinkStatement] = useState('');
  const [snapshot, setSnapshot] = useState('');

  const updateSelection = (view: WhereKnowledgeView, preferredId?: string) => {
    const ids = view.elements.map(element => element.id);
    const id = preferredId && ids.includes(preferredId) ? preferredId : ids[0] ?? '';
    setNodeIds(ids);
    setSelectedId(id);
    setStatement(view.elements.find(element => element.id === id)?.pendant.data.detail.knowledge
      ? (view.elements.find(element => element.id === id)!.pendant.data.detail.knowledge as WhereKnowledge).statement : '');
    setLinkFrom(ids.includes(linkFrom) ? linkFrom : ids[0] ?? '');
    setLinkTo(ids.includes(linkTo) ? linkTo : ids[1] ?? ids[0] ?? '');
  };

  const replaceView = (view: WhereKnowledgeView, preferredId?: string) => {
    diagramRef.current?.setView(view);
    updateSelection(view, preferredId);
  };

  const currentView = (): WhereKnowledgeView => {
    const view = diagramRef.current?.getView();
    if (!view) throw new Error('画布尚未就绪');
    return view as unknown as WhereKnowledgeView;
  };

  useEffect(() => {
    replaceView(createWikiSqlWhereKnowledgeView(wherePriorDemoTable));
    // The diagram ref is stable; only initialize the fixture on mount.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [diagramRef]);

  const loadTable = () => {
    try {
      const next = JSON.parse(tableText) as WhereKnowledgeTable;
      const view = createWikiSqlWhereKnowledgeView(next);
      replaceView(view);
      setTable(next);
      setResult(`已载入表 ${next.id}；画布中的 TABLE 知识已按此表重建。`);
    } catch (error) {
      setResult(`载入失败：${error instanceof Error ? error.message : String(error)}`);
    }
  };

  const editSelected = () => {
    try {
      if (!statement.trim()) throw new Error('知识内容不能为空');
      const view = currentView();
      const elements = view.elements.map(element => {
        if (element.id !== selectedId) return element;
        const detail = element.pendant.data.detail;
        const knowledge = detail.knowledge as WhereKnowledge;
        return { ...element, pendant: { ...element.pendant, data: { ...element.pendant.data,
          detail: { ...detail, knowledge: { ...knowledge, statement: statement.trim() } } } } };
      });
      replaceView({ ...view, elements }, selectedId);
      setResult(`知识 ${selectedId} 已更新；下一次 Prepare/Parse 将使用新内容。`);
    } catch (error) { setResult(`编辑失败：${error instanceof Error ? error.message : String(error)}`); }
  };

  const addNode = () => {
    try {
      if (!newTitle.trim() || !newStatement.trim()) throw new Error('新节点标题和内容不能为空');
      const view = currentView();
      const id = `manual-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
      const knowledge: WhereKnowledge = {
        id, title: newTitle.trim(), kind: 'DISAMBIGUATION', source: 'HUMAN', scope: newScope,
        trust: 'EXPLICIT_RULE', support: 'SUPPORTED', statement: newStatement.trim(),
        structured: {}, ...(newScope === 'TABLE' ? { tableId: table.id } : {}),
      };
      const element: WhereKnowledgeView['elements'][number] = {
        id, name: knowledge.title, type: 'model', color: newScope === 'TABLE' ? '#13a8a8' : '#73d13d',
        x: 80, y: 750, select: false, locked: false,
        pendant: { data: { type: 'model', detail: { knowledge } }, onDataChange: () => {} },
      };
      replaceView({ ...view, elements: [...view.elements, element] }, id);
      setNewTitle(''); setNewStatement('');
      setResult(`已在画布添加 ${newScope} 知识 ${id}。`);
    } catch (error) { setResult(`添加失败：${error instanceof Error ? error.message : String(error)}`); }
  };

  const removeSelected = () => {
    try {
      const view = currentView();
      replaceView({ ...view, elements: view.elements.filter(element => element.id !== selectedId),
        links: view.links.filter(link => link.fromElementId !== selectedId && link.toElementId !== selectedId) });
      setResult(`已移除知识 ${selectedId} 及其关联。`);
    } catch (error) { setResult(`移除失败：${error instanceof Error ? error.message : String(error)}`); }
  };

  const addRelation = () => {
    try {
      if (!linkFrom || !linkTo || linkFrom === linkTo) throw new Error('请选择两个不同的知识节点');
      if (!linkStatement.trim()) throw new Error('请明确填写知识之间的语义关联');
      const view = currentView();
      if (!view.elements.some(element => element.id === linkFrom) || !view.elements.some(element => element.id === linkTo))
        throw new Error('关联端点不存在');
      const id = `manual-rel-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
      const relation: WhereKnowledgeRelation = { id, from: linkFrom, to: linkTo, relation: linkType,
        statement: linkStatement.trim() };
      const link: WhereKnowledgeView['links'][number] = { id, name: relation.relation, type: 'relation',
        color: '#8c8c8c', points: [], fromElementId: linkFrom, toElementId: linkTo,
        pendant: { data: { type: 'relation', detail: { knowledgeRelation: relation } }, onDataChange: () => {} } };
      replaceView({ ...view, links: [...view.links, link] }, selectedId);
      setResult(`已添加知识关联 ${linkFrom} → ${linkTo}。`);
    } catch (error) { setResult(`关联失败：${error instanceof Error ? error.message : String(error)}`); }
  };

  const exportKnowledge = () => {
    try {
      const view = currentView();
      const knowledge = extractWikiSqlWhereKnowledge(view, table.id);
      setSnapshot(JSON.stringify({ table, view, knowledge }, null, 2));
      setResult('已导出当前画布和结构化知识；可复制保存后重新载入。');
    } catch (error) { setResult(`导出失败：${error instanceof Error ? error.message : String(error)}`); }
  };

  const importKnowledge = () => {
    try {
      const saved = JSON.parse(snapshot) as { table: WhereKnowledgeTable; view: WhereKnowledgeView };
      createWikiSqlWhereKnowledgeView(saved.table); // Validate table shape without replacing authored nodes.
      extractWikiSqlWhereKnowledge(saved.view, saved.table.id);
      const view = { ...saved.view,
        elements: saved.view.elements.map(element => ({ ...element, pendant: { ...element.pendant, onDataChange: () => {} } })),
        links: saved.view.links.map(link => ({ ...link, pendant: { ...link.pendant, onDataChange: () => {} } })),
      };
      replaceView(view);
      setTable(saved.table); setTableText(JSON.stringify(saved.table, null, 2));
      setResult('已载入保存的表格和知识画布。');
    } catch (error) { setResult(`导入失败：${error instanceof Error ? error.message : String(error)}`); }
  };

  const run = async (mode: 'prepare' | 'parse') => {
    setBusy(true);
    try {
      const knowledge = extractWikiSqlWhereKnowledge(currentView(), table.id);
      const response = await fetch(`${endpoint}/${mode}`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question, table, knowledge }),
      });
      const body = await response.json();
      setResult(JSON.stringify({ httpStatus: response.status, ...body }, null, 2));
    } catch (error) {
      setResult(`链路失败：${error instanceof Error ? error.message : String(error)}`);
    } finally {
      setBusy(false);
    }
  };

  return <section style={{ width: '100%', padding: 12, background: '#fff', display: 'grid', gap: 8 }}>
    <div>WHERE 先验知识链路 · 自造小表验证（不会运行 WikiSQL 数据集）</div>
    <label>表格 JSON（修改后点击“载入表格”）</label>
    <textarea aria-label="表格 JSON" value={tableText} onChange={event => setTableText(event.target.value)} rows={4} style={{ width: '100%' }} />
    <label>自然语言问题</label>
    <input aria-label="自然语言问题" value={question} onChange={event => setQuestion(event.target.value)} style={{ width: '100%' }} />
    <details>
      <summary>人工编辑画布知识（修改内容会进入下一次 LLM Prompt）</summary>
      <div style={{ display: 'grid', gap: 6, padding: 8 }}>
        <label>选中知识节点</label>
        <select aria-label="选中知识节点" value={selectedId} onChange={event => {
          const id = event.target.value; setSelectedId(id);
          const node = currentView().elements.find(element => element.id === id);
          setStatement((node?.pendant.data.detail.knowledge as WhereKnowledge | undefined)?.statement ?? '');
        }}>{nodeIds.map(id => <option key={id} value={id}>{id}</option>)}</select>
        <textarea aria-label="知识内容" value={statement} onChange={event => setStatement(event.target.value)} rows={2} />
        <div style={{ display: 'flex', gap: 8 }}>
          <button onClick={editSelected} disabled={!selectedId || busy}>保存知识内容</button>
          <button onClick={removeSelected} disabled={!selectedId || busy}>移除选中节点</button>
        </div>
        <label>新增知识</label>
        <div style={{ display: 'flex', gap: 8 }}>
          <select aria-label="新知识作用域" value={newScope} onChange={event => setNewScope(event.target.value as 'GLOBAL' | 'TABLE')}>
            <option value="GLOBAL">GLOBAL 人工规则</option><option value="TABLE">TABLE 当前表规则</option>
          </select>
          <input aria-label="新知识标题" placeholder="标题" value={newTitle} onChange={event => setNewTitle(event.target.value)} />
        </div>
        <textarea aria-label="新知识内容" placeholder="明确、可复用的 WHERE 知识" value={newStatement}
          onChange={event => setNewStatement(event.target.value)} rows={2} />
        <button onClick={addNode} disabled={busy}>添加知识节点到画布</button>
        <label>添加语义知识关联（不是执行顺序）</label>
        <div style={{ display: 'flex', gap: 8 }}>
          <select aria-label="关联起点" value={linkFrom} onChange={event => setLinkFrom(event.target.value)}>
            {nodeIds.map(id => <option key={id} value={id}>{id}</option>)}
          </select>
          <span>→</span>
          <select aria-label="关联终点" value={linkTo} onChange={event => setLinkTo(event.target.value)}>
            {nodeIds.map(id => <option key={id} value={id}>{id}</option>)}
          </select>
          <select aria-label="关联类型" value={linkType} onChange={event => setLinkType(event.target.value as WhereKnowledgeRelation['relation'])}>
            {['SUPPORTS', 'COMBINES_WITH', 'CONSTRAINS', 'DISAMBIGUATES', 'ILLUSTRATES'].map(type => <option key={type} value={type}>{type}</option>)}
          </select>
          <input aria-label="关联语义说明" placeholder="明确说明二者的语义关联" value={linkStatement} onChange={event => setLinkStatement(event.target.value)} />
          <button onClick={addRelation} disabled={busy}>连接</button>
        </div>
        <label>移除已有语义关联</label>
        <select aria-label="移除语义关联" defaultValue="" onChange={event => {
          if (!event.target.value) return;
          const view = currentView();
          replaceView({ ...view, links: view.links.filter(link => link.id !== event.target.value) }, selectedId);
          setResult('已移除选中的语义关联。'); event.target.value = '';
        }}>
          <option value="">选择要移除的关联</option>
          {nodeIds.length > 0 && diagramRef.current?.getView().links.map((link: WhereKnowledgeView['links'][number]) =>
            <option key={link.id} value={link.id}>{link.fromElementId} → {link.toElementId}: {link.name}</option>)}
        </select>
      </div>
    </details>
    <details>
      <summary>保存/载入编排知识</summary>
      <div style={{ display: 'flex', gap: 8, padding: 8 }}>
        <button onClick={exportKnowledge} disabled={busy}>提取画布与结构化知识</button>
        <button onClick={importKnowledge} disabled={busy || !snapshot.trim()}>载入保存的知识</button>
      </div>
      <textarea aria-label="知识快照 JSON" value={snapshot} onChange={event => setSnapshot(event.target.value)} rows={5}
        placeholder="提取后复制保存；粘贴已有快照可重新载入。" style={{ width: '100%' }} />
    </details>
    <div style={{ display: 'flex', gap: 8 }}>
      <button onClick={loadTable} disabled={busy}>载入表格</button>
      <button onClick={() => run('prepare')} disabled={busy}>验证知识注入（不调用 LLM）</button>
      <button onClick={() => run('parse')} disabled={busy}>调用 LLM 解析 WHERE</button>
    </div>
    <pre aria-label="链路结果" style={{ maxHeight: 180, overflow: 'auto', margin: 0, whiteSpace: 'pre-wrap' }}>{result}</pre>
  </section>;
}
