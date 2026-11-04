package application.domain.models;

import application.domain.enums.ProductType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.valueobjects.CartId;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Shopping cart of a buyer (aggregate root).
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>It belongs to a single buyer, and each buyer has one cart.</li>
 *   <li>It never has two lines for the same product: adding an existing product
 *       increases its quantity.</li>
 *   <li>An empty cart cannot be turned into an order.</li>
 * </ul>
 */
public final class Cart {

    private final CartId id;
    private final UserId buyerId;
    private final Map<ProductId, CartItem> items;
    private LocalDateTime updatedAt;

    public Cart(CartId id, UserId buyerId, List<CartItem> items, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Cart id is required");
        this.buyerId = Objects.requireNonNull(buyerId, "Buyer is required");
        this.items = new LinkedHashMap<>();
        if (items != null) {
            items.forEach(this::merge);
        }
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    /**
     * Creates the empty cart of a buyer.
     */
    public static Cart createFor(CartId id, UserId buyerId, LocalDateTime now) {
        return new Cart(id, buyerId, List.of(), now);
    }

    /**
     * Adds a product; if it is already in the cart, its quantity is increased.
     */
    public void addItem(ProductId productId, ProductType productType, Quantity quantity, Money unitPrice) {
        merge(new CartItem(productId, productType, quantity, unitPrice));
        touch();
    }

    /**
     * Changes the quantity of a product; a quantity of zero removes it.
     */
    public void changeQuantity(ProductId productId, Quantity newQuantity) {
        CartItem current = requireItem(productId);
        if (newQuantity.isZero()) {
            items.remove(productId);
        } else {
            items.put(productId, current.withQuantity(newQuantity));
        }
        touch();
    }

    public void removeItem(ProductId productId) {
        requireItem(productId);
        items.remove(productId);
        touch();
    }

    public void clear() {
        items.clear();
        touch();
    }

    /**
     * Turns the cart into an order pending payment and empties the cart.
     *
     * @throws BusinessRuleException when the cart is empty
     */
    public Order checkout(OrderId orderId, LocalDateTime now) {
        if (items.isEmpty()) {
            throw new BusinessRuleException("An order cannot be created from an empty cart");
        }
        List<OrderItem> orderItems = new ArrayList<>();
        items.values().forEach(item -> orderItems.add(item.toOrderItem()));
        Order order = Order.create(orderId, buyerId, orderItems, now);
        clear();
        return order;
    }

    /**
     * Sum of the subtotals; empty when the cart has no items.
     */
    public Optional<Money> total() {
        Money result = null;
        for (CartItem item : items.values()) {
            result = (result == null) ? item.subtotal() : result.add(item.subtotal());
        }
        return Optional.ofNullable(result);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean belongsTo(UserId userId) {
        return buyerId.equals(userId);
    }

    private void merge(CartItem item) {
        Objects.requireNonNull(item, "Item is required");
        CartItem existing = items.get(item.getProductId());
        if (existing == null) {
            items.put(item.getProductId(), item);
        } else {
            items.put(item.getProductId(),
                existing.withQuantity(existing.getQuantity().add(item.getQuantity())));
        }
    }

    private CartItem requireItem(ProductId productId) {
        CartItem item = items.get(productId);
        if (item == null) {
            throw new BusinessRuleException("Product " + productId + " is not in the cart");
        }
        return item;
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public CartId getId() {
        return id;
    }

    public UserId getBuyerId() {
        return buyerId;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items.values()));
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
