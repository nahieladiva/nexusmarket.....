package application.domain.ports.out;

import java.util.UUID;

/**
 * Output port — identifier generation outside the domain.
 */
public interface IdGeneratorPort {

    UUID newId();
}
