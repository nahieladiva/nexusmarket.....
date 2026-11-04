package application.domain.models;

import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de producto publicado por un vendedor.
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li>El SKU ({@code ProductCode}) y el id son inmutables y únicos.</li>
 *   <li>El precio no puede ser negativo (validado por {@link Money}).</li>
 *   <li>Solo se puede modificar precio, descripción y estado.</li>
 * </ul>
 */
public final class Product {

    private final ProductId id;
    private final ProductCode code;
    private String name;
    private String description;
    private Money price;
    private final UserId sellerId;
    private boolean active;
    private final LocalDateTime createdAt;

    public Product(ProductId id, ProductCode code, String name, String description,
                   Money price, UserId sellerId, boolean active, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "El id de producto es obligatorio");
        this.code = Objects.requireNonNull(code, "El código de producto es obligatorio");
        this.name = requireNotBlank(name, "El nombre del producto es obligatorio");
        this.description = description;
        this.price = requirePositive(price);
        this.sellerId = Objects.requireNonNull(sellerId, "El vendedor es obligatorio");
        this.active = active;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public static Product create(ProductCode code, String name, String description,
                                 Money price, UserId sellerId) {
        return new Product(ProductId.random(), code, name, description, price, sellerId,
            true, LocalDateTime.now());
    }

    private static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static Money requirePositive(Money price) {
        Objects.requireNonNull(price, "El precio es obligatorio");
        if (price.isZero()) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        return price;
    }

    public void changePrice(Money newPrice) {
        Money validated = requirePositive(newPrice);
        if (!validated.getCurrency().equals(this.price.getCurrency())) {
            throw new IllegalArgumentException(
                "La moneda del producto no se puede cambiar: " + this.price.getCurrency());
        }
        this.price = validated;
    }

    public void rename(String newName) {
        this.name = requireNotBlank(newName, "El nombre del producto es obligatorio");
    }

    /** Indica si el producto pertenece al vendedor dado. */
    public boolean isOwnedBy(UserId userId) {
        return this.sellerId.equals(userId);
    }

    public void changeDescription(String newDescription) {
        this.description = newDescription;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public ProductId getId() {
        return id;
    }

    public ProductCode getCode() {
        return code;
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

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}