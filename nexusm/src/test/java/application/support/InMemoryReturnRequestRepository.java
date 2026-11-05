package application.support;

import application.domain.models.ReturnRequest;
import application.domain.ports.out.ReturnRequestRepository;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ReturnId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory return repository for tests.
 */
public class InMemoryReturnRequestRepository implements ReturnRequestRepository {

    private final Map<ReturnId, ReturnRequest> returns = new LinkedHashMap<>();

    @Override
    public ReturnRequest save(ReturnRequest returnRequest) {
        returns.put(returnRequest.getId(), returnRequest);
        return returnRequest;
    }

    @Override
    public Optional<ReturnRequest> findById(ReturnId id) {
        return Optional.ofNullable(returns.get(id));
    }

    @Override
    public List<ReturnRequest> findByOrderId(OrderId orderId) {
        return returns.values().stream()
            .filter(returnRequest -> returnRequest.getOrderId().equals(orderId))
            .toList();
    }

    @Override
    public List<ReturnRequest> findAll() {
        return new ArrayList<>(returns.values());
    }
}
