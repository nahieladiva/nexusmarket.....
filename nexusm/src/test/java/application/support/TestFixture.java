package application.support;

import application.domain.valueobjects.ProductId;
import application.domain.enums.ProductType;
import application.domain.enums.SellerStatus;
import application.domain.models.Administrator;
import application.domain.models.Buyer;
import application.domain.models.Inventory;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.services.inventory.RecordStockMovementService;
import application.domain.services.order.CancelOrderService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.order.NotifyOrderStatusChangeService;
import application.domain.services.order.PayOrderService;
import application.domain.services.order.PlaceOrderService;
import application.domain.services.cart.AddItemToCartService;
import application.domain.services.cart.ChangeCartItemQuantityService;
import application.domain.services.cart.ClearCartService;
import application.domain.services.cart.ConsultCartService;
import application.domain.services.cart.RemoveItemFromCartService;
import application.domain.services.InventoryDomainService;
import application.domain.services.order.OrderFulfillmentDomainService;
import application.domain.services.PasswordPolicy;
import application.domain.services.WarehouseAllocationService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseLocation;
import application.services.AccessRules;
import application.services.AuthenticationApplicationService;
import application.services.InventoryApplicationService;
import application.services.OrderApplicationService;
import application.services.ProductApplicationService;
import application.services.StockMovementRecorder;
import application.services.UserApplicationService;
import application.services.WarehouseApplicationService;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Arma todos los casos de uso con adaptadores en memoria, sin Spring ni bases de datos.
 */
public class TestFixture {

    public static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 2, 10, 0);

    public final InMemoryUserRepository users = new InMemoryUserRepository();
    public final InMemoryProductRepository products = new InMemoryProductRepository();
    public final InMemoryWarehouseRepository warehouses = new InMemoryWarehouseRepository();
    public final InMemoryInventoryRepository inventories = new InMemoryInventoryRepository();
    public final InMemoryOrderRepository orders = new InMemoryOrderRepository();
    public final InMemoryCartRepository carts = new InMemoryCartRepository();
    public final InMemoryStockMovementLog movements = new InMemoryStockMovementLog();
    public final InMemoryTokenStore tokenStore = new InMemoryTokenStore();
    public final FakeTokenProvider tokenProvider = new FakeTokenProvider();
    public final FakePasswordEncoder passwordEncoder = new FakePasswordEncoder();
    public final RecordingEventPublisher events = new RecordingEventPublisher();
    public final RecordingNotifier notifier = new RecordingNotifier();
    public final FixedClock clock = new FixedClock(NOW);
    public final SequentialIdGenerator ids = new SequentialIdGenerator();

    public final AuthenticationApplicationService auth;
    public final UserApplicationService userService;
    public final ProductApplicationService productService;
    public final WarehouseApplicationService warehouseService;
    public final InventoryApplicationService inventoryService;
    public final OrderApplicationService orderService;

    public final ConsultCartService consultCart;
    public final AddItemToCartService addItemToCart;
    public final ChangeCartItemQuantityService changeCartItemQuantity;
    public final RemoveItemFromCartService removeItemFromCart;
    public final ClearCartService clearCart;

    public final RecordStockMovementService recordStockMovement;
    public final NotifyOrderStatusChangeService notifyOrderStatusChange;
    public final PlaceOrderService placeOrder;
    public final PayOrderService payOrder;
    public final CancelOrderService cancelOrder;
    public final ConsultOrderService consultOrder;

    public TestFixture() {
        AccessRules accessRules = new AccessRules(users);
        InventoryDomainService inventoryDomain = new InventoryDomainService(inventories);
        WarehouseAllocationService allocation = new WarehouseAllocationService(inventories, warehouses);
        OrderFulfillmentDomainService fulfillment = new OrderFulfillmentDomainService(inventoryDomain, allocation);
        StockMovementRecorder recorder = new StockMovementRecorder(movements, events, notifier,
            products, clock, ids);

        auth = new AuthenticationApplicationService(users, passwordEncoder, tokenProvider, tokenStore,
            clock, ids, new PasswordPolicy());
        userService = new UserApplicationService(users, notifier);
        productService = new ProductApplicationService(products, accessRules, events, clock, ids);
        warehouseService = new WarehouseApplicationService(warehouses, ids);
        inventoryService = new InventoryApplicationService(inventories, products, warehouses, movements,
            recorder, accessRules, ids);
        orderService = new OrderApplicationService(orders, products, fulfillment, allocation,
            recorder, accessRules, events, notifier, clock, ids);

        consultCart = new ConsultCartService(carts, clock, ids);
        addItemToCart = new AddItemToCartService(carts, products, consultCart);
        changeCartItemQuantity = new ChangeCartItemQuantityService(carts, consultCart);
        removeItemFromCart = new RemoveItemFromCartService(carts, consultCart);
        clearCart = new ClearCartService(carts, consultCart);

        recordStockMovement = new RecordStockMovementService(movements, events, notifier, products, clock, ids);
        notifyOrderStatusChange = new NotifyOrderStatusChangeService(events, notifier, clock);
        placeOrder = new PlaceOrderService(carts, orders, products, consultCart, allocation, events, clock, ids);
        payOrder = new PayOrderService(orders, fulfillment, recordStockMovement, notifyOrderStatusChange, clock);
        cancelOrder = new CancelOrderService(orders, fulfillment, recordStockMovement, notifyOrderStatusChange);
        consultOrder = new ConsultOrderService(orders, products);
    }

    // ---------- Datos de apoyo

    public static Address address() {
        return Address.of("Calle 10 # 43-20", "Medellín", "Antioquia", "050021", "Colombia");
    }

    public User admin() {
        return users.save(new Administrator(UserId.of(ids.newId()), "Admin", Email.of("admin@nexus.com"),
            PhoneNumber.of("+573000000000"), "hashed:Admin1234", List.of(), NOW));
    }

    public Seller approvedSeller(String email) {
        Seller seller = new Seller(UserId.of(ids.newId()), "Vendedor " + email, Email.of(email),
            PhoneNumber.of("+573001234567"), "hashed:Clave123", "Tienda " + email, List.of(),
            SellerStatus.APPROVED, NOW);
        return (Seller) users.save(seller);
    }

    public Buyer buyer(String email) {
        Buyer buyer = new Buyer(UserId.of(ids.newId()), "Comprador " + email, Email.of(email),
            PhoneNumber.of("+573009876543"), "hashed:Clave123", address(), List.of(), NOW);
        return (Buyer) users.save(buyer);
    }

    public Warehouse warehouse(String name) {
        return warehouseService.createWarehouse(name, address(), WarehouseLocation.of("A", "1", "1"));
    }

    public Product digitalProduct(Seller seller, String code, String price) {
        Product product = Product.register(ProductId.random(), ProductCode.of(code), ProductType.DIGITAL,
            "Digital " + code, "Digital product", Money.of(price, "COP"), seller.getId(), NOW);
        product.publish();
        return products.save(product);
    }

    public Product product(Seller seller, String code, String price) {
        return productService.createProduct(seller.getId(), ProductCode.of(code), "Producto " + code,
            "Descripción", Money.of(price, "COP"));
    }

    public Inventory stock(Seller seller, Product product, Warehouse warehouse, int onHand, int threshold) {
        return inventoryService.createInventory(seller.getId(), product.getId(), warehouse.getId(),
            Quantity.of(onHand), Quantity.of(threshold), WarehouseLocation.of("A", "1", "1"));
    }
}
