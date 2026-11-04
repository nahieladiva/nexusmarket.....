package application.domain.models;

import application.domain.enums.OrderStatus;
import application.domain.enums.RefundStatus;
import application.domain.exceptions.BusinessRuleException;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.RefundId;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Money returned to a buyer, processed by an administrator.
 *
 * <p>It is created in two cases:</p>
 * <ul>
 *   <li>A return was received in the warehouse: the amount is the return's refund amount.</li>
 *   <li>A paid order was cancelled before shipping: the amount is the order total.</li>
 * </ul>
 * <p>The amount is always greater than zero.</p>
 */
public final class Refund {

    private final RefundId id;
    private final OrderId orderId;
    private final UserId buyerId;
    private final ReturnId returnId;
    private final Money amount;
    private RefundStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public Refund(RefundId id, OrderId orderId, UserId buyerId, ReturnId returnId, Money amount,
                  RefundStatus status, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        this.id = Objects.requireNonNull(id, "Refund id is required");
        this.orderId = Objects.requireNonNull(orderId, "Order is required");
        this.buyerId = Objects.requireNonNull(buyerId, "Buyer is required");
        this.returnId = returnId;
        this.amount = Objects.requireNonNull(amount, "Amount is required");
        if (amount.isZero()) {
            throw new IllegalArgumentException("The refund amount must be greater than zero");
        }
        this.status = Objects.requireNonNull(status, "Refund status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "Creation date is required");
        this.resolvedAt = resolvedAt;
    }

    /**
     * Creates the refund of a return received in the warehouse.
     *
     * @throws BusinessRuleException when the return has not been received
     */
    public static Refund forReturn(ReturnRequest returnRequest, RefundId id, LocalDateTime now) {
        Objects.requireNonNull(returnRequest, "Return is required");
        if (!returnRequest.getStatus().entitlesRefund()) {
            throw new BusinessRuleException("Only a return received in the warehouse can be refunded");
        }
        return new Refund(id, returnRequest.getOrderId(), returnRequest.getBuyerId(),
            returnRequest.getId(), returnRequest.getRefundAmount(), RefundStatus.PENDING, now, null);
    }

    /**
     * Creates the refund of a paid order that was cancelled before shipping.
     *
     * @throws BusinessRuleException when the order is not cancelled or was never paid
     */
    public static Refund forCancelledOrder(Order order, RefundId id, LocalDateTime now) {
        Objects.requireNonNull(order, "Order is required");
        if (order.getStatus() != OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Only a cancelled order can be refunded");
        }
        if (!order.wasPaid()) {
            throw new BusinessRuleException("An order that was never paid cannot be refunded");
        }
        return new Refund(id, order.getId(), order.getBuyerId(), null, order.getTotal(),
            RefundStatus.PENDING, now, null);
    }

    public void process(LocalDateTime now) {
        changeStatus(RefundStatus.PROCESSED);
        this.resolvedAt = now;
    }

    public void reject(LocalDateTime now) {
        changeStatus(RefundStatus.REJECTED);
        this.resolvedAt = now;
    }

    private void changeStatus(RefundStatus target) {
        status.requireCanTransitionTo(target);
        this.status = target;
    }

    public RefundId getId() {
        return id;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public UserId getBuyerId() {
        return buyerId;
    }

    public Optional<ReturnId> getReturnId() {
        return Optional.ofNullable(returnId);
    }

    public Money getAmount() {
        return amount;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Optional<LocalDateTime> getResolvedAt() {
        return Optional.ofNullable(resolvedAt);
    }
}
