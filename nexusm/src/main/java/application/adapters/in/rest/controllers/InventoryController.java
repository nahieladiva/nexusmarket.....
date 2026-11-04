package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.InventoryMapper;
import application.adapters.in.rest.requests.AdjustInventoryRequest;
import application.adapters.in.rest.requests.CreateInventoryRequest;
import application.adapters.in.rest.requests.RestockRequest;
import application.adapters.in.rest.requests.ThresholdRequest;
import application.adapters.in.rest.responses.InventoryResponse;
import application.adapters.in.rest.responses.StockMovementResponse;
import application.adapters.in.rest.security.AuthenticatedUser;
import application.domain.ports.in.InventoryManagementPort;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST del puerto de entrada {@link InventoryManagementPort}.
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryManagementPort inventoryManagementPort;
    private final InventoryMapper inventoryMapper;

    public InventoryController(InventoryManagementPort inventoryManagementPort,
                               InventoryMapper inventoryMapper) {
        this.inventoryManagementPort = inventoryManagementPort;
        this.inventoryMapper = inventoryMapper;
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> create(@AuthenticationPrincipal AuthenticatedUser me,
                                                    @RequestBody CreateInventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryMapper.toResponse(
            inventoryManagementPort.createInventory(me.userId(),
                ProductId.of(request.productId()), WarehouseId.of(request.warehouseId()),
                Quantity.of(request.onHand()), Quantity.of(request.reorderThreshold()),
                inventoryMapper.toDomain(request.location()))));
    }

    @PatchMapping("/restock")
    public ResponseEntity<InventoryResponse> restock(@AuthenticationPrincipal AuthenticatedUser me,
                                                     @RequestBody RestockRequest request) {
        return ResponseEntity.ok(inventoryMapper.toResponse(inventoryManagementPort.restock(
            me.userId(), ProductId.of(request.productId()), WarehouseId.of(request.warehouseId()),
            Quantity.of(request.quantity()), request.reason())));
    }

    @PatchMapping("/adjust")
    public ResponseEntity<InventoryResponse> adjust(@AuthenticationPrincipal AuthenticatedUser me,
                                                    @RequestBody AdjustInventoryRequest request) {
        return ResponseEntity.ok(inventoryMapper.toResponse(inventoryManagementPort.adjustStock(
            me.userId(), ProductId.of(request.productId()), WarehouseId.of(request.warehouseId()),
            request.delta(), request.reason())));
    }

    @PatchMapping("/threshold")
    public ResponseEntity<InventoryResponse> changeThreshold(@AuthenticationPrincipal AuthenticatedUser me,
                                                             @RequestBody ThresholdRequest request) {
        return ResponseEntity.ok(inventoryMapper.toResponse(inventoryManagementPort.changeReorderThreshold(
            me.userId(), ProductId.of(request.productId()), WarehouseId.of(request.warehouseId()),
            Quantity.of(request.reorderThreshold()))));
    }

    @GetMapping
    public ResponseEntity<InventoryResponse> find(@RequestParam String productId,
                                                  @RequestParam String warehouseId) {
        return ResponseEntity.ok(inventoryMapper.toResponse(inventoryManagementPort.findInventory(
            ProductId.of(productId), WarehouseId.of(warehouseId))));
    }

    @GetMapping("/availability")
    public ResponseEntity<Map<String, Boolean>> availability(@RequestParam String productId,
                                                             @RequestParam String warehouseId,
                                                             @RequestParam int quantity) {
        return ResponseEntity.ok(Map.of("available", inventoryManagementPort.isAvailable(
            ProductId.of(productId), WarehouseId.of(warehouseId), Quantity.of(quantity))));
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryResponse>> byWarehouse(@PathVariable String warehouseId) {
        return ResponseEntity.ok(inventoryManagementPort.findByWarehouse(WarehouseId.of(warehouseId))
            .stream().map(inventoryMapper::toResponse).toList());
    }

    @GetMapping("/all")
    public ResponseEntity<List<InventoryResponse>> all() {
        return ResponseEntity.ok(inventoryManagementPort.findAllInventory().stream()
            .map(inventoryMapper::toResponse).toList());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> lowStock() {
        return ResponseEntity.ok(inventoryManagementPort.findBelowReorderPoint().stream()
            .map(inventoryMapper::toResponse).toList());
    }

    @GetMapping("/movements")
    public ResponseEntity<List<StockMovementResponse>> movements(@RequestParam String productId,
                                                                 @RequestParam String warehouseId) {
        return ResponseEntity.ok(inventoryManagementPort.findMovements(
                ProductId.of(productId), WarehouseId.of(warehouseId)).stream()
            .map(inventoryMapper::toResponse).toList());
    }
}
