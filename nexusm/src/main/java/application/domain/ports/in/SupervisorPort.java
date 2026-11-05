package application.domain.ports.in;

import application.domain.models.Inventory;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.models.StockMovement;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;

import java.util.List;

/**
 * Input port of the supervisor: read-only access to all marketplace information.
 */
public interface SupervisorPort {

    User findUser(UserId supervisorId, UserId userId);

    List<User> findUsers(UserId supervisorId);

    List<Product> findProducts(UserId supervisorId);

    Warehouse findWarehouse(UserId supervisorId, WarehouseId warehouseId);

    List<Warehouse> findWarehouses(UserId supervisorId);

    Inventory findInventory(UserId supervisorId, ProductId productId, WarehouseId warehouseId);

    List<Inventory> findInventories(UserId supervisorId);

    List<Inventory> findBelowReorderPoint(UserId supervisorId);

    List<StockMovement> findMovements(UserId supervisorId, ProductId productId, WarehouseId warehouseId);

    Order findOrder(UserId supervisorId, OrderId orderId);

    List<Order> findOrders(UserId supervisorId);

    Invoice findInvoice(UserId supervisorId, OrderId orderId);

    List<Invoice> findInvoices(UserId supervisorId);

    List<Shipment> findShipments(UserId supervisorId);

    List<ReturnRequest> findReturns(UserId supervisorId);

    List<Refund> findRefunds(UserId supervisorId);
}
