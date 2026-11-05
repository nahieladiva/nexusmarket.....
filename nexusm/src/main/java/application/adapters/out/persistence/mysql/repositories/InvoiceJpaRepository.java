package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.InvoiceJpaEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository of invoices.
 */
public interface InvoiceJpaRepository extends JpaRepository<InvoiceJpaEntity, String> {

    Optional<InvoiceJpaEntity> findByOrderId(String orderId);
}
