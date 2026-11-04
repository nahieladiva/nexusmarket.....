package application.domain.models;

import application.domain.enums.WarehouseStatus;
import application.domain.enums.WarehouseType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.Objects;
import java.util.Optional;

/**
 * Physical warehouse where inventory is stored.
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>The id and the type are immutable.</li>
 *   <li>A {@code SELLER} warehouse always has an owner seller; a {@code MARKETPLACE}
 *       warehouse never has one.</li>
 *   <li>Only {@code ACTIVE} warehouses can receive stock or ship orders.</li>
 * </ul>
 */
public final class Warehouse {

    private final WarehouseId id;
    private final WarehouseType type;
    private final UserId ownerSellerId;
    private String name;
    private Address address;
    private WarehouseLocation location;
    private WarehouseStatus status;

    public Warehouse(WarehouseId id, WarehouseType type, UserId ownerSellerId, String name,
                     Address address, WarehouseLocation location, WarehouseStatus status) {
        this.id = Objects.requireNonNull(id, "Warehouse id is required");
        this.type = Objects.requireNonNull(type, "Warehouse type is required");
        if (type.requiresOwnerSeller() && ownerSellerId == null) {
            throw new BusinessRuleException("A seller warehouse requires an owner seller");
        }
        if (!type.requiresOwnerSeller() && ownerSellerId != null) {
            throw new BusinessRuleException("A marketplace warehouse cannot have an owner seller");
        }
        this.ownerSellerId = ownerSellerId;
        this.name = requireNotBlank(name, "Warehouse name is required");
        this.address = Objects.requireNonNull(address, "Address is required");
        this.location = Objects.requireNonNull(location, "Location is required");
        this.status = Objects.requireNonNull(status, "Warehouse status is required");
    }

    /**
     * Creates an active warehouse operated by the marketplace.
     */
    public static Warehouse createMarketplaceWarehouse(WarehouseId id, String name, Address address,
                                                       WarehouseLocation location) {
        return new Warehouse(id, WarehouseType.MARKETPLACE, null, name, address, location,
            WarehouseStatus.ACTIVE);
    }

    /**
     * Creates an active warehouse owned by a seller.
     */
    public static Warehouse createSellerWarehouse(WarehouseId id, UserId ownerSellerId, String name,
                                                  Address address, WarehouseLocation location) {
        return new Warehouse(id, WarehouseType.SELLER, ownerSellerId, name, address, location,
            WarehouseStatus.ACTIVE);
    }

    private static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    public void changeName(String newName) {
        this.name = requireNotBlank(newName, "Warehouse name is required");
    }

    public void changeAddress(Address newAddress) {
        this.address = Objects.requireNonNull(newAddress, "Address is required");
    }

    public void changeLocation(WarehouseLocation newLocation) {
        this.location = Objects.requireNonNull(newLocation, "Location is required");
    }

    /**
     * @throws BusinessRuleException when the warehouse is already active
     */
    public void activate() {
        status.requireCanTransitionTo(WarehouseStatus.ACTIVE);
        this.status = WarehouseStatus.ACTIVE;
    }

    /**
     * @throws BusinessRuleException when the warehouse is already inactive
     */
    public void deactivate() {
        status.requireCanTransitionTo(WarehouseStatus.INACTIVE);
        this.status = WarehouseStatus.INACTIVE;
    }

    /**
     * Tells whether the warehouse can store and ship inventory.
     */
    public boolean isActive() {
        return status.isOperational();
    }

    /**
     * Tells whether the given seller owns this warehouse (RG-03).
     */
    public boolean isOwnedBy(UserId sellerId) {
        return ownerSellerId != null && ownerSellerId.equals(sellerId);
    }

    public WarehouseId getId() {
        return id;
    }

    public WarehouseType getType() {
        return type;
    }

    public Optional<UserId> getOwnerSellerId() {
        return Optional.ofNullable(ownerSellerId);
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

    public WarehouseStatus getStatus() {
        return status;
    }
}
