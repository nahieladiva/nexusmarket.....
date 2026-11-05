package application.domain.services.order;

import application.domain.enums.OrderStatus;
import application.domain.events.OrderStatusChangedEvent;
import application.domain.models.Order;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.DomainEventPublisherPort;
import application.domain.ports.out.NotificationPort;
import application.domain.services.DomainService;

import java.util.Objects;

/**
 * Publishes the status change of an order and notifies its buyer.
 */
@DomainService
public class NotifyOrderStatusChangeService {

    private final DomainEventPublisherPort eventPublisher;
    private final NotificationPort notificationPort;
    private final ClockPort clock;

    public NotifyOrderStatusChangeService(DomainEventPublisherPort eventPublisher,
                                          NotificationPort notificationPort, ClockPort clock) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.notificationPort = Objects.requireNonNull(notificationPort);
        this.clock = Objects.requireNonNull(clock);
    }

    public void execute(Order order, OrderStatus previous) {
        eventPublisher.publish(new OrderStatusChangedEvent(
            order.getId(), previous, order.getStatus(), clock.now()));
        notificationPort.notify(order.getBuyerId(), "Your order changed status",
            "Order " + order.getId() + " moved from " + previous + " to " + order.getStatus() + ".");
    }
}
