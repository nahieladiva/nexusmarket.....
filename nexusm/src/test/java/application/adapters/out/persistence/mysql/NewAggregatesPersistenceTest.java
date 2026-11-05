package application.adapters.out.persistence.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.adapters.out.persistence.mysql.adapters.CartPersistenceAdapter;
import application.adapters.out.persistence.mysql.adapters.InvoicePersistenceAdapter;
import application.adapters.out.persistence.mysql.adapters.RefundPersistenceAdapter;
import application.adapters.out.persistence.mysql.adapters.ReturnRequestPersistenceAdapter;
import application.adapters.out.persistence.mysql.adapters.ShipmentPersistenceAdapter;
import application.adapters.out.persistence.mysql.mappers.CartEntityMapper;
import application.adapters.out.persistence.mysql.mappers.InvoiceEntityMapper;
import application.adapters.out.persistence.mysql.mappers.RefundEntityMapper;
import application.adapters.out.persistence.mysql.mappers.ReturnRequestEntityMapper;
import application.adapters.out.persistence.mysql.mappers.ShipmentEntityMapper;
import application.domain.enums.InvoiceStatus;
import application.domain.enums.ProductType;
import application.domain.enums.RefundStatus;
import application.domain.enums.ReturnStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.models.Cart;
import application.domain.models.CartItem;
import application.domain.models.Invoice;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.valueobjects.CartId;
import application.domain.valueobjects.InvoiceId;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.RefundId;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.support.H2WithoutChecksDialect;
import application.support.TestFixture;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Saves and reads the cart, invoice, shipment, return and refund through their adapters, on an in-memory database.
 * Each read happens after clearing the session, so it comes from the tables.
 */
@DataJpaTest(properties = H2WithoutChecksDialect.PROPERTY)
@Import({CartPersistenceAdapter.class, CartEntityMapper.class,
    InvoicePersistenceAdapter.class, InvoiceEntityMapper.class,
    ShipmentPersistenceAdapter.class, ShipmentEntityMapper.class,
    ReturnRequestPersistenceAdapter.class, ReturnRequestEntityMapper.class,
    RefundPersistenceAdapter.class, RefundEntityMapper.class})
class NewAggregatesPersistenceTest {

    private static final LocalDateTime NOW = TestFixture.NOW;
    private static final Money PRICE = Money.of("150000.50", "COP");

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CartPersistenceAdapter carts;

    @Autowired
    private InvoicePersistenceAdapter invoices;

    @Autowired
    private ShipmentPersistenceAdapter shipments;

    @Autowired
    private ReturnRequestPersistenceAdapter returns;

    @Autowired
    private RefundPersistenceAdapter refunds;

    @Test
    void cartKeepsItsItemsInOrder() {
        UserId buyer = userId();
        ProductId first = productId();
        ProductId second = productId();
        carts.save(cart(buyer, item(first, 2), item(second, 1)));
        flushAndClear();

        Cart stored = carts.findByBuyerId(buyer).orElseThrow();

        assertEquals(List.of(first, second),
            stored.getItems().stream().map(CartItem::getProductId).toList());
        CartItem item = stored.getItems().get(0);
        assertEquals(ProductType.PHYSICAL, item.getProductType());
        assertEquals(Quantity.of(2), item.getQuantity());
        assertEquals(PRICE, item.getUnitPrice());
    }

    @Test
    void savingACartReplacesItsItems() {
        UserId buyer = userId();
        ProductId kept = productId();
        Cart cart = cart(buyer, item(kept, 1), item(productId(), 1));
        carts.save(cart);
        flushAndClear();

        Long version = carts.findByBuyerId(buyer).orElseThrow().getVersion();
        carts.save(new Cart(cart.getId(), buyer, List.of(item(kept, 5)), NOW.plusHours(1), version));
        flushAndClear();

        Cart stored = carts.findByBuyerId(buyer).orElseThrow();
        assertEquals(1, stored.getItems().size());
        assertEquals(Quantity.of(5), stored.getItems().get(0).getQuantity());
        assertEquals(NOW.plusHours(1), stored.getUpdatedAt());
    }

    @Test
    void savingAnEmptyCartRemovesItsItems() {
        UserId buyer = userId();
        Cart cart = cart(buyer, item(productId(), 1));
        carts.save(cart);
        flushAndClear();

        Long version = carts.findByBuyerId(buyer).orElseThrow().getVersion();
        carts.save(new Cart(cart.getId(), buyer, List.of(), NOW, version));
        flushAndClear();

        assertTrue(carts.findByBuyerId(buyer).orElseThrow().getItems().isEmpty());
    }

    @Test
    void aBuyerHasASingleCart() {
        UserId buyer = userId();
        carts.save(cart(buyer));
        flushAndClear();

        carts.save(cart(buyer));

        assertThrows(PersistenceException.class, this::flushAndClear);
    }

    @Test
    void invoiceIsFoundByIdAndByOrder() {
        OrderId order = orderId();
        InvoiceId id = InvoiceId.of(uuid());
        invoices.save(new Invoice(id, "INV-" + order, order, userId(), PRICE,
            InvoiceStatus.VOIDED, NOW, NOW.plusDays(1)));
        flushAndClear();

        Invoice stored = invoices.findByOrderId(order).orElseThrow();

        assertEquals(id, stored.getId());
        assertEquals("INV-" + order, stored.getInvoiceNumber());
        assertEquals(PRICE, stored.getTotal());
        assertEquals(InvoiceStatus.VOIDED, stored.getStatus());
        assertEquals(NOW.plusDays(1), stored.getVoidedAt());
        assertTrue(invoices.findById(id).isPresent());
        assertTrue(invoices.findAll().stream().anyMatch(invoice -> invoice.getId().equals(id)));
    }

    @Test
    void anOrderHasASingleInvoice() {
        OrderId order = orderId();
        invoices.save(invoice(order, "INV-1"));
        flushAndClear();

        invoices.save(invoice(order, "INV-2"));

        assertThrows(PersistenceException.class, this::flushAndClear);
    }

    @Test
    void shipmentKeepsItsAddressAndOptionalData() {
        OrderId order = orderId();
        UserId operator = userId();
        ShipmentId id = ShipmentId.of(uuid());
        shipments.save(new Shipment(id, order, warehouseId(), TestFixture.address(), operator,
            "TRACK-1", ShipmentStatus.DELIVERED, NOW, NOW.plusDays(1), NOW.plusDays(2)));
        flushAndClear();

        Shipment stored = shipments.findById(id).orElseThrow();

        assertEquals(TestFixture.address(), stored.getShippingAddress());
        assertEquals(operator, stored.getLogisticsOperatorId().orElseThrow());
        assertEquals("TRACK-1", stored.getTrackingNumber().orElseThrow());
        assertEquals(ShipmentStatus.DELIVERED, stored.getStatus());
        assertEquals(NOW.plusDays(1), stored.getDispatchedAt().orElseThrow());
        assertEquals(NOW.plusDays(2), stored.getDeliveredAt().orElseThrow());
    }

    @Test
    void shipmentWithoutOptionalDataStaysEmpty() {
        OrderId order = orderId();
        shipments.save(preparing(order, warehouseId()));
        flushAndClear();

        Shipment stored = shipments.findByOrderId(order).get(0);

        assertFalse(stored.getLogisticsOperatorId().isPresent());
        assertFalse(stored.getTrackingNumber().isPresent());
        assertFalse(stored.getDispatchedAt().isPresent());
        assertFalse(stored.getDeliveredAt().isPresent());
    }

    @Test
    void anOrderHasOneShipmentPerWarehouse() {
        OrderId order = orderId();
        WarehouseId warehouse = warehouseId();
        shipments.save(preparing(order, warehouse));
        shipments.save(preparing(order, warehouseId()));
        flushAndClear();
        assertEquals(2, shipments.findByOrderId(order).size());
        assertEquals(2, shipments.findAll().stream()
            .filter(shipment -> shipment.getOrderId().equals(order)).count());

        shipments.save(preparing(order, warehouse));

        assertThrows(PersistenceException.class, this::flushAndClear);
    }

    @Test
    void returnRequestIsFoundByOrder() {
        OrderId order = orderId();
        ProductId product = productId();
        ReturnId id = ReturnId.of(uuid());
        String longReason = "The product arrived damaged. ".repeat(40).trim();
        returns.save(new ReturnRequest(id, order, userId(), product, Quantity.of(2), PRICE,
            longReason, ReturnStatus.RECEIVED, NOW, NOW.plusDays(3)));
        flushAndClear();

        ReturnRequest stored = returns.findByOrderId(order).get(0);

        assertEquals(id, stored.getId());
        assertEquals(product, stored.getProductId());
        assertEquals(Quantity.of(2), stored.getQuantity());
        assertEquals(PRICE, stored.getRefundAmount());
        assertEquals(longReason, stored.getReason());
        assertEquals(ReturnStatus.RECEIVED, stored.getStatus());
        assertEquals(NOW.plusDays(3), stored.getResolvedAt().orElseThrow());
        assertTrue(returns.findById(id).isPresent());
        assertTrue(returns.findAll().stream().anyMatch(request -> request.getId().equals(id)));
    }

    @Test
    void refundOfAReturnKeepsTheReturn() {
        OrderId order = orderId();
        ReturnId returnId = ReturnId.of(uuid());
        RefundId id = RefundId.of(uuid());
        refunds.save(new Refund(id, order, userId(), returnId, PRICE, RefundStatus.PROCESSED, NOW,
            NOW.plusDays(1)));
        flushAndClear();

        Refund stored = refunds.findById(id).orElseThrow();

        assertEquals(returnId, stored.getReturnId().orElseThrow());
        assertEquals(PRICE, stored.getAmount());
        assertEquals(RefundStatus.PROCESSED, stored.getStatus());
        assertEquals(NOW.plusDays(1), stored.getResolvedAt().orElseThrow());
    }

    @Test
    void refundOfACancelledOrderHasNoReturn() {
        OrderId order = orderId();
        refunds.save(new Refund(RefundId.of(uuid()), order, userId(), null, PRICE,
            RefundStatus.PENDING, NOW, null));
        flushAndClear();

        Refund stored = refunds.findByOrderId(order).get(0);

        assertFalse(stored.getReturnId().isPresent());
        assertFalse(stored.getResolvedAt().isPresent());
        assertTrue(refunds.findAll().stream().anyMatch(refund -> refund.getOrderId().equals(order)));
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private static Cart cart(UserId buyer, CartItem... items) {
        return new Cart(CartId.of(uuid()), buyer, List.of(items), NOW);
    }

    private static CartItem item(ProductId product, int quantity) {
        return new CartItem(product, ProductType.PHYSICAL, Quantity.of(quantity), PRICE);
    }

    private static Invoice invoice(OrderId order, String number) {
        return new Invoice(InvoiceId.of(uuid()), number, order, userId(), PRICE, InvoiceStatus.ISSUED,
            NOW, null);
    }

    private static Shipment preparing(OrderId order, WarehouseId warehouse) {
        return new Shipment(ShipmentId.of(uuid()), order, warehouse, TestFixture.address(), null, null,
            ShipmentStatus.PREPARING, NOW, null, null);
    }

    private static UserId userId() {
        return UserId.of(uuid());
    }

    private static OrderId orderId() {
        return OrderId.of(uuid());
    }

    private static ProductId productId() {
        return ProductId.of(uuid());
    }

    private static WarehouseId warehouseId() {
        return WarehouseId.of(uuid());
    }

    private static String uuid() {
        return UUID.randomUUID().toString();
    }
}
