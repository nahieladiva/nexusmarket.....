package application.domain.services.user;

import application.domain.enums.UserRole;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.NotificationPort;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.UserId;

import java.util.Objects;

/**
 * An administrator suspends an approved seller, and the seller is notified.
 */
@DomainService
public class SuspendSellerService {

    private final UserRepository userRepository;
    private final NotificationPort notificationPort;

    public SuspendSellerService(UserRepository userRepository, NotificationPort notificationPort) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.notificationPort = Objects.requireNonNull(notificationPort);
    }

    public Seller execute(User administrator, UserId sellerId) {
        administrator.requireRole(UserRole.ADMINISTRATOR);
        User user = userRepository.findById(sellerId)
            .orElseThrow(() -> new ResourceNotFoundException("User", sellerId.toString()));
        if (!(user instanceof Seller seller)) {
            throw new BusinessRuleException("User " + sellerId + " is not a seller");
        }
        seller.suspend();
        userRepository.save(seller);
        notificationPort.notify(sellerId, "Account suspended",
            "Your seller account was suspended. You cannot publish products.");
        return seller;
    }
}
