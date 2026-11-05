package application.domain.services.user;

import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.PasswordEncoderPort;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;

import java.util.List;
import java.util.Objects;

/**
 * Public registration of a buyer, the only role that registers without being authenticated (RG-01).
 */
@DomainService
public class RegisterBuyerService {

    private final UserRepository userRepository;
    private final ValidateUserUniquenessService validateUserUniquenessService;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoderPort passwordEncoder;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public RegisterBuyerService(UserRepository userRepository,
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

    public User execute(String fullName, Email email, PhoneNumber phone, String rawPassword,
                        Address defaultShippingAddress) {
        validateUserUniquenessService.execute(email);
        passwordPolicy.validate(rawPassword);
        return userRepository.save(new Buyer(UserId.of(idGenerator.newId()), fullName, email, phone,
            passwordEncoder.encode(rawPassword), defaultShippingAddress, List.of(), clock.now()));
    }
}
