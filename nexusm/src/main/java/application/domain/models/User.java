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
 * Base entity for every system user.
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>The id is immutable.</li>
 *   <li>The email is immutable and unique.</li>
 *   <li>The full name is required.</li>
 *   <li>The role is fixed and defines the specialization (RG-02): Buyer, Seller,
 *       LogisticsOperator, Administrator or Supervisor.</li>
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
        this.id = Objects.requireNonNull(id, "User id is required");
        this.fullName = requireNotBlank(fullName, "Full name is required");
        this.email = Objects.requireNonNull(email, "Email is required");
        this.phone = Objects.requireNonNull(phone, "Phone number is required");
        this.role = Objects.requireNonNull(role, "Role is required");
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
        this.fullName = requireNotBlank(newName, "Full name is required");
    }

    public void changePhone(PhoneNumber newPhone) {
        this.phone = Objects.requireNonNull(newPhone, "Phone number is required");
    }

    public void changePasswordHash(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void addAddress(Address address) {
        this.addresses.add(Objects.requireNonNull(address, "Address is required"));
    }

    /**
     * Removes a registered address.
     *
     * @throws IllegalArgumentException when the address is not registered for this user
     */
    public void removeAddress(Address address) {
        Objects.requireNonNull(address, "Address is required");
        if (!this.addresses.remove(address)) {
            throw new IllegalArgumentException("Address is not registered for this user");
        }
    }

    public boolean hasAddress(Address address) {
        return this.addresses.contains(address);
    }

    /**
     * Tells whether the user has the given role.
     */
    public boolean hasRole(UserRole expectedRole) {
        return role == expectedRole;
    }

    public boolean isBuyer() {
        return role == UserRole.BUYER;
    }

    public boolean isSeller() {
        return role == UserRole.SELLER;
    }

    public boolean isLogisticsOperator() {
        return role == UserRole.LOGISTICS_OPERATOR;
    }

    public boolean isAdministrator() {
        return role == UserRole.ADMINISTRATOR;
    }

    public boolean isSupervisor() {
        return role == UserRole.SUPERVISOR;
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
