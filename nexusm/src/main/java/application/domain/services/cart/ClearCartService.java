package application.domain.services.cart;

import application.domain.models.Cart;
import application.domain.models.User;
import application.domain.ports.out.CartRepository;
import application.domain.services.DomainService;

import java.util.Objects;

/**
 * Empties the cart of a buyer.
 */
@DomainService
public class ClearCartService {

    private final CartRepository cartRepository;
    private final ConsultCartService consultCartService;

    public ClearCartService(CartRepository cartRepository, ConsultCartService consultCartService) {
        this.cartRepository = Objects.requireNonNull(cartRepository);
        this.consultCartService = Objects.requireNonNull(consultCartService);
    }

    public Cart execute(User buyer) {
        Cart cart = consultCartService.execute(buyer);
        cart.clear();
        return cartRepository.save(cart);
    }
}
