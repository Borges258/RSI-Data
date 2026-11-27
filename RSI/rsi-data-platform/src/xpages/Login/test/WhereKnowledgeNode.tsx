'use client';

import React from 'react';
import type { VisualElementProps } from '@horsecoder/visual-core';
import type { WhereKnowledge } from './wikisql_where_knowledge';

/** Small canvas renderer kept outside the generated base components. */
export default function WhereKnowledgeNode({ element }: VisualElementProps) {
  const knowledge = element.state.pendant?.data.detail?.knowledge as WhereKnowledge | undefined;
  return <div style={{
    width: 200, minHeight: 80, boxSizing: 'border-box', border: `2px solid ${element.state.color}`,
    borderRadius: 6, background: '#fff', padding: 8, fontSize: 12, lineHeight: 1.3,
    boxShadow: '0 2px 6px #0002', cursor: 'move',
  }} title={knowledge?.statement}>
    <div style={{ fontWeight: 700 }}>{element.state.name}</div>
    <div style={{ color: '#666' }}>{knowledge?.scope} · {knowledge?.kind}</div>
    <div style={{ marginTop: 4, maxHeight: 35, overflow: 'hidden' }}>{knowledge?.statement}</div>
  </div>;
}
