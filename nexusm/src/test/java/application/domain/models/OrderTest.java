package application.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import application.domain.enums.OrderStatus;
import application.domain.enums.ProductType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.OrderStateTransitionException;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;

import org.junit.jupiter.api.Test;

/**
 * Unit tests of the {@link Order} entity.
 */
class OrderTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 15, 10, 0);

    private OrderItem item(String price) {
        return new OrderItem(ProductId.random(), ProductType.PHYSICAL, Quantity.of(1), Money.of(price, "USD"));
    }

    private void payWithWarehouse(Order order) {
        order.assignWarehouses(Map.of(order.getItems().get(0).getProductId(), WarehouseId.random()));
        order.pay(NOW);
    }

    @Test
    void cannotPayWithoutWarehouse() {
        Order order = Order.create(UserId.random(), List.of(item("1.00")));
        assertThrows(BusinessRuleException.class, () -> order.pay(NOW));
    }

    @Test
    void rejectsRepeatedProducts() {
        OrderItem first = new OrderItem(ProductId.random(), ProductType.PHYSICAL, Quantity.of(1), Money.of("1.00", "USD"));
        OrderItem repeated = new OrderItem(first.getProductId(), ProductType.PHYSICAL, Quantity.of(2), Money.of("1.00", "USD"));
        assertThrows(BusinessRuleException.class,
            () -> Order.create(UserId.random(), List.of(first, repeated)));
    }

    @Test
    void totalIsSumOfSubtotals() {
        OrderItem item1 = new OrderItem(ProductId.random(), ProductType.PHYSICAL, Quantity.of(2), Money.of("10.00", "USD"));
        OrderItem item2 = new OrderItem(ProductId.random(), ProductType.PHYSICAL, Quantity.of(1), Money.of("5.00", "USD"));
        Order order = Order.create(UserId.random(), List.of(item1, item2));
        assertEquals("25.00", order.getTotal().getAmount().toPlainString());
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
    }

    @Test
    void orderRequiresAtLeastOneItem() {
        assertThrows(IllegalArgumentException.class,
            () -> Order.create(UserId.random(), List.of()));
    }

    @Test
    void followsValidLifecycle() {
        Order order = Order.create(UserId.random(), List.of(item("1.00")));
        payWithWarehouse(order);
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertTrue(order.hasReservedStock());
        order.ship();
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        order.deliver();
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void rejectsInvalidTransition() {
        Order order = Order.create(UserId.random(), List.of(item("1.00")));
        assertThrows(OrderStateTransitionException.class, order::ship);
        assertThrows(OrderStateTransitionException.class, order::deliver);
    }

    @Test
    void doesNotAllowAddingItemsAfterPayment() {
        Order order = Order.create(UserId.random(), List.of(item("1.00")));
        payWithWarehouse(order);
        assertThrows(OrderStateTransitionException.class,
            () -> order.addItem(item("2.00")));
    }

    @Test
    void shippedOrderCannotBeCancelled() {
        Order order = Order.create(UserId.random(), List.of(item("1.00")));
        payWithWarehouse(order);
        order.ship();
        assertThrows(OrderStateTransitionException.class, order::cancel);
    }

    @Test
    void cancelledOrderIsTerminal() {
        Order order = Order.create(UserId.random(), List.of(item("1.00")));
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertThrows(OrderStateTransitionException.class, () -> order.pay(NOW));
    }
}
