package application.domain.services.inventory;

import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.User;
import application.domain.ports.out.InventoryRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;

import java.util.Objects;

/**
 * Changes the stock level below which a product counts as low stock.
 */
@DomainService
public class ChangeReorderThresholdService {

    private final InventoryRepository inventoryRepository;
    private final ValidateInventoryAccessService validateInventoryAccessService;

    public ChangeReorderThresholdService(InventoryRepository inventoryRepository,
                                         ValidateInventoryAccessService validateInventoryAccessService) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository);
        this.validateInventoryAccessService = Objects.requireNonNull(validateInventoryAccessService);
    }

    public Inventory execute(User actor, ProductId productId, WarehouseId warehouseId,
                             Quantity newThreshold) {
        validateInventoryAccessService.requireManage(actor, productId, warehouseId);
        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory", productId + "|" + warehouseId));
        inventory.changeReorderThreshold(newThreshold);
        return inventoryRepository.save(inventory);
    }
}
