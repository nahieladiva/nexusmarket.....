package application.domain.models;

import application.domain.enums.OrderStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.exceptions.BusinessRuleException;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Shipment of a paid order from a warehouse, handled by a logistics operator.
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>It is created only for a {@code PAID} order, from one of the warehouses of the order.</li>
 *   <li>Each warehouse of an order has its own shipment.</li>
 *   <li>Lifecycle: PREPARING -> PACKED -> IN_TRANSIT -> DELIVERED (see {@link ShipmentStatus}).</li>
 *   <li>A tracking number is required to dispatch it.</li>
 * </ul>
 */
public final class Shipment {

    private final ShipmentId id;
    private final OrderId orderId;
    private final WarehouseId warehouseId;
    private final Address shippingAddress;
    private UserId logisticsOperatorId;
    private String trackingNumber;
    private ShipmentStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime deliveredAt;

    public Shipment(ShipmentId id, OrderId orderId, WarehouseId warehouseId,
                    Address shippingAddress, UserId logisticsOperatorId, String trackingNumber,
                    ShipmentStatus status, LocalDateTime createdAt, LocalDateTime dispatchedAt,
                    LocalDateTime deliveredAt) {
        this.id = Objects.requireNonNull(id, "Shipment id is required");
        this.orderId = Objects.requireNonNull(orderId, "Order is required");
        this.warehouseId = Objects.requireNonNull(warehouseId, "Origin warehouse is required");
        this.shippingAddress = Objects.requireNonNull(shippingAddress, "Shipping address is required");
        this.logisticsOperatorId = logisticsOperatorId;
        this.trackingNumber = trackingNumber;
        this.status = Objects.requireNonNull(status, "Shipment status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "Creation date is required");
        this.dispatchedAt = dispatchedAt;
        this.deliveredAt = deliveredAt;
    }

    /**
     * Creates the shipment of a paid order from one of its warehouses.
     *
     * @throws BusinessRuleException when the order is not paid or does not ship from the warehouse
     */
    public static Shipment prepareFor(Order order, ShipmentId id, WarehouseId warehouseId,
                                      Address shippingAddress, LocalDateTime now) {
        Objects.requireNonNull(order, "Order is required");
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessRuleException(
                "Only a paid order can be shipped (current status: " + order.getStatus() + ")");
        }
        if (!order.shippingWarehouses().contains(warehouseId)) {
            throw new BusinessRuleException(
                "Order " + order.getId() + " does not ship from warehouse " + warehouseId);
        }
        return new Shipment(id, order.getId(), warehouseId, shippingAddress, null, null,
            ShipmentStatus.PREPARING, now, null, null);
    }

    /**
     * Marks the shipment as packed and ready to leave the warehouse.
     */
    public void pack() {
        changeStatus(ShipmentStatus.PACKED);
    }

    /**
     * Hands the shipment to the carrier.
     *
     * @throws IllegalArgumentException when the tracking number is blank
     */
    public void dispatch(UserId operatorId, String newTrackingNumber, LocalDateTime now) {
        Objects.requireNonNull(operatorId, "Logistics operator is required");
        if (newTrackingNumber == null || newTrackingNumber.isBlank()) {
            throw new IllegalArgumentException("Tracking number is required to dispatch a shipment");
        }
        changeStatus(ShipmentStatus.IN_TRANSIT);
        this.logisticsOperatorId = operatorId;
        this.trackingNumber = newTrackingNumber.trim();
        this.dispatchedAt = now;
    }

    /**
     * Confirms that the buyer received the shipment.
     */
    public void confirmDelivery(LocalDateTime now) {
        changeStatus(ShipmentStatus.DELIVERED);
        this.deliveredAt = now;
    }

    private void changeStatus(ShipmentStatus target) {
        status.requireCanTransitionTo(target);
        this.status = target;
    }

    public ShipmentId getId() {
        return id;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public WarehouseId getWarehouseId() {
        return warehouseId;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public Optional<UserId> getLogisticsOperatorId() {
        return Optional.ofNullable(logisticsOperatorId);
    }

    public Optional<String> getTrackingNumber() {
        return Optional.ofNullable(trackingNumber);
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Optional<LocalDateTime> getDispatchedAt() {
        return Optional.ofNullable(dispatchedAt);
    }

    public Optional<LocalDateTime> getDeliveredAt() {
        return Optional.ofNullable(deliveredAt);
    }
}
