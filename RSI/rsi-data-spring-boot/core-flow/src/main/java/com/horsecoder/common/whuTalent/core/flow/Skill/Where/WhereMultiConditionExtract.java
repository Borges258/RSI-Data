package com.horsecoder.common.whuTalent.core.flow.Skill.Where;

import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Locale;

/** Separate AND clauses while keeping conjunctions inside a literal intact. */
@FlowComponent
public class WhereMultiConditionExtract {
    @Flow("WHERE_MULTI_CONDITION_EXTRACT")
    public void run(String request) { WhereSkillSupport.emit(apply(WhereSkillSupport.read(request))); }

    public static ObjectNode apply(ObjectNode request) {
        String question = WhereSkillSupport.text(request, "question");
        ArrayNode conditions = request.path("conditions").isArray()
                ? WhereSkillSupport.copyConditions(request)
                : WhereSkillSupport.extracted(question, request.path("table"));
        String lower = question.toLowerCase(Locale.ROOT);
        for (int i = 0; i < conditions.size(); i++) {
            ObjectNode condition = (ObjectNode) conditions.get(i);
            int start = condition.path("start").asInt();
            int end = condition.path("end").asInt();
            int left = lower.lastIndexOf(" and ", start);
            if (left < 0) left = lower.lastIndexOf(',', start);
            int right = lower.indexOf(" and ", end);
            if (right < 0) right = lower.indexOf(',', end);
            condition.put("clause", question.substring(Math.max(0, left + 1), right < 0 ? question.length() : right).trim());
        }
        ObjectNode result = WhereSkillSupport.output();
        if (conditions.size() > 4 || lower.matches("(?s).*\\bor\\b.*")) result.put("status", "CLARIFY");
        result.set("conditions", conditions);
        result.put("count", conditions.size());
        return result;
    }
}
