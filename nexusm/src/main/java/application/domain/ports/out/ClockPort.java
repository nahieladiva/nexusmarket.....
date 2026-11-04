package application.domain.ports.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Puerto de salida 14/15 — hora actual. Permite pruebas deterministas.
 */
public interface ClockPort {

    LocalDateTime now();

    default LocalDate today() {
        return now().toLocalDate();
    }
}
