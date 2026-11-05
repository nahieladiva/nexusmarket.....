package application.domain.services.product;

import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.out.NotificationPort;
import application.domain.ports.out.ProductRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.ProductId;

import java.util.Objects;

/**
 * An administrator blocks a product: it leaves the catalog, the seller cannot publish it again, and
 * the seller is notified.
 */
@DomainService
public class BlockProductService {

    private final ProductRepository productRepository;
    private final NotificationPort notificationPort;

    public BlockProductService(ProductRepository productRepository, NotificationPort notificationPort) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.notificationPort = Objects.requireNonNull(notificationPort);
    }

    public Product execute(User administrator, ProductId productId) {
        administrator.requireRole(UserRole.ADMINISTRATOR);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));
        product.block();
        Product saved = productRepository.save(product);
        notificationPort.notify(saved.getSellerId(), "Product blocked",
            "Product " + saved.getName() + " (" + saved.getCode() + ") was blocked by an administrator.");
        return saved;
    }
}
