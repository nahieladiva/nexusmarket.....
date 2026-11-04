package application.domain.ports.out;

/**
 * Output port — password hashing, without coupling the domain to Spring Security.
 */
public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
