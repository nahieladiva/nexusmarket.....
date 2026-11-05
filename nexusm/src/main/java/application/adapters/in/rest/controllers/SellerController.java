package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.InventoryMapper;
import application.adapters.in.rest.mappers.OrderMapper;
import application.adapters.in.rest.mappers.ProductMapper;
import application.adapters.in.rest.mappers.UserMapper;
import application.adapters.in.rest.mappers.WarehouseMapper;
import application.adapters.in.rest.requests.AdjustInventoryRequest;
import application.adapters.in.rest.requests.CreateInventoryRequest;
import application.adapters.in.rest.requests.CreateProductRequest;
import application.adapters.in.rest.requests.CreateWarehouseRequest;
import application.adapters.in.rest.requests.PriceChangeRequest;
import application.adapters.in.rest.requests.RestockRequest;
import application.adapters.in.rest.requests.ThresholdRequest;
import application.adapters.in.rest.requests.UpdateProductRequest;
import application.adapters.in.rest.requests.UpdateWarehouseRequest;
import application.adapters.in.rest.responses.InventoryResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.StockMovementResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.adapters.in.rest.security.AuthenticatedUser;
import application.domain.ports.in.SellerPort;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter of {@link SellerPort}. The seller always comes from the token.
 */
@RestController
@RequestMapping("/api/v1/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerPort port;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;
    private final InventoryMapper inventoryMapper;
    private final OrderMapper orderMapper;

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse registerProduct(@AuthenticationPrincipal AuthenticatedUser me,
                                           @Valid @RequestBody CreateProductRequest request) {
        return productMapper.toResponse(port.registerProduct(me.userId(), ProductCode.of(request.code()),
            request.type(), request.name(), request.description(),
            productMapper.toDomain(request.price())));
    }

    @PatchMapping("/products/{productId}")
    public ProductResponse updateProduct(@AuthenticationPrincipal AuthenticatedUser me,
                                         @PathVariable ProductId productId,
                                         @Valid @RequestBody UpdateProductRequest request) {
        return productMapper.toResponse(port.updateProduct(me.userId(), productId, request.name(),
            request.description()));
    }

    @PutMapping("/products/{productId}/price")
    public ProductResponse changeProductPrice(@AuthenticationPrincipal AuthenticatedUser me,
                                              @PathVariable ProductId productId,
                                              @Valid @RequestBody PriceChangeRequest request) {
        return productMapper.toResponse(port.changeProductPrice(me.userId(), productId,
            productMapper.toDomain(request.price())));
    }

    @PostMapping("/products/{productId}/publish")
    public ProductResponse publishProduct(@AuthenticationPrincipal AuthenticatedUser me,
                                          @PathVariable ProductId productId) {
        return productMapper.toResponse(port.publishProduct(me.userId(), productId));
    }

    @PostMapping("/products/{productId}/suspend")
    public ProductResponse suspendProduct(@AuthenticationPrincipal AuthenticatedUser me,
                                          @PathVariable ProductId productId) {
        return productMapper.toResponse(port.suspendProduct(me.userId(), productId));
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

    @PostMapping("/inventory")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse createInventory(@AuthenticationPrincipal AuthenticatedUser me,
                                             @Valid @RequestBody CreateInventoryRequest request) {
        return inventoryMapper.toResponse(port.createInventory(me.userId(),
            ProductId.of(request.productId()), WarehouseId.of(request.warehouseId()),
            Quantity.of(request.onHand()), Quantity.of(request.reorderThreshold()),
            warehouseMapper.toDomain(request.location())));
    }

    @PostMapping("/inventory/restock")
    public InventoryResponse restock(@AuthenticationPrincipal AuthenticatedUser me,
                                     @Valid @RequestBody RestockRequest request) {
        return inventoryMapper.toResponse(port.restock(me.userId(), ProductId.of(request.productId()),
            WarehouseId.of(request.warehouseId()), Quantity.of(request.quantity()), request.reason()));
    }

    @PostMapping("/inventory/adjust")
    public InventoryResponse adjustStock(@AuthenticationPrincipal AuthenticatedUser me,
                                         @Valid @RequestBody AdjustInventoryRequest request) {
        return inventoryMapper.toResponse(port.adjustStock(me.userId(), ProductId.of(request.productId()),
            WarehouseId.of(request.warehouseId()), request.delta(), request.reason()));
    }

    @PutMapping("/inventory/threshold")
    public InventoryResponse changeReorderThreshold(@AuthenticationPrincipal AuthenticatedUser me,
                                                    @Valid @RequestBody ThresholdRequest request) {
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

    @GetMapping("/inventory")
    public List<InventoryResponse> findInventories(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findInventories(me.userId()).stream().map(inventoryMapper::toResponse).toList();
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
}
