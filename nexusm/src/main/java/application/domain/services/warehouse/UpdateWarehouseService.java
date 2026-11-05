package application.domain.services.warehouse;

import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.Objects;

/**
 * Changes the data of a warehouse; a {@code null} value keeps the current one.
 */
@DomainService
public class UpdateWarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ValidateWarehouseNameService validateWarehouseNameService;

    public UpdateWarehouseService(WarehouseRepository warehouseRepository,
                                  ValidateWarehouseNameService validateWarehouseNameService) {
        this.warehouseRepository = Objects.requireNonNull(warehouseRepository);
        this.validateWarehouseNameService = Objects.requireNonNull(validateWarehouseNameService);
    }

    public Warehouse execute(User actor, WarehouseId warehouseId, String name, Address address,
                             WarehouseLocation location) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId.toString()));
        warehouse.requireManagedBy(actor);
        if (name != null) {
            validateWarehouseNameService.execute(name, warehouseId);
            warehouse.changeName(name);
        }
        if (address != null) {
            warehouse.changeAddress(address);
        }
        if (location != null) {
            warehouse.changeLocation(location);
        }
        return warehouseRepository.save(warehouse);
    }
}
