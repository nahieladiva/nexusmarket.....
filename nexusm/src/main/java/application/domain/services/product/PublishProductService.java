package application.domain.services.product;

import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.ProductRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.ProductId;

import java.util.Objects;

/**
 * The approved seller who owns a product publishes it in the catalog.
 */
@DomainService
public class PublishProductService {

    private final ProductRepository productRepository;

    public PublishProductService(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository);
    }

    public Product execute(User actor, ProductId productId) {
        actor.requireRole(UserRole.SELLER);
        ((Seller) actor).requireApprovedToPublish();
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));
        product.requireOwnedBy(actor.getId());
        product.publish();
        return productRepository.save(product);
    }
}
