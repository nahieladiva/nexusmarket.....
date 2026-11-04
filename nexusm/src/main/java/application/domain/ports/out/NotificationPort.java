package application.domain.ports.out;

import application.domain.valueobjects.UserId;

/**
 * Output port 13/15 — user notifications (low stock, order status changes).
 */
public interface NotificationPort {

    void notify(UserId recipient, String subject, String message);
}
