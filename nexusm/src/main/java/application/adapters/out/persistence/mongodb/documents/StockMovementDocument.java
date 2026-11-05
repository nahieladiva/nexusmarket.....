package application.adapters.out.persistence.mongodb.documents;

import application.domain.enums.StockMovementType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document of a stock movement.
 */
@Document(collection = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
public class StockMovementDocument {

    @Id
    private String id;
    private String productId;
    private String warehouseId;
    private StockMovementType type;
    private int delta;
    private int resultingOnHand;
    private String reason;
    private String performedBy;
    private String reference;
    private LocalDateTime occurredAt;
}
