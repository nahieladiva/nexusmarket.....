package application.services;

import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.PriceQuote;
import application.domain.models.Product;
import application.domain.models.Promotion;
import application.domain.ports.in.PromotionManagementPort;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.PromotionRepository;
import application.domain.services.PricingDomainService;
import application.domain.services.PromotionPolicy;
import application.domain.valueobjects.DateRange;
import application.domain.valueobjects.Percentage;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.PromotionId;
import application.domain.valueobjects.UserId;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de promociones. Las reglas están en {@link PromotionPolicy}.
 */
@Service
@Transactional
public class PromotionApplicationService implements PromotionManagementPort {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final PromotionPolicy promotionPolicy;
    private final PricingDomainService pricing;
    private final AccessRules accessRules;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public PromotionApplicationService(PromotionRepository promotionRepository,
                                       ProductRepository productRepository,
                                       PromotionPolicy promotionPolicy,
                                       PricingDomainService pricing,
                                       AccessRules accessRules,
                                       ClockPort clock,
                                       IdGeneratorPort idGenerator) {
        this.promotionRepository = Objects.requireNonNull(promotionRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
        this.promotionPolicy = Objects.requireNonNull(promotionPolicy);
        this.pricing = Objects.requireNonNull(pricing);
        this.accessRules = Objects.requireNonNull(accessRules);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public Promotion createPromotion(UserId sellerId, ProductId productId, Percentage discount,
                                     DateRange period) {
        Product product = requireProduct(productId);
        accessRules.requireOwner(sellerId, product);
        promotionPolicy.validateNew(discount, period, clock.today(),
            promotionRepository.findByProductId(productId));
        Promotion promotion = new Promotion(PromotionId.of(idGenerator.newId()), productId,
            sellerId, discount, period, false, clock.now());
        return promotionRepository.save(promotion);
    }

    @Override
    public Promotion endPromotion(UserId sellerId, PromotionId promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
            .orElseThrow(() -> new ResourceNotFoundException("Promoción", promotionId.toString()));
        if (!promotion.getCreatedBy().equals(sellerId)) {
            throw new ForbiddenActionException("Solo el vendedor que creó la promoción puede finalizarla");
        }
        promotion.end();
        return promotionRepository.save(promotion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promotion> findActivePromotions() {
        return promotionRepository.findAll().stream()
            .filter(promotion -> promotion.isEffectiveOn(clock.today()))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promotion> findByProduct(ProductId productId) {
        return promotionRepository.findByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public PriceQuote calculateFinalPrice(ProductId productId) {
        Product product = requireProduct(productId);
        return pricing.quote(product, promotionPolicy.effectiveOn(
            promotionRepository.findByProductId(productId), clock.today()));
    }

    private Product requireProduct(ProductId productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", productId.toString()));
    }
}
