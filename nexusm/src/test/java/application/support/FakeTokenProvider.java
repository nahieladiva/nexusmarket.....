package application.support;

import application.domain.models.TokenClaims;
import application.domain.ports.out.TokenProviderPort;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Issues opaque "token-N" tokens and remembers them. It does not check expiration (the use case does). */
public class FakeTokenProvider implements TokenProviderPort {
    private final Map<String, TokenClaims> issued = new HashMap<>();
    private int counter;

    @Override public String issue(TokenClaims claims) {
        String token = "token-" + (++counter);
        issued.put(token, claims);
        return token;
    }
    @Override public Optional<TokenClaims> parse(String token) { return Optional.ofNullable(issued.get(token)); }
}
