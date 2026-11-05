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
 * The seller who owns a product changes its name or description; a {@code null} value keeps the
 * current one.
 */
@DomainService
public class UpdateProductService {

    private final ProductRepository productRepository;

    public UpdateProductService(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository);
    }

    public Product execute(User seller, ProductId productId, String name, String description) {
        seller.requireRole(UserRole.SELLER);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));
        product.requireOwnedBy(seller.getId());
        if (name != null) {
            product.rename(name);
        }
        if (description != null) {
            product.changeDescription(description);
        }
        return productRepository.save(product);
    }
}
