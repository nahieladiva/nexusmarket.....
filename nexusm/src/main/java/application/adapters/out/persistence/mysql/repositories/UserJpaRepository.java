package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.UserJpaEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA de usuarios.
 */
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, String> {

    Optional<UserJpaEntity> findByEmail(String email);
}