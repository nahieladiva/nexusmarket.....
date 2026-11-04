package application.support;

import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepository;
import application.domain.valueobjects.WarehouseId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryWarehouseRepository implements WarehouseRepository {
    private final Map<WarehouseId, Warehouse> data = new LinkedHashMap<>();

    @Override public Warehouse save(Warehouse w) { data.put(w.getId(), w); return w; }
    @Override public Optional<Warehouse> findById(WarehouseId id) { return Optional.ofNullable(data.get(id)); }
    @Override public List<Warehouse> findByNameContaining(String keyword) {
        return data.values().stream()
            .filter(w -> w.getName().toLowerCase().contains(keyword.toLowerCase())).toList();
    }
    @Override public List<Warehouse> findAll() { return new ArrayList<>(data.values()); }
}
