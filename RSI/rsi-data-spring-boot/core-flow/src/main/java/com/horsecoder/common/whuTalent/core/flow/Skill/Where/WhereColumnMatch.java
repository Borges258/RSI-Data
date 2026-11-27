package com.horsecoder.common.whuTalent.core.flow.Skill.Where;

import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Resolve a condition's natural-language value and column clues to a table column. */
@FlowComponent
public class WhereColumnMatch {
    @Flow("WHERE_COLUMN_MATCH")
    public void run(String request) { WhereSkillSupport.emit(WhereLlmSkillClient.apply("WHERE_COLUMN_MATCH", WhereSkillSupport.read(request))); }

    public static ObjectNode apply(ObjectNode request) {
        ArrayNode conditions = WhereSkillSupport.copyConditions(request);
        boolean unresolved = false;
        for (JsonNode item : conditions) {
            ObjectNode condition = (ObjectNode) item;
            int column = WhereSkillSupport.bestColumn(request.path("table"),
                    WhereSkillSupport.text(request, "question"), condition);
            if (column < 0) unresolved = true;
            else condition.put("columnIndex", column);
        }
        ObjectNode result = WhereSkillSupport.output();
        if (unresolved) result.put("status", "CLARIFY");
        result.set("conditions", conditions);
        return result;
    }
}
