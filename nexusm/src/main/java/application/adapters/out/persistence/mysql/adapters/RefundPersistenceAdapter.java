package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.mappers.RefundEntityMapper;
import application.adapters.out.persistence.mysql.repositories.RefundJpaRepository;
import application.domain.models.Refund;
import application.domain.ports.out.RefundRepository;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.RefundId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Output adapter that implements {@link RefundRepository} on MySQL.
 */
@Component
public class RefundPersistenceAdapter implements RefundRepository {

    private final RefundJpaRepository jpaRepository;
    private final RefundEntityMapper mapper;

    public RefundPersistenceAdapter(RefundJpaRepository jpaRepository, RefundEntityMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository is required");
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
    }

    @Override
    @Transactional
    public Refund save(Refund refund) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(refund)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Refund> findById(RefundId id) {
        return jpaRepository.findById(id.toString()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Refund> findByOrderId(OrderId orderId) {
        return jpaRepository.findByOrderId(orderId.toString()).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Refund> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
