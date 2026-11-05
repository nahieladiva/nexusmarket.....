package application.adapters.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.adapters.in.rest.controllers.GlobalExceptionHandler;
import application.adapters.in.rest.responses.ErrorResponse;
import application.domain.enums.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Every error code of the API, always with the same JSON format.
 */
class ErrorHandlingTest extends RestApiTest {

    private static void assertError(ResultActions result, int status) throws Exception {
        result.andExpect(status().is(status))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(status))
            .andExpect(jsonPath("$.error").value(HttpStatus.valueOf(status).getReasonPhrase()))
            .andExpect(jsonPath("$.message").isNotEmpty())
            .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void aMalformedBodyIs400() throws Exception {
        assertError(send(post("/api/v1/public/login"), null, "{not json"), 400);
    }

    @Test
    void anUnknownEnumValueIs400() throws Exception {
        String seller = tokenFor(UserRole.SELLER);
        assertError(send(post("/api/v1/seller/products"), seller, """
            {"code":"TEC-0001","type":"LIQUID","name":"X","price":{"amount":"1","currency":"COP"}}"""), 400);
    }

    @Test
    void anIdThatIsNotAUuidIs400() throws Exception {
        assertError(send(get("/api/v1/public/products/not-a-uuid"), null), 400);
    }

    @Test
    void anInvalidEmailFormatIs400() throws Exception {
        assertError(send(post("/api/v1/public/login"), null,
            "{\"email\":\"no-at-sign\",\"password\":\"x\"}"), 400);
    }

    @Test
    void wrongCredentialsAre401() throws Exception {
        assertError(send(post("/api/v1/public/login"), null,
            "{\"email\":\"%s\",\"password\":\"Wrong2026\"}".formatted(ADMIN_EMAIL)), 401);
    }

    @Test
    void aMissingTokenIs401() throws Exception {
        assertError(send(get("/api/v1/buyer/cart"), null), 401);
    }

    @Test
    void aRoleOutsideItsPrefixIs403() throws Exception {
        assertError(send(get("/api/v1/admin/users"), tokenFor(UserRole.BUYER)), 403);
    }

    @Test
    void theProductOfAnotherSellerIs403() throws Exception {
        String owner = tokenFor(UserRole.SELLER);
        String productId = createProduct(owner);

        assertError(send(post("/api/v1/seller/products/" + productId + "/publish"),
            tokenFor(UserRole.SELLER)), 403);
    }

    @Test
    void theOrderOfAnotherBuyerIs403() throws Exception {
        StockedProduct product = stockedProduct(tokenFor(UserRole.SELLER), 5);
        String orderId = placeOrder(tokenFor(UserRole.BUYER), product.productId());

        assertError(send(get("/api/v1/buyer/orders/" + orderId), tokenFor(UserRole.BUYER)), 403);
    }

    @Test
    void aMissingResourceIs404() throws Exception {
        assertError(send(get("/api/v1/public/products/" + UUID.randomUUID()), null), 404);
    }

    @Test
    void anUnknownRouteInsideAPrefixIs404() throws Exception {
        assertError(send(get("/api/v1/public/unknown"), null), 404);
    }

    @Test
    void aDuplicateEmailIs409() throws Exception {
        String email = registerBuyer();

        assertError(send(post("/api/v1/public/buyers"), null, """
            {"fullName":"Other","email":"%s","phone":"+573001112233","password":"%s",
             "defaultShippingAddress":%s}""".formatted(email, PASSWORD, ADDRESS)), 409);
    }

    @Test
    void anInvalidStatusChangeIs409() throws Exception {
        String seller = tokenFor(UserRole.SELLER);
        String warehouseId = createWarehouse(seller);
        send(post("/api/v1/seller/warehouses/" + warehouseId + "/deactivate"), seller)
            .andExpect(status().isOk());

        assertError(send(post("/api/v1/seller/warehouses/" + warehouseId + "/deactivate"), seller), 409);
    }

    @Test
    void cancellingACancelledOrderIs409() throws Exception {
        StockedProduct product = stockedProduct(tokenFor(UserRole.SELLER), 5);
        String buyer = tokenFor(UserRole.BUYER);
        String orderId = placeOrder(buyer, product.productId());
        send(post("/api/v1/buyer/orders/" + orderId + "/cancel"), buyer).andExpect(status().isOk());

        assertError(send(post("/api/v1/buyer/orders/" + orderId + "/cancel"), buyer), 409);
    }

    @Test
    void insufficientStockIs409() throws Exception {
        StockedProduct product = stockedProduct(tokenFor(UserRole.SELLER), 1);
        String buyer = tokenFor(UserRole.BUYER);
        send(post("/api/v1/buyer/cart/items"), buyer,
            "{\"productId\":\"%s\",\"quantity\":2}".formatted(product.productId()))
            .andExpect(status().isOk());

        assertError(send(post("/api/v1/buyer/orders"), buyer), 409);
    }

    @Test
    void anEmptyCartCannotBecomeAnOrderAnd422() throws Exception {
        String buyer = tokenFor(UserRole.BUYER);
        send(delete("/api/v1/buyer/cart"), buyer).andExpect(status().isOk());

        assertError(send(post("/api/v1/buyer/orders"), buyer), 422);
    }

    @Test
    void aWeakPasswordIs422() throws Exception {
        assertError(send(post("/api/v1/public/buyers"), null, """
            {"fullName":"Ana","email":"%s","phone":"+573001112233","password":"short",
             "defaultShippingAddress":%s}""".formatted(uniqueEmail("weak"), ADDRESS)), 422);
    }

    @Test
    void aWrongMethodIs405() throws Exception {
        assertError(send(delete("/api/v1/public/products"), null), 405);
    }

    @Test
    void aMissingContentTypeIs415() throws Exception {
        assertError(mvc.perform(post("/api/v1/public/login").content("{}")), 415);
    }

    @Test
    void anUnexpectedErrorIs500AndHidesItsDetails() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/public/products");

        ResponseEntity<ErrorResponse> response = new GlobalExceptionHandler()
            .handleUnexpected(new NullPointerException("secret detail"), request);

        assertEquals(500, response.getStatusCode().value());
        assertFalse(response.getBody().message().contains("secret"));
    }
}
