package com.horsecoder.common.whuTalent.core.flow.Skill.Where;

import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Locale;
import java.util.regex.Pattern;

/** Checks question coverage without treating every table value mention as a predicate. */
@FlowComponent
public class WhereCompletenessCheck {
    private static final Pattern COMPARISON = Pattern.compile(
            "(?i)(?:>=|<=|!=|>|<|at least|at most|no (?:more|less) than|"
                    + "(?:more|less|greater|fewer|higher|lower|larger|smaller) than|"
                    + "(?:over|under|above|below|before|after))\\s*$");
    @Flow("WHERE_COMPLETENESS_CHECK")
    public void run(String request) {
        ObjectNode context = WhereSkillSupport.read(request);
        ObjectNode result = apply(context);
        if ("UNCERTAIN".equals(result.path("completeness").asText())
                || result.path("conditions").size() == 0)
            review(context, result);
        WhereSkillSupport.emit(result);
    }

    private static void review(ObjectNode request, ObjectNode result) {
        try {
            ObjectNode reviewed = WhereLlmSkillClient.review(request, result.path("uncertain"));
            // Keep provider usage even when the proposed condition fails local constraints.
            result.set("modelCall", reviewed.path("modelCall").deepCopy());
            ArrayNode conditions = (ArrayNode) result.path("conditions");
            ArrayNode recovered = WhereSkillSupport.JSON.createArrayNode();
            String question = WhereSkillSupport.text(request, "question");
            JsonNode proposed = reviewed.path("missing");
            if (proposed.size() > 4 || conditions.size() + proposed.size() > 4)
                throw new IllegalArgumentException("review exceeded WikiSQL condition limit");
            for (JsonNode item : proposed) {
                String surface = item.path("surface").asText("");
                JsonNode value = item.path("value");
                if (surface.trim().isEmpty() || !(value.isTextual() || value.isNumber())
                        || !WhereSkillSupport.valueKey(surface).equals(WhereSkillSupport.valueKey(value.asText()))
                        || isHeader(surface, request.path("table").path("header")))
                    throw new IllegalArgumentException("review proposed a nonliteral or header-only condition");
                int start = findUncovered(question, surface, conditions);
                if (start < 0) throw new IllegalArgumentException("review condition has no uncovered question span");
                ObjectNode condition = WhereSkillSupport.JSON.createObjectNode();
                condition.put("surface", question.substring(start, start + surface.length()));
                condition.set("value", value.deepCopy());
                condition.put("start", start);
                condition.put("end", start + surface.length());
                conditions.add(condition);
                recovered.add(condition.deepCopy());
            }
            if (recovered.size() > 0) {
                java.util.List<JsonNode> ordered = new java.util.ArrayList<>();
                conditions.forEach(ordered::add);
                ordered.sort(java.util.Comparator.comparingInt(n -> n.path("start").asInt()));
                ArrayNode sorted = WhereSkillSupport.JSON.createArrayNode();
                ordered.forEach(sorted::add);
                ObjectNode withConditions = request.deepCopy();
                withConditions.set("conditions", sorted);
                result.set("conditions", WhereMultiConditionExtract.apply(withConditions).path("conditions"));
            }
            result.put("count", result.path("conditions").size());
            result.set("recovered", recovered);
            result.set("reviewedUncertain", result.path("uncertain").deepCopy());
            result.set("uncertain", WhereSkillSupport.JSON.createArrayNode());
            result.put("completeness", "COMPLETE");
            result.put("complete", true);
            result.remove("warnings");
        } catch (Exception error) {
            result.put("completeness", "UNCERTAIN");
            result.put("complete", false);
            result.putArray("warnings").add("semantic review unavailable or rejected: "
                    + error.getClass().getSimpleName());
        }
    }

    private static boolean isHeader(String surface, JsonNode headers) {
        if (headers.isArray()) for (JsonNode header : headers)
            if (WhereSkillSupport.valueKey(surface).equals(WhereSkillSupport.valueKey(header.asText()))) return true;
        return false;
    }

    private static int findUncovered(String question, String surface, ArrayNode conditions) {
        String lower = question.toLowerCase(Locale.ROOT), needle = surface.toLowerCase(Locale.ROOT);
        int at = lower.indexOf(needle);
        while (at >= 0) {
            int end = at + surface.length();
            boolean boundary = (at == 0 || !Character.isLetterOrDigit(question.charAt(at - 1)))
                    && (end == question.length() || !Character.isLetterOrDigit(question.charAt(end)));
            boolean overlaps = false;
            for (JsonNode item : conditions)
                if (at < item.path("end").asInt() && end > item.path("start").asInt()) { overlaps = true; break; }
            if (boundary && !overlaps) return at;
            at = lower.indexOf(needle, at + 1);
        }
        return -1;
    }

    public static ObjectNode apply(ObjectNode request) {
        String question = WhereSkillSupport.text(request, "question");
        ArrayNode supplied = WhereSkillSupport.copyConditions(request);
        ArrayNode missing = WhereSkillSupport.JSON.createArrayNode();
        ArrayNode uncertain = WhereSkillSupport.JSON.createArrayNode();
        for (WhereSkillSupport.Mention mention : WhereSkillSupport.mentions(question, request.path("table"))) {
            boolean covered = false;
            for (JsonNode item : supplied) {
                int left = item.path("start").asInt(-1), right = item.path("end").asInt(-1);
                if (left <= mention.start && right >= mention.end && left >= 0) { covered = true; break; }
                if (WhereSkillSupport.valueKey(mention.value).equals(
                        WhereSkillSupport.valueKey(WhereSkillSupport.text(item, "value")))) { covered = true; break; }
            }
            if (covered) continue;
            ObjectNode candidate = WhereSkillSupport.JSON.createObjectNode();
            candidate.put("surface", question.substring(mention.start, mention.end));
            candidate.put("start", mention.start);
            candidate.put("end", mention.end);
            String prefix = question.substring(Math.max(0, mention.start - 35), mention.start);
            // Only an independent comparison boundary is clear evidence of an omitted predicate.
            if (mention.value.matches("[+-]?\\d[\\d,]*(?:\\.\\d+)?")
                    && COMPARISON.matcher(prefix).find()) missing.add(candidate);
            else uncertain.add(candidate);
        }
        String state = missing.size() > 0 ? "INCOMPLETE"
                : uncertain.size() > 0 ? "UNCERTAIN" : "COMPLETE";
        ObjectNode result = WhereSkillSupport.output();
        result.put("completeness", state);
        result.put("complete", "COMPLETE".equals(state));
        if (missing.size() > 0) {
            result.put("status", "INCOMPLETE");
            result.putArray("errors").add("explicit comparison value is absent from conditions");
        }
        if (uncertain.size() > 0) result.putArray("warnings")
                .add("possible condition mentions require semantic review; continuing");
        result.set("missing", missing);
        result.set("uncertain", uncertain);
        result.set("extra", WhereSkillSupport.JSON.createArrayNode());
        result.set("conditions", supplied);
        return result;
    }
}
