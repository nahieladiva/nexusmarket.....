package application.domain.models;

import application.domain.enums.UserRole;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidad base de usuario del sistema.
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li>El id es inmutable.</li>
 *   <li>El email es inmutable y único.</li>
 *   <li>El nombre completo es obligatorio.</li>
 *   <li>El rol define la especialización (Buyer / Seller / Admin).</li>
 * </ul>
 */
public class User {

    private final UserId id;
    private String fullName;
    private final Email email;
    private PhoneNumber phone;
    private final UserRole role;
    private String passwordHash;
    private final List<Address> addresses;
    private final LocalDateTime createdAt;

    protected User(UserId id, String fullName, Email email, PhoneNumber phone,
                   UserRole role, String passwordHash, List<Address> addresses,
                   LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "El id de usuario es obligatorio");
        this.fullName = requireNotBlank(fullName, "El nombre completo es obligatorio");
        this.email = Objects.requireNonNull(email, "El email es obligatorio");
        this.phone = Objects.requireNonNull(phone, "El teléfono es obligatorio");
        this.role = Objects.requireNonNull(role, "El rol es obligatorio");
        this.passwordHash = passwordHash;
        this.addresses = new ArrayList<>(addresses == null ? List.of() : addresses);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    protected static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    public void changeName(String newName) {
        this.fullName = requireNotBlank(newName, "El nombre completo es obligatorio");
    }

    public void changePhone(PhoneNumber newPhone) {
        this.phone = Objects.requireNonNull(newPhone, "El teléfono es obligatorio");
    }

    public void changePasswordHash(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void addAddress(Address address) {
        this.addresses.add(Objects.requireNonNull(address, "La dirección es obligatoria"));
    }

    /**
     * Quita una dirección registrada.
     *
     * @throws IllegalArgumentException si la dirección no está registrada
     */
    public void removeAddress(Address address) {
        Objects.requireNonNull(address, "La dirección es obligatoria");
        if (!this.addresses.remove(address)) {
            throw new IllegalArgumentException("La dirección no está registrada para este usuario");
        }
    }

    public boolean hasAddress(Address address) {
        return this.addresses.contains(address);
    }

    public boolean isBuyer() {
        return role == UserRole.BUYER;
    }

    public boolean isSeller() {
        return role == UserRole.SELLER;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public UserId getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Email getEmail() {
        return email;
    }

    public PhoneNumber getPhone() {
        return phone;
    }

    public UserRole getRole() {
        return role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}