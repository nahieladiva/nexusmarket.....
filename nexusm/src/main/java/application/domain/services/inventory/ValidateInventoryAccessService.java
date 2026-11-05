package application.domain.services.inventory;

import application.domain.enums.UserRole;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;

import java.util.Objects;

/**
 * Inventory access rules (RG-03). Logistics operators and administrators manage any warehouse. A
 * seller manages only their products in their own warehouses. Supervisors can only read.
 */
@DomainService
public class ValidateInventoryAccessService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public ValidateInventoryAccessService(ProductRepository productRepository,
                                          WarehouseRepository warehouseRepository) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.warehouseRepository = Objects.requireNonNull(warehouseRepository);
    }

    /** Validates that the actor can change the stock of a product in a warehouse. */
    public void requireManage(User actor, ProductId productId, WarehouseId warehouseId) {
        actor.requireAnyRole(UserRole.LOGISTICS_OPERATOR, UserRole.ADMINISTRATOR, UserRole.SELLER);
        if (actor.isSeller()) {
            requireOwner(actor, productId, warehouseId);
        }
    }

    /** Validates that the actor can see the stock of a product in a warehouse. */
    public void requireRead(User actor, ProductId productId, WarehouseId warehouseId) {
        actor.requireAnyRole(UserRole.LOGISTICS_OPERATOR, UserRole.ADMINISTRATOR,
            UserRole.SUPERVISOR, UserRole.SELLER);
        if (actor.isSeller()) {
            requireOwner(actor, productId, warehouseId);
        }
    }

    private void requireOwner(User seller, ProductId productId, WarehouseId warehouseId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId.toString()));
        if (!product.isOwnedBy(seller.getId()) || !warehouse.isOwnedBy(seller.getId())) {
            throw new ForbiddenActionException(
                "A seller can only manage their products in their own warehouses");
        }
    }
}
