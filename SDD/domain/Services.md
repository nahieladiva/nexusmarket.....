# Servicios de dominio — NexusMarket

Este documento es la matriz de trazabilidad entre el SDD y el código. Muestra cada servicio de
`application.domain.services`, el rol que lo usa, el puerto que lo expone y el documento con su detalle.
Las tablas se generaron a partir del código.

Cada servicio es una clase con la anotación `DomainService`, que pertenece al dominio y no depende de
Spring. `ApplicationConfig` la escanea y registra los servicios como beans. Los casos de uso de
`application.adapters.useCases` implementan los puertos de entrada y delegan en estos servicios.

## 1. Servicios expuestos por los puertos

| Servicio | Rol(es) | Puerto y método | Detalle |
|---|---|---|---|
| `LoginService` | Anónimo | `PublicAccessPort.login` | [authorization](services/authorization.md) |
| `LogoutService` | Cualquier usuario autenticado | `AccountPort.logout` | [authorization](services/authorization.md) |
| `ResolveActorService` | Cualquier usuario autenticado | Todos los casos de uso con actor; `AccountPort.findProfile` | [authorization](services/authorization.md) |
| `ValidateTokenService` | Cualquier usuario autenticado | `AccountPort.validateToken` | [authorization](services/authorization.md) |
| `AddItemToCartService` | `BUYER` | `BuyerPort.addCartItem` | [cart](services/cart.md) |
| `ChangeCartItemQuantityService` | `BUYER` | `BuyerPort.changeCartItemQuantity` | [cart](services/cart.md) |
| `ClearCartService` | `BUYER` | `BuyerPort.clearCart` | [cart](services/cart.md) |
| `ConsultCartService` | `BUYER` | `BuyerPort.findCart` | [cart](services/cart.md) |
| `RemoveItemFromCartService` | `BUYER` | `BuyerPort.removeCartItem` | [cart](services/cart.md) |
| `AdjustStockService` | `LOGISTICS_OPERATOR`, `SELLER` | `LogisticsOperatorPort.adjustStock`<br>`SellerPort.adjustStock` | [inventory](services/inventory.md) |
| `ChangeReorderThresholdService` | `LOGISTICS_OPERATOR`, `SELLER` | `LogisticsOperatorPort.changeReorderThreshold`<br>`SellerPort.changeReorderThreshold` | [inventory](services/inventory.md) |
| `ConsultInventoryService` | `ADMINISTRATOR`, `LOGISTICS_OPERATOR`, `SELLER`, `SUPERVISOR` | `AdministratorPort.findBelowReorderPoint`<br>`AdministratorPort.findInventories`<br>`AdministratorPort.findInventory`<br>`AdministratorPort.findMovements`<br>`LogisticsOperatorPort.findBelowReorderPoint`<br>`LogisticsOperatorPort.findInventories`<br>`LogisticsOperatorPort.findInventoriesByWarehouse`<br>`LogisticsOperatorPort.findInventory`<br>`LogisticsOperatorPort.findMovements`<br>`SellerPort.findInventories`<br>`SellerPort.findInventory`<br>`SellerPort.findMovements`<br>`SupervisorPort.findBelowReorderPoint`<br>`SupervisorPort.findInventories`<br>`SupervisorPort.findInventory`<br>`SupervisorPort.findMovements` | [inventory](services/inventory.md) |
| `CreateInventoryService` | `LOGISTICS_OPERATOR`, `SELLER` | `LogisticsOperatorPort.createInventory`<br>`SellerPort.createInventory` | [inventory](services/inventory.md) |
| `RestockInventoryService` | `LOGISTICS_OPERATOR`, `SELLER` | `LogisticsOperatorPort.restock`<br>`SellerPort.restock` | [inventory](services/inventory.md) |
| `ConsultInvoiceService` | `ADMINISTRATOR`, `BUYER`, `SUPERVISOR` | `AdministratorPort.findInvoice`<br>`AdministratorPort.findInvoices`<br>`BuyerPort.findInvoice`<br>`SupervisorPort.findInvoice`<br>`SupervisorPort.findInvoices` | [invoice](services/invoice.md) |
| `CancelOrderService` | `ADMINISTRATOR`, `BUYER` | `AdministratorPort.cancelOrder`<br>`BuyerPort.cancelOrder` | [order](services/order.md) |
| `ConsultOrderService` | `ADMINISTRATOR`, `BUYER`, `LOGISTICS_OPERATOR`, `SELLER`, `SUPERVISOR` | `AdministratorPort.findOrder`<br>`AdministratorPort.findOrders`<br>`BuyerPort.findOrder`<br>`BuyerPort.findOrders`<br>`LogisticsOperatorPort.findOrder`<br>`LogisticsOperatorPort.findOrders`<br>`SellerPort.findOrder`<br>`SellerPort.findOrders`<br>`SupervisorPort.findOrder`<br>`SupervisorPort.findOrders` | [order](services/order.md) |
| `PayOrderService` | `BUYER` | `BuyerPort.payOrder` | [order](services/order.md) |
| `PlaceOrderService` | `BUYER` | `BuyerPort.placeOrder` | [order](services/order.md) |
| `BlockProductService` | `ADMINISTRATOR` | `AdministratorPort.blockProduct` | [product](services/product.md) |
| `ChangeProductPriceService` | `SELLER` | `SellerPort.changeProductPrice` | [product](services/product.md) |
| `ConsultProductService` | `ADMINISTRATOR`, `LOGISTICS_OPERATOR`, Anónimo, `SELLER`, `SUPERVISOR` | `AdministratorPort.findProducts`<br>`LogisticsOperatorPort.findProducts`<br>`PublicAccessPort.findProduct`<br>`PublicAccessPort.searchProducts`<br>`SellerPort.findProducts`<br>`SupervisorPort.findProducts` | [product](services/product.md) |
| `DiscontinueProductService` | `ADMINISTRATOR`, `SELLER` | `AdministratorPort.discontinueProduct`<br>`SellerPort.discontinueProduct` | [product](services/product.md) |
| `PublishProductService` | `SELLER` | `SellerPort.publishProduct` | [product](services/product.md) |
| `RegisterProductService` | `SELLER` | `SellerPort.registerProduct` | [product](services/product.md) |
| `SuspendProductService` | `SELLER` | `SellerPort.suspendProduct` | [product](services/product.md) |
| `UnblockProductService` | `ADMINISTRATOR` | `AdministratorPort.unblockProduct` | [product](services/product.md) |
| `UpdateProductService` | `SELLER` | `SellerPort.updateProduct` | [product](services/product.md) |
| `ConsultRefundService` | `ADMINISTRATOR`, `BUYER`, `SUPERVISOR` | `AdministratorPort.findRefunds`<br>`BuyerPort.findRefunds`<br>`SupervisorPort.findRefunds` | [refund](services/refund.md) |
| `ProcessRefundService` | `ADMINISTRATOR` | `AdministratorPort.processRefund` | [refund](services/refund.md) |
| `RejectRefundService` | `ADMINISTRATOR` | `AdministratorPort.rejectRefund` | [refund](services/refund.md) |
| `ApproveReturnService` | `ADMINISTRATOR` | `AdministratorPort.approveReturn` | [returns](services/returns.md) |
| `ConsultReturnService` | `ADMINISTRATOR`, `BUYER`, `LOGISTICS_OPERATOR`, `SUPERVISOR` | `AdministratorPort.findReturns`<br>`BuyerPort.findReturns`<br>`LogisticsOperatorPort.findReturns`<br>`SupervisorPort.findReturns` | [returns](services/returns.md) |
| `ReceiveReturnService` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.receiveReturn` | [returns](services/returns.md) |
| `RejectReturnService` | `ADMINISTRATOR` | `AdministratorPort.rejectReturn` | [returns](services/returns.md) |
| `RequestReturnService` | `BUYER` | `BuyerPort.requestReturn` | [returns](services/returns.md) |
| `ConfirmDeliveryService` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.confirmDelivery` | [shipment](services/shipment.md) |
| `ConsultShipmentService` | `ADMINISTRATOR`, `BUYER`, `LOGISTICS_OPERATOR`, `SUPERVISOR` | `AdministratorPort.findShipments`<br>`BuyerPort.findShipments`<br>`LogisticsOperatorPort.findShipments`<br>`LogisticsOperatorPort.findShipmentsByOrder`<br>`SupervisorPort.findShipments` | [shipment](services/shipment.md) |
| `CreateShipmentService` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.createShipment` | [shipment](services/shipment.md) |
| `DispatchShipmentService` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.dispatchShipment` | [shipment](services/shipment.md) |
| `PackShipmentService` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.packShipment` | [shipment](services/shipment.md) |
| `AddAddressService` | Cualquier usuario autenticado | `AccountPort.addAddress` | [user](services/user.md) |
| `ApproveSellerService` | `ADMINISTRATOR` | `AdministratorPort.approveSeller` | [user](services/user.md) |
| `ChangePasswordService` | Cualquier usuario autenticado | `AccountPort.changePassword` | [user](services/user.md) |
| `ConsultUserService` | `ADMINISTRATOR`, `SUPERVISOR` | `AdministratorPort.findUser`<br>`AdministratorPort.findUsers`<br>`SupervisorPort.findUser`<br>`SupervisorPort.findUsers` | [user](services/user.md) |
| `RegisterBuyerService` | Anónimo | `PublicAccessPort.registerBuyer` | [user](services/user.md) |
| `RegisterSellerService` | `ADMINISTRATOR` | `AdministratorPort.registerSeller` | [user](services/user.md) |
| `RegisterStaffUserService` | `ADMINISTRATOR` | `AdministratorPort.registerStaffUser` | [user](services/user.md) |
| `RemoveAddressService` | Cualquier usuario autenticado | `AccountPort.removeAddress` | [user](services/user.md) |
| `SuspendSellerService` | `ADMINISTRATOR` | `AdministratorPort.suspendSeller` | [user](services/user.md) |
| `UpdateProfileService` | Cualquier usuario autenticado | `AccountPort.updateProfile` | [user](services/user.md) |
| `ActivateWarehouseService` | `ADMINISTRATOR`, `SELLER` | `AdministratorPort.activateWarehouse`<br>`SellerPort.activateWarehouse` | [warehouse](services/warehouse.md) |
| `ConsultWarehouseService` | `ADMINISTRATOR`, `LOGISTICS_OPERATOR`, `SELLER`, `SUPERVISOR` | `AdministratorPort.findWarehouse`<br>`AdministratorPort.findWarehouses`<br>`LogisticsOperatorPort.findWarehouse`<br>`LogisticsOperatorPort.findWarehouses`<br>`SellerPort.findWarehouse`<br>`SellerPort.findWarehouses`<br>`SupervisorPort.findWarehouse`<br>`SupervisorPort.findWarehouses` | [warehouse](services/warehouse.md) |
| `CreateMarketplaceWarehouseService` | `ADMINISTRATOR` | `AdministratorPort.createWarehouse` | [warehouse](services/warehouse.md) |
| `CreateSellerWarehouseService` | `SELLER` | `SellerPort.createWarehouse` | [warehouse](services/warehouse.md) |
| `DeactivateWarehouseService` | `ADMINISTRATOR`, `SELLER` | `AdministratorPort.deactivateWarehouse`<br>`SellerPort.deactivateWarehouse` | [warehouse](services/warehouse.md) |
| `UpdateWarehouseService` | `ADMINISTRATOR`, `SELLER` | `AdministratorPort.updateWarehouse`<br>`SellerPort.updateWarehouse` | [warehouse](services/warehouse.md) |

## 2. Servicios auxiliares

Ningún puerto los expone. Los usan otros servicios de dominio.

| Servicio | Usado por | Detalle |
|---|---|---|
| `InventoryDomainService` | `OrderFulfillmentDomainService`, `ReceiveReturnService`, `RestockInventoryService` | [inventory](services/inventory.md) |
| `RecordStockMovementService` | `AdjustStockService`, `CancelOrderService`, `CreateInventoryService`, `PayOrderService`, `ReceiveReturnService`, `RestockInventoryService` | [inventory](services/inventory.md) |
| `ValidateInventoryAccessService` | `AdjustStockService`, `ChangeReorderThresholdService`, `ConsultInventoryService`, `CreateInventoryService`, `RestockInventoryService` | [inventory](services/inventory.md) |
| `WarehouseAllocationService` | `OrderFulfillmentDomainService`, `PlaceOrderService` | [inventory](services/inventory.md) |
| `IssueInvoiceService` | `PayOrderService` | [invoice](services/invoice.md) |
| `VoidInvoiceService` | `CancelOrderService` | [invoice](services/invoice.md) |
| `NotifyOrderStatusChangeService` | `ApplyShipmentProgressService`, `CancelOrderService`, `PayOrderService` | [order](services/order.md) |
| `OrderFulfillmentDomainService` | `CancelOrderService`, `PayOrderService` | [order](services/order.md) |
| `ValidateProductAvailabilityService` | `AddItemToCartService`, `ConsultProductService`, `PayOrderService`, `PlaceOrderService` | [product](services/product.md) |
| `CreateRefundService` | `CancelOrderService`, `ReceiveReturnService` | [refund](services/refund.md) |
| `ApplyShipmentProgressService` | `ConfirmDeliveryService`, `DispatchShipmentService` | [shipment](services/shipment.md) |
| `PasswordPolicy` | `ChangePasswordService`, `RegisterBuyerService`, `RegisterSellerService`, `RegisterStaffUserService` | [user](services/user.md) |
| `ValidateUserUniquenessService` | `RegisterBuyerService`, `RegisterSellerService`, `RegisterStaffUserService` | [user](services/user.md) |
| `ValidateWarehouseNameService` | `CreateMarketplaceWarehouseService`, `CreateSellerWarehouseService`, `UpdateWarehouseService` | [warehouse](services/warehouse.md) |
