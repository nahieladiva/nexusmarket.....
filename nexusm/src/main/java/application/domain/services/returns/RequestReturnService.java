package application.domain.services.returns;

import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceStateConflictException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Order;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ReturnRequestRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ReturnId;

import java.util.List;
import java.util.Objects;

/**
 * A buyer requests the return of a product of their delivered order. An order can have only one
 * open return, and the units returned so far count against what was bought.
 */
@DomainService
public class RequestReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public RequestReturnService(ReturnRequestRepository returnRequestRepository,
                                OrderRepository orderRepository, ClockPort clock,
                                IdGeneratorPort idGenerator) {
        this.returnRequestRepository = Objects.requireNonNull(returnRequestRepository);
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public ReturnRequest execute(User buyer, OrderId orderId, ProductId productId, Quantity quantity,
                                 String reason) {
        buyer.requireRole(UserRole.BUYER);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId.toString()));
        order.requireBelongsTo(buyer.getId());
        List<ReturnRequest> previous = returnRequestRepository.findByOrderId(orderId);
        if (previous.stream().anyMatch(existing -> existing.getStatus().isOpen())) {
            throw new ResourceStateConflictException("Order " + orderId + " already has an open return");
        }
        Quantity alreadyReturned = Quantity.of(previous.stream()
            .filter(existing -> existing.getProductId().equals(productId))
            .filter(existing -> existing.getStatus().countsAsReturned())
            .mapToInt(existing -> existing.getQuantity().getValue())
            .sum());
        return returnRequestRepository.save(ReturnRequest.request(order, ReturnId.of(idGenerator.newId()),
            productId, quantity, alreadyReturned, reason, clock.now()));
    }
}
