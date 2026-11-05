package application.domain.services.refund;

import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Refund;
import application.domain.models.User;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.NotificationPort;
import application.domain.ports.out.RefundRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.RefundId;

import java.util.Objects;

/**
 * An administrator processes a pending refund, and the buyer is notified.
 */
@DomainService
public class ProcessRefundService {

    private final RefundRepository refundRepository;
    private final NotificationPort notificationPort;
    private final ClockPort clock;

    public ProcessRefundService(RefundRepository refundRepository, NotificationPort notificationPort,
                                ClockPort clock) {
        this.refundRepository = Objects.requireNonNull(refundRepository);
        this.notificationPort = Objects.requireNonNull(notificationPort);
        this.clock = Objects.requireNonNull(clock);
    }

    public Refund execute(User administrator, RefundId refundId) {
        administrator.requireRole(UserRole.ADMINISTRATOR);
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new ResourceNotFoundException("Refund", refundId.toString()));
        refund.process(clock.now());
        Refund saved = refundRepository.save(refund);
        notificationPort.notify(saved.getBuyerId(), "Refund processed",
            "Your refund of " + saved.getAmount() + " was processed.");
        return saved;
    }
}
