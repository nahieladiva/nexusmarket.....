package application.domain.services.returns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.enums.ReturnStatus;
import application.domain.enums.StockMovementType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.models.Buyer;
import application.domain.models.LogisticsOperator;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ReturnRequest;
import application.domain.models.Seller;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Quantity;
import application.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the return use cases.
 */
class ReturnServicesTest {

    private TestFixture f;
    private Buyer buyer;
    private Seller seller;
    private User admin;
    private LogisticsOperator operator;
    private Warehouse warehouse;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        buyer = f.buyer("buyer@mail.com");
        admin = f.admin();
        operator = f.logisticsOperator("operator@mail.com");
        seller = f.approvedSeller("seller@mail.com");
        warehouse = f.warehouse("Main");
        product = f.product(seller, "TEC-0001", "1000");
        f.stock(seller, product, warehouse, 10, 1);
        f.addItemToCart.execute(buyer, product.getId(), Quantity.of(3));
        order = deliver(f.payOrder.execute(buyer, f.placeOrder.execute(buyer).getId()));
    }

    private Order deliver(Order paid) {
        Shipment shipment = f.createShipment.execute(operator, paid.getId(), warehouse.getId());
        f.packShipment.execute(operator, shipment.getId());
        f.dispatchShipment.execute(operator, shipment.getId(), "TRACK-1");
        f.confirmDelivery.execute(operator, shipment.getId());
        return f.orders.findById(paid.getId()).orElseThrow();
    }

    private ReturnRequest request(int units) {
        return f.requestReturn.execute(buyer, order.getId(), product.getId(), Quantity.of(units), "Damaged");
    }

    private ReturnRequest receive(ReturnRequest returnRequest) {
        f.approveReturn.execute(admin, returnRequest.getId());
        return f.receiveReturn.execute(operator, returnRequest.getId());
    }

    private int onHand() {
        return f.inventories.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
            .orElseThrow().getOnHand().getValue();
    }

    @Test
    void receivedReturnGoesBackToTheWarehouse() {
        assertEquals(7, onHand());
        ReturnRequest received = receive(request(2));
        assertEquals(ReturnStatus.RECEIVED, received.getStatus());
        assertEquals(9, onHand());
        assertEquals(2, f.movements.findByProductAndWarehouse(product.getId(), warehouse.getId()).stream()
            .filter(movement -> movement.type() == StockMovementType.RETURN)
            .findFirst().orElseThrow().delta());
    }

    @Test
    void anOrderHasOnlyOneOpenReturn() {
        request(1);
        assertThrows(BusinessRuleException.class, () -> request(1));
    }

    @Test
    void previousReturnsCountAgainstWhatWasBought() {
        receive(request(2));
        assertThrows(BusinessRuleException.class, () -> request(2));
        assertEquals(ReturnStatus.REQUESTED, request(1).getStatus());
    }

    @Test
    void rejectedReturnsDoNotCount() {
        f.rejectReturn.execute(admin, request(3).getId());
        assertEquals(ReturnStatus.REQUESTED, request(3).getStatus());
    }

    @Test
    void digitalProductCannotBeReturned() {
        Product digital = f.digitalProduct(seller, "DIG-0001", "500");
        f.addItemToCart.execute(buyer, digital.getId(), Quantity.of(1));
        Order digitalOrder = f.payOrder.execute(buyer, f.placeOrder.execute(buyer).getId());
        assertThrows(BusinessRuleException.class, () -> f.requestReturn.execute(buyer, digitalOrder.getId(),
            digital.getId(), Quantity.of(1), "Changed my mind"));
    }

    @Test
    void returnsAreLimitedByRole() {
        Buyer other = f.buyer("other@mail.com");
        assertThrows(ForbiddenActionException.class, () -> f.requestReturn.execute(other, order.getId(),
            product.getId(), Quantity.of(1), "Damaged"));
        ReturnRequest returnRequest = request(1);
        assertThrows(ForbiddenActionException.class,
            () -> f.approveReturn.execute(buyer, returnRequest.getId()));
        assertThrows(ForbiddenActionException.class,
            () -> f.receiveReturn.execute(admin, returnRequest.getId()));
        assertThrows(ForbiddenActionException.class, () -> f.consultReturn.findByOrder(other, order.getId()));
        assertEquals(1, f.consultReturn.findByOrder(buyer, order.getId()).size());
    }

    @Test
    void returnMustBeApprovedBeforeBeingReceived() {
        ReturnRequest returnRequest = request(1);
        assertThrows(BusinessRuleException.class,
            () -> f.receiveReturn.execute(operator, returnRequest.getId()));
    }
}
