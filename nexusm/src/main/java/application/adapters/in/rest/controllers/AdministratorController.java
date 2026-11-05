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

    @PostMapping("/sellers")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerSeller(@AuthenticationPrincipal AuthenticatedUser me,
                                       @Valid @RequestBody CreateSellerRequest request) {
        return userMapper.toResponse(port.registerSeller(me.userId(), request.fullName(),
            Email.of(request.email()), PhoneNumber.of(request.phone()), request.password(),
            request.businessName()));
    }

    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerStaffUser(@AuthenticationPrincipal AuthenticatedUser me,
                                          @Valid @RequestBody CreateStaffUserRequest request) {
        return userMapper.toResponse(port.registerStaffUser(me.userId(), request.role(),
            request.fullName(), Email.of(request.email()), PhoneNumber.of(request.phone()),
            request.password()));
    }

    @PostMapping("/sellers/{sellerId}/approve")
    public UserResponse approveSeller(@AuthenticationPrincipal AuthenticatedUser me,
                                      @PathVariable UserId sellerId) {
        return userMapper.toResponse(port.approveSeller(me.userId(), sellerId));
    }

    @PostMapping("/sellers/{sellerId}/suspend")
    public UserResponse suspendSeller(@AuthenticationPrincipal AuthenticatedUser me,
                                      @PathVariable UserId sellerId) {
        return userMapper.toResponse(port.suspendSeller(me.userId(), sellerId));
    }

    @GetMapping("/users/{userId}")
    public UserResponse findUser(@AuthenticationPrincipal AuthenticatedUser me,
                                 @PathVariable UserId userId) {
        return userMapper.toResponse(port.findUser(me.userId(), userId));
    }

    @GetMapping("/users")
    public List<UserResponse> findUsers(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findUsers(me.userId()).stream().map(userMapper::toResponse).toList();
    }

    @PostMapping("/warehouses")
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponse createWarehouse(@AuthenticationPrincipal AuthenticatedUser me,
                                             @Valid @RequestBody CreateWarehouseRequest request) {
        return warehouseMapper.toResponse(port.createWarehouse(me.userId(), request.name(),
            userMapper.toDomain(request.address()), warehouseMapper.toDomain(request.location())));
    }

    @PutMapping("/warehouses/{warehouseId}")
    public WarehouseResponse updateWarehouse(@AuthenticationPrincipal AuthenticatedUser me,
                                             @PathVariable WarehouseId warehouseId,
                                             @Valid @RequestBody UpdateWarehouseRequest request) {
        return warehouseMapper.toResponse(port.updateWarehouse(me.userId(), warehouseId, request.name(),
            userMapper.toDomain(request.address()), warehouseMapper.toDomain(request.location())));
    }

    @PostMapping("/warehouses/{warehouseId}/activate")
    public WarehouseResponse activateWarehouse(@AuthenticationPrincipal AuthenticatedUser me,
                                               @PathVariable WarehouseId warehouseId) {
        return warehouseMapper.toResponse(port.activateWarehouse(me.userId(), warehouseId));
    }

    @PostMapping("/warehouses/{warehouseId}/deactivate")
    public WarehouseResponse deactivateWarehouse(@AuthenticationPrincipal AuthenticatedUser me,
                                                 @PathVariable WarehouseId warehouseId) {
        return warehouseMapper.toResponse(port.deactivateWarehouse(me.userId(), warehouseId));
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

    @PostMapping("/products/{productId}/block")
    public ProductResponse blockProduct(@AuthenticationPrincipal AuthenticatedUser me,
                                        @PathVariable ProductId productId) {
        return productMapper.toResponse(port.blockProduct(me.userId(), productId));
    }

    @PostMapping("/products/{productId}/unblock")
    public ProductResponse unblockProduct(@AuthenticationPrincipal AuthenticatedUser me,
                                          @PathVariable ProductId productId) {
        return productMapper.toResponse(port.unblockProduct(me.userId(), productId));
    }

    @PostMapping("/products/{productId}/discontinue")
    public ProductResponse discontinueProduct(@AuthenticationPrincipal AuthenticatedUser me,
                                              @PathVariable ProductId productId) {
        return productMapper.toResponse(port.discontinueProduct(me.userId(), productId));
    }

    @GetMapping("/products")
    public List<ProductResponse> findProducts(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findProducts(me.userId()).stream().map(productMapper::toResponse).toList();
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

    @PostMapping("/orders/{orderId}/cancel")
    public OrderResponse cancelOrder(@AuthenticationPrincipal AuthenticatedUser me,
                                     @PathVariable OrderId orderId) {
        return orderMapper.toResponse(port.cancelOrder(me.userId(), orderId));
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

    @PostMapping("/returns/{returnId}/approve")
    public ReturnResponse approveReturn(@AuthenticationPrincipal AuthenticatedUser me,
                                        @PathVariable ReturnId returnId) {
        return returnMapper.toResponse(port.approveReturn(me.userId(), returnId));
    }

    @PostMapping("/returns/{returnId}/reject")
    public ReturnResponse rejectReturn(@AuthenticationPrincipal AuthenticatedUser me,
                                       @PathVariable ReturnId returnId) {
        return returnMapper.toResponse(port.rejectReturn(me.userId(), returnId));
    }

    @GetMapping("/returns")
    public List<ReturnResponse> findReturns(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findReturns(me.userId()).stream().map(returnMapper::toResponse).toList();
    }

    @PostMapping("/refunds/{refundId}/process")
    public RefundResponse processRefund(@AuthenticationPrincipal AuthenticatedUser me,
                                        @PathVariable RefundId refundId) {
        return refundMapper.toResponse(port.processRefund(me.userId(), refundId));
    }

    @PostMapping("/refunds/{refundId}/reject")
    public RefundResponse rejectRefund(@AuthenticationPrincipal AuthenticatedUser me,
                                       @PathVariable RefundId refundId) {
        return refundMapper.toResponse(port.rejectRefund(me.userId(), refundId));
    }

    @GetMapping("/refunds")
    public List<RefundResponse> findRefunds(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findRefunds(me.userId()).stream().map(refundMapper::toResponse).toList();
    }
}
