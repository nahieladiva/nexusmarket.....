package application.domain.services.cart;

import application.domain.models.Cart;
import application.domain.models.User;
import application.domain.ports.out.CartRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.ProductId;

import java.util.Objects;

/**
 * Removes a product from the cart of a buyer.
 */
@DomainService
public class RemoveItemFromCartService {

    private final CartRepository cartRepository;
    private final ConsultCartService consultCartService;

    public RemoveItemFromCartService(CartRepository cartRepository,
                                     ConsultCartService consultCartService) {
        this.cartRepository = Objects.requireNonNull(cartRepository);
        this.consultCartService = Objects.requireNonNull(consultCartService);
    }

    public Cart execute(User buyer, ProductId productId) {
        Cart cart = consultCartService.execute(buyer);
        cart.removeItem(productId);
        return cartRepository.save(cart);
    }
}
