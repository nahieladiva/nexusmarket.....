package application.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import application.domain.models.OrderItem;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.Percentage;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;

import org.junit.jupiter.api.Test;

/**
 * Pruebas del servicio de dominio {@link PricingDomainService}.
 */
class PricingDomainServiceTest {

    private final PricingDomainService pricing = new PricingDomainService();

    @Test
    void appliesPercentageDiscount() {
        Money discounted = pricing.applyPercentageDiscount(
            Money.of("100.00", "USD"), Percentage.of(15));
        assertEquals("85.00", discounted.getAmount().toPlainString());
    }

    @Test
    void calculatesOrderTotal() {
        List<OrderItem> items = List.of(
            new OrderItem(ProductId.random(), Quantity.of(2), Money.of("10.00", "USD")),
            new OrderItem(ProductId.random(), Quantity.of(3), Money.of("5.50", "USD")));
        assertEquals("36.50", pricing.calculateOrderTotal(items).getAmount().toPlainString());
    }

    @Test
    void appliesVolumeDiscountOverThreshold() {
        List<OrderItem> items = List.of(
            new OrderItem(ProductId.random(), Quantity.of(100), Money.of("1.00", "USD")));
        Money total = pricing.applyVolumeDiscount(items, 100, Percentage.of(10));
        assertEquals("90.00", total.getAmount().toPlainString());
    }

    @Test
    void keepsTotalWhenBelowVolumeThreshold() {
        List<OrderItem> items = List.of(
            new OrderItem(ProductId.random(), Quantity.of(50), Money.of("1.00", "USD")));
        Money total = pricing.applyVolumeDiscount(items, 100, Percentage.of(10));
        assertEquals("50.00", total.getAmount().toPlainString());
    }
}