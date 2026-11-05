package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.WarehouseLocationRequest;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.models.Warehouse;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseLocation;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Maps between REST DTOs and the warehouse domain model.
 */
@Component
public class WarehouseMapper {

    private final UserMapper userMapper;

    public WarehouseMapper(UserMapper userMapper) {
        this.userMapper = Objects.requireNonNull(userMapper, "User mapper is required");
    }

    /** Null stays null, because updates treat it as "do not change". */
    public WarehouseLocation toDomain(WarehouseLocationRequest location) {
        return location == null ? null
            : WarehouseLocation.of(location.aisle(), location.shelf(), location.bin());
    }

    public WarehouseResponse toResponse(Warehouse warehouse) {
        WarehouseLocation location = warehouse.getLocation();
        return new WarehouseResponse(
            warehouse.getId().toString(),
            warehouse.getType(),
            warehouse.getOwnerSellerId().map(UserId::toString).orElse(null),
            warehouse.getName(),
            userMapper.toResponse(warehouse.getAddress()),
            location.getAisle(),
            location.getShelf(),
            location.getBin(),
            warehouse.getStatus());
    }
}
