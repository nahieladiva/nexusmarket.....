package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.ShipmentJpaEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository of shipments.
 */
public interface ShipmentJpaRepository extends JpaRepository<ShipmentJpaEntity, String> {

    List<ShipmentJpaEntity> findByOrderId(String orderId);
}
