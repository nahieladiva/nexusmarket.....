package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.CreateWarehouseRequest;
import application.adapters.in.rest.responses.AddressResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.models.Warehouse;
import application.domain.valueobjects.WarehouseLocation;

import java.util.Objects;

import org.springframework.stereotype.Component;

/**
 * Mapea entre DTOs REST y el modelo de dominio de almacenes.
 */
@Component
public class WarehouseMapper {

    private final UserMapper userMapper;

    public WarehouseMapper(UserMapper userMapper) {
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper es obligatorio");
    }

    public Warehouse toDomain(CreateWarehouseRequest request) {
        return Warehouse.create(
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