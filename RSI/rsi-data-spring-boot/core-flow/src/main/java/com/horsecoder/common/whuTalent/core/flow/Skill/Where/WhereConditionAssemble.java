package com.horsecoder.common.whuTalent.core.flow.Skill.Where;

import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Build WikiSQL conds as [columnIndex, operatorIndex, value] triples. */
@FlowComponent
public class WhereConditionAssemble {
    @Flow("WHERE_CONDITION_ASSEMBLE")
    public void run(String request) { WhereSkillSupport.emit(apply(WhereSkillSupport.read(request))); }

    public static ObjectNode apply(ObjectNode request) {
        ObjectNode validation = WhereConditionValidate.apply(request);
        ObjectNode result = WhereSkillSupport.output();
        ArrayNode conds = WhereSkillSupport.JSON.createArrayNode();
        if (!validation.path("valid").asBoolean()) {
            result.put("status", validation.path("status").asText("INVALID"));
            result.set("errors", validation.path("errors"));
            result.set("conds", conds);
            return result;
        }
        ArrayNode conditions = WhereSkillSupport.copyConditions(request);
        ArrayNode unrepresentable = WhereSkillSupport.JSON.createArrayNode();
        for (int i = 0; i < conditions.size(); i++) {
            ArrayNode triple = WhereWikiSqlOperatorAdapter.map(request.path("table"), conditions.get(i));
            if (triple == null) {
                ObjectNode item = unrepresentable.addObject();
                item.put("conditionIndex", i);
                item.put("operator", conditions.get(i).path("operator").asText());
                item.set("value", conditions.get(i).path("value").deepCopy());
                item.put("reason", "no equivalent standard WikiSQL predicate on the supplied table");
            } else {
                conds.add(triple);
            }
        }
        if (unrepresentable.size() > 0) {
            result.put("status", "UNREPRESENTABLE_OPERATOR");
            result.putArray("errors").add("one or more conditions cannot be encoded in standard WikiSQL");
            result.set("unrepresentableConditions", unrepresentable);
            result.set("partialConds", conds);
            result.set("conds", WhereSkillSupport.JSON.createArrayNode());
            return result;
        }
        result.set("conds", conds);
        return result;
    }
}
