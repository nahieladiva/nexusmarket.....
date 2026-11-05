package application.domain.services.order;

import application.domain.events.OrderCreatedEvent;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Cart;
import application.domain.models.CartItem;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.out.CartRepository;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.DomainEventPublisherPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ProductRepository;
import application.domain.services.DomainService;
import application.domain.services.WarehouseAllocationService;
import application.domain.services.cart.ConsultCartService;
import application.domain.valueobjects.OrderId;

import java.util.Objects;

/**
 * Turns the cart of a buyer into an order pending payment.
 *
 * <p>Every product must still be published, and an active warehouse must have stock. Stock is
 * not reserved yet: that happens when the order is paid. The order keeps the prices of the cart.</p>
 */
@DomainService
public class PlaceOrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ConsultCartService consultCartService;
    private final WarehouseAllocationService warehouseAllocationService;
    private final DomainEventPublisherPort eventPublisher;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public PlaceOrderService(CartRepository cartRepository, OrderRepository orderRepository,
                             ProductRepository productRepository,
                             ConsultCartService consultCartService,
                             WarehouseAllocationService warehouseAllocationService,
                             DomainEventPublisherPort eventPublisher, ClockPort clock,
                             IdGeneratorPort idGenerator) {
        this.cartRepository = Objects.requireNonNull(cartRepository);
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
        this.consultCartService = Objects.requireNonNull(consultCartService);
        this.warehouseAllocationService = Objects.requireNonNull(warehouseAllocationService);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public Order execute(User buyer) {
        Cart cart = consultCartService.execute(buyer);
        cart.getItems().forEach(this::requireAvailable);
        warehouseAllocationService.allocate(
            cart.getItems().stream().map(CartItem::toOrderItem).toList());
        Order order = cart.checkout(OrderId.of(idGenerator.newId()), clock.now());
        Order saved = orderRepository.save(order);
        cartRepository.save(cart);
        eventPublisher.publish(new OrderCreatedEvent(
            saved.getId(), saved.getBuyerId(), saved.getTotal(), clock.now()));
        return saved;
    }

    private void requireAvailable(CartItem item) {
        Product product = productRepository.findById(item.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product", item.getProductId().toString()));
        if (!product.isActive()) {
            throw new BusinessRuleException("Product " + product.getName() + " is not available");
        }
    }
}
