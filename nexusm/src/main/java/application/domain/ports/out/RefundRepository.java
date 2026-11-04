package application.domain.ports.out;

import application.domain.models.Refund;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.RefundId;

import java.util.List;
import java.util.Optional;

/**
 * Output port — refunds (MySQL). An order can have several refunds, one per received return.
 */
public interface RefundRepository {

    Refund save(Refund refund);

    Optional<Refund> findById(RefundId id);

    List<Refund> findByOrderId(OrderId orderId);

    List<Refund> findAll();
}
