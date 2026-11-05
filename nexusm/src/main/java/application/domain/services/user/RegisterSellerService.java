package application.domain.services.user;

import application.domain.enums.UserRole;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.PasswordEncoderPort;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;

import java.util.Objects;

/**
 * An administrator registers a seller, who starts as {@code PENDING_APPROVAL}.
 */
@DomainService
public class RegisterSellerService {

    private final UserRepository userRepository;
    private final ValidateUserUniquenessService validateUserUniquenessService;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoderPort passwordEncoder;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public RegisterSellerService(UserRepository userRepository,
                                 ValidateUserUniquenessService validateUserUniquenessService,
                                 PasswordPolicy passwordPolicy, PasswordEncoderPort passwordEncoder,
                                 ClockPort clock, IdGeneratorPort idGenerator) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.validateUserUniquenessService = Objects.requireNonNull(validateUserUniquenessService);
        this.passwordPolicy = Objects.requireNonNull(passwordPolicy);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public User execute(User administrator, String fullName, Email email, PhoneNumber phone,
                        String rawPassword, String businessName) {
        administrator.requireRole(UserRole.ADMINISTRATOR);
        validateUserUniquenessService.execute(email);
        passwordPolicy.validate(rawPassword);
        return userRepository.save(Seller.register(UserId.of(idGenerator.newId()), fullName, email, phone,
            passwordEncoder.encode(rawPassword), businessName, clock.now()));
    }
}
