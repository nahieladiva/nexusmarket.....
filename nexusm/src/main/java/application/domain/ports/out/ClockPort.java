package application.domain.ports.out;

import java.time.LocalDateTime;

/**
 * Output port — current time. Enables deterministic tests.
 */
public interface ClockPort {

    LocalDateTime now();
}
