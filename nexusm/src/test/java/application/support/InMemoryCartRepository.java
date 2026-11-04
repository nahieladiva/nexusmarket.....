package application.support;

import application.domain.models.Cart;
import application.domain.ports.out.CartRepository;
import application.domain.valueobjects.UserId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory cart repository for tests.
 */
public class InMemoryCartRepository implements CartRepository {

    private final Map<UserId, Cart> carts = new HashMap<>();

    @Override
    public Cart save(Cart cart) {
        carts.put(cart.getBuyerId(), cart);
        return cart;
    }

    @Override
    public Optional<Cart> findByBuyerId(UserId buyerId) {
        return Optional.ofNullable(carts.get(buyerId));
    }
}
