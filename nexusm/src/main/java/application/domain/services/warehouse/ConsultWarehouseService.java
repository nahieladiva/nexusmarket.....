package application.domain.services.warehouse;

import application.domain.enums.UserRole;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.WarehouseId;

import java.util.List;
import java.util.Objects;

/**
 * Warehouse queries: internal staff see every warehouse, and a seller sees their own warehouses.
 */
@DomainService
public class ConsultWarehouseService {

    private final WarehouseRepository warehouseRepository;

    public ConsultWarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = Objects.requireNonNull(warehouseRepository);
    }

    public Warehouse execute(User actor, WarehouseId warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId.toString()));
        if (!actor.getRole().isInternalStaff() && !warehouse.isOwnedBy(actor.getId())) {
            throw new ForbiddenActionException("You cannot see warehouse " + warehouseId);
        }
        return warehouse;
    }

    public List<Warehouse> findBySeller(User seller) {
        seller.requireRole(UserRole.SELLER);
        return warehouseRepository.findAll().stream()
            .filter(warehouse -> warehouse.isOwnedBy(seller.getId()))
            .toList();
    }

    public List<Warehouse> findAll(User actor) {
        actor.requireAnyRole(UserRole.ADMINISTRATOR, UserRole.SUPERVISOR, UserRole.LOGISTICS_OPERATOR);
        return warehouseRepository.findAll();
    }
}
