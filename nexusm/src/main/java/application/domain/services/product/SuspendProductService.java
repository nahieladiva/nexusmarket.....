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
 * The seller who owns a product temporarily removes it from the catalog.
 */
@DomainService
public class SuspendProductService {

    private final ProductRepository productRepository;

    public SuspendProductService(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository);
    }

    public Product execute(User seller, ProductId productId) {
        seller.requireRole(UserRole.SELLER);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));
        product.requireOwnedBy(seller.getId());
        product.suspend();
        return productRepository.save(product);
    }
}
