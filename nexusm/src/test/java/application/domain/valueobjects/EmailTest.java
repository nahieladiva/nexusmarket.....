package application.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit tests of the {@link Email} value object.
 */
class EmailTest {

    @Test
    void normalizesToLowercase() {
        assertEquals("user@example.com", Email.of("  User@Example.COM ").getValue());
    }

    @Test
    void rejectsInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> Email.of("no-at-sign"));
        assertThrows(IllegalArgumentException.class, () -> Email.of("user@"));
        assertThrows(IllegalArgumentException.class, () -> Email.of(""));
        assertThrows(IllegalArgumentException.class, () -> Email.of(null));
    }

    @Test
    void equalityIsByValue() {
        assertEquals(Email.of("a@b.com"), Email.of("A@B.COM"));
    }
}