package application.domain.ports.out;

import java.util.UUID;

/**
 * Output port 15/15 — identifier generation outside the domain.
 */
public interface IdGeneratorPort {

    UUID newId();
}
