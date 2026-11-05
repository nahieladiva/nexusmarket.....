package application.adapters.useCases;

import application.domain.models.Cart;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.ports.in.BuyerPort;
import application.domain.services.authorization.ResolveActorService;
import application.domain.services.cart.AddItemToCartService;
import application.domain.services.cart.ChangeCartItemQuantityService;
import application.domain.services.cart.ClearCartService;
import application.domain.services.cart.ConsultCartService;
import application.domain.services.cart.RemoveItemFromCartService;
import application.domain.services.invoice.ConsultInvoiceService;
import application.domain.services.order.CancelOrderService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.order.PayOrderService;
import application.domain.services.order.PlaceOrderService;
import application.domain.services.refund.ConsultRefundService;
import application.domain.services.returns.ConsultReturnService;
import application.domain.services.returns.RequestReturnService;
import application.domain.services.shipment.ConsultShipmentService;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements {@link BuyerPort} by delegating to the domain services.
 */
@Service
@RequiredArgsConstructor
public class BuyerUseCase implements BuyerPort {

    private final ResolveActorService resolveActor;
    private final ConsultCartService consultCart;
    private final AddItemToCartService addItemToCart;
    private final ChangeCartItemQuantityService changeCartItemQuantity;
    private final RemoveItemFromCartService removeItemFromCart;
    private final ClearCartService clearCart;
    private final PlaceOrderService placeOrder;
    private final PayOrderService payOrder;
    private final CancelOrderService cancelOrder;
    private final ConsultOrderService consultOrder;
    private final ConsultInvoiceService consultInvoice;
    private final ConsultShipmentService consultShipment;
    private final RequestReturnService requestReturn;
    private final ConsultReturnService consultReturn;
    private final ConsultRefundService consultRefund;

    @Override
    public Cart findCart(UserId buyerId) {
        return consultCart.execute(resolveActor.execute(buyerId));
    }

    @Override
    public Cart addCartItem(UserId buyerId, ProductId productId, Quantity quantity) {
        return addItemToCart.execute(resolveActor.execute(buyerId), productId, quantity);
    }

    @Override
    public Cart changeCartItemQuantity(UserId buyerId, ProductId productId, Quantity quantity) {
        return changeCartItemQuantity.execute(resolveActor.execute(buyerId), productId, quantity);
    }

    @Override
    public Cart removeCartItem(UserId buyerId, ProductId productId) {
        return removeItemFromCart.execute(resolveActor.execute(buyerId), productId);
    }

    @Override
    public Cart clearCart(UserId buyerId) {
        return clearCart.execute(resolveActor.execute(buyerId));
    }

    @Override
    public Order placeOrder(UserId buyerId) {
        return placeOrder.execute(resolveActor.execute(buyerId));
    }

    @Override
    public Order payOrder(UserId buyerId, OrderId orderId) {
        return payOrder.execute(resolveActor.execute(buyerId), orderId);
    }

    @Override
    public Order cancelOrder(UserId buyerId, OrderId orderId) {
        return cancelOrder.execute(resolveActor.execute(buyerId), orderId);
    }

    @Override
    public Order findOrder(UserId buyerId, OrderId orderId) {
        return consultOrder.execute(resolveActor.execute(buyerId), orderId);
    }

    @Override
    public List<Order> findOrders(UserId buyerId) {
        return consultOrder.findByBuyer(resolveActor.execute(buyerId));
    }

    @Override
    public Invoice findInvoice(UserId buyerId, OrderId orderId) {
        return consultInvoice.findByOrder(resolveActor.execute(buyerId), orderId);
    }

    @Override
    public List<Shipment> findShipments(UserId buyerId, OrderId orderId) {
        return consultShipment.findByOrder(resolveActor.execute(buyerId), orderId);
    }

    @Override
    public ReturnRequest requestReturn(UserId buyerId, OrderId orderId, ProductId productId,
                                       Quantity quantity, String reason) {
        return requestReturn.execute(resolveActor.execute(buyerId), orderId, productId, quantity, reason);
    }

    @Override
    public List<ReturnRequest> findReturns(UserId buyerId, OrderId orderId) {
        return consultReturn.findByOrder(resolveActor.execute(buyerId), orderId);
    }

    @Override
    public List<Refund> findRefunds(UserId buyerId, OrderId orderId) {
        return consultRefund.findByOrder(resolveActor.execute(buyerId), orderId);
    }
}
