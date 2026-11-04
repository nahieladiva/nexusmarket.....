package application.support;

import application.domain.models.Promotion;
import application.domain.ports.out.PromotionRepository;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.PromotionId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryPromotionRepository implements PromotionRepository {
    private final Map<PromotionId, Promotion> data = new LinkedHashMap<>();

    @Override public Promotion save(Promotion p) { data.put(p.getId(), p); return p; }
    @Override public Optional<Promotion> findById(PromotionId id) { return Optional.ofNullable(data.get(id)); }
    @Override public List<Promotion> findByProductId(ProductId productId) {
        return data.values().stream().filter(p -> p.getProductId().equals(productId)).toList();
    }
    @Override public List<Promotion> findAll() { return new ArrayList<>(data.values()); }
}
