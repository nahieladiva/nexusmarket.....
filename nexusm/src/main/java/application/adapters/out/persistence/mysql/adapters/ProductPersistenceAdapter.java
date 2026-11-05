package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.ProductJpaEntity;
import application.adapters.out.persistence.mysql.mappers.ProductEntityMapper;
import application.adapters.out.persistence.mysql.repositories.ProductJpaRepository;
import application.domain.models.Product;
import application.domain.ports.out.ProductRepository;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * Output adapter that implements {@link ProductRepository} on MySQL.
 */
@Component
public class ProductPersistenceAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final ProductEntityMapper mapper;

    public ProductPersistenceAdapter(ProductJpaRepository jpaRepository,
                                     ProductEntityMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository is required");
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = mapper.toEntity(product);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpaRepository.findById(id.toString()).map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findByCode(ProductCode code) {
        return jpaRepository.findByCode(code.getValue()).map(mapper::toDomain);
    }

    @Override
    public List<Product> findByNameContaining(String keyword) {
        return jpaRepository.findByNameContainingIgnoreCase(keyword).stream()
            .map(mapper::toDomain).toList();
    }

    @Override
    public List<Product> findBySellerId(UserId sellerId) {
        return jpaRepository.findBySellerId(sellerId.toString()).stream()
            .map(mapper::toDomain).toList();
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}