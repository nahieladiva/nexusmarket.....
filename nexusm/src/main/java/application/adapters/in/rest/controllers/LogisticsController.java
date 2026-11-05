package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.InventoryMapper;
import application.adapters.in.rest.mappers.OrderMapper;
import application.adapters.in.rest.mappers.ProductMapper;
import application.adapters.in.rest.mappers.ReturnMapper;
import application.adapters.in.rest.mappers.ShipmentMapper;
import application.adapters.in.rest.mappers.WarehouseMapper;
import application.adapters.in.rest.requests.AdjustInventoryRequest;
import application.adapters.in.rest.requests.CreateInventoryRequest;
import application.adapters.in.rest.requests.CreateShipmentRequest;
import application.adapters.in.rest.requests.DispatchShipmentRequest;
import application.adapters.in.rest.requests.RestockRequest;
import application.adapters.in.rest.requests.ThresholdRequest;
import application.adapters.in.rest.responses.InventoryResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.ReturnResponse;
import application.adapters.in.rest.responses.ShipmentResponse;
import application.adapters.in.rest.responses.StockMovementResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.adapters.in.rest.security.AuthenticatedUser;
import application.domain.ports.in.LogisticsOperatorPort;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.WarehouseId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter of {@link LogisticsOperatorPort}. The operator always comes from the token.
 */
@RestController
@RequestMapping("/api/v1/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsOperatorPort port;
    private final InventoryMapper inventoryMapper;
    private final WarehouseMapper warehouseMapper;
    private final ShipmentMapper shipmentMapper;
    private final ReturnMapper returnMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @PostMapping("/inventory")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse createInventory(@AuthenticationPrincipal AuthenticatedUser me,
                                             @RequestBody CreateInventoryRequest request) {
        return inventoryMapper.toResponse(port.createInventory(me.userId(),
            ProductId.of(request.productId()), WarehouseId.of(request.warehouseId()),
            Quantity.of(request.onHand()), Quantity.of(request.reorderThreshold()),
            warehouseMapper.toDomain(request.location())));
    }

    @PostMapping("/inventory/restock")
    public InventoryResponse restock(@AuthenticationPrincipal AuthenticatedUser me,
                                     @RequestBody RestockRequest request) {
        return inventoryMapper.toResponse(port.restock(me.userId(), ProductId.of(request.productId()),
            WarehouseId.of(request.warehouseId()), Quantity.of(request.quantity()), request.reason()));
    }

    @PostMapping("/inventory/adjust")
    public InventoryResponse adjustStock(@AuthenticationPrincipal AuthenticatedUser me,
                                         @RequestBody AdjustInventoryRequest request) {
        return inventoryMapper.toResponse(port.adjustStock(me.userId(), ProductId.of(request.productId()),
            WarehouseId.of(request.warehouseId()), request.delta(), request.reason()));
    }

    @PutMapping("/inventory/threshold")
    public InventoryResponse changeReorderThreshold(@AuthenticationPrincipal AuthenticatedUser me,
                                                    @RequestBody ThresholdRequest request) {
        return inventoryMapper.toResponse(port.changeReorderThreshold(me.userId(),
            ProductId.of(request.productId()), WarehouseId.of(request.warehouseId()),
            Quantity.of(request.reorderThreshold())));
    }

    @GetMapping("/inventory/{productId}/{warehouseId}")
    public InventoryResponse findInventory(@AuthenticationPrincipal AuthenticatedUser me,
                                           @PathVariable ProductId productId,
                                           @PathVariable WarehouseId warehouseId) {
        return inventoryMapper.toResponse(port.findInventory(me.userId(), productId, warehouseId));
    }

    @GetMapping("/warehouses/{warehouseId}/inventory")
    public List<InventoryResponse> findInventoriesByWarehouse(
            @AuthenticationPrincipal AuthenticatedUser me, @PathVariable WarehouseId warehouseId) {
        return port.findInventoriesByWarehouse(me.userId(), warehouseId).stream()
            .map(inventoryMapper::toResponse).toList();
    }

    @GetMapping("/inventory")
    public List<InventoryResponse> findInventories(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findInventories(me.userId()).stream().map(inventoryMapper::toResponse).toList();
    }

    @GetMapping("/inventory/low-stock")
    public List<InventoryResponse> findBelowReorderPoint(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findBelowReorderPoint(me.userId()).stream().map(inventoryMapper::toResponse).toList();
    }

    @GetMapping("/inventory/{productId}/{warehouseId}/movements")
    public List<StockMovementResponse> findMovements(@AuthenticationPrincipal AuthenticatedUser me,
                                                     @PathVariable ProductId productId,
                                                     @PathVariable WarehouseId warehouseId) {
        return port.findMovements(me.userId(), productId, warehouseId).stream()
            .map(inventoryMapper::toResponse).toList();
    }

    @PostMapping("/shipments")
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponse createShipment(@AuthenticationPrincipal AuthenticatedUser me,
                                           @RequestBody CreateShipmentRequest request) {
        return shipmentMapper.toResponse(port.createShipment(me.userId(),
            OrderId.of(request.orderId()), WarehouseId.of(request.warehouseId())));
    }

    @PostMapping("/shipments/{shipmentId}/pack")
    public ShipmentResponse packShipment(@AuthenticationPrincipal AuthenticatedUser me,
                                         @PathVariable ShipmentId shipmentId) {
        return shipmentMapper.toResponse(port.packShipment(me.userId(), shipmentId));
    }

    @PostMapping("/shipments/{shipmentId}/dispatch")
    public ShipmentResponse dispatchShipment(@AuthenticationPrincipal AuthenticatedUser me,
                                             @PathVariable ShipmentId shipmentId,
                                             @RequestBody DispatchShipmentRequest request) {
        return shipmentMapper.toResponse(port.dispatchShipment(me.userId(), shipmentId,
            request.trackingNumber()));
    }

    @PostMapping("/shipments/{shipmentId}/deliver")
    public ShipmentResponse confirmDelivery(@AuthenticationPrincipal AuthenticatedUser me,
                                            @PathVariable ShipmentId shipmentId) {
        return shipmentMapper.toResponse(port.confirmDelivery(me.userId(), shipmentId));
    }

    @GetMapping("/orders/{orderId}/shipments")
    public List<ShipmentResponse> findShipmentsByOrder(@AuthenticationPrincipal AuthenticatedUser me,
                                                       @PathVariable OrderId orderId) {
        return port.findShipmentsByOrder(me.userId(), orderId).stream()
            .map(shipmentMapper::toResponse).toList();
    }

    @GetMapping("/shipments")
    public List<ShipmentResponse> findShipments(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findShipments(me.userId()).stream().map(shipmentMapper::toResponse).toList();
    }

    @PostMapping("/returns/{returnId}/receive")
    public ReturnResponse receiveReturn(@AuthenticationPrincipal AuthenticatedUser me,
                                        @PathVariable ReturnId returnId) {
        return returnMapper.toResponse(port.receiveReturn(me.userId(), returnId));
    }

    @GetMapping("/returns")
    public List<ReturnResponse> findReturns(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findReturns(me.userId()).stream().map(returnMapper::toResponse).toList();
    }

    @GetMapping("/orders/{orderId}")
    public OrderResponse findOrder(@AuthenticationPrincipal AuthenticatedUser me,
                                   @PathVariable OrderId orderId) {
        return orderMapper.toResponse(port.findOrder(me.userId(), orderId));
    }

    @GetMapping("/orders")
    public List<OrderResponse> findOrders(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findOrders(me.userId()).stream().map(orderMapper::toResponse).toList();
    }

    @GetMapping("/products")
    public List<ProductResponse> findProducts(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findProducts(me.userId()).stream().map(productMapper::toResponse).toList();
    }

    @GetMapping("/warehouses/{warehouseId}")
    public WarehouseResponse findWarehouse(@AuthenticationPrincipal AuthenticatedUser me,
                                           @PathVariable WarehouseId warehouseId) {
        return warehouseMapper.toResponse(port.findWarehouse(me.userId(), warehouseId));
    }

    @GetMapping("/warehouses")
    public List<WarehouseResponse> findWarehouses(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findWarehouses(me.userId()).stream().map(warehouseMapper::toResponse).toList();
    }
}
