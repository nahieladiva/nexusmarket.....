package application.domain.ports.in;

import application.domain.enums.UserRole;
import application.domain.models.Inventory;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Seller;
import application.domain.models.Shipment;
import application.domain.models.StockMovement;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.RefundId;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.List;

/**
 * Input port of the administrator: users, marketplace warehouses, product moderation, order
 * cancellation, returns and refunds. Stock can be read but not changed.
 */
public interface AdministratorPort {

    User registerSeller(UserId administratorId, String fullName, Email email, PhoneNumber phone,
                        String rawPassword, String businessName);

    User registerStaffUser(UserId administratorId, UserRole role, String fullName, Email email,
                           PhoneNumber phone, String rawPassword);

    Seller approveSeller(UserId administratorId, UserId sellerId);

    Seller suspendSeller(UserId administratorId, UserId sellerId);

    User findUser(UserId administratorId, UserId userId);

    List<User> findUsers(UserId administratorId);

    Warehouse createWarehouse(UserId administratorId, String name, Address address,
                              WarehouseLocation location);

    Warehouse updateWarehouse(UserId administratorId, WarehouseId warehouseId, String name,
                              Address address, WarehouseLocation location);

    Warehouse activateWarehouse(UserId administratorId, WarehouseId warehouseId);

    Warehouse deactivateWarehouse(UserId administratorId, WarehouseId warehouseId);

    Warehouse findWarehouse(UserId administratorId, WarehouseId warehouseId);

    List<Warehouse> findWarehouses(UserId administratorId);

    Product blockProduct(UserId administratorId, ProductId productId);

    Product unblockProduct(UserId administratorId, ProductId productId);

    Product discontinueProduct(UserId administratorId, ProductId productId);

    List<Product> findProducts(UserId administratorId);

    Inventory findInventory(UserId administratorId, ProductId productId, WarehouseId warehouseId);

    List<Inventory> findInventories(UserId administratorId);

    List<Inventory> findBelowReorderPoint(UserId administratorId);

    List<StockMovement> findMovements(UserId administratorId, ProductId productId, WarehouseId warehouseId);

    Order cancelOrder(UserId administratorId, OrderId orderId);

    Order findOrder(UserId administratorId, OrderId orderId);

    List<Order> findOrders(UserId administratorId);

    Invoice findInvoice(UserId administratorId, OrderId orderId);

    List<Invoice> findInvoices(UserId administratorId);

    List<Shipment> findShipments(UserId administratorId);

    ReturnRequest approveReturn(UserId administratorId, ReturnId returnId);

    ReturnRequest rejectReturn(UserId administratorId, ReturnId returnId);

    List<ReturnRequest> findReturns(UserId administratorId);

    Refund processRefund(UserId administratorId, RefundId refundId);

    Refund rejectRefund(UserId administratorId, RefundId refundId);

    List<Refund> findRefunds(UserId administratorId);
}
