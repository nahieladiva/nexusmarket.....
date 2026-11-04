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
 * Especialización de {@link User} con rol {@code SELLER}.
 *
 * <p>Agrega el nombre comercial y un estado de aprobación.</p>
 */
public class Seller extends User {

    private String businessName;
    private boolean approved;

    public Seller(UserId id, String fullName, Email email, PhoneNumber phone,
                  String passwordHash, String businessName,
                  List<Address> addresses, boolean approved, LocalDateTime createdAt) {
        super(id, fullName, email, phone, UserRole.SELLER, passwordHash, addresses, createdAt);
        this.businessName = requireNotBlank(businessName, "El nombre comercial es obligatorio");
        this.approved = approved;
    }

    public static Seller create(String fullName, Email email, PhoneNumber phone,
                                String passwordHash, String businessName) {
        return new Seller(UserId.random(), fullName, email, phone, passwordHash,
            businessName, List.of(), false, LocalDateTime.now());
    }

    public void approve() {
        this.approved = true;
    }

    public void changeBusinessName(String newBusinessName) {
        this.businessName = requireNotBlank(newBusinessName, "El nombre comercial es obligatorio");
    }

    public String getBusinessName() {
        return businessName;
    }

    public boolean isApproved() {
        return approved;
    }

    /**
     * Regla de negocio: solo vendedores aprobados pueden publicar productos.
     */
    public void requireApprovedToPublish() {
        if (!approved) {
            throw new IllegalStateException("El vendedor debe estar aprobado para publicar productos");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Seller seller = (Seller) o;
        return Objects.equals(getId(), seller.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}