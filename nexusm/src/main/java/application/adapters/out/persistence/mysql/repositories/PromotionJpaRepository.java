package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.PromotionJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionJpaRepository extends JpaRepository<PromotionJpaEntity, String> {

    List<PromotionJpaEntity> findByProductId(String productId);
}
