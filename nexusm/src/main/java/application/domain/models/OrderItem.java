package application.domain.models;

import application.domain.enums.ProductType;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import java.util.Objects;

/**
 * Order item: product, quantity and effective unit price at the time of purchase.
 *
 * <p>{@code warehouseId} is {@code null} until the order is allocated; it is assigned before
 * payment, when stock is reserved in a specific warehouse. Items that do not require shipment
 * (digital products) never have a warehouse.</p>
 */
public final class OrderItem {

    private final ProductId productId;
    private final ProductType productType;
    private final Quantity quantity;
    private final Money unitPrice;
    private final WarehouseId warehouseId;

    public OrderItem(ProductId productId, ProductType productType, Quantity quantity, Money unitPrice) {
        this(productId, productType, quantity, unitPrice, null);
    }

    public OrderItem(ProductId productId, ProductType productType, Quantity quantity, Money unitPrice,
                     WarehouseId warehouseId) {
        this.productId = Objects.requireNonNull(productId, "Product is required");
        this.productType = Objects.requireNonNull(productType, "Product type is required");
        this.quantity = Objects.requireNonNull(quantity, "Quantity is required");
        if (quantity.getValue() <= 0) {
            throw new IllegalArgumentException("The quantity of an item must be greater than zero");
        }
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price is required");
        this.warehouseId = warehouseId;
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity.getValue());
    }

    /** Returns a copy of the item assigned to the given warehouse. */
    public OrderItem assignedTo(WarehouseId warehouse) {
        return new OrderItem(productId, productType, quantity, unitPrice,
            Objects.requireNonNull(warehouse, "Warehouse is required"));
    }

    public boolean hasWarehouse() {
        return warehouseId != null;
    }

    /** Tells whether the item takes stock from a warehouse and travels in a shipment. */
    public boolean requiresShipment() {
        return productType.requiresShipment();
    }

    public boolean isReturnable() {
        return productType.isReturnable();
    }

    public ProductType getProductType() {
        return productType;
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
            && productType == orderItem.productType
            && quantity.equals(orderItem.quantity)
            && unitPrice.equals(orderItem.unitPrice)
            && Objects.equals(warehouseId, orderItem.warehouseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productType, quantity, unitPrice, warehouseId);
    }

    @Override
    public String toString() {
        return "OrderItem{product=" + productId + ", qty=" + quantity + ", unitPrice=" + unitPrice
            + (warehouseId != null ? ", warehouse=" + warehouseId : "") + '}';
    }
}
