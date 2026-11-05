package application.adapters.useCases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.InvoiceStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.RefundStatus;
import application.domain.enums.ReturnStatus;
import application.domain.enums.SellerStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.enums.UserRole;
import application.domain.enums.WarehouseStatus;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.InvalidTokenException;
import application.domain.models.AuthSession;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.ports.in.AccountPort;
import application.domain.ports.in.AdministratorPort;
import application.domain.ports.in.BuyerPort;
import application.domain.ports.in.LogisticsOperatorPort;
import application.domain.ports.in.PublicAccessPort;
import application.domain.ports.in.SellerPort;
import application.domain.ports.in.SupervisorPort;
import application.domain.valueobjects.Address;
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
import application.support.PortRecorder;
import application.support.TestFixture;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The whole marketplace through its seven input ports. After all the tests, every operation of
 * every port must have been executed.
 */
class MarketplaceFlowTest {

    private static final PortRecorder RECORDER = new PortRecorder();
    private static final PhoneNumber PHONE = PhoneNumber.of("+573001112233");
    private static final String PASSWORD = "Clave2026";
    private static final WarehouseLocation LOCATION = WarehouseLocation.of("A", "1", "2");
    private static final Address ADDRESS = TestFixture.address();

    private TestFixture f;
    private PublicAccessPort publicAccess;
    private AccountPort account;
    private BuyerPort buyerPort;
    private SellerPort sellerPort;
    private LogisticsOperatorPort logisticsPort;
    private AdministratorPort administratorPort;
    private SupervisorPort supervisorPort;

    private UserId admin;
    private UserId seller;
    private UserId operator;
    private UserId supervisor;
    private UserId buyer;
    private WarehouseId warehouse;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        publicAccess = RECORDER.wrap(PublicAccessPort.class, f.publicAccessPort);
        account = RECORDER.wrap(AccountPort.class, f.accountPort);
        buyerPort = RECORDER.wrap(BuyerPort.class, f.buyerPort);
        sellerPort = RECORDER.wrap(SellerPort.class, f.sellerPort);
        logisticsPort = RECORDER.wrap(LogisticsOperatorPort.class, f.logisticsOperatorPort);
        administratorPort = RECORDER.wrap(AdministratorPort.class, f.administratorPort);
        supervisorPort = RECORDER.wrap(SupervisorPort.class, f.supervisorPort);

        admin = f.admin().getId();
        seller = administratorPort.registerSeller(admin, "Seller", Email.of("seller@mail.com"), PHONE,
            PASSWORD, "Store").getId();
        operator = administratorPort.registerStaffUser(admin, UserRole.LOGISTICS_OPERATOR, "Operator",
            Email.of("operator@mail.com"), PHONE, PASSWORD).getId();
        supervisor = administratorPort.registerStaffUser(admin, UserRole.SUPERVISOR, "Supervisor",
            Email.of("supervisor@mail.com"), PHONE, PASSWORD).getId();
        buyer = publicAccess.registerBuyer("Buyer", Email.of("buyer@mail.com"), PHONE, PASSWORD, ADDRESS)
            .getId();
        assertEquals(SellerStatus.APPROVED, administratorPort.approveSeller(admin, seller).getStatus());
        warehouse = sellerPort.createWarehouse(seller, "Seller warehouse", ADDRESS, LOCATION).getId();
    }

    @AfterAll
    static void everyOperationOfEveryPortWasExecuted() {
        assertEquals(List.of(), RECORDER.notCalled(PublicAccessPort.class, AccountPort.class,
            BuyerPort.class, SellerPort.class, LogisticsOperatorPort.class, AdministratorPort.class,
            SupervisorPort.class));
    }

    private ProductId publishedProduct(String code, ProductType type) {
        ProductId id = sellerPort.registerProduct(seller, ProductCode.of(code), type, "Product " + code,
            "Description", Money.of("1000", "COP")).getId();
        sellerPort.publishProduct(seller, id);
        return id;
    }

    private ProductId stockedProduct(String code, int units) {
        ProductId id = publishedProduct(code, ProductType.PHYSICAL);
        sellerPort.createInventory(seller, id, warehouse, Quantity.of(units), Quantity.of(2), LOCATION);
        return id;
    }

    private OrderId paidOrder(ProductId product, int units) {
        buyerPort.addCartItem(buyer, product, Quantity.of(units));
        return buyerPort.payOrder(buyer, buyerPort.placeOrder(buyer).getId()).getId();
    }

    private OrderId deliveredOrder(ProductId product, int units) {
        OrderId order = paidOrder(product, units);
        Shipment shipment = logisticsPort.createShipment(operator, order, warehouse);
        logisticsPort.packShipment(operator, shipment.getId());
        logisticsPort.dispatchShipment(operator, shipment.getId(), "TRACK-1");
        logisticsPort.confirmDelivery(operator, shipment.getId());
        return order;
    }

    private int onHand(ProductId product) {
        return sellerPort.findInventory(seller, product, warehouse).getOnHand().getValue();
    }

    @Test
    void physicalProductFromTheCatalogToTheRefund() {
        ProductId product = sellerPort.registerProduct(seller, ProductCode.of("TEC-0001"),
            ProductType.PHYSICAL, "Laptop", "A laptop", Money.of("1000", "COP")).getId();
        assertTrue(publicAccess.searchProducts(null).isEmpty());
        sellerPort.publishProduct(seller, product);
        sellerPort.createInventory(seller, product, warehouse, Quantity.of(10), Quantity.of(2), LOCATION);
        assertEquals("Laptop", publicAccess.findProduct(product).getName());
        assertEquals(1, publicAccess.searchProducts("lap").size());

        AuthSession session = publicAccess.login(Email.of("buyer@mail.com"), PASSWORD);
        assertEquals(buyer, account.validateToken(session.token()).userId());

        buyerPort.addCartItem(buyer, product, Quantity.of(3));
        Order placed = buyerPort.placeOrder(buyer);
        assertEquals(OrderStatus.PENDING_PAYMENT, placed.getStatus());
        assertEquals(10, onHand(product));

        OrderId order = placed.getId();
        assertEquals(OrderStatus.PAID, buyerPort.payOrder(buyer, order).getStatus());
        assertEquals(7, onHand(product));
        assertEquals(InvoiceStatus.ISSUED, buyerPort.findInvoice(buyer, order).getStatus());

        Shipment shipment = logisticsPort.createShipment(operator, order, warehouse);
        assertEquals(ShipmentStatus.PACKED, logisticsPort.packShipment(operator, shipment.getId()).getStatus());
        assertEquals(ShipmentStatus.IN_TRANSIT,
            logisticsPort.dispatchShipment(operator, shipment.getId(), "TRACK-1").getStatus());
        assertEquals(OrderStatus.SHIPPED, buyerPort.findOrder(buyer, order).getStatus());
        assertEquals(ShipmentStatus.DELIVERED,
            logisticsPort.confirmDelivery(operator, shipment.getId()).getStatus());
        assertEquals(OrderStatus.DELIVERED, buyerPort.findOrder(buyer, order).getStatus());

        ReturnRequest returnRequest = buyerPort.requestReturn(buyer, order, product, Quantity.of(1), "Damaged");
        assertEquals(ReturnStatus.APPROVED,
            administratorPort.approveReturn(admin, returnRequest.getId()).getStatus());
        assertEquals(ReturnStatus.RECEIVED,
            logisticsPort.receiveReturn(operator, returnRequest.getId()).getStatus());
        assertEquals(8, onHand(product));

        Refund refund = buyerPort.findRefunds(buyer, order).get(0);
        assertEquals(Money.of("1000", "COP"), refund.getAmount());
        assertEquals(RefundStatus.PROCESSED, administratorPort.processRefund(admin, refund.getId()).getStatus());

        account.logout(session.token());
        assertThrows(InvalidTokenException.class, () -> account.validateToken(session.token()));
    }

    @Test
    void digitalProductIsDeliveredWhenPaidAndCannotBeReturned() {
        ProductId product = publishedProduct("DIG-0001", ProductType.DIGITAL);
        OrderId order = paidOrder(product, 1);
        assertEquals(OrderStatus.DELIVERED, buyerPort.findOrder(buyer, order).getStatus());
        assertEquals(InvoiceStatus.ISSUED, buyerPort.findInvoice(buyer, order).getStatus());
        assertTrue(buyerPort.findShipments(buyer, order).isEmpty());
        assertThrows(BusinessRuleException.class,
            () -> buyerPort.requestReturn(buyer, order, product, Quantity.of(1), "Changed my mind"));
    }

    @Test
    void accountOperationsWorkForAnyAuthenticatedUser() {
        assertEquals(seller, account.findProfile(seller).getId());
        assertEquals("New name", account.updateProfile(seller, "New name", null).getFullName());
        assertEquals(1, account.addAddress(supervisor, ADDRESS).getAddresses().size());
        assertTrue(account.removeAddress(supervisor, ADDRESS).getAddresses().isEmpty());
        account.changePassword(operator, PASSWORD, "Nueva2026");
        assertEquals(operator, publicAccess.login(Email.of("operator@mail.com"), "Nueva2026").user().getId());
        assertThrows(InvalidCredentialsException.class,
            () -> publicAccess.login(Email.of("operator@mail.com"), PASSWORD));
    }

    @Test
    void buyerManagesTheCartAndTheirOrders() {
        ProductId first = stockedProduct("TEC-0001", 10);
        ProductId second = stockedProduct("TEC-0002", 10);
        assertTrue(buyerPort.findCart(buyer).isEmpty());
        buyerPort.addCartItem(buyer, first, Quantity.of(1));
        buyerPort.addCartItem(buyer, second, Quantity.of(1));
        assertEquals(5, buyerPort.changeCartItemQuantity(buyer, first, Quantity.of(5)).getItems().get(0)
            .getQuantity().getValue());
        assertEquals(1, buyerPort.removeCartItem(buyer, second).getItems().size());
        assertTrue(buyerPort.clearCart(buyer).isEmpty());

        OrderId order = paidOrder(first, 1);
        assertEquals(List.of(order), buyerPort.findOrders(buyer).stream().map(Order::getId).toList());
        assertEquals(OrderStatus.CANCELLED, buyerPort.cancelOrder(buyer, order).getStatus());
        assertTrue(buyerPort.findReturns(buyer, order).isEmpty());
    }

    @Test
    void sellerManagesProductsWarehousesAndStock() {
        ProductId product = stockedProduct("TEC-0001", 10);
        assertEquals("Desk", sellerPort.updateProduct(seller, product, "Desk", null).getName());
        assertEquals(Money.of("5", "COP"),
            sellerPort.changeProductPrice(seller, product, Money.of("5", "COP")).getPrice());
        assertEquals(ProductStatus.SUSPENDED, sellerPort.suspendProduct(seller, product).getStatus());
        assertEquals(ProductStatus.PUBLISHED, sellerPort.publishProduct(seller, product).getStatus());
        assertEquals(1, sellerPort.findProducts(seller).size());

        assertEquals("Renamed",
            sellerPort.updateWarehouse(seller, warehouse, "Renamed", null, null).getName());
        assertEquals(WarehouseStatus.INACTIVE, sellerPort.deactivateWarehouse(seller, warehouse).getStatus());
        assertEquals(WarehouseStatus.ACTIVE, sellerPort.activateWarehouse(seller, warehouse).getStatus());
        assertEquals(warehouse, sellerPort.findWarehouse(seller, warehouse).getId());
        assertEquals(1, sellerPort.findWarehouses(seller).size());

        assertEquals(15, sellerPort.restock(seller, product, warehouse, Quantity.of(5), null)
            .getOnHand().getValue());
        assertEquals(12, sellerPort.adjustStock(seller, product, warehouse, -3, "Damaged")
            .getOnHand().getValue());
        assertEquals(4, sellerPort.changeReorderThreshold(seller, product, warehouse, Quantity.of(4))
            .getReorderThreshold().getValue());
        assertEquals(1, sellerPort.findInventories(seller).size());
        assertEquals(3, sellerPort.findMovements(seller, product, warehouse).size());

        OrderId order = paidOrder(product, 1);
        assertEquals(order, sellerPort.findOrder(seller, order).getId());
        assertEquals(1, sellerPort.findOrders(seller).size());
        assertEquals(ProductStatus.DISCONTINUED, sellerPort.discontinueProduct(seller, product).getStatus());
    }

    @Test
    void logisticsOperatorManagesStockAndConsultsOperations() {
        ProductId product = publishedProduct("TEC-0001", ProductType.PHYSICAL);
        logisticsPort.createInventory(operator, product, warehouse, Quantity.of(10), Quantity.of(2), LOCATION);
        assertEquals(15, logisticsPort.restock(operator, product, warehouse, Quantity.of(5), "Arrival")
            .getOnHand().getValue());
        assertEquals(14, logisticsPort.adjustStock(operator, product, warehouse, -1, "Damaged")
            .getOnHand().getValue());
        assertEquals(20, logisticsPort.changeReorderThreshold(operator, product, warehouse, Quantity.of(20))
            .getReorderThreshold().getValue());
        assertEquals(14, logisticsPort.findInventory(operator, product, warehouse).getOnHand().getValue());
        assertEquals(1, logisticsPort.findInventoriesByWarehouse(operator, warehouse).size());
        assertEquals(1, logisticsPort.findInventories(operator).size());
        assertEquals(1, logisticsPort.findBelowReorderPoint(operator).size());
        assertEquals(3, logisticsPort.findMovements(operator, product, warehouse).size());

        OrderId order = deliveredOrder(product, 1);
        buyerPort.requestReturn(buyer, order, product, Quantity.of(1), "Damaged");
        assertEquals(1, logisticsPort.findShipmentsByOrder(operator, order).size());
        assertEquals(1, logisticsPort.findShipments(operator).size());
        assertEquals(1, logisticsPort.findReturns(operator).size());
        assertEquals(order, logisticsPort.findOrder(operator, order).getId());
        assertEquals(1, logisticsPort.findOrders(operator).size());
        assertEquals(1, logisticsPort.findProducts(operator).size());
        assertEquals(warehouse, logisticsPort.findWarehouse(operator, warehouse).getId());
        assertEquals(1, logisticsPort.findWarehouses(operator).size());
    }

    @Test
    void administratorModeratesTheMarketplace() {
        ProductId product = stockedProduct("TEC-0001", 10);
        assertEquals(SellerStatus.SUSPENDED, administratorPort.suspendSeller(admin, seller).getStatus());
        assertTrue(publicAccess.searchProducts(null).isEmpty());
        administratorPort.approveSeller(admin, seller);
        assertEquals(seller, administratorPort.findUser(admin, seller).getId());
        assertEquals(5, administratorPort.findUsers(admin).size());

        WarehouseId main = administratorPort.createWarehouse(admin, "Main", ADDRESS, LOCATION).getId();
        assertEquals("Central", administratorPort.updateWarehouse(admin, main, "Central", null, null).getName());
        assertEquals(WarehouseStatus.INACTIVE, administratorPort.deactivateWarehouse(admin, main).getStatus());
        assertEquals(WarehouseStatus.ACTIVE, administratorPort.activateWarehouse(admin, main).getStatus());
        assertEquals(main, administratorPort.findWarehouse(admin, main).getId());
        assertEquals(2, administratorPort.findWarehouses(admin).size());

        assertEquals(ProductStatus.BLOCKED, administratorPort.blockProduct(admin, product).getStatus());
        assertEquals(ProductStatus.SUSPENDED, administratorPort.unblockProduct(admin, product).getStatus());
        sellerPort.publishProduct(seller, product);
        assertEquals(1, administratorPort.findProducts(admin).size());

        assertEquals(10, administratorPort.findInventory(admin, product, warehouse).getOnHand().getValue());
        assertEquals(1, administratorPort.findInventories(admin).size());
        assertTrue(administratorPort.findBelowReorderPoint(admin).isEmpty());
        assertEquals(1, administratorPort.findMovements(admin, product, warehouse).size());

        OrderId cancelled = paidOrder(product, 1);
        assertEquals(OrderStatus.CANCELLED, administratorPort.cancelOrder(admin, cancelled).getStatus());
        assertEquals(InvoiceStatus.VOIDED, administratorPort.findInvoice(admin, cancelled).getStatus());
        Refund refund = administratorPort.findRefunds(admin).get(0);
        assertEquals(RefundStatus.REJECTED, administratorPort.rejectRefund(admin, refund.getId()).getStatus());

        OrderId delivered = deliveredOrder(product, 1);
        assertEquals(delivered, administratorPort.findOrder(admin, delivered).getId());
        assertEquals(2, administratorPort.findOrders(admin).size());
        assertEquals(2, administratorPort.findInvoices(admin).size());
        assertEquals(1, administratorPort.findShipments(admin).size());
        ReturnRequest returnRequest = buyerPort.requestReturn(buyer, delivered, product, Quantity.of(1), "Damaged");
        assertEquals(ReturnStatus.REJECTED, administratorPort.rejectReturn(admin, returnRequest.getId()).getStatus());
        assertEquals(1, administratorPort.findReturns(admin).size());

        assertEquals(ProductStatus.DISCONTINUED, administratorPort.discontinueProduct(admin, product).getStatus());
    }

    @Test
    void supervisorReadsEverything() {
        ProductId product = stockedProduct("TEC-0001", 10);
        OrderId order = deliveredOrder(product, 1);
        buyerPort.requestReturn(buyer, order, product, Quantity.of(1), "Damaged");
        OrderId cancelled = paidOrder(product, 1);
        buyerPort.cancelOrder(buyer, cancelled);

        assertEquals(buyer, supervisorPort.findUser(supervisor, buyer).getId());
        assertEquals(5, supervisorPort.findUsers(supervisor).size());
        assertEquals(1, supervisorPort.findProducts(supervisor).size());
        assertEquals(warehouse, supervisorPort.findWarehouse(supervisor, warehouse).getId());
        assertEquals(1, supervisorPort.findWarehouses(supervisor).size());
        assertEquals(9, supervisorPort.findInventory(supervisor, product, warehouse).getOnHand().getValue());
        assertEquals(1, supervisorPort.findInventories(supervisor).size());
        assertTrue(supervisorPort.findBelowReorderPoint(supervisor).isEmpty());
        assertFalse(supervisorPort.findMovements(supervisor, product, warehouse).isEmpty());
        assertEquals(order, supervisorPort.findOrder(supervisor, order).getId());
        assertEquals(2, supervisorPort.findOrders(supervisor).size());
        assertEquals(order, supervisorPort.findInvoice(supervisor, order).getOrderId());
        assertEquals(2, supervisorPort.findInvoices(supervisor).size());
        assertEquals(1, supervisorPort.findShipments(supervisor).size());
        assertEquals(1, supervisorPort.findReturns(supervisor).size());
        assertEquals(1, supervisorPort.findRefunds(supervisor).size());
    }

    @Test
    void unavailableProductIsNotFoundInTheCatalog() {
        Product product = sellerPort.registerProduct(seller, ProductCode.of("TEC-0001"), ProductType.PHYSICAL,
            "Laptop", "A laptop", Money.of("1000", "COP"));
        assertEquals(ProductStatus.DRAFT, product.getStatus());
        assertTrue(publicAccess.searchProducts("Laptop").isEmpty());
    }
}
