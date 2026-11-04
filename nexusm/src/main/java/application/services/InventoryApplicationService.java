package application.services;

import application.domain.enums.StockMovementType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.Product;
import application.domain.models.StockMovement;
import application.domain.models.Warehouse;
import application.domain.ports.in.InventoryManagementPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.InventoryRepository;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.StockMovementLogPort;
import application.domain.ports.out.WarehouseRepository;
import application.domain.valueobjects.InventoryId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de inventario.
 *
 * <p>Reglas: solo el vendedor dueño del producto o un administrador modifica stock;
 * un registro por producto y bodega; solo en bodegas activas; el stock nunca queda negativo;
 * todo movimiento lleva motivo y queda en el historial.</p>
 */
@Service
@Transactional
public class InventoryApplicationService implements InventoryManagementPort {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockMovementLogPort movementLog;
    private final StockMovementRecorder recorder;
    private final AccessRules accessRules;
    private final IdGeneratorPort idGenerator;

    public InventoryApplicationService(InventoryRepository inventoryRepository,
                                       ProductRepository productRepository,
                                       WarehouseRepository warehouseRepository,
                                       StockMovementLogPort movementLog,
                                       StockMovementRecorder recorder,
                                       AccessRules accessRules,
                                       IdGeneratorPort idGenerator) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
        this.warehouseRepository = Objects.requireNonNull(warehouseRepository);
        this.movementLog = Objects.requireNonNull(movementLog);
        this.recorder = Objects.requireNonNull(recorder);
        this.accessRules = Objects.requireNonNull(accessRules);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public Inventory createInventory(UserId actorId, ProductId productId, WarehouseId warehouseId,
                                     Quantity onHand, Quantity reorderThreshold,
                                     WarehouseLocation location) {
        Product product = requireProduct(productId);
        accessRules.requireOwnerOrAdmin(actorId, product);
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException("Bodega", warehouseId.toString()));
        if (!warehouse.isActive()) {
            throw new BusinessRuleException("No se puede registrar stock en una bodega inactiva");
        }
        if (inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId).isPresent()) {
            throw new DuplicateResourceException(
                "El producto ya tiene inventario en esa bodega; use reabastecer o ajustar");
        }
        Inventory inventory = new Inventory(InventoryId.of(idGenerator.newId()), productId,
            warehouseId, onHand, reorderThreshold, location);
        Inventory saved = inventoryRepository.save(inventory);
        recorder.record(saved, StockMovementType.INITIAL, onHand.getValue(),
            "Stock inicial", actorId, null);
        return saved;
    }

    @Override
    public Inventory restock(UserId actorId, ProductId productId, WarehouseId warehouseId,
                             Quantity amount, String reason) {
        if (amount.isZero()) {
            throw new BusinessRuleException("La cantidad a reabastecer debe ser mayor a cero");
        }
        Inventory inventory = findInventory(productId, warehouseId);
        accessRules.requireOwnerOrAdmin(actorId, requireProduct(productId));
        inventory.increase(amount);
        Inventory saved = inventoryRepository.save(inventory);
        recorder.record(saved, StockMovementType.RESTOCK, amount.getValue(),
            defaultReason(reason, "Reabastecimiento"), actorId, null);
        return saved;
    }

    @Override
    public Inventory adjustStock(UserId actorId, ProductId productId, WarehouseId warehouseId,
                                 int delta, String reason) {
        if (delta == 0) {
            throw new BusinessRuleException("El ajuste no puede ser cero");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException("Todo ajuste manual de stock requiere un motivo");
        }
        Inventory inventory = findInventory(productId, warehouseId);
        accessRules.requireOwnerOrAdmin(actorId, requireProduct(productId));
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
        recorder.record(saved, StockMovementType.ADJUSTMENT, delta, reason.trim(), actorId, null);
        return saved;
    }

    @Override
    public Inventory changeReorderThreshold(UserId actorId, ProductId productId,
                                            WarehouseId warehouseId, Quantity newThreshold) {
        Inventory inventory = findInventory(productId, warehouseId);
        accessRules.requireOwnerOrAdmin(actorId, requireProduct(productId));
        inventory.changeReorderThreshold(newThreshold);
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public Inventory findInventory(ProductId productId, WarehouseId warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Inventario", productId + "|" + warehouseId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAvailable(ProductId productId, WarehouseId warehouseId, Quantity requested) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .map(inv -> inv.isAvailable(requested))
            .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findByWarehouse(WarehouseId warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findAllInventory() {
        return inventoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findBelowReorderPoint() {
        return inventoryRepository.findAll().stream()
            .filter(Inventory::isBelowReorderPoint)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovement> findMovements(ProductId productId, WarehouseId warehouseId) {
        return movementLog.findByProductAndWarehouse(productId, warehouseId);
    }

    private Product requireProduct(ProductId productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", productId.toString()));
    }

    private String defaultReason(String reason, String fallback) {
        return (reason == null || reason.isBlank()) ? fallback : reason.trim();
    }
}
