package application.adapters.in.rest;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/**
 * Tests of what anyone can do without a token.
 */
class PublicControllerTest extends RestApiTest {

    @Test
    void theInitialAdministratorCanLogIn() throws Exception {
        send(post("/api/v1/public/login"), null,
            "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(ADMIN_EMAIL, ADMIN_PASSWORD))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.user.role").value("ADMINISTRATOR"))
            .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void aBuyerCanRegisterAndLogIn() throws Exception {
        String email = uniqueEmail("buyer");
        send(post("/api/v1/public/buyers"), null, """
            {"fullName":"Ana","email":"%s","phone":"+573001112233","password":"%s",
             "defaultShippingAddress":%s}""".formatted(email, PASSWORD, ADDRESS))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.role").value("BUYER"));

        send(get("/api/v1/account/profile"), login(email, PASSWORD))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void theCatalogNeedsNoToken() throws Exception {
        send(get("/api/v1/public/products"), null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void theOldRoutesAreDenied() throws Exception {
        send(post("/api/auth/login"), null, "{}").andExpect(status().isUnauthorized());
        send(post("/api/auth/login"), adminToken(), "{}").andExpect(status().isForbidden());
    }

    @Test
    void anAdministratorIsListedWithItsRole() throws Exception {
        send(get("/api/v1/admin/users"), adminToken())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].role", hasItem("ADMINISTRATOR")));
    }
}
