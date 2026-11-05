package application.adapters.out.system;

import application.domain.ports.out.NotificationPort;
import application.domain.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adapter of {@link NotificationPort} that writes the notifications to the log.
 * It can be replaced by an email or SMS adapter without touching the domain.
 */
@Component
public class LoggingNotificationAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationAdapter.class);

    @Override
    public void notify(UserId recipient, String subject, String message) {
        log.info("[NOTIFICATION] to={} subject=\"{}\" message=\"{}\"", recipient, subject, message);
    }
}
