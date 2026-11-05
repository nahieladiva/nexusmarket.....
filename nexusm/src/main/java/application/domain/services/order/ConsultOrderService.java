package application.domain.services.order;

import application.domain.enums.UserRole;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ProductRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Order queries, limited by role (RG-03): a buyer sees their orders, a seller sees the orders that
 * contain their products, and internal staff see every order.
 */
@DomainService
public class ConsultOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public ConsultOrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
    }

    public Order execute(User actor, OrderId orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId.toString()));
        boolean allowed = actor.getRole().isInternalStaff()
            || order.belongsTo(actor.getId())
            || (actor.isSeller() && order.containsAnyProduct(productIdsOf(actor.getId())));
        if (!allowed) {
            throw new ForbiddenActionException("You cannot see order " + orderId);
        }
        return order;
    }

    public List<Order> findByBuyer(User buyer) {
        buyer.requireRole(UserRole.BUYER);
        return orderRepository.findByBuyerId(buyer.getId());
    }

    public List<Order> findBySeller(User seller) {
        seller.requireRole(UserRole.SELLER);
        Set<ProductId> productIds = productIdsOf(seller.getId());
        return orderRepository.findAll().stream()
            .filter(order -> order.containsAnyProduct(productIds))
            .toList();
    }

    public List<Order> findAll(User actor) {
        actor.requireAnyRole(UserRole.ADMINISTRATOR, UserRole.SUPERVISOR, UserRole.LOGISTICS_OPERATOR);
        return orderRepository.findAll();
    }

    private Set<ProductId> productIdsOf(UserId sellerId) {
        return productRepository.findBySellerId(sellerId).stream()
            .map(Product::getId)
            .collect(Collectors.toSet());
    }
}
