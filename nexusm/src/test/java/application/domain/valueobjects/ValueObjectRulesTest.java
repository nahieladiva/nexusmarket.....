package application.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Validation and normalization rules of the value objects.
 */
class ValueObjectRulesTest {

    private static final String ID = "3f2c1a9e-7b1d-4c55-9b3a-2a1f6d0e8c11";

    @Test
    void aProductCodeIsNormalized() {
        assertEquals("ELE-000123", ProductCode.of("  ele-000123 ").getValue());
        assertEquals(ProductCode.of("ELE-0001"), ProductCode.of("ele-0001"));
        assertEquals(ProductCode.of("ELE-0001").hashCode(), ProductCode.of("ele-0001").hashCode());
        assertEquals("ELE-0001", ProductCode.of("ELE-0001").toString());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {" ", "ELE", "E-0001", "ELECTR-0001", "ELE-1", "ELE-123456789", "123-0001"})
    void anInvalidProductCodeIsRejected(String value) {
        assertThrows(IllegalArgumentException.class, () -> ProductCode.of(value));
    }

    @Test
    void aProductCodeCanBeGenerated() {
        assertEquals("ELEC-0007", ProductCode.generate("elec", 7).getValue());
    }

    @Test
    void anInvalidGeneratedCodeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> ProductCode.generate(null, 1));
        assertThrows(IllegalArgumentException.class, () -> ProductCode.generate("A", 1));
        assertThrows(IllegalArgumentException.class, () -> ProductCode.generate("TOOLONG", 1));
        assertThrows(IllegalArgumentException.class, () -> ProductCode.generate("ELE", -1));
    }

    @Test
    void aPhoneNumberRemovesSpacesAndHyphens() {
        assertEquals("+573001234567", PhoneNumber.of(" +57 300-123 4567 ").getValue());
        assertEquals(PhoneNumber.of("+573001234567"), PhoneNumber.of("+57 3001234567"));
        assertEquals(PhoneNumber.of("+573001234567").hashCode(), PhoneNumber.of("+57 3001234567").hashCode());
        assertEquals("+573001234567", PhoneNumber.of("+573001234567").toString());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {" ", "3001234567", "+0123456", "+1", "+1234567890123456", "+57abc"})
    void anInvalidPhoneNumberIsRejected(String value) {
        assertThrows(IllegalArgumentException.class, () -> PhoneNumber.of(value));
    }

    @Test
    void aWarehouseLocationIsNormalized() {
        WarehouseLocation location = WarehouseLocation.of(" a 1 ", "b", "c");

        assertEquals("A-1", location.getAisle());
        assertEquals("B", location.getShelf());
        assertEquals("C", location.getBin());
        assertEquals(WarehouseLocation.of("A-1", "B", "C"), location);
        assertEquals(WarehouseLocation.of("A-1", "B", "C").hashCode(), location.hashCode());
        assertNotEquals(WarehouseLocation.of("A-1", "B", "D"), location);
        assertTrue(location.toString().contains("A-1"));
    }

    @Test
    void everyPartOfAWarehouseLocationIsRequired() {
        assertThrows(IllegalArgumentException.class, () -> WarehouseLocation.of(null, "1", "1"));
        assertThrows(IllegalArgumentException.class, () -> WarehouseLocation.of("A", " ", "1"));
        assertThrows(IllegalArgumentException.class, () -> WarehouseLocation.of("A", "1", ""));
    }

    @Test
    void anAddressTrimsItsParts() {
        Address address = Address.of(" 10th Street ", " Springfield ", " Oregon ", " 97477 ", " US ");

        assertEquals("10th Street", address.getStreet());
        assertEquals("Springfield", address.getCity());
        assertEquals("Oregon", address.getState());
        assertEquals("97477", address.getZipCode());
        assertEquals("US", address.getCountry());
        assertEquals(Address.of("10th Street", "Springfield", "Oregon", "97477", "US"), address);
        assertEquals(Address.of("10th Street", "Springfield", "Oregon", "97477", "US").hashCode(),
            address.hashCode());
        assertNotEquals(Address.of("Other", "Springfield", "Oregon", "97477", "US"), address);
        assertFalse(address.toString().isBlank());
    }

    @Test
    void everyPartOfAnAddressIsRequired() {
        assertThrows(IllegalArgumentException.class, () -> Address.of(null, "c", "s", "z", "o"));
        assertThrows(IllegalArgumentException.class, () -> Address.of("a", " ", "s", "z", "o"));
        assertThrows(IllegalArgumentException.class, () -> Address.of("a", "c", null, "z", "o"));
        assertThrows(IllegalArgumentException.class, () -> Address.of("a", "c", "s", "", "o"));
        assertThrows(IllegalArgumentException.class, () -> Address.of("a", "c", "s", "z", null));
    }

    @Test
    void moneyNeedsAValidAmountAndCurrency() {
        Currency cop = Currency.getInstance("COP");

        assertThrows(IllegalArgumentException.class, () -> new Money(null, cop));
        assertThrows(IllegalArgumentException.class, () -> new Money(BigDecimal.ONE, null));
        assertThrows(IllegalArgumentException.class, () -> Money.of("-1", "COP"));
        assertThrows(IllegalArgumentException.class, () -> Money.of("abc", "COP"));
        assertThrows(IllegalArgumentException.class, () -> Money.of("1", "XXXX"));
    }

    @Test
    void moneyOfDifferentCurrenciesCannotBeCombined() {
        Money pesos = Money.of("10", "COP");
        Money dollars = Money.of("10", "USD");

        assertThrows(IllegalArgumentException.class, () -> pesos.add(dollars));
        assertThrows(IllegalArgumentException.class, () -> pesos.subtract(dollars));
        assertThrows(IllegalArgumentException.class, () -> pesos.isGreaterThan(dollars));
        assertThrows(IllegalArgumentException.class, () -> pesos.isLessThan(dollars));
    }

    @Test
    void moneyComparesByValue() {
        Money ten = Money.of("10.00", "COP");

        assertEquals(Money.of("10", "COP"), ten);
        assertEquals(Money.of("10", "COP").hashCode(), ten.hashCode());
        assertNotEquals(Money.of("10", "USD"), ten);
        assertNotEquals(Money.of("11", "COP"), ten);
        assertTrue(Money.of("11", "COP").isGreaterThan(ten));
        assertTrue(ten.isLessThan(Money.of("11", "COP")));
        assertTrue(Money.zero("COP").isZero());
        assertFalse(ten.isZero());
        assertEquals("10.00 COP", ten.toString());
    }

    static Stream<Function<String, Object>> identifiers() {
        return Stream.of(CartId::of, InventoryId::of, InvoiceId::of, OrderId::of, ProductId::of,
            RefundId::of, ReturnId::of, ShipmentId::of, UserId::of, WarehouseId::of);
    }

    @ParameterizedTest
    @MethodSource("identifiers")
    void anIdentifierComparesByItsValue(Function<String, Object> factory) {
        Object id = factory.apply(ID);

        assertEquals(id, id);
        assertEquals(factory.apply(ID), id);
        assertEquals(factory.apply(ID).hashCode(), id.hashCode());
        assertEquals(ID, id.toString());
        assertNotEquals(factory.apply(UUID.randomUUID().toString()), id);
        assertNotEquals(null, id);
        assertNotEquals("other type", id);
    }

    @ParameterizedTest
    @MethodSource("identifiers")
    void anIdentifierMustBeAUuid(Function<String, Object> factory) {
        assertThrows(IllegalArgumentException.class, () -> factory.apply("not-a-uuid"));
    }

    @Test
    void randomIdentifiersAreDifferent() {
        assertNotEquals(OrderId.random(), OrderId.random());
        assertNotEquals(ProductId.random(), ProductId.random());
        assertNotEquals(CartId.random(), CartId.random());
    }
}
