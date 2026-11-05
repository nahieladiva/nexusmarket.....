package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.mappers.InvoiceEntityMapper;
import application.adapters.out.persistence.mysql.repositories.InvoiceJpaRepository;
import application.domain.models.Invoice;
import application.domain.ports.out.InvoiceRepository;
import application.domain.valueobjects.InvoiceId;
import application.domain.valueobjects.OrderId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Output adapter that implements {@link InvoiceRepository} on MySQL.
 */
@Component
public class InvoicePersistenceAdapter implements InvoiceRepository {

    private final InvoiceJpaRepository jpaRepository;
    private final InvoiceEntityMapper mapper;

    public InvoicePersistenceAdapter(InvoiceJpaRepository jpaRepository, InvoiceEntityMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository is required");
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
    }

    @Override
    @Transactional
    public Invoice save(Invoice invoice) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(invoice)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invoice> findById(InvoiceId id) {
        return jpaRepository.findById(id.toString()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invoice> findByOrderId(OrderId orderId) {
        return jpaRepository.findByOrderId(orderId.toString()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
