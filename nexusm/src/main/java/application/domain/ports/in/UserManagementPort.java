package application.domain.ports.in;

import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada 2/7 — perfil de usuario y administración de usuarios.
 */
public interface UserManagementPort {

    User findUserById(UserId id);

    Optional<User> findByEmail(Email email);

    /** Actualiza nombre y teléfono. El email y el rol no cambian después del registro. */
    User updateProfile(UserId userId, String fullName, PhoneNumber phone);

    User addAddress(UserId userId, Address address);

    User removeAddress(UserId userId, Address address);

    /** Solo administradores: habilita a un vendedor para publicar productos. */
    User approveSeller(UserId sellerId);

    List<User> findAllUsers();
}
