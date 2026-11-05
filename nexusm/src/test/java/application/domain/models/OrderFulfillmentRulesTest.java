package application.domain.models;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.OrderStatus;
import application.domain.enums.ProductType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.RefundId;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Rules that connect orders with shipments, returns and refunds.
 */
class OrderFulfillmentRulesTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 15, 10, 0);
    private static final Address ADDRESS = Address.of("Street 1", "City", "State", "050001", "CO");
    private static final UserId OPERATOR = UserId.random();

    private final WarehouseId firstWarehouse = WarehouseId.random();
    private final WarehouseId secondWarehouse = WarehouseId.random();

    private OrderItem physical(int units) {
        return new OrderItem(ProductId.random(), ProductType.PHYSICAL, Quantity.of(units),
            Money.of("10.00", "USD"));
    }

    private OrderItem digital() {
        return new OrderItem(ProductId.random(), ProductType.DIGITAL, Quantity.of(1),
            Money.of("5.00", "USD"));
    }

    private Order paidOrderFromTwoWarehouses() {
        OrderItem first = physical(1);
        OrderItem second = physical(1);
        Order order = Order.create(UserId.random(), List.of(first, second));
        order.assignWarehouses(Map.of(first.getProductId(), firstWarehouse,
            second.getProductId(), secondWarehouse));
        order.pay(NOW);
        return order;
    }

    private Order deliveredOrder(OrderItem item) {
        Order order = Order.create(UserId.random(), List.of(item));
        if (!item.requiresShipment()) {
            order.pay(NOW);
            return order;
        }
        order.assignWarehouses(Map.of(item.getProductId(), firstWarehouse));
        order.pay(NOW);
        Shipment shipment = shipment(order, firstWarehouse);
        dispatch(shipment);
        shipment.confirmDelivery(NOW);
        order.applyShipmentProgress(List.of(shipment));
        return order;
    }

    private Shipment shipment(Order order, WarehouseId warehouse) {
        return Shipment.prepareFor(order, ShipmentId.random(), warehouse, ADDRESS, NOW);
    }

    private void dispatch(Shipment shipment) {
        shipment.pack();
        shipment.dispatch(OPERATOR, "TRACK-1", NOW);
    }

    @Test
    void payingStoresThePaymentDate() {
        Order order = paidOrderFromTwoWarehouses();
        assertTrue(order.wasPaid());
        assertEquals(NOW, order.getPaidAt().orElseThrow());
    }

    @Test
    void cancelledOrderRemembersItWasPaid() {
        Order order = paidOrderFromTwoWarehouses();
        order.cancel();
        assertTrue(order.wasPaid());
        assertEquals(order.getTotal(),
            Refund.forCancelledOrder(order, RefundId.random(), NOW).getAmount());
    }

    @Test
    void neverPaidOrderCannotBeRefunded() {
        Order order = Order.create(UserId.random(), List.of(physical(1)));
        order.cancel();
        assertFalse(order.wasPaid());
        assertThrows(BusinessRuleException.class,
            () -> Refund.forCancelledOrder(order, RefundId.random(), NOW));
    }

    @Test
    void digitalOnlyOrderIsDeliveredWhenPaid() {
        Order order = Order.create(UserId.random(), List.of(digital()));
        order.pay(NOW);
        assertFalse(order.requiresShipment());
        assertTrue(order.wasPaid());
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void digitalOnlyOrderHasNoShipmentProgress() {
        Order order = Order.create(UserId.random(), List.of(digital()));
        order.pay(NOW);
        assertThrows(BusinessRuleException.class, () -> order.applyShipmentProgress(List.of()));
    }

    @Test
    void orderWithPhysicalItemsStaysPaidAfterPayment() {
        assertEquals(OrderStatus.PAID, paidOrderFromTwoWarehouses().getStatus());
    }

    @Test
    void mixedOrderOnlyNeedsWarehouseForPhysicalItems() {
        OrderItem physical = physical(1);
        Order order = Order.create(UserId.random(), List.of(physical, digital()));
        order.assignWarehouses(Map.of(physical.getProductId(), firstWarehouse));
        assertDoesNotThrow(() -> order.pay(NOW));
        assertEquals(1, order.shippingWarehouses().size());
    }

    @Test
    void shipmentMustComeFromAWarehouseOfTheOrder() {
        Order order = paidOrderFromTwoWarehouses();
        assertThrows(BusinessRuleException.class, () -> shipment(order, WarehouseId.random()));
    }

    @Test
    void orderIsShippedOnlyWhenEveryWarehouseHasDispatched() {
        Order order = paidOrderFromTwoWarehouses();
        Shipment first = shipment(order, firstWarehouse);
        dispatch(first);
        order.applyShipmentProgress(List.of(first));
        assertEquals(OrderStatus.PAID, order.getStatus());

        Shipment second = shipment(order, secondWarehouse);
        order.applyShipmentProgress(List.of(first, second));
        assertEquals(OrderStatus.PAID, order.getStatus());

        dispatch(second);
        order.applyShipmentProgress(List.of(first, second));
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    void shipmentDeliveredEarlyDoesNotBlockTheOrder() {
        Order order = paidOrderFromTwoWarehouses();
        Shipment first = shipment(order, firstWarehouse);
        Shipment second = shipment(order, secondWarehouse);
        dispatch(first);
        first.confirmDelivery(NOW);
        dispatch(second);
        order.applyShipmentProgress(List.of(first, second));
        assertEquals(OrderStatus.SHIPPED, order.getStatus());

        second.confirmDelivery(NOW);
        order.applyShipmentProgress(List.of(first, second));
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void shipmentOfAnotherOrderIsRejected() {
        Order order = paidOrderFromTwoWarehouses();
        Shipment foreign = shipment(paidOrderFromTwoWarehouses(), firstWarehouse);
        assertThrows(BusinessRuleException.class, () -> order.applyShipmentProgress(List.of(foreign)));
    }

    @Test
    void returnsAddUpAgainstTheQuantityBought() {
        OrderItem item = physical(3);
        Order order = deliveredOrder(item);
        ReturnRequest allowed = ReturnRequest.request(order, ReturnId.random(), item.getProductId(),
            Quantity.of(1), Quantity.of(2), "Damaged", NOW);
        assertEquals("10.00", allowed.getRefundAmount().getAmount().toPlainString());
        assertThrows(BusinessRuleException.class,
            () -> ReturnRequest.request(order, ReturnId.random(), item.getProductId(),
                Quantity.of(2), Quantity.of(2), "Damaged", NOW));
    }

    @Test
    void digitalProductsCannotBeReturned() {
        OrderItem item = digital();
        Order order = deliveredOrder(item);
        assertThrows(BusinessRuleException.class,
            () -> ReturnRequest.request(order, ReturnId.random(), item.getProductId(),
                Quantity.of(1), Quantity.zero(), "Changed my mind", NOW));
    }
}
