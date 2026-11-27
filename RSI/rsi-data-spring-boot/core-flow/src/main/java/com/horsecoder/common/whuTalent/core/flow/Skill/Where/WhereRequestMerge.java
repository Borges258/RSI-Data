package com.horsecoder.common.whuTalent.core.flow.Skill.Where;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Carries the complete WHERE request between independently registered child flows. */
@FlowComponent
public class WhereRequestMerge {
    public static final String FLOW_ID =
            "@com.horsecoder.common.whuTalent.dataTesting.core/WIKISQL_WHERE_GENERATION";

    private static final String[] STAGES = {
            "WHERE_CONDITION_EXTRACT",
            "WHERE_MULTI_CONDITION_EXTRACT",
            "WHERE_COMPLETENESS_CHECK",
            "WHERE_COLUMN_MATCH",
            "WHERE_OPERATOR_RECOGNIZE",
            "WHERE_VALUE_EXTRACT",
            "WHERE_SCHEMA_VALUE_MATCH",
            "WHERE_VALUE_NORMALIZE",
            "WHERE_CONDITION_VALIDATE",
            "WHERE_CONDITION_ASSEMBLE"
    };

    @Flow("WHERE_REQUEST_PREPARE")
    public void prepare(String request) {
        ObjectNode context;
        try {
            context = WhereSkillSupport.read(request);
        } catch (IllegalArgumentException e) {
            finish(WhereSkillSupport.JSON.createObjectNode(), true,
                    failure("INVALID_REQUEST", "WHERE_REQUEST_PREPARE", "request must be a JSON object"));
            return;
        }

        ArrayNode errors = WhereSkillSupport.JSON.createArrayNode();
        JsonNode question = context.path("question");
        JsonNode table = context.path("table");
        if (table.isObject() && !table.path("header").isArray() && table.path("headers").isArray())
            ((ObjectNode) table).set("header", table.path("headers").deepCopy());
        if (table.isObject() && table.path("valueAliases").isArray()) {
            ObjectNode aliases = WhereSkillSupport.JSON.createObjectNode();
            for (JsonNode alias : table.path("valueAliases"))
                if (alias.path("alias").isTextual() && !alias.path("canonical").isMissingNode())
                    aliases.set(alias.path("alias").asText(), alias.path("canonical").deepCopy());
            ((ObjectNode) table).set("valueAliases", aliases);
        }
        if (context.has("structure")) {
            try {
                ObjectNode derived = knowledgeFromStructure(context.path("structure"), table.path("id").asText());
                JsonNode supplied = context.path("knowledge");
                if (supplied.isObject() && (!derived.path("atoms").equals(supplied.path("atoms"))
                        || !derived.path("patterns").equals(supplied.path("patterns"))
                        || !derived.path("relations").equals(supplied.path("relations"))))
                    throw new IllegalArgumentException("canvas knowledge does not match submitted knowledge");
                context.set("knowledgeContext", derived);
            } catch (IllegalArgumentException e) {
                finish(context, true, failure("INVALID_REQUEST", "WHERE_REQUEST_PREPARE", e.getMessage()));
                return;
            }
        }
        if (!question.isTextual() || question.asText().trim().isEmpty())
            errors.add("question must be a nonempty string");
        if (!table.isObject()) {
            errors.add("table must be an object");
        } else {
            JsonNode header = table.path("header");
            JsonNode types = table.path("types");
            if (!header.isArray() || header.size() == 0) errors.add("table.header must be a nonempty array");
            if (!types.isArray() || !header.isArray() || types.size() != header.size())
                errors.add("table.types must match table.header");
            if (!table.path("rows").isArray()) errors.add("table.rows must be an array");
            if (header.isArray()) for (JsonNode column : header)
                if (!column.isTextual() || column.asText().trim().isEmpty())
                    errors.add("table.header entries must be nonempty strings");
            if (types.isArray()) for (JsonNode type : types)
                if (!type.isTextual() || type.asText().trim().isEmpty())
                    errors.add("table.types entries must be nonempty strings");
            if (table.path("rows").isArray()) for (JsonNode row : table.path("rows"))
                if (!row.isArray() || !header.isArray() || row.size() != header.size())
                    errors.add("table.rows entries must match table.header");
        }
        if (errors.size() != 0) {
            ObjectNode result = failure("INVALID_REQUEST", "WHERE_REQUEST_PREPARE", "missing or invalid fields");
            result.set("errors", errors);
            finish(context, true, result);
            return;
        }

        // Internal fields are owned by the composition, not by its caller.
        context.remove("conditions");
        context.remove("originalConditions");
        context.remove("stageResults");
        context.remove("conds");
        context.remove("count");
        context.remove("execution");
        context.remove("warnings");
        context.put("_whereStage", 0);
        context.set("stageResults", WhereSkillSupport.JSON.createObjectNode());
        context.set("warnings", WhereSkillSupport.JSON.createArrayNode());
        ObjectNode result = WhereSkillSupport.output();
        result.put("stage", "WHERE_REQUEST_PREPARE");
        finish(context, false, result);
    }

    @Flow("WHERE_REQUEST_MERGE")
    public void merge(String request, String stageResult) {
        ObjectNode context;
        try {
            context = WhereSkillSupport.read(request);
        } catch (IllegalArgumentException e) {
            finish(WhereSkillSupport.JSON.createObjectNode(), true,
                    failure("FLOW_ERROR", "WHERE_REQUEST_MERGE", "working request is invalid JSON"));
            return;
        }

        JsonNode stageIndex = context.path("_whereStage");
        int index = stageIndex.asInt(-1);
        if (!stageIndex.isIntegralNumber() || !stageIndex.canConvertToInt() || index < 0 || index >= STAGES.length
                || !context.path("stageResults").isObject()) {
            finish(context, true, failure("FLOW_ERROR", "WHERE_REQUEST_MERGE", "invalid stage index"));
            return;
        }
        String stage = STAGES[index];
        ObjectNode output;
        try {
            output = WhereSkillSupport.read(stageResult);
        } catch (IllegalArgumentException e) {
            finish(context, true, failure("FLOW_ERROR", stage, "child flow returned invalid JSON"));
            return;
        }

        ((ObjectNode) context.path("stageResults")).set(stage, output.deepCopy());
        if (output.path("warnings").isArray()) for (JsonNode warning : output.path("warnings"))
            ((ArrayNode) context.path("warnings")).add(warning.deepCopy());
        String status = output.path("status").asText("");
        if (!output.path("status").isTextual() || status.isEmpty()) {
            finish(context, true, failure("FLOW_ERROR", stage, "child flow returned no status"));
            return;
        }

        // Assembly returns conds instead of conditions. Its result must also pass through merge.
        if (index == STAGES.length - 1) {
            if (!"SUCCEEDED".equals(status)) {
                ObjectNode result = output.deepCopy();
                result.put("stage", stage);
                finish(context, true, result);
                return;
            }
            JsonNode conds = output.path("conds");
            if (!validAssembly(context, conds)) {
                finish(context, true, failure("FLOW_ERROR", stage, "invalid or reordered WikiSQL conds"));
                return;
            }
            context.set("conds", conds.deepCopy());
            context.put("_whereStage", index + 1);
            ObjectNode result = output.deepCopy();
            result.put("stage", stage);
            finish(context, false, result);
            return;
        }

        JsonNode conditions = output.path("conditions");
        if (!conditions.isArray() || !allObjects(conditions)) {
            if (!"SUCCEEDED".equals(status)) {
                ObjectNode result = output.deepCopy();
                result.put("stage", stage);
                finish(context, true, result);
                return;
            }
            finish(context, true, failure("FLOW_ERROR", stage, "child flow returned invalid conditions"));
            return;
        }
        if ((index <= 1 || output.has("count")) && (!output.path("count").isIntegralNumber()
                || !output.path("count").canConvertToInt()
                || output.path("count").asInt() != conditions.size())) {
            finish(context, true, failure("FLOW_ERROR", stage, "condition count does not match conditions"));
            return;
        }
        if (!validConditionSpans(context.path("question").asText(), conditions)) {
            finish(context, true, failure("FLOW_ERROR", stage, "missing or invalid condition spans"));
            return;
        }
        if (index > 0 && !(index == 2 && output.path("recovered").isArray()
                && conditions.size() == context.path("conditions").size() + output.path("recovered").size()
                && preservesConditionOrder(context.path("conditions"), conditions))
                && !sameConditionOrder(context.path("conditions"), conditions)) {
            finish(context, true, failure("FLOW_ERROR", stage, "condition order or identity changed"));
            return;
        }

        if (index == 0) context.set("originalConditions", conditions.deepCopy());
        context.set("conditions", conditions.deepCopy());
        context.put("count", conditions.size());
        context.put("_whereStage", index + 1);

        ObjectNode result = output.deepCopy();
        result.put("stage", stage);
        result.set("originalConditions", context.path("originalConditions").deepCopy());
        boolean halt = !"SUCCEEDED".equals(status)
                || (index == 2 && !"COMPLETE".equals(output.path("completeness").asText())
                    && !"UNCERTAIN".equals(output.path("completeness").asText()))
                || (index == 8 && (!output.path("valid").isBoolean() || !output.path("valid").asBoolean()));
        if (halt && "SUCCEEDED".equals(status)) result.put("status", "INVALID");
        finish(context, halt, result);
    }

    /** Used by the existing execution entrance when a child flow throws. */
    public static String flowFailure(Exception error, Object workingRequest) {
        ObjectNode context;
        try {
            context = WhereSkillSupport.read(workingRequest instanceof String ? (String) workingRequest : null);
        } catch (IllegalArgumentException e) {
            context = WhereSkillSupport.JSON.createObjectNode();
        }
        int index = context.path("_whereStage").asInt(-1);
        String stage = index >= 0 && index < STAGES.length ? STAGES[index] : "WHERE_REQUEST_PREPARE";
        String message = error.getMessage();
        if (message == null || message.trim().isEmpty()) message = error.getClass().getSimpleName();
        ObjectNode result = failure("FLOW_ERROR", stage, message);
        finish(context, true, result);
        return result.toString();
    }

    private static boolean allObjects(JsonNode conditions) {
        for (JsonNode condition : conditions) if (!condition.isObject()) return false;
        return true;
    }

    private static ObjectNode knowledgeFromStructure(JsonNode raw, String tableId) {
        JsonNode structure;
        try {
            structure = raw.isTextual() ? WhereSkillSupport.JSON.readTree(raw.asText()) : raw;
        } catch (Exception e) {
            throw new IllegalArgumentException("structure must be valid canvas JSON", e);
        }
        if (!structure.path("elements").isArray() || !structure.path("links").isArray() || tableId.isEmpty())
            throw new IllegalArgumentException("structure elements/links and table.id are required");
        ObjectNode knowledge = WhereSkillSupport.JSON.createObjectNode();
        knowledge.put("version", "wikisql-where-prior-v1");
        knowledge.put("activeTableId", tableId);
        ArrayNode atoms = knowledge.putArray("atoms");
        ArrayNode patterns = knowledge.putArray("patterns");
        ArrayNode relations = knowledge.putArray("relations");
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (JsonNode element : structure.path("elements")) {
            JsonNode node = element.path("pendant").path("data").path("detail").path("knowledge");
            if (!node.isObject() || !element.path("id").asText().equals(node.path("id").asText()))
                throw new IllegalArgumentException("invalid canvas knowledge node");
            if ("TABLE".equals(node.path("scope").asText()) && !tableId.equals(node.path("tableId").asText()))
                continue;
            if (!ids.add(node.path("id").asText())) throw new IllegalArgumentException("duplicate canvas knowledge node");
            ("PATTERN".equals(node.path("kind").asText()) ? patterns : atoms).add(node.deepCopy());
        }
        for (JsonNode link : structure.path("links")) {
            JsonNode relation = link.path("pendant").path("data").path("detail").path("knowledgeRelation");
            if (!relation.isObject() || !link.path("id").asText().equals(relation.path("id").asText()))
                throw new IllegalArgumentException("invalid canvas knowledge relation");
            if (ids.contains(relation.path("from").asText()) && ids.contains(relation.path("to").asText()))
                relations.add(relation.deepCopy());
        }
        return knowledge;
    }

    private static boolean validConditionSpans(String question, JsonNode conditions) {
        int previousEnd = 0;
        for (JsonNode condition : conditions) {
            JsonNode start = condition.path("start");
            JsonNode end = condition.path("end");
            if (!start.isIntegralNumber() || !start.canConvertToInt()
                    || !end.isIntegralNumber() || !end.canConvertToInt()
                    || !condition.path("surface").isTextual()) return false;
            int left = start.asInt(), right = end.asInt();
            if (left < previousEnd || right <= left || right > question.length()
                    || !question.substring(left, right).equals(condition.path("surface").asText())) return false;
            previousEnd = right;
        }
        return true;
    }

    private static boolean sameConditionOrder(JsonNode before, JsonNode after) {
        if (!before.isArray() || before.size() != after.size()) return false;
        for (int i = 0; i < before.size(); i++) {
            JsonNode previous = before.get(i);
            JsonNode current = after.get(i);
            if (!previous.isObject() || !current.isObject()
                    || !previous.path("start").equals(current.path("start"))
                    || !previous.path("end").equals(current.path("end"))
                    || !previous.path("surface").equals(current.path("surface"))) return false;
        }
        return true;
    }

    private static boolean preservesConditionOrder(JsonNode before, JsonNode after) {
        if (!before.isArray() || !after.isArray() || after.size() < before.size()) return false;
        int previous = 0;
        for (JsonNode condition : after) if (previous < before.size()
                && sameIdentity(before.get(previous), condition)) previous++;
        return previous == before.size();
    }

    private static boolean sameIdentity(JsonNode previous, JsonNode current) {
        return previous.isObject() && current.isObject()
                && previous.path("start").equals(current.path("start"))
                && previous.path("end").equals(current.path("end"))
                && previous.path("surface").equals(current.path("surface"));
    }

    private static boolean validAssembly(ObjectNode context, JsonNode conds) {
        ObjectNode expected = WhereConditionAssemble.apply(context);
        return "SUCCEEDED".equals(expected.path("status").asText())
                && expected.path("conds").equals(conds);
    }

    private static ObjectNode failure(String status, String stage, String message) {
        ObjectNode result = WhereSkillSupport.JSON.createObjectNode();
        result.put("status", status);
        result.put("stage", stage);
        result.putArray("errors").add(message);
        result.putArray("conds");
        return result;
    }

    private static void finish(ObjectNode request, boolean halt, ObjectNode result) {
        boolean completed = !halt && request.path("_whereStage").asInt() == STAGES.length;
        if (halt && (!result.path("errors").isArray() || result.path("errors").size() == 0))
            result.putArray("errors").add("stage stopped with status " + result.path("status").asText("FLOW_ERROR"));
        ObjectNode execution = WhereSkillSupport.JSON.createObjectNode();
        execution.put("status", result.path("status").asText("FLOW_ERROR"));
        execution.put("stage", result.path("stage").asText());
        execution.put("halt", halt);
        execution.put("completed", completed);
        if (halt) execution.set("errors", result.path("errors").deepCopy());
        if (request.path("warnings").isArray()) execution.set("warnings", request.path("warnings").deepCopy());
        request.set("execution", execution);
        result.put("halt", halt);
        result.put("completed", completed);
        if (!result.has("conds") || halt) result.putArray("conds");
        if (request.has("conditions")) result.set("conditions", request.path("conditions").deepCopy());
        if (request.has("originalConditions")) result.set("originalConditions", request.path("originalConditions").deepCopy());
        if (request.path("warnings").isArray()) result.set("warnings", request.path("warnings").deepCopy());
        result.set("request", request.deepCopy());
        FlowEngine engine = FlowEngine.instance();
        engine.register(FLOW_ID, "request", request.toString());
        engine.register(FLOW_ID, "halt", halt);
        engine.register(FLOW_ID, "result", result.toString());
    }
}
