package application.adapters.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.domain.enums.UserRole;
import application.domain.services.order.NotifyOrderStatusChangeService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * A payment that fails at its last step leaves no order change, reserved stock or invoice.
 * {@code ConsistencyIT} repeats it on the real MySQL and MongoDB.
 */
class PaymentRollbackTest extends RestApiTest {

    @MockitoSpyBean
    private NotifyOrderStatusChangeService notifier;

    @Test
    void aFailedPaymentChangesNothing() throws Exception {
        String seller = tokenFor(UserRole.SELLER);
        StockedProduct product = stockedProduct(seller, 7);
        String buyer = tokenFor(UserRole.BUYER);
        String orderId = placeOrder(buyer, product.productId());
        doThrow(new IllegalStateException("The payment failed")).when(notifier).execute(any(), any());

        send(post("/api/v1/buyer/orders/" + orderId + "/pay"), buyer)
            .andExpect(status().isInternalServerError());

        doCallRealMethod().when(notifier).execute(any(), any());
        send(get("/api/v1/buyer/orders/" + orderId), buyer)
            .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));
        send(get("/api/v1/seller/inventory/" + product.productId() + "/" + product.warehouseId()), seller)
            .andExpect(jsonPath("$.onHand").value(7));
        send(get("/api/v1/buyer/orders/" + orderId + "/invoice"), buyer)
            .andExpect(status().isNotFound());
    }
}
