package application.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.ProductType;
import application.domain.events.LowStockEvent;
import application.domain.events.ProductAddedEvent;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.InvalidTokenException;
import application.domain.models.AuthSession;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.TokenClaims;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseLocation;
import application.support.TestFixture;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Rules that do not belong to a single use case: passwords, token lifetime, stock alerts and
 * inactive warehouses.
 */
class MarketplaceRulesTest {

    private static final Email EMAIL = Email.of("ana@mail.com");
    private static final PhoneNumber PHONE = PhoneNumber.of("+573001112233");

    private TestFixture f;
    private Seller seller;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        seller = f.approvedSeller("seller@mail.com");
    }

    private User registerBuyer() {
        return f.registerBuyer.execute("Ana", EMAIL, PHONE, "Clave2026", TestFixture.address());
    }

    @Test
    void passwordIsStoredHashed() {
        assertEquals("hashed:Clave2026", registerBuyer().getPasswordHash());
    }

    @Test
    void loginIssuesAOneHourTokenAndStoresIt() {
        registerBuyer();
        AuthSession session = f.login.execute(EMAIL, "Clave2026");
        assertEquals(TestFixture.NOW.plusHours(1), session.expiresAt());
        TokenClaims claims = f.validateToken.execute(session.token());
        assertTrue(f.tokenStore.findById(claims.tokenId()).isPresent());
    }

    @Test
    void tokenExpiresAfterOneHour() {
        registerBuyer();
        String token = f.login.execute(EMAIL, "Clave2026").token();
        f.clock.advance(Duration.ofHours(1));
        assertThrows(InvalidTokenException.class, () -> f.validateToken.execute(token));
    }

    @Test
    void stockCannotBeRegisteredInAnInactiveWarehouse() {
        Warehouse warehouse = f.warehouse("Main");
        warehouse.deactivate();
        f.warehouses.save(warehouse);
        Product product = f.product(seller, "TEC-0001", "1000");
        assertThrows(BusinessRuleException.class, () -> f.createInventory.execute(
            f.logisticsOperator("operator@mail.com"), product.getId(), warehouse.getId(), Quantity.of(1),
            Quantity.of(0), WarehouseLocation.of("A", "1", "2")));
    }

    @Test
    void inactiveWarehouseIsNotUsedForOrders() {
        Warehouse warehouse = f.warehouse("Main");
        Product product = f.product(seller, "TEC-0001", "1000");
        f.stock(seller, product, warehouse, 10, 0);
        warehouse.deactivate();
        f.warehouses.save(warehouse);
        Buyer buyer = f.buyer("buyer@mail.com");
        f.addItemToCart.execute(buyer, product.getId(), Quantity.of(1));
        assertThrows(InsufficientStockException.class, () -> f.placeOrder.execute(buyer));
    }

    @Test
    void lowStockAfterAPaymentNotifiesTheSeller() {
        Product product = f.product(seller, "TEC-0001", "1000");
        f.stock(seller, product, f.warehouse("Main"), 3, 2);
        Buyer buyer = f.buyer("buyer@mail.com");
        f.addItemToCart.execute(buyer, product.getId(), Quantity.of(2));
        Order order = f.placeOrder.execute(buyer);
        f.payOrder.execute(buyer, order.getId());
        assertEquals(1, f.events.count(LowStockEvent.class));
        assertTrue(f.notifier.sent.stream().anyMatch(sent ->
            sent.recipient().equals(seller.getId()) && sent.subject().equals("Low stock")));
    }

    @Test
    void registeringAProductPublishesItsEvent() {
        f.registerProduct.execute(seller, ProductCode.of("TEC-0001"), ProductType.PHYSICAL, "Laptop", null,
            Money.of("1000", "COP"));
        assertEquals(1, f.events.count(ProductAddedEvent.class));
    }

    @Test
    void priceMustBeGreaterThanZero() {
        assertThrows(IllegalArgumentException.class, () -> f.registerProduct.execute(seller,
            ProductCode.of("TEC-0001"), ProductType.PHYSICAL, "Laptop", null, Money.of("0", "COP")));
    }

    @Test
    void buyerCannotRegisterOrPublishProducts() {
        Buyer buyer = f.buyer("buyer@mail.com");
        Product product = f.product(seller, "TEC-0001", "1000");
        assertThrows(ForbiddenActionException.class, () -> f.registerProduct.execute(buyer,
            ProductCode.of("TEC-0002"), ProductType.PHYSICAL, "Laptop", null, Money.of("1000", "COP")));
        assertThrows(ForbiddenActionException.class, () -> f.publishProduct.execute(buyer, product.getId()));
    }
}
