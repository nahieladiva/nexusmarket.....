package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.InventoryMapper;
import application.adapters.in.rest.mappers.InvoiceMapper;
import application.adapters.in.rest.mappers.OrderMapper;
import application.adapters.in.rest.mappers.ProductMapper;
import application.adapters.in.rest.mappers.RefundMapper;
import application.adapters.in.rest.mappers.ReturnMapper;
import application.adapters.in.rest.mappers.ShipmentMapper;
import application.adapters.in.rest.mappers.UserMapper;
import application.adapters.in.rest.mappers.WarehouseMapper;
import application.adapters.in.rest.responses.InventoryResponse;
import application.adapters.in.rest.responses.InvoiceResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.RefundResponse;
import application.adapters.in.rest.responses.ReturnResponse;
import application.adapters.in.rest.responses.ShipmentResponse;
import application.adapters.in.rest.responses.StockMovementResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.adapters.in.rest.security.AuthenticatedUser;
import application.domain.ports.in.SupervisorPort;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter of {@link SupervisorPort}. It only reads.
 */
@RestController
@RequestMapping("/api/v1/supervisor")
@RequiredArgsConstructor
public class SupervisorController {

    private final SupervisorPort port;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;
    private final InventoryMapper inventoryMapper;
    private final OrderMapper orderMapper;
    private final InvoiceMapper invoiceMapper;
    private final ShipmentMapper shipmentMapper;
    private final ReturnMapper returnMapper;
    private final RefundMapper refundMapper;

    @GetMapping("/users/{userId}")
    public UserResponse findUser(@AuthenticationPrincipal AuthenticatedUser me,
                                 @PathVariable UserId userId) {
        return userMapper.toResponse(port.findUser(me.userId(), userId));
    }

    @GetMapping("/users")
    public List<UserResponse> findUsers(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findUsers(me.userId()).stream().map(userMapper::toResponse).toList();
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

    @GetMapping("/inventory/{productId}/{warehouseId}")
    public InventoryResponse findInventory(@AuthenticationPrincipal AuthenticatedUser me,
                                           @PathVariable ProductId productId,
                                           @PathVariable WarehouseId warehouseId) {
        return inventoryMapper.toResponse(port.findInventory(me.userId(), productId, warehouseId));
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

    @GetMapping("/orders/{orderId}")
    public OrderResponse findOrder(@AuthenticationPrincipal AuthenticatedUser me,
                                   @PathVariable OrderId orderId) {
        return orderMapper.toResponse(port.findOrder(me.userId(), orderId));
    }

    @GetMapping("/orders")
    public List<OrderResponse> findOrders(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findOrders(me.userId()).stream().map(orderMapper::toResponse).toList();
    }

    @GetMapping("/orders/{orderId}/invoice")
    public InvoiceResponse findInvoice(@AuthenticationPrincipal AuthenticatedUser me,
                                       @PathVariable OrderId orderId) {
        return invoiceMapper.toResponse(port.findInvoice(me.userId(), orderId));
    }

    @GetMapping("/invoices")
    public List<InvoiceResponse> findInvoices(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findInvoices(me.userId()).stream().map(invoiceMapper::toResponse).toList();
    }

    @GetMapping("/shipments")
    public List<ShipmentResponse> findShipments(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findShipments(me.userId()).stream().map(shipmentMapper::toResponse).toList();
    }

    @GetMapping("/returns")
    public List<ReturnResponse> findReturns(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findReturns(me.userId()).stream().map(returnMapper::toResponse).toList();
    }

    @GetMapping("/refunds")
    public List<RefundResponse> findRefunds(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findRefunds(me.userId()).stream().map(refundMapper::toResponse).toList();
    }
}
