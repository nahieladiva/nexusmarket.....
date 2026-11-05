# Puertos de entrada — NexusMarket

Los puertos de entrada están en `application.domain.ports.in`. Hay uno por rol, más `PublicAccessPort`
para lo anónimo y `AccountPort` para la cuenta de cualquier usuario autenticado. Cada uno tiene una clase
en `application.adapters.useCases` que lo implementa con `Transactional` y delega en los servicios de
dominio. Las consultas usan transacciones de solo lectura.

| Puerto | Caso de uso | Quién lo usa | Operaciones |
|---|---|---|---|
| `PublicAccessPort` | `PublicAccessUseCase` | Anónimo | 4 |
| `AccountPort` | `AccountUseCase` | Cualquier usuario autenticado | 7 |
| `BuyerPort` | `BuyerUseCase` | `BUYER` | 15 |
| `SellerPort` | `SellerUseCase` | `SELLER` | 22 |
| `LogisticsOperatorPort` | `LogisticsOperatorUseCase` | `LOGISTICS_OPERATOR` | 22 |
| `AdministratorPort` | `AdministratorUseCase` | `ADMINISTRATOR` | 32 |
| `SupervisorPort` | `SupervisorUseCase` | `SUPERVISOR` | 16 |
| **Total** | | | **118** |

## `PublicAccessPort`

| Operación | Servicio |
|---|---|
| `User registerBuyer(String fullName, Email email, PhoneNumber phone, String rawPassword, Address defaultShippingAddress)` | `RegisterBuyerService.execute` |
| `AuthSession login(Email email, String rawPassword)` | `LoginService.execute` |
| `Product findProduct(ProductId productId)` | `ConsultProductService.findPublished` |
| `List<Product> searchProducts(String keyword)` | `ConsultProductService.searchPublished` |

## `AccountPort`

| Operación | Servicio |
|---|---|
| `User findProfile(UserId userId)` | `ResolveActorService.execute` |
| `User updateProfile(UserId userId, String fullName, PhoneNumber phone)` | `UpdateProfileService.execute` |
| `User addAddress(UserId userId, Address address)` | `AddAddressService.execute` |
| `User removeAddress(UserId userId, Address address)` | `RemoveAddressService.execute` |
| `User changePassword(UserId userId, String currentPassword, String newPassword)` | `ChangePasswordService.execute` |
| `TokenClaims validateToken(String token)` | `ValidateTokenService.execute` |
| `void logout(String token)` | `LogoutService.execute` |

## `BuyerPort`

| Operación | Servicio |
|---|---|
| `Cart findCart(UserId buyerId)` | `ConsultCartService.execute` |
| `Cart addCartItem(UserId buyerId, ProductId productId, Quantity quantity)` | `AddItemToCartService.execute` |
| `Cart changeCartItemQuantity(UserId buyerId, ProductId productId, Quantity quantity)` | `ChangeCartItemQuantityService.execute` |
| `Cart removeCartItem(UserId buyerId, ProductId productId)` | `RemoveItemFromCartService.execute` |
| `Cart clearCart(UserId buyerId)` | `ClearCartService.execute` |
| `Order placeOrder(UserId buyerId)` | `PlaceOrderService.execute` |
| `Order payOrder(UserId buyerId, OrderId orderId)` | `PayOrderService.execute` |
| `Order cancelOrder(UserId buyerId, OrderId orderId)` | `CancelOrderService.execute` |
| `Order findOrder(UserId buyerId, OrderId orderId)` | `ConsultOrderService.execute` |
| `List<Order> findOrders(UserId buyerId)` | `ConsultOrderService.findByBuyer` |
| `Invoice findInvoice(UserId buyerId, OrderId orderId)` | `ConsultInvoiceService.findByOrder` |
| `List<Shipment> findShipments(UserId buyerId, OrderId orderId)` | `ConsultShipmentService.findByOrder` |
| `ReturnRequest requestReturn(UserId buyerId, OrderId orderId, ProductId productId, Quantity quantity, String reason)` | `RequestReturnService.execute` |
| `List<ReturnRequest> findReturns(UserId buyerId, OrderId orderId)` | `ConsultReturnService.findByOrder` |
| `List<Refund> findRefunds(UserId buyerId, OrderId orderId)` | `ConsultRefundService.findByOrder` |

## `SellerPort`

| Operación | Servicio |
|---|---|
| `Product registerProduct(UserId sellerId, ProductCode code, ProductType type, String name, String description, Money price)` | `RegisterProductService.execute` |
| `Product updateProduct(UserId sellerId, ProductId productId, String name, String description)` | `UpdateProductService.execute` |
| `Product changeProductPrice(UserId sellerId, ProductId productId, Money newPrice)` | `ChangeProductPriceService.execute` |
| `Product publishProduct(UserId sellerId, ProductId productId)` | `PublishProductService.execute` |
| `Product suspendProduct(UserId sellerId, ProductId productId)` | `SuspendProductService.execute` |
| `Product discontinueProduct(UserId sellerId, ProductId productId)` | `DiscontinueProductService.execute` |
| `List<Product> findProducts(UserId sellerId)` | `ConsultProductService.findBySeller` |
| `Warehouse createWarehouse(UserId sellerId, String name, Address address, WarehouseLocation location)` | `CreateSellerWarehouseService.execute` |
| `Warehouse updateWarehouse(UserId sellerId, WarehouseId warehouseId, String name, Address address, WarehouseLocation location)` | `UpdateWarehouseService.execute` |
| `Warehouse activateWarehouse(UserId sellerId, WarehouseId warehouseId)` | `ActivateWarehouseService.execute` |
| `Warehouse deactivateWarehouse(UserId sellerId, WarehouseId warehouseId)` | `DeactivateWarehouseService.execute` |
| `Warehouse findWarehouse(UserId sellerId, WarehouseId warehouseId)` | `ConsultWarehouseService.execute` |
| `List<Warehouse> findWarehouses(UserId sellerId)` | `ConsultWarehouseService.findBySeller` |
| `Inventory createInventory(UserId sellerId, ProductId productId, WarehouseId warehouseId, Quantity onHand, Quantity reorderThreshold, WarehouseLocation location)` | `CreateInventoryService.execute` |
| `Inventory restock(UserId sellerId, ProductId productId, WarehouseId warehouseId, Quantity amount, String reason)` | `RestockInventoryService.execute` |
| `Inventory adjustStock(UserId sellerId, ProductId productId, WarehouseId warehouseId, int delta, String reason)` | `AdjustStockService.execute` |
| `Inventory changeReorderThreshold(UserId sellerId, ProductId productId, WarehouseId warehouseId, Quantity newThreshold)` | `ChangeReorderThresholdService.execute` |
| `Inventory findInventory(UserId sellerId, ProductId productId, WarehouseId warehouseId)` | `ConsultInventoryService.execute` |
| `List<Inventory> findInventories(UserId sellerId)` | `ConsultInventoryService.findBySeller` |
| `List<StockMovement> findMovements(UserId sellerId, ProductId productId, WarehouseId warehouseId)` | `ConsultInventoryService.findMovements` |
| `Order findOrder(UserId sellerId, OrderId orderId)` | `ConsultOrderService.execute` |
| `List<Order> findOrders(UserId sellerId)` | `ConsultOrderService.findBySeller` |

## `LogisticsOperatorPort`

| Operación | Servicio |
|---|---|
| `Inventory createInventory(UserId operatorId, ProductId productId, WarehouseId warehouseId, Quantity onHand, Quantity reorderThreshold, WarehouseLocation location)` | `CreateInventoryService.execute` |
| `Inventory restock(UserId operatorId, ProductId productId, WarehouseId warehouseId, Quantity amount, String reason)` | `RestockInventoryService.execute` |
| `Inventory adjustStock(UserId operatorId, ProductId productId, WarehouseId warehouseId, int delta, String reason)` | `AdjustStockService.execute` |
| `Inventory changeReorderThreshold(UserId operatorId, ProductId productId, WarehouseId warehouseId, Quantity newThreshold)` | `ChangeReorderThresholdService.execute` |
| `Inventory findInventory(UserId operatorId, ProductId productId, WarehouseId warehouseId)` | `ConsultInventoryService.execute` |
| `List<Inventory> findInventoriesByWarehouse(UserId operatorId, WarehouseId warehouseId)` | `ConsultInventoryService.findByWarehouse` |
| `List<Inventory> findInventories(UserId operatorId)` | `ConsultInventoryService.findAll` |
| `List<Inventory> findBelowReorderPoint(UserId operatorId)` | `ConsultInventoryService.findBelowReorderPoint` |
| `List<StockMovement> findMovements(UserId operatorId, ProductId productId, WarehouseId warehouseId)` | `ConsultInventoryService.findMovements` |
| `Shipment createShipment(UserId operatorId, OrderId orderId, WarehouseId warehouseId)` | `CreateShipmentService.execute` |
| `Shipment packShipment(UserId operatorId, ShipmentId shipmentId)` | `PackShipmentService.execute` |
| `Shipment dispatchShipment(UserId operatorId, ShipmentId shipmentId, String trackingNumber)` | `DispatchShipmentService.execute` |
| `Shipment confirmDelivery(UserId operatorId, ShipmentId shipmentId)` | `ConfirmDeliveryService.execute` |
| `List<Shipment> findShipmentsByOrder(UserId operatorId, OrderId orderId)` | `ConsultShipmentService.findByOrder` |
| `List<Shipment> findShipments(UserId operatorId)` | `ConsultShipmentService.findAll` |
| `ReturnRequest receiveReturn(UserId operatorId, ReturnId returnId)` | `ReceiveReturnService.execute` |
| `List<ReturnRequest> findReturns(UserId operatorId)` | `ConsultReturnService.findAll` |
| `Order findOrder(UserId operatorId, OrderId orderId)` | `ConsultOrderService.execute` |
| `List<Order> findOrders(UserId operatorId)` | `ConsultOrderService.findAll` |
| `List<Product> findProducts(UserId operatorId)` | `ConsultProductService.findAll` |
| `Warehouse findWarehouse(UserId operatorId, WarehouseId warehouseId)` | `ConsultWarehouseService.execute` |
| `List<Warehouse> findWarehouses(UserId operatorId)` | `ConsultWarehouseService.findAll` |

## `AdministratorPort`

| Operación | Servicio |
|---|---|
| `User registerSeller(UserId administratorId, String fullName, Email email, PhoneNumber phone, String rawPassword, String businessName)` | `RegisterSellerService.execute` |
| `User registerStaffUser(UserId administratorId, UserRole role, String fullName, Email email, PhoneNumber phone, String rawPassword)` | `RegisterStaffUserService.execute` |
| `Seller approveSeller(UserId administratorId, UserId sellerId)` | `ApproveSellerService.execute` |
| `Seller suspendSeller(UserId administratorId, UserId sellerId)` | `SuspendSellerService.execute` |
| `User findUser(UserId administratorId, UserId userId)` | `ConsultUserService.execute` |
| `List<User> findUsers(UserId administratorId)` | `ConsultUserService.findAll` |
| `Warehouse createWarehouse(UserId administratorId, String name, Address address, WarehouseLocation location)` | `CreateMarketplaceWarehouseService.execute` |
| `Warehouse updateWarehouse(UserId administratorId, WarehouseId warehouseId, String name, Address address, WarehouseLocation location)` | `UpdateWarehouseService.execute` |
| `Warehouse activateWarehouse(UserId administratorId, WarehouseId warehouseId)` | `ActivateWarehouseService.execute` |
| `Warehouse deactivateWarehouse(UserId administratorId, WarehouseId warehouseId)` | `DeactivateWarehouseService.execute` |
| `Warehouse findWarehouse(UserId administratorId, WarehouseId warehouseId)` | `ConsultWarehouseService.execute` |
| `List<Warehouse> findWarehouses(UserId administratorId)` | `ConsultWarehouseService.findAll` |
| `Product blockProduct(UserId administratorId, ProductId productId)` | `BlockProductService.execute` |
| `Product unblockProduct(UserId administratorId, ProductId productId)` | `UnblockProductService.execute` |
| `Product discontinueProduct(UserId administratorId, ProductId productId)` | `DiscontinueProductService.execute` |
| `List<Product> findProducts(UserId administratorId)` | `ConsultProductService.findAll` |
| `Inventory findInventory(UserId administratorId, ProductId productId, WarehouseId warehouseId)` | `ConsultInventoryService.execute` |
| `List<Inventory> findInventories(UserId administratorId)` | `ConsultInventoryService.findAll` |
| `List<Inventory> findBelowReorderPoint(UserId administratorId)` | `ConsultInventoryService.findBelowReorderPoint` |
| `List<StockMovement> findMovements(UserId administratorId, ProductId productId, WarehouseId warehouseId)` | `ConsultInventoryService.findMovements` |
| `Order cancelOrder(UserId administratorId, OrderId orderId)` | `CancelOrderService.execute` |
| `Order findOrder(UserId administratorId, OrderId orderId)` | `ConsultOrderService.execute` |
| `List<Order> findOrders(UserId administratorId)` | `ConsultOrderService.findAll` |
| `Invoice findInvoice(UserId administratorId, OrderId orderId)` | `ConsultInvoiceService.findByOrder` |
| `List<Invoice> findInvoices(UserId administratorId)` | `ConsultInvoiceService.findAll` |
| `List<Shipment> findShipments(UserId administratorId)` | `ConsultShipmentService.findAll` |
| `ReturnRequest approveReturn(UserId administratorId, ReturnId returnId)` | `ApproveReturnService.execute` |
| `ReturnRequest rejectReturn(UserId administratorId, ReturnId returnId)` | `RejectReturnService.execute` |
| `List<ReturnRequest> findReturns(UserId administratorId)` | `ConsultReturnService.findAll` |
| `Refund processRefund(UserId administratorId, RefundId refundId)` | `ProcessRefundService.execute` |
| `Refund rejectRefund(UserId administratorId, RefundId refundId)` | `RejectRefundService.execute` |
| `List<Refund> findRefunds(UserId administratorId)` | `ConsultRefundService.findAll` |

## `SupervisorPort`

| Operación | Servicio |
|---|---|
| `User findUser(UserId supervisorId, UserId userId)` | `ConsultUserService.execute` |
| `List<User> findUsers(UserId supervisorId)` | `ConsultUserService.findAll` |
| `List<Product> findProducts(UserId supervisorId)` | `ConsultProductService.findAll` |
| `Warehouse findWarehouse(UserId supervisorId, WarehouseId warehouseId)` | `ConsultWarehouseService.execute` |
| `List<Warehouse> findWarehouses(UserId supervisorId)` | `ConsultWarehouseService.findAll` |
| `Inventory findInventory(UserId supervisorId, ProductId productId, WarehouseId warehouseId)` | `ConsultInventoryService.execute` |
| `List<Inventory> findInventories(UserId supervisorId)` | `ConsultInventoryService.findAll` |
| `List<Inventory> findBelowReorderPoint(UserId supervisorId)` | `ConsultInventoryService.findBelowReorderPoint` |
| `List<StockMovement> findMovements(UserId supervisorId, ProductId productId, WarehouseId warehouseId)` | `ConsultInventoryService.findMovements` |
| `Order findOrder(UserId supervisorId, OrderId orderId)` | `ConsultOrderService.execute` |
| `List<Order> findOrders(UserId supervisorId)` | `ConsultOrderService.findAll` |
| `Invoice findInvoice(UserId supervisorId, OrderId orderId)` | `ConsultInvoiceService.findByOrder` |
| `List<Invoice> findInvoices(UserId supervisorId)` | `ConsultInvoiceService.findAll` |
| `List<Shipment> findShipments(UserId supervisorId)` | `ConsultShipmentService.findAll` |
| `List<ReturnRequest> findReturns(UserId supervisorId)` | `ConsultReturnService.findAll` |
| `List<Refund> findRefunds(UserId supervisorId)` | `ConsultRefundService.findAll` |
