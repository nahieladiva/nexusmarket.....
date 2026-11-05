package application.services;

import application.domain.enums.OrderStatus;
import application.domain.enums.StockMovementType;
import application.domain.events.OrderCreatedEvent;
import application.domain.events.OrderStatusChangedEvent;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.OrderLine;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.in.OrderManagementPort;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.DomainEventPublisherPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.NotificationPort;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ProductRepository;
import application.domain.services.order.OrderFulfillmentDomainService;
import application.domain.services.WarehouseAllocationService;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de pedidos.
 *
 * <p>Reglas: solo compradores crean pedidos; solo productos activos; máximo 10 unidades por
 * producto; debe existir una bodega activa con stock suficiente; el precio sale del catálogo
 * (promoción vigente o descuento por volumen, el que sea mejor para el comprador); el stock
 * se reserva al confirmar y se devuelve al cancelar un pedido confirmado.</p>
 */
@Service
@Transactional
public class OrderApplicationService implements OrderManagementPort {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderFulfillmentDomainService fulfillment;
    private final WarehouseAllocationService allocationService;
    private final StockMovementRecorder recorder;
    private final AccessRules accessRules;
    private final DomainEventPublisherPort eventPublisher;
    private final NotificationPort notificationPort;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public OrderApplicationService(OrderRepository orderRepository,
                                   ProductRepository productRepository,
                                   OrderFulfillmentDomainService fulfillment,
                                   WarehouseAllocationService allocationService,
                                   StockMovementRecorder recorder,
                                   AccessRules accessRules,
                                   DomainEventPublisherPort eventPublisher,
                                   NotificationPort notificationPort,
                                   ClockPort clock,
                                   IdGeneratorPort idGenerator) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
        this.fulfillment = Objects.requireNonNull(fulfillment);
        this.allocationService = Objects.requireNonNull(allocationService);
        this.recorder = Objects.requireNonNull(recorder);
        this.accessRules = Objects.requireNonNull(accessRules);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.notificationPort = Objects.requireNonNull(notificationPort);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public Order placeOrder(UserId buyerId, List<OrderLine> lines) {
        User buyer = accessRules.requireUser(buyerId);
        if (!buyer.isBuyer()) {
            throw new BusinessRuleException("Solo los compradores pueden crear pedidos");
        }
        if (lines == null || lines.isEmpty()) {
            throw new BusinessRuleException("El pedido debe tener al menos un producto");
        }
        List<OrderItem> items = new ArrayList<>();
        for (OrderLine line : lines) {
            Product product = productRepository.findById(line.productId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Producto", line.productId().toString()));
            if (!product.isActive()) {
                throw new BusinessRuleException("El producto " + product.getName()
                    + " no está disponible");
            }
            items.add(new OrderItem(product.getId(), product.getType(), line.quantity(), product.getPrice()));
        }
        Order order = Order.create(OrderId.of(idGenerator.newId()), buyerId, items, clock.now());
        // Checks that an active warehouse has stock, without reserving it yet.
        allocationService.allocate(items);
        Order saved = orderRepository.save(order);
        eventPublisher.publish(new OrderCreatedEvent(
            saved.getId(), saved.getBuyerId(), saved.getTotal(), clock.now()));
        return saved;
    }

    @Override
    public Order confirmOrder(OrderId id) {
        Order order = requireOrder(id);
        OrderStatus previous = order.getStatus();
        List<Inventory> touched = fulfillment.confirm(order, clock.now());
        Order saved = orderRepository.save(order);
        List<OrderItem> shippable = saved.getItems().stream().filter(OrderItem::requiresShipment).toList();
        for (int i = 0; i < touched.size(); i++) {
            OrderItem item = shippable.get(i);
            recorder.record(touched.get(i), StockMovementType.RESERVATION,
                -item.getQuantity().getValue(), "Reserva por pedido confirmado", null,
                saved.getId().toString());
        }
        afterStatusChange(saved, previous);
        return saved;
    }

    @Override
    public Order shipOrder(OrderId id) {
        Order order = requireOrder(id);
        OrderStatus previous = order.getStatus();
        order.ship();
        Order saved = orderRepository.save(order);
        afterStatusChange(saved, previous);
        return saved;
    }

    @Override
    public Order deliverOrder(OrderId id) {
        Order order = requireOrder(id);
        OrderStatus previous = order.getStatus();
        order.deliver();
        Order saved = orderRepository.save(order);
        afterStatusChange(saved, previous);
        return saved;
    }

    @Override
    public Order cancelOrder(UserId actorId, OrderId id) {
        Order order = requireOrder(id);
        User actor = accessRules.requireUser(actorId);
        if (!actor.isAdministrator() && !order.belongsTo(actorId)) {
            throw new ForbiddenActionException(
                "Solo el comprador del pedido o un administrador puede cancelarlo");
        }
        OrderStatus previous = order.getStatus();
        List<Inventory> touched = fulfillment.cancel(order);
        Order saved = orderRepository.save(order);
        List<OrderItem> shippable = saved.getItems().stream().filter(OrderItem::requiresShipment).toList();
        for (int i = 0; i < touched.size(); i++) {
            OrderItem item = shippable.get(i);
            recorder.record(touched.get(i), StockMovementType.RELEASE,
                item.getQuantity().getValue(), "Devolución por pedido cancelado", actorId,
                saved.getId().toString());
        }
        afterStatusChange(saved, previous);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Order findOrderById(UserId actorId, OrderId id) {
        Order order = requireOrder(id);
        User actor = accessRules.requireUser(actorId);
        boolean allowed = actor.isAdministrator()
            || order.belongsTo(actorId)
            || (actor.isSeller() && order.containsAnyProduct(productIdsOf(actorId)));
        if (!allowed) {
            throw new ForbiddenActionException("No tiene permiso para ver este pedido");
        }
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findOrdersByBuyer(UserId buyerId) {
        return orderRepository.findByBuyerId(buyerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findOrdersForSeller(UserId sellerId) {
        Set<ProductId> productIds = productIdsOf(sellerId);
        if (productIds.isEmpty()) {
            return List.of();
        }
        return orderRepository.findAll().stream()
            .filter(order -> order.containsAnyProduct(productIds))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    private Order requireOrder(OrderId id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido", id.toString()));
    }

    private Set<ProductId> productIdsOf(UserId sellerId) {
        return productRepository.findBySellerId(sellerId).stream()
            .map(Product::getId)
            .collect(Collectors.toSet());
    }

    private void afterStatusChange(Order order, OrderStatus previous) {
        eventPublisher.publish(new OrderStatusChangedEvent(
            order.getId(), previous, order.getStatus(), clock.now()));
        notificationPort.notify(order.getBuyerId(), "Tu pedido cambió de estado",
            "El pedido " + order.getId() + " pasó de " + previous + " a " + order.getStatus() + ".");
    }
}
