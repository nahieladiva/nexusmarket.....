package application.domain.models;

import application.domain.valueobjects.InventoryId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.Objects;

/**
 * Entidad de inventario: stock disponible de un producto en un almacén.
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li>Un par (producto, almacén) identifica de forma única el inventario.</li>
 *   <li>La cantidad disponible nunca puede ser negativa.</li>
 *   <li>Cuando el stock disponible cae por debajo del punto de reorden se
 *       considera inventario bajo (genera {@code LowStockEvent}).</li>
 * </ul>
 */
public final class Inventory {

    private final InventoryId id;
    private final ProductId productId;
    private final WarehouseId warehouseId;
    private Quantity onHand;
    private Quantity reorderThreshold;
    private WarehouseLocation location;

    public Inventory(InventoryId id, ProductId productId, WarehouseId warehouseId,
                     Quantity onHand, Quantity reorderThreshold, WarehouseLocation location) {
        this.id = Objects.requireNonNull(id, "El id de inventario es obligatorio");
        this.productId = Objects.requireNonNull(productId, "El producto es obligatorio");
        this.warehouseId = Objects.requireNonNull(warehouseId, "El almacén es obligatorio");
        this.onHand = Objects.requireNonNull(onHand, "La cantidad disponible es obligatoria");
        this.reorderThreshold =
            Objects.requireNonNull(reorderThreshold, "El punto de reorden es obligatorio");
        this.location = Objects.requireNonNull(location, "La ubicación es obligatoria");
    }

    public static Inventory create(ProductId productId, WarehouseId warehouseId,
                                   Quantity onHand, Quantity reorderThreshold,
                                   WarehouseLocation location) {
        return new Inventory(InventoryId.random(), productId, warehouseId,
            onHand, reorderThreshold, location);
    }

    /**
     * Incrementa el stock disponible (entrada de mercancía).
     */
    public void increase(Quantity amount) {
        Objects.requireNonNull(amount, "La cantidad es obligatoria");
        this.onHand = this.onHand.add(amount);
    }

    /**
     * Decrementa el stock disponible (venta, merma). Valida que exista disponibilidad.
     */
    public void decrease(Quantity amount) {
        Objects.requireNonNull(amount, "La cantidad es obligatoria");
        if (!isAvailable(amount)) {
            throw new IllegalStateException(
                "Stock insuficiente en el almacén " + warehouseId
                    + ": disponible " + onHand + ", solicitado " + amount);
        }
        this.onHand = this.onHand.subtract(amount);
    }

    /**
     * Indica si hay stock suficiente para satisfacer la cantidad solicitada.
     */
    public boolean isAvailable(Quantity requested) {
        Objects.requireNonNull(requested, "La cantidad es obligatoria");
        return this.onHand.compareTo(requested) >= 0;
    }

    /**
     * Regla de negocio: se considera stock bajo cuando la disponibilidad
     * es menor que el punto de reorden.
     */
    public boolean isBelowReorderPoint() {
        return this.onHand.compareTo(this.reorderThreshold) < 0;
    }

    public void changeReorderThreshold(Quantity newThreshold) {
        this.reorderThreshold =
            Objects.requireNonNull(newThreshold, "El punto de reorden es obligatorio");
    }

    public InventoryId getId() {
        return id;
    }

    public ProductId getProductId() {
        return productId;
    }

    public WarehouseId getWarehouseId() {
        return warehouseId;
    }

    public Quantity getOnHand() {
        return onHand;
    }

    public Quantity getReorderThreshold() {
        return reorderThreshold;
    }

    public WarehouseLocation getLocation() {
        return location;
    }
}