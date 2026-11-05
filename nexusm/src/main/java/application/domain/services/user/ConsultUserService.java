package application.domain.services.user;

import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.User;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.UserId;

import java.util.List;
import java.util.Objects;

/**
 * User queries, for administrators and supervisors. Each user reads their own profile from the
 * authenticated user.
 */
@DomainService
public class ConsultUserService {

    private final UserRepository userRepository;

    public ConsultUserService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    public User execute(User actor, UserId userId) {
        actor.requireAnyRole(UserRole.ADMINISTRATOR, UserRole.SUPERVISOR);
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }

    public List<User> findAll(User actor) {
        actor.requireAnyRole(UserRole.ADMINISTRATOR, UserRole.SUPERVISOR);
        return userRepository.findAll();
    }
}
