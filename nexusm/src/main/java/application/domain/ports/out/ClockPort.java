package application.domain.ports.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Output port 14/15 — current time. Enables deterministic tests.
 */
public interface ClockPort {

    LocalDateTime now();

    default LocalDate today() {
        return now().toLocalDate();
    }
}
