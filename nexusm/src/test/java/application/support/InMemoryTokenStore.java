package application.support;

import application.domain.models.IssuedToken;
import application.domain.ports.out.TokenStorePort;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryTokenStore implements TokenStorePort {
    private final Map<String, IssuedToken> data = new HashMap<>();

    @Override public IssuedToken save(IssuedToken token) { data.put(token.getTokenId(), token); return token; }
    @Override public Optional<IssuedToken> findById(String tokenId) { return Optional.ofNullable(data.get(tokenId)); }
}
