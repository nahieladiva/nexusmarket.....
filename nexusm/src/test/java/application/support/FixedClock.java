package application.support;

import application.domain.ports.out.ClockPort;
import java.time.Duration;
import java.time.LocalDateTime;

public class FixedClock implements ClockPort {
    private LocalDateTime now;

    public FixedClock(LocalDateTime now) { this.now = now; }
    @Override public LocalDateTime now() { return now; }
    public void advance(Duration duration) { now = now.plus(duration); }
}
