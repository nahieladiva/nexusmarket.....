package application.domain.ports.out;

import application.domain.models.User;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Output port — users (MySQL).
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(Email email);

    List<User> findAll();
}