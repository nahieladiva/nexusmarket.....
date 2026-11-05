package application.adapters.in.rest.responses;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import java.time.LocalDateTime;

/**
 * Product DTO for REST responses.
 */
public record ProductResponse(String id, String code, ProductType type, String name,
                              String description, String priceAmount, String priceCurrency,
                              String sellerId, ProductStatus status, LocalDateTime createdAt) {
}
