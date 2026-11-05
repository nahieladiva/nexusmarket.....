package application.adapters.in.rest;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

/**
 * The main path of each controller, over the real context.
 */
class ControllersHappyPathTest extends RestApiTest {

    /** Sells a unit to a new buyer and leaves the order paid. */
    private PaidOrder paidOrder(String seller) throws Exception {
        StockedProduct product = stockedProduct(seller, 10);
        String buyer = tokenFor(UserRole.BUYER);
        String orderId = placeOrder(buyer, product.productId());
        send(post("/api/v1/buyer/orders/" + orderId + "/pay"), buyer)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"));
        return new PaidOrder(buyer, orderId, product);
    }

    /** Takes a paid order through its shipment until it is delivered. */
    private void deliver(PaidOrder order, String operator) throws Exception {
        String shipmentId = read(send(post("/api/v1/logistics/shipments"), operator,
            "{\"orderId\":\"%s\",\"warehouseId\":\"%s\"}".formatted(order.id(),
                order.product().warehouseId()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PREPARING")), "$.id");
        send(post("/api/v1/logistics/shipments/" + shipmentId + "/pack"), operator)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PACKED"));
        send(post("/api/v1/logistics/shipments/" + shipmentId + "/dispatch"), operator,
            "{\"trackingNumber\":\"TRACK-1\"}")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_TRANSIT"))
            .andExpect(jsonPath("$.trackingNumber").value("TRACK-1"));
        send(post("/api/v1/logistics/shipments/" + shipmentId + "/deliver"), operator)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void accountManagesProfileAddressesAndPassword() throws Exception {
        String email = registerBuyer();
        String token = login(email, PASSWORD);

        send(patch("/api/v1/account/profile"), token, "{\"phone\":\"+573009998877\"}")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.phone").value("+573009998877"));
        send(post("/api/v1/account/addresses"), token, ADDRESS)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.addresses", hasSize(1)));
        send(post("/api/v1/account/addresses/remove"), token, ADDRESS)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.addresses", hasSize(0)));

        send(put("/api/v1/account/password"), token,
            "{\"currentPassword\":\"%s\",\"newPassword\":\"NewPassword2026\"}".formatted(PASSWORD))
            .andExpect(status().isOk());

        send(get("/api/v1/account/profile"), token).andExpect(status().isUnauthorized());
        send(get("/api/v1/account/profile"), login(email, "NewPassword2026"))
            .andExpect(status().isOk());
    }

    @Test
    void sellerManagesProductsWarehousesAndStock() throws Exception {
        String seller = tokenFor(UserRole.SELLER);
        StockedProduct product = stockedProduct(seller, 10);
        String item = "\"productId\":\"%s\",\"warehouseId\":\"%s\""
            .formatted(product.productId(), product.warehouseId());

        send(patch("/api/v1/seller/products/" + product.productId()), seller, "{\"name\":\"Speaker\"}")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Speaker"));
        send(put("/api/v1/seller/products/" + product.productId() + "/price"), seller,
            "{\"price\":{\"amount\":\"99000.00\",\"currency\":\"COP\"}}")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.priceAmount").value("99000.00"));
        send(post("/api/v1/seller/inventory/restock"), seller,
            "{%s,\"quantity\":5,\"reason\":\"Purchase\"}".formatted(item))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.onHand").value(15));
        send(post("/api/v1/seller/inventory/adjust"), seller,
            "{%s,\"delta\":-3,\"reason\":\"Damaged\"}".formatted(item))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.onHand").value(12));
        send(put("/api/v1/seller/inventory/threshold"), seller,
            "{%s,\"reorderThreshold\":4}".formatted(item))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reorderThreshold").value(4));
        send(get("/api/v1/seller/inventory/" + product.productId() + "/" + product.warehouseId()
            + "/movements"), seller)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));
        send(get("/api/v1/seller/products"), seller)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("PUBLISHED"));
        send(post("/api/v1/seller/products/" + product.productId() + "/suspend"), seller)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUSPENDED"));
        send(get("/api/v1/public/products/" + product.productId()), null)
            .andExpect(status().isNotFound());
    }

    @Test
    void buyerUsesTheCartAndPays() throws Exception {
        StockedProduct product = stockedProduct(tokenFor(UserRole.SELLER), 10);
        String buyer = tokenFor(UserRole.BUYER);

        send(post("/api/v1/buyer/cart/items"), buyer,
            "{\"productId\":\"%s\",\"quantity\":1}".formatted(product.productId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(1)));
        send(patch("/api/v1/buyer/cart/items/" + product.productId()), buyer, "{\"quantity\":3}")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].quantity").value(3))
            .andExpect(jsonPath("$.totalAmount").value("450000.00"));

        String orderId = read(send(post("/api/v1/buyer/orders"), buyer)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING_PAYMENT")), "$.id");
        send(get("/api/v1/buyer/cart"), buyer)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(0)));

        send(post("/api/v1/buyer/orders/" + orderId + "/pay"), buyer)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"))
            .andExpect(jsonPath("$.items[0].warehouseId").value(product.warehouseId()));
        send(get("/api/v1/buyer/orders/" + orderId + "/invoice"), buyer)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ISSUED"))
            .andExpect(jsonPath("$.totalAmount").value("450000.00"));
        send(get("/api/v1/buyer/orders"), buyer)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void logisticsDeliversAnOrder() throws Exception {
        PaidOrder order = paidOrder(tokenFor(UserRole.SELLER));

        deliver(order, tokenFor(UserRole.LOGISTICS_OPERATOR));

        send(get("/api/v1/buyer/orders/" + order.id()), order.buyer())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DELIVERED"));
        send(get("/api/v1/buyer/orders/" + order.id() + "/shipments"), order.buyer())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("DELIVERED"));
    }

    @Test
    void aReturnEndsInAProcessedRefund() throws Exception {
        PaidOrder order = paidOrder(tokenFor(UserRole.SELLER));
        String operator = tokenFor(UserRole.LOGISTICS_OPERATOR);
        String admin = adminToken();
        deliver(order, operator);

        String returnId = read(send(post("/api/v1/buyer/orders/" + order.id() + "/returns"), order.buyer(),
            "{\"productId\":\"%s\",\"quantity\":1,\"reason\":\"Damaged\"}"
                .formatted(order.product().productId()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("REQUESTED")), "$.id");
        send(post("/api/v1/admin/returns/" + returnId + "/approve"), admin)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"));
        send(post("/api/v1/logistics/returns/" + returnId + "/receive"), operator)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RECEIVED"));

        String refundId = read(send(get("/api/v1/buyer/orders/" + order.id() + "/refunds"), order.buyer())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].returnId").value(returnId))
            .andExpect(jsonPath("$[0].status").value("PENDING")), "$[0].id");
        send(post("/api/v1/admin/refunds/" + refundId + "/process"), admin)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PROCESSED"));
    }

    @Test
    void cancellingAPaidOrderCreatesARefundAndVoidsTheInvoice() throws Exception {
        PaidOrder order = paidOrder(tokenFor(UserRole.SELLER));

        send(post("/api/v1/buyer/orders/" + order.id() + "/cancel"), order.buyer())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));

        send(get("/api/v1/buyer/orders/" + order.id() + "/invoice"), order.buyer())
            .andExpect(jsonPath("$.status").value("VOIDED"));
        send(get("/api/v1/buyer/orders/" + order.id() + "/refunds"), order.buyer())
            .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void administratorManagesSellersWarehousesAndProducts() throws Exception {
        String admin = adminToken();
        String sellerEmail = registerSeller();
        String seller = login(sellerEmail, PASSWORD);
        StockedProduct product = stockedProduct(seller, 5);

        send(post("/api/v1/admin/products/" + product.productId() + "/block"), admin)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("BLOCKED"));
        send(get("/api/v1/public/products/" + product.productId()), null)
            .andExpect(status().isNotFound());
        send(post("/api/v1/admin/products/" + product.productId() + "/unblock"), admin)
            .andExpect(status().isOk());

        String warehouseId = read(send(post("/api/v1/admin/warehouses"), admin, """
            {"name":"Central %d","address":%s,"location":%s}""".formatted(next(), ADDRESS, LOCATION))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.type").value("MARKETPLACE")), "$.id");
        send(put("/api/v1/admin/warehouses/" + warehouseId), admin, "{\"name\":\"Renamed %d\"}".formatted(next()))
            .andExpect(status().isOk());
        send(get("/api/v1/admin/inventory/low-stock"), admin).andExpect(status().isOk());
        send(get("/api/v1/admin/orders"), admin).andExpect(status().isOk());
        send(get("/api/v1/admin/refunds"), admin).andExpect(status().isOk());
    }

    @Test
    void administratorCanCancelAnOrder() throws Exception {
        PaidOrder order = paidOrder(tokenFor(UserRole.SELLER));

        send(post("/api/v1/admin/orders/" + order.id() + "/cancel"), adminToken())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void supervisorReadsEverything() throws Exception {
        String supervisor = tokenFor(UserRole.SUPERVISOR);
        PaidOrder order = paidOrder(tokenFor(UserRole.SELLER));

        send(get("/api/v1/supervisor/users"), supervisor)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].role", hasItem("ADMINISTRATOR")));
        send(get("/api/v1/supervisor/orders/" + order.id()), supervisor)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"));
        send(get("/api/v1/supervisor/orders/" + order.id() + "/invoice"), supervisor)
            .andExpect(status().isOk());
        for (String route : new String[] {"products", "warehouses", "inventory", "inventory/low-stock",
            "orders", "invoices", "shipments", "returns", "refunds"}) {
            send(get("/api/v1/supervisor/" + route), supervisor).andExpect(status().isOk());
        }
    }

    @Test
    void theSupervisorCannotWrite() throws Exception {
        String supervisor = tokenFor(UserRole.SUPERVISOR);

        send(post("/api/v1/admin/warehouses"), supervisor, "{}").andExpect(status().isForbidden());
        send(delete("/api/v1/supervisor/users"), supervisor).andExpect(status().isMethodNotAllowed());
    }

    private record PaidOrder(String buyer, String id, StockedProduct product) {
    }
}
