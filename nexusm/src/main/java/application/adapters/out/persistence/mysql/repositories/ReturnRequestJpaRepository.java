package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.ReturnRequestJpaEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository of return requests.
 */
public interface ReturnRequestJpaRepository extends JpaRepository<ReturnRequestJpaEntity, String> {

    List<ReturnRequestJpaEntity> findByOrderId(String orderId);
}
