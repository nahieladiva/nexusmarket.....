# Servicios de reembolso — NexusMarket

Paquete: `application.domain.services.refund`. La vista general está en [Services](../Services.md).

### `CreateRefundService`

- **Propósito:** crea un reembolso pendiente.
- **Quién lo usa:** `CancelOrderService`, `ReceiveReturnService`
- **Entradas:** `Refund forReturn(ReturnRequest returnRequest)`; `Refund forCancelledOrder(Order order)`
- **Reglas de negocio:** `forReturn` usa `refundAmount` de una devolución `RECEIVED`. `forCancelledOrder` usa el total de un pedido `CANCELLED` que se había pagado.
- **Excepciones:** `ResourceStateConflictException` si no se cumple esa condición.
- **Estados que cambia:** crea el `Refund` en `PENDING`.

### `ProcessRefundService`

- **Propósito:** un administrador procesa un reembolso.
- **Quién lo usa:** `AdministratorPort.processRefund`
- **Entradas:** `Refund execute(User administrator, RefundId refundId)`
- **Reglas de negocio:** solo un `ADMINISTRATOR`. Se notifica al comprador.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `ResourceStateConflictException` si la transición no es válida.
- **Estados que cambia:** `Refund` de `PENDING` a `PROCESSED`.

### `RejectRefundService`

- **Propósito:** un administrador rechaza un reembolso.
- **Quién lo usa:** `AdministratorPort.rejectRefund`
- **Entradas:** `Refund execute(User administrator, RefundId refundId)`
- **Reglas de negocio:** solo un `ADMINISTRATOR`. Se notifica al comprador.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `ResourceStateConflictException` si la transición no es válida.
- **Estados que cambia:** `Refund` de `PENDING` a `REJECTED`.

### `ConsultRefundService`

- **Propósito:** consulta reembolsos.
- **Quién lo usa:** `AdministratorPort.findRefunds`, `BuyerPort.findRefunds`, `SupervisorPort.findRefunds`
- **Entradas:** `List<Refund> findByOrder(User actor, OrderId orderId)`; `List<Refund> findAll(User actor)`
- **Reglas de negocio:** quien puede ver un pedido puede ver sus reembolsos. El administrador y el supervisor ven todos.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`.
- **Estados que cambia:** ninguno.
