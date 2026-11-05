# Servicios de bodega — NexusMarket

Paquete: `application.domain.services.warehouse`. La vista general está en [Services](../Services.md).

### `CreateMarketplaceWarehouseService`

- **Propósito:** un administrador crea una bodega del marketplace.
- **Quién lo usa:** `AdministratorPort.createWarehouse`
- **Entradas:** `Warehouse execute(User administrator, String name, Address address, WarehouseLocation location)`
- **Reglas de negocio:** solo un `ADMINISTRATOR`. El nombre es único.
- **Excepciones:** `ForbiddenActionException`, `DuplicateResourceException`.
- **Estados que cambia:** crea el `Warehouse` en `ACTIVE`, de tipo `MARKETPLACE`.

### `CreateSellerWarehouseService`

- **Propósito:** un vendedor crea su propia bodega.
- **Quién lo usa:** `SellerPort.createWarehouse`
- **Entradas:** `Warehouse execute(User seller, String name, Address address, WarehouseLocation location)`
- **Reglas de negocio:** solo un `SELLER`. El nombre es único.
- **Excepciones:** `ForbiddenActionException`, `DuplicateResourceException`.
- **Estados que cambia:** crea el `Warehouse` en `ACTIVE`, de tipo `SELLER`.

### `UpdateWarehouseService`

- **Propósito:** cambia los datos de una bodega.
- **Quién lo usa:** `AdministratorPort.updateWarehouse`, `SellerPort.updateWarehouse`
- **Entradas:** `Warehouse execute(User actor, WarehouseId warehouseId, String name, Address address, WarehouseLocation location)`
- **Reglas de negocio:** aplica `Warehouse.requireManagedBy`: el dueño en una bodega `SELLER` y un administrador en una `MARKETPLACE`. Un valor `null` conserva el dato actual.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `DuplicateResourceException`.
- **Estados que cambia:** ninguno.

### `ActivateWarehouseService`

- **Propósito:** activa una bodega inactiva.
- **Quién lo usa:** `AdministratorPort.activateWarehouse`, `SellerPort.activateWarehouse`
- **Entradas:** `Warehouse execute(User actor, WarehouseId warehouseId)`
- **Reglas de negocio:** aplica `Warehouse.requireManagedBy`.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `BusinessRuleException` si ya está activa.
- **Estados que cambia:** `Warehouse` de `INACTIVE` a `ACTIVE`.

### `DeactivateWarehouseService`

- **Propósito:** desactiva una bodega.
- **Quién lo usa:** `AdministratorPort.deactivateWarehouse`, `SellerPort.deactivateWarehouse`
- **Entradas:** `Warehouse execute(User actor, WarehouseId warehouseId)`
- **Reglas de negocio:** aplica `Warehouse.requireManagedBy`. Una bodega inactiva no recibe stock ni participa en la asignación de pedidos.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `BusinessRuleException` si ya está inactiva.
- **Estados que cambia:** `Warehouse` de `ACTIVE` a `INACTIVE`.

### `ConsultWarehouseService`

- **Propósito:** consulta bodegas.
- **Quién lo usa:** `AdministratorPort.findWarehouse`, `AdministratorPort.findWarehouses`, `LogisticsOperatorPort.findWarehouse`, `LogisticsOperatorPort.findWarehouses`, `SellerPort.findWarehouse`, `SellerPort.findWarehouses`, `SupervisorPort.findWarehouse`, `SupervisorPort.findWarehouses`
- **Entradas:** `Warehouse execute(User actor, WarehouseId warehouseId)`; `List<Warehouse> findBySeller(User seller)`; `List<Warehouse> findAll(User actor)`
- **Reglas de negocio:** el personal interno ve todas. El vendedor ve las suyas.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`.
- **Estados que cambia:** ninguno.

### `ValidateWarehouseNameService`

- **Propósito:** valida que ninguna otra bodega use un nombre.
- **Quién lo usa:** `CreateMarketplaceWarehouseService`, `CreateSellerWarehouseService`, `UpdateWarehouseService`
- **Entradas:** `void execute(String name, WarehouseId ignoredId)`
- **Reglas de negocio:** no distingue mayúsculas de minúsculas. `ignoredId` permite conservar el nombre al actualizar.
- **Excepciones:** `DuplicateResourceException`.
- **Estados que cambia:** ninguno.
