package application.support;

import application.domain.enums.TokenStatus;
import application.domain.models.IssuedToken;
import application.domain.ports.out.TokenStorePort;
import application.domain.valueobjects.UserId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryTokenStore implements TokenStorePort {
    private final Map<String, IssuedToken> data = new HashMap<>();

    @Override public IssuedToken save(IssuedToken token) { data.put(token.getTokenId(), token); return token; }
    @Override public Optional<IssuedToken> findById(String tokenId) { return Optional.ofNullable(data.get(tokenId)); }

    @Override
    public void revokeAllForUser(UserId userId) {
        data.values().stream()
            .filter(token -> token.getUserId().equals(userId) && token.getStatus() == TokenStatus.ACTIVE)
            .forEach(IssuedToken::revoke);
    }
}
