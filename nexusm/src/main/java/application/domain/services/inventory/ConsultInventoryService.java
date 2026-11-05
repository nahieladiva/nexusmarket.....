package application.domain.services.inventory;

import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.Product;
import application.domain.models.StockMovement;
import application.domain.models.User;
import application.domain.ports.out.InventoryRepository;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.StockMovementLogPort;
import application.domain.services.DomainService;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;

import java.util.List;
import java.util.Objects;

/**
 * Inventory queries: internal staff see all stock, and a seller sees the stock of their products.
 */
@DomainService
public class ConsultInventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StockMovementLogPort movementLog;
    private final ValidateInventoryAccessService validateInventoryAccessService;

    public ConsultInventoryService(InventoryRepository inventoryRepository,
                                   ProductRepository productRepository,
                                   StockMovementLogPort movementLog,
                                   ValidateInventoryAccessService validateInventoryAccessService) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
        this.movementLog = Objects.requireNonNull(movementLog);
        this.validateInventoryAccessService = Objects.requireNonNull(validateInventoryAccessService);
    }

    public Inventory execute(User actor, ProductId productId, WarehouseId warehouseId) {
        validateInventoryAccessService.requireRead(actor, productId, warehouseId);
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory", productId + "|" + warehouseId));
    }

    public List<Inventory> findBySeller(User seller) {
        seller.requireRole(UserRole.SELLER);
        return productRepository.findBySellerId(seller.getId()).stream()
            .map(Product::getId)
            .flatMap(productId -> inventoryRepository.findByProductId(productId).stream())
            .toList();
    }

    public List<Inventory> findByWarehouse(User actor, WarehouseId warehouseId) {
        requireInternalStaff(actor);
        return inventoryRepository.findByWarehouseId(warehouseId);
    }

    public List<Inventory> findAll(User actor) {
        requireInternalStaff(actor);
        return inventoryRepository.findAll();
    }

    public List<Inventory> findBelowReorderPoint(User actor) {
        requireInternalStaff(actor);
        return inventoryRepository.findAll().stream()
            .filter(Inventory::isBelowReorderPoint)
            .toList();
    }

    public List<StockMovement> findMovements(User actor, ProductId productId, WarehouseId warehouseId) {
        validateInventoryAccessService.requireRead(actor, productId, warehouseId);
        return movementLog.findByProductAndWarehouse(productId, warehouseId);
    }

    private void requireInternalStaff(User actor) {
        actor.requireAnyRole(UserRole.LOGISTICS_OPERATOR, UserRole.ADMINISTRATOR, UserRole.SUPERVISOR);
    }
}
