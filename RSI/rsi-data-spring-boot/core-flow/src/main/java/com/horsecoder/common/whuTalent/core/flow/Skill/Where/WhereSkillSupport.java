package com.horsecoder.common.whuTalent.core.flow.Skill.Where;

import cc.eamon.open.flow.core.FlowEngine;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared JSON and table lookup primitives for the independent WHERE flow events.
 * Request: {question, table:{header,types,rows,valueAliases?,columnAliases?}, conditions?}.
 * Each event receives that request as a JSON string and registers a JSON-string result.
 * A condition carries surface, value, columnIndex, operator, start and end as available.
 */
final class WhereSkillSupport {
    static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern SPACE = Pattern.compile("\\s+");
    private static final Pattern NUMBER = Pattern.compile("(?<![\\w.])-?\\d[\\d,]*(?:\\.\\d+)?%?(?![\\w.])");

    private WhereSkillSupport() { }

    static ObjectNode read(String request) {
        try {
            JsonNode node = JSON.readTree(request);
            if (node == null || !node.isObject()) throw new IllegalArgumentException("request must be a JSON object");
            return (ObjectNode) node;
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid WHERE skill request JSON", e);
        }
    }

    static void emit(ObjectNode result) { FlowEngine.instance().register("result", result.toString()); }
    static ObjectNode output() { ObjectNode n = JSON.createObjectNode(); n.put("status", "SUCCEEDED"); return n; }
    static ArrayNode copyConditions(JsonNode request) {
        JsonNode conditions = request.path("conditions");
        if (!conditions.isArray()) return JSON.createArrayNode();
        return ((ArrayNode) conditions).deepCopy();
    }
    static String clean(String value) {
        if (value == null) return "";
        String s = SPACE.matcher(value.trim()).replaceAll(" ");
        while (s.length() >= 2 && ((s.startsWith("\"") && s.endsWith("\""))
                || (s.startsWith("“") && s.endsWith("”"))
                || (s.startsWith("'") && s.endsWith("'")))) s = s.substring(1, s.length() - 1).trim();
        return s.toLowerCase(Locale.ROOT);
    }
    /** Comparison key only; never use it as a replacement for a WHERE literal. */
    static String valueKey(String value) {
        String s = Normalizer.normalize(clean(value), Normalizer.Form.NFKC);
        if (s.matches("[+-]?(?:\\d{1,3}(?:,\\d{3})+|\\d+)(?:\\.\\d+)?")) {
            try { return new BigDecimal(s.replace(",", "")).stripTrailingZeros().toPlainString(); }
            catch (NumberFormatException ignored) { /* keep the original comparison key */ }
        }
        return s.replaceAll("\\s*([,.;:!?])\\s*", "$1");
    }
    static String text(JsonNode n, String field) { return n.path(field).asText(""); }
    static boolean present(String question, String value) {
        String q = clean(question), v = clean(value);
        if (v.length() < 2) return false;
        int at = q.indexOf(v);
        while (at >= 0) {
            int end = at + v.length();
            boolean left = at == 0 || !Character.isLetterOrDigit(q.charAt(at - 1));
            boolean right = end == q.length() || !Character.isLetterOrDigit(q.charAt(end));
            if (left && right) return true;
            at = q.indexOf(v, at + 1);
        }
        return false;
    }

    static final class Mention {
        final String value;
        final int column;
        final int start;
        final int end;
        Mention(String value, int column, int start, int end) {
            this.value = value; this.column = column; this.start = start; this.end = end;
        }
    }

    static List<Mention> mentions(String question, JsonNode table) {
        List<Mention> found = new ArrayList<>();
        String lower = question.toLowerCase(Locale.ROOT);
        JsonNode headers = table.path("header");
        JsonNode rows = table.path("rows");
        if (rows.isArray()) for (JsonNode row : rows) {
            if (!row.isArray()) continue;
            for (int col = 0; col < row.size(); col++) {
                JsonNode cell = row.get(col);
                if (cell == null || cell.isNull()) continue;
                String value = cell.asText();
                String stripped = clean(value);
                if (stripped.length() < 3 || stripped.equals("n/a") || !present(question, stripped)) continue;
                boolean headerValue = false;
                if (headers.isArray()) for (JsonNode header : headers)
                    if (clean(header.asText()).equals(stripped)) { headerValue = true; break; }
                if (headerValue) continue;
                int start = lower.indexOf(stripped);
                if (start < 0) continue;
                found.add(new Mention(value, col, start, start + stripped.length()));
            }
        }
        JsonNode aliases = table.path("valueAliases");
        if (aliases.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> entries = aliases.fields();
            while (entries.hasNext()) {
                Map.Entry<String, JsonNode> alias = entries.next();
                String key = clean(alias.getKey()), canonical = alias.getValue().asText();
                if (key.length() < 3 || !present(question, key)) continue;
                int start = lower.indexOf(key);
                if (start < 0) continue;
                for (int col = 0; headers.isArray() && col < headers.size(); col++) {
                    if (hasCell(table, col, canonical)) {
                        found.add(new Mention(canonical, col, start, start + key.length()));
                        break;
                    }
                }
            }
        }
        // Preserve numbers even when the table has no matching row (threshold comparisons).
        Matcher matcher = NUMBER.matcher(question);
        while (matcher.find()) {
            int end = matcher.end();
            while (end > matcher.start() && question.charAt(end - 1) == ',') end--;
            boolean inHeader = false;
            if (headers.isArray()) for (JsonNode header : headers) {
                String h = clean(header.asText());
                int at = lower.indexOf(h);
                if (at >= 0 && matcher.start() >= at && end <= at + h.length()) { inHeader = true; break; }
            }
            String after = lower.substring(end, Math.min(lower.length(), end + 15));
            if (after.matches("^\\s+minutes?\\b.*") && headers.isArray()) for (JsonNode header : headers)
                if (clean(header.asText()).contains("mins")) { inHeader = true; break; }
            if (!inHeader && end > matcher.start())
                found.add(new Mention(question.substring(matcher.start(), end), -1, matcher.start(), end));
        }
        found.sort(Comparator.comparingInt((Mention m) -> m.start)
                .thenComparing((Mention m) -> -(m.end - m.start)));
        List<Mention> unique = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Mention m : found) {
            boolean contained = false;
            for (Mention prior : unique) if (m.start >= prior.start && m.end <= prior.end) { contained = true; break; }
            if (!contained && seen.add(m.start + ":" + m.end)) unique.add(m);
        }
        return unique;
    }

    private static boolean hasCell(JsonNode table, int column, String value) {
        JsonNode rows = table.path("rows");
        if (rows.isArray()) for (JsonNode row : rows)
            if (row.isArray() && column < row.size() && clean(row.get(column).asText()).equals(clean(value))) return true;
        return false;
    }

    static ArrayNode extracted(String question, JsonNode table) {
        ArrayNode out = JSON.createArrayNode();
        for (Mention mention : mentions(question, table)) {
            ObjectNode condition = JSON.createObjectNode();
            condition.put("surface", question.substring(mention.start, mention.end));
            condition.put("value", mention.value);
            if (mention.column >= 0) condition.put("columnIndex", mention.column);
            condition.put("start", mention.start);
            condition.put("end", mention.end);
            out.add(condition);
        }
        return out;
    }

    static int bestColumn(JsonNode table, String question, JsonNode condition) {
        JsonNode headers = table.path("header");
        if (!headers.isArray()) return -1;
        String surface = clean(text(condition, "surface"));
        if (surface.isEmpty()) surface = clean(text(condition, "value"));
        String before = question.toLowerCase(Locale.ROOT);
        int position = before.indexOf(surface);
        if (position >= 0) before = before.substring(Math.max(0, position - 60), position);
        int boundary = -1;
        for (String cue : new String[]{" for ", " when ", " with ", " where ", " whose ", " by "})
            boundary = Math.max(boundary, before.lastIndexOf(cue));
        if (boundary >= 0) before = before.substring(boundary + 1);
        double best = -1; int selected = -1;
        for (int c = 0; c < headers.size(); c++) {
            String header = clean(headers.get(c).asText());
            double score = 0;
            if (present(question, header)) score += 1;
            if (present(before, header)) score += 10;
            for (String part : header.split("[^\\p{Alnum}]+")) {
                if (part.length() >= 4 && present(before, part)) score += 3;
            }
            if (header.contains("title") && question.matches("(?is).*\\b(title|titled|named|episode|song)\\b.*")) score += 3;
            if (header.contains("winner") && question.matches("(?is).*\\b(winner|winning)\\b.*")) score += 3;
            if (header.contains("home") && question.matches("(?is).*\\bhome\\b.*")) score += 2;
            if (header.contains("away") && question.matches("(?is).*\\baway\\b.*")) score += 2;
            if (header.contains("season") && question.matches("(?is).*\\bseason\\b.*")) score += 2;
            JsonNode columnAliases = table.path("columnAliases").path(String.valueOf(c));
            if (columnAliases.isArray()) for (JsonNode alias : columnAliases)
                if (present(before, alias.asText())) score += 8;
            JsonNode rows = table.path("rows");
            if (rows.isArray()) for (JsonNode row : rows) if (row.isArray() && c < row.size()
                    && clean(row.get(c).asText()).equals(surface)) { score += 20; break; }
            if (condition.path("columnIndex").asInt(-1) == c) score += 0.1;
            if (score > best) { best = score; selected = c; }
        }
        return best > 0 ? selected : -1;
    }

    static String canonical(JsonNode table, int column, String value) {
        if (column < 0) return value;
        JsonNode rows = table.path("rows");
        if (rows.isArray()) for (JsonNode row : rows) if (row.isArray() && column < row.size()) {
            JsonNode cell = row.get(column);
            if (cell != null && !cell.isNull() && clean(cell.asText()).equals(clean(value))) return cell.asText();
        }
        return value;
    }

    static int operatorCode(String operator) {
        if ("=".equals(operator)) return 0;
        if (">".equals(operator)) return 1;
        if ("<".equals(operator)) return 2;
        // Extended WHERE codes; keep the existing WikiSQL codes unchanged.
        if (">=".equals(operator)) return 3;
        if ("<=".equals(operator)) return 4;
        if ("!=".equals(operator)) return 5;
        return -1;
    }
}
