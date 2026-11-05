package application.adapters.in.rest.responses;

import application.domain.enums.ReturnStatus;
import java.time.LocalDateTime;

public record ReturnResponse(String id, String orderId, String buyerId, String productId, int quantity,
                             String refundAmount, String refundCurrency, String reason,
                             ReturnStatus status, LocalDateTime requestedAt,
                             LocalDateTime resolvedAt) {
}
