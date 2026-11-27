package com.horsecoder.common.whuTalent.core.flow.Skill.Where;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Calls the existing where-prior model service for semantic WHERE stages. */
final class WhereLlmSkillClient {
    private WhereLlmSkillClient() { }

    static ObjectNode review(ObjectNode request, JsonNode candidates) {
        if (!request.path("knowledgeContext").isObject())
            throw new IllegalArgumentException("knowledgeContext is required for WHERE_CONDITION_REVIEW");
        ObjectNode input = WhereSkillSupport.JSON.createObjectNode();
        input.put("task", "WHERE_CONDITION_REVIEW");
        ObjectNode modelRequest = input.putObject("request");
        for (String field : new String[]{"question", "table", "knowledgeContext", "conditions", "stageResults"})
            if (request.has(field)) modelRequest.set(field, request.path(field).deepCopy());
        modelRequest.set("reviewCandidates", candidates.deepCopy());
        ObjectNode response = post(input);
        if (!"SUCCEEDED".equals(response.path("status").asText())
                || !response.path("result").path("missing").isArray())
            throw new IllegalStateException("WHERE condition review returned no missing-condition list");
        ObjectNode result = WhereSkillSupport.output();
        result.set("missing", response.path("result").path("missing").deepCopy());
        setModelCall(result, response);
        return result;
    }

    static ObjectNode reviewGrounding(ObjectNode request, JsonNode condition, JsonNode candidates) {
        if (!request.path("knowledgeContext").isObject())
            throw new IllegalArgumentException("knowledgeContext is required for WHERE_GROUNDING_REVIEW");
        ObjectNode input = WhereSkillSupport.JSON.createObjectNode();
        input.put("task", "WHERE_GROUNDING_REVIEW");
        ObjectNode modelRequest = input.putObject("request");
        for (String field : new String[]{"question", "table", "knowledgeContext"})
            modelRequest.set(field, request.path(field).deepCopy());
        ObjectNode candidate = modelRequest.putObject("groundingCandidate");
        candidate.set("condition", condition.deepCopy());
        candidate.set("columns", candidates.deepCopy());
        ObjectNode response = post(input);
        if (!"SUCCEEDED".equals(response.path("status").asText())
                || !response.path("result").isObject()
                || !response.path("result").has("column"))
            throw new IllegalStateException("grounding review returned no column decision");
        ObjectNode result = WhereSkillSupport.output();
        result.set("decision", response.path("result").deepCopy());
        setModelCall(result, response);
        return result;
    }

    static ObjectNode apply(String task, ObjectNode request) {
        if (!request.path("knowledgeContext").isObject())
            throw new IllegalArgumentException("knowledgeContext is required for " + task);
        if (!"WHERE_CONDITION_EXTRACT".equals(task) && WhereSkillSupport.copyConditions(request).size() == 0) {
            ObjectNode empty = WhereSkillSupport.output();
            empty.set("conditions", WhereSkillSupport.JSON.createArrayNode());
            return empty;
        }
        ObjectNode input = WhereSkillSupport.JSON.createObjectNode();
        input.put("task", task);
        ObjectNode modelRequest = input.putObject("request");
        for (String field : new String[]{"question", "table", "knowledgeContext", "conditions"})
            if (request.has(field)) modelRequest.set(field, request.path(field).deepCopy());
        ObjectNode response = post(input);
        if (!"SUCCEEDED".equals(response.path("status").asText()) || !response.path("result").isObject())
            throw new IllegalStateException("WHERE model service returned no result for " + task);
        JsonNode prediction = response.path("result");
        ObjectNode result = WhereSkillSupport.output();
        ArrayNode conditions;
        if ("WHERE_CONDITION_EXTRACT".equals(task)) {
            JsonNode proposed = prediction.path("conditions");
            if (!proposed.isArray() || proposed.size() > 4) throw new IllegalStateException("invalid extracted conditions");
            conditions = WhereSkillSupport.JSON.createArrayNode();
            String question = request.path("question").asText();
            int cursor = 0;
            for (JsonNode item : proposed) {
                String surface = item.path("surface").asText("");
                JsonNode value = item.path("value");
                int start = findValueSpan(question, surface, cursor);
                if (start < 0) {
                    String numericSpan = numericSurface(question, surface, cursor);
                    if (numericSpan != null) {
                        surface = numericSpan;
                        start = findValueSpan(question, surface, cursor);
                    }
                }
                if (surface.isEmpty() || start < 0 || value.isMissingNode() || value.isNull()
                        || value.asText().trim().isEmpty())
                    throw new IllegalStateException("model condition is not grounded in the question: "
                            + surface.substring(0, Math.min(surface.length(), 80)));
                ObjectNode condition = WhereSkillSupport.JSON.createObjectNode();
                condition.put("surface", question.substring(start, start + surface.length()));
                condition.set("value", value.deepCopy());
                condition.put("start", start);
                condition.put("end", start + surface.length());
                conditions.add(condition);
                cursor = start + surface.length();
            }
            result.put("count", conditions.size());
        } else {
            conditions = WhereSkillSupport.copyConditions(request);
            String key = "WHERE_COLUMN_MATCH".equals(task) ? "columns"
                    : "WHERE_OPERATOR_RECOGNIZE".equals(task) ? "operators" : "values";
            JsonNode values = prediction.path(key);
            boolean operatorFallback = "operators".equals(key)
                    && (!values.isArray() || values.size() != conditions.size());
            if (operatorFallback) {
                values = WhereOperatorRecognize.apply(request).path("conditions");
                result.putArray("warnings").add("operator model output count mismatch; used local clause cues");
            }
            if (!values.isArray() || values.size() != conditions.size())
                throw new IllegalStateException("model output count does not match conditions for " + task);
            for (int i = 0; i < conditions.size(); i++) {
                ObjectNode condition = (ObjectNode) conditions.get(i);
                JsonNode value = values.get(i);
                if ("columns".equals(key)) {
                    int width = request.path("table").path("header").size();
                    if (!value.isNull() && (!value.isIntegralNumber() || value.asInt() < 0 || value.asInt() >= width))
                        throw new IllegalStateException("model returned an invalid column");
                    if (value.isNull()) condition.put("columnMatchStatus", "UNRESOLVED");
                    else condition.put("columnIndex", value.asInt());
                    JsonNode candidates = prediction.path("columnCandidates");
                    if (candidates.isArray() && candidates.size() == conditions.size()) {
                        JsonNode options = candidates.get(i);
                        if (!options.isArray()) throw new IllegalStateException("invalid column candidates");
                        ArrayNode safe = condition.putArray("columnCandidates");
                        for (JsonNode option : options) {
                            if (!option.isIntegralNumber() || option.asInt() < 0 || option.asInt() >= width)
                                throw new IllegalStateException("invalid column candidate");
                            safe.add(option.asInt());
                        }
                    }
                    JsonNode reasons = prediction.path("columnReasons");
                    if (reasons.isArray() && reasons.size() == conditions.size())
                        condition.put("columnMatchReason", reasons.get(i).asText(""));
                } else if ("operators".equals(key)) {
                    String operator = operatorFallback ? value.path("operator").asText("") : value.asText("");
                    if (WhereSkillSupport.operatorCode(operator) < 0)
                        throw new IllegalStateException("model returned an unsupported operator");
                    condition.put("operator", operator);
                } else {
                    if (!(value.isTextual() || value.isNumber()) || value.asText().trim().isEmpty())
                        throw new IllegalStateException("model returned an invalid value");
                    if (!WhereSkillSupport.valueKey(condition.path("value").asText())
                            .equals(WhereSkillSupport.valueKey(value.asText())))
                        result.withArray("warnings").add("value model disagreed with extracted question span; original retained");
                    else condition.set("value", value.deepCopy());
                }
            }
            if ("operators".equals(key)) {
                ArrayNode explicit = (ArrayNode) WhereOperatorRecognize.apply(request).path("conditions");
                for (int i = 0; i < conditions.size(); i++) {
                    String operator = explicit.get(i).path("operator").asText();
                    if (">=".equals(operator) || "<=".equals(operator) || "!=".equals(operator))
                        ((ObjectNode) conditions.get(i)).put("operator", operator);
                }
            }
        }
        result.set("conditions", conditions);
        setModelCall(result, response);
        return result;
    }

    private static void setModelCall(ObjectNode result, ObjectNode response) {
        ObjectNode call = result.putObject("modelCall");
        call.put("model", response.path("model").asText());
        call.put("promptSha256", response.path("promptSha256").asText());
        call.set("knowledgeIds", response.path("knowledgeIds").deepCopy());
        call.set("usage", response.path("usage").deepCopy());
    }

    private static int findValueSpan(String question, String surface, int from) {
        if (surface.isEmpty()) return -1;
        String text = question.toLowerCase(java.util.Locale.ROOT);
        String needle = surface.toLowerCase(java.util.Locale.ROOT);
        int at = text.indexOf(needle, from);
        while (at >= 0) {
            int end = at + surface.length();
            if ((at == 0 || !Character.isLetterOrDigit(question.charAt(at - 1)))
                    && (end == question.length() || !Character.isLetterOrDigit(question.charAt(end)))) return at;
            at = text.indexOf(needle, at + 1);
        }
        return -1;
    }

    private static String numericSurface(String question, String surface, int from) {
        if (!surface.matches("[+-]?\\d[\\d,]*(?:\\.\\d+)?")) return null;
        java.util.regex.Matcher found = java.util.regex.Pattern.compile(
                "(?<![\\w.])[+-]?\\d[\\d,]*(?:\\.\\d+)?(?![\\w.])").matcher(question);
        while (found.find()) if (found.start() >= from
                && WhereSkillSupport.valueKey(found.group()).equals(WhereSkillSupport.valueKey(surface)))
            return found.group();
        return null;
    }

    private static ObjectNode post(ObjectNode input) {
        String endpoint = System.getenv("WHERE_PRIOR_SKILL_URL");
        if (endpoint == null || endpoint.trim().isEmpty())
            endpoint = "http://127.0.0.1:18086/api/where-prior/skill";
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(120000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            byte[] body = input.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream stream = connection.getOutputStream()) { stream.write(body); }
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
            StringBuilder text = new StringBuilder();
            if (stream != null) try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) text.append(line);
            }
            if (status != 200) {
                JsonNode error = WhereSkillSupport.JSON.readTree(text.toString());
                throw new IllegalStateException("WHERE model service HTTP " + status + ": " + error.path("errorCode").asText("UNKNOWN"));
            }
            JsonNode response = WhereSkillSupport.JSON.readTree(text.toString());
            if (!response.isObject()) throw new IllegalStateException("WHERE model service returned invalid JSON");
            return (ObjectNode) response;
        } catch (Exception error) {
            throw new IllegalStateException("WHERE model request failed: " + error.getMessage(), error);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
