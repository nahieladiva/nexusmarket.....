package application.adapters.in.rest;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

/**
 * Validation of the request DTOs and normalization of emails.
 */
class ValidationTest extends RestApiTest {

    private static final String BUYER_BODY = """
        {"fullName":"Ana","email":"%s","phone":"+573001112233","password":"%s",
         "defaultShippingAddress":%s}""";

    @Test
    void aMissingFieldIsReportedByName() throws Exception {
        send(post("/api/v1/public/login"), null, "{\"email\":\"a@mail.com\"}")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message", containsString("password")));
    }

    @Test
    void everyInvalidFieldIsReported() throws Exception {
        send(post("/api/v1/public/login"), null, "{\"email\":\" \",\"password\":\"\"}")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("email")))
            .andExpect(jsonPath("$.message", containsString("password")));
    }

    @Test
    void nestedObjectsAreValidated() throws Exception {
        send(post("/api/v1/public/buyers"), null, BUYER_BODY.formatted(
            uniqueEmail("nested"), PASSWORD, "{\"street\":\"10th Street\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("defaultShippingAddress.city")));
    }

    @Test
    void aMissingNestedObjectIsRejected() throws Exception {
        send(post("/api/v1/public/buyers"), null, """
            {"fullName":"Ana","email":"%s","phone":"+573001112233","password":"%s"}"""
            .formatted(uniqueEmail("noaddress"), PASSWORD))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("defaultShippingAddress")));
    }

    @Test
    void aQuantityMustBePositive() throws Exception {
        StockedProduct product = stockedProduct(tokenFor(UserRole.SELLER), 5);

        send(post("/api/v1/buyer/cart/items"), tokenFor(UserRole.BUYER),
            "{\"productId\":\"%s\",\"quantity\":0}".formatted(product.productId()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("quantity")));
    }

    @Test
    void anIdInTheBodyMustBeAUuid() throws Exception {
        send(post("/api/v1/buyer/cart/items"), tokenFor(UserRole.BUYER),
            "{\"productId\":\"not-a-uuid\",\"quantity\":1}")
            .andExpect(status().isBadRequest());
    }

    @Test
    void optionalFieldsCanBeOmitted() throws Exception {
        send(patch("/api/v1/account/profile"), tokenFor(UserRole.BUYER), "{\"fullName\":\"New Name\"}")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("New Name"));
    }

    @Test
    void anEmailIsStoredTrimmedAndInLowerCase() throws Exception {
        String email = uniqueEmail("clean");

        send(post("/api/v1/public/buyers"), null,
            BUYER_BODY.formatted("  " + email.toUpperCase() + " ", PASSWORD, ADDRESS))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void aDuplicateEmailIsDetectedWithUpperCaseAndSpaces() throws Exception {
        String email = registerBuyer();

        send(post("/api/v1/public/buyers"), null,
            BUYER_BODY.formatted(" " + email.toUpperCase() + "  ", PASSWORD, ADDRESS))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void theLoginIgnoresCaseAndSpacesInTheEmail() throws Exception {
        String email = registerBuyer();

        login("  " + email.toUpperCase() + " ", PASSWORD);
    }
}
