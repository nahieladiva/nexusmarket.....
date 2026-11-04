package application.adapters.in.rest.requests;

import java.util.List;

/** El comprador se toma del token y los precios del catálogo. */
public record CreateOrderRequest(List<OrderItemRequest> items) {
}
