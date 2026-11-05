package application.domain.models;

import application.domain.enums.SellerStatus;
import application.domain.enums.UserRole;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ResourceStateConflictException;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Specialization of {@link User} with the {@code SELLER} role.
 *
 * <p>Adds the business name and the commercial status of the seller.
 * A new seller starts as {@code PENDING_APPROVAL} and can only publish products
 * once it is {@code APPROVED}.</p>
 */
public class Seller extends User {

    private String businessName;
    private SellerStatus status;

    public Seller(UserId id, String fullName, Email email, PhoneNumber phone,
                  String passwordHash, String businessName,
                  List<Address> addresses, SellerStatus status, LocalDateTime createdAt) {
        super(id, fullName, email, phone, UserRole.SELLER, passwordHash, addresses, createdAt);
        this.businessName = requireNotBlank(businessName, "Business name is required");
        this.status = Objects.requireNonNull(status, "Seller status is required");
    }

    /**
     * Registers a new seller in {@code PENDING_APPROVAL} status.
     */
    public static Seller register(UserId id, String fullName, Email email, PhoneNumber phone,
                                  String passwordHash, String businessName, LocalDateTime createdAt) {
        return new Seller(id, fullName, email, phone, passwordHash, businessName, List.of(),
            SellerStatus.PENDING_APPROVAL, createdAt);
    }

    /**
     * Approves the seller (also used to lift a suspension).
     *
     * @throws BusinessRuleException when the seller is already approved
     */
    public void approve() {
        changeStatus(SellerStatus.APPROVED);
    }

    /**
     * Suspends an approved seller; it can no longer publish products.
     *
     * @throws BusinessRuleException when the seller is not approved
     */
    public void suspend() {
        changeStatus(SellerStatus.SUSPENDED);
    }

    private void changeStatus(SellerStatus target) {
        status.requireCanTransitionTo(target);
        this.status = target;
    }

    public void changeBusinessName(String newBusinessName) {
        this.businessName = requireNotBlank(newBusinessName, "Business name is required");
    }

    /**
     * Tells whether the seller is currently approved.
     */
    public boolean isApproved() {
        return status == SellerStatus.APPROVED;
    }

    /**
     * Business rule: only approved sellers can publish products.
     *
     * @throws BusinessRuleException when the seller is not approved
     */
    public void requireApprovedToPublish() {
        if (!status.canPublishProducts()) {
            throw new ResourceStateConflictException("Seller must be approved to publish products");
        }
    }

    public String getBusinessName() {
        return businessName;
    }

    public SellerStatus getStatus() {
        return status;
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
