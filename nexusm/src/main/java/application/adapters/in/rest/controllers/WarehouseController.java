package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.InventoryMapper;
import application.adapters.in.rest.mappers.UserMapper;
import application.adapters.in.rest.mappers.WarehouseMapper;
import application.adapters.in.rest.requests.AvailabilityRequest;
import application.adapters.in.rest.requests.CreateWarehouseRequest;
import application.adapters.in.rest.requests.UpdateWarehouseRequest;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.ports.in.WarehouseManagementPort;
import application.domain.valueobjects.WarehouseId;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST del puerto de entrada {@link WarehouseManagementPort}.
 */
@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseManagementPort warehouseManagementPort;
    private final WarehouseMapper warehouseMapper;
    private final UserMapper userMapper;
    private final InventoryMapper inventoryMapper;

    public WarehouseController(WarehouseManagementPort warehouseManagementPort,
                               WarehouseMapper warehouseMapper, UserMapper userMapper,
                               InventoryMapper inventoryMapper) {
        this.warehouseManagementPort = warehouseManagementPort;
        this.warehouseMapper = warehouseMapper;
        this.userMapper = userMapper;
        this.inventoryMapper = inventoryMapper;
    }

    @PostMapping
    public ResponseEntity<WarehouseResponse> create(@RequestBody CreateWarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseMapper.toResponse(
            warehouseManagementPort.createWarehouse(request.name(),
                userMapper.toDomain(request.address()),
                inventoryMapper.toDomain(request.location()))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarehouseResponse> update(@PathVariable String id,
                                                    @RequestBody UpdateWarehouseRequest request) {
        return ResponseEntity.ok(warehouseMapper.toResponse(warehouseManagementPort.updateWarehouse(
            WarehouseId.of(id), request.name(),
            request.address() == null ? null : userMapper.toDomain(request.address()),
            request.location() == null ? null : inventoryMapper.toDomain(request.location()))));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<WarehouseResponse> changeStatus(@PathVariable String id,
                                                          @RequestBody AvailabilityRequest request) {
        return ResponseEntity.ok(warehouseMapper.toResponse(
            warehouseManagementPort.changeStatus(WarehouseId.of(id), request.active())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(warehouseMapper.toResponse(
            warehouseManagementPort.findWarehouseById(WarehouseId.of(id))));
    }

    @GetMapping
    public ResponseEntity<List<WarehouseResponse>> findAll() {
        return ResponseEntity.ok(warehouseManagementPort.findAllWarehouses().stream()
            .map(warehouseMapper::toResponse).toList());
    }
}
