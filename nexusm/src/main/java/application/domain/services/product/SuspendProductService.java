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
 * Temporarily removes a product from the catalog. The seller who owns it or an administrator can
 * do it.
 */
@DomainService
public class SuspendProductService {

    private final ProductRepository productRepository;

    public SuspendProductService(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository);
    }

    public Product execute(User actor, ProductId productId) {
        actor.requireAnyRole(UserRole.SELLER, UserRole.ADMINISTRATOR);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));
        if (actor.isSeller()) {
            product.requireOwnedBy(actor.getId());
        }
        product.suspend();
        return productRepository.save(product);
    }
}
