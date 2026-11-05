package application.domain.services.warehouse;

import application.domain.exceptions.DuplicateResourceException;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.WarehouseId;

import java.util.Objects;

/**
 * Validates that no other warehouse uses a name, ignoring upper and lower case.
 */
@DomainService
public class ValidateWarehouseNameService {

    private final WarehouseRepository warehouseRepository;

    public ValidateWarehouseNameService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = Objects.requireNonNull(warehouseRepository);
    }

    /**
     * @param ignoredId warehouse that may keep the name; {@code null} for a new warehouse
     */
    public void execute(String name, WarehouseId ignoredId) {
        if (name == null) {
            return;
        }
        boolean taken = warehouseRepository.findByNameContaining(name.trim()).stream()
            .anyMatch(warehouse -> warehouse.getName().equalsIgnoreCase(name.trim())
                && !warehouse.getId().equals(ignoredId));
        if (taken) {
            throw new DuplicateResourceException("A warehouse named " + name.trim() + " already exists");
        }
    }
}
