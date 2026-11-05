package application.domain.services.inventory;

import application.domain.enums.StockMovementType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.InventoryRepository;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.InventoryId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.Objects;

/**
 * Registers the initial stock of a product in a warehouse. There is one inventory per product and
 * warehouse, the warehouse must be active, and digital products have no inventory.
 */
@DomainService
public class CreateInventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final ValidateInventoryAccessService validateInventoryAccessService;
    private final RecordStockMovementService recordStockMovementService;
    private final IdGeneratorPort idGenerator;

    public CreateInventoryService(InventoryRepository inventoryRepository,
                                  ProductRepository productRepository,
                                  WarehouseRepository warehouseRepository,
                                  ValidateInventoryAccessService validateInventoryAccessService,
                                  RecordStockMovementService recordStockMovementService,
                                  IdGeneratorPort idGenerator) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
        this.warehouseRepository = Objects.requireNonNull(warehouseRepository);
        this.validateInventoryAccessService = Objects.requireNonNull(validateInventoryAccessService);
        this.recordStockMovementService = Objects.requireNonNull(recordStockMovementService);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public Inventory execute(User actor, ProductId productId, WarehouseId warehouseId, Quantity onHand,
                             Quantity reorderThreshold, WarehouseLocation location) {
        validateInventoryAccessService.requireManage(actor, productId, warehouseId);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));
        if (!product.getType().requiresInventory()) {
            throw new BusinessRuleException("Product " + product.getCode() + " has no inventory");
        }
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId.toString()));
        if (!warehouse.isActive()) {
            throw new BusinessRuleException("Stock cannot be registered in an inactive warehouse");
        }
        if (inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId).isPresent()) {
            throw new DuplicateResourceException("The product already has inventory in that warehouse");
        }
        Inventory saved = inventoryRepository.save(new Inventory(InventoryId.of(idGenerator.newId()),
            productId, warehouseId, onHand, reorderThreshold, location));
        recordStockMovementService.execute(saved, StockMovementType.INITIAL, onHand.getValue(),
            "Initial stock", actor.getId(), null);
        return saved;
    }
}
