package application.domain.ports.in;

import application.domain.models.Order;
import application.domain.models.OrderLine;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.UserId;
import java.util.List;

/**
 * Puerto de entrada 6/7 — pedidos.
 */
public interface OrderManagementPort {

    /**
     * Crea un pedido pendiente. Los precios se toman del catálogo (con promoción o
     * descuento por volumen, el mejor de los dos), nunca del cliente.
     */
    Order placeOrder(UserId buyerId, List<OrderLine> lines);

    /** Administrador: asigna bodega y reserva stock. */
    Order confirmOrder(OrderId id);

    /** Administrador. */
    Order shipOrder(OrderId id);

    /** Administrador. */
    Order deliverOrder(OrderId id);

    /** Comprador dueño o administrador. Si estaba confirmado, devuelve el stock. */
    Order cancelOrder(UserId actorId, OrderId id);

    /** Comprador dueño, vendedor con productos en el pedido, o administrador. */
    Order findOrderById(UserId actorId, OrderId id);

    List<Order> findOrdersByBuyer(UserId buyerId);

    /** Pedidos que contienen al menos un producto del vendedor. */
    List<Order> findOrdersForSeller(UserId sellerId);

    /** Administrador. */
    List<Order> findAllOrders();
}
