package application.adapters.in.rest.responses;

import application.domain.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(String id, String buyerId, OrderStatus status,
                            String totalAmount, String totalCurrency, LocalDateTime paidAt,
                            LocalDateTime createdAt, LocalDateTime updatedAt,
                            List<OrderItemResponse> items) {
}
