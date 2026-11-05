package application.domain.services.warehouse;

import application.domain.enums.UserRole;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.Objects;

/**
 * A seller creates their own warehouse.
 */
@DomainService
public class CreateSellerWarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ValidateWarehouseNameService validateWarehouseNameService;
    private final IdGeneratorPort idGenerator;

    public CreateSellerWarehouseService(WarehouseRepository warehouseRepository,
                                        ValidateWarehouseNameService validateWarehouseNameService,
                                        IdGeneratorPort idGenerator) {
        this.warehouseRepository = Objects.requireNonNull(warehouseRepository);
        this.validateWarehouseNameService = Objects.requireNonNull(validateWarehouseNameService);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public Warehouse execute(User seller, String name, Address address, WarehouseLocation location) {
        seller.requireRole(UserRole.SELLER);
        validateWarehouseNameService.execute(name, null);
        return warehouseRepository.save(Warehouse.createSellerWarehouse(
            WarehouseId.of(idGenerator.newId()), seller.getId(), name, address, location));
    }
}
