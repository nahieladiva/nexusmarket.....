# Servicios de inventario — NexusMarket

Paquete: `application.domain.services.inventory`. La vista general está en [Services](../Services.md).

### `CreateInventoryService`

- **Propósito:** registra el stock inicial de un producto en una bodega.
- **Quién lo usa:** `LogisticsOperatorPort.createInventory`, `SellerPort.createInventory`
- **Entradas:** `Inventory execute(User actor, ProductId productId, WarehouseId warehouseId, Quantity onHand, Quantity reorderThreshold, WarehouseLocation location)`
- **Reglas de negocio:** aplica `ValidateInventoryAccessService`. Hay un inventario por producto y bodega, la bodega debe estar activa y los productos `DIGITAL` no tienen inventario. Registra un movimiento `INITIAL`.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `BusinessRuleException`, `DuplicateResourceException`, `ResourceStateConflictException` si el estado del recurso no lo permite.
- **Estados que cambia:** ninguno. Crea el `Inventory`.

### `RestockInventoryService`

- **Propósito:** registra una entrada de mercancía.
- **Quién lo usa:** `LogisticsOperatorPort.restock`, `SellerPort.restock`
- **Entradas:** `Inventory execute(User actor, ProductId productId, WarehouseId warehouseId, Quantity amount, String reason)`
- **Reglas de negocio:** aplica `ValidateInventoryAccessService`. La cantidad es mayor que cero. Registra un movimiento `RESTOCK`.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `BusinessRuleException`.
- **Estados que cambia:** ninguno. Aumenta `onHand`.

### `AdjustStockService`

- **Propósito:** ajusta manualmente el stock por conteo, merma o daño.
- **Quién lo usa:** `LogisticsOperatorPort.adjustStock`, `SellerPort.adjustStock`
- **Entradas:** `Inventory execute(User actor, ProductId productId, WarehouseId warehouseId, int delta, String reason)`
- **Reglas de negocio:** aplica `ValidateInventoryAccessService`. `delta` no es cero y todo ajuste tiene motivo. Registra un movimiento `ADJUSTMENT`.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `BusinessRuleException`, `InsufficientStockException`.
- **Estados que cambia:** ninguno. Cambia `onHand`.

### `ChangeReorderThresholdService`

- **Propósito:** cambia el punto de reorden.
- **Quién lo usa:** `LogisticsOperatorPort.changeReorderThreshold`, `SellerPort.changeReorderThreshold`
- **Entradas:** `Inventory execute(User actor, ProductId productId, WarehouseId warehouseId, Quantity newThreshold)`
- **Reglas de negocio:** aplica `ValidateInventoryAccessService`.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`.
- **Estados que cambia:** ninguno. Cambia `reorderThreshold`.

### `ConsultInventoryService`

- **Propósito:** consulta el stock y su historial.
- **Quién lo usa:** `AdministratorPort.findBelowReorderPoint`, `AdministratorPort.findInventories`, `AdministratorPort.findInventory`, `AdministratorPort.findMovements`, `LogisticsOperatorPort.findBelowReorderPoint`, `LogisticsOperatorPort.findInventories`, `LogisticsOperatorPort.findInventoriesByWarehouse`, `LogisticsOperatorPort.findInventory`, `LogisticsOperatorPort.findMovements`, `SellerPort.findInventories`, `SellerPort.findInventory`, `SellerPort.findMovements`, `SupervisorPort.findBelowReorderPoint`, `SupervisorPort.findInventories`, `SupervisorPort.findInventory`, `SupervisorPort.findMovements`
- **Entradas:** `Inventory execute(User actor, ProductId productId, WarehouseId warehouseId)`; `List<Inventory> findBySeller(User seller)`; `List<Inventory> findByWarehouse(User actor, WarehouseId warehouseId)`; `List<Inventory> findAll(User actor)`; `List<Inventory> findBelowReorderPoint(User actor)`; `List<StockMovement> findMovements(User actor, ProductId productId, WarehouseId warehouseId)`
- **Reglas de negocio:** el personal interno ve todo el stock. El vendedor ve el de sus productos y, por producto y bodega, solo en sus propias bodegas.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`.
- **Estados que cambia:** ninguno.

### `ValidateInventoryAccessService`

- **Propósito:** aplica las reglas de acceso al inventario (RG-03).
- **Quién lo usa:** `AdjustStockService`, `ChangeReorderThresholdService`, `ConsultInventoryService`, `CreateInventoryService`, `RestockInventoryService`
- **Entradas:** `void requireManage(User actor, ProductId productId, WarehouseId warehouseId)`; `void requireRead(User actor, ProductId productId, WarehouseId warehouseId)`
- **Reglas de negocio:** `requireManage` admite al `LOGISTICS_OPERATOR` en cualquier bodega y al `SELLER` con sus productos en sus bodegas. El `ADMINISTRATOR` y el `SUPERVISOR` no modifican stock. `requireRead` los admite también a ellos.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`.
- **Estados que cambia:** ninguno.

### `InventoryDomainService`

- **Propósito:** reserva y repone stock.
- **Quién lo usa:** `OrderFulfillmentDomainService`, `ReceiveReturnService`, `RestockInventoryService`
- **Entradas:** `Inventory reserve(ProductId productId, WarehouseId warehouseId, Quantity quantity)`; `Inventory restock(ProductId productId, WarehouseId warehouseId, Quantity quantity)`
- **Reglas de negocio:** `reserve` valida que haya stock antes de descontarlo.
- **Excepciones:** `InsufficientStockException`, `ResourceNotFoundException`.
- **Estados que cambia:** ninguno. Cambia `onHand`.

### `WarehouseAllocationService`

- **Propósito:** decide desde qué bodega se despacha cada ítem.
- **Quién lo usa:** `OrderFulfillmentDomainService`, `PlaceOrderService`
- **Entradas:** `Map<ProductId, WarehouseId> allocate(List<OrderItem> items)`
- **Reglas de negocio:** ignora los ítems que no requieren envío. Solo considera bodegas activas y, entre las que tienen stock suficiente, elige la que tiene más unidades.
- **Excepciones:** `InsufficientStockException`.
- **Estados que cambia:** ninguno.

### `RecordStockMovementService`

- **Propósito:** aplica lo que sigue a todo cambio de stock.
- **Quién lo usa:** `AdjustStockService`, `CancelOrderService`, `CreateInventoryService`, `PayOrderService`, `ReceiveReturnService`, `RestockInventoryService`
- **Entradas:** `void execute(Inventory inventory, StockMovementType type, int delta, String reason, UserId performedBy, String reference)`
- **Reglas de negocio:** guarda el `StockMovement` y publica `InventoryUpdatedEvent`. Si el stock baja del punto de reorden, publica `LowStockEvent` y notifica al vendedor.
- **Excepciones:** `IllegalArgumentException` si el signo no coincide con el tipo.
- **Estados que cambia:** ninguno.
