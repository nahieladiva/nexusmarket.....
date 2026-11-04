package application.domain.models;

import application.domain.valueobjects.Address;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.Objects;

/**
 * Entidad de almacén físico.
 */
public final class Warehouse {

    private final WarehouseId id;
    private String name;
    private Address address;
    private WarehouseLocation location;
    private boolean active;

    public Warehouse(WarehouseId id, String name, Address address,
                     WarehouseLocation location, boolean active) {
        this.id = Objects.requireNonNull(id, "El id de almacén es obligatorio");
        this.name = requireNotBlank(name, "El nombre del almacén es obligatorio");
        this.address = Objects.requireNonNull(address, "La dirección es obligatoria");
        this.location = Objects.requireNonNull(location, "La ubicación es obligatoria");
        this.active = active;
    }

    public static Warehouse create(String name, Address address, WarehouseLocation location) {
        return new Warehouse(WarehouseId.random(), name, address, location, true);
    }

    private static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    public void changeName(String newName) {
        this.name = requireNotBlank(newName, "El nombre del almacén es obligatorio");
    }

    public void changeAddress(Address newAddress) {
        this.address = Objects.requireNonNull(newAddress, "La dirección es obligatoria");
    }

    public void changeLocation(WarehouseLocation newLocation) {
        this.location = Objects.requireNonNull(newLocation, "La ubicación es obligatoria");
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public WarehouseId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public WarehouseLocation getLocation() {
        return location;
    }

    public boolean isActive() {
        return active;
    }
}