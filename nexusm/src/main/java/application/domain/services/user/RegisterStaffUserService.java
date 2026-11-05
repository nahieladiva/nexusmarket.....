package application.domain.services.user;

import application.domain.enums.UserRole;
import application.domain.exceptions.BusinessRuleException;
import application.domain.models.Administrator;
import application.domain.models.LogisticsOperator;
import application.domain.models.Supervisor;
import application.domain.models.User;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.PasswordEncoderPort;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;

import java.util.List;
import java.util.Objects;

/**
 * An administrator registers an internal staff user: logistics operator, administrator or supervisor.
 */
@DomainService
public class RegisterStaffUserService {

    private final UserRepository userRepository;
    private final ValidateUserUniquenessService validateUserUniquenessService;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoderPort passwordEncoder;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public RegisterStaffUserService(UserRepository userRepository,
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

    public User execute(User administrator, UserRole role, String fullName, Email email,
                        PhoneNumber phone, String rawPassword) {
        administrator.requireRole(UserRole.ADMINISTRATOR);
        if (!role.isInternalStaff()) {
            throw new BusinessRuleException("Role " + role + " is not internal staff");
        }
        validateUserUniquenessService.execute(email);
        passwordPolicy.validate(rawPassword);
        UserId id = UserId.of(idGenerator.newId());
        String passwordHash = passwordEncoder.encode(rawPassword);
        User user = switch (role) {
            case LOGISTICS_OPERATOR ->
                new LogisticsOperator(id, fullName, email, phone, passwordHash, List.of(), clock.now());
            case SUPERVISOR ->
                new Supervisor(id, fullName, email, phone, passwordHash, List.of(), clock.now());
            default ->
                new Administrator(id, fullName, email, phone, passwordHash, List.of(), clock.now());
        };
        return userRepository.save(user);
    }
}
