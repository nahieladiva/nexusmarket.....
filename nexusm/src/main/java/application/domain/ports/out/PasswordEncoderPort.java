package application.domain.ports.out;

/**
 * Puerto de salida 11/15 — hash de contraseñas, sin acoplar el dominio a Spring Security.
 */
public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
