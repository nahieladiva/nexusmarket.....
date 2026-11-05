package application.domain.services.authorization;

import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.User;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.UserId;

import java.util.Objects;

/**
 * Loads the authenticated user that the other domain services receive as the actor.
 */
@DomainService
public class ResolveActorService {

    private final UserRepository userRepository;

    public ResolveActorService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    public User execute(UserId userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }
}
