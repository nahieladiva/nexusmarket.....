package application.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit tests of the {@link Quantity} value object.
 */
class QuantityTest {

    @Test
    void addsQuantities() {
        assertEquals(Quantity.of(7), Quantity.of(3).add(Quantity.of(4)));
    }

    @Test
    void subtractsQuantities() {
        assertEquals(Quantity.of(2), Quantity.of(5).subtract(Quantity.of(3)));
    }

    @Test
    void rejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> Quantity.of(-1));
    }

    @Test
    void rejectsSubtractionResultBelowZero() {
        assertThrows(IllegalArgumentException.class,
            () -> Quantity.of(2).subtract(Quantity.of(5)));
    }

    @Test
    void comparesQuantities() {
        assertEquals(0, Quantity.of(5).compareTo(Quantity.of(5)));
        assertEquals(-1, Quantity.of(3).compareTo(Quantity.of(4)));
    }

    @Test
    void zeroQuantityIsAllowed() {
        assertEquals(0, Quantity.zero().getValue());
    }
}