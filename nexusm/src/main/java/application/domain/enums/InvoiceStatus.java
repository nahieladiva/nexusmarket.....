package application.domain.enums;

import application.domain.exceptions.ResourceStateConflictException;

/**
 * Status of the invoice generated when an order is paid.
 *
 * <ul>
 *   <li>{@code ISSUED}: the invoice is valid.</li>
 *   <li>{@code VOIDED}: the order was cancelled after payment, so the invoice is annulled.</li>
 * </ul>
 */
public enum InvoiceStatus {

    ISSUED,
    VOIDED;

    /**
     * Tells whether the invoice still has legal value.
     */
    public boolean isValid() {
        return this == ISSUED;
    }

    /**
     * Validates that the invoice can be voided.
     */
    public void requireCanVoid() {
        if (this == VOIDED) {
            throw new ResourceStateConflictException("Invoice is already voided");
        }
    }
}
