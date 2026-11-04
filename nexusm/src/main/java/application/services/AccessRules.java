package application.services;

import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.out.UserRepository;
import application.domain.valueobjects.UserId;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Reglas de autorización de negocio compartidas por los casos de uso
 * (quién puede actuar sobre qué recurso).
 */
@Component
public class AccessRules {

    private final UserRepository userRepository;

    public AccessRules(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository es obligatorio");
    }

    public User requireUser(UserId userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId.toString()));
    }

    public User requireAdmin(UserId userId) {
        User user = requireUser(userId);
        if (!user.isAdmin()) {
            throw new ForbiddenActionException("Solo un administrador puede hacer esta operación");
        }
        return user;
    }

    /** El vendedor dueño del producto, o un administrador. */
    public User requireOwnerOrAdmin(UserId actorId, Product product) {
        User actor = requireUser(actorId);
        if (!actor.isAdmin() && !product.isOwnedBy(actorId)) {
            throw new ForbiddenActionException(
                "Solo el vendedor dueño del producto o un administrador puede hacer esta operación");
        }
        return actor;
    }

    /** Estrictamente el vendedor dueño del producto. */
    public void requireOwner(UserId actorId, Product product) {
        if (!product.isOwnedBy(actorId)) {
            throw new ForbiddenActionException("Solo el vendedor dueño del producto puede modificarlo");
        }
    }
}
