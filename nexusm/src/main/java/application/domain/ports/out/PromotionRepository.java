package application.domain.ports.out;

import application.domain.models.Promotion;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.PromotionId;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida 7/15 — persistencia de promociones (MySQL).
 */
public interface PromotionRepository {

    Promotion save(Promotion promotion);

    Optional<Promotion> findById(PromotionId id);

    List<Promotion> findByProductId(ProductId productId);

    List<Promotion> findAll();
}
