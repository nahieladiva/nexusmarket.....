package application.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;

import application.domain.enums.OrderStatus;
import application.domain.enums.ProductType;
import application.domain.enums.StockMovementType;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.StockMovement;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.AdministratorPort;
import application.domain.ports.in.BuyerPort;
import application.domain.ports.in.PublicAccessPort;
import application.domain.ports.in.SellerPort;
import application.domain.ports.out.UserRepository;
import application.domain.services.order.NotifyOrderStatusChangeService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseLocation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * Transactions and optimistic locking on the real MySQL and MongoDB.
 *
 * <p>{@code mvnw test} does not run it. It needs {@code docker compose up -d mysql mongo}, and then:
 * {@code mvnw test -Dtest=*IT}.</p>
 */
@SpringBootTest
class ConsistencyIT {

    private static final String PASSWORD = "Password2026";
    private static final int INITIAL_UNITS = 50;
    private static final int THREADS = 10;
    private static final Address ADDRESS =
        Address.of("10th Street", "Springfield", "Oregon", "97477", "United States");
    private static final WarehouseLocation LOCATION = WarehouseLocation.of("A", "1", "1");
    private static final PhoneNumber PHONE = PhoneNumber.of("+573001234567");

    @Autowired
    private PublicAccessPort publicAccess;

    @Autowired
    private AdministratorPort administrator;

    @Autowired
    private SellerPort seller;

    @Autowired
    private BuyerPort buyer;

    @Autowired
    private UserRepository users;

    @MockitoSpyBean
    private NotifyOrderStatusChangeService notifier;

    private UserId sellerId;
    private UserId buyerId;
    private Product product;
    private Warehouse warehouse;

    @BeforeEach
    void prepareCatalog() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        UserId adminId = users.findByEmail(Email.of("admin@nexusmarket.com")).orElseThrow().getId();
        User newSeller = administrator.registerSeller(adminId, "Seller",
            Email.of("seller." + unique + "@nexus.test"), PHONE, PASSWORD, "Store");
        sellerId = newSeller.getId();
        administrator.approveSeller(adminId, sellerId);
        warehouse = seller.createWarehouse(sellerId, "Warehouse " + unique, ADDRESS, LOCATION);
        product = seller.registerProduct(sellerId,
            ProductCode.of("TEC-" + (System.nanoTime() % 100_000_000L + 100_000_000L) % 100_000_000L),
            ProductType.PHYSICAL, "Headphones", "Bluetooth", Money.of("1000.00", "COP"));
        seller.createInventory(sellerId, product.getId(), warehouse.getId(), Quantity.of(INITIAL_UNITS),
            Quantity.of(1), LOCATION);
        seller.publishProduct(sellerId, product.getId());
        buyerId = publicAccess.registerBuyer("Buyer", Email.of("buyer." + unique + "@nexus.test"), PHONE,
            PASSWORD, ADDRESS).getId();
    }

    private int onHand() {
        return seller.findInventory(sellerId, product.getId(), warehouse.getId()).getOnHand().getValue();
    }

    private List<StockMovementType> movements() {
        return seller.findMovements(sellerId, product.getId(), warehouse.getId()).stream()
            .map(StockMovement::type).toList();
    }

    @Test
    void aPaymentThatFailsHalfwayLeavesNothingBehind() {
        buyer.addCartItem(buyerId, product.getId(), Quantity.of(2));
        Order order = buyer.placeOrder(buyerId);
        doThrow(new IllegalStateException("The payment failed at the last step"))
            .when(notifier).execute(any(), any());

        assertThrows(IllegalStateException.class, () -> buyer.payOrder(buyerId, order.getId()));

        doCallRealMethod().when(notifier).execute(any(), any());
        assertEquals(OrderStatus.PENDING_PAYMENT, buyer.findOrder(buyerId, order.getId()).getStatus());
        assertEquals(INITIAL_UNITS, onHand());
        assertThrows(ResourceNotFoundException.class, () -> buyer.findInvoice(buyerId, order.getId()));
        assertEquals(List.of(StockMovementType.INITIAL), movements());
    }

    @Test
    void aSuccessfulPaymentWritesItsHistoryAfterTheCommit() {
        buyer.addCartItem(buyerId, product.getId(), Quantity.of(2));
        Order order = buyer.placeOrder(buyerId);

        buyer.payOrder(buyerId, order.getId());

        assertEquals(INITIAL_UNITS - 2, onHand());
        assertTrue(movements().contains(StockMovementType.RESERVATION));
    }

    @Test
    void simultaneousAdjustmentsDoNotLoseUnits() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            results.add(pool.submit(() -> {
                start.await();
                try {
                    seller.adjustStock(sellerId, product.getId(), warehouse.getId(), 1, "Count");
                    return true;
                } catch (ConcurrencyFailureException ex) {
                    return false;
                }
            }));
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(1, TimeUnit.MINUTES));

        int applied = 0;
        for (Future<Boolean> result : results) {
            applied += result.get() ? 1 : 0;
        }
        assertTrue(applied >= 1);
        assertEquals(INITIAL_UNITS + applied, onHand());
    }
}
