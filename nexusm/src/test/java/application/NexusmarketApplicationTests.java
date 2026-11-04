package application;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Levanta el contexto completo de Spring. Necesita MySQL y MongoDB corriendo
 * ({@code docker compose up -d mysql mongo}); por eso está deshabilitada por defecto
 * y las reglas de negocio se prueban con los tests de {@code application.services}.
 * Para correrla, quite {@code @Disabled} con las bases de datos arriba.
 */
@Disabled("Requiere MySQL y MongoDB levantados con docker compose")
@SpringBootTest
class NexusmarketApplicationTests {

    @Test
    void contextLoads() {
    }
}
