package application.domain.models;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Product registered by a seller in the catalog.
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>The id, the code ({@link ProductCode}), the type and the seller are immutable.</li>
 *   <li>The price is greater than zero and its currency never changes.</li>
 *   <li>A new product starts as {@code DRAFT}; only {@code PUBLISHED} products can be sold.</li>
 *   <li>A {@code DISCONTINUED} product cannot be edited.</li>
 * </ul>
 */
public final class Product {

    private final ProductId id;
    private final ProductCode code;
    private final ProductType type;
    private String name;
    private String description;
    private Money price;
    private final UserId sellerId;
    private ProductStatus status;
    private final LocalDateTime createdAt;

    public Product(ProductId id, ProductCode code, ProductType type, String name, String description,
                   Money price, UserId sellerId, ProductStatus status, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "Product id is required");
        this.code = Objects.requireNonNull(code, "Product code is required");
        this.type = Objects.requireNonNull(type, "Product type is required");
        this.name = requireNotBlank(name, "Product name is required");
        this.description = description;
        this.price = requirePositive(price);
        this.sellerId = Objects.requireNonNull(sellerId, "Seller is required");
        this.status = Objects.requireNonNull(status, "Product status is required");
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    /**
     * Registers a new product in {@code DRAFT} status.
     */
    public static Product register(ProductId id, ProductCode code, ProductType type, String name,
                                   String description, Money price, UserId sellerId,
                                   LocalDateTime createdAt) {
        return new Product(id, code, type, name, description, price, sellerId,
            ProductStatus.DRAFT, createdAt);
    }

    private static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static Money requirePositive(Money price) {
        Objects.requireNonNull(price, "Price is required");
        if (price.isZero()) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        return price;
    }

    private void requireEditable() {
        if (status.isTerminal()) {
            throw new BusinessRuleException("A discontinued product cannot be modified");
        }
    }

    public void changePrice(Money newPrice) {
        requireEditable();
        Money validated = requirePositive(newPrice);
        if (!validated.getCurrency().equals(this.price.getCurrency())) {
            throw new IllegalArgumentException(
                "The product currency cannot be changed: " + this.price.getCurrency());
        }
        this.price = validated;
    }

    public void rename(String newName) {
        requireEditable();
        this.name = requireNotBlank(newName, "Product name is required");
    }

    public void changeDescription(String newDescription) {
        requireEditable();
        this.description = newDescription;
    }

    /**
     * Tells whether the product belongs to the given seller (RG-03).
     */
    public boolean isOwnedBy(UserId userId) {
        return this.sellerId.equals(userId);
    }

    /**
     * Makes the product visible in the public catalog.
     */
    public void publish() {
        changeStatus(ProductStatus.PUBLISHED);
    }

    /**
     * Temporarily removes the product from the public catalog.
     */
    public void suspend() {
        changeStatus(ProductStatus.SUSPENDED);
    }

    /**
     * Permanently removes the product from sale.
     */
    public void discontinue() {
        changeStatus(ProductStatus.DISCONTINUED);
    }

    private void changeStatus(ProductStatus target) {
        status.requireCanTransitionTo(target);
        this.status = target;
    }

    /**
     * Tells whether the product can be shown in the catalog and purchased.
     */
    public boolean isActive() {
        return status.isSellable();
    }

    public ProductId getId() {
        return id;
    }

    public ProductCode getCode() {
        return code;
    }

    public ProductType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Money getPrice() {
        return price;
    }

    public UserId getSellerId() {
        return sellerId;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
