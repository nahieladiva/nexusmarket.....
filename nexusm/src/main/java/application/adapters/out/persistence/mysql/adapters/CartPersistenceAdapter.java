package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.mappers.CartEntityMapper;
import application.adapters.out.persistence.mysql.repositories.CartJpaRepository;
import application.domain.models.Cart;
import application.domain.ports.out.CartRepository;
import application.domain.valueobjects.UserId;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Output adapter that implements {@link CartRepository} on MySQL.
 */
@Component
public class CartPersistenceAdapter implements CartRepository {

    private final CartJpaRepository jpaRepository;
    private final CartEntityMapper mapper;

    public CartPersistenceAdapter(CartJpaRepository jpaRepository, CartEntityMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository is required");
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
    }

    @Override
    @Transactional
    public Cart save(Cart cart) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(cart)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> findByBuyerId(UserId buyerId) {
        return jpaRepository.findByBuyerId(buyerId.toString()).map(mapper::toDomain);
    }
}
