package application.support;

import application.domain.models.Order;
import application.domain.ports.out.OrderRepository;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.UserId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryOrderRepository implements OrderRepository {
    private final Map<OrderId, Order> data = new LinkedHashMap<>();

    @Override public Order save(Order order) { data.put(order.getId(), order); return order; }
    @Override public Optional<Order> findById(OrderId id) { return Optional.ofNullable(data.get(id)); }
    @Override public List<Order> findByBuyerId(UserId buyerId) {
        return data.values().stream().filter(o -> o.getBuyerId().equals(buyerId)).toList();
    }
    @Override public List<Order> findAll() { return new ArrayList<>(data.values()); }
}
