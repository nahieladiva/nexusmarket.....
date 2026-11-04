package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.OrderMapper;
import application.adapters.in.rest.requests.CreateOrderRequest;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.security.AuthenticatedUser;
import application.domain.models.Order;
import application.domain.ports.in.OrderManagementPort;
import application.domain.valueobjects.OrderId;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST del puerto de entrada {@link OrderManagementPort}.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderManagementPort orderManagementPort;
    private final OrderMapper orderMapper;

    public OrderController(OrderManagementPort orderManagementPort, OrderMapper orderMapper) {
        this.orderManagementPort = orderManagementPort;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@AuthenticationPrincipal AuthenticatedUser me,
                                                    @RequestBody CreateOrderRequest request) {
        Order order = orderManagementPort.placeOrder(me.userId(), orderMapper.toLines(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(order));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<OrderResponse>> mine(@AuthenticationPrincipal AuthenticatedUser me) {
        return ResponseEntity.ok(toResponses(orderManagementPort.findOrdersByBuyer(me.userId())));
    }

    @GetMapping("/seller")
    public ResponseEntity<List<OrderResponse>> forSeller(@AuthenticationPrincipal AuthenticatedUser me) {
        return ResponseEntity.ok(toResponses(orderManagementPort.findOrdersForSeller(me.userId())));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> all() {
        return ResponseEntity.ok(toResponses(orderManagementPort.findAllOrders()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@AuthenticationPrincipal AuthenticatedUser me,
                                                  @PathVariable String id) {
        return ResponseEntity.ok(orderMapper.toResponse(
            orderManagementPort.findOrderById(me.userId(), OrderId.of(id))));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirm(@PathVariable String id) {
        return ResponseEntity.ok(orderMapper.toResponse(orderManagementPort.confirmOrder(OrderId.of(id))));
    }

    @PatchMapping("/{id}/ship")
    public ResponseEntity<OrderResponse> ship(@PathVariable String id) {
        return ResponseEntity.ok(orderMapper.toResponse(orderManagementPort.shipOrder(OrderId.of(id))));
    }

    @PatchMapping("/{id}/deliver")
    public ResponseEntity<OrderResponse> deliver(@PathVariable String id) {
        return ResponseEntity.ok(orderMapper.toResponse(orderManagementPort.deliverOrder(OrderId.of(id))));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@AuthenticationPrincipal AuthenticatedUser me,
                                                @PathVariable String id) {
        return ResponseEntity.ok(orderMapper.toResponse(
            orderManagementPort.cancelOrder(me.userId(), OrderId.of(id))));
    }

    private List<OrderResponse> toResponses(List<Order> orders) {
        return orders.stream().map(orderMapper::toResponse).toList();
    }
}
