package application.services;

import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.InvalidTokenException;
import application.domain.models.AuthSession;
import application.domain.models.Buyer;
import application.domain.models.IssuedToken;
import application.domain.models.Seller;
import application.domain.models.TokenClaims;
import application.domain.models.User;
import application.domain.ports.in.AuthenticationPort;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.PasswordEncoderPort;
import application.domain.ports.out.TokenProviderPort;
import application.domain.ports.out.TokenStorePort;
import application.domain.ports.out.UserRepository;
import application.domain.services.user.PasswordPolicy;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de autenticación.
 *
 * <p>Reglas: email único; contraseña según {@link PasswordPolicy}; el token dura
 * {@link #TOKEN_LIFETIME}; un token revocado se rechaza aunque no haya expirado.</p>
 */
@Service
@Transactional
public class AuthenticationApplicationService implements AuthenticationPort {

    public static final Duration TOKEN_LIFETIME = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;
    private final TokenStorePort tokenStore;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;
    private final PasswordPolicy passwordPolicy;

    public AuthenticationApplicationService(UserRepository userRepository,
                                            PasswordEncoderPort passwordEncoder,
                                            TokenProviderPort tokenProvider,
                                            TokenStorePort tokenStore,
                                            ClockPort clock,
                                            IdGeneratorPort idGenerator,
                                            PasswordPolicy passwordPolicy) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
        this.tokenProvider = Objects.requireNonNull(tokenProvider);
        this.tokenStore = Objects.requireNonNull(tokenStore);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.passwordPolicy = Objects.requireNonNull(passwordPolicy);
    }

    @Override
    public User registerBuyer(String fullName, Email email, PhoneNumber phone, String rawPassword,
                              Address defaultShippingAddress) {
        requireEmailAvailable(email);
        passwordPolicy.validate(rawPassword);
        Buyer buyer = new Buyer(UserId.of(idGenerator.newId()), fullName, email, phone,
            passwordEncoder.encode(rawPassword), defaultShippingAddress, List.of(), clock.now());
        return userRepository.save(buyer);
    }

    @Override
    public User registerSeller(String fullName, Email email, PhoneNumber phone, String rawPassword,
                               String businessName) {
        requireEmailAvailable(email);
        passwordPolicy.validate(rawPassword);
        Seller seller = Seller.register(UserId.of(idGenerator.newId()), fullName, email, phone,
            passwordEncoder.encode(rawPassword), businessName, clock.now());
        return userRepository.save(seller);
    }

    @Override
    public AuthSession login(Email email, String rawPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(InvalidCredentialsException::new);
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

    @Override
    public void logout(String token) {
        TokenClaims claims = validateToken(token);
        IssuedToken stored = tokenStore.findById(claims.tokenId())
            .orElseThrow(() -> new InvalidTokenException("El token no está registrado"));
        stored.revoke();
        tokenStore.save(stored);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenClaims validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException("El token es obligatorio");
        }
        TokenClaims claims = tokenProvider.parse(token)
            .orElseThrow(() -> new InvalidTokenException("Token inválido o expirado"));
        IssuedToken stored = tokenStore.findById(claims.tokenId())
            .orElseThrow(() -> new InvalidTokenException("El token no está registrado"));
        if (!stored.isUsableAt(clock.now())) {
            throw new InvalidTokenException("El token fue revocado o expiró");
        }
        return claims;
    }

    private void requireEmailAvailable(Email email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("Ya existe un usuario con el email " + email);
        }
    }
}
