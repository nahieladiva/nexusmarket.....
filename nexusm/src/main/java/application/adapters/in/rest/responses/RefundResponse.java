package application.adapters.in.rest.responses;

import application.domain.enums.RefundStatus;
import java.time.LocalDateTime;

/** {@code returnId} is null when the refund comes from a cancelled order. */
public record RefundResponse(String id, String orderId, String buyerId, String returnId,
                             String amount, String currency, RefundStatus status,
                             LocalDateTime createdAt, LocalDateTime resolvedAt) {
}
