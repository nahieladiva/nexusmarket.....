package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity of an inventory (stock of a product in a warehouse).
 */
@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class InventoryJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(name = "warehouse_id", nullable = false, length = 36)
    private String warehouseId;

    @Column(name = "on_hand", nullable = false)
    private int onHand;

    @Column(name = "reorder_threshold", nullable = false)
    private int reorderThreshold;

    @Column(name = "aisle", nullable = false)
    private String aisle;

    @Column(name = "shelf", nullable = false)
    private String shelf;

    @Column(name = "bin", nullable = false)
    private String bin;
}