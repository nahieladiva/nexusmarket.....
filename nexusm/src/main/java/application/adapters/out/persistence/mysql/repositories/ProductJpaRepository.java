package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.ProductJpaEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository of products.
 */
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, String> {

    Optional<ProductJpaEntity> findByCode(String code);

    List<ProductJpaEntity> findByNameContainingIgnoreCase(String name);

    List<ProductJpaEntity> findBySellerId(String sellerId);
}