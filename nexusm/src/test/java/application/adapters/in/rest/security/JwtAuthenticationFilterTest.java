package application.adapters.in.rest.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import application.domain.enums.UserRole;
import application.domain.exceptions.InvalidTokenException;
import application.domain.models.TokenClaims;
import application.domain.ports.in.AccountPort;
import application.domain.valueobjects.UserId;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Tests of the JWT filter: each role becomes its Spring Security authority.
 */
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "a-token";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 2, 10, 0);

    private final AccountPort accountPort = mock(AccountPort.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(accountPort);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void everyRoleBecomesItsAuthority(UserRole role) throws ServletException, IOException {
        UserId userId = UserId.random();
        when(accountPort.validateToken(TOKEN))
            .thenReturn(new TokenClaims("token-id", userId, role, NOW, NOW.plusHours(1)));

        MockFilterChain chain = filter(TOKEN);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(List.of("ROLE_" + role.name()), authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).toList());
        assertEquals(new AuthenticatedUser(userId, role, TOKEN), authentication.getPrincipal());
        assertNotNull(chain.getRequest());
    }

    @Test
    void aRevokedTokenLeavesTheRequestAnonymous() throws ServletException, IOException {
        when(accountPort.validateToken(TOKEN)).thenThrow(new InvalidTokenException("Token was revoked"));

        MockFilterChain chain = filter(TOKEN);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
    }

    @Test
    void aRequestWithoutTokenStaysAnonymous() throws ServletException, IOException {
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
    }

    private MockFilterChain filter(String token) throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        return chain;
    }
}
