package application.domain.models;

import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import java.util.Objects;

/**
 * Ítem de una orden: producto, cantidad y precio unitario efectivo al momento de la compra.
 *
 * <p>{@code warehouseId} es {@code null} mientras la orden está pendiente; se asigna al
 * confirmar la orden, cuando se reserva el stock en una bodega concreta.</p>
 */
public final class OrderItem {

    private final ProductId productId;
    private final Quantity quantity;
    private final Money unitPrice;
    private final WarehouseId warehouseId;

    public OrderItem(ProductId productId, Quantity quantity, Money unitPrice) {
        this(productId, quantity, unitPrice, null);
    }

    public OrderItem(ProductId productId, Quantity quantity, Money unitPrice,
                     WarehouseId warehouseId) {
        this.productId = Objects.requireNonNull(productId, "El producto es obligatorio");
        this.quantity = Objects.requireNonNull(quantity, "La cantidad es obligatoria");
        if (quantity.getValue() <= 0) {
            throw new IllegalArgumentException("La cantidad de un item debe ser mayor a cero");
        }
        this.unitPrice = Objects.requireNonNull(unitPrice, "El precio unitario es obligatorio");
        this.warehouseId = warehouseId;
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity.getValue());
    }

    /** Devuelve una copia del ítem asignado a la bodega indicada. */
    public OrderItem assignedTo(WarehouseId warehouse) {
        return new OrderItem(productId, quantity, unitPrice,
            Objects.requireNonNull(warehouse, "La bodega es obligatoria"));
    }

    public boolean hasWarehouse() {
        return warehouseId != null;
    }

    public ProductId getProductId() {
        return productId;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public WarehouseId getWarehouseId() {
        return warehouseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderItem orderItem = (OrderItem) o;
        return productId.equals(orderItem.productId)
            && quantity.equals(orderItem.quantity)
            && unitPrice.equals(orderItem.unitPrice)
            && Objects.equals(warehouseId, orderItem.warehouseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, quantity, unitPrice, warehouseId);
    }

    @Override
    public String toString() {
        return "OrderItem{product=" + productId + ", qty=" + quantity + ", unitPrice=" + unitPrice
            + (warehouseId != null ? ", warehouse=" + warehouseId : "") + '}';
    }
}
