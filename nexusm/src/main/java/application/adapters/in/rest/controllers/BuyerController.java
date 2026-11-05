package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.CartMapper;
import application.adapters.in.rest.mappers.InvoiceMapper;
import application.adapters.in.rest.mappers.OrderMapper;
import application.adapters.in.rest.mappers.RefundMapper;
import application.adapters.in.rest.mappers.ReturnMapper;
import application.adapters.in.rest.mappers.ShipmentMapper;
import application.adapters.in.rest.requests.CartItemQuantityRequest;
import application.adapters.in.rest.requests.CartItemRequest;
import application.adapters.in.rest.requests.CreateReturnRequest;
import application.adapters.in.rest.responses.CartResponse;
import application.adapters.in.rest.responses.InvoiceResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.responses.RefundResponse;
import application.adapters.in.rest.responses.ReturnResponse;
import application.adapters.in.rest.responses.ShipmentResponse;
import application.adapters.in.rest.security.AuthenticatedUser;
import application.domain.ports.in.BuyerPort;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter of {@link BuyerPort}. The buyer always comes from the token.
 */
@RestController
@RequestMapping("/api/v1/buyer")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerPort port;
    private final CartMapper cartMapper;
    private final OrderMapper orderMapper;
    private final InvoiceMapper invoiceMapper;
    private final ShipmentMapper shipmentMapper;
    private final ReturnMapper returnMapper;
    private final RefundMapper refundMapper;

    @GetMapping("/cart")
    public CartResponse findCart(@AuthenticationPrincipal AuthenticatedUser me) {
        return cartMapper.toResponse(port.findCart(me.userId()));
    }

    @PostMapping("/cart/items")
    public CartResponse addCartItem(@AuthenticationPrincipal AuthenticatedUser me,
                                    @Valid @RequestBody CartItemRequest request) {
        return cartMapper.toResponse(port.addCartItem(me.userId(), ProductId.of(request.productId()),
            Quantity.of(request.quantity())));
    }

    @PatchMapping("/cart/items/{productId}")
    public CartResponse changeCartItemQuantity(@AuthenticationPrincipal AuthenticatedUser me,
                                               @PathVariable ProductId productId,
                                               @Valid @RequestBody CartItemQuantityRequest request) {
        return cartMapper.toResponse(port.changeCartItemQuantity(me.userId(), productId,
            Quantity.of(request.quantity())));
    }

    @DeleteMapping("/cart/items/{productId}")
    public CartResponse removeCartItem(@AuthenticationPrincipal AuthenticatedUser me,
                                       @PathVariable ProductId productId) {
        return cartMapper.toResponse(port.removeCartItem(me.userId(), productId));
    }

    @DeleteMapping("/cart")
    public CartResponse clearCart(@AuthenticationPrincipal AuthenticatedUser me) {
        return cartMapper.toResponse(port.clearCart(me.userId()));
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@AuthenticationPrincipal AuthenticatedUser me) {
        return orderMapper.toResponse(port.placeOrder(me.userId()));
    }

    @PostMapping("/orders/{orderId}/pay")
    public OrderResponse payOrder(@AuthenticationPrincipal AuthenticatedUser me,
                                  @PathVariable OrderId orderId) {
        return orderMapper.toResponse(port.payOrder(me.userId(), orderId));
    }

    @PostMapping("/orders/{orderId}/cancel")
    public OrderResponse cancelOrder(@AuthenticationPrincipal AuthenticatedUser me,
                                     @PathVariable OrderId orderId) {
        return orderMapper.toResponse(port.cancelOrder(me.userId(), orderId));
    }

    @GetMapping("/orders/{orderId}")
    public OrderResponse findOrder(@AuthenticationPrincipal AuthenticatedUser me,
                                   @PathVariable OrderId orderId) {
        return orderMapper.toResponse(port.findOrder(me.userId(), orderId));
    }

    @GetMapping("/orders")
    public List<OrderResponse> findOrders(@AuthenticationPrincipal AuthenticatedUser me) {
        return port.findOrders(me.userId()).stream().map(orderMapper::toResponse).toList();
    }

    @GetMapping("/orders/{orderId}/invoice")
    public InvoiceResponse findInvoice(@AuthenticationPrincipal AuthenticatedUser me,
                                       @PathVariable OrderId orderId) {
        return invoiceMapper.toResponse(port.findInvoice(me.userId(), orderId));
    }

    @GetMapping("/orders/{orderId}/shipments")
    public List<ShipmentResponse> findShipments(@AuthenticationPrincipal AuthenticatedUser me,
                                                @PathVariable OrderId orderId) {
        return port.findShipments(me.userId(), orderId).stream()
            .map(shipmentMapper::toResponse).toList();
    }

    @PostMapping("/orders/{orderId}/returns")
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnResponse requestReturn(@AuthenticationPrincipal AuthenticatedUser me,
                                        @PathVariable OrderId orderId,
                                        @Valid @RequestBody CreateReturnRequest request) {
        return returnMapper.toResponse(port.requestReturn(me.userId(), orderId,
            ProductId.of(request.productId()), Quantity.of(request.quantity()), request.reason()));
    }

    @GetMapping("/orders/{orderId}/returns")
    public List<ReturnResponse> findReturns(@AuthenticationPrincipal AuthenticatedUser me,
                                            @PathVariable OrderId orderId) {
        return port.findReturns(me.userId(), orderId).stream().map(returnMapper::toResponse).toList();
    }

    @GetMapping("/orders/{orderId}/refunds")
    public List<RefundResponse> findRefunds(@AuthenticationPrincipal AuthenticatedUser me,
                                            @PathVariable OrderId orderId) {
        return port.findRefunds(me.userId(), orderId).stream().map(refundMapper::toResponse).toList();
    }
}
