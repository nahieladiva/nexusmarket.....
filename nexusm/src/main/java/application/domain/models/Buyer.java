package application.domain.models;

import application.domain.enums.UserRole;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Specialization of {@link User} with the {@code BUYER} role.
 *
 * <p>Adds the default shipping address used in orders.</p>
 */
public class Buyer extends User {

    private Address defaultShippingAddress;

    public Buyer(UserId id, String fullName, Email email, PhoneNumber phone,
                 String passwordHash, Address defaultShippingAddress,
                 List<Address> addresses, LocalDateTime createdAt) {
        super(id, fullName, email, phone, UserRole.BUYER, passwordHash, addresses, createdAt);
        this.defaultShippingAddress =
            Objects.requireNonNull(defaultShippingAddress, "Shipping address is required");
    }

    public static Buyer create(String fullName, Email email, PhoneNumber phone,
                               String passwordHash, Address defaultShippingAddress) {
        return new Buyer(UserId.random(), fullName, email, phone, passwordHash,
            defaultShippingAddress, List.of(), LocalDateTime.now());
    }

    public void changeDefaultShippingAddress(Address newAddress) {
        this.defaultShippingAddress =
            Objects.requireNonNull(newAddress, "Shipping address is required");
    }

    public Address getDefaultShippingAddress() {
        return defaultShippingAddress;
    }
}