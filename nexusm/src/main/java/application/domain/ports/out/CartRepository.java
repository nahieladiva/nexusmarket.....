package application.domain.ports.out;

import application.domain.models.Cart;
import application.domain.valueobjects.UserId;

import java.util.Optional;

/**
 * Output port — shopping carts (MySQL). Each buyer has one cart, so it is found by its buyer.
 */
public interface CartRepository {

    Cart save(Cart cart);

    Optional<Cart> findByBuyerId(UserId buyerId);
}
