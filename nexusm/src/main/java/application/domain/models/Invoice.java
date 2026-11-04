package application.domain.models;

import application.domain.enums.InvoiceStatus;
import application.domain.enums.OrderStatus;
import application.domain.exceptions.BusinessRuleException;
import application.domain.valueobjects.InvoiceId;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Invoice of an order.
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>It is issued only for a {@code PAID} order, and an order has one invoice.</li>
 *   <li>The invoice total is the order total.</li>
 *   <li>It is voided when the order is cancelled after payment; a voided invoice never changes.</li>
 * </ul>
 */
public final class Invoice {

    private final InvoiceId id;
    private final String invoiceNumber;
    private final OrderId orderId;
    private final UserId buyerId;
    private final Money total;
    private InvoiceStatus status;
    private final LocalDateTime issuedAt;
    private LocalDateTime voidedAt;

    public Invoice(InvoiceId id, String invoiceNumber, OrderId orderId, UserId buyerId,
                   Money total, InvoiceStatus status, LocalDateTime issuedAt,
                   LocalDateTime voidedAt) {
        this.id = Objects.requireNonNull(id, "Invoice id is required");
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new IllegalArgumentException("Invoice number is required");
        }
        this.invoiceNumber = invoiceNumber.trim();
        this.orderId = Objects.requireNonNull(orderId, "Order is required");
        this.buyerId = Objects.requireNonNull(buyerId, "Buyer is required");
        this.total = Objects.requireNonNull(total, "Total is required");
        this.status = Objects.requireNonNull(status, "Invoice status is required");
        this.issuedAt = Objects.requireNonNull(issuedAt, "Issue date is required");
        this.voidedAt = voidedAt;
    }

    /**
     * Issues the invoice of a paid order.
     *
     * @throws BusinessRuleException when the order is not paid
     */
    public static Invoice issueFor(Order order, InvoiceId id, String invoiceNumber, LocalDateTime now) {
        Objects.requireNonNull(order, "Order is required");
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessRuleException(
                "Only a paid order can be invoiced (current status: " + order.getStatus() + ")");
        }
        return new Invoice(id, invoiceNumber, order.getId(), order.getBuyerId(),
            order.getTotal(), InvoiceStatus.ISSUED, now, null);
    }

    /**
     * Voids the invoice because the order was cancelled after payment.
     */
    public void voidInvoice(LocalDateTime now) {
        status.requireCanVoid();
        this.status = InvoiceStatus.VOIDED;
        this.voidedAt = now;
    }

    public boolean belongsTo(UserId userId) {
        return buyerId.equals(userId);
    }

    public InvoiceId getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public UserId getBuyerId() {
        return buyerId;
    }

    public Money getTotal() {
        return total;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getVoidedAt() {
        return voidedAt;
    }
}
