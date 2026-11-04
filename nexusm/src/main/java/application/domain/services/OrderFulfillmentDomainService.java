package application.domain.services;

import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Coordinates stock with the order lifecycle.
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>When the order is paid, each item gets a warehouse and its stock is reserved.</li>
 *   <li>When a paid order is cancelled, the stock goes back to the same warehouse.</li>
 *   <li>Cancelling an order pending payment does not touch the stock, because it was never reserved.</li>
 * </ul>
 * This service does not persist the order; the use case does.
 */
public final class OrderFulfillmentDomainService {

    private final InventoryDomainService inventoryDomainService;
    private final WarehouseAllocationService warehouseAllocationService;

    public OrderFulfillmentDomainService(InventoryDomainService inventoryDomainService,
                                         WarehouseAllocationService warehouseAllocationService) {
        this.inventoryDomainService = Objects.requireNonNull(inventoryDomainService,
            "Inventory domain service is required");
        this.warehouseAllocationService = Objects.requireNonNull(warehouseAllocationService,
            "Warehouse allocation service is required");
    }

    /**
     * Registers the payment of the order and reserves its stock.
     *
     * @return modified inventories (to record movements and low-stock alerts)
     */
    public List<Inventory> confirm(Order order) {
        Map<ProductId, WarehouseId> allocation = warehouseAllocationService.allocate(order.getItems());
        order.assignWarehouses(allocation);
        order.pay();
        List<Inventory> touched = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            touched.add(inventoryDomainService.reserve(
                item.getProductId(), item.getWarehouseId(), item.getQuantity()));
        }
        return touched;
    }

    /**
     * Cancels the order and, if it had reserved stock, returns it.
     *
     * @return modified inventories (empty when the order was pending payment)
     */
    public List<Inventory> cancel(Order order) {
        boolean hadReservation = order.hasReservedStock();
        order.cancel();
        List<Inventory> touched = new ArrayList<>();
        if (hadReservation) {
            for (OrderItem item : order.getItems()) {
                touched.add(inventoryDomainService.restock(
                    item.getProductId(), item.getWarehouseId(), item.getQuantity()));
            }
        }
        return touched;
    }
}
