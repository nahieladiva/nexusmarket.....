# API REST — NexusMarket

La API está en `application.adapters.in.rest`. Hay un controlador por puerto de entrada, y cada uno solo
usa su puerto. Las rutas de "lo propio" no llevan el id del usuario: sale del JWT.

La documentación interactiva está en `/swagger-ui.html` y la especificación en `/v3/api-docs`. Hay un
grupo por puerto, por ejemplo `/v3/api-docs/buyer`.

Las rutas de este documento se generan con `tools/VerifySdd.java` y la opción `--print-rest-api`, y el
verificador las compara con los controladores.

## Seguridad

| Prefijo | Quién entra |
|---|---|
| `/api/v1/public` | Cualquiera, sin token |
| `/api/v1/account` | Cualquier usuario autenticado |
| `/api/v1/buyer` | `BUYER` |
| `/api/v1/seller` | `SELLER` |
| `/api/v1/logistics` | `LOGISTICS_OPERATOR` |
| `/api/v1/admin` | `ADMINISTRATOR` |
| `/api/v1/supervisor` | `SUPERVISOR` |
| `/swagger-ui.html`, `/v3/api-docs` | Cualquiera, sin token |
| Cualquier otra ruta | Nadie: 401 sin token y 403 con token |

## Errores

Todos los errores tienen el mismo formato, con `ErrorResponse`:

```json
{"timestamp": "2026-11-05T10:00:00", "status": 409, "error": "Conflict",
 "message": "Refund status cannot change from PROCESSED to PROCESSED", "path": "/api/v1/admin/refunds/.../process"}
```

| Código | Cuándo | Origen |
|---|---|---|
| 400 | Falta un dato, el JSON está mal formado o un id no es válido | Validación de los DTO, `IllegalArgumentException` |
| 401 | Falta el token, es inválido o está revocado, o las credenciales son incorrectas | `SecurityErrorHandler`, `InvalidTokenException`, `InvalidCredentialsException` |
| 403 | El rol no puede entrar, o el recurso es de otro usuario | `SecurityErrorHandler`, `ForbiddenActionException` |
| 404 | El recurso o la ruta no existen | `ResourceNotFoundException` |
| 405 | El método HTTP no aplica a esa ruta | Spring |
| 409 | El recurso está duplicado, o su estado o su stock no permiten la operación | `DuplicateResourceException`, `ResourceStateConflictException`, `OrderStateTransitionException`, `InsufficientStockException`, restricciones únicas de la base de datos |
| 415 | Falta el `Content-Type` del cuerpo | Spring |
| 422 | La operación rompe una regla de negocio | `BusinessRuleException` |
| 500 | Error inesperado; no muestra detalles internos | Cualquier otra excepción |

`GlobalExceptionHandler` hace la traducción. Spring elige el manejador más específico, por eso
`ResourceStateConflictException` responde 409 aunque herede de `BusinessRuleException`, que responde 422.

## `AccountController`

**Prefijo:** `/api/v1/account`. **Rol:** Cualquier usuario autenticado. **Puerto:** `AccountPort`.

| Método | Ruta | Éxito | Operación |
|---|---|---|---|
| `POST` | `/api/v1/account/addresses` | 201 | `AccountPort.addAddress` |
| `POST` | `/api/v1/account/addresses/remove` | 200 | `AccountPort.removeAddress` |
| `POST` | `/api/v1/account/logout` | 200 | `AccountPort.logout` |
| `PUT` | `/api/v1/account/password` | 200 | `AccountPort.changePassword` |
| `GET` | `/api/v1/account/profile` | 200 | `AccountPort.findProfile` |
| `PATCH` | `/api/v1/account/profile` | 200 | `AccountPort.updateProfile` |

## `AdministratorController`

**Prefijo:** `/api/v1/admin`. **Rol:** `ADMINISTRATOR`. **Puerto:** `AdministratorPort`.

| Método | Ruta | Éxito | Operación |
|---|---|---|---|
| `GET` | `/api/v1/admin/inventory` | 200 | `AdministratorPort.findInventories` |
| `GET` | `/api/v1/admin/inventory/low-stock` | 200 | `AdministratorPort.findBelowReorderPoint` |
| `GET` | `/api/v1/admin/inventory/{productId}/{warehouseId}` | 200 | `AdministratorPort.findInventory` |
| `GET` | `/api/v1/admin/inventory/{productId}/{warehouseId}/movements` | 200 | `AdministratorPort.findMovements` |
| `GET` | `/api/v1/admin/invoices` | 200 | `AdministratorPort.findInvoices` |
| `GET` | `/api/v1/admin/orders` | 200 | `AdministratorPort.findOrders` |
| `GET` | `/api/v1/admin/orders/{orderId}` | 200 | `AdministratorPort.findOrder` |
| `POST` | `/api/v1/admin/orders/{orderId}/cancel` | 200 | `AdministratorPort.cancelOrder` |
| `GET` | `/api/v1/admin/orders/{orderId}/invoice` | 200 | `AdministratorPort.findInvoice` |
| `GET` | `/api/v1/admin/products` | 200 | `AdministratorPort.findProducts` |
| `POST` | `/api/v1/admin/products/{productId}/block` | 200 | `AdministratorPort.blockProduct` |
| `POST` | `/api/v1/admin/products/{productId}/discontinue` | 200 | `AdministratorPort.discontinueProduct` |
| `POST` | `/api/v1/admin/products/{productId}/unblock` | 200 | `AdministratorPort.unblockProduct` |
| `GET` | `/api/v1/admin/refunds` | 200 | `AdministratorPort.findRefunds` |
| `POST` | `/api/v1/admin/refunds/{refundId}/process` | 200 | `AdministratorPort.processRefund` |
| `POST` | `/api/v1/admin/refunds/{refundId}/reject` | 200 | `AdministratorPort.rejectRefund` |
| `GET` | `/api/v1/admin/returns` | 200 | `AdministratorPort.findReturns` |
| `POST` | `/api/v1/admin/returns/{returnId}/approve` | 200 | `AdministratorPort.approveReturn` |
| `POST` | `/api/v1/admin/returns/{returnId}/reject` | 200 | `AdministratorPort.rejectReturn` |
| `POST` | `/api/v1/admin/sellers` | 201 | `AdministratorPort.registerSeller` |
| `POST` | `/api/v1/admin/sellers/{sellerId}/approve` | 200 | `AdministratorPort.approveSeller` |
| `POST` | `/api/v1/admin/sellers/{sellerId}/suspend` | 200 | `AdministratorPort.suspendSeller` |
| `GET` | `/api/v1/admin/shipments` | 200 | `AdministratorPort.findShipments` |
| `POST` | `/api/v1/admin/staff` | 201 | `AdministratorPort.registerStaffUser` |
| `GET` | `/api/v1/admin/users` | 200 | `AdministratorPort.findUsers` |
| `GET` | `/api/v1/admin/users/{userId}` | 200 | `AdministratorPort.findUser` |
| `GET` | `/api/v1/admin/warehouses` | 200 | `AdministratorPort.findWarehouses` |
| `POST` | `/api/v1/admin/warehouses` | 201 | `AdministratorPort.createWarehouse` |
| `GET` | `/api/v1/admin/warehouses/{warehouseId}` | 200 | `AdministratorPort.findWarehouse` |
| `PUT` | `/api/v1/admin/warehouses/{warehouseId}` | 200 | `AdministratorPort.updateWarehouse` |
| `POST` | `/api/v1/admin/warehouses/{warehouseId}/activate` | 200 | `AdministratorPort.activateWarehouse` |
| `POST` | `/api/v1/admin/warehouses/{warehouseId}/deactivate` | 200 | `AdministratorPort.deactivateWarehouse` |

## `BuyerController`

**Prefijo:** `/api/v1/buyer`. **Rol:** `BUYER`. **Puerto:** `BuyerPort`.

| Método | Ruta | Éxito | Operación |
|---|---|---|---|
| `DELETE` | `/api/v1/buyer/cart` | 200 | `BuyerPort.clearCart` |
| `GET` | `/api/v1/buyer/cart` | 200 | `BuyerPort.findCart` |
| `POST` | `/api/v1/buyer/cart/items` | 200 | `BuyerPort.addCartItem` |
| `DELETE` | `/api/v1/buyer/cart/items/{productId}` | 200 | `BuyerPort.removeCartItem` |
| `PATCH` | `/api/v1/buyer/cart/items/{productId}` | 200 | `BuyerPort.changeCartItemQuantity` |
| `GET` | `/api/v1/buyer/orders` | 200 | `BuyerPort.findOrders` |
| `POST` | `/api/v1/buyer/orders` | 201 | `BuyerPort.placeOrder` |
| `GET` | `/api/v1/buyer/orders/{orderId}` | 200 | `BuyerPort.findOrder` |
| `POST` | `/api/v1/buyer/orders/{orderId}/cancel` | 200 | `BuyerPort.cancelOrder` |
| `GET` | `/api/v1/buyer/orders/{orderId}/invoice` | 200 | `BuyerPort.findInvoice` |
| `POST` | `/api/v1/buyer/orders/{orderId}/pay` | 200 | `BuyerPort.payOrder` |
| `GET` | `/api/v1/buyer/orders/{orderId}/refunds` | 200 | `BuyerPort.findRefunds` |
| `GET` | `/api/v1/buyer/orders/{orderId}/returns` | 200 | `BuyerPort.findReturns` |
| `POST` | `/api/v1/buyer/orders/{orderId}/returns` | 201 | `BuyerPort.requestReturn` |
| `GET` | `/api/v1/buyer/orders/{orderId}/shipments` | 200 | `BuyerPort.findShipments` |

## `LogisticsController`

**Prefijo:** `/api/v1/logistics`. **Rol:** `LOGISTICS_OPERATOR`. **Puerto:** `LogisticsOperatorPort`.

| Método | Ruta | Éxito | Operación |
|---|---|---|---|
| `GET` | `/api/v1/logistics/inventory` | 200 | `LogisticsOperatorPort.findInventories` |
| `POST` | `/api/v1/logistics/inventory` | 201 | `LogisticsOperatorPort.createInventory` |
| `POST` | `/api/v1/logistics/inventory/adjust` | 200 | `LogisticsOperatorPort.adjustStock` |
| `GET` | `/api/v1/logistics/inventory/low-stock` | 200 | `LogisticsOperatorPort.findBelowReorderPoint` |
| `POST` | `/api/v1/logistics/inventory/restock` | 200 | `LogisticsOperatorPort.restock` |
| `PUT` | `/api/v1/logistics/inventory/threshold` | 200 | `LogisticsOperatorPort.changeReorderThreshold` |
| `GET` | `/api/v1/logistics/inventory/{productId}/{warehouseId}` | 200 | `LogisticsOperatorPort.findInventory` |
| `GET` | `/api/v1/logistics/inventory/{productId}/{warehouseId}/movements` | 200 | `LogisticsOperatorPort.findMovements` |
| `GET` | `/api/v1/logistics/orders` | 200 | `LogisticsOperatorPort.findOrders` |
| `GET` | `/api/v1/logistics/orders/{orderId}` | 200 | `LogisticsOperatorPort.findOrder` |
| `GET` | `/api/v1/logistics/orders/{orderId}/shipments` | 200 | `LogisticsOperatorPort.findShipmentsByOrder` |
| `GET` | `/api/v1/logistics/products` | 200 | `LogisticsOperatorPort.findProducts` |
| `GET` | `/api/v1/logistics/returns` | 200 | `LogisticsOperatorPort.findReturns` |
| `POST` | `/api/v1/logistics/returns/{returnId}/receive` | 200 | `LogisticsOperatorPort.receiveReturn` |
| `GET` | `/api/v1/logistics/shipments` | 200 | `LogisticsOperatorPort.findShipments` |
| `POST` | `/api/v1/logistics/shipments` | 201 | `LogisticsOperatorPort.createShipment` |
| `POST` | `/api/v1/logistics/shipments/{shipmentId}/deliver` | 200 | `LogisticsOperatorPort.confirmDelivery` |
| `POST` | `/api/v1/logistics/shipments/{shipmentId}/dispatch` | 200 | `LogisticsOperatorPort.dispatchShipment` |
| `POST` | `/api/v1/logistics/shipments/{shipmentId}/pack` | 200 | `LogisticsOperatorPort.packShipment` |
| `GET` | `/api/v1/logistics/warehouses` | 200 | `LogisticsOperatorPort.findWarehouses` |
| `GET` | `/api/v1/logistics/warehouses/{warehouseId}` | 200 | `LogisticsOperatorPort.findWarehouse` |
| `GET` | `/api/v1/logistics/warehouses/{warehouseId}/inventory` | 200 | `LogisticsOperatorPort.findInventoriesByWarehouse` |

## `PublicController`

**Prefijo:** `/api/v1/public`. **Rol:** Anónimo. **Puerto:** `PublicAccessPort`.

| Método | Ruta | Éxito | Operación |
|---|---|---|---|
| `POST` | `/api/v1/public/buyers` | 201 | `PublicAccessPort.registerBuyer` |
| `POST` | `/api/v1/public/login` | 200 | `PublicAccessPort.login` |
| `GET` | `/api/v1/public/products` | 200 | `PublicAccessPort.searchProducts` |
| `GET` | `/api/v1/public/products/{productId}` | 200 | `PublicAccessPort.findProduct` |

## `SellerController`

**Prefijo:** `/api/v1/seller`. **Rol:** `SELLER`. **Puerto:** `SellerPort`.

| Método | Ruta | Éxito | Operación |
|---|---|---|---|
| `GET` | `/api/v1/seller/inventory` | 200 | `SellerPort.findInventories` |
| `POST` | `/api/v1/seller/inventory` | 201 | `SellerPort.createInventory` |
| `POST` | `/api/v1/seller/inventory/adjust` | 200 | `SellerPort.adjustStock` |
| `POST` | `/api/v1/seller/inventory/restock` | 200 | `SellerPort.restock` |
| `PUT` | `/api/v1/seller/inventory/threshold` | 200 | `SellerPort.changeReorderThreshold` |
| `GET` | `/api/v1/seller/inventory/{productId}/{warehouseId}` | 200 | `SellerPort.findInventory` |
| `GET` | `/api/v1/seller/inventory/{productId}/{warehouseId}/movements` | 200 | `SellerPort.findMovements` |
| `GET` | `/api/v1/seller/orders` | 200 | `SellerPort.findOrders` |
| `GET` | `/api/v1/seller/orders/{orderId}` | 200 | `SellerPort.findOrder` |
| `GET` | `/api/v1/seller/products` | 200 | `SellerPort.findProducts` |
| `POST` | `/api/v1/seller/products` | 201 | `SellerPort.registerProduct` |
| `PATCH` | `/api/v1/seller/products/{productId}` | 200 | `SellerPort.updateProduct` |
| `POST` | `/api/v1/seller/products/{productId}/discontinue` | 200 | `SellerPort.discontinueProduct` |
| `PUT` | `/api/v1/seller/products/{productId}/price` | 200 | `SellerPort.changeProductPrice` |
| `POST` | `/api/v1/seller/products/{productId}/publish` | 200 | `SellerPort.publishProduct` |
| `POST` | `/api/v1/seller/products/{productId}/suspend` | 200 | `SellerPort.suspendProduct` |
| `GET` | `/api/v1/seller/warehouses` | 200 | `SellerPort.findWarehouses` |
| `POST` | `/api/v1/seller/warehouses` | 201 | `SellerPort.createWarehouse` |
| `GET` | `/api/v1/seller/warehouses/{warehouseId}` | 200 | `SellerPort.findWarehouse` |
| `PUT` | `/api/v1/seller/warehouses/{warehouseId}` | 200 | `SellerPort.updateWarehouse` |
| `POST` | `/api/v1/seller/warehouses/{warehouseId}/activate` | 200 | `SellerPort.activateWarehouse` |
| `POST` | `/api/v1/seller/warehouses/{warehouseId}/deactivate` | 200 | `SellerPort.deactivateWarehouse` |

## `SupervisorController`

**Prefijo:** `/api/v1/supervisor`. **Rol:** `SUPERVISOR`. **Puerto:** `SupervisorPort`.

| Método | Ruta | Éxito | Operación |
|---|---|---|---|
| `GET` | `/api/v1/supervisor/inventory` | 200 | `SupervisorPort.findInventories` |
| `GET` | `/api/v1/supervisor/inventory/low-stock` | 200 | `SupervisorPort.findBelowReorderPoint` |
| `GET` | `/api/v1/supervisor/inventory/{productId}/{warehouseId}` | 200 | `SupervisorPort.findInventory` |
| `GET` | `/api/v1/supervisor/inventory/{productId}/{warehouseId}/movements` | 200 | `SupervisorPort.findMovements` |
| `GET` | `/api/v1/supervisor/invoices` | 200 | `SupervisorPort.findInvoices` |
| `GET` | `/api/v1/supervisor/orders` | 200 | `SupervisorPort.findOrders` |
| `GET` | `/api/v1/supervisor/orders/{orderId}` | 200 | `SupervisorPort.findOrder` |
| `GET` | `/api/v1/supervisor/orders/{orderId}/invoice` | 200 | `SupervisorPort.findInvoice` |
| `GET` | `/api/v1/supervisor/products` | 200 | `SupervisorPort.findProducts` |
| `GET` | `/api/v1/supervisor/refunds` | 200 | `SupervisorPort.findRefunds` |
| `GET` | `/api/v1/supervisor/returns` | 200 | `SupervisorPort.findReturns` |
| `GET` | `/api/v1/supervisor/shipments` | 200 | `SupervisorPort.findShipments` |
| `GET` | `/api/v1/supervisor/users` | 200 | `SupervisorPort.findUsers` |
| `GET` | `/api/v1/supervisor/users/{userId}` | 200 | `SupervisorPort.findUser` |
| `GET` | `/api/v1/supervisor/warehouses` | 200 | `SupervisorPort.findWarehouses` |
| `GET` | `/api/v1/supervisor/warehouses/{warehouseId}` | 200 | `SupervisorPort.findWarehouse` |
