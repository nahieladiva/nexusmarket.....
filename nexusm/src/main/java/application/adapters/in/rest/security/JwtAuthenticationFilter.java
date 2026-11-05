package application.adapters.in.rest.security;

import application.domain.exceptions.InvalidTokenException;
import application.domain.models.TokenClaims;
import application.domain.ports.in.AccountPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee el header {@code Authorization: Bearer <token>}, lo valida con el puerto de
 * autenticación (firma, expiración y que no esté revocado) y deja al usuario autenticado
 * en el contexto de seguridad. Si el token es inválido, la petición sigue como anónima y
 * Spring Security responde 401 en las rutas protegidas.
 *
 * <p>No es {@code @Component} a propósito: se registra solo dentro de la cadena de
 * seguridad, para que no se ejecute dos veces.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final AccountPort accountPort;

    public JwtAuthenticationFilter(AccountPort accountPort) {
        this.accountPort = Objects.requireNonNull(accountPort);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER)) {
            String token = header.substring(BEARER.length()).trim();
            try {
                TokenClaims claims = accountPort.validateToken(token);
                AuthenticatedUser principal = new AuthenticatedUser(claims.userId(), claims.role(), token);
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + claims.role().name())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (InvalidTokenException ex) {
                SecurityContextHolder.clearContext();
                request.setAttribute("auth_error", ex.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}
