package application.support;

import application.domain.models.Refund;
import application.domain.ports.out.RefundRepository;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.RefundId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory refund repository for tests.
 */
public class InMemoryRefundRepository implements RefundRepository {

    private final Map<RefundId, Refund> refunds = new LinkedHashMap<>();

    @Override
    public Refund save(Refund refund) {
        refunds.put(refund.getId(), refund);
        return refund;
    }

    @Override
    public Optional<Refund> findById(RefundId id) {
        return Optional.ofNullable(refunds.get(id));
    }

    @Override
    public List<Refund> findByOrderId(OrderId orderId) {
        return refunds.values().stream()
            .filter(refund -> refund.getOrderId().equals(orderId))
            .toList();
    }

    @Override
    public List<Refund> findAll() {
        return new ArrayList<>(refunds.values());
    }
}
