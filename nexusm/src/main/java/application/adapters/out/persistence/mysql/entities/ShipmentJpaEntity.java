package application.adapters.out.persistence.mysql.entities;

import application.domain.enums.ShipmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity of a shipment. An order has a single shipment per warehouse.
 */
@Entity
@Table(name = "shipments",
    uniqueConstraints = @UniqueConstraint(name = "uk_shipments_order_warehouse",
        columnNames = {"order_id", "warehouse_id"}),
    indexes = @Index(name = "idx_shipments_order", columnList = "order_id"))
@Getter
@Setter
@NoArgsConstructor
public class ShipmentJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Column(name = "warehouse_id", nullable = false, length = 36)
    private String warehouseId;

    @Embedded
    private AddressEmbeddable shippingAddress;

    @Column(name = "logistics_operator_id", length = 36)
    private String logisticsOperatorId;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(20)")
    private ShipmentStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}
