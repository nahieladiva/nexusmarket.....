package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.CreateWarehouseRequest;
import application.adapters.in.rest.responses.AddressResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.models.Warehouse;
import application.domain.valueobjects.WarehouseId;
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

    public Warehouse toDomain(CreateWarehouseRequest request) {
        return Warehouse.createMarketplaceWarehouse(
            WarehouseId.random(),
            request.name(),
            userMapper.toDomain(request.address()),
            new WarehouseLocation(request.location().aisle(),
                request.location().shelf(), request.location().bin()));
    }

    public WarehouseResponse toResponse(Warehouse warehouse) {
        WarehouseLocation location = warehouse.getLocation();
        AddressResponse address = new AddressResponse(
            warehouse.getAddress().getStreet(),
            warehouse.getAddress().getCity(),
            warehouse.getAddress().getState(),
            warehouse.getAddress().getZipCode(),
            warehouse.getAddress().getCountry());
        return new WarehouseResponse(
            warehouse.getId().toString(),
            warehouse.getName(),
            address,
            location.getAisle(),
            location.getShelf(),
            location.getBin(),
            warehouse.isActive());
    }
}