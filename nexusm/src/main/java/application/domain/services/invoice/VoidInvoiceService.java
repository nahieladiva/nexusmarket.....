package application.domain.services.invoice;

import application.domain.models.Order;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.InvoiceRepository;
import application.domain.services.DomainService;

import java.util.Objects;

/**
 * Voids the invoice of a cancelled order. An order cancelled before payment has no invoice.
 */
@DomainService
public class VoidInvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClockPort clock;

    public VoidInvoiceService(InvoiceRepository invoiceRepository, ClockPort clock) {
        this.invoiceRepository = Objects.requireNonNull(invoiceRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    public void execute(Order order) {
        invoiceRepository.findByOrderId(order.getId()).ifPresent(invoice -> {
            invoice.voidInvoice(clock.now());
            invoiceRepository.save(invoice);
        });
    }
}
