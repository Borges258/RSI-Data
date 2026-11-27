package com.horsecoder.common.whuTalent.core.flow.Skill.Where;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Lowers extended comparisons to one standard WikiSQL predicate on the supplied table. */
final class WhereWikiSqlOperatorAdapter {
    private WhereWikiSqlOperatorAdapter() { }

    static ArrayNode map(JsonNode table, JsonNode condition) {
        int column = condition.path("columnIndex").asInt(-1);
        String operator = condition.path("operator").asText("");
        JsonNode value = condition.path("value");
        int code = WhereSkillSupport.operatorCode(operator);
        if (column < 0 || value.isMissingNode() || value.isNull() || code < 0) return null;
        if (code <= 2) return triple(column, code, value);

        JsonNode types = table.path("types"), rows = table.path("rows");
        if (!types.isArray() || column >= types.size() || !rows.isArray()) return null;
        boolean numeric = "real".equalsIgnoreCase(types.get(column).asText());
        Double targetNumber = numeric ? number(value) : null;
        String targetText = numeric ? null : text(value);
        if (numeric && targetNumber == null) return null;

        List<JsonNode> observed = new ArrayList<>();
        boolean asciiText = !numeric && ascii(targetText);
        JsonNode minimumCell = null, maximumCell = null;
        JsonNode predecessor = null, successor = null, firstMatch = null;
        for (JsonNode row : rows) {
            if (!row.isArray() || column >= row.size()) return null;
            JsonNode cell = row.get(column);
            observed.add(cell);
            if (cell == null || cell.isNull()) continue;
            int relation;
            if (numeric) {
                Double n = number(cell);
                if (n == null) return null;
                relation = compareNumbers(n, targetNumber);
            } else {
                String cellText = text(cell);
                if (!ascii(cellText)) asciiText = false;
                relation = cellText.compareTo(targetText);
            }
            if (minimumCell == null || compare(cell, minimumCell, numeric) < 0) minimumCell = cell;
            if (maximumCell == null || compare(cell, maximumCell, numeric) > 0) maximumCell = cell;
            if (relation < 0 && (predecessor == null || compare(cell, predecessor, numeric) > 0)) predecessor = cell;
            if (relation > 0 && (successor == null || compare(cell, successor, numeric) < 0)) successor = cell;
            if (firstMatch == null && matches(operator, relation)) firstMatch = cell;
        }

        // Prefer the original boundary; it already works when no row equals it.
        List<JsonNode> candidates = new ArrayList<>();
        candidates.add(value);
        add(candidates, predecessor);
        add(candidates, successor);
        add(candidates, minimumCell);
        add(candidates, maximumCell);
        add(candidates, firstMatch);

        int[] order = code == 3 ? new int[]{1, 0, 2}
                : code == 4 ? new int[]{2, 0, 1} : new int[]{1, 2, 0};
        for (int standard : order) {
            if (!numeric && !asciiText && standard != 0) continue;
            for (JsonNode candidate : candidates) {
                if (equivalent(observed, numeric, operator, targetNumber, targetText, standard, candidate))
                    return triple(column, standard, candidate);
            }
        }
        return null;
    }

    private static boolean equivalent(List<JsonNode> observed, boolean numeric, String operator,
                                      Double targetNumber, String targetText, int standard, JsonNode candidate) {
        Double candidateNumber = numeric ? number(candidate) : null;
        String candidateText = numeric ? null : text(candidate);
        if (numeric && candidateNumber == null) return false;
        boolean compared = false;
        for (JsonNode cell : observed) {
            if (cell == null || cell.isNull()) continue; // SQL comparisons with NULL are false.
            compared = true;
            int expected = numeric ? compareNumbers(number(cell), targetNumber) : text(cell).compareTo(targetText);
            int actual = numeric ? compareNumbers(number(cell), candidateNumber) : text(cell).compareTo(candidateText);
            boolean wanted = matches(operator, expected);
            boolean matched = standard == 0 ? actual == 0 : standard == 1 ? actual > 0 : actual < 0;
            if (wanted != matched) return false;
        }
        return compared;
    }

    private static ArrayNode triple(int column, int code, JsonNode value) {
        ArrayNode result = WhereSkillSupport.JSON.createArrayNode();
        result.add(column);
        result.add(code);
        result.add(value.deepCopy());
        return result;
    }

    private static int compareNumbers(double left, double right) {
        return left == right ? 0 : left < right ? -1 : 1;
    }

    private static int compare(JsonNode left, JsonNode right, boolean numeric) {
        return numeric ? compareNumbers(number(left), number(right)) : text(left).compareTo(text(right));
    }

    private static boolean matches(String operator, int comparison) {
        return ">=".equals(operator) ? comparison >= 0
                : "<=".equals(operator) ? comparison <= 0 : comparison != 0;
    }

    private static void add(List<JsonNode> candidates, JsonNode value) {
        if (value != null && !candidates.contains(value)) candidates.add(value);
    }

    private static Double number(JsonNode value) {
        if (value == null || value.isNull() || (!value.isTextual() && !value.isNumber())) return null;
        String text = value.asText().trim();
        if (!text.matches("[+-]?(?:(?:\\d{1,3}(?:,\\d{3})+|\\d+)(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?")) return null;
        try {
            double parsed = Double.parseDouble(text.replace(",", ""));
            return Double.isFinite(parsed) ? parsed : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String text(JsonNode value) { return value.asText().toLowerCase(Locale.ROOT); }

    private static boolean ascii(String value) {
        for (int i = 0; i < value.length(); i++) if (value.charAt(i) > 127) return false;
        return true;
    }
}
