package application.domain.services.user;

import application.domain.models.User;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.PhoneNumber;

import java.util.Objects;

/**
 * A user changes their own name or phone; a {@code null} value keeps the current one.
 */
@DomainService
public class UpdateProfileService {

    private final UserRepository userRepository;

    public UpdateProfileService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    public User execute(User user, String fullName, PhoneNumber phone) {
        if (fullName != null) {
            user.changeName(fullName);
        }
        if (phone != null) {
            user.changePhone(phone);
        }
        return userRepository.save(user);
    }
}
