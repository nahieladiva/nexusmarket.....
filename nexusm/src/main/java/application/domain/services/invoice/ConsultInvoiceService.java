package application.domain.services.invoice;

import application.domain.enums.UserRole;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Invoice;
import application.domain.models.User;
import application.domain.ports.out.InvoiceRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.OrderId;

import java.util.List;
import java.util.Objects;

/**
 * Invoice queries: a buyer sees their invoices, and administrators and supervisors see all of them.
 */
@DomainService
public class ConsultInvoiceService {

    private final InvoiceRepository invoiceRepository;

    public ConsultInvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = Objects.requireNonNull(invoiceRepository);
    }

    public Invoice findByOrder(User actor, OrderId orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", orderId.toString()));
        boolean allowed = actor.hasRole(UserRole.ADMINISTRATOR) || actor.hasRole(UserRole.SUPERVISOR)
            || invoice.belongsTo(actor.getId());
        if (!allowed) {
            throw new ForbiddenActionException("You cannot see the invoice of order " + orderId);
        }
        return invoice;
    }

    public List<Invoice> findAll(User actor) {
        actor.requireAnyRole(UserRole.ADMINISTRATOR, UserRole.SUPERVISOR);
        return invoiceRepository.findAll();
    }
}
