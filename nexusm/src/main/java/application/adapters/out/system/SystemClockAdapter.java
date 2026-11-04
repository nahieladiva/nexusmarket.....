package application.adapters.out.system;

import application.domain.ports.out.ClockPort;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Adaptador de {@link ClockPort} sobre el reloj del sistema.
 */
@Component
public class SystemClockAdapter implements ClockPort {

    private final Clock clock;

    public SystemClockAdapter(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock es obligatorio");
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
