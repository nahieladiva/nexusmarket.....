package application.domain.ports.out;

import application.domain.models.Order;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Output port 5/15 — orders (MySQL).
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId id);

    List<Order> findByBuyerId(UserId buyerId);

    List<Order> findAll();
}