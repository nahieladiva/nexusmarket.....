package application.adapters.in.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.domain.enums.UserRole;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * RG-01, RG-02 and RG-03 at the HTTP level: anonymous and the 5 roles against every prefix.
 */
class SecurityMatrixTest extends RestApiTest {

    /** A read-only route of each prefix that belongs to a single role. */
    private static final Map<UserRole, String> ROUTE_OF_ROLE = new EnumMap<>(Map.of(
        UserRole.BUYER, "/api/v1/buyer/orders",
        UserRole.SELLER, "/api/v1/seller/products",
        UserRole.LOGISTICS_OPERATOR, "/api/v1/logistics/orders",
        UserRole.ADMINISTRATOR, "/api/v1/admin/users",
        UserRole.SUPERVISOR, "/api/v1/supervisor/users"));

    private static final String ACCOUNT_ROUTE = "/api/v1/account/profile";
    private static final String PUBLIC_ROUTE = "/api/v1/public/products";

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void eachRoleOnlyEntersItsOwnPrefix(UserRole role) throws Exception {
        String token = tokenFor(role);

        for (Map.Entry<UserRole, String> route : ROUTE_OF_ROLE.entrySet()) {
            if (route.getKey() == role) {
                send(get(route.getValue()), token).andExpect(status().isOk());
            } else {
                send(get(route.getValue()), token)
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.path").value(route.getValue()));
            }
        }
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void everyRoleEntersItsAccountAndThePublicCatalog(UserRole role) throws Exception {
        String token = tokenFor(role);

        send(get(ACCOUNT_ROUTE), token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value(role.name()));
        send(get(PUBLIC_ROUTE), token).andExpect(status().isOk());
    }

    @Test
    void anonymousOnlyEntersThePublicPrefix() throws Exception {
        send(get(PUBLIC_ROUTE), null).andExpect(status().isOk());
        send(get(ACCOUNT_ROUTE), null)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
        for (String route : ROUTE_OF_ROLE.values()) {
            send(get(route), null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.path").value(route));
        }
    }

    @Test
    void aMalformedTokenIsRejected() throws Exception {
        send(get(ACCOUNT_ROUTE), "not-a-token").andExpect(status().isUnauthorized());
    }

    @Test
    void aTokenStopsWorkingAfterLogout() throws Exception {
        String token = tokenFor(UserRole.BUYER);
        send(post("/api/v1/account/logout"), token).andExpect(status().isOk());

        send(get(ACCOUNT_ROUTE), token)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Token was revoked or expired"));
    }

    @Test
    void routesOutsideThePrefixesAreDenied() throws Exception {
        send(get("/api/v1/unknown"), null).andExpect(status().isUnauthorized());
        send(get("/api/v1/unknown"), adminToken()).andExpect(status().isForbidden());
    }
}
