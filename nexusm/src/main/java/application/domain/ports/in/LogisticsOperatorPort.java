package application.domain.ports.in;

import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.models.StockMovement;
import application.domain.models.Warehouse;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.List;

/**
 * Input port of the logistics operator: stock in any warehouse, shipments and the reception of
 * returns.
 */
public interface LogisticsOperatorPort {

    Inventory createInventory(UserId operatorId, ProductId productId, WarehouseId warehouseId,
                              Quantity onHand, Quantity reorderThreshold, WarehouseLocation location);

    Inventory restock(UserId operatorId, ProductId productId, WarehouseId warehouseId, Quantity amount,
                      String reason);

    Inventory adjustStock(UserId operatorId, ProductId productId, WarehouseId warehouseId, int delta,
                          String reason);

    Inventory changeReorderThreshold(UserId operatorId, ProductId productId, WarehouseId warehouseId,
                                     Quantity newThreshold);

    Inventory findInventory(UserId operatorId, ProductId productId, WarehouseId warehouseId);

    List<Inventory> findInventoriesByWarehouse(UserId operatorId, WarehouseId warehouseId);

    List<Inventory> findInventories(UserId operatorId);

    List<Inventory> findBelowReorderPoint(UserId operatorId);

    List<StockMovement> findMovements(UserId operatorId, ProductId productId, WarehouseId warehouseId);

    Shipment createShipment(UserId operatorId, OrderId orderId, WarehouseId warehouseId);

    Shipment packShipment(UserId operatorId, ShipmentId shipmentId);

    Shipment dispatchShipment(UserId operatorId, ShipmentId shipmentId, String trackingNumber);

    Shipment confirmDelivery(UserId operatorId, ShipmentId shipmentId);

    List<Shipment> findShipmentsByOrder(UserId operatorId, OrderId orderId);

    List<Shipment> findShipments(UserId operatorId);

    ReturnRequest receiveReturn(UserId operatorId, ReturnId returnId);

    List<ReturnRequest> findReturns(UserId operatorId);

    Order findOrder(UserId operatorId, OrderId orderId);

    List<Order> findOrders(UserId operatorId);

    List<Product> findProducts(UserId operatorId);

    Warehouse findWarehouse(UserId operatorId, WarehouseId warehouseId);

    List<Warehouse> findWarehouses(UserId operatorId);
}
