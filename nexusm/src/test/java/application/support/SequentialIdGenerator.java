package application.support;

import application.domain.ports.out.IdGeneratorPort;
import java.util.UUID;

public class SequentialIdGenerator implements IdGeneratorPort {
    private long counter;

    @Override public UUID newId() { return new UUID(0L, ++counter); }
}
