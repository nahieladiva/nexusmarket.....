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
 * Especialización de {@link User} con rol {@code BUYER}.
 *
 * <p>Agrega la dirección de envío predeterminada utilizada en las órdenes.</p>
 */
public class Buyer extends User {

    private Address defaultShippingAddress;

    public Buyer(UserId id, String fullName, Email email, PhoneNumber phone,
                 String passwordHash, Address defaultShippingAddress,
                 List<Address> addresses, LocalDateTime createdAt) {
        super(id, fullName, email, phone, UserRole.BUYER, passwordHash, addresses, createdAt);
        this.defaultShippingAddress =
            Objects.requireNonNull(defaultShippingAddress, "La dirección de envío es obligatoria");
    }

    public static Buyer create(String fullName, Email email, PhoneNumber phone,
                               String passwordHash, Address defaultShippingAddress) {
        return new Buyer(UserId.random(), fullName, email, phone, passwordHash,
            defaultShippingAddress, List.of(), LocalDateTime.now());
    }

    public void changeDefaultShippingAddress(Address newAddress) {
        this.defaultShippingAddress =
            Objects.requireNonNull(newAddress, "La dirección de envío es obligatoria");
    }

    public Address getDefaultShippingAddress() {
        return defaultShippingAddress;
    }
}