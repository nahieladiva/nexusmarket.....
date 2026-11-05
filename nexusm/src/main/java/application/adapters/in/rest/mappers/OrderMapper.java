package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.OrderItemResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
            order.getId().toString(),
            order.getBuyerId().toString(),
            order.getStatus().name(),
            order.getTotal().getAmount().toPlainString(),
            order.getTotal().getCurrency().getCurrencyCode(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            order.getItems().stream().map(this::toItemResponse).toList());
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
            item.getProductId().toString(),
            item.getQuantity().getValue(),
            item.getUnitPrice().getAmount().toPlainString(),
            item.getUnitPrice().getCurrency().getCurrencyCode(),
            item.subtotal().getAmount().toPlainString(),
            item.getWarehouseId() == null ? null : item.getWarehouseId().toString());
    }
}
