package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.OrderJpaEntity;
import application.adapters.out.persistence.mysql.mappers.OrderEntityMapper;
import application.adapters.out.persistence.mysql.repositories.OrderJpaRepository;
import application.domain.models.Order;
import application.domain.ports.out.OrderRepository;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.UserId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Output adapter that implements {@link OrderRepository} on MySQL.
 */
@Component
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderEntityMapper mapper;

    public OrderPersistenceAdapter(OrderJpaRepository jpaRepository, OrderEntityMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository is required");
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderJpaEntity entity = mapper.toEntity(order);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findById(id.toString()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByBuyerId(UserId buyerId) {
        return jpaRepository.findByBuyerId(buyerId.toString()).stream()
            .map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}