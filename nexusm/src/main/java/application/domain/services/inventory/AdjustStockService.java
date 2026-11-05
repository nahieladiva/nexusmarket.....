package application.domain.services.inventory;

import application.domain.enums.StockMovementType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.InsufficientStockException;
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
 * Manually adjusts stock (physical count, shrinkage, damage). Every adjustment needs a reason.
 */
@DomainService
public class AdjustStockService {

    private final InventoryRepository inventoryRepository;
    private final ValidateInventoryAccessService validateInventoryAccessService;
    private final RecordStockMovementService recordStockMovementService;

    public AdjustStockService(InventoryRepository inventoryRepository,
                              ValidateInventoryAccessService validateInventoryAccessService,
                              RecordStockMovementService recordStockMovementService) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository);
        this.validateInventoryAccessService = Objects.requireNonNull(validateInventoryAccessService);
        this.recordStockMovementService = Objects.requireNonNull(recordStockMovementService);
    }

    /**
     * @param delta units to add (positive) or remove (negative)
     */
    public Inventory execute(User actor, ProductId productId, WarehouseId warehouseId, int delta,
                             String reason) {
        validateInventoryAccessService.requireManage(actor, productId, warehouseId);
        if (delta == 0) {
            throw new BusinessRuleException("An adjustment cannot be zero");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException("Every manual stock adjustment needs a reason");
        }
        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory", productId + "|" + warehouseId));
        if (delta > 0) {
            inventory.increase(Quantity.of(delta));
        } else {
            Quantity toRemove = Quantity.of(-delta);
            if (!inventory.isAvailable(toRemove)) {
                throw new InsufficientStockException(productId, warehouseId, toRemove,
                    inventory.getOnHand());
            }
            inventory.decrease(toRemove);
        }
        Inventory saved = inventoryRepository.save(inventory);
        recordStockMovementService.execute(saved, StockMovementType.ADJUSTMENT, delta, reason.trim(),
            actor.getId(), null);
        return saved;
    }
}
