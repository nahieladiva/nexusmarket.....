package application.domain.ports.in;

import application.domain.enums.ProductType;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.StockMovement;
import application.domain.models.Warehouse;
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

/**
 * Input port of the seller: their products, their own warehouses, the stock in those warehouses,
 * and the orders that contain their products.
 */
public interface SellerPort {

    Product registerProduct(UserId sellerId, ProductCode code, ProductType type, String name,
                            String description, Money price);

    Product updateProduct(UserId sellerId, ProductId productId, String name, String description);

    Product changeProductPrice(UserId sellerId, ProductId productId, Money newPrice);

    Product publishProduct(UserId sellerId, ProductId productId);

    Product suspendProduct(UserId sellerId, ProductId productId);

    Product discontinueProduct(UserId sellerId, ProductId productId);

    List<Product> findProducts(UserId sellerId);

    Warehouse createWarehouse(UserId sellerId, String name, Address address, WarehouseLocation location);

    Warehouse updateWarehouse(UserId sellerId, WarehouseId warehouseId, String name, Address address,
                              WarehouseLocation location);

    Warehouse activateWarehouse(UserId sellerId, WarehouseId warehouseId);

    Warehouse deactivateWarehouse(UserId sellerId, WarehouseId warehouseId);

    Warehouse findWarehouse(UserId sellerId, WarehouseId warehouseId);

    List<Warehouse> findWarehouses(UserId sellerId);

    Inventory createInventory(UserId sellerId, ProductId productId, WarehouseId warehouseId,
                              Quantity onHand, Quantity reorderThreshold, WarehouseLocation location);

    Inventory restock(UserId sellerId, ProductId productId, WarehouseId warehouseId, Quantity amount,
                      String reason);

    Inventory adjustStock(UserId sellerId, ProductId productId, WarehouseId warehouseId, int delta,
                          String reason);

    Inventory changeReorderThreshold(UserId sellerId, ProductId productId, WarehouseId warehouseId,
                                     Quantity newThreshold);

    Inventory findInventory(UserId sellerId, ProductId productId, WarehouseId warehouseId);

    List<Inventory> findInventories(UserId sellerId);

    List<StockMovement> findMovements(UserId sellerId, ProductId productId, WarehouseId warehouseId);

    Order findOrder(UserId sellerId, OrderId orderId);

    List<Order> findOrders(UserId sellerId);
}
