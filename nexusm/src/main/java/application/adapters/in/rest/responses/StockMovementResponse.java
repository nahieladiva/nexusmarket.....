package application.adapters.in.rest.responses;

import java.time.LocalDateTime;

public record StockMovementResponse(String id, String type, int delta, int resultingOnHand,
                                    String reason, String performedBy, String reference,
                                    LocalDateTime occurredAt) {
}
