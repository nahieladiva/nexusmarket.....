package application.domain.services.product;

import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.out.ProductRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.ProductId;

import java.util.List;
import java.util.Objects;

/**
 * Product queries. The public catalog shows only available products, a seller sees all of their
 * products, and internal staff see every product.
 */
@DomainService
public class ConsultProductService {

    private final ProductRepository productRepository;
    private final ValidateProductAvailabilityService validateProductAvailabilityService;

    public ConsultProductService(ProductRepository productRepository,
                                 ValidateProductAvailabilityService validateProductAvailabilityService) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.validateProductAvailabilityService = Objects.requireNonNull(validateProductAvailabilityService);
    }

    public Product findPublished(ProductId productId) {
        return productRepository.findById(productId)
            .filter(validateProductAvailabilityService::isAvailable)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));
    }

    public List<Product> searchPublished(String keyword) {
        List<Product> candidates = (keyword == null || keyword.isBlank())
            ? productRepository.findAll()
            : productRepository.findByNameContaining(keyword.trim());
        return candidates.stream().filter(validateProductAvailabilityService::isAvailable).toList();
    }

    public List<Product> findBySeller(User seller) {
        seller.requireRole(UserRole.SELLER);
        return productRepository.findBySellerId(seller.getId());
    }

    public List<Product> findAll(User actor) {
        actor.requireAnyRole(UserRole.ADMINISTRATOR, UserRole.SUPERVISOR, UserRole.LOGISTICS_OPERATOR);
        return productRepository.findAll();
    }
}
