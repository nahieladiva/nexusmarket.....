package application.services;

import application.domain.events.ProductAddedEvent;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.in.ProductManagementPort;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.DomainEventPublisherPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.ProductRepository;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso del catálogo.
 *
 * <p>Reglas: solo vendedores aprobados publican; código único; precio mayor que cero;
 * solo el vendedor dueño edita, cambia precio o activa/desactiva; los productos inactivos
 * no aparecen en la búsqueda pública.</p>
 */
@Service
@Transactional
public class ProductApplicationService implements ProductManagementPort {

    private final ProductRepository productRepository;
    private final AccessRules accessRules;
    private final DomainEventPublisherPort eventPublisher;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public ProductApplicationService(ProductRepository productRepository, AccessRules accessRules,
                                     DomainEventPublisherPort eventPublisher, ClockPort clock,
                                     IdGeneratorPort idGenerator) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.accessRules = Objects.requireNonNull(accessRules);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public Product createProduct(UserId sellerId, ProductCode code, String name,
                                 String description, Money price) {
        User user = accessRules.requireUser(sellerId);
        if (!(user instanceof Seller seller)) {
            throw new BusinessRuleException("Solo los vendedores pueden publicar productos");
        }
        seller.requireApprovedToPublish();
        if (productRepository.findByCode(code).isPresent()) {
            throw new DuplicateResourceException("Ya existe un producto con el código " + code);
        }
        Product product = new Product(ProductId.of(idGenerator.newId()), code, name, description,
            price, sellerId, true, clock.now());
        Product saved = productRepository.save(product);
        eventPublisher.publish(new ProductAddedEvent(
            saved.getId(), saved.getCode(), saved.getSellerId(), clock.now()));
        return saved;
    }

    @Override
    public Product updateProduct(UserId sellerId, ProductId productId, String name,
                                 String description) {
        Product product = findProductById(productId);
        accessRules.requireOwner(sellerId, product);
        if (name != null) {
            product.rename(name);
        }
        if (description != null) {
            product.changeDescription(description);
        }
        return productRepository.save(product);
    }

    @Override
    public Product changePrice(UserId sellerId, ProductId productId, Money newPrice) {
        Product product = findProductById(productId);
        accessRules.requireOwner(sellerId, product);
        product.changePrice(newPrice);
        return productRepository.save(product);
    }

    @Override
    public Product changeAvailability(UserId sellerId, ProductId productId, boolean active) {
        Product product = findProductById(productId);
        accessRules.requireOwner(sellerId, product);
        if (active) {
            product.activate();
        } else {
            product.deactivate();
        }
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product findProductById(ProductId id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", id.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        List<Product> candidates = (keyword == null || keyword.isBlank())
            ? productRepository.findAll()
            : productRepository.findByNameContaining(keyword.trim());
        return candidates.stream().filter(Product::isActive).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findBySeller(UserId sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }
}
