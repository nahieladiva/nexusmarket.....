package application.domain.ports.out;

import application.domain.models.ReturnRequest;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ReturnId;

import java.util.List;
import java.util.Optional;

/**
 * Output port — returns (MySQL).
 */
public interface ReturnRequestRepository {

    ReturnRequest save(ReturnRequest returnRequest);

    Optional<ReturnRequest> findById(ReturnId id);

    /** Returns of an order, used to calculate returned units and detect open returns. */
    List<ReturnRequest> findByOrderId(OrderId orderId);

    List<ReturnRequest> findAll();
}
