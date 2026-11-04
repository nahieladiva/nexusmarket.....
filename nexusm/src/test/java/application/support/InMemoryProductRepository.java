package application.support;

import application.domain.models.Product;
import application.domain.ports.out.ProductRepository;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryProductRepository implements ProductRepository {
    private final Map<ProductId, Product> data = new LinkedHashMap<>();

    @Override public Product save(Product product) { data.put(product.getId(), product); return product; }
    @Override public Optional<Product> findById(ProductId id) { return Optional.ofNullable(data.get(id)); }
    @Override public Optional<Product> findByCode(ProductCode code) {
        return data.values().stream().filter(p -> p.getCode().equals(code)).findFirst();
    }
    @Override public List<Product> findByNameContaining(String keyword) {
        return data.values().stream()
            .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase())).toList();
    }
    @Override public List<Product> findBySellerId(UserId sellerId) {
        return data.values().stream().filter(p -> p.getSellerId().equals(sellerId)).toList();
    }
    @Override public List<Product> findAll() { return new ArrayList<>(data.values()); }
}
