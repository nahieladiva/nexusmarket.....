package application.domain.exceptions;

import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;

/**
 * Thrown when there is not enough available inventory to satisfy
 * a requested quantity of a product. The {@code warehouseId} may be
 * {@code null} when the shortage is evaluated in aggregate.
 */
public class InsufficientStockException extends DomainException {

    private final ProductId productId;
    private final WarehouseId warehouseId;
    private final int requested;
    private final int available;

    public InsufficientStockException(ProductId productId, WarehouseId warehouseId,
                                      Quantity requested, Quantity available) {
        super("Insufficient stock for product " + productId
            + (warehouseId != null ? " in warehouse " + warehouseId : "")
            + ": requested " + requested.getValue()
            + ", available " + available.getValue());
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.requested = requested.getValue();
        this.available = available.getValue();
    }

    public ProductId getProductId() {
        return productId;
    }

    public WarehouseId getWarehouseId() {
        return warehouseId;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}