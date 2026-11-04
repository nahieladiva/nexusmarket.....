package application.support;

import application.domain.models.TokenClaims;
import application.domain.ports.out.TokenProviderPort;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Emite tokens opacos "token-N" y los recuerda. No verifica expiración (eso lo hace el caso de uso). */
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
