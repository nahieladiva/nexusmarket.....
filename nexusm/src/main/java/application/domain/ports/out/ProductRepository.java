package application.domain.ports.out;

import application.domain.models.Product;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Output port 2/15 — product catalog (MySQL).
 */
public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(ProductId id);

    Optional<Product> findByCode(ProductCode code);

    List<Product> findByNameContaining(String keyword);

    List<Product> findBySellerId(UserId sellerId);

    List<Product> findAll();
}