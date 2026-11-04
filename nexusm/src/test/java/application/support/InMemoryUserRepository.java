package application.support;

import application.domain.models.User;
import application.domain.ports.out.UserRepository;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.UserId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {
    private final Map<UserId, User> data = new LinkedHashMap<>();

    @Override public User save(User user) { data.put(user.getId(), user); return user; }
    @Override public Optional<User> findById(UserId id) { return Optional.ofNullable(data.get(id)); }
    @Override public Optional<User> findByEmail(Email email) {
        return data.values().stream().filter(u -> u.getEmail().equals(email)).findFirst();
    }
    @Override public List<User> findAll() { return new ArrayList<>(data.values()); }
}
