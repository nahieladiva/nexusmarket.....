package application.domain.ports.out;

import application.domain.valueobjects.UserId;

/**
 * Output port — user notifications (low stock, order status changes).
 */
public interface NotificationPort {

    void notify(UserId recipient, String subject, String message);
}
