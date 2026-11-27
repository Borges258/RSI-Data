import request from '@/utils/axios';
import type { StandardPromise } from '@/utils/axios';

/**
 * 入参明细:
 * structure 结构
 */
export interface FlowRunParams {
  structure: string;
}

/**
 * 出参明细:
 * result 结果
 */
export interface FlowRunResults {
  result: string;
}

/**
 * 运行
 * @com.horsecoder.common.whuTalent.dataTesting.core/FLOW_RUN
 */
export const flowRun = async (params: FlowRunParams): StandardPromise<FlowRunResults> => {
  return request('/dataTesting-core/api/engine/flow', {
    method: 'POST',
    data: {
      flowId: '@com.horsecoder.common.whuTalent.dataTesting.core/FLOW_RUN',
      params: { ...params },
    },
  });
};


export const flowRunFacade = async (params: FlowRunParams): Promise<FlowRunResults> => {
  const { data: { value } } =  await flowRun(params);
  return value;
};

/** Run the registered WikiSQL WHERE composition through the existing Flow API. */
export const flowRunWhere = async (requestJson: string): StandardPromise<FlowRunResults> => {
  const endpoint = process.env.NEXT_PUBLIC_WHERE_FLOW_API_URL
    ?? (process.env.NODE_ENV === 'development'
      ? 'http://127.0.0.1:30845/api/engine/flow'
      : '/dataTesting-core/api/engine/flow');
  return request(endpoint, {
    method: 'POST',
    data: {
      flowId: '@com.horsecoder.common.whuTalent.dataTesting.core/WIKISQL_WHERE_GENERATION',
      params: { request: requestJson },
    },
  });
};

export const flowRunWhereFacade = async (requestJson: string): Promise<FlowRunResults> => {
  const { data: { value } } = await flowRunWhere(requestJson);
  return value;
};
