# Servicios de envío — NexusMarket

Paquete: `application.domain.services.shipment`. La vista general está en [Services](../Services.md).

### `CreateShipmentService`

- **Propósito:** crea el envío de un pedido pagado desde una de sus bodegas.
- **Quién lo usa:** `LogisticsOperatorPort.createShipment`
- **Entradas:** `Shipment execute(User operator, OrderId orderId, WarehouseId warehouseId)`
- **Reglas de negocio:** solo un `LOGISTICS_OPERATOR`. Cada bodega del pedido tiene un solo envío. La dirección es `defaultShippingAddress` del comprador.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `DuplicateResourceException` si esa bodega ya tiene envío, `BusinessRuleException` si el pedido no está `PAID`, no sale de esa bodega o su comprador no existe.
- **Estados que cambia:** crea el `Shipment` en `PREPARING`.

### `PackShipmentService`

- **Propósito:** marca un envío como empacado.
- **Quién lo usa:** `LogisticsOperatorPort.packShipment`
- **Entradas:** `Shipment execute(User operator, ShipmentId shipmentId)`
- **Reglas de negocio:** solo un `LOGISTICS_OPERATOR`.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `BusinessRuleException` si la transición no es válida.
- **Estados que cambia:** `Shipment` de `PREPARING` a `PACKED`.

### `DispatchShipmentService`

- **Propósito:** entrega el envío al transportador.
- **Quién lo usa:** `LogisticsOperatorPort.dispatchShipment`
- **Entradas:** `Shipment execute(User operator, ShipmentId shipmentId, String trackingNumber)`
- **Reglas de negocio:** solo un `LOGISTICS_OPERATOR`. El número de guía es obligatorio. Después se aplica el avance del pedido.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `BusinessRuleException` si la transición no es válida, `IllegalArgumentException` si falta el número de guía.
- **Estados que cambia:** `Shipment` de `PACKED` a `IN_TRANSIT`. Puede pasar el `Order` a `SHIPPED`.

### `ConfirmDeliveryService`

- **Propósito:** confirma que el comprador recibió un envío.
- **Quién lo usa:** `LogisticsOperatorPort.confirmDelivery`
- **Entradas:** `Shipment execute(User operator, ShipmentId shipmentId)`
- **Reglas de negocio:** solo un `LOGISTICS_OPERATOR`. Después se aplica el avance del pedido.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `BusinessRuleException` si la transición no es válida.
- **Estados que cambia:** `Shipment` de `IN_TRANSIT` a `DELIVERED`. Puede pasar el `Order` a `DELIVERED`.

### `ApplyShipmentProgressService`

- **Propósito:** hace avanzar un pedido según sus envíos.
- **Quién lo usa:** `ConfirmDeliveryService`, `DispatchShipmentService`
- **Entradas:** `void execute(OrderId orderId)`
- **Reglas de negocio:** aplica `Order.applyShipmentProgress` con todos los envíos del pedido. Solo guarda y notifica si el estado cambió.
- **Excepciones:** `ResourceNotFoundException`, `BusinessRuleException`.
- **Estados que cambia:** `Order` de `PAID` a `SHIPPED` y de `SHIPPED` a `DELIVERED`.

### `ConsultShipmentService`

- **Propósito:** consulta envíos.
- **Quién lo usa:** `AdministratorPort.findShipments`, `BuyerPort.findShipments`, `LogisticsOperatorPort.findShipments`, `LogisticsOperatorPort.findShipmentsByOrder`, `SupervisorPort.findShipments`
- **Entradas:** `List<Shipment> findByOrder(User actor, OrderId orderId)`; `List<Shipment> findAll(User actor)`
- **Reglas de negocio:** quien puede ver un pedido puede ver sus envíos. El personal interno ve todos.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`.
- **Estados que cambia:** ninguno.
