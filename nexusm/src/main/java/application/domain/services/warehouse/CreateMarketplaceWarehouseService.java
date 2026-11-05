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
 * An administrator creates a warehouse operated by the marketplace.
 */
@DomainService
public class CreateMarketplaceWarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ValidateWarehouseNameService validateWarehouseNameService;
    private final IdGeneratorPort idGenerator;

    public CreateMarketplaceWarehouseService(WarehouseRepository warehouseRepository,
                                             ValidateWarehouseNameService validateWarehouseNameService,
                                             IdGeneratorPort idGenerator) {
        this.warehouseRepository = Objects.requireNonNull(warehouseRepository);
        this.validateWarehouseNameService = Objects.requireNonNull(validateWarehouseNameService);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public Warehouse execute(User administrator, String name, Address address,
                             WarehouseLocation location) {
        administrator.requireRole(UserRole.ADMINISTRATOR);
        validateWarehouseNameService.execute(name, null);
        return warehouseRepository.save(Warehouse.createMarketplaceWarehouse(
            WarehouseId.of(idGenerator.newId()), name, address, location));
    }
}
