package application.domain.services.cart;

import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Cart;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.out.CartRepository;
import application.domain.ports.out.ProductRepository;
import application.domain.services.DomainService;
import application.domain.services.product.ValidateProductAvailabilityService;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;

import java.util.Objects;

/**
 * Adds a product to the cart of a buyer. Only available products are accepted, and the unit
 * price comes from the catalog, never from the client.
 */
@DomainService
public class AddItemToCartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ConsultCartService consultCartService;
    private final ValidateProductAvailabilityService validateProductAvailabilityService;

    public AddItemToCartService(CartRepository cartRepository, ProductRepository productRepository,
                                ConsultCartService consultCartService,
                                ValidateProductAvailabilityService validateProductAvailabilityService) {
        this.cartRepository = Objects.requireNonNull(cartRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
        this.consultCartService = Objects.requireNonNull(consultCartService);
        this.validateProductAvailabilityService = Objects.requireNonNull(validateProductAvailabilityService);
    }

    public Cart execute(User buyer, ProductId productId, Quantity quantity) {
        Cart cart = consultCartService.execute(buyer);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));
        validateProductAvailabilityService.execute(product);
        cart.addItem(productId, product.getType(), quantity, product.getPrice());
        return cartRepository.save(cart);
    }
}
