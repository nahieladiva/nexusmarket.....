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
 * An administrator unblocks a product. It stays {@code SUSPENDED}, and the seller decides whether
 * to publish it again.
 */
@DomainService
public class UnblockProductService {

    private final ProductRepository productRepository;
    private final NotificationPort notificationPort;

    public UnblockProductService(ProductRepository productRepository, NotificationPort notificationPort) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.notificationPort = Objects.requireNonNull(notificationPort);
    }

    public Product execute(User administrator, ProductId productId) {
        administrator.requireRole(UserRole.ADMINISTRATOR);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));
        product.unblock();
        Product saved = productRepository.save(product);
        notificationPort.notify(saved.getSellerId(), "Product unblocked",
            "Product " + saved.getName() + " (" + saved.getCode() + ") was unblocked. You can publish it again.");
        return saved;
    }
}
