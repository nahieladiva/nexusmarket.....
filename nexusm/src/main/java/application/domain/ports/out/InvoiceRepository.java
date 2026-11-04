package application.domain.ports.out;

import application.domain.models.Invoice;
import application.domain.valueobjects.InvoiceId;
import application.domain.valueobjects.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * Output port — invoices (MySQL). An order has at most one invoice.
 */
public interface InvoiceRepository {

    Invoice save(Invoice invoice);

    Optional<Invoice> findById(InvoiceId id);

    Optional<Invoice> findByOrderId(OrderId orderId);

    List<Invoice> findAll();
}
