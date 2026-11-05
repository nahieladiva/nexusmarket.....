package application.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Currency;

import org.junit.jupiter.api.Test;

/**
 * Unit tests of the {@link Money} value object.
 */
class MoneyTest {

    @Test
    void preservesTwoDecimals() {
        Money money = Money.of("10.126", "USD");
        assertEquals("10.13", money.getAmount().toPlainString());
    }

    @Test
    void addsMoneyInSameCurrency() {
        Money result = Money.of("10.00", "USD").add(Money.of("5.50", "USD"));
        assertEquals("15.50", result.getAmount().toPlainString());
    }

    @Test
    void subtractsMoneyInSameCurrency() {
        Money result = Money.of("10.00", "USD").subtract(Money.of("3.25", "USD"));
        assertEquals("6.75", result.getAmount().toPlainString());
    }

    @Test
    void multipliesByInteger() {
        Money result = Money.of("2.50", "USD").multiply(3);
        assertEquals("7.50", result.getAmount().toPlainString());
    }

    @Test
    void equalityIsByValue() {
        assertEquals(Money.of("10.00", "USD"), Money.of("10.0", "USD"));
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
            () -> Money.of("-1.00", "USD"));
    }

    @Test
    void rejectsOperationsBetweenDifferentCurrencies() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> Money.of("10.00", "USD").add(Money.of("10.00", "MXN")));
        assertTrue(ex.getMessage().contains("different currencies"));
    }

    @Test
    void buildsFromZero() {
        Money zero = Money.zero("USD");
        assertTrue(zero.isZero());
        assertEquals(Currency.getInstance("USD"), zero.getCurrency());
    }
}