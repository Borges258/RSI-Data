package com.horsecoder.common.whuTalent.core.flow.Skill.Where;

import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Recover the full condition literal, including punctuation and stored quotes. */
@FlowComponent
public class WhereValueExtract {
    @Flow("WHERE_VALUE_EXTRACT")
    public void run(String request) { WhereSkillSupport.emit(WhereLlmSkillClient.apply("WHERE_VALUE_EXTRACT", WhereSkillSupport.read(request))); }

    public static ObjectNode apply(ObjectNode request) {
        ArrayNode conditions = WhereSkillSupport.copyConditions(request);
        for (JsonNode item : conditions) {
            ObjectNode condition = (ObjectNode) item;
            String value = WhereSkillSupport.text(condition, "value");
            if (value.isEmpty()) value = WhereSkillSupport.text(condition, "surface");
            int col = condition.path("columnIndex").asInt(-1);
            condition.put("value", WhereSkillSupport.canonical(request.path("table"), col, value));
        }
        ObjectNode result = WhereSkillSupport.output();
        result.set("conditions", conditions);
        return result;
    }
}
