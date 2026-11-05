package application.adapters.out.persistence.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.adapters.out.persistence.mysql.adapters.CartPersistenceAdapter;
import application.adapters.out.persistence.mysql.adapters.InventoryPersistenceAdapter;
import application.adapters.out.persistence.mysql.adapters.OrderPersistenceAdapter;
import application.adapters.out.persistence.mysql.mappers.CartEntityMapper;
import application.adapters.out.persistence.mysql.mappers.InventoryEntityMapper;
import application.adapters.out.persistence.mysql.mappers.OrderEntityMapper;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductType;
import application.domain.models.Cart;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.valueobjects.CartId;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
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
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * A copy of an aggregate read before another update cannot overwrite that update.
 */
@DataJpaTest(properties = H2WithoutChecksDialect.PROPERTY)
@Import({InventoryPersistenceAdapter.class, InventoryEntityMapper.class,
    OrderPersistenceAdapter.class, OrderEntityMapper.class,
    CartPersistenceAdapter.class, CartEntityMapper.class})
class OptimisticLockingTest {

    private static final LocalDateTime NOW = TestFixture.NOW;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private InventoryPersistenceAdapter inventories;

    @Autowired
    private OrderPersistenceAdapter orders;

    @Autowired
    private CartPersistenceAdapter carts;

    @Test
    void aNewInventoryStartsWithAVersion() {
        Inventory stored = storedInventory();

        assertNotNull(stored.getVersion());
    }

    @Test
    void aStaleInventoryCannotOverwriteANewerOne() {
        Inventory stored = storedInventory();
        Inventory first = readInventory(stored);
        Inventory stale = readInventory(stored);

        first.increase(Quantity.of(5));
        inventories.save(first);
        flushAndClear();

        stale.increase(Quantity.of(1));
        assertThrows(OptimisticLockingFailureException.class, () -> inventories.save(stale));
    }

    @Test
    void theVersionGrowsWithEachUpdate() {
        Inventory stored = storedInventory();
        Inventory copy = readInventory(stored);
        long before = copy.getVersion();

        copy.increase(Quantity.of(1));
        inventories.save(copy);
        flushAndClear();

        assertEquals(before + 1, readInventory(stored).getVersion());
    }

    @Test
    void aStaleOrderCannotOverwriteANewerOne() {
        OrderId id = OrderId.random();
        UserId buyer = UserId.random();
        OrderItem item = new OrderItem(ProductId.random(), ProductType.PHYSICAL, Quantity.of(1),
            Money.of("1000.00", "COP"), WarehouseId.random());
        orders.save(new Order(id, buyer, List.of(item), OrderStatus.PAID, NOW, NOW, NOW));
        flushAndClear();
        Order first = orders.findById(id).orElseThrow();
        Order stale = orders.findById(id).orElseThrow();
        flushAndClear();

        first.cancel();
        orders.save(first);
        flushAndClear();

        assertThrows(OptimisticLockingFailureException.class, () -> orders.save(stale));
    }

    @Test
    void aStaleCartCannotOverwriteANewerOne() {
        UserId buyer = UserId.random();
        CartId id = CartId.random();
        carts.save(new Cart(id, buyer, List.of(), NOW));
        flushAndClear();
        Cart first = carts.findByBuyerId(buyer).orElseThrow();
        Cart stale = carts.findByBuyerId(buyer).orElseThrow();
        flushAndClear();

        carts.save(new Cart(id, buyer, List.of(), NOW.plusHours(1), first.getVersion()));
        flushAndClear();

        assertThrows(OptimisticLockingFailureException.class, () -> carts.save(
            new Cart(id, buyer, List.of(), NOW.plusHours(2), stale.getVersion())));
    }

    private Inventory storedInventory() {
        Inventory stored = inventories.save(Inventory.create(ProductId.random(), WarehouseId.random(),
            Quantity.of(10), Quantity.of(2), WarehouseLocation.of("A", "1", "1")));
        flushAndClear();
        return readInventory(stored);
    }

    private Inventory readInventory(Inventory inventory) {
        Inventory read = inventories.findById(inventory.getId()).orElseThrow();
        entityManager.clear();
        return read;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
