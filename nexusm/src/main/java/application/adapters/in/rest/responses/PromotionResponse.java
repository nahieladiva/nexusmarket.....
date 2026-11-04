package application.adapters.in.rest.responses;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PromotionResponse(String id, String productId, String createdBy,
                                String discountPercent, LocalDate startDate, LocalDate endDate,
                                boolean ended, LocalDateTime createdAt) {
}
