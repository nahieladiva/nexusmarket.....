package application.adapters.useCases;

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
import application.domain.ports.in.AdministratorPort;
import application.domain.services.authorization.ResolveActorService;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.invoice.ConsultInvoiceService;
import application.domain.services.order.CancelOrderService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.product.BlockProductService;
import application.domain.services.product.ConsultProductService;
import application.domain.services.product.DiscontinueProductService;
import application.domain.services.product.UnblockProductService;
import application.domain.services.refund.ConsultRefundService;
import application.domain.services.refund.ProcessRefundService;
import application.domain.services.refund.RejectRefundService;
import application.domain.services.returns.ApproveReturnService;
import application.domain.services.returns.ConsultReturnService;
import application.domain.services.returns.RejectReturnService;
import application.domain.services.shipment.ConsultShipmentService;
import application.domain.services.user.ApproveSellerService;
import application.domain.services.user.ConsultUserService;
import application.domain.services.user.RegisterSellerService;
import application.domain.services.user.RegisterStaffUserService;
import application.domain.services.user.SuspendSellerService;
import application.domain.services.warehouse.ActivateWarehouseService;
import application.domain.services.warehouse.ConsultWarehouseService;
import application.domain.services.warehouse.CreateMarketplaceWarehouseService;
import application.domain.services.warehouse.DeactivateWarehouseService;
import application.domain.services.warehouse.UpdateWarehouseService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link AdministratorPort} by delegating to the domain services.
 */
@Service
@RequiredArgsConstructor
public class AdministratorUseCase implements AdministratorPort {

    private final ResolveActorService resolveActor;
    private final RegisterSellerService registerSeller;
    private final RegisterStaffUserService registerStaffUser;
    private final ApproveSellerService approveSeller;
    private final SuspendSellerService suspendSeller;
    private final ConsultUserService consultUser;
    private final CreateMarketplaceWarehouseService createMarketplaceWarehouse;
    private final UpdateWarehouseService updateWarehouse;
    private final ActivateWarehouseService activateWarehouse;
    private final DeactivateWarehouseService deactivateWarehouse;
    private final ConsultWarehouseService consultWarehouse;
    private final BlockProductService blockProduct;
    private final UnblockProductService unblockProduct;
    private final DiscontinueProductService discontinueProduct;
    private final ConsultProductService consultProduct;
    private final ConsultInventoryService consultInventory;
    private final CancelOrderService cancelOrder;
    private final ConsultOrderService consultOrder;
    private final ConsultInvoiceService consultInvoice;
    private final ConsultShipmentService consultShipment;
    private final ApproveReturnService approveReturn;
    private final RejectReturnService rejectReturn;
    private final ConsultReturnService consultReturn;
    private final ProcessRefundService processRefund;
    private final RejectRefundService rejectRefund;
    private final ConsultRefundService consultRefund;

    @Override
    public User registerSeller(UserId administratorId, String fullName, Email email, PhoneNumber phone,
                               String rawPassword, String businessName) {
        return registerSeller.execute(resolveActor.execute(administratorId), fullName, email, phone,
            rawPassword, businessName);
    }

    @Override
    public User registerStaffUser(UserId administratorId, UserRole role, String fullName, Email email,
                                  PhoneNumber phone, String rawPassword) {
        return registerStaffUser.execute(resolveActor.execute(administratorId), role, fullName, email,
            phone, rawPassword);
    }

    @Override
    public Seller approveSeller(UserId administratorId, UserId sellerId) {
        return approveSeller.execute(resolveActor.execute(administratorId), sellerId);
    }

    @Override
    public Seller suspendSeller(UserId administratorId, UserId sellerId) {
        return suspendSeller.execute(resolveActor.execute(administratorId), sellerId);
    }

    @Override
    public User findUser(UserId administratorId, UserId userId) {
        return consultUser.execute(resolveActor.execute(administratorId), userId);
    }

    @Override
    public List<User> findUsers(UserId administratorId) {
        return consultUser.findAll(resolveActor.execute(administratorId));
    }

    @Override
    public Warehouse createWarehouse(UserId administratorId, String name, Address address,
                                     WarehouseLocation location) {
        return createMarketplaceWarehouse.execute(resolveActor.execute(administratorId), name, address,
            location);
    }

    @Override
    public Warehouse updateWarehouse(UserId administratorId, WarehouseId warehouseId, String name,
                                     Address address, WarehouseLocation location) {
        return updateWarehouse.execute(resolveActor.execute(administratorId), warehouseId, name, address,
            location);
    }

    @Override
    public Warehouse activateWarehouse(UserId administratorId, WarehouseId warehouseId) {
        return activateWarehouse.execute(resolveActor.execute(administratorId), warehouseId);
    }

    @Override
    public Warehouse deactivateWarehouse(UserId administratorId, WarehouseId warehouseId) {
        return deactivateWarehouse.execute(resolveActor.execute(administratorId), warehouseId);
    }

    @Override
    public Warehouse findWarehouse(UserId administratorId, WarehouseId warehouseId) {
        return consultWarehouse.execute(resolveActor.execute(administratorId), warehouseId);
    }

    @Override
    public List<Warehouse> findWarehouses(UserId administratorId) {
        return consultWarehouse.findAll(resolveActor.execute(administratorId));
    }

    @Override
    public Product blockProduct(UserId administratorId, ProductId productId) {
        return blockProduct.execute(resolveActor.execute(administratorId), productId);
    }

    @Override
    public Product unblockProduct(UserId administratorId, ProductId productId) {
        return unblockProduct.execute(resolveActor.execute(administratorId), productId);
    }

    @Override
    public Product discontinueProduct(UserId administratorId, ProductId productId) {
        return discontinueProduct.execute(resolveActor.execute(administratorId), productId);
    }

    @Override
    public List<Product> findProducts(UserId administratorId) {
        return consultProduct.findAll(resolveActor.execute(administratorId));
    }

    @Override
    public Inventory findInventory(UserId administratorId, ProductId productId, WarehouseId warehouseId) {
        return consultInventory.execute(resolveActor.execute(administratorId), productId, warehouseId);
    }

    @Override
    public List<Inventory> findInventories(UserId administratorId) {
        return consultInventory.findAll(resolveActor.execute(administratorId));
    }

    @Override
    public List<Inventory> findBelowReorderPoint(UserId administratorId) {
        return consultInventory.findBelowReorderPoint(resolveActor.execute(administratorId));
    }

    @Override
    public List<StockMovement> findMovements(UserId administratorId, ProductId productId,
                                             WarehouseId warehouseId) {
        return consultInventory.findMovements(resolveActor.execute(administratorId), productId, warehouseId);
    }

    @Override
    public Order cancelOrder(UserId administratorId, OrderId orderId) {
        return cancelOrder.execute(resolveActor.execute(administratorId), orderId);
    }

    @Override
    public Order findOrder(UserId administratorId, OrderId orderId) {
        return consultOrder.execute(resolveActor.execute(administratorId), orderId);
    }

    @Override
    public List<Order> findOrders(UserId administratorId) {
        return consultOrder.findAll(resolveActor.execute(administratorId));
    }

    @Override
    public Invoice findInvoice(UserId administratorId, OrderId orderId) {
        return consultInvoice.findByOrder(resolveActor.execute(administratorId), orderId);
    }

    @Override
    public List<Invoice> findInvoices(UserId administratorId) {
        return consultInvoice.findAll(resolveActor.execute(administratorId));
    }

    @Override
    public List<Shipment> findShipments(UserId administratorId) {
        return consultShipment.findAll(resolveActor.execute(administratorId));
    }

    @Override
    public ReturnRequest approveReturn(UserId administratorId, ReturnId returnId) {
        return approveReturn.execute(resolveActor.execute(administratorId), returnId);
    }

    @Override
    public ReturnRequest rejectReturn(UserId administratorId, ReturnId returnId) {
        return rejectReturn.execute(resolveActor.execute(administratorId), returnId);
    }

    @Override
    public List<ReturnRequest> findReturns(UserId administratorId) {
        return consultReturn.findAll(resolveActor.execute(administratorId));
    }

    @Override
    public Refund processRefund(UserId administratorId, RefundId refundId) {
        return processRefund.execute(resolveActor.execute(administratorId), refundId);
    }

    @Override
    public Refund rejectRefund(UserId administratorId, RefundId refundId) {
        return rejectRefund.execute(resolveActor.execute(administratorId), refundId);
    }

    @Override
    public List<Refund> findRefunds(UserId administratorId) {
        return consultRefund.findAll(resolveActor.execute(administratorId));
    }
}
