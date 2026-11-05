package application.domain.services.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.InvoiceStatus;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.valueobjects.Quantity;
import application.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the invoice use cases.
 */
class InvoiceServicesTest {

    private TestFixture f;
    private Buyer buyer;
    private Seller seller;
    private Product product;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        buyer = f.buyer("buyer@mail.com");
        seller = f.approvedSeller("seller@mail.com");
        product = f.product(seller, "TEC-0001", "1000");
        f.stock(seller, product, f.warehouse("Main"), 10, 1);
    }

    private Order placeOrder() {
        f.addItemToCart.execute(buyer, product.getId(), Quantity.of(2));
        return f.placeOrder.execute(buyer);
    }

    @Test
    void payingIssuesTheInvoiceWithTheOrderTotal() {
        Order order = f.payOrder.execute(buyer, placeOrder().getId());
        Invoice invoice = f.consultInvoice.findByOrder(buyer, order.getId());
        assertEquals(InvoiceStatus.ISSUED, invoice.getStatus());
        assertEquals(order.getTotal(), invoice.getTotal());
        assertEquals("INV-" + order.getId(), invoice.getInvoiceNumber());
    }

    @Test
    void digitalOrderIsInvoicedEvenThoughItIsDelivered() {
        Product digital = f.digitalProduct(seller, "DIG-0001", "500");
        f.addItemToCart.execute(buyer, digital.getId(), Quantity.of(1));
        Order order = f.payOrder.execute(buyer, f.placeOrder.execute(buyer).getId());
        assertEquals(InvoiceStatus.ISSUED, f.consultInvoice.findByOrder(buyer, order.getId()).getStatus());
    }

    @Test
    void anOrderHasOnlyOneInvoice() {
        Order order = f.payOrder.execute(buyer, placeOrder().getId());
        assertThrows(DuplicateResourceException.class, () -> f.issueInvoice.execute(order));
    }

    @Test
    void cancellingAPaidOrderVoidsTheInvoice() {
        Order order = f.payOrder.execute(buyer, placeOrder().getId());
        f.cancelOrder.execute(buyer, order.getId());
        assertEquals(InvoiceStatus.VOIDED, f.consultInvoice.findByOrder(buyer, order.getId()).getStatus());
    }

    @Test
    void cancellingAnUnpaidOrderHasNoInvoice() {
        Order order = placeOrder();
        f.cancelOrder.execute(buyer, order.getId());
        assertThrows(ResourceNotFoundException.class,
            () -> f.consultInvoice.findByOrder(buyer, order.getId()));
    }

    @Test
    void invoicesAreLimitedByRole() {
        Order order = f.payOrder.execute(buyer, placeOrder().getId());
        Buyer other = f.buyer("other@mail.com");
        assertThrows(ForbiddenActionException.class,
            () -> f.consultInvoice.findByOrder(other, order.getId()));
        assertThrows(ForbiddenActionException.class, () -> f.consultInvoice.findAll(buyer));
        assertTrue(f.consultInvoice.findAll(f.admin()).size() == 1);
    }
}
