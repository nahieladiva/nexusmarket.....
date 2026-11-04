package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.CreateOrderRequest;
import application.adapters.in.rest.responses.OrderItemResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.OrderLine;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public List<OrderLine> toLines(CreateOrderRequest request) {
        if (request == null || request.items() == null) {
            throw new IllegalArgumentException("El pedido debe tener ítems");
        }
        return request.items().stream()
            .map(item -> new OrderLine(ProductId.of(item.productId()), Quantity.of(item.quantity())))
            .toList();
    }

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
