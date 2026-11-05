package application.domain.services.returns;

import application.domain.enums.UserRole;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.ReturnRequestRepository;
import application.domain.services.DomainService;
import application.domain.services.order.ConsultOrderService;
import application.domain.valueobjects.OrderId;

import java.util.List;
import java.util.Objects;

/**
 * Return queries: whoever can see an order can see its returns, and internal staff see all.
 */
@DomainService
public class ConsultReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final ConsultOrderService consultOrderService;

    public ConsultReturnService(ReturnRequestRepository returnRequestRepository,
                                ConsultOrderService consultOrderService) {
        this.returnRequestRepository = Objects.requireNonNull(returnRequestRepository);
        this.consultOrderService = Objects.requireNonNull(consultOrderService);
    }

    public List<ReturnRequest> findByOrder(User actor, OrderId orderId) {
        consultOrderService.execute(actor, orderId);
        return returnRequestRepository.findByOrderId(orderId);
    }

    public List<ReturnRequest> findAll(User actor) {
        actor.requireAnyRole(UserRole.ADMINISTRATOR, UserRole.SUPERVISOR, UserRole.LOGISTICS_OPERATOR);
        return returnRequestRepository.findAll();
    }
}
