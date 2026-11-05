package application.support;

import application.domain.models.Supervisor;
import application.domain.models.LogisticsOperator;
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
import application.domain.services.inventory.AdjustStockService;
import application.domain.services.inventory.ChangeReorderThresholdService;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.inventory.CreateInventoryService;
import application.domain.services.inventory.RecordStockMovementService;
import application.domain.services.inventory.RestockInventoryService;
import application.domain.services.inventory.ValidateInventoryAccessService;
import application.domain.services.invoice.ConsultInvoiceService;
import application.domain.services.invoice.IssueInvoiceService;
import application.domain.services.invoice.VoidInvoiceService;
import application.domain.services.order.CancelOrderService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.order.NotifyOrderStatusChangeService;
import application.domain.services.order.PayOrderService;
import application.domain.services.order.PlaceOrderService;
import application.domain.services.product.BlockProductService;
import application.domain.services.product.ChangeProductPriceService;
import application.domain.services.product.ConsultProductService;
import application.domain.services.product.DiscontinueProductService;
import application.domain.services.product.PublishProductService;
import application.domain.services.product.RegisterProductService;
import application.domain.services.product.SuspendProductService;
import application.domain.services.product.UnblockProductService;
import application.domain.services.product.UpdateProductService;
import application.domain.services.product.ValidateProductAvailabilityService;
import application.domain.services.refund.ConsultRefundService;
import application.domain.services.refund.CreateRefundService;
import application.domain.services.refund.ProcessRefundService;
import application.domain.services.refund.RejectRefundService;
import application.domain.services.returns.ApproveReturnService;
import application.domain.services.returns.ConsultReturnService;
import application.domain.services.returns.ReceiveReturnService;
import application.domain.services.returns.RejectReturnService;
import application.domain.services.returns.RequestReturnService;
import application.domain.services.user.AddAddressService;
import application.domain.services.user.ApproveSellerService;
import application.domain.services.user.ConsultUserService;
import application.domain.services.user.RegisterBuyerService;
import application.domain.services.user.RegisterSellerService;
import application.domain.services.user.RegisterStaffUserService;
import application.domain.services.user.RemoveAddressService;
import application.domain.services.user.SuspendSellerService;
import application.domain.services.user.UpdateProfileService;
import application.domain.services.user.ValidateUserUniquenessService;
import application.domain.services.warehouse.ActivateWarehouseService;
import application.domain.services.warehouse.ConsultWarehouseService;
import application.domain.services.warehouse.CreateMarketplaceWarehouseService;
import application.domain.services.warehouse.CreateSellerWarehouseService;
import application.domain.services.warehouse.DeactivateWarehouseService;
import application.domain.services.warehouse.UpdateWarehouseService;
import application.domain.services.warehouse.ValidateWarehouseNameService;
import application.domain.services.shipment.ApplyShipmentProgressService;
import application.domain.services.shipment.ConfirmDeliveryService;
import application.domain.services.shipment.ConsultShipmentService;
import application.domain.services.shipment.CreateShipmentService;
import application.domain.services.shipment.DispatchShipmentService;
import application.domain.services.shipment.PackShipmentService;
import application.domain.services.authorization.LoginService;
import application.domain.services.authorization.LogoutService;
import application.domain.services.authorization.ResolveActorService;
import application.domain.services.authorization.ValidateTokenService;
import application.domain.services.cart.AddItemToCartService;
import application.domain.services.cart.ChangeCartItemQuantityService;
import application.domain.services.cart.ClearCartService;
import application.domain.services.cart.ConsultCartService;
import application.domain.services.cart.RemoveItemFromCartService;
import application.domain.services.inventory.InventoryDomainService;
import application.domain.services.order.OrderFulfillmentDomainService;
import application.domain.services.user.PasswordPolicy;
import application.domain.services.inventory.WarehouseAllocationService;
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
    public final InMemoryInvoiceRepository invoices = new InMemoryInvoiceRepository();
    public final InMemoryShipmentRepository shipments = new InMemoryShipmentRepository();
    public final InMemoryReturnRequestRepository returns = new InMemoryReturnRequestRepository();
    public final InMemoryRefundRepository refunds = new InMemoryRefundRepository();
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

    public final IssueInvoiceService issueInvoice;
    public final VoidInvoiceService voidInvoice;
    public final ConsultInvoiceService consultInvoice;

    public final CreateShipmentService createShipment;
    public final PackShipmentService packShipment;
    public final DispatchShipmentService dispatchShipment;
    public final ConfirmDeliveryService confirmDelivery;
    public final ConsultShipmentService consultShipment;

    public final RequestReturnService requestReturn;
    public final ApproveReturnService approveReturn;
    public final RejectReturnService rejectReturn;
    public final ReceiveReturnService receiveReturn;
    public final ConsultReturnService consultReturn;

    public final ProcessRefundService processRefund;
    public final RejectRefundService rejectRefund;
    public final ConsultRefundService consultRefund;

    public final RegisterProductService registerProduct;
    public final UpdateProductService updateProduct;
    public final ChangeProductPriceService changeProductPrice;
    public final PublishProductService publishProduct;
    public final SuspendProductService suspendProduct;
    public final DiscontinueProductService discontinueProduct;
    public final ConsultProductService consultProduct;
    public final BlockProductService blockProduct;
    public final UnblockProductService unblockProduct;

    public final CreateInventoryService createInventory;
    public final RestockInventoryService restockInventory;
    public final AdjustStockService adjustStock;
    public final ChangeReorderThresholdService changeReorderThreshold;
    public final ConsultInventoryService consultInventory;

    public final CreateMarketplaceWarehouseService createMarketplaceWarehouse;
    public final CreateSellerWarehouseService createSellerWarehouse;
    public final UpdateWarehouseService updateWarehouse;
    public final ActivateWarehouseService activateWarehouse;
    public final DeactivateWarehouseService deactivateWarehouse;
    public final ConsultWarehouseService consultWarehouse;

    public final RegisterBuyerService registerBuyer;
    public final RegisterSellerService registerSeller;
    public final RegisterStaffUserService registerStaffUser;
    public final ApproveSellerService approveSeller;
    public final SuspendSellerService suspendSeller;
    public final UpdateProfileService updateProfile;
    public final AddAddressService addAddress;
    public final RemoveAddressService removeAddress;
    public final ConsultUserService consultUser;

    public final ResolveActorService resolveActor;
    public final LoginService login;
    public final ValidateTokenService validateToken;
    public final LogoutService logout;

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
        ValidateProductAvailabilityService productAvailability = new ValidateProductAvailabilityService(users);
        addItemToCart = new AddItemToCartService(carts, products, consultCart, productAvailability);
        changeCartItemQuantity = new ChangeCartItemQuantityService(carts, consultCart);
        removeItemFromCart = new RemoveItemFromCartService(carts, consultCart);
        clearCart = new ClearCartService(carts, consultCart);

        recordStockMovement = new RecordStockMovementService(movements, events, notifier, products, clock, ids);
        notifyOrderStatusChange = new NotifyOrderStatusChangeService(events, notifier, clock);
        placeOrder = new PlaceOrderService(carts, orders, products, consultCart, productAvailability, allocation,
            events, clock, ids);
        CreateRefundService createRefund = new CreateRefundService(refunds, clock, ids);
        issueInvoice = new IssueInvoiceService(invoices, clock, ids);
        voidInvoice = new VoidInvoiceService(invoices, clock);
        consultInvoice = new ConsultInvoiceService(invoices);
        payOrder = new PayOrderService(orders, fulfillment, recordStockMovement, notifyOrderStatusChange,
            issueInvoice, clock);
        cancelOrder = new CancelOrderService(orders, fulfillment, recordStockMovement, notifyOrderStatusChange,
            voidInvoice, shipments, createRefund);
        consultOrder = new ConsultOrderService(orders, products);

        ApplyShipmentProgressService applyShipmentProgress =
            new ApplyShipmentProgressService(orders, shipments, notifyOrderStatusChange);
        createShipment = new CreateShipmentService(shipments, orders, users, clock, ids);
        packShipment = new PackShipmentService(shipments);
        dispatchShipment = new DispatchShipmentService(shipments, applyShipmentProgress, clock);
        confirmDelivery = new ConfirmDeliveryService(shipments, applyShipmentProgress, clock);
        consultShipment = new ConsultShipmentService(shipments, consultOrder);

        requestReturn = new RequestReturnService(returns, orders, clock, ids);
        approveReturn = new ApproveReturnService(returns, clock);
        rejectReturn = new RejectReturnService(returns, clock);
        receiveReturn = new ReceiveReturnService(returns, orders, inventoryDomain, recordStockMovement,
            createRefund);
        consultReturn = new ConsultReturnService(returns, consultOrder);

        processRefund = new ProcessRefundService(refunds, notifier, clock);
        rejectRefund = new RejectRefundService(refunds, notifier, clock);
        consultRefund = new ConsultRefundService(refunds, consultOrder);

        registerProduct = new RegisterProductService(products, events, clock, ids);
        updateProduct = new UpdateProductService(products);
        changeProductPrice = new ChangeProductPriceService(products);
        publishProduct = new PublishProductService(products);
        suspendProduct = new SuspendProductService(products);
        discontinueProduct = new DiscontinueProductService(products);
        consultProduct = new ConsultProductService(products, productAvailability);
        blockProduct = new BlockProductService(products, notifier);
        unblockProduct = new UnblockProductService(products, notifier);

        ValidateInventoryAccessService inventoryAccess = new ValidateInventoryAccessService(products, warehouses);
        createInventory = new CreateInventoryService(inventories, products, warehouses, inventoryAccess,
            recordStockMovement, ids);
        restockInventory = new RestockInventoryService(inventoryDomain, inventoryAccess, recordStockMovement);
        adjustStock = new AdjustStockService(inventories, inventoryAccess, recordStockMovement);
        changeReorderThreshold = new ChangeReorderThresholdService(inventories, inventoryAccess);
        consultInventory = new ConsultInventoryService(inventories, products, movements, inventoryAccess);

        ValidateWarehouseNameService warehouseName = new ValidateWarehouseNameService(warehouses);
        createMarketplaceWarehouse = new CreateMarketplaceWarehouseService(warehouses, warehouseName, ids);
        createSellerWarehouse = new CreateSellerWarehouseService(warehouses, warehouseName, ids);
        updateWarehouse = new UpdateWarehouseService(warehouses, warehouseName);
        activateWarehouse = new ActivateWarehouseService(warehouses);
        deactivateWarehouse = new DeactivateWarehouseService(warehouses);
        consultWarehouse = new ConsultWarehouseService(warehouses);

        ValidateUserUniquenessService userUniqueness = new ValidateUserUniquenessService(users);
        PasswordPolicy passwordPolicy = new PasswordPolicy();
        registerBuyer = new RegisterBuyerService(users, userUniqueness, passwordPolicy, passwordEncoder, clock, ids);
        registerSeller = new RegisterSellerService(users, userUniqueness, passwordPolicy, passwordEncoder, clock, ids);
        registerStaffUser = new RegisterStaffUserService(users, userUniqueness, passwordPolicy, passwordEncoder,
            clock, ids);
        approveSeller = new ApproveSellerService(users, notifier);
        suspendSeller = new SuspendSellerService(users, notifier);
        updateProfile = new UpdateProfileService(users);
        addAddress = new AddAddressService(users);
        removeAddress = new RemoveAddressService(users);
        consultUser = new ConsultUserService(users);

        resolveActor = new ResolveActorService(users);
        login = new LoginService(users, passwordEncoder, tokenProvider, tokenStore, clock, ids);
        validateToken = new ValidateTokenService(tokenProvider, tokenStore, clock);
        logout = new LogoutService(tokenStore, validateToken);
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

    public LogisticsOperator logisticsOperator(String email) {
        LogisticsOperator operator = new LogisticsOperator(UserId.random(), "Operator", Email.of(email),
            PhoneNumber.of("+573001234567"), "hashed:Clave123", List.of(), NOW);
        users.save(operator);
        return operator;
    }

    public Supervisor supervisor(String email) {
        Supervisor supervisor = new Supervisor(UserId.random(), "Supervisor", Email.of(email),
            PhoneNumber.of("+573001234568"), "hashed:Clave123", List.of(), NOW);
        users.save(supervisor);
        return supervisor;
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
