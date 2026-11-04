package application.services;

import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.in.UserManagementPort;
import application.domain.ports.out.NotificationPort;
import application.domain.ports.out.UserRepository;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de perfil y administración de usuarios.
 */
@Service
@Transactional
public class UserApplicationService implements UserManagementPort {

    /** Máximo de direcciones adicionales por usuario. */
    public static final int MAX_ADDRESSES = 5;

    private final UserRepository userRepository;
    private final NotificationPort notificationPort;

    public UserApplicationService(UserRepository userRepository, NotificationPort notificationPort) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository es obligatorio");
        this.notificationPort = Objects.requireNonNull(notificationPort,
            "notificationPort es obligatorio");
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserById(UserId id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", id.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(Email email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User updateProfile(UserId userId, String fullName, PhoneNumber phone) {
        User user = findUserById(userId);
        if (fullName != null) {
            user.changeName(fullName);
        }
        if (phone != null) {
            user.changePhone(phone);
        }
        return userRepository.save(user);
    }

    @Override
    public User addAddress(UserId userId, Address address) {
        User user = findUserById(userId);
        if (user.hasAddress(address)) {
            throw new DuplicateResourceException("La dirección ya está registrada");
        }
        if (user.getAddresses().size() >= MAX_ADDRESSES) {
            throw new BusinessRuleException("Máximo " + MAX_ADDRESSES + " direcciones por usuario");
        }
        user.addAddress(address);
        return userRepository.save(user);
    }

    @Override
    public User removeAddress(UserId userId, Address address) {
        User user = findUserById(userId);
        user.removeAddress(address);
        return userRepository.save(user);
    }

    @Override
    public User approveSeller(UserId sellerId) {
        User user = findUserById(sellerId);
        if (!(user instanceof Seller seller)) {
            throw new BusinessRuleException("El usuario " + sellerId + " no es un vendedor");
        }
        if (seller.isApproved()) {
            throw new BusinessRuleException("El vendedor ya está aprobado");
        }
        seller.approve();
        User saved = userRepository.save(seller);
        notificationPort.notify(sellerId, "Cuenta aprobada",
            "Tu cuenta de vendedor fue aprobada. Ya puedes publicar productos.");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}
