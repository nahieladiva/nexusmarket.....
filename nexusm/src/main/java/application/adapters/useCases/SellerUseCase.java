package application.adapters.useCases;

import application.domain.enums.ProductType;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.StockMovement;
import application.domain.models.Warehouse;
import application.domain.ports.in.SellerPort;
import application.domain.services.authorization.ResolveActorService;
import application.domain.services.inventory.AdjustStockService;
import application.domain.services.inventory.ChangeReorderThresholdService;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.inventory.CreateInventoryService;
import application.domain.services.inventory.RestockInventoryService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.product.ChangeProductPriceService;
import application.domain.services.product.ConsultProductService;
import application.domain.services.product.DiscontinueProductService;
import application.domain.services.product.PublishProductService;
import application.domain.services.product.RegisterProductService;
import application.domain.services.product.SuspendProductService;
import application.domain.services.product.UpdateProductService;
import application.domain.services.warehouse.ActivateWarehouseService;
import application.domain.services.warehouse.ConsultWarehouseService;
import application.domain.services.warehouse.CreateSellerWarehouseService;
import application.domain.services.warehouse.DeactivateWarehouseService;
import application.domain.services.warehouse.UpdateWarehouseService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link SellerPort} by delegating to the domain services.
 */
@Service
@RequiredArgsConstructor
public class SellerUseCase implements SellerPort {

    private final ResolveActorService resolveActor;
    private final RegisterProductService registerProduct;
    private final UpdateProductService updateProduct;
    private final ChangeProductPriceService changeProductPrice;
    private final PublishProductService publishProduct;
    private final SuspendProductService suspendProduct;
    private final DiscontinueProductService discontinueProduct;
    private final ConsultProductService consultProduct;
    private final CreateSellerWarehouseService createSellerWarehouse;
    private final UpdateWarehouseService updateWarehouse;
    private final ActivateWarehouseService activateWarehouse;
    private final DeactivateWarehouseService deactivateWarehouse;
    private final ConsultWarehouseService consultWarehouse;
    private final CreateInventoryService createInventory;
    private final RestockInventoryService restockInventory;
    private final AdjustStockService adjustStock;
    private final ChangeReorderThresholdService changeReorderThreshold;
    private final ConsultInventoryService consultInventory;
    private final ConsultOrderService consultOrder;

    @Override
    public Product registerProduct(UserId sellerId, ProductCode code, ProductType type, String name,
                                   String description, Money price) {
        return registerProduct.execute(resolveActor.execute(sellerId), code, type, name, description, price);
    }

    @Override
    public Product updateProduct(UserId sellerId, ProductId productId, String name, String description) {
        return updateProduct.execute(resolveActor.execute(sellerId), productId, name, description);
    }

    @Override
    public Product changeProductPrice(UserId sellerId, ProductId productId, Money newPrice) {
        return changeProductPrice.execute(resolveActor.execute(sellerId), productId, newPrice);
    }

    @Override
    public Product publishProduct(UserId sellerId, ProductId productId) {
        return publishProduct.execute(resolveActor.execute(sellerId), productId);
    }

    @Override
    public Product suspendProduct(UserId sellerId, ProductId productId) {
        return suspendProduct.execute(resolveActor.execute(sellerId), productId);
    }

    @Override
    public Product discontinueProduct(UserId sellerId, ProductId productId) {
        return discontinueProduct.execute(resolveActor.execute(sellerId), productId);
    }

    @Override
    public List<Product> findProducts(UserId sellerId) {
        return consultProduct.findBySeller(resolveActor.execute(sellerId));
    }

    @Override
    public Warehouse createWarehouse(UserId sellerId, String name, Address address,
                                     WarehouseLocation location) {
        return createSellerWarehouse.execute(resolveActor.execute(sellerId), name, address, location);
    }

    @Override
    public Warehouse updateWarehouse(UserId sellerId, WarehouseId warehouseId, String name, Address address,
                                     WarehouseLocation location) {
        return updateWarehouse.execute(resolveActor.execute(sellerId), warehouseId, name, address, location);
    }

    @Override
    public Warehouse activateWarehouse(UserId sellerId, WarehouseId warehouseId) {
        return activateWarehouse.execute(resolveActor.execute(sellerId), warehouseId);
    }

    @Override
    public Warehouse deactivateWarehouse(UserId sellerId, WarehouseId warehouseId) {
        return deactivateWarehouse.execute(resolveActor.execute(sellerId), warehouseId);
    }

    @Override
    public Warehouse findWarehouse(UserId sellerId, WarehouseId warehouseId) {
        return consultWarehouse.execute(resolveActor.execute(sellerId), warehouseId);
    }

    @Override
    public List<Warehouse> findWarehouses(UserId sellerId) {
        return consultWarehouse.findBySeller(resolveActor.execute(sellerId));
    }

    @Override
    public Inventory createInventory(UserId sellerId, ProductId productId, WarehouseId warehouseId,
                                     Quantity onHand, Quantity reorderThreshold,
                                     WarehouseLocation location) {
        return createInventory.execute(resolveActor.execute(sellerId), productId, warehouseId, onHand,
            reorderThreshold, location);
    }

    @Override
    public Inventory restock(UserId sellerId, ProductId productId, WarehouseId warehouseId, Quantity amount,
                             String reason) {
        return restockInventory.execute(resolveActor.execute(sellerId), productId, warehouseId, amount, reason);
    }

    @Override
    public Inventory adjustStock(UserId sellerId, ProductId productId, WarehouseId warehouseId, int delta,
                                 String reason) {
        return adjustStock.execute(resolveActor.execute(sellerId), productId, warehouseId, delta, reason);
    }

    @Override
    public Inventory changeReorderThreshold(UserId sellerId, ProductId productId, WarehouseId warehouseId,
                                            Quantity newThreshold) {
        return changeReorderThreshold.execute(resolveActor.execute(sellerId), productId, warehouseId,
            newThreshold);
    }

    @Override
    public Inventory findInventory(UserId sellerId, ProductId productId, WarehouseId warehouseId) {
        return consultInventory.execute(resolveActor.execute(sellerId), productId, warehouseId);
    }

    @Override
    public List<Inventory> findInventories(UserId sellerId) {
        return consultInventory.findBySeller(resolveActor.execute(sellerId));
    }

    @Override
    public List<StockMovement> findMovements(UserId sellerId, ProductId productId, WarehouseId warehouseId) {
        return consultInventory.findMovements(resolveActor.execute(sellerId), productId, warehouseId);
    }

    @Override
    public Order findOrder(UserId sellerId, OrderId orderId) {
        return consultOrder.execute(resolveActor.execute(sellerId), orderId);
    }

    @Override
    public List<Order> findOrders(UserId sellerId) {
        return consultOrder.findBySeller(resolveActor.execute(sellerId));
    }
}
