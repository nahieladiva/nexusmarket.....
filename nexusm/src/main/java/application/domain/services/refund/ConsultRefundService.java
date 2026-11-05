package application.domain.services.refund;

import application.domain.enums.UserRole;
import application.domain.models.Refund;
import application.domain.models.User;
import application.domain.ports.out.RefundRepository;
import application.domain.services.DomainService;
import application.domain.services.order.ConsultOrderService;
import application.domain.valueobjects.OrderId;

import java.util.List;
import java.util.Objects;

/**
 * Refund queries: whoever can see an order can see its refunds, and administrators and supervisors
 * see all of them.
 */
@DomainService
public class ConsultRefundService {

    private final RefundRepository refundRepository;
    private final ConsultOrderService consultOrderService;

    public ConsultRefundService(RefundRepository refundRepository,
                                ConsultOrderService consultOrderService) {
        this.refundRepository = Objects.requireNonNull(refundRepository);
        this.consultOrderService = Objects.requireNonNull(consultOrderService);
    }

    public List<Refund> findByOrder(User actor, OrderId orderId) {
        consultOrderService.execute(actor, orderId);
        return refundRepository.findByOrderId(orderId);
    }

    public List<Refund> findAll(User actor) {
        actor.requireAnyRole(UserRole.ADMINISTRATOR, UserRole.SUPERVISOR);
        return refundRepository.findAll();
    }
}
