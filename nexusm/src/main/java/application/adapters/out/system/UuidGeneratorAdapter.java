package application.adapters.out.system;

import application.domain.ports.out.IdGeneratorPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adapter of {@link IdGeneratorPort} with random UUIDs.
 */
@Component
public class UuidGeneratorAdapter implements IdGeneratorPort {

    @Override
    public UUID newId() {
        return UUID.randomUUID();
    }
}
