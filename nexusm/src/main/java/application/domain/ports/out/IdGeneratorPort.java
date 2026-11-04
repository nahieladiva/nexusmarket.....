package application.domain.ports.out;

import java.util.UUID;

/**
 * Puerto de salida 15/15 — generación de identificadores fuera del dominio.
 */
public interface IdGeneratorPort {

    UUID newId();
}
