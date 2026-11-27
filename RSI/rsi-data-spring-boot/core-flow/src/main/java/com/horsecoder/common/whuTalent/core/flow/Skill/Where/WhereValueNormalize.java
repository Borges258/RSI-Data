package com.horsecoder.common.whuTalent.core.flow.Skill.Where;

import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;

/** Normalize numeric values without altering text literals stored in the table. */
@FlowComponent
public class WhereValueNormalize {
    @Flow("WHERE_VALUE_NORMALIZE")
    public void run(String request) { WhereSkillSupport.emit(apply(WhereSkillSupport.read(request))); }

    public static ObjectNode apply(ObjectNode request) {
        ArrayNode conditions = WhereSkillSupport.copyConditions(request);
        JsonNode types = request.path("table").path("types");
        for (JsonNode item : conditions) {
            ObjectNode condition = (ObjectNode) item;
            int col = condition.path("columnIndex").asInt(-1);
            String value = WhereSkillSupport.text(condition, "value").trim();
            boolean numeric = col >= 0 && types.isArray() && col < types.size()
                    && "real".equalsIgnoreCase(types.get(col).asText());
            boolean storedWithComma = false;
            if (value.contains(",") && col >= 0) {
                JsonNode rows = request.path("table").path("rows");
                if (rows.isArray()) for (JsonNode row : rows)
                    if (row.isArray() && col < row.size() && row.get(col).asText().equals(value)) {
                        storedWithComma = true; break;
                    }
            }
            if (numeric && !storedWithComma && value.matches("-?\\d[\\d,]*(?:\\.\\d+)?")) {
                try {
                    BigDecimal number = new BigDecimal(value.replace(",", ""));
                    if (number.scale() == 0 && number.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) >= 0
                            && number.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0)
                        condition.put("value", number.intValueExact());
                    else if (number.scale() == 0 && number.precision() < 19)
                        condition.put("value", number.longValueExact());
                    else condition.put("value", number);
                }
                catch (NumberFormatException ignored) { condition.put("value", value); }
            } else condition.put("value", value);
        }
        ObjectNode result = WhereSkillSupport.output();
        result.set("conditions", conditions);
        return result;
    }
}
