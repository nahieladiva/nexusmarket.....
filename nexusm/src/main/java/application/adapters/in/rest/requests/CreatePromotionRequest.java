package application.adapters.in.rest.requests;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Fechas en formato ISO (2026-11-15). */
public record CreatePromotionRequest(String productId, BigDecimal discountPercent,
                                     LocalDate startDate, LocalDate endDate) {
}
