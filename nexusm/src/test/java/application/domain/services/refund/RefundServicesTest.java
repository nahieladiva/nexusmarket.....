package application.domain.services.refund;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.RefundStatus;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.models.Buyer;
import application.domain.models.LogisticsOperator;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Seller;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.Quantity;
import application.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the refund use cases.
 */
class RefundServicesTest {

    private TestFixture f;
    private Buyer buyer;
    private User admin;
    private LogisticsOperator operator;
    private Warehouse warehouse;
    private Product product;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        buyer = f.buyer("buyer@mail.com");
        admin = f.admin();
        operator = f.logisticsOperator("operator@mail.com");
        Seller seller = f.approvedSeller("seller@mail.com");
        warehouse = f.warehouse("Main");
        product = f.product(seller, "TEC-0001", "1000");
        f.stock(seller, product, warehouse, 10, 1);
    }

    private Order placeOrder() {
        f.addItemToCart.execute(buyer, product.getId(), Quantity.of(2));
        return f.placeOrder.execute(buyer);
    }

    private Refund refundOfCancelledOrder() {
        Order order = f.payOrder.execute(buyer, placeOrder().getId());
        f.cancelOrder.execute(buyer, order.getId());
        return f.consultRefund.findByOrder(buyer, order.getId()).get(0);
    }

    @Test
    void cancellingAPaidOrderCreatesARefundOfItsTotal() {
        Refund refund = refundOfCancelledOrder();
        assertEquals(RefundStatus.PENDING, refund.getStatus());
        assertEquals(Money.of("2000", "COP"), refund.getAmount());
        assertTrue(refund.getReturnId().isEmpty());
    }

    @Test
    void cancellingAnUnpaidOrderCreatesNoRefund() {
        Order order = placeOrder();
        f.cancelOrder.execute(buyer, order.getId());
        assertTrue(f.consultRefund.findByOrder(buyer, order.getId()).isEmpty());
    }

    @Test
    void receivedReturnCreatesARefundOfTheReturnedUnits() {
        Order paid = f.payOrder.execute(buyer, placeOrder().getId());
        Shipment shipment = f.createShipment.execute(operator, paid.getId(), warehouse.getId());
        f.packShipment.execute(operator, shipment.getId());
        f.dispatchShipment.execute(operator, shipment.getId(), "TRACK-1");
        f.confirmDelivery.execute(operator, shipment.getId());
        ReturnRequest returnRequest = f.requestReturn.execute(buyer, paid.getId(), product.getId(),
            Quantity.of(1), "Damaged");
        f.approveReturn.execute(admin, returnRequest.getId());
        f.receiveReturn.execute(operator, returnRequest.getId());

        Refund refund = f.consultRefund.findByOrder(buyer, paid.getId()).get(0);
        assertEquals(Money.of("1000", "COP"), refund.getAmount());
        assertEquals(returnRequest.getId(), refund.getReturnId().orElseThrow());
    }

    @Test
    void administratorProcessesARefundOnlyOnce() {
        Refund refund = refundOfCancelledOrder();
        assertEquals(RefundStatus.PROCESSED, f.processRefund.execute(admin, refund.getId()).getStatus());
        assertThrows(BusinessRuleException.class, () -> f.rejectRefund.execute(admin, refund.getId()));
    }

    @Test
    void administratorCanRejectARefund() {
        Refund refund = refundOfCancelledOrder();
        assertEquals(RefundStatus.REJECTED, f.rejectRefund.execute(admin, refund.getId()).getStatus());
    }

    @Test
    void refundsAreLimitedByRole() {
        Refund refund = refundOfCancelledOrder();
        assertThrows(ForbiddenActionException.class, () -> f.processRefund.execute(buyer, refund.getId()));
        assertThrows(ForbiddenActionException.class, () -> f.consultRefund.findAll(buyer));
        assertEquals(1, f.consultRefund.findAll(admin).size());
    }
}
