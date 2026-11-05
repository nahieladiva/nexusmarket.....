package application.adapters.in.rest.responses;

import application.domain.enums.ShipmentStatus;
import java.time.LocalDateTime;

public record ShipmentResponse(String id, String orderId, String warehouseId,
                               AddressResponse shippingAddress, String logisticsOperatorId,
                               String trackingNumber, ShipmentStatus status, LocalDateTime createdAt,
                               LocalDateTime dispatchedAt, LocalDateTime deliveredAt) {
}
