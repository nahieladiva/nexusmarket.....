package application.domain.services.product;

import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.events.ProductAddedEvent;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.DomainEventPublisherPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.ProductRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;

import java.util.Objects;

/**
 * A seller registers a product in {@code DRAFT} status. It stays out of the catalog until it is
 * published.
 */
@DomainService
public class RegisterProductService {

    private final ProductRepository productRepository;
    private final DomainEventPublisherPort eventPublisher;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public RegisterProductService(ProductRepository productRepository,
                                  DomainEventPublisherPort eventPublisher, ClockPort clock,
                                  IdGeneratorPort idGenerator) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public Product execute(User seller, ProductCode code, ProductType type, String name,
                           String description, Money price) {
        seller.requireRole(UserRole.SELLER);
        if (productRepository.findByCode(code).isPresent()) {
            throw new DuplicateResourceException("A product with code " + code + " already exists");
        }
        Product saved = productRepository.save(Product.register(ProductId.of(idGenerator.newId()), code,
            type, name, description, price, seller.getId(), clock.now()));
        eventPublisher.publish(new ProductAddedEvent(
            saved.getId(), saved.getCode(), saved.getSellerId(), clock.now()));
        return saved;
    }
}
