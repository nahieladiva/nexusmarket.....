package application.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifier of an invoice (UUID).
 */
public final class InvoiceId {

    private final UUID value;

    public InvoiceId(UUID value) {
        this.value = Objects.requireNonNull(value, "Invoice id cannot be null");
    }

    public static InvoiceId of(UUID value) {
        return new InvoiceId(value);
    }

    public static InvoiceId of(String value) {
        return new InvoiceId(UUID.fromString(value));
    }

    public static InvoiceId random() {
        return new InvoiceId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InvoiceId invoiceId = (InvoiceId) o;
        return value.equals(invoiceId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
