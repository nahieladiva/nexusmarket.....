package application.adapters.out.system;

import application.domain.ports.out.ClockPort;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Adapter of {@link ClockPort} on the system clock.
 */
@Component
public class SystemClockAdapter implements ClockPort {

    private final Clock clock;

    public SystemClockAdapter(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
