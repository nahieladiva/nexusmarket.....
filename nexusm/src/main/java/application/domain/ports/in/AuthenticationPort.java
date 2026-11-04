package application.domain.ports.in;

import application.domain.models.AuthSession;
import application.domain.models.TokenClaims;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;

/**
 * Puerto de entrada 1/7 — registro, inicio y cierre de sesión con JWT.
 */
public interface AuthenticationPort {

    /** Registra un comprador. El email debe ser único y la contraseña cumplir la política. */
    User registerBuyer(String fullName, Email email, PhoneNumber phone, String rawPassword,
                       Address defaultShippingAddress);

    /** Registra un vendedor. Queda pendiente de aprobación por un administrador. */
    User registerSeller(String fullName, Email email, PhoneNumber phone, String rawPassword,
                        String businessName);

    /** Valida credenciales y emite un JWT de 1 hora, que queda guardado en la base de tokens. */
    AuthSession login(Email email, String rawPassword);

    /** Revoca el token: deja de ser válido aunque no haya expirado. */
    void logout(String token);

    /** Verifica firma, expiración y que el token no esté revocado. */
    TokenClaims validateToken(String token);
}
