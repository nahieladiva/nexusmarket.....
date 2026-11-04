package application.domain.models;

import application.domain.enums.ReturnStatus;
import application.domain.exceptions.BusinessRuleException;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Return requested by a buyer for a product of a delivered order.
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>It can only be requested for a {@code DELIVERED} order.</li>
 *   <li>The product must belong to the order, and the quantity cannot exceed what was bought.</li>
 *   <li>The refund amount is the unit price paid times the returned quantity.</li>
 *   <li>The original order is not modified; the return is an independent entity.</li>
 * </ul>
 */
public final class ReturnRequest {

    private final ReturnId id;
    private final OrderId orderId;
    private final UserId buyerId;
    private final ProductId productId;
    private final Quantity quantity;
    private final Money refundAmount;
    private final String reason;
    private ReturnStatus status;
    private final LocalDateTime requestedAt;
    private LocalDateTime resolvedAt;

    public ReturnRequest(ReturnId id, OrderId orderId, UserId buyerId, ProductId productId,
                         Quantity quantity, Money refundAmount, String reason, ReturnStatus status,
                         LocalDateTime requestedAt, LocalDateTime resolvedAt) {
        this.id = Objects.requireNonNull(id, "Return id is required");
        this.orderId = Objects.requireNonNull(orderId, "Order is required");
        this.buyerId = Objects.requireNonNull(buyerId, "Buyer is required");
        this.productId = Objects.requireNonNull(productId, "Product is required");
        this.quantity = Objects.requireNonNull(quantity, "Quantity is required");
        if (quantity.isZero()) {
            throw new IllegalArgumentException("The quantity to return must be greater than zero");
        }
        this.refundAmount = Objects.requireNonNull(refundAmount, "Refund amount is required");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("The return reason is required");
        }
        this.reason = reason.trim();
        this.status = Objects.requireNonNull(status, "Return status is required");
        this.requestedAt = Objects.requireNonNull(requestedAt, "Request date is required");
        this.resolvedAt = resolvedAt;
    }

    /**
     * Creates a return for a product of a delivered order.
     *
     * @throws BusinessRuleException when the order is not delivered, the product is not in the
     *                               order or the quantity exceeds what was bought
     */
    public static ReturnRequest request(Order order, ReturnId id, ProductId productId, Quantity quantity,
                                        String reason, LocalDateTime now) {
        Objects.requireNonNull(order, "Order is required");
        if (!order.getStatus().allowsReturns()) {
            throw new BusinessRuleException("Only products of delivered orders can be returned");
        }
        OrderItem purchased = order.getItems().stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleException(
                "Product " + productId + " does not belong to order " + order.getId()));
        if (quantity.compareTo(purchased.getQuantity()) > 0) {
            throw new BusinessRuleException("Cannot return more units than were bought");
        }
        Money amount = purchased.getUnitPrice().multiply(quantity.getValue());
        return new ReturnRequest(id, order.getId(), order.getBuyerId(), productId, quantity, amount,
            reason, ReturnStatus.REQUESTED, now, null);
    }

    public void approve(LocalDateTime now) {
        changeStatus(ReturnStatus.APPROVED);
        this.resolvedAt = now;
    }

    public void reject(LocalDateTime now) {
        changeStatus(ReturnStatus.REJECTED);
        this.resolvedAt = now;
    }

    /**
     * The warehouse physically receives the returned product; this entitles the refund.
     */
    public void markAsReceived() {
        changeStatus(ReturnStatus.RECEIVED);
    }

    private void changeStatus(ReturnStatus target) {
        status.requireCanTransitionTo(target);
        this.status = target;
    }

    public boolean belongsTo(UserId userId) {
        return buyerId.equals(userId);
    }

    public ReturnId getId() {
        return id;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public UserId getBuyerId() {
        return buyerId;
    }

    public ProductId getProductId() {
        return productId;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public Money getRefundAmount() {
        return refundAmount;
    }

    public String getReason() {
        return reason;
    }

    public ReturnStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public Optional<LocalDateTime> getResolvedAt() {
        return Optional.ofNullable(resolvedAt);
    }
}
