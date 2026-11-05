package application.domain.services.user;

import application.domain.exceptions.InvalidCredentialsException;
import application.domain.models.User;
import application.domain.ports.out.PasswordEncoderPort;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;

import java.util.Objects;

/**
 * A user changes their own password: the current one is required, and the new one must follow the
 * password policy.
 */
@DomainService
public class ChangePasswordService {

    private final UserRepository userRepository;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoderPort passwordEncoder;

    public ChangePasswordService(UserRepository userRepository, PasswordPolicy passwordPolicy,
                                 PasswordEncoderPort passwordEncoder) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordPolicy = Objects.requireNonNull(passwordPolicy);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
    }

    public User execute(User user, String currentPassword, String newPassword) {
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        passwordPolicy.validate(newPassword);
        user.changePasswordHash(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
}
