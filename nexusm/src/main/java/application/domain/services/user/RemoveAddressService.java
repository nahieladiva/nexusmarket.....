package application.domain.services.user;

import application.domain.models.User;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.Address;

import java.util.Objects;

/**
 * A user removes an address from their profile.
 */
@DomainService
public class RemoveAddressService {

    private final UserRepository userRepository;

    public RemoveAddressService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    public User execute(User user, Address address) {
        user.removeAddress(address);
        return userRepository.save(user);
    }
}
