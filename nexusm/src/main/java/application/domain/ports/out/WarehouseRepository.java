package application.domain.ports.out;

import application.domain.models.Warehouse;
import application.domain.valueobjects.WarehouseId;

import java.util.List;
import java.util.Optional;

/**
 * Output port 3/15 — warehouses (MySQL).
 */
public interface WarehouseRepository {

    Warehouse save(Warehouse warehouse);

    Optional<Warehouse> findById(WarehouseId id);

    List<Warehouse> findByNameContaining(String keyword);

    List<Warehouse> findAll();
}