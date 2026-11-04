package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.mappers.PromotionEntityMapper;
import application.adapters.out.persistence.mysql.repositories.PromotionJpaRepository;
import application.domain.models.Promotion;
import application.domain.ports.out.PromotionRepository;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.PromotionId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adaptador MySQL de {@link PromotionRepository}.
 */
@Component
public class PromotionPersistenceAdapter implements PromotionRepository {

    private final PromotionJpaRepository jpaRepository;
    private final PromotionEntityMapper mapper;

    public PromotionPersistenceAdapter(PromotionJpaRepository jpaRepository,
                                       PromotionEntityMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository es obligatorio");
        this.mapper = Objects.requireNonNull(mapper, "mapper es obligatorio");
    }

    @Override
    public Promotion save(Promotion promotion) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(promotion)));
    }

    @Override
    public Optional<Promotion> findById(PromotionId id) {
        return jpaRepository.findById(id.toString()).map(mapper::toDomain);
    }

    @Override
    public List<Promotion> findByProductId(ProductId productId) {
        return jpaRepository.findByProductId(productId.toString()).stream()
            .map(mapper::toDomain).toList();
    }

    @Override
    public List<Promotion> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
