package application.adapters.in.rest;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The OpenAPI description is public and complete.
 */
class OpenApiTest extends RestApiTest {

    private static final int ENDPOINTS = 117;
    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "patch", "delete");

    private String apiDocs(String path) throws Exception {
        return send(get(path), null).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    }

    /** Every operation of the group, as "METHOD path" and its OpenAPI object. */
    private static Map<String, Map<String, Object>> operations(String json) {
        Map<String, Map<String, Map<String, Object>>> paths = JsonPath.read(json, "$.paths");
        Map<String, Map<String, Object>> operations = new java.util.TreeMap<>();
        paths.forEach((path, methods) -> methods.forEach((method, operation) -> {
            if (HTTP_METHODS.contains(method)) {
                operations.put(method.toUpperCase() + " " + path, cast(operation));
            }
        }));
        return operations;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> cast(Object value) {
        return (Map<String, Object>) value;
    }

    @Test
    void theSpecificationIsPublic() throws Exception {
        send(get("/v3/api-docs"), null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.info.title").value("NexusMarket API"))
            .andExpect(jsonPath("$.info.version").value("1.0"))
            .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
            .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"));
    }

    @Test
    void swaggerUiIsPublic() throws Exception {
        send(get("/swagger-ui.html"), null).andExpect(status().is3xxRedirection());
        send(get("/swagger-ui/index.html"), null)
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("swagger")));
    }

    @Test
    void theSpecificationCoversEveryEndpoint() throws Exception {
        assertEquals(ENDPOINTS, operations(apiDocs("/v3/api-docs")).size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"public", "account", "buyer", "seller", "logistics", "admin", "supervisor"})
    void thereIsAGroupPerPort(String group) throws Exception {
        Map<String, Map<String, Object>> operations = operations(apiDocs("/v3/api-docs/" + group));

        assertFalse(operations.isEmpty());
        operations.keySet().forEach(key -> assertTrue(key.contains("/api/v1/" + group + "/"), key));
    }

    @Test
    void everyOperationHasASummaryAndItsResponses() throws Exception {
        operations(apiDocs("/v3/api-docs")).forEach((key, operation) -> {
            assertTrue(operation.containsKey("summary"), key);
            Map<String, Object> responses = cast(operation.get("responses"));
            assertTrue(responses.containsKey("200") || responses.containsKey("201"), key);
            if (!key.contains("/public/")) {
                assertTrue(responses.containsKey("401"), key);
                assertTrue(operation.containsKey("security"), key);
            }
            if (!key.contains("/public/") && !key.contains("/account/")) {
                assertTrue(responses.containsKey("403"), key);
            }
            if (!key.startsWith("GET") && !key.contains("/logout") && !key.contains("/login")) {
                assertTrue(responses.containsKey("400"), key);
            }
            if (!key.startsWith("GET") && !key.contains("/public/") && !key.contains("/account/")) {
                assertTrue(responses.containsKey("404"), key);
                assertTrue(responses.containsKey("409"), key);
                assertTrue(responses.containsKey("422"), key);
            }
        });
    }

    @Test
    void theAuthenticatedUserIsNotAParameter() throws Exception {
        List<String> names = JsonPath.read(apiDocs("/v3/api-docs"),
            "$.paths.*.*.parameters[*].name");

        assertFalse(names.contains("me"));
    }

    @Test
    void anIdIsDocumentedAsText() throws Exception {
        String type = JsonPath.read(apiDocs("/v3/api-docs"),
            "$.paths['/api/v1/buyer/orders/{orderId}'].get.parameters[0].schema.type");

        assertEquals("string", type);
    }
}
