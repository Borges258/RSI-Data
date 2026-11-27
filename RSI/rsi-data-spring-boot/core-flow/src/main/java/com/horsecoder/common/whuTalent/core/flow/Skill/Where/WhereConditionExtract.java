package com.horsecoder.common.whuTalent.core.flow.Skill.Where;

import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Extract all table-grounded value mentions and their count from the question. */
@FlowComponent
public class WhereConditionExtract {
    @Flow("WHERE_CONDITION_EXTRACT")
    public void run(String request) { WhereSkillSupport.emit(WhereLlmSkillClient.apply("WHERE_CONDITION_EXTRACT", WhereSkillSupport.read(request))); }

    public static ObjectNode apply(ObjectNode request) {
        ArrayNode conditions = WhereSkillSupport.extracted(
                WhereSkillSupport.text(request, "question"), request.path("table"));
        ObjectNode result = WhereSkillSupport.output();
        result.set("conditions", conditions);
        result.put("count", conditions.size());
        return result;
    }
}
