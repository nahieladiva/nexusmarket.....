package application.adapters.useCases;

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
import application.domain.ports.in.SupervisorPort;
import application.domain.services.authorization.ResolveActorService;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.invoice.ConsultInvoiceService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.product.ConsultProductService;
import application.domain.services.refund.ConsultRefundService;
import application.domain.services.returns.ConsultReturnService;
import application.domain.services.shipment.ConsultShipmentService;
import application.domain.services.user.ConsultUserService;
import application.domain.services.warehouse.ConsultWarehouseService;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements {@link SupervisorPort} by delegating to the domain services. It only reads.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SupervisorUseCase implements SupervisorPort {

    private final ResolveActorService resolveActor;
    private final ConsultUserService consultUser;
    private final ConsultProductService consultProduct;
    private final ConsultWarehouseService consultWarehouse;
    private final ConsultInventoryService consultInventory;
    private final ConsultOrderService consultOrder;
    private final ConsultInvoiceService consultInvoice;
    private final ConsultShipmentService consultShipment;
    private final ConsultReturnService consultReturn;
    private final ConsultRefundService consultRefund;

    @Override
    public User findUser(UserId supervisorId, UserId userId) {
        return consultUser.execute(resolveActor.execute(supervisorId), userId);
    }

    @Override
    public List<User> findUsers(UserId supervisorId) {
        return consultUser.findAll(resolveActor.execute(supervisorId));
    }

    @Override
    public List<Product> findProducts(UserId supervisorId) {
        return consultProduct.findAll(resolveActor.execute(supervisorId));
    }

    @Override
    public Warehouse findWarehouse(UserId supervisorId, WarehouseId warehouseId) {
        return consultWarehouse.execute(resolveActor.execute(supervisorId), warehouseId);
    }

    @Override
    public List<Warehouse> findWarehouses(UserId supervisorId) {
        return consultWarehouse.findAll(resolveActor.execute(supervisorId));
    }

    @Override
    public Inventory findInventory(UserId supervisorId, ProductId productId, WarehouseId warehouseId) {
        return consultInventory.execute(resolveActor.execute(supervisorId), productId, warehouseId);
    }

    @Override
    public List<Inventory> findInventories(UserId supervisorId) {
        return consultInventory.findAll(resolveActor.execute(supervisorId));
    }

    @Override
    public List<Inventory> findBelowReorderPoint(UserId supervisorId) {
        return consultInventory.findBelowReorderPoint(resolveActor.execute(supervisorId));
    }

    @Override
    public List<StockMovement> findMovements(UserId supervisorId, ProductId productId,
                                             WarehouseId warehouseId) {
        return consultInventory.findMovements(resolveActor.execute(supervisorId), productId, warehouseId);
    }

    @Override
    public Order findOrder(UserId supervisorId, OrderId orderId) {
        return consultOrder.execute(resolveActor.execute(supervisorId), orderId);
    }

    @Override
    public List<Order> findOrders(UserId supervisorId) {
        return consultOrder.findAll(resolveActor.execute(supervisorId));
    }

    @Override
    public Invoice findInvoice(UserId supervisorId, OrderId orderId) {
        return consultInvoice.findByOrder(resolveActor.execute(supervisorId), orderId);
    }

    @Override
    public List<Invoice> findInvoices(UserId supervisorId) {
        return consultInvoice.findAll(resolveActor.execute(supervisorId));
    }

    @Override
    public List<Shipment> findShipments(UserId supervisorId) {
        return consultShipment.findAll(resolveActor.execute(supervisorId));
    }

    @Override
    public List<ReturnRequest> findReturns(UserId supervisorId) {
        return consultReturn.findAll(resolveActor.execute(supervisorId));
    }

    @Override
    public List<Refund> findRefunds(UserId supervisorId) {
        return consultRefund.findAll(resolveActor.execute(supervisorId));
    }
}
