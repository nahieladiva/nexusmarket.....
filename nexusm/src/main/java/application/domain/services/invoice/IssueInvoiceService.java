package application.domain.services.invoice;

import application.domain.exceptions.DuplicateResourceException;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.InvoiceRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.InvoiceId;

import java.util.Objects;

/**
 * Issues the invoice of a paid order. An order has only one invoice, and its number is built from
 * the order id.
 */
@DomainService
public class IssueInvoiceService {

    private static final String NUMBER_PREFIX = "INV-";

    private final InvoiceRepository invoiceRepository;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public IssueInvoiceService(InvoiceRepository invoiceRepository, ClockPort clock,
                               IdGeneratorPort idGenerator) {
        this.invoiceRepository = Objects.requireNonNull(invoiceRepository);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public Invoice execute(Order order) {
        if (invoiceRepository.findByOrderId(order.getId()).isPresent()) {
            throw new DuplicateResourceException("Order " + order.getId() + " already has an invoice");
        }
        return invoiceRepository.save(Invoice.issueFor(order, InvoiceId.of(idGenerator.newId()),
            NUMBER_PREFIX + order.getId(), clock.now()));
    }
}
