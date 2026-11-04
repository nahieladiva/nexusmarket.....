package application.domain.models;

import application.domain.valueobjects.DateRange;
import application.domain.valueobjects.Percentage;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.PromotionId;
import application.domain.valueobjects.UserId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Promoción de un producto: un porcentaje de descuento durante un periodo.
 * Una promoción finalizada manualmente deja de aplicar aunque su periodo no haya terminado.
 */
public final class Promotion {

    private final PromotionId id;
    private final ProductId productId;
    private final UserId createdBy;
    private final Percentage discount;
    private final DateRange period;
    private boolean ended;
    private final LocalDateTime createdAt;

    public Promotion(PromotionId id, ProductId productId, UserId createdBy, Percentage discount,
                     DateRange period, boolean ended, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "El id de la promoción es obligatorio");
        this.productId = Objects.requireNonNull(productId, "El producto es obligatorio");
        this.createdBy = Objects.requireNonNull(createdBy, "El creador es obligatorio");
        this.discount = Objects.requireNonNull(discount, "El descuento es obligatorio");
        this.period = Objects.requireNonNull(period, "El periodo es obligatorio");
        this.ended = ended;
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
    }

    /** Indica si la promoción aplica en la fecha dada. */
    public boolean isEffectiveOn(LocalDate date) {
        return !ended && period.contains(date);
    }

    /** Indica si la promoción todavía puede aplicar hoy o en el futuro. */
    public boolean isPendingOrEffective(LocalDate today) {
        return !ended && !period.getEndDate().isBefore(today);
    }

    public void end() {
        if (ended) {
            throw new IllegalStateException("La promoción ya está finalizada");
        }
        this.ended = true;
    }

    public PromotionId getId() {
        return id;
    }

    public ProductId getProductId() {
        return productId;
    }

    public UserId getCreatedBy() {
        return createdBy;
    }

    public Percentage getDiscount() {
        return discount;
    }

    public DateRange getPeriod() {
        return period;
    }

    public boolean isEnded() {
        return ended;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
