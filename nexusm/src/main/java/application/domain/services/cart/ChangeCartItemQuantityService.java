package application.domain.services.cart;

import application.domain.models.Cart;
import application.domain.models.User;
import application.domain.ports.out.CartRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;

import java.util.Objects;

/**
 * Changes the quantity of a product in the cart of a buyer; zero removes the product.
 */
@DomainService
public class ChangeCartItemQuantityService {

    private final CartRepository cartRepository;
    private final ConsultCartService consultCartService;

    public ChangeCartItemQuantityService(CartRepository cartRepository,
                                         ConsultCartService consultCartService) {
        this.cartRepository = Objects.requireNonNull(cartRepository);
        this.consultCartService = Objects.requireNonNull(consultCartService);
    }

    public Cart execute(User buyer, ProductId productId, Quantity newQuantity) {
        Cart cart = consultCartService.execute(buyer);
        cart.changeQuantity(productId, newQuantity);
        return cartRepository.save(cart);
    }
}
