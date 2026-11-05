package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.RefundJpaEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository of refunds.
 */
public interface RefundJpaRepository extends JpaRepository<RefundJpaEntity, String> {

    List<RefundJpaEntity> findByOrderId(String orderId);
}
