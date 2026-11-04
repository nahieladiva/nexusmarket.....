package application.domain.models;

import application.domain.enums.OrderStatus;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.OrderStateTransitionException;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Purchase order (aggregate root).
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>It has at least one item, and each product appears only once.</li>
 *   <li>Transitions: PENDING_PAYMENT -> PAID -> SHIPPED -> DELIVERED; it can be cancelled
 *       only from PENDING_PAYMENT or PAID (see {@link OrderStatus}).</li>
 *   <li>To be paid, every item must have an assigned warehouse.</li>
 *   <li>The total can be lower than the sum of the subtotals (discounts), never higher.</li>
 * </ul>
 */
public final class Order {

    private final OrderId id;
    private final UserId buyerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private Money total;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order(OrderId id, UserId buyerId, List<OrderItem> items, OrderStatus status,
                 Money total, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Order id is required");
        this.buyerId = Objects.requireNonNull(buyerId, "Buyer is required");
        this.items = new ArrayList<>(items == null ? List.of() : items);
        if (this.items.isEmpty()) {
            throw new IllegalArgumentException("An order must have at least one item");
        }
        this.status = status == null ? OrderStatus.PENDING_PAYMENT : status;
        Money subtotal = calculateSubtotal();
        if (total != null && total.isGreaterThan(subtotal)) {
            throw new IllegalArgumentException(
                "The order total cannot be greater than the sum of its items");
        }
        this.total = total != null ? total : subtotal;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /** Creates an order pending payment whose total is the sum of its items. */
    public static Order create(UserId buyerId, List<OrderItem> items) {
        return create(OrderId.random(), buyerId, items, null, LocalDateTime.now());
    }

    /**
     * Creates an order pending payment.
     *
     * @param total total already calculated (with discounts); {@code null} means the sum of the items
     */
    public static Order create(OrderId id, UserId buyerId, List<OrderItem> items, Money total,
                               LocalDateTime now) {
        validateItems(items);
        return new Order(id, buyerId, items, OrderStatus.PENDING_PAYMENT, total, now, now);
    }

    private static void validateItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order must have at least one item");
        }
        Set<ProductId> seen = new HashSet<>();
        for (OrderItem item : items) {
            if (!seen.add(item.getProductId())) {
                throw new BusinessRuleException(
                    "Product " + item.getProductId() + " is repeated in the order");
            }
        }
    }

    public void addItem(OrderItem item) {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new OrderStateTransitionException(id, status, status);
        }
        List<OrderItem> candidate = new ArrayList<>(items);
        candidate.add(Objects.requireNonNull(item, "Item is required"));
        validateItems(candidate);
        this.items.add(item);
        this.total = calculateSubtotal();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Assigns to each item the warehouse it ships from. Only while the order is pending payment.
     */
    public void assignWarehouses(Map<ProductId, WarehouseId> allocation) {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("Warehouses can only be assigned to orders pending payment");
        }
        Objects.requireNonNull(allocation, "Warehouse allocation is required");
        List<OrderItem> assigned = new ArrayList<>();
        for (OrderItem item : items) {
            WarehouseId warehouse = allocation.get(item.getProductId());
            if (warehouse == null) {
                throw new BusinessRuleException(
                    "No warehouse is assigned to product " + item.getProductId());
            }
            assigned.add(item.assignedTo(warehouse));
        }
        this.items.clear();
        this.items.addAll(assigned);
    }

    /**
     * Registers the payment of the order.
     *
     * @throws BusinessRuleException when some item has no assigned warehouse
     */
    public void pay() {
        if (!this.status.canTransitionTo(OrderStatus.PAID)) {
            throw new OrderStateTransitionException(id, this.status, OrderStatus.PAID);
        }
        if (items.stream().anyMatch(item -> !item.hasWarehouse())) {
            throw new BusinessRuleException(
                "Order " + id + " cannot be paid until every item has an assigned warehouse");
        }
        transitionTo(OrderStatus.PAID);
    }

    public void ship() {
        transitionTo(OrderStatus.SHIPPED);
    }

    public void deliver() {
        transitionTo(OrderStatus.DELIVERED);
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    /** Tells whether the order keeps stock reserved (paid and not shipped yet). */
    public boolean hasReservedStock() {
        return status.keepsStockReserved();
    }

    public boolean belongsTo(UserId userId) {
        return buyerId.equals(userId);
    }

    public boolean containsAnyProduct(Set<ProductId> productIds) {
        return items.stream().anyMatch(item -> productIds.contains(item.getProductId()));
    }

    /** Total units per product. */
    public Map<ProductId, Integer> unitsByProduct() {
        Map<ProductId, Integer> result = new HashMap<>();
        items.forEach(item -> result.merge(item.getProductId(),
            item.getQuantity().getValue(), Integer::sum));
        return result;
    }

    private void transitionTo(OrderStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new OrderStateTransitionException(id, this.status, target);
        }
        this.status = target;
        this.updatedAt = LocalDateTime.now();
    }

    private Money calculateSubtotal() {
        Money result = null;
        for (OrderItem item : items) {
            result = (result == null) ? item.subtotal() : result.add(item.subtotal());
        }
        return result;
    }

    /** Sum of the item subtotals, before order discounts. */
    public Money getSubtotal() {
        return calculateSubtotal();
    }

    public Money getDiscount() {
        return calculateSubtotal().subtract(total);
    }

    public OrderId getId() {
        return id;
    }

    public UserId getBuyerId() {
        return buyerId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Money getTotal() {
        return total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
