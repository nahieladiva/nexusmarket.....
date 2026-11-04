package application.domain.models;

import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;

import java.util.Objects;

/**
 * Line of a shopping cart. It is immutable: changing the quantity creates a new instance.
 * The unit price is the catalog price at the moment the product was added.
 */
public final class CartItem {

    private final ProductId productId;
    private final Quantity quantity;
    private final Money unitPrice;

    public CartItem(ProductId productId, Quantity quantity, Money unitPrice) {
        this.productId = Objects.requireNonNull(productId, "Product is required");
        this.quantity = Objects.requireNonNull(quantity, "Quantity is required");
        if (quantity.isZero()) {
            throw new IllegalArgumentException("The quantity of a cart item must be greater than zero");
        }
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price is required");
    }

    public CartItem withQuantity(Quantity newQuantity) {
        return new CartItem(productId, newQuantity, unitPrice);
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity.getValue());
    }

    /**
     * Converts the cart line into an order line with the same product, quantity and price.
     */
    public OrderItem toOrderItem() {
        return new OrderItem(productId, quantity, unitPrice);
    }

    public ProductId getProductId() {
        return productId;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CartItem other)) {
            return false;
        }
        return productId.equals(other.productId) && quantity.equals(other.quantity)
            && unitPrice.equals(other.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, quantity, unitPrice);
    }
}
