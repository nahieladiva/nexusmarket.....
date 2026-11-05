package application.adapters.out.persistence.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import application.adapters.out.persistence.mysql.adapters.OrderPersistenceAdapter;
import application.adapters.out.persistence.mysql.adapters.ProductPersistenceAdapter;
import application.adapters.out.persistence.mysql.adapters.UserPersistenceAdapter;
import application.adapters.out.persistence.mysql.adapters.WarehousePersistenceAdapter;
import application.adapters.out.persistence.mysql.mappers.OrderEntityMapper;
import application.adapters.out.persistence.mysql.mappers.ProductEntityMapper;
import application.adapters.out.persistence.mysql.mappers.UserEntityMapper;
import application.adapters.out.persistence.mysql.mappers.WarehouseEntityMapper;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.SellerStatus;
import application.domain.enums.UserRole;
import application.domain.enums.WarehouseStatus;
import application.domain.enums.WarehouseType;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;
import application.support.H2WithoutChecksDialect;
import application.support.TestFixture;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Enum columns of the first tables: they store the name of the constant, as they did when the
 * entities used {@code String}. The raw values are read with SQL.
 */
@DataJpaTest(properties = H2WithoutChecksDialect.PROPERTY)
@Import({UserPersistenceAdapter.class, UserEntityMapper.class,
    ProductPersistenceAdapter.class, ProductEntityMapper.class,
    WarehousePersistenceAdapter.class, WarehouseEntityMapper.class,
    OrderPersistenceAdapter.class, OrderEntityMapper.class})
class EnumColumnsPersistenceTest {

    private static final LocalDateTime NOW = TestFixture.NOW;
    private static final Money PRICE = Money.of("1000.00", "COP");

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserPersistenceAdapter users;

    @Autowired
    private ProductPersistenceAdapter products;

    @Autowired
    private WarehousePersistenceAdapter warehouses;

    @Autowired
    private OrderPersistenceAdapter orders;

    @Test
    void usersStoreTheNamesOfRoleAndSellerStatus() {
        UserId id = UserId.random();
        users.save(new Seller(id, "Seller", Email.of("seller@mail.com"),
            PhoneNumber.of("+573001234567"), "hashed:Password123", "Store", List.of(),
            SellerStatus.SUSPENDED, NOW));
        flushAndClear();

        assertEquals("SELLER", column("role", "users", id));
        assertEquals("SUSPENDED", column("seller_status", "users", id));
        Seller stored = (Seller) users.findById(id).orElseThrow();
        assertEquals(UserRole.SELLER, stored.getRole());
        assertEquals(SellerStatus.SUSPENDED, stored.getStatus());
    }

    @Test
    void productsStoreTheNamesOfTypeAndStatus() {
        ProductId id = ProductId.random();
        products.save(new Product(id, ProductCode.of("ELE-000123"), ProductType.DIGITAL, "Name",
            "Description", PRICE, UserId.random(), ProductStatus.BLOCKED, NOW));
        flushAndClear();

        assertEquals("DIGITAL", column("type", "products", id));
        assertEquals("BLOCKED", column("status", "products", id));
        Product stored = products.findById(id).orElseThrow();
        assertEquals(ProductType.DIGITAL, stored.getType());
        assertEquals(ProductStatus.BLOCKED, stored.getStatus());
    }

    @Test
    void warehousesStoreTheNamesOfTypeAndStatus() {
        WarehouseId id = WarehouseId.random();
        warehouses.save(new Warehouse(id, WarehouseType.MARKETPLACE, null, "North",
            TestFixture.address(), WarehouseLocation.of("A", "1", "1"), WarehouseStatus.INACTIVE));
        flushAndClear();

        assertEquals("MARKETPLACE", column("type", "warehouses", id));
        assertEquals("INACTIVE", column("status", "warehouses", id));
        Warehouse stored = warehouses.findById(id).orElseThrow();
        assertEquals(WarehouseType.MARKETPLACE, stored.getType());
        assertEquals(WarehouseStatus.INACTIVE, stored.getStatus());
    }

    @Test
    void ordersStoreTheNamesOfStatusAndProductType() {
        OrderId id = OrderId.random();
        OrderItem item = new OrderItem(ProductId.random(), ProductType.PHYSICAL, Quantity.of(1), PRICE,
            WarehouseId.random());
        orders.save(new Order(id, UserId.random(), List.of(item), OrderStatus.PAID, NOW, NOW, NOW));
        flushAndClear();

        assertEquals("PAID", column("status", "orders", id));
        assertEquals("PHYSICAL", entityManager
            .createNativeQuery("select product_type from order_items where order_id = :id")
            .setParameter("id", id.toString())
            .getSingleResult());
        Order stored = orders.findById(id).orElseThrow();
        assertEquals(OrderStatus.PAID, stored.getStatus());
        assertEquals(ProductType.PHYSICAL, stored.getItems().get(0).getProductType());
    }

    private String column(String column, String table, Object id) {
        return (String) entityManager
            .createNativeQuery("select " + column + " from " + table + " where id = :id")
            .setParameter("id", id.toString())
            .getSingleResult();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
