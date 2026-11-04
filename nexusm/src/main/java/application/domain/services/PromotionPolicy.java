package application.domain.services;

import application.domain.exceptions.BusinessRuleException;
import application.domain.models.Promotion;
import application.domain.valueobjects.DateRange;
import application.domain.valueobjects.Percentage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio de las promociones.
 * <ul>
 *   <li>El descuento va de {@value #MIN_DISCOUNT}% a {@value #MAX_DISCOUNT}%.</li>
 *   <li>La fecha de fin debe ser posterior a la de inicio.</li>
 *   <li>La promoción no puede empezar en el pasado.</li>
 *   <li>Un producto no puede tener dos promociones vigentes al mismo tiempo
 *       (los periodos de promociones no finalizadas no pueden cruzarse).</li>
 * </ul>
 */
public final class PromotionPolicy {

    public static final int MIN_DISCOUNT = 1;
    public static final int MAX_DISCOUNT = 70;

    public void validateNew(Percentage discount, DateRange period, LocalDate today,
                            List<Promotion> existingForProduct) {
        if (discount.getValue().compareTo(BigDecimal.valueOf(MIN_DISCOUNT)) < 0
            || discount.getValue().compareTo(BigDecimal.valueOf(MAX_DISCOUNT)) > 0) {
            throw new BusinessRuleException("El descuento debe estar entre " + MIN_DISCOUNT
                + "% y " + MAX_DISCOUNT + "%");
        }
        if (!period.getEndDate().isAfter(period.getStartDate())) {
            throw new BusinessRuleException(
                "La fecha de fin de la promoción debe ser posterior a la de inicio");
        }
        if (period.getStartDate().isBefore(today)) {
            throw new BusinessRuleException("La promoción no puede empezar en el pasado");
        }
        boolean overlaps = existingForProduct.stream()
            .filter(promotion -> !promotion.isEnded())
            .anyMatch(promotion -> promotion.getPeriod().overlaps(period));
        if (overlaps) {
            throw new BusinessRuleException(
                "El producto ya tiene una promoción en ese periodo; finalícela primero");
        }
    }

    /** Promoción que aplica en la fecha dada, si existe. */
    public Optional<Promotion> effectiveOn(List<Promotion> promotionsForProduct, LocalDate date) {
        return promotionsForProduct.stream()
            .filter(promotion -> promotion.isEffectiveOn(date))
            .findFirst();
    }
}
