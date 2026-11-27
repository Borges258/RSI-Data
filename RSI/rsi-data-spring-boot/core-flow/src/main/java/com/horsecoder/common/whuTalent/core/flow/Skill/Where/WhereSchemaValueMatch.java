package com.horsecoder.common.whuTalent.core.flow.Skill.Where;

import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Ground text values in the active table, retaining its exact spelling and quotes. */
@FlowComponent
public class WhereSchemaValueMatch {
    @Flow("WHERE_SCHEMA_VALUE_MATCH")
    public void run(String request) {
        ObjectNode context = WhereSkillSupport.read(request);
        ObjectNode result = apply(context);
        ArrayNode conditions = (ArrayNode) result.path("conditions");
        ArrayNode reviews = WhereSkillSupport.JSON.createArrayNode();
        for (JsonNode item : conditions) {
            ObjectNode condition = (ObjectNode) item;
            if (!"NO_MATCH".equals(condition.path("groundingStatus").asText())) continue;
            JsonNode candidates = condition.path("columnCandidates");
            if (!candidates.isArray() || candidates.size() == 0) continue;
            try {
                ObjectNode review = WhereLlmSkillClient.reviewGrounding(context, condition, candidates);
                reviews.add(review.path("modelCall").deepCopy());
                JsonNode chosen = review.path("decision").path("column");
                condition.set("groundingReview", review.path("decision").deepCopy());
                if (!chosen.isIntegralNumber() || explicitFieldConflict(condition, context.path("table"), chosen.asInt()))
                    continue;
                for (JsonNode candidate : candidates) {
                    if (candidate.path("columnIndex").asInt(-1) != chosen.asInt()) continue;
                    Set<String> exact = new LinkedHashSet<>(), normalized = new LinkedHashSet<>();
                    collectMatches(context.path("table"), chosen.asInt(),
                            WhereSkillSupport.text(condition, "value"), exact, normalized);
                    Set<String> matched = !exact.isEmpty() ? exact : normalized;
                    if (matched.size() != 1) break;
                    condition.put("columnIndex", chosen.asInt());
                    condition.put("grounded", true);
                    condition.put("groundingStatus", exact.isEmpty() ? "NORMALIZED_MATCH" : "EXACT_MATCH");
                    condition.put("columnRecovery", "REVIEWED_FIELD_AND_TABLE_VALUE");
                    condition.put("value", matched.iterator().next());
                    break;
                }
            } catch (Exception error) {
                result.withArray("warnings").add("grounding review unavailable: "
                        + error.getClass().getSimpleName());
            }
        }
        if (reviews.size() > 0) {
            result.set("reviewModelCalls", reviews);
            result.set("modelCall", reviews.get(0).deepCopy());
        }
        WhereSkillSupport.emit(result);
    }

    public static ObjectNode apply(ObjectNode request) {
        ArrayNode conditions = WhereSkillSupport.copyConditions(request);
        JsonNode table = request.path("table");
        ArrayNode warnings = WhereSkillSupport.JSON.createArrayNode();
        for (JsonNode item : conditions) {
            ObjectNode condition = (ObjectNode) item;
            int col = condition.path("columnIndex").asInt(-1);
            String value = WhereSkillSupport.text(condition, "value");
            if (col < 0 && !"UNRESOLVED".equals(condition.path("columnMatchStatus").asText())) col = WhereSkillSupport.bestColumn(table,
                    WhereSkillSupport.text(request, "question"), condition);
            if (col < 0) {
                condition.put("grounded", false);
                condition.put("groundingStatus", "NO_MATCH");
                condition.set("columnCandidates", valueColumns(table, -1, value));
                continue;
            }
            condition.put("columnIndex", col);
            Set<String> exact = new LinkedHashSet<>(), normalized = new LinkedHashSet<>();
            collectMatches(table, col, value, exact, normalized);
            if (exact.isEmpty() && normalized.isEmpty()) {
                ArrayNode alternatives = valueColumns(table, col, value);
                int recovered = supportedColumn(condition, table.path("header"), alternatives);
                if (recovered >= 0) {
                    col = recovered;
                    condition.put("columnIndex", col);
                    collectMatches(table, col, value, exact, normalized);
                    condition.put("columnRecovery", "VALUE_AND_EXPLICIT_HEADER");
                } else {
                    if (alternatives.size() == 0 && dateThreshold(table, col, condition, value)) {
                        alternatives = dateColumns(table);
                        condition.put("groundingReason", "DATE_THRESHOLD_UNVERIFIED");
                    } else if (alternatives.size() > 0) {
                        condition.put("groundingReason", "FIELD_VALUE_CONFLICT");
                    }
                    if (alternatives.size() > 0) condition.set("columnCandidates", alternatives);
                }
            }
            Set<String> candidates = !exact.isEmpty() ? exact : normalized;
            if (candidates.size() == 1) {
                condition.put("grounded", true);
                condition.put("groundingStatus", exact.isEmpty() ? "NORMALIZED_MATCH" : "EXACT_MATCH");
                condition.put("value", candidates.iterator().next());
            } else if (candidates.size() > 1) {
                condition.put("grounded", false);
                condition.put("groundingStatus", "AMBIGUOUS");
                ArrayNode options = condition.putArray("groundingCandidates");
                candidates.forEach(options::add);
            } else if (isNumericThreshold(table, col, condition, value)) {
                condition.put("grounded", true);
                condition.put("groundingStatus", "THRESHOLD_LITERAL");
                warnings.add("condition threshold is not a stored table value: " + value);
            } else {
                condition.put("grounded", false);
                condition.put("groundingStatus", "NO_MATCH");
            }
        }
        ObjectNode result = WhereSkillSupport.output();
        if (warnings.size() > 0) result.set("warnings", warnings);
        result.set("conditions", conditions);
        return result;
    }

    private static void collectMatches(JsonNode table, int col, String value,
                                       Set<String> exact, Set<String> normalized) {
        String key = WhereSkillSupport.valueKey(value);
        JsonNode rows = table.path("rows");
        if (rows.isArray()) for (JsonNode row : rows) if (row.isArray() && col < row.size()) {
            JsonNode cell = row.get(col);
            if (cell == null || cell.isNull()) continue;
            String literal = cell.asText();
            if (literal.equals(value)) exact.add(literal);
            else if (WhereSkillSupport.valueKey(literal).equals(key)
                    || sameDate(literal, value)) normalized.add(literal);
        }
        JsonNode alias = table.path("valueAliases").path(value);
        if (!alias.isMissingNode() && rows.isArray()) for (JsonNode row : rows)
            if (row.isArray() && col < row.size() && row.get(col) != null && !row.get(col).isNull()
                    && WhereSkillSupport.valueKey(row.get(col).asText())
                    .equals(WhereSkillSupport.valueKey(alias.asText()))) normalized.add(row.get(col).asText());
    }

    private static ArrayNode valueColumns(JsonNode table, int selected, String value) {
        ArrayNode out = WhereSkillSupport.JSON.createArrayNode();
        JsonNode headers = table.path("header");
        if (!headers.isArray()) return out;
        for (int col = 0; col < headers.size(); col++) {
            if (col == selected) continue;
            Set<String> exact = new LinkedHashSet<>(), normalized = new LinkedHashSet<>();
            collectMatches(table, col, value, exact, normalized);
            if (exact.isEmpty() && normalized.isEmpty()) continue;
            ObjectNode candidate = out.addObject();
            candidate.put("columnIndex", col);
            candidate.put("header", headers.get(col).asText());
            candidate.put("evidence", exact.isEmpty() ? "NORMALIZED_VALUE_MATCH" : "EXACT_VALUE_MATCH");
        }
        return out;
    }

    private static int supportedColumn(JsonNode condition, JsonNode headers, ArrayNode alternatives) {
        String clause = condition.path("clause").asText("");
        if (clause.isEmpty() || !headers.isArray()) return -1;
        int mentioned = -1;
        for (int col = 0; col < headers.size(); col++) if (WhereSkillSupport.present(clause, headers.get(col).asText())) {
            if (mentioned >= 0) return -1;
            mentioned = col;
        }
        if (mentioned < 0) return -1;
        for (JsonNode candidate : alternatives) if (candidate.path("columnIndex").asInt(-1) == mentioned)
            return mentioned;
        return -1;
    }

    private static boolean explicitFieldConflict(JsonNode condition, JsonNode table, int chosen) {
        String clause = condition.path("clause").asText("");
        JsonNode headers = table.path("header");
        if (!headers.isArray() || chosen < 0 || chosen >= headers.size()) return true;
        for (int col = 0; col < headers.size(); col++)
            if (col != chosen && WhereSkillSupport.present(clause, headers.get(col).asText())) return true;
        return false;
    }

    private static boolean sameDate(String left, String right) {
        LocalDate a = parseDate(left), b = parseDate(right);
        return a != null && a.equals(b);
    }

    private static LocalDate parseDate(String value) {
        String text = value.trim().replaceAll("(?<=\\d),(?=\\s*\\d{4}$)", "");
        for (String pattern : new String[]{"MMMM d yyyy", "d MMMM yyyy", "MMM d yyyy", "d MMM yyyy", "yyyy-MM-dd"}) {
            try { return LocalDate.parse(text, DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)); }
            catch (DateTimeParseException ignored) { /* try a different unambiguous date layout */ }
        }
        return null;
    }

    private static boolean dateThreshold(JsonNode table, int col, JsonNode condition, String value) {
        String op = condition.path("operator").asText();
        JsonNode types = table.path("types");
        return types.isArray() && col < types.size()
                && "text".equalsIgnoreCase(types.get(col).asText())
                && (">".equals(op) || "<".equals(op) || ">=".equals(op) || "<=".equals(op))
                && value.matches("(?i)^(?:January|February|March|April|May|June|July|August|September|October|November|December)\\s+\\d{1,2},?\\s+\\d{4}$");
    }

    private static ArrayNode dateColumns(JsonNode table) {
        ArrayNode out = WhereSkillSupport.JSON.createArrayNode();
        JsonNode headers = table.path("header");
        if (!headers.isArray()) return out;
        for (int col = 0; col < headers.size(); col++) {
            String header = headers.get(col).asText().toLowerCase(java.util.Locale.ROOT);
            if (!(header.contains("date") || header.contains("laid down") || header.contains("launched")
                    || header.contains("commissioned"))) continue;
            ObjectNode candidate = out.addObject();
            candidate.put("columnIndex", col);
            candidate.put("header", headers.get(col).asText());
            candidate.put("evidence", "DATE_COLUMN_ONLY");
        }
        return out;
    }

    private static boolean isNumericThreshold(JsonNode table, int col, JsonNode condition, String value) {
        JsonNode types = table.path("types");
        String operator = condition.path("operator").asText();
        return types.isArray() && col < types.size()
                && "real".equalsIgnoreCase(types.get(col).asText())
                && (">".equals(operator) || "<".equals(operator)
                    || ">=".equals(operator) || "<=".equals(operator))
                && WhereSkillSupport.valueKey(value).matches("[+-]?\\d+(?:\\.\\d+)?");
    }
}
