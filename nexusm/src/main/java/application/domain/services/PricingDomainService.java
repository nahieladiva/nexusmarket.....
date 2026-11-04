package application.domain.services;

import application.domain.models.OrderItem;
import application.domain.models.PriceQuote;
import application.domain.models.Product;
import application.domain.models.Promotion;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.Percentage;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Servicio de dominio para cálculos de precios.
 *
 * <p>Regla de no acumulación: el descuento por volumen de una orden y las promociones de
 * producto no se suman. Se calcula el total de ambas formas y se cobra el menor
 * (ver {@link #chooseBestPricing}).</p>
 */
public final class PricingDomainService {

    /** Unidades totales a partir de las cuales aplica el descuento por volumen. */
    public static final int VOLUME_THRESHOLD = 15;

    /** Porcentaje del descuento por volumen. */
    public static final Percentage VOLUME_DISCOUNT = Percentage.of(5);

    public Money applyPercentageDiscount(Money price, Percentage discount) {
        BigDecimal factor = BigDecimal.ONE.subtract(discount.asFraction());
        return price.multiply(factor);
    }

    public Money calculateOrderTotal(List<OrderItem> items) {
        Money total = null;
        for (OrderItem item : items) {
            total = (total == null) ? item.subtotal() : total.add(item.subtotal());
        }
        if (total == null) {
            throw new IllegalArgumentException("La orden debe tener al menos un item");
        }
        return total;
    }

    public Money applyVolumeDiscount(List<OrderItem> items, int volumeThreshold,
                                     Percentage discount) {
        int totalUnits = totalUnits(items);
        Money total = calculateOrderTotal(items);
        if (totalUnits >= volumeThreshold) {
            return applyPercentageDiscount(total, discount);
        }
        return total;
    }

    /**
     * Precio final de un producto con la promoción vigente (si la hay).
     */
    public PriceQuote quote(Product product, Optional<Promotion> effectivePromotion) {
        Objects.requireNonNull(product, "El producto es obligatorio");
        Percentage discount = effectivePromotion.map(Promotion::getDiscount)
            .orElse(Percentage.of(0));
        Money finalPrice = applyPercentageDiscount(product.getPrice(), discount);
        return new PriceQuote(product.getId(), product.getPrice(), discount, finalPrice);
    }

    /**
     * Elige la forma de cobro más favorable para el comprador.
     *
     * @param baseItems  ítems a precio de catálogo (sin promociones)
     * @param promoItems los mismos ítems con el precio de promoción aplicado
     * @return los ítems a guardar en la orden y el total a cobrar
     */
    public PricingResult chooseBestPricing(List<OrderItem> baseItems, List<OrderItem> promoItems) {
        Money promoTotal = calculateOrderTotal(promoItems);
        if (totalUnits(baseItems) < VOLUME_THRESHOLD) {
            return new PricingResult(promoItems, promoTotal, false);
        }
        Money volumeTotal = applyVolumeDiscount(baseItems, VOLUME_THRESHOLD, VOLUME_DISCOUNT);
        if (volumeTotal.isLessThan(promoTotal)) {
            return new PricingResult(baseItems, volumeTotal, true);
        }
        return new PricingResult(promoItems, promoTotal, false);
    }

    private int totalUnits(List<OrderItem> items) {
        return items.stream().mapToInt(item -> item.getQuantity().getValue()).sum();
    }

    /**
     * Resultado de la elección de precios.
     *
     * @param volumeDiscountApplied {@code true} si se cobró con descuento por volumen
     */
    public record PricingResult(List<OrderItem> items, Money total,
                                boolean volumeDiscountApplied) {
    }
}
