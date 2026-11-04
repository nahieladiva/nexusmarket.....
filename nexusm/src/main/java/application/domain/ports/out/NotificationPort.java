package application.domain.ports.out;

import application.domain.valueobjects.UserId;

/**
 * Puerto de salida 13/15 — avisos a usuarios (stock bajo, cambios de estado de pedidos).
 */
public interface NotificationPort {

    void notify(UserId recipient, String subject, String message);
}
