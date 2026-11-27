package com.horsecoder.common.whuTalent.core.flow.Skill.Where;

import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Reject missing columns, unsupported operators and invalid typed values. */
@FlowComponent
public class WhereConditionValidate {
    @Flow("WHERE_CONDITION_VALIDATE")
    public void run(String request) { WhereSkillSupport.emit(apply(WhereSkillSupport.read(request))); }

    public static ObjectNode apply(ObjectNode request) {
        ArrayNode conditions = WhereSkillSupport.copyConditions(request);
        ArrayNode errors = WhereSkillSupport.JSON.createArrayNode();
        boolean groundingFailed = false;
        JsonNode headers = request.path("table").path("header");
        JsonNode types = request.path("table").path("types");
        if (!headers.isArray() || !types.isArray() || headers.size() != types.size()) errors.add("invalid table schema");
        if (conditions.size() > 4) errors.add("WikiSQL supports at most four AND conditions here");
        for (int i = 0; i < conditions.size(); i++) {
            JsonNode condition = conditions.get(i);
            int col = condition.path("columnIndex").asInt(-1);
            String op = WhereSkillSupport.text(condition, "operator");
            if (col < 0 || !headers.isArray() || col >= headers.size()) errors.add("condition " + i + ": invalid column");
            if (WhereSkillSupport.operatorCode(op) < 0) errors.add("condition " + i + ": unsupported operator " + op);
            JsonNode value = condition.path("value");
            if (value.isMissingNode() || value.isNull() || value.asText().trim().isEmpty()) errors.add("condition " + i + ": empty value");
            String grounding = condition.path("groundingStatus").asText("");
            if (!condition.path("grounded").asBoolean(false)
                    || !("EXACT_MATCH".equals(grounding) || "NORMALIZED_MATCH".equals(grounding)
                        || "THRESHOLD_LITERAL".equals(grounding))) {
                errors.add("condition " + i + ": grounding " + (grounding.isEmpty() ? "missing" : grounding));
                groundingFailed = true;
            }
            if (col >= 0 && types.isArray() && col < types.size()
                    && "real".equalsIgnoreCase(types.get(col).asText())
                    && !value.isNumber() && !value.asText().matches("-?\\d[\\d,]*(?:\\.\\d+)?%?"))
                errors.add("condition " + i + ": nonnumeric value for real column");
        }
        ObjectNode result = WhereSkillSupport.output();
        result.put("valid", errors.size() == 0);
        if (errors.size() != 0) result.put("status", groundingFailed ? "GROUNDING_FAILED" : "INVALID");
        result.set("errors", errors);
        result.set("conditions", conditions);
        return result;
    }
}
