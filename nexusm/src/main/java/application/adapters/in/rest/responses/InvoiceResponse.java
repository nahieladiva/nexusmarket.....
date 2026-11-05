package application.adapters.in.rest.responses;

import application.domain.enums.InvoiceStatus;
import java.time.LocalDateTime;

public record InvoiceResponse(String id, String invoiceNumber, String orderId, String buyerId,
                              String totalAmount, String totalCurrency, InvoiceStatus status,
                              LocalDateTime issuedAt, LocalDateTime voidedAt) {
}
