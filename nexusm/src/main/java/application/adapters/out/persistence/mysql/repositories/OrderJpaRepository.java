package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.OrderJpaEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository of orders.
 */
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, String> {

    List<OrderJpaEntity> findByBuyerId(String buyerId);
}