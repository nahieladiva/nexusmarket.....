package application.domain.services.warehouse;

import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.WarehouseId;

import java.util.Objects;

/**
 * Deactivates a warehouse: it no longer receives stock or takes part in order allocation.
 */
@DomainService
public class DeactivateWarehouseService {

    private final WarehouseRepository warehouseRepository;

    public DeactivateWarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = Objects.requireNonNull(warehouseRepository);
    }

    public Warehouse execute(User actor, WarehouseId warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId.toString()));
        warehouse.requireManagedBy(actor);
        warehouse.deactivate();
        return warehouseRepository.save(warehouse);
    }
}
