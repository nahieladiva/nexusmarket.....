package application.adapters.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.domain.enums.UserRole;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/**
 * Simultaneous updates of the same inventory: each one is applied or answers 409, and no unit is lost.
 */
class ConcurrentStockTest extends RestApiTest {

    private static final int REQUESTS = 12;
    private static final int INITIAL_UNITS = 100;

    @Test
    void simultaneousAdjustmentsDoNotLoseUnits() throws Exception {
        String seller = tokenFor(UserRole.SELLER);
        StockedProduct product = stockedProduct(seller, INITIAL_UNITS);
        String body = "{\"productId\":\"%s\",\"warehouseId\":\"%s\",\"delta\":1,\"reason\":\"Count\"}"
            .formatted(product.productId(), product.warehouseId());

        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> results = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(REQUESTS);
        for (int i = 0; i < REQUESTS; i++) {
            results.add(pool.submit(() -> {
                start.await();
                return send(post("/api/v1/seller/inventory/adjust"), seller, body)
                    .andReturn().getResponse().getStatus();
            }));
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(1, TimeUnit.MINUTES));

        int applied = 0;
        for (Future<Integer> result : results) {
            int status = result.get();
            assertTrue(status == 200 || status == 409, "Unexpected status " + status);
            applied += status == 200 ? 1 : 0;
        }
        assertTrue(applied >= 1);
        int onHand = com.jayway.jsonpath.JsonPath.read(send(get("/api/v1/seller/inventory/"
                + product.productId() + "/" + product.warehouseId()), seller)
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "$.onHand");
        assertEquals(INITIAL_UNITS + applied, onHand);
    }
}
