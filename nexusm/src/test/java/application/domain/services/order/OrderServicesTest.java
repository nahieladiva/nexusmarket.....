package application.domain.services.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.OrderStatus;
import application.domain.enums.StockMovementType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.InsufficientStockException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.StockMovement;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Quantity;
import application.support.TestFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the order use cases.
 */
class OrderServicesTest {

    private TestFixture f;
    private Buyer buyer;
    private Seller seller;
    private Warehouse warehouse;
    private Product product;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        buyer = f.buyer("buyer@mail.com");
        seller = f.approvedSeller("seller@mail.com");
        warehouse = f.warehouse("Main");
        product = f.product(seller, "TEC-0001", "1000");
        f.stock(seller, product, warehouse, 10, 1);
    }

    private Order placeOrder(int units) {
        f.addItemToCart.execute(buyer, product.getId(), Quantity.of(units));
        return f.placeOrder.execute(buyer);
    }

    private int onHand() {
        return f.inventories.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
            .orElseThrow().getOnHand().getValue();
    }

    private StockMovement lastMovement(StockMovementType type) {
        return f.movements.findByProductAndWarehouse(product.getId(), warehouse.getId()).stream()
            .filter(movement -> movement.type() == type)
            .findFirst().orElseThrow();
    }

    @Test
    void placingAnOrderEmptiesTheCartWithoutReservingStock() {
        Order order = placeOrder(2);
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
        assertTrue(f.consultCart.execute(buyer).isEmpty());
        assertEquals(10, onHand());
    }

    @Test
    void cartIsKeptWhenThereIsNoStock() {
        f.addItemToCart.execute(buyer, product.getId(), Quantity.of(50));
        assertThrows(InsufficientStockException.class, () -> f.placeOrder.execute(buyer));
        assertFalse(f.consultCart.execute(buyer).isEmpty());
    }

    @Test
    void emptyCartCannotBeOrdered() {
        assertThrows(BusinessRuleException.class, () -> f.placeOrder.execute(buyer));
    }

    @Test
    void payingReservesStockAndRecordsTheMovement() {
        Order paid = f.payOrder.execute(buyer, placeOrder(2).getId());
        assertEquals(OrderStatus.PAID, paid.getStatus());
        assertTrue(paid.wasPaid());
        assertEquals(8, onHand());
        assertEquals(-2, lastMovement(StockMovementType.RESERVATION).delta());
    }

    @Test
    void onlyTheOwnerCanPay() {
        Order order = placeOrder(1);
        Buyer other = f.buyer("other@mail.com");
        assertThrows(ForbiddenActionException.class, () -> f.payOrder.execute(other, order.getId()));
    }

    @Test
    void cancellingAPaidOrderReturnsTheStock() {
        Order order = placeOrder(3);
        f.payOrder.execute(buyer, order.getId());
        Order cancelled = f.cancelOrder.execute(buyer, order.getId());
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        assertEquals(10, onHand());
        assertEquals(3, lastMovement(StockMovementType.RELEASE).delta());
    }

    @Test
    void anotherBuyerCannotCancel() {
        Order order = placeOrder(1);
        Buyer other = f.buyer("other@mail.com");
        assertThrows(ForbiddenActionException.class, () -> f.cancelOrder.execute(other, order.getId()));
        assertEquals(OrderStatus.CANCELLED, f.cancelOrder.execute(f.admin(), order.getId()).getStatus());
    }

    @Test
    void digitalOnlyOrderIsDeliveredWhenPaid() {
        Product digital = f.digitalProduct(seller, "DIG-0001", "500");
        f.addItemToCart.execute(buyer, digital.getId(), Quantity.of(1));
        Order order = f.placeOrder.execute(buyer);
        Order paid = f.payOrder.execute(buyer, order.getId());
        assertEquals(OrderStatus.DELIVERED, paid.getStatus());
        assertTrue(paid.wasPaid());
    }

    @Test
    void mixedOrderPairsMovementsWithPhysicalItems() {
        Product digital = f.digitalProduct(seller, "DIG-0001", "500");
        f.addItemToCart.execute(buyer, digital.getId(), Quantity.of(1));
        Order order = placeOrder(3);
        Order paid = f.payOrder.execute(buyer, order.getId());
        assertEquals(OrderStatus.PAID, paid.getStatus());
        assertEquals(-3, lastMovement(StockMovementType.RESERVATION).delta());
        f.cancelOrder.execute(buyer, order.getId());
        assertEquals(3, lastMovement(StockMovementType.RELEASE).delta());
        assertEquals(10, onHand());
    }

    @Test
    void queriesAreLimitedByRole() {
        Order order = placeOrder(1);
        Buyer other = f.buyer("other@mail.com");
        Seller otherSeller = f.approvedSeller("other-seller@mail.com");
        assertThrows(ForbiddenActionException.class, () -> f.consultOrder.execute(other, order.getId()));
        assertThrows(ForbiddenActionException.class,
            () -> f.consultOrder.execute(otherSeller, order.getId()));
        assertEquals(order.getId(), f.consultOrder.execute(seller, order.getId()).getId());
        assertEquals(List.of(order.getId()),
            f.consultOrder.findBySeller(seller).stream().map(Order::getId).toList());
        assertThrows(ForbiddenActionException.class, () -> f.consultOrder.findAll(buyer));
    }
}
