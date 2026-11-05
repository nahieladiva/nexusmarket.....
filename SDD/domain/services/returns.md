# Servicios de devolución — NexusMarket

Paquete: `application.domain.services.returns`. La vista general está en [Services](../Services.md).

### `RequestReturnService`

- **Propósito:** un comprador solicita la devolución de un producto de su pedido entregado.
- **Quién lo usa:** `BuyerPort.requestReturn`
- **Entradas:** `ReturnRequest execute(User buyer, OrderId orderId, ProductId productId, Quantity quantity, String reason)`
- **Reglas de negocio:** solo el comprador dueño. Un pedido tiene una sola devolución abierta (`ReturnStatus.isOpen`). Suma lo ya devuelto de ese producto con `ReturnStatus.countsAsReturned`, de modo que las `REJECTED` no cuentan. Lo ya devuelto más lo solicitado no supera lo comprado. Los productos `DIGITAL` no se devuelven.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `BusinessRuleException`, `ResourceStateConflictException` si el estado del recurso no lo permite.
- **Estados que cambia:** crea el `ReturnRequest` en `REQUESTED`.

### `ApproveReturnService`

- **Propósito:** un administrador aprueba una devolución.
- **Quién lo usa:** `AdministratorPort.approveReturn`
- **Entradas:** `ReturnRequest execute(User administrator, ReturnId returnId)`
- **Reglas de negocio:** solo un `ADMINISTRATOR`.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `ResourceStateConflictException` si la transición no es válida.
- **Estados que cambia:** `ReturnRequest` de `REQUESTED` a `APPROVED`.

### `RejectReturnService`

- **Propósito:** un administrador rechaza una devolución.
- **Quién lo usa:** `AdministratorPort.rejectReturn`
- **Entradas:** `ReturnRequest execute(User administrator, ReturnId returnId)`
- **Reglas de negocio:** solo un `ADMINISTRATOR`.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `ResourceStateConflictException` si la transición no es válida.
- **Estados que cambia:** `ReturnRequest` de `REQUESTED` a `REJECTED`.

### `ReceiveReturnService`

- **Propósito:** un operador logístico recibe una devolución aprobada.
- **Quién lo usa:** `LogisticsOperatorPort.receiveReturn`
- **Entradas:** `ReturnRequest execute(User operator, ReturnId returnId)`
- **Reglas de negocio:** solo un `LOGISTICS_OPERATOR`. Las unidades vuelven a la bodega que las despachó (`Order.warehouseOf`) con un movimiento `RETURN`. Se crea el reembolso.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `ResourceStateConflictException` si la transición no es válida.
- **Estados que cambia:** `ReturnRequest` de `APPROVED` a `RECEIVED`. Crea el `Refund` en `PENDING`.

### `ConsultReturnService`

- **Propósito:** consulta devoluciones.
- **Quién lo usa:** `AdministratorPort.findReturns`, `BuyerPort.findReturns`, `LogisticsOperatorPort.findReturns`, `SupervisorPort.findReturns`
- **Entradas:** `List<ReturnRequest> findByOrder(User actor, OrderId orderId)`; `List<ReturnRequest> findAll(User actor)`
- **Reglas de negocio:** quien puede ver un pedido puede ver sus devoluciones. El personal interno ve todas.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`.
- **Estados que cambia:** ninguno.
