package application.adapters.in.rest.controllers;

import application.adapters.in.rest.documentation.ApiDocumentation;
import application.adapters.in.rest.documentation.CreateResponses;
import application.adapters.in.rest.documentation.ListResponses;
import application.adapters.in.rest.documentation.ReadResponses;
import application.adapters.in.rest.documentation.WriteResponses;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Logistics", description = "Stock, shipments and returns handled by the logistics operator")
@SecurityRequirement(name = ApiDocumentation.BEARER_AUTH)
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

    @Operation(summary = "Create inventory")
    @CreateResponses
    @PostMapping("/inventory")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse createInventory(@AuthenticationPrincipal AuthenticatedUser me,
                                             @Valid @RequestBody CreateInventoryRequest request) {
        return inventoryMapper.toResponse(port.createInventory(me.userId(),
            ProductId.of(request.productId()), WarehouseId.of(request.warehouseId()),
            Quantity.of(request.onHand()), Quantity.of(request.reorderThreshold()),
            warehouseMapper.toDomain(request.location())));
    }

    @Operation(summary = "Register an inbound of stock")
    @WriteResponses
    @PostMapping("/inventory/restock")
    public InventoryResponse restock(@AuthenticationPrincipal AuthenticatedUser me,
                                     @Valid @RequestBody RestockRequest request) {
        return inventoryMapper.toResponse(port.restock(me.userId(), ProductId.of(request.productId()),
            WarehouseId.of(request.warehouseId()), Quantity.of(request.quantity()), request.reason()));
    }

    @Operation(summary = "Adjust stock manually")
    @WriteResponses
    @PostMapping("/inventory/adjust")
    public InventoryResponse adjustStock(@AuthenticationPrincipal AuthenticatedUser me,
                                         @Valid @RequestBody AdjustInventoryRequest request) {
        return inventoryMapper.toResponse(port.adjustStock(me.userId(), ProductId.of(request.productId()),
            WarehouseId.of(request.warehouseId()), request.delta(), request.reason()));
    }

    @Operation(summary = "Change the reorder threshold")
    @WriteResponses
    @PutMapping("/inventory/threshold")
    public InventoryResponse changeReorderThreshold(@AuthenticationPrincipal AuthenticatedUser me,
                                                    @Valid @RequestBody ThresholdRequest request) {
        return inventoryMapper.toResponse(port.changeReorderThreshold(me.userId(),
            ProductId.of(request.productId()), WarehouseId.of(request.warehouseId()),
            Quantity.of(request.reorderThreshold())));
    }

    @Operation(summary = "Find one inventory")
    @ReadResponses
    @GetMapping("/inventory/{productId}/{warehouseId}")
    public InventoryResponse findInventory(@AuthenticationPrincipal AuthenticatedUser me,
                                           @PathVariable ProductId productId,
                                           @PathVariable WarehouseId warehouseId) {
        return inventoryMapper.toResponse(port.findInventory(me.userId(), productId, warehouseId));
    }

    @Operation(summary = "List the inventories of a warehouse")
    @ReadResponses
    @GetMapping("/warehouses/{warehouseId}/inventory")
    public List<InventoryResponse> findInventoriesByWarehouse(
            @AuthenticationPrincipal AuthenticatedUser me, @PathVariable WarehouseId warehouseId) {
        return port.findInventoriesByWarehouse(me.userId(), warehouseId).stream()
            .map(inventoryMapper::toResponse).toList();
    }

    @Operation(summary = "List inventories")
    @ListResponses
    @GetMapping("/inventory")
    public List<InventoryResponse> findInventories(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findInventories(me.userId()).stream().map(inventoryMapper::toResponse).toList();
    }

    @Operation(summary = "List inventories below their reorder point")
    @ListResponses
    @GetMapping("/inventory/low-stock")
    public List<InventoryResponse> findBelowReorderPoint(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findBelowReorderPoint(me.userId()).stream().map(inventoryMapper::toResponse).toList();
    }

    @Operation(summary = "List the stock movements of an inventory")
    @ReadResponses
    @GetMapping("/inventory/{productId}/{warehouseId}/movements")
    public List<StockMovementResponse> findMovements(@AuthenticationPrincipal AuthenticatedUser me,
                                                     @PathVariable ProductId productId,
                                                     @PathVariable WarehouseId warehouseId) {
        return port.findMovements(me.userId(), productId, warehouseId).stream()
            .map(inventoryMapper::toResponse).toList();
    }

    @Operation(summary = "Create shipment")
    @CreateResponses
    @PostMapping("/shipments")
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponse createShipment(@AuthenticationPrincipal AuthenticatedUser me,
                                           @Valid @RequestBody CreateShipmentRequest request) {
        return shipmentMapper.toResponse(port.createShipment(me.userId(),
            OrderId.of(request.orderId()), WarehouseId.of(request.warehouseId())));
    }

    @Operation(summary = "Pack shipment")
    @WriteResponses
    @PostMapping("/shipments/{shipmentId}/pack")
    public ShipmentResponse packShipment(@AuthenticationPrincipal AuthenticatedUser me,
                                         @PathVariable ShipmentId shipmentId) {
        return shipmentMapper.toResponse(port.packShipment(me.userId(), shipmentId));
    }

    @Operation(summary = "Dispatch shipment")
    @WriteResponses
    @PostMapping("/shipments/{shipmentId}/dispatch")
    public ShipmentResponse dispatchShipment(@AuthenticationPrincipal AuthenticatedUser me,
                                             @PathVariable ShipmentId shipmentId,
                                             @Valid @RequestBody DispatchShipmentRequest request) {
        return shipmentMapper.toResponse(port.dispatchShipment(me.userId(), shipmentId,
            request.trackingNumber()));
    }

    @Operation(summary = "Confirm the delivery of a shipment")
    @WriteResponses
    @PostMapping("/shipments/{shipmentId}/deliver")
    public ShipmentResponse confirmDelivery(@AuthenticationPrincipal AuthenticatedUser me,
                                            @PathVariable ShipmentId shipmentId) {
        return shipmentMapper.toResponse(port.confirmDelivery(me.userId(), shipmentId));
    }

    @Operation(summary = "List the shipments of an order")
    @ReadResponses
    @GetMapping("/orders/{orderId}/shipments")
    public List<ShipmentResponse> findShipmentsByOrder(@AuthenticationPrincipal AuthenticatedUser me,
                                                       @PathVariable OrderId orderId) {
        return port.findShipmentsByOrder(me.userId(), orderId).stream()
            .map(shipmentMapper::toResponse).toList();
    }

    @Operation(summary = "List shipments")
    @ListResponses
    @GetMapping("/shipments")
    public List<ShipmentResponse> findShipments(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findShipments(me.userId()).stream().map(shipmentMapper::toResponse).toList();
    }

    @Operation(summary = "Receive return")
    @WriteResponses
    @PostMapping("/returns/{returnId}/receive")
    public ReturnResponse receiveReturn(@AuthenticationPrincipal AuthenticatedUser me,
                                        @PathVariable ReturnId returnId) {
        return returnMapper.toResponse(port.receiveReturn(me.userId(), returnId));
    }

    @Operation(summary = "List returns")
    @ListResponses
    @GetMapping("/returns")
    public List<ReturnResponse> findReturns(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findReturns(me.userId()).stream().map(returnMapper::toResponse).toList();
    }

    @Operation(summary = "Find one order")
    @ReadResponses
    @GetMapping("/orders/{orderId}")
    public OrderResponse findOrder(@AuthenticationPrincipal AuthenticatedUser me,
                                   @PathVariable OrderId orderId) {
        return orderMapper.toResponse(port.findOrder(me.userId(), orderId));
    }

    @Operation(summary = "List orders")
    @ListResponses
    @GetMapping("/orders")
    public List<OrderResponse> findOrders(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findOrders(me.userId()).stream().map(orderMapper::toResponse).toList();
    }

    @Operation(summary = "List products")
    @ListResponses
    @GetMapping("/products")
    public List<ProductResponse> findProducts(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findProducts(me.userId()).stream().map(productMapper::toResponse).toList();
    }

    @Operation(summary = "Find one warehouse")
    @ReadResponses
    @GetMapping("/warehouses/{warehouseId}")
    public WarehouseResponse findWarehouse(@AuthenticationPrincipal AuthenticatedUser me,
                                           @PathVariable WarehouseId warehouseId) {
        return warehouseMapper.toResponse(port.findWarehouse(me.userId(), warehouseId));
    }

    @Operation(summary = "List warehouses")
    @ListResponses
    @GetMapping("/warehouses")
    public List<WarehouseResponse> findWarehouses(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findWarehouses(me.userId()).stream().map(warehouseMapper::toResponse).toList();
    }
}
