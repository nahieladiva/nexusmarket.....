package application.domain.models;

import application.domain.valueobjects.InventoryId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.Objects;

/**
 * Inventory entity: available stock of a product in a warehouse.
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>A (product, warehouse) pair uniquely identifies the inventory.</li>
 *   <li>The available quantity can never be negative.</li>
 *   <li>When the available stock falls below the reorder threshold it is
 *       considered low inventory (raises {@code LowStockEvent}).</li>
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
        this.id = Objects.requireNonNull(id, "Inventory id is required");
        this.productId = Objects.requireNonNull(productId, "Product is required");
        this.warehouseId = Objects.requireNonNull(warehouseId, "Warehouse is required");
        this.onHand = Objects.requireNonNull(onHand, "Available quantity is required");
        this.reorderThreshold =
            Objects.requireNonNull(reorderThreshold, "Reorder threshold is required");
        this.location = Objects.requireNonNull(location, "Location is required");
    }

    public static Inventory create(ProductId productId, WarehouseId warehouseId,
                                   Quantity onHand, Quantity reorderThreshold,
                                   WarehouseLocation location) {
        return new Inventory(InventoryId.random(), productId, warehouseId,
            onHand, reorderThreshold, location);
    }

    /**
     * Increases the available stock (goods coming in).
     */
    public void increase(Quantity amount) {
        Objects.requireNonNull(amount, "Quantity is required");
        this.onHand = this.onHand.add(amount);
    }

    /**
     * Decreases the available stock (sale, shrinkage). Validates that stock is available.
     */
    public void decrease(Quantity amount) {
        Objects.requireNonNull(amount, "Quantity is required");
        if (!isAvailable(amount)) {
            throw new IllegalStateException(
                "Insufficient stock in warehouse " + warehouseId
                    + ": available " + onHand + ", requested " + amount);
        }
        this.onHand = this.onHand.subtract(amount);
    }

    /**
     * Tells whether there is enough stock to satisfy the requested quantity.
     */
    public boolean isAvailable(Quantity requested) {
        Objects.requireNonNull(requested, "Quantity is required");
        return this.onHand.compareTo(requested) >= 0;
    }

    /**
     * Business rule: stock is considered low when the available quantity
     * is lower than the reorder threshold.
     */
    public boolean isBelowReorderPoint() {
        return this.onHand.compareTo(this.reorderThreshold) < 0;
    }

    public void changeReorderThreshold(Quantity newThreshold) {
        this.reorderThreshold =
            Objects.requireNonNull(newThreshold, "Reorder threshold is required");
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