package application.domain.ports.in;

import application.domain.models.Warehouse;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;
import java.util.List;

/**
 * Puerto de entrada 4/7 — bodegas del marketplace (solo administradores modifican).
 */
public interface WarehouseManagementPort {

    Warehouse createWarehouse(String name, Address address, WarehouseLocation location);

    Warehouse updateWarehouse(WarehouseId id, String name, Address address,
                              WarehouseLocation location);

    /** Una bodega inactiva no se usa para despachar pedidos. */
    Warehouse changeStatus(WarehouseId id, boolean active);

    Warehouse findWarehouseById(WarehouseId id);

    List<Warehouse> findAllWarehouses();
}
