package application.support;

import application.domain.models.StockMovement;
import application.domain.ports.out.StockMovementLogPort;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryStockMovementLog implements StockMovementLogPort {
    public final List<StockMovement> movements = new ArrayList<>();

    @Override public void record(StockMovement movement) { movements.add(movement); }
    @Override public List<StockMovement> findByProductAndWarehouse(ProductId p, WarehouseId w) {
        List<StockMovement> result = new ArrayList<>(movements.stream()
            .filter(m -> m.productId().equals(p) && m.warehouseId().equals(w)).toList());
        Collections.reverse(result);
        return result;
    }
}
