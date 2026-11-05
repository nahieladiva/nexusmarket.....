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
 * Reads the {@code Authorization: Bearer <token>} header, validates it with the account port
 * (signature, expiration and not revoked) and puts the authenticated user in the security
 * context. If the token is invalid, the request continues as anonymous and Spring Security
 * answers 401 on protected routes.
 *
 * <p>It is not a {@code @Component} on purpose: it is registered only inside the security
 * chain, so that it does not run twice.</p>
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
