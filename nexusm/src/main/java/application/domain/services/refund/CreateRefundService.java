package application.domain.services.refund;

import application.domain.models.Order;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.RefundRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.RefundId;

import java.util.Objects;

/**
 * Creates the pending refund of a received return or of a paid order that was cancelled.
 */
@DomainService
public class CreateRefundService {

    private final RefundRepository refundRepository;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public CreateRefundService(RefundRepository refundRepository, ClockPort clock,
                               IdGeneratorPort idGenerator) {
        this.refundRepository = Objects.requireNonNull(refundRepository);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public Refund forReturn(ReturnRequest returnRequest) {
        return refundRepository.save(
            Refund.forReturn(returnRequest, RefundId.of(idGenerator.newId()), clock.now()));
    }

    public Refund forCancelledOrder(Order order) {
        return refundRepository.save(
            Refund.forCancelledOrder(order, RefundId.of(idGenerator.newId()), clock.now()));
    }
}
