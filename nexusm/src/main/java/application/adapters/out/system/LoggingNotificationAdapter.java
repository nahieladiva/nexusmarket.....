package application.adapters.out.system;

import application.domain.ports.out.NotificationPort;
import application.domain.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adaptador de {@link NotificationPort} que escribe las notificaciones en el log.
 * Se puede reemplazar por uno de correo o SMS sin tocar el dominio.
 */
@Component
public class LoggingNotificationAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationAdapter.class);

    @Override
    public void notify(UserId recipient, String subject, String message) {
        log.info("[NOTIFICACIÓN] para={} asunto=\"{}\" mensaje=\"{}\"", recipient, subject, message);
    }
}
