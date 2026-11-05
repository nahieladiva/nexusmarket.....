package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.CartJpaEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository of carts.
 */
public interface CartJpaRepository extends JpaRepository<CartJpaEntity, String> {

    Optional<CartJpaEntity> findByBuyerId(String buyerId);
}
