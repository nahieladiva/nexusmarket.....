package application.domain.services.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.OrderStatus;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Quantity;
import application.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A payment is rejected when a product stopped being available after the order was placed.
 */
class PayOrderAvailabilityTest {

    private TestFixture f;
    private User admin;
    private Buyer buyer;
    private Seller seller;
    private Warehouse warehouse;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        admin = f.admin();
        buyer = f.buyer("buyer@mail.com");
        seller = f.approvedSeller("seller@mail.com");
        warehouse = f.warehouse("Main");
        product = f.product(seller, "TEC-0001", "1000");
        f.stock(seller, product, warehouse, 10, 1);
        f.addItemToCart.execute(buyer, product.getId(), Quantity.of(2));
        order = f.placeOrder.execute(buyer);
    }

    private void assertPaymentIsRejected() {
        assertThrows(BusinessRuleException.class, () -> f.payOrder.execute(buyer, order.getId()));
        Order stored = f.orders.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.PENDING_PAYMENT, stored.getStatus());
        assertFalse(stored.wasPaid());
        assertEquals(10, f.inventories.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
            .orElseThrow().getOnHand().getValue());
        assertThrows(ResourceNotFoundException.class,
            () -> f.consultInvoice.findByOrder(buyer, order.getId()));
    }

    @Test
    void blockedProductRejectsThePayment() {
        f.blockProduct.execute(admin, product.getId());
        assertPaymentIsRejected();
    }

    @Test
    void discontinuedProductRejectsThePayment() {
        f.discontinueProduct.execute(seller, product.getId());
        assertPaymentIsRejected();
    }

    @Test
    void suspendedSellerRejectsThePayment() {
        f.suspendSeller.execute(admin, seller.getId());
        assertPaymentIsRejected();
    }

    @Test
    void orderCanBePaidOnceTheProductIsAvailableAgain() {
        f.suspendSeller.execute(admin, seller.getId());
        assertPaymentIsRejected();
        f.approveSeller.execute(admin, seller.getId());
        assertTrue(f.payOrder.execute(buyer, order.getId()).wasPaid());
    }
}
