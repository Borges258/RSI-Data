import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Button } from 'antd';
import styles from './index.module.css';
import type { LoginProps } from './types';

// WHERE-PRIOR BASE ADAPTER: rollback boundaries are documented in WHERE_PRIOR_BASE_CHANGELOG.md.

import VisualDiagram, { VisualToolbarConfig } from '@horsecoder/visual-core';


import {
  VisualCanvasConfig,
  VisualElementConfig,
  VisualLinkConfig,
} from '@horsecoder/visual-core';
import StandardModelEntityWrapper from '@/components/StandardModelEntity/wrapper';
import StandardModelLinkWrapper from '@/components/StandardModelLink/wrapper';
import { getView, refreshCanvasLocked, setView } from './handler/handler/diagramHandler';
import { flowRunWhereFacade } from '@/src/flows/com.horsecoder.common.whuTalent.dataTesting.core/Run';
import { createWikiSqlWhereKnowledgeView, extractWikiSqlWhereKnowledge, type WhereKnowledgeTable, type WhereKnowledgeView } from './test/wikisql_where_knowledge';
import { wherePriorDemoTable } from './test/WherePriorWorkbench';


/**
 * 组件布局: 登录
 */
const LoginPage: React.FC<LoginProps> = () => {
  const [table, setTable] = useState<WhereKnowledgeTable>(wherePriorDemoTable);
  const [tableText, setTableText] = useState(JSON.stringify(wherePriorDemoTable, null, 2));
  const [question, setQuestion] = useState('Which cities have population greater than 100 and country is Canada?');
  const [output, setOutput] = useState('');
  const [busy, setBusy] = useState(false);



  // 内部数据: diagramRefInner(diagramRef)
  const diagramRef = useRef<any>(null);
  // 内部数据: 工具栏配置(toolbarConfig)
  const toolbarConfig: VisualToolbarConfig = useMemo(() => [], []);

  useEffect(() => {
    if (diagramRef.current) {
      refreshCanvasLocked(diagramRef, false);
      setView(diagramRef, createWikiSqlWhereKnowledgeView(wherePriorDemoTable) as any);
    }
  }, []);

  const elementConfig: VisualElementConfig = useMemo(
    () => ({
      components: {
        model: {
          component: StandardModelEntityWrapper,
          minSize: {
            width: 100,
            height: 55,
          },
        },
      },
    }),
    [],
  );

  const linkConfig: VisualLinkConfig = useMemo(
    () => ({
      components: {
        relation: {
          component: StandardModelLinkWrapper,
          hasArrow: true,
          showName: true,
        },
      },
    }),
    [],
  );

    const canvasConfig: VisualCanvasConfig = useMemo(
    () => ({
      toolbarConfig,
      elementConfig,
      linkConfig,
      globalConfig: {
        gridSize: 25,
      },
    }),
    [toolbarConfig, elementConfig, linkConfig],
  );


  return (
    <>
      <div style={{ display: 'grid', gap: 8, padding: 12 }}>
        <label>WikiSQL 问题</label>
        <input aria-label="WikiSQL 问题" value={question} onChange={event => setQuestion(event.target.value)} />
        <label>当前表格 JSON</label>
        <textarea aria-label="当前表格 JSON" rows={4} value={tableText} onChange={event => setTableText(event.target.value)} />
        <button onClick={() => {
          try {
            const next = JSON.parse(tableText) as WhereKnowledgeTable;
            const view = createWikiSqlWhereKnowledgeView(next);
            setTable(next);
            setView(diagramRef, view as any);
            setOutput(`已载入表 ${next.id}`);
          } catch (error) { setOutput(error instanceof Error ? error.message : String(error)); }
        }}>载入表格并更新表级知识</button>
      </div>
      <div className={styles.div}>
        <Button
          className={styles.divButton0}
          type="primary"
          loading={busy}
          onClick={async () => {
            setBusy(true);
            try {
              const view = getView(diagramRef);
              if (!view) throw new Error('知识画布尚未就绪');
              const knowledge = extractWikiSqlWhereKnowledge(view as unknown as WhereKnowledgeView, table.id);
              const response = await flowRunWhereFacade(JSON.stringify({
                question, table, structure: JSON.stringify(view), knowledge,
              }));
              setOutput(JSON.stringify(JSON.parse(response.result), null, 2));
            } catch (error) {
              setOutput(error instanceof Error ? error.message : String(error));
            } finally { setBusy(false); }
          }}
        >
          运行
        </Button>
      </div>
      {output && <pre style={{ whiteSpace: 'pre-wrap', padding: 12 }}>{output}</pre>}
      <VisualDiagram
        // className={styles.divDiv0}
        ref={diagramRef}
        // {...props.actions.eventsDiv}
        canvasConfig={canvasConfig}
      />
    </>
  );
};

export default LoginPage;
