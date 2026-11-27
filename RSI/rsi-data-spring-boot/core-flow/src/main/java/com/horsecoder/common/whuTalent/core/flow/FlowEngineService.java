package com.horsecoder.common.whuTalent.core.flow;

import cc.eamon.open.flow.config.constants.FlowConstants;
import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.support.route.ExecutorHolder;
import cc.eamon.open.flow.remote.FlowRequest;
import java.lang.Exception;
import java.lang.Object;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.horsecoder.common.whuTalent.core.flow.Skill.Where.WhereRequestMerge;

/**
 * Author: horsecoder
 * Email: maintainer@example.com
 */
@Service
public class FlowEngineService {
  public Map<String, Object> runFlowByFlowId(FlowRequest request) throws Exception {
    // 定义引擎;
    FlowEngine flowEngine = FlowEngine.instance();
    try {
      // 注入参数;
      if (request.getParams() != null)
          request.getParams().forEach((key, value) -> flowEngine.register(FlowConstants.SCOPE_DEFAULT, key, value));
      // 运行流程;
      flowEngine.run(request.getFlowId());
      // 取出所需结果;
      return flowEngine.retrieveFlowResults(request.getFlowId());
    } catch (Exception e) {
      if (request != null && WhereRequestMerge.FLOW_ID.equals(request.getFlowId())) {
        Object workingRequest = flowEngine.retrieve(WhereRequestMerge.FLOW_ID, "request");
        if (workingRequest == null)
          workingRequest = flowEngine.retrieve(FlowConstants.SCOPE_DEFAULT, "request");
        Map<String, Object> result = new HashMap<>();
        result.put("result", WhereRequestMerge.flowFailure(e, workingRequest));
        return result;
      }
      throw e;
    } finally {
      // A failed child may leave its routing stack in the thread-local executor.
      ExecutorHolder.clear();
      flowEngine.release();
    }
  }
}
