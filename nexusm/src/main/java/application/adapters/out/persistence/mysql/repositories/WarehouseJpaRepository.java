package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.WarehouseJpaEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository of warehouses.
 */
public interface WarehouseJpaRepository extends JpaRepository<WarehouseJpaEntity, String> {

    List<WarehouseJpaEntity> findByNameContainingIgnoreCase(String name);
}