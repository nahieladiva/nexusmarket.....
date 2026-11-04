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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Purchase order (aggregate root).
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>It has at least one item, and each product appears only once.</li>
 *   <li>Transitions: PENDING_PAYMENT -> PAID -> SHIPPED -> DELIVERED; it can be cancelled
 *       only from PENDING_PAYMENT or PAID (see {@link OrderStatus}).</li>
 *   <li>An order with no items that require shipment goes directly from PAID to DELIVERED.</li>
 *   <li>To be paid, every item that requires shipment must have an assigned warehouse.</li>
 *   <li>Each warehouse of the order has one shipment. The order is SHIPPED when every shipment
 *       has left its warehouse, and DELIVERED when every shipment is delivered.</li>
 *   <li>The total is always the sum of the item subtotals.</li>
 * </ul>
 */
public final class Order {

    private final OrderId id;
    private final UserId buyerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private Money total;
    private LocalDateTime paidAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order(OrderId id, UserId buyerId, List<OrderItem> items, OrderStatus status,
                 LocalDateTime paidAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Order id is required");
        this.buyerId = Objects.requireNonNull(buyerId, "Buyer is required");
        this.items = new ArrayList<>(items == null ? List.of() : items);
        if (this.items.isEmpty()) {
            throw new IllegalArgumentException("An order must have at least one item");
        }
        this.status = status == null ? OrderStatus.PENDING_PAYMENT : status;
        this.total = calculateTotal();
        this.paidAt = paidAt;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /** Creates an order pending payment with a new id. */
    public static Order create(UserId buyerId, List<OrderItem> items) {
        return create(OrderId.random(), buyerId, items, LocalDateTime.now());
    }

    /** Creates an order pending payment. */
    public static Order create(OrderId id, UserId buyerId, List<OrderItem> items, LocalDateTime now) {
        validateItems(items);
        return new Order(id, buyerId, items, OrderStatus.PENDING_PAYMENT, null, now, now);
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
        this.total = calculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Assigns to each item that requires shipment the warehouse it ships from.
     * Only while the order is pending payment.
     */
    public void assignWarehouses(Map<ProductId, WarehouseId> allocation) {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("Warehouses can only be assigned to orders pending payment");
        }
        Objects.requireNonNull(allocation, "Warehouse allocation is required");
        List<OrderItem> assigned = new ArrayList<>();
        for (OrderItem item : items) {
            if (!item.requiresShipment()) {
                assigned.add(item);
                continue;
            }
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
     * @throws BusinessRuleException when some item that requires shipment has no assigned warehouse
     */
    public void pay(LocalDateTime now) {
        Objects.requireNonNull(now, "Payment date is required");
        if (!this.status.canTransitionTo(OrderStatus.PAID)) {
            throw new OrderStateTransitionException(id, this.status, OrderStatus.PAID);
        }
        if (items.stream().anyMatch(item -> item.requiresShipment() && !item.hasWarehouse())) {
            throw new BusinessRuleException(
                "Order " + id + " cannot be paid until every item has an assigned warehouse");
        }
        transitionTo(OrderStatus.PAID);
        this.paidAt = now;
    }

    /**
     * Moves the order to {@code SHIPPED}.
     *
     * @throws BusinessRuleException when no item requires shipment
     */
    public void ship() {
        if (status == OrderStatus.PAID && !requiresShipment()) {
            throw new BusinessRuleException("Order " + id + " has no items that require shipment");
        }
        transitionTo(OrderStatus.SHIPPED);
    }

    /**
     * Moves the order to {@code DELIVERED}. From {@code PAID} this is allowed only when no item
     * requires shipment.
     *
     * @throws OrderStateTransitionException when the transition is not allowed
     */
    public void deliver() {
        if (status == OrderStatus.PAID && requiresShipment()) {
            throw new OrderStateTransitionException(id, status, OrderStatus.DELIVERED);
        }
        transitionTo(OrderStatus.DELIVERED);
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    /**
     * Updates the order status from its shipments: {@code SHIPPED} when there is a shipment for
     * every warehouse and all of them have left, {@code DELIVERED} when all of them are delivered.
     *
     * @throws BusinessRuleException when no item requires shipment or a shipment belongs to
     *                               another order
     */
    public void applyShipmentProgress(List<Shipment> shipments) {
        Objects.requireNonNull(shipments, "Shipments are required");
        if (!requiresShipment()) {
            throw new BusinessRuleException("Order " + id + " has no items that require shipment");
        }
        if (shipments.stream().anyMatch(shipment -> !shipment.getOrderId().equals(id))) {
            throw new BusinessRuleException("Every shipment must belong to order " + id);
        }
        Set<WarehouseId> covered = shipments.stream()
            .map(Shipment::getWarehouseId)
            .collect(Collectors.toSet());
        if (!covered.containsAll(shippingWarehouses())) {
            return;
        }
        if (status == OrderStatus.PAID
                && shipments.stream().allMatch(shipment -> shipment.getStatus().hasLeftWarehouse())) {
            ship();
        }
        if (status == OrderStatus.SHIPPED
                && shipments.stream().allMatch(shipment -> shipment.getStatus().isTerminal())) {
            deliver();
        }
    }

    /** Tells whether at least one item travels in a shipment. */
    public boolean requiresShipment() {
        return items.stream().anyMatch(OrderItem::requiresShipment);
    }

    /** Warehouses that must ship items of this order: one shipment for each. */
    public Set<WarehouseId> shippingWarehouses() {
        Set<WarehouseId> warehouses = new LinkedHashSet<>();
        items.stream()
            .filter(item -> item.requiresShipment() && item.hasWarehouse())
            .forEach(item -> warehouses.add(item.getWarehouseId()));
        return warehouses;
    }

    /** Tells whether the order was paid, even if it was cancelled afterwards. */
    public boolean wasPaid() {
        return paidAt != null;
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

    private Money calculateTotal() {
        Money result = null;
        for (OrderItem item : items) {
            result = (result == null) ? item.subtotal() : result.add(item.subtotal());
        }
        return result;
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

    public Optional<LocalDateTime> getPaidAt() {
        return Optional.ofNullable(paidAt);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
