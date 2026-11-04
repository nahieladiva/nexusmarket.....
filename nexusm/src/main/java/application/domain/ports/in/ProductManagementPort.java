package application.domain.ports.in;

import application.domain.models.Product;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;
import java.util.List;

/**
 * Puerto de entrada 3/7 — catálogo de productos.
 */
public interface ProductManagementPort {

    /** Solo vendedores aprobados. El código debe ser único y el precio mayor que cero. */
    Product createProduct(UserId sellerId, ProductCode code, String name, String description,
                          Money price);

    /** Solo el vendedor dueño. */
    Product updateProduct(UserId sellerId, ProductId productId, String name, String description);

    /** Solo el vendedor dueño. */
    Product changePrice(UserId sellerId, ProductId productId, Money newPrice);

    /** Solo el vendedor dueño. Un producto inactivo no aparece en búsquedas ni se puede pedir. */
    Product changeAvailability(UserId sellerId, ProductId productId, boolean active);

    Product findProductById(ProductId id);

    /** Búsqueda pública: solo productos activos. */
    List<Product> searchProducts(String keyword);

    /** Todos los productos de un vendedor, activos o no. */
    List<Product> findBySeller(UserId sellerId);

    /** Solo administradores: catálogo completo, incluidos inactivos. */
    List<Product> findAllProducts();
}
