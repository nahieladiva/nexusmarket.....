package application.support;

import application.domain.models.Invoice;
import application.domain.ports.out.InvoiceRepository;
import application.domain.valueobjects.InvoiceId;
import application.domain.valueobjects.OrderId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory invoice repository for tests.
 */
public class InMemoryInvoiceRepository implements InvoiceRepository {

    private final Map<InvoiceId, Invoice> invoices = new LinkedHashMap<>();

    @Override
    public Invoice save(Invoice invoice) {
        invoices.put(invoice.getId(), invoice);
        return invoice;
    }

    @Override
    public Optional<Invoice> findById(InvoiceId id) {
        return Optional.ofNullable(invoices.get(id));
    }

    @Override
    public Optional<Invoice> findByOrderId(OrderId orderId) {
        return invoices.values().stream()
            .filter(invoice -> invoice.getOrderId().equals(orderId))
            .findFirst();
    }

    @Override
    public List<Invoice> findAll() {
        return new ArrayList<>(invoices.values());
    }
}
