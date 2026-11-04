package application.domain.ports.in;

import application.domain.models.PriceQuote;
import application.domain.models.Promotion;
import application.domain.valueobjects.DateRange;
import application.domain.valueobjects.Percentage;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.PromotionId;
import application.domain.valueobjects.UserId;
import java.util.List;

/**
 * Puerto de entrada 7/7 — promociones y precio final.
 */
public interface PromotionManagementPort {

    /** Solo el vendedor dueño del producto. Ver reglas en {@code PromotionPolicy}. */
    Promotion createPromotion(UserId sellerId, ProductId productId, Percentage discount,
                              DateRange period);

    /** Solo el vendedor que la creó. */
    Promotion endPromotion(UserId sellerId, PromotionId promotionId);

    /** Promociones que aplican hoy. */
    List<Promotion> findActivePromotions();

    List<Promotion> findByProduct(ProductId productId);

    /** Precio final de hoy, con la promoción vigente si la hay. */
    PriceQuote calculateFinalPrice(ProductId productId);
}
