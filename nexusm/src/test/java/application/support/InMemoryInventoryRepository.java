package application.support;

import application.domain.models.Inventory;
import application.domain.ports.out.InventoryRepository;
import application.domain.valueobjects.InventoryId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryInventoryRepository implements InventoryRepository {
    private final Map<InventoryId, Inventory> data = new LinkedHashMap<>();

    @Override public Inventory save(Inventory inv) { data.put(inv.getId(), inv); return inv; }
    @Override public Optional<Inventory> findById(InventoryId id) { return Optional.ofNullable(data.get(id)); }
    @Override public Optional<Inventory> findByProductIdAndWarehouseId(ProductId p, WarehouseId w) {
        return data.values().stream()
            .filter(i -> i.getProductId().equals(p) && i.getWarehouseId().equals(w)).findFirst();
    }
    @Override public List<Inventory> findByWarehouseId(WarehouseId w) {
        return data.values().stream().filter(i -> i.getWarehouseId().equals(w)).toList();
    }
    @Override public List<Inventory> findByProductId(ProductId p) {
        return data.values().stream().filter(i -> i.getProductId().equals(p)).toList();
    }
    @Override public List<Inventory> findAll() { return new ArrayList<>(data.values()); }
}
