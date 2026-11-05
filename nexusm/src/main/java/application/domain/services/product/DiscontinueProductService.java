package application.domain.services.product;

import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.out.ProductRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.ProductId;

import java.util.Objects;

/**
 * Permanently removes a product from sale. The seller who owns it or an administrator can do it.
 */
@DomainService
public class DiscontinueProductService {

    private final ProductRepository productRepository;

    public DiscontinueProductService(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository);
    }

    public Product execute(User actor, ProductId productId) {
        actor.requireAnyRole(UserRole.SELLER, UserRole.ADMINISTRATOR);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));
        if (actor.isSeller()) {
            product.requireOwnedBy(actor.getId());
        }
        product.discontinue();
        return productRepository.save(product);
    }
}
