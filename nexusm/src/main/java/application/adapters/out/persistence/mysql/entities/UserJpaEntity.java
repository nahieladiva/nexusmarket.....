package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity of a user. Every role is stored in the same table
 * (SINGLE_TABLE) through the {@code role} column.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** Sellers only. */
    @Column(name = "business_name")
    private String businessName;

    /** Sellers only: name of {@link application.domain.enums.SellerStatus}. */
    @Column(name = "seller_status", length = 20)
    private String sellerStatus;

    /** Buyers only. */
    @Column(name = "shipping_street")
    private String shippingStreet;

    /** Buyers only. */
    @Column(name = "shipping_city")
    private String shippingCity;

    /** Buyers only. */
    @Column(name = "shipping_state")
    private String shippingState;

    /** Buyers only. */
    @Column(name = "shipping_zip")
    private String shippingZip;

    /** Buyers only. */
    @Column(name = "shipping_country")
    private String shippingCountry;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_addresses", joinColumns = @JoinColumn(name = "user_id"))
    private List<AddressEmbeddable> addresses = new ArrayList<>();

    public UserJpaEntity(String id, String fullName, String email, String phone,
                         String role, String passwordHash, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }
}