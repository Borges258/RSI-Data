package com.horsecoder.common.whuTalent.core.flow.Skill.Where;

import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Locale;
import java.util.regex.Pattern;

/** Identify six comparison operators; unsupported WikiSQL operators remain explicit. */
@FlowComponent
public class WhereOperatorRecognize {
    private static final Pattern GE = Pattern.compile("(?:>=|at least|no less than|not less than|or greater|or more|or above)");
    private static final Pattern LE = Pattern.compile("(?:<=|at most|no more than|not more than|or less|or below|or fewer)");
    private static final Pattern NE = Pattern.compile("(?:!=|<>|not equal|other than|except for|excluding)");
    private static final Pattern GT = Pattern.compile("(?:>|greater than|larger than|more than|higher than|over |above |after )");
    private static final Pattern LT = Pattern.compile("(?:<|less than|lower than|smaller than|fewer than|under |below |before )");

    @Flow("WHERE_OPERATOR_RECOGNIZE")
    public void run(String request) { WhereSkillSupport.emit(WhereLlmSkillClient.apply("WHERE_OPERATOR_RECOGNIZE", WhereSkillSupport.read(request))); }

    public static ObjectNode apply(ObjectNode request) {
        String question = WhereSkillSupport.text(request, "question").toLowerCase(Locale.ROOT);
        ArrayNode conditions = WhereSkillSupport.copyConditions(request);
        boolean unsupported = false;
        for (JsonNode item : conditions) {
            ObjectNode condition = (ObjectNode) item;
            int start = condition.path("start").asInt(question.indexOf(
                    WhereSkillSupport.text(condition, "surface").toLowerCase(Locale.ROOT)));
            if (start < 0) start = question.length();
            String context = question.substring(Math.max(0, start - 35), Math.min(start, question.length()));
            // A preceding condition must not donate its operator to this value.
            int boundary = Math.max(context.lastIndexOf(" and "), context.lastIndexOf(","));
            if (boundary >= 0) context = context.substring(boundary + 1);
            String operator = "=";
            if (GE.matcher(context).find()) operator = ">=";
            else if (LE.matcher(context).find()) operator = "<=";
            else if (NE.matcher(context).find()) operator = "!=";
            else if (GT.matcher(context).find()) operator = ">";
            else if (LT.matcher(context).find()) operator = "<";
            condition.put("operator", operator);
            if (WhereSkillSupport.operatorCode(operator) < 0) unsupported = true;
        }
        ObjectNode result = WhereSkillSupport.output();
        if (unsupported) result.put("status", "UNSUPPORTED_OPERATOR");
        result.set("conditions", conditions);
        return result;
    }
}
