package application.domain.services.inventory;

import application.domain.enums.StockMovementType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.models.Inventory;
import application.domain.models.User;
import application.domain.services.DomainService;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;

import java.util.Objects;

/**
 * Registers goods coming into a warehouse.
 */
@DomainService
public class RestockInventoryService {

    private static final String DEFAULT_REASON = "Restock";

    private final InventoryDomainService inventoryDomainService;
    private final ValidateInventoryAccessService validateInventoryAccessService;
    private final RecordStockMovementService recordStockMovementService;

    public RestockInventoryService(InventoryDomainService inventoryDomainService,
                                   ValidateInventoryAccessService validateInventoryAccessService,
                                   RecordStockMovementService recordStockMovementService) {
        this.inventoryDomainService = Objects.requireNonNull(inventoryDomainService);
        this.validateInventoryAccessService = Objects.requireNonNull(validateInventoryAccessService);
        this.recordStockMovementService = Objects.requireNonNull(recordStockMovementService);
    }

    public Inventory execute(User actor, ProductId productId, WarehouseId warehouseId, Quantity amount,
                             String reason) {
        validateInventoryAccessService.requireManage(actor, productId, warehouseId);
        if (amount.isZero()) {
            throw new BusinessRuleException("The quantity to restock must be greater than zero");
        }
        Inventory saved = inventoryDomainService.restock(productId, warehouseId, amount);
        recordStockMovementService.execute(saved, StockMovementType.RESTOCK, amount.getValue(),
            reason == null || reason.isBlank() ? DEFAULT_REASON : reason.trim(), actor.getId(), null);
        return saved;
    }
}
