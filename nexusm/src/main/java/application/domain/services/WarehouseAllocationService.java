package application.domain.services;

import application.domain.exceptions.InsufficientStockException;
import application.domain.models.Inventory;
import application.domain.models.OrderItem;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryRepository;
import application.domain.ports.out.WarehouseRepository;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Decides which warehouse ships each item of an order.
 *
 * <p>Rule: only active warehouses are considered, and among those with enough stock
 * the one with the most available units is chosen (to avoid draining small warehouses).</p>
 */
public final class WarehouseAllocationService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;

    public WarehouseAllocationService(InventoryRepository inventoryRepository,
                                      WarehouseRepository warehouseRepository) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository,
            "inventoryRepository is required");
        this.warehouseRepository = Objects.requireNonNull(warehouseRepository,
            "warehouseRepository is required");
    }

    /**
     * @return product → assigned warehouse
     * @throws InsufficientStockException if a product has no active warehouse with stock
     */
    public Map<ProductId, WarehouseId> allocate(List<OrderItem> items) {
        Set<WarehouseId> activeWarehouses = warehouseRepository.findAll().stream()
            .filter(Warehouse::isActive)
            .map(Warehouse::getId)
            .collect(Collectors.toSet());
        Map<ProductId, WarehouseId> allocation = new LinkedHashMap<>();
        for (OrderItem item : items) {
            allocation.put(item.getProductId(), allocateForProduct(item, activeWarehouses));
        }
        return allocation;
    }

    /** Total stock of the product, counting only active warehouses. */
    public Quantity availableInActiveWarehouses(ProductId productId) {
        Set<WarehouseId> activeWarehouses = warehouseRepository.findAll().stream()
            .filter(Warehouse::isActive)
            .map(Warehouse::getId)
            .collect(Collectors.toSet());
        return totalAvailable(inventoryRepository.findByProductId(productId).stream()
            .filter(inv -> activeWarehouses.contains(inv.getWarehouseId()))
            .toList());
    }

    private WarehouseId allocateForProduct(OrderItem item, Set<WarehouseId> activeWarehouses) {
        List<Inventory> inventories = inventoryRepository.findByProductId(item.getProductId())
            .stream()
            .filter(inv -> activeWarehouses.contains(inv.getWarehouseId()))
            .toList();
        return inventories.stream()
            .filter(inv -> inv.isAvailable(item.getQuantity()))
            .max(Comparator.comparing(Inventory::getOnHand))
            .map(Inventory::getWarehouseId)
            .orElseThrow(() -> new InsufficientStockException(
                item.getProductId(), null, item.getQuantity(), totalAvailable(inventories)));
    }

    private Quantity totalAvailable(List<Inventory> inventories) {
        return Quantity.of(inventories.stream().mapToInt(inv -> inv.getOnHand().getValue()).sum());
    }
}
