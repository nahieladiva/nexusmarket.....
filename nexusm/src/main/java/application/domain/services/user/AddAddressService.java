package application.domain.services.user;

import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.models.User;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.Address;

import java.util.Objects;

/**
 * A user adds an address to their profile, up to {@value #MAX_ADDRESSES} addresses.
 */
@DomainService
public class AddAddressService {

    public static final int MAX_ADDRESSES = 5;

    private final UserRepository userRepository;

    public AddAddressService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    public User execute(User user, Address address) {
        if (user.hasAddress(address)) {
            throw new DuplicateResourceException("The address is already registered");
        }
        if (user.getAddresses().size() >= MAX_ADDRESSES) {
            throw new BusinessRuleException("A user can have at most " + MAX_ADDRESSES + " addresses");
        }
        user.addAddress(address);
        return userRepository.save(user);
    }
}
