package application.adapters.in.rest.controllers;

import application.adapters.in.rest.documentation.ApiDocumentation;
import application.adapters.in.rest.documentation.CreateResponses;
import application.adapters.in.rest.documentation.ListResponses;
import application.adapters.in.rest.documentation.ReadResponses;
import application.adapters.in.rest.documentation.WriteResponses;
import application.adapters.in.rest.mappers.InventoryMapper;
import application.adapters.in.rest.mappers.InvoiceMapper;
import application.adapters.in.rest.mappers.OrderMapper;
import application.adapters.in.rest.mappers.ProductMapper;
import application.adapters.in.rest.mappers.RefundMapper;
import application.adapters.in.rest.mappers.ReturnMapper;
import application.adapters.in.rest.mappers.ShipmentMapper;
import application.adapters.in.rest.mappers.UserMapper;
import application.adapters.in.rest.mappers.WarehouseMapper;
import application.adapters.in.rest.requests.CreateSellerRequest;
import application.adapters.in.rest.requests.CreateStaffUserRequest;
import application.adapters.in.rest.requests.CreateWarehouseRequest;
import application.adapters.in.rest.requests.UpdateWarehouseRequest;
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
import application.domain.ports.in.AdministratorPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.RefundId;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;
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
 * REST adapter of {@link AdministratorPort}. The administrator always comes from the token.
 */
@Tag(name = "Administrator", description = "Users, warehouses, product moderation, orders, returns and refunds")
@SecurityRequirement(name = ApiDocumentation.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdministratorController {

    private final AdministratorPort port;
    private final UserMapper userMapper;
    private final WarehouseMapper warehouseMapper;
    private final ProductMapper productMapper;
    private final InventoryMapper inventoryMapper;
    private final OrderMapper orderMapper;
    private final InvoiceMapper invoiceMapper;
    private final ShipmentMapper shipmentMapper;
    private final ReturnMapper returnMapper;
    private final RefundMapper refundMapper;

    @Operation(summary = "Register seller")
    @CreateResponses
    @PostMapping("/sellers")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerSeller(@AuthenticationPrincipal AuthenticatedUser me,
                                       @Valid @RequestBody CreateSellerRequest request) {
        return userMapper.toResponse(port.registerSeller(me.userId(), request.fullName(),
            Email.of(request.email()), PhoneNumber.of(request.phone()), request.password(),
            request.businessName()));
    }

    @Operation(summary = "Register a logistics operator or a supervisor")
    @CreateResponses
    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerStaffUser(@AuthenticationPrincipal AuthenticatedUser me,
                                          @Valid @RequestBody CreateStaffUserRequest request) {
        return userMapper.toResponse(port.registerStaffUser(me.userId(), request.role(),
            request.fullName(), Email.of(request.email()), PhoneNumber.of(request.phone()),
            request.password()));
    }

    @Operation(summary = "Approve seller")
    @WriteResponses
    @PostMapping("/sellers/{sellerId}/approve")
    public UserResponse approveSeller(@AuthenticationPrincipal AuthenticatedUser me,
                                      @PathVariable UserId sellerId) {
        return userMapper.toResponse(port.approveSeller(me.userId(), sellerId));
    }

    @Operation(summary = "Suspend seller")
    @WriteResponses
    @PostMapping("/sellers/{sellerId}/suspend")
    public UserResponse suspendSeller(@AuthenticationPrincipal AuthenticatedUser me,
                                      @PathVariable UserId sellerId) {
        return userMapper.toResponse(port.suspendSeller(me.userId(), sellerId));
    }

    @Operation(summary = "Find one user")
    @ReadResponses
    @GetMapping("/users/{userId}")
    public UserResponse findUser(@AuthenticationPrincipal AuthenticatedUser me,
                                 @PathVariable UserId userId) {
        return userMapper.toResponse(port.findUser(me.userId(), userId));
    }

    @Operation(summary = "List users")
    @ListResponses
    @GetMapping("/users")
    public List<UserResponse> findUsers(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findUsers(me.userId()).stream().map(userMapper::toResponse).toList();
    }

    @Operation(summary = "Create warehouse")
    @CreateResponses
    @PostMapping("/warehouses")
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponse createWarehouse(@AuthenticationPrincipal AuthenticatedUser me,
                                             @Valid @RequestBody CreateWarehouseRequest request) {
        return warehouseMapper.toResponse(port.createWarehouse(me.userId(), request.name(),
            userMapper.toDomain(request.address()), warehouseMapper.toDomain(request.location())));
    }

    @Operation(summary = "Update warehouse")
    @WriteResponses
    @PutMapping("/warehouses/{warehouseId}")
    public WarehouseResponse updateWarehouse(@AuthenticationPrincipal AuthenticatedUser me,
                                             @PathVariable WarehouseId warehouseId,
                                             @Valid @RequestBody UpdateWarehouseRequest request) {
        return warehouseMapper.toResponse(port.updateWarehouse(me.userId(), warehouseId, request.name(),
            userMapper.toDomain(request.address()), warehouseMapper.toDomain(request.location())));
    }

    @Operation(summary = "Activate warehouse")
    @WriteResponses
    @PostMapping("/warehouses/{warehouseId}/activate")
    public WarehouseResponse activateWarehouse(@AuthenticationPrincipal AuthenticatedUser me,
                                               @PathVariable WarehouseId warehouseId) {
        return warehouseMapper.toResponse(port.activateWarehouse(me.userId(), warehouseId));
    }

    @Operation(summary = "Deactivate warehouse")
    @WriteResponses
    @PostMapping("/warehouses/{warehouseId}/deactivate")
    public WarehouseResponse deactivateWarehouse(@AuthenticationPrincipal AuthenticatedUser me,
                                                 @PathVariable WarehouseId warehouseId) {
        return warehouseMapper.toResponse(port.deactivateWarehouse(me.userId(), warehouseId));
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

    @Operation(summary = "Block product")
    @WriteResponses
    @PostMapping("/products/{productId}/block")
    public ProductResponse blockProduct(@AuthenticationPrincipal AuthenticatedUser me,
                                        @PathVariable ProductId productId) {
        return productMapper.toResponse(port.blockProduct(me.userId(), productId));
    }

    @Operation(summary = "Unblock product")
    @WriteResponses
    @PostMapping("/products/{productId}/unblock")
    public ProductResponse unblockProduct(@AuthenticationPrincipal AuthenticatedUser me,
                                          @PathVariable ProductId productId) {
        return productMapper.toResponse(port.unblockProduct(me.userId(), productId));
    }

    @Operation(summary = "Discontinue product")
    @WriteResponses
    @PostMapping("/products/{productId}/discontinue")
    public ProductResponse discontinueProduct(@AuthenticationPrincipal AuthenticatedUser me,
                                              @PathVariable ProductId productId) {
        return productMapper.toResponse(port.discontinueProduct(me.userId(), productId));
    }

    @Operation(summary = "List products")
    @ListResponses
    @GetMapping("/products")
    public List<ProductResponse> findProducts(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findProducts(me.userId()).stream().map(productMapper::toResponse).toList();
    }

    @Operation(summary = "Find one inventory")
    @ReadResponses
    @GetMapping("/inventory/{productId}/{warehouseId}")
    public InventoryResponse findInventory(@AuthenticationPrincipal AuthenticatedUser me,
                                           @PathVariable ProductId productId,
                                           @PathVariable WarehouseId warehouseId) {
        return inventoryMapper.toResponse(port.findInventory(me.userId(), productId, warehouseId));
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

    @Operation(summary = "Cancel order")
    @WriteResponses
    @PostMapping("/orders/{orderId}/cancel")
    public OrderResponse cancelOrder(@AuthenticationPrincipal AuthenticatedUser me,
                                     @PathVariable OrderId orderId) {
        return orderMapper.toResponse(port.cancelOrder(me.userId(), orderId));
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

    @Operation(summary = "Find one invoice")
    @ReadResponses
    @GetMapping("/orders/{orderId}/invoice")
    public InvoiceResponse findInvoice(@AuthenticationPrincipal AuthenticatedUser me,
                                       @PathVariable OrderId orderId) {
        return invoiceMapper.toResponse(port.findInvoice(me.userId(), orderId));
    }

    @Operation(summary = "List invoices")
    @ListResponses
    @GetMapping("/invoices")
    public List<InvoiceResponse> findInvoices(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findInvoices(me.userId()).stream().map(invoiceMapper::toResponse).toList();
    }

    @Operation(summary = "List shipments")
    @ListResponses
    @GetMapping("/shipments")
    public List<ShipmentResponse> findShipments(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findShipments(me.userId()).stream().map(shipmentMapper::toResponse).toList();
    }

    @Operation(summary = "Approve return")
    @WriteResponses
    @PostMapping("/returns/{returnId}/approve")
    public ReturnResponse approveReturn(@AuthenticationPrincipal AuthenticatedUser me,
                                        @PathVariable ReturnId returnId) {
        return returnMapper.toResponse(port.approveReturn(me.userId(), returnId));
    }

    @Operation(summary = "Reject return")
    @WriteResponses
    @PostMapping("/returns/{returnId}/reject")
    public ReturnResponse rejectReturn(@AuthenticationPrincipal AuthenticatedUser me,
                                       @PathVariable ReturnId returnId) {
        return returnMapper.toResponse(port.rejectReturn(me.userId(), returnId));
    }

    @Operation(summary = "List returns")
    @ListResponses
    @GetMapping("/returns")
    public List<ReturnResponse> findReturns(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findReturns(me.userId()).stream().map(returnMapper::toResponse).toList();
    }

    @Operation(summary = "Process refund")
    @WriteResponses
    @PostMapping("/refunds/{refundId}/process")
    public RefundResponse processRefund(@AuthenticationPrincipal AuthenticatedUser me,
                                        @PathVariable RefundId refundId) {
        return refundMapper.toResponse(port.processRefund(me.userId(), refundId));
    }

    @Operation(summary = "Reject refund")
    @WriteResponses
    @PostMapping("/refunds/{refundId}/reject")
    public RefundResponse rejectRefund(@AuthenticationPrincipal AuthenticatedUser me,
                                       @PathVariable RefundId refundId) {
        return refundMapper.toResponse(port.rejectRefund(me.userId(), refundId));
    }

    @Operation(summary = "List refunds")
    @ListResponses
    @GetMapping("/refunds")
    public List<RefundResponse> findRefunds(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findRefunds(me.userId()).stream().map(refundMapper::toResponse).toList();
    }
}
