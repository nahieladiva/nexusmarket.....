package application.adapters.in.rest.responses;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(String id, String buyerId, String status,
                            String totalAmount, String totalCurrency,
                            LocalDateTime createdAt, LocalDateTime updatedAt,
                            List<OrderItemResponse> items) {
}
