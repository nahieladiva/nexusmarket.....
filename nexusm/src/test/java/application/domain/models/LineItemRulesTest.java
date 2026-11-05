package application.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.ProductType;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import org.junit.jupiter.api.Test;

/**
 * Rules of the lines of a cart and of an order.
 */
class LineItemRulesTest {

    private static final ProductId PRODUCT = ProductId.random();
    private static final Money PRICE = Money.of("1000.00", "COP");

    private static OrderItem physical(int quantity) {
        return new OrderItem(PRODUCT, ProductType.PHYSICAL, Quantity.of(quantity), PRICE);
    }

    private static CartItem cartItem(int quantity) {
        return new CartItem(PRODUCT, ProductType.PHYSICAL, Quantity.of(quantity), PRICE);
    }

    @Test
    void anOrderItemNeedsAPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () -> physical(0));
    }

    @Test
    void anOrderItemNeedsItsData() {
        Quantity one = Quantity.of(1);
        assertThrows(NullPointerException.class,
            () -> new OrderItem(null, ProductType.PHYSICAL, one, PRICE));
        assertThrows(NullPointerException.class, () -> new OrderItem(PRODUCT, null, one, PRICE));
        assertThrows(NullPointerException.class,
            () -> new OrderItem(PRODUCT, ProductType.PHYSICAL, null, PRICE));
        assertThrows(NullPointerException.class,
            () -> new OrderItem(PRODUCT, ProductType.PHYSICAL, one, null));
    }

    @Test
    void theSubtotalIsThePriceTimesTheQuantity() {
        assertEquals(Money.of("3000.00", "COP"), physical(3).subtotal());
        assertEquals(Money.of("3000.00", "COP"), cartItem(3).subtotal());
    }

    @Test
    void aPhysicalItemShipsAndCanBeReturned() {
        OrderItem item = physical(1);

        assertTrue(item.requiresShipment());
        assertTrue(item.isReturnable());
    }

    @Test
    void aDigitalItemNeitherShipsNorCanBeReturned() {
        OrderItem item = new OrderItem(PRODUCT, ProductType.DIGITAL, Quantity.of(1), PRICE);

        assertFalse(item.requiresShipment());
        assertFalse(item.isReturnable());
    }

    @Test
    void anItemReceivesItsWarehouse() {
        WarehouseId warehouse = WarehouseId.random();
        OrderItem item = physical(1);

        OrderItem assigned = item.assignedTo(warehouse);

        assertFalse(item.hasWarehouse());
        assertNull(item.getWarehouseId());
        assertTrue(assigned.hasWarehouse());
        assertEquals(warehouse, assigned.getWarehouseId());
    }

    @Test
    void orderItemsCompareByValue() {
        assertEquals(physical(2), physical(2));
        assertEquals(physical(2).hashCode(), physical(2).hashCode());
        assertNotEquals(physical(3), physical(2));
        assertNotEquals(null, physical(2));
        assertFalse(physical(2).toString().isBlank());
    }

    @Test
    void aCartItemNeedsAPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () -> cartItem(0));
    }

    @Test
    void aCartItemNeedsItsData() {
        Quantity one = Quantity.of(1);
        assertThrows(NullPointerException.class,
            () -> new CartItem(null, ProductType.PHYSICAL, one, PRICE));
        assertThrows(NullPointerException.class, () -> new CartItem(PRODUCT, null, one, PRICE));
        assertThrows(NullPointerException.class,
            () -> new CartItem(PRODUCT, ProductType.PHYSICAL, null, PRICE));
        assertThrows(NullPointerException.class,
            () -> new CartItem(PRODUCT, ProductType.PHYSICAL, one, null));
    }

    @Test
    void aCartItemKeepsItsDataWhenTheQuantityChanges() {
        CartItem changed = cartItem(1).withQuantity(Quantity.of(4));

        assertEquals(Quantity.of(4), changed.getQuantity());
        assertEquals(PRODUCT, changed.getProductId());
        assertEquals(PRICE, changed.getUnitPrice());
    }

    @Test
    void aCartItemBecomesAnOrderItem() {
        OrderItem item = cartItem(2).toOrderItem();

        assertEquals(PRODUCT, item.getProductId());
        assertEquals(ProductType.PHYSICAL, item.getProductType());
        assertEquals(Quantity.of(2), item.getQuantity());
        assertEquals(PRICE, item.getUnitPrice());
        assertFalse(item.hasWarehouse());
    }

    @Test
    void cartItemsCompareByValue() {
        assertEquals(cartItem(2), cartItem(2));
        assertEquals(cartItem(2).hashCode(), cartItem(2).hashCode());
        assertNotEquals(null, cartItem(2));
    }
}
