package application.adapters.in.rest.responses;

import application.domain.enums.StockMovementType;
import java.time.LocalDateTime;

public record StockMovementResponse(String id, StockMovementType type, int delta, int resultingOnHand,
                                    String reason, String performedBy, String reference,
                                    LocalDateTime occurredAt) {
}
