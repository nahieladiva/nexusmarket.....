package application.domain.services.user;

import application.domain.exceptions.DuplicateResourceException;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.Email;

import java.util.Objects;

/**
 * Validates that no other user has an email. {@link Email} is already normalized to lowercase.
 */
@DomainService
public class ValidateUserUniquenessService {

    private final UserRepository userRepository;

    public ValidateUserUniquenessService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    public void execute(Email email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("A user with email " + email + " already exists");
        }
    }
}
