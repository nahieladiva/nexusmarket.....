package application.adapters.out.security;

import application.domain.enums.UserRole;
import application.domain.models.TokenClaims;
import application.domain.ports.out.TokenProviderPort;
import application.domain.valueobjects.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptador de {@link TokenProviderPort} con la librería jjwt (HMAC-SHA256).
 */
@Component
public class JjwtTokenProviderAdapter implements TokenProviderPort {

    private static final String ROLE_CLAIM = "role";
    private static final String ISSUER = "nexusmarket";

    private final SecretKey key;
    private final ZoneId zone = ZoneId.systemDefault();

    public JjwtTokenProviderAdapter(@Value("${nexusmarket.jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("nexusmarket.jwt.secret debe tener al menos 32 caracteres");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    @Override
    public String issue(TokenClaims claims) {
        return Jwts.builder()
            .id(claims.tokenId())
            .issuer(ISSUER)
            .subject(claims.userId().toString())
            .claim(ROLE_CLAIM, claims.role().name())
            .issuedAt(toDate(claims.issuedAt()))
            .expiration(toDate(claims.expiresAt()))
            .signWith(key)
            .compact();
    }

    @Override
    public Optional<TokenClaims> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return Optional.of(new TokenClaims(
                claims.getId(),
                UserId.of(claims.getSubject()),
                UserRole.valueOf(claims.get(ROLE_CLAIM, String.class)),
                toLocal(claims.getIssuedAt()),
                toLocal(claims.getExpiration())));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private Date toDate(LocalDateTime value) {
        return Date.from(value.atZone(zone).toInstant());
    }

    private LocalDateTime toLocal(Date value) {
        return LocalDateTime.ofInstant(value.toInstant(), zone);
    }
}
