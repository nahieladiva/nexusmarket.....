package application.domain.models;

import application.domain.valueobjects.Money;
import application.domain.valueobjects.Percentage;
import application.domain.valueobjects.ProductId;
import java.util.Objects;

/**
 * Precio final de un producto en una fecha: precio base, descuento aplicado y precio final.
 * {@code discount} es 0% cuando no hay promoción vigente.
 */
public record PriceQuote(ProductId productId, Money basePrice, Percentage discount,
                         Money finalPrice) {

    public PriceQuote {
        Objects.requireNonNull(productId, "productId es obligatorio");
        Objects.requireNonNull(basePrice, "basePrice es obligatorio");
        Objects.requireNonNull(discount, "discount es obligatorio");
        Objects.requireNonNull(finalPrice, "finalPrice es obligatorio");
    }

    public boolean hasDiscount() {
        return discount.getValue().signum() > 0;
    }
}
