package application.adapters.useCases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.InvoiceStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.RefundStatus;
import application.domain.enums.ReturnStatus;
import application.domain.enums.StockMovementType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Seller;
import application.domain.models.Shipment;
import application.domain.models.StockMovement;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.support.TestFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Business scenarios that cross several aggregates, run through the role ports.
 */
class MarketplaceScenariosTest {

    private TestFixture f;
    private UserId admin;
    private UserId operator;
    private UserId buyer;
    private Seller seller;
    private Warehouse firstWarehouse;
    private Warehouse secondWarehouse;
    private Product firstProduct;
    private Product secondProduct;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        admin = f.admin().getId();
        operator = f.logisticsOperator("operator@mail.com").getId();
        buyer = f.buyer("buyer@mail.com").getId();
        seller = f.approvedSeller("seller@mail.com");
        firstWarehouse = f.warehouse("First");
        secondWarehouse = f.warehouse("Second");
        firstProduct = f.product(seller, "TEC-0001", "1000");
        secondProduct = f.product(seller, "TEC-0002", "2000");
        f.stock(seller, firstProduct, firstWarehouse, 10, 0);
        f.stock(seller, secondProduct, secondWarehouse, 10, 0);
    }

    private void addToCart(Product product, int units) {
        f.buyerPort.addCartItem(buyer, product.getId(), Quantity.of(units));
    }

    private OrderId pay() {
        return f.buyerPort.payOrder(buyer, f.buyerPort.placeOrder(buyer).getId()).getId();
    }

    private OrderId deliveredOrder(Product product, int units) {
        addToCart(product, units);
        OrderId order = pay();
        WarehouseId warehouse = product.equals(firstProduct) ? firstWarehouse.getId() : secondWarehouse.getId();
        Shipment shipment = dispatch(order, warehouse);
        f.logisticsOperatorPort.confirmDelivery(operator, shipment.getId());
        return order;
    }

    private Shipment dispatch(OrderId order, WarehouseId warehouse) {
        Shipment shipment = f.logisticsOperatorPort.createShipment(operator, order, warehouse);
        f.logisticsOperatorPort.packShipment(operator, shipment.getId());
        return f.logisticsOperatorPort.dispatchShipment(operator, shipment.getId(), "TRACK-1");
    }

    private int onHand(Product product, Warehouse warehouse) {
        return f.inventories.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
            .orElseThrow().getOnHand().getValue();
    }

    private List<Integer> deltas(Product product, Warehouse warehouse, StockMovementType type) {
        return f.movements.findByProductAndWarehouse(product.getId(), warehouse.getId()).stream()
            .filter(movement -> movement.type() == type)
            .map(StockMovement::delta)
            .toList();
    }

    private OrderStatus status(OrderId order) {
        return f.buyerPort.findOrder(buyer, order).getStatus();
    }

    private ReturnRequest requestReturn(OrderId order, Product product, int units) {
        return f.buyerPort.requestReturn(buyer, order, product.getId(), Quantity.of(units), "Damaged");
    }

    private void receive(ReturnRequest returnRequest) {
        f.administratorPort.approveReturn(admin, returnRequest.getId());
        f.logisticsOperatorPort.receiveReturn(operator, returnRequest.getId());
    }

    // Mixed order

    @Test
    void mixedOrderTakesStockFromTheRightLineWhenPaid() {
        Product digital = f.digitalProduct(seller, "DIG-0001", "500");
        addToCart(firstProduct, 2);
        addToCart(digital, 1);
        addToCart(secondProduct, 3);

        OrderId order = pay();

        assertEquals(OrderStatus.PAID, status(order));
        assertEquals(8, onHand(firstProduct, firstWarehouse));
        assertEquals(7, onHand(secondProduct, secondWarehouse));
        assertEquals(List.of(-2), deltas(firstProduct, firstWarehouse, StockMovementType.RESERVATION));
        assertEquals(List.of(-3), deltas(secondProduct, secondWarehouse, StockMovementType.RESERVATION));
        OrderItem digitalItem = f.buyerPort.findOrder(buyer, order).getItems().stream()
            .filter(item -> item.getProductId().equals(digital.getId()))
            .findFirst().orElseThrow();
        assertNull(digitalItem.getWarehouseId());
    }

    @Test
    void mixedOrderReturnsTheStockOfEachLineWhenCancelled() {
        Product digital = f.digitalProduct(seller, "DIG-0001", "500");
        addToCart(digital, 1);
        addToCart(firstProduct, 2);
        addToCart(secondProduct, 3);
        OrderId order = pay();

        f.buyerPort.cancelOrder(buyer, order);

        assertEquals(10, onHand(firstProduct, firstWarehouse));
        assertEquals(10, onHand(secondProduct, secondWarehouse));
        assertEquals(List.of(2), deltas(firstProduct, firstWarehouse, StockMovementType.RELEASE));
        assertEquals(List.of(3), deltas(secondProduct, secondWarehouse, StockMovementType.RELEASE));
    }

    // Cancellation with shipments

    @Test
    void paidOrderWithShipmentsCannotBeCancelled() {
        addToCart(firstProduct, 1);
        OrderId order = pay();
        f.logisticsOperatorPort.createShipment(operator, order, firstWarehouse.getId());

        assertThrows(BusinessRuleException.class, () -> f.buyerPort.cancelOrder(buyer, order));
        assertThrows(BusinessRuleException.class, () -> f.administratorPort.cancelOrder(admin, order));

        assertEquals(OrderStatus.PAID, status(order));
        assertEquals(9, onHand(firstProduct, firstWarehouse));
        assertEquals(InvoiceStatus.ISSUED, f.buyerPort.findInvoice(buyer, order).getStatus());
        assertTrue(f.buyerPort.findRefunds(buyer, order).isEmpty());
    }

    // Cumulative returns

    @Test
    void returnsInARowCannotExceedWhatWasBought() {
        OrderId order = deliveredOrder(firstProduct, 3);

        receive(requestReturn(order, firstProduct, 2));
        assertThrows(BusinessRuleException.class, () -> requestReturn(order, firstProduct, 2));

        receive(requestReturn(order, firstProduct, 1));
        assertThrows(BusinessRuleException.class, () -> requestReturn(order, firstProduct, 1));
        assertEquals(10, onHand(firstProduct, firstWarehouse));
    }

    @Test
    void secondReturnIsRejectedWhileTheFirstIsOpen() {
        OrderId order = deliveredOrder(firstProduct, 3);
        requestReturn(order, firstProduct, 1);
        assertThrows(BusinessRuleException.class, () -> requestReturn(order, firstProduct, 1));
    }

    @Test
    void rejectedReturnsDoNotCountAgainstWhatWasBought() {
        OrderId order = deliveredOrder(firstProduct, 3);
        ReturnRequest rejected = requestReturn(order, firstProduct, 3);
        f.administratorPort.rejectReturn(admin, rejected.getId());

        ReturnRequest second = requestReturn(order, firstProduct, 3);

        assertEquals(ReturnStatus.REQUESTED, second.getStatus());
        assertEquals(2, f.buyerPort.findReturns(buyer, order).size());
    }

    // Two warehouses

    @Test
    void orderFromTwoWarehousesAdvancesOnlyWhenBothShipmentsDo() {
        addToCart(firstProduct, 1);
        addToCart(secondProduct, 1);
        OrderId order = pay();

        Shipment first = dispatch(order, firstWarehouse.getId());
        assertEquals(OrderStatus.PAID, status(order));

        Shipment second = dispatch(order, secondWarehouse.getId());
        assertEquals(OrderStatus.SHIPPED, status(order));

        f.logisticsOperatorPort.confirmDelivery(operator, first.getId());
        assertEquals(OrderStatus.SHIPPED, status(order));

        f.logisticsOperatorPort.confirmDelivery(operator, second.getId());
        assertEquals(OrderStatus.DELIVERED, status(order));
    }

    // RG-03

    @Test
    void sellerCannotPerformBuyerOperations() {
        UserId sellerId = seller.getId();
        addToCart(firstProduct, 1);
        OrderId order = pay();
        assertThrows(ForbiddenActionException.class,
            () -> f.buyerPort.addCartItem(sellerId, firstProduct.getId(), Quantity.of(1)));
        assertThrows(ForbiddenActionException.class, () -> f.buyerPort.findCart(sellerId));
        assertThrows(ForbiddenActionException.class, () -> f.buyerPort.placeOrder(sellerId));
        assertThrows(ForbiddenActionException.class, () -> f.buyerPort.payOrder(sellerId, order));
        assertThrows(ForbiddenActionException.class, () -> f.buyerPort.cancelOrder(sellerId, order));
        assertThrows(ForbiddenActionException.class, () -> f.buyerPort.findOrders(sellerId));
        assertThrows(ForbiddenActionException.class, () -> f.buyerPort.requestReturn(sellerId, order,
            firstProduct.getId(), Quantity.of(1), "Damaged"));
    }

    @Test
    void buyerCannotSeeTheOrderOrInvoiceOfAnotherBuyer() {
        addToCart(firstProduct, 1);
        OrderId order = pay();
        UserId other = f.buyer("other@mail.com").getId();

        assertThrows(ForbiddenActionException.class, () -> f.buyerPort.findOrder(other, order));
        assertThrows(ForbiddenActionException.class, () -> f.buyerPort.findInvoice(other, order));
        assertThrows(ForbiddenActionException.class, () -> f.buyerPort.findShipments(other, order));
        assertThrows(ForbiddenActionException.class, () -> f.buyerPort.findReturns(other, order));
        assertThrows(ForbiddenActionException.class, () -> f.buyerPort.findRefunds(other, order));
        assertTrue(f.buyerPort.findOrders(other).isEmpty());
    }

    // Refund on cancellation

    @Test
    void cancellingAPaidOrderRefundsItsTotalAndVoidsTheInvoice() {
        addToCart(firstProduct, 2);
        OrderId order = pay();

        f.buyerPort.cancelOrder(buyer, order);

        List<Refund> refunds = f.buyerPort.findRefunds(buyer, order);
        assertEquals(1, refunds.size());
        assertEquals(RefundStatus.PENDING, refunds.get(0).getStatus());
        assertEquals(Money.of("2000", "COP"), refunds.get(0).getAmount());
        assertEquals(InvoiceStatus.VOIDED, f.buyerPort.findInvoice(buyer, order).getStatus());
    }

    @Test
    void cancellingAnUnpaidOrderCreatesNoRefundAndNoInvoice() {
        addToCart(firstProduct, 2);
        OrderId order = f.buyerPort.placeOrder(buyer).getId();

        f.buyerPort.cancelOrder(buyer, order);

        assertEquals(OrderStatus.CANCELLED, status(order));
        assertTrue(f.buyerPort.findRefunds(buyer, order).isEmpty());
        assertThrows(ResourceNotFoundException.class, () -> f.buyerPort.findInvoice(buyer, order));
    }
}
