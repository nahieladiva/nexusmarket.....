package application.domain.ports.in;

import application.domain.models.Cart;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;

import java.util.List;

/**
 * Input port of the buyer: cart, orders, payment and post-sale.
 */
public interface BuyerPort {

    Cart findCart(UserId buyerId);

    Cart addCartItem(UserId buyerId, ProductId productId, Quantity quantity);

    Cart changeCartItemQuantity(UserId buyerId, ProductId productId, Quantity quantity);

    Cart removeCartItem(UserId buyerId, ProductId productId);

    Cart clearCart(UserId buyerId);

    Order placeOrder(UserId buyerId);

    Order payOrder(UserId buyerId, OrderId orderId);

    Order cancelOrder(UserId buyerId, OrderId orderId);

    Order findOrder(UserId buyerId, OrderId orderId);

    List<Order> findOrders(UserId buyerId);

    Invoice findInvoice(UserId buyerId, OrderId orderId);

    List<Shipment> findShipments(UserId buyerId, OrderId orderId);

    ReturnRequest requestReturn(UserId buyerId, OrderId orderId, ProductId productId, Quantity quantity,
                                String reason);

    List<ReturnRequest> findReturns(UserId buyerId, OrderId orderId);

    List<Refund> findRefunds(UserId buyerId, OrderId orderId);
}
