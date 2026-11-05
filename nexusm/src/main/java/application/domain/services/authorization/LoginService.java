package application.domain.services.authorization;

import application.domain.exceptions.InvalidCredentialsException;
import application.domain.models.AuthSession;
import application.domain.models.IssuedToken;
import application.domain.models.TokenClaims;
import application.domain.models.User;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.PasswordEncoderPort;
import application.domain.ports.out.TokenProviderPort;
import application.domain.ports.out.TokenStorePort;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.Email;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Validates the credentials of a user, issues a JWT and stores it so that it can be revoked.
 */
@DomainService
public class LoginService {

    public static final Duration TOKEN_LIFETIME = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;
    private final TokenStorePort tokenStore;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public LoginService(UserRepository userRepository, PasswordEncoderPort passwordEncoder,
                        TokenProviderPort tokenProvider, TokenStorePort tokenStore, ClockPort clock,
                        IdGeneratorPort idGenerator) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
        this.tokenProvider = Objects.requireNonNull(tokenProvider);
        this.tokenStore = Objects.requireNonNull(tokenStore);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public AuthSession execute(Email email, String rawPassword) {
        User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        if (rawPassword == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        LocalDateTime issuedAt = clock.now();
        LocalDateTime expiresAt = issuedAt.plus(TOKEN_LIFETIME);
        TokenClaims claims = new TokenClaims(idGenerator.newId().toString(), user.getId(),
            user.getRole(), issuedAt, expiresAt);
        String token = tokenProvider.issue(claims);
        tokenStore.save(IssuedToken.issue(claims.tokenId(), user.getId(), issuedAt, expiresAt));
        return new AuthSession(token, expiresAt, user);
    }
}
