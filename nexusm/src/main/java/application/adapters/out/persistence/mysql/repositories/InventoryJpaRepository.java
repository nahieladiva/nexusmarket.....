package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.InventoryJpaEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository of inventories.
 */
public interface InventoryJpaRepository extends JpaRepository<InventoryJpaEntity, String> {

    Optional<InventoryJpaEntity> findByProductIdAndWarehouseId(String productId,
                                                              String warehouseId);

    List<InventoryJpaEntity> findByWarehouseId(String warehouseId);

    List<InventoryJpaEntity> findByProductId(String productId);
}