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
 * Domain service for price calculations.
 *
 * <p>Non-stacking rule: the volume discount of an order and product promotions
 * are not added together. The total is calculated both ways and the lower one is charged
 * (see {@link #chooseBestPricing}).</p>
 */
public final class PricingDomainService {

    /** Total units from which the volume discount applies. */
    public static final int VOLUME_THRESHOLD = 15;

    /** Volume discount percentage. */
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
            throw new IllegalArgumentException("The order must have at least one item");
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
     * Final price of a product with the current promotion (if any).
     */
    public PriceQuote quote(Product product, Optional<Promotion> effectivePromotion) {
        Objects.requireNonNull(product, "Product is required");
        Percentage discount = effectivePromotion.map(Promotion::getDiscount)
            .orElse(Percentage.of(0));
        Money finalPrice = applyPercentageDiscount(product.getPrice(), discount);
        return new PriceQuote(product.getId(), product.getPrice(), discount, finalPrice);
    }

    /**
     * Chooses the pricing most favorable to the buyer.
     *
     * @param baseItems  items at catalog price (without promotions)
     * @param promoItems the same items with the promotional price applied
     * @return the items to store in the order and the total to charge
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
     * Result of the pricing choice.
     *
     * @param volumeDiscountApplied {@code true} if the volume discount was charged
     */
    public record PricingResult(List<OrderItem> items, Money total,
                                boolean volumeDiscountApplied) {
    }
}
