package com.horsecoder.agentlab.whereprior;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WherePriorControllerTest {
    private static final String REQUEST = "{\"question\":\"Which cities have population greater than 100?\","
            + "\"table\":{\"id\":\"demo\",\"headers\":[\"City\",\"Population\"],\"types\":[\"text\",\"real\"],\"rows\":[[\"A\",110],[\"B\",90]]},"
            + "\"knowledge\":{\"version\":\"wikisql-where-prior-v1\",\"activeTableId\":\"demo\","
            + "\"atoms\":[{\"id\":\"g-gt\",\"scope\":\"GLOBAL\",\"source\":\"HUMAN\",\"trust\":\"EXPLICIT_RULE\",\"statement\":\"greater than means strict >\"}],"
            + "\"patterns\":[],\"relations\":[],\"precedence\":[\"CURRENT_TABLE_FACT\",\"EXPLICIT_RULE\",\"STATISTICAL_REFERENCE\"]}}";

    @Test public void prepareInjectsKnowledgeAndRejectsCrossTableScope() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new WherePriorController()).build();
        mvc.perform(post("/api/where-prior/prepare").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isOk()).andExpect(jsonPath("$.knowledgeIds", hasItem("g-gt")))
                .andExpect(jsonPath("$.promptSha256").exists());
        mvc.perform(post("/api/where-prior/prepare").contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST.replace("\"activeTableId\":\"demo\"", "\"activeTableId\":\"other\"")))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
    }

    @Test public void disabledProviderMakesNoRequest() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        MockMvc mvc = mvc(new HashMap<>(), calls, null);
        mvc.perform(post("/api/where-prior/parse").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.errorCode").value("LLM_DISABLED"));
        org.junit.Assert.assertEquals(0, calls.get());
    }

    @Test public void statisticalKnowledgeCannotClaimHumanPriority() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new WherePriorController()).build();
        mvc.perform(post("/api/where-prior/prepare").contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST.replace("\"source\":\"HUMAN\"", "\"source\":\"WIKISQL_TRAIN\"")))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
    }

    @Test public void enabledProviderAcceptsValidWhereAndRejectsMalformedJson() throws Exception {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("WHERE_PRIOR_ALLOW_LLM", "true");
        configuration.put("WHERE_PRIOR_BASE_URL", "http://localhost:9999/v1");
        configuration.put("WHERE_PRIOR_API_KEY", "test-only");
        configuration.put("WHERE_PRIOR_MODEL", "stub");
        AtomicInteger calls = new AtomicInteger();
        String valid = "{\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"content\":\"{\\\"status\\\":\\\"SUCCEEDED\\\",\\\"conds\\\":[[1,1,100]],\\\"explanation\\\":\\\"strict threshold\\\"}\"}}],\"usage\":{\"total_tokens\":25}}";
        mvc(configuration, calls, valid).perform(post("/api/where-prior/parse")
                .contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isOk()).andExpect(jsonPath("$.conds[0][0]").value(1))
                .andExpect(jsonPath("$.knowledgeIds", hasItem("g-gt")));
        org.junit.Assert.assertEquals(1, calls.get());
        String invalid = "{\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"content\":\"not JSON\"}}]}";
        mvc(configuration, calls, invalid).perform(post("/api/where-prior/parse")
                .contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isBadGateway()).andExpect(jsonPath("$.errorCode").value("LLM_OUTPUT_INVALID"));
    }

    @Test public void invalidProviderUrlIsConfigurationErrorWithoutRequest() throws Exception {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("WHERE_PRIOR_ALLOW_LLM", "true");
        configuration.put("WHERE_PRIOR_BASE_URL", "http://example.invalid/v1");
        configuration.put("WHERE_PRIOR_API_KEY", "test-only");
        configuration.put("WHERE_PRIOR_MODEL", "stub");
        AtomicInteger calls = new AtomicInteger();
        mvc(configuration, calls, null).perform(post("/api/where-prior/parse")
                .contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("LLM_CONFIGURATION_INVALID"));
        org.junit.Assert.assertEquals(0, calls.get());
    }

    private MockMvc mvc(Map<String, String> configuration, AtomicInteger calls, String providerJson) {
        RestTemplate provider = new RestTemplate() {
            @Override public <T> T postForObject(String url, Object request, Class<T> responseType, Object... uriVariables) {
                calls.incrementAndGet();
                try { return responseType.cast(new ObjectMapper().readTree(providerJson)); }
                catch (Exception error) { throw new RuntimeException(error); }
            }
        };
        return MockMvcBuilders.standaloneSetup(new WherePriorController(configuration::get, provider)).build();
    }
}
