package application.adapters.useCases;

import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.models.StockMovement;
import application.domain.models.Warehouse;
import application.domain.ports.in.LogisticsOperatorPort;
import application.domain.services.authorization.ResolveActorService;
import application.domain.services.inventory.AdjustStockService;
import application.domain.services.inventory.ChangeReorderThresholdService;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.inventory.CreateInventoryService;
import application.domain.services.inventory.RestockInventoryService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.product.ConsultProductService;
import application.domain.services.returns.ConsultReturnService;
import application.domain.services.returns.ReceiveReturnService;
import application.domain.services.shipment.ConfirmDeliveryService;
import application.domain.services.shipment.ConsultShipmentService;
import application.domain.services.shipment.CreateShipmentService;
import application.domain.services.shipment.DispatchShipmentService;
import application.domain.services.shipment.PackShipmentService;
import application.domain.services.warehouse.ConsultWarehouseService;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements {@link LogisticsOperatorPort} by delegating to the domain services.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class LogisticsOperatorUseCase implements LogisticsOperatorPort {

    private final ResolveActorService resolveActor;
    private final CreateInventoryService createInventory;
    private final RestockInventoryService restockInventory;
    private final AdjustStockService adjustStock;
    private final ChangeReorderThresholdService changeReorderThreshold;
    private final ConsultInventoryService consultInventory;
    private final CreateShipmentService createShipment;
    private final PackShipmentService packShipment;
    private final DispatchShipmentService dispatchShipment;
    private final ConfirmDeliveryService confirmDelivery;
    private final ConsultShipmentService consultShipment;
    private final ReceiveReturnService receiveReturn;
    private final ConsultReturnService consultReturn;
    private final ConsultOrderService consultOrder;
    private final ConsultProductService consultProduct;
    private final ConsultWarehouseService consultWarehouse;

    @Override
    public Inventory createInventory(UserId operatorId, ProductId productId, WarehouseId warehouseId,
                                     Quantity onHand, Quantity reorderThreshold,
                                     WarehouseLocation location) {
        return createInventory.execute(resolveActor.execute(operatorId), productId, warehouseId, onHand,
            reorderThreshold, location);
    }

    @Override
    public Inventory restock(UserId operatorId, ProductId productId, WarehouseId warehouseId,
                             Quantity amount, String reason) {
        return restockInventory.execute(resolveActor.execute(operatorId), productId, warehouseId, amount,
            reason);
    }

    @Override
    public Inventory adjustStock(UserId operatorId, ProductId productId, WarehouseId warehouseId, int delta,
                                 String reason) {
        return adjustStock.execute(resolveActor.execute(operatorId), productId, warehouseId, delta, reason);
    }

    @Override
    public Inventory changeReorderThreshold(UserId operatorId, ProductId productId, WarehouseId warehouseId,
                                            Quantity newThreshold) {
        return changeReorderThreshold.execute(resolveActor.execute(operatorId), productId, warehouseId,
            newThreshold);
    }

    @Override
    @Transactional(readOnly = true)
    public Inventory findInventory(UserId operatorId, ProductId productId, WarehouseId warehouseId) {
        return consultInventory.execute(resolveActor.execute(operatorId), productId, warehouseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findInventoriesByWarehouse(UserId operatorId, WarehouseId warehouseId) {
        return consultInventory.findByWarehouse(resolveActor.execute(operatorId), warehouseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findInventories(UserId operatorId) {
        return consultInventory.findAll(resolveActor.execute(operatorId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findBelowReorderPoint(UserId operatorId) {
        return consultInventory.findBelowReorderPoint(resolveActor.execute(operatorId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovement> findMovements(UserId operatorId, ProductId productId,
                                             WarehouseId warehouseId) {
        return consultInventory.findMovements(resolveActor.execute(operatorId), productId, warehouseId);
    }

    @Override
    public Shipment createShipment(UserId operatorId, OrderId orderId, WarehouseId warehouseId) {
        return createShipment.execute(resolveActor.execute(operatorId), orderId, warehouseId);
    }

    @Override
    public Shipment packShipment(UserId operatorId, ShipmentId shipmentId) {
        return packShipment.execute(resolveActor.execute(operatorId), shipmentId);
    }

    @Override
    public Shipment dispatchShipment(UserId operatorId, ShipmentId shipmentId, String trackingNumber) {
        return dispatchShipment.execute(resolveActor.execute(operatorId), shipmentId, trackingNumber);
    }

    @Override
    public Shipment confirmDelivery(UserId operatorId, ShipmentId shipmentId) {
        return confirmDelivery.execute(resolveActor.execute(operatorId), shipmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipment> findShipmentsByOrder(UserId operatorId, OrderId orderId) {
        return consultShipment.findByOrder(resolveActor.execute(operatorId), orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipment> findShipments(UserId operatorId) {
        return consultShipment.findAll(resolveActor.execute(operatorId));
    }

    @Override
    public ReturnRequest receiveReturn(UserId operatorId, ReturnId returnId) {
        return receiveReturn.execute(resolveActor.execute(operatorId), returnId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> findReturns(UserId operatorId) {
        return consultReturn.findAll(resolveActor.execute(operatorId));
    }

    @Override
    @Transactional(readOnly = true)
    public Order findOrder(UserId operatorId, OrderId orderId) {
        return consultOrder.execute(resolveActor.execute(operatorId), orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findOrders(UserId operatorId) {
        return consultOrder.findAll(resolveActor.execute(operatorId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findProducts(UserId operatorId) {
        return consultProduct.findAll(resolveActor.execute(operatorId));
    }

    @Override
    @Transactional(readOnly = true)
    public Warehouse findWarehouse(UserId operatorId, WarehouseId warehouseId) {
        return consultWarehouse.execute(resolveActor.execute(operatorId), warehouseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Warehouse> findWarehouses(UserId operatorId) {
        return consultWarehouse.findAll(resolveActor.execute(operatorId));
    }
}
