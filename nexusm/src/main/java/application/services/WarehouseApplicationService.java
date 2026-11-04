package application.services;

import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Warehouse;
import application.domain.ports.in.WarehouseManagementPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.WarehouseRepository;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de bodegas. Regla: el nombre de bodega es único (sin distinguir mayúsculas).
 */
@Service
@Transactional
public class WarehouseApplicationService implements WarehouseManagementPort {

    private final WarehouseRepository warehouseRepository;
    private final IdGeneratorPort idGenerator;

    public WarehouseApplicationService(WarehouseRepository warehouseRepository,
                                       IdGeneratorPort idGenerator) {
        this.warehouseRepository = Objects.requireNonNull(warehouseRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public Warehouse createWarehouse(String name, Address address, WarehouseLocation location) {
        requireNameAvailable(name, null);
        Warehouse warehouse = new Warehouse(WarehouseId.of(idGenerator.newId()), name, address,
            location, true);
        return warehouseRepository.save(warehouse);
    }

    @Override
    public Warehouse updateWarehouse(WarehouseId id, String name, Address address,
                                     WarehouseLocation location) {
        Warehouse warehouse = findWarehouseById(id);
        if (name != null) {
            requireNameAvailable(name, id);
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

    @Override
    public Warehouse changeStatus(WarehouseId id, boolean active) {
        Warehouse warehouse = findWarehouseById(id);
        if (active) {
            warehouse.activate();
        } else {
            warehouse.deactivate();
        }
        return warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public Warehouse findWarehouseById(WarehouseId id) {
        return warehouseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bodega", id.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Warehouse> findAllWarehouses() {
        return warehouseRepository.findAll();
    }

    private void requireNameAvailable(String name, WarehouseId ignore) {
        if (name == null) {
            return;
        }
        boolean taken = warehouseRepository.findByNameContaining(name.trim()).stream()
            .anyMatch(w -> w.getName().equalsIgnoreCase(name.trim()) && !w.getId().equals(ignore));
        if (taken) {
            throw new DuplicateResourceException("Ya existe una bodega llamada " + name.trim());
        }
    }
}
