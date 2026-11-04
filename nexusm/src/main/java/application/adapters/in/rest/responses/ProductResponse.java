package application.adapters.in.rest.responses;

import java.time.LocalDateTime;

/**
 * DTO de producto para respuestas REST.
 */
public record ProductResponse(String id, String code, String name, String description,
                              String priceAmount, String priceCurrency,
                              String sellerId, boolean active, LocalDateTime createdAt) {
}