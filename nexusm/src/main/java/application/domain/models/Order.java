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
 * Orden de compra (agregado raíz).
 *
 * <p>Reglas de negocio:</p>
 * <ul>
 *   <li>Debe tener al menos un ítem.</li>
 *   <li>Cada producto aparece una sola vez y con máximo {@value #MAX_UNITS_PER_PRODUCT} unidades.</li>
 *   <li>Transiciones: PENDING → CONFIRMED → SHIPPED → DELIVERED; se cancela solo desde
 *       PENDING o CONFIRMED (ver {@link OrderStatus}).</li>
 *   <li>Para confirmar, todos los ítems deben tener bodega asignada.</li>
 *   <li>El total puede ser menor que la suma de subtotales (descuento por volumen),
 *       nunca mayor.</li>
 * </ul>
 */
public final class Order {

    public static final int MAX_UNITS_PER_PRODUCT = 10;

    private final OrderId id;
    private final UserId buyerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private Money total;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order(OrderId id, UserId buyerId, List<OrderItem> items, OrderStatus status,
                 Money total, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "El id de orden es obligatorio");
        this.buyerId = Objects.requireNonNull(buyerId, "El comprador es obligatorio");
        this.items = new ArrayList<>(items == null ? List.of() : items);
        if (this.items.isEmpty()) {
            throw new IllegalArgumentException("Una orden debe tener al menos un item");
        }
        this.status = status == null ? OrderStatus.PENDING : status;
        Money subtotal = calculateSubtotal();
        if (total != null && total.isGreaterThan(subtotal)) {
            throw new IllegalArgumentException(
                "El total de la orden no puede ser mayor que la suma de sus ítems");
        }
        this.total = total != null ? total : subtotal;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /** Crea una orden pendiente cuyo total es la suma de sus ítems. */
    public static Order create(UserId buyerId, List<OrderItem> items) {
        return create(OrderId.random(), buyerId, items, null, LocalDateTime.now());
    }

    /**
     * Crea una orden pendiente aplicando las reglas de cantidades.
     *
     * @param total total ya calculado (con descuentos); {@code null} = suma de ítems
     */
    public static Order create(OrderId id, UserId buyerId, List<OrderItem> items, Money total,
                               LocalDateTime now) {
        validateQuantities(items);
        return new Order(id, buyerId, items, OrderStatus.PENDING, total, now, now);
    }

    private static void validateQuantities(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Una orden debe tener al menos un item");
        }
        Set<ProductId> seen = new HashSet<>();
        for (OrderItem item : items) {
            if (!seen.add(item.getProductId())) {
                throw new BusinessRuleException(
                    "El producto " + item.getProductId() + " está repetido en la orden");
            }
            if (item.getQuantity().getValue() > MAX_UNITS_PER_PRODUCT) {
                throw new BusinessRuleException("Máximo " + MAX_UNITS_PER_PRODUCT
                    + " unidades por producto en una orden (producto " + item.getProductId() + ")");
            }
        }
    }

    public void addItem(OrderItem item) {
        if (status != OrderStatus.PENDING) {
            throw new OrderStateTransitionException(id, status, status);
        }
        List<OrderItem> candidate = new ArrayList<>(items);
        candidate.add(Objects.requireNonNull(item, "El item es obligatorio"));
        validateQuantities(candidate);
        this.items.add(item);
        this.total = calculateSubtotal();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Asigna a cada ítem la bodega desde la que se despacha. Solo en estado PENDING.
     */
    public void assignWarehouses(Map<ProductId, WarehouseId> allocation) {
        if (status != OrderStatus.PENDING) {
            throw new BusinessRuleException("Solo se asignan bodegas a órdenes pendientes");
        }
        Objects.requireNonNull(allocation, "La asignación de bodegas es obligatoria");
        List<OrderItem> assigned = new ArrayList<>();
        for (OrderItem item : items) {
            WarehouseId warehouse = allocation.get(item.getProductId());
            if (warehouse == null) {
                throw new BusinessRuleException(
                    "No hay bodega asignada para el producto " + item.getProductId());
            }
            assigned.add(item.assignedTo(warehouse));
        }
        this.items.clear();
        this.items.addAll(assigned);
    }

    public void confirm() {
        if (!this.status.canTransitionTo(OrderStatus.CONFIRMED)) {
            throw new OrderStateTransitionException(id, this.status, OrderStatus.CONFIRMED);
        }
        if (items.stream().anyMatch(item -> !item.hasWarehouse())) {
            throw new BusinessRuleException(
                "No se puede confirmar la orden " + id + " sin asignar bodega a todos los ítems");
        }
        transitionTo(OrderStatus.CONFIRMED);
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

    /** Indica si la orden reservó stock (es decir, fue confirmada y no despachada aún). */
    public boolean hasReservedStock() {
        return status == OrderStatus.CONFIRMED;
    }

    public boolean belongsTo(UserId userId) {
        return buyerId.equals(userId);
    }

    public boolean containsAnyProduct(Set<ProductId> productIds) {
        return items.stream().anyMatch(item -> productIds.contains(item.getProductId()));
    }

    /** Unidades totales por producto. */
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

    /** Suma de subtotales de los ítems, antes de descuentos de orden. */
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
