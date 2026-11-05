# Servicios de factura — NexusMarket

Paquete: `application.domain.services.invoice`. La vista general está en [Services](../Services.md).

### `IssueInvoiceService`

- **Propósito:** emite la factura de un pedido pagado.
- **Quién lo usa:** `PayOrderService`
- **Entradas:** `Invoice execute(Order order)`
- **Reglas de negocio:** un pedido tiene una sola factura. El número es `INV-` seguido del id del pedido. El pedido debe haberse pagado y no estar cancelado.
- **Excepciones:** `DuplicateResourceException` si ya existe, `BusinessRuleException` si el pedido no se pagó o está cancelado.
- **Estados que cambia:** crea el `Invoice` en `ISSUED`.

### `VoidInvoiceService`

- **Propósito:** anula la factura de un pedido cancelado.
- **Quién lo usa:** `CancelOrderService`
- **Entradas:** `void execute(Order order)`
- **Reglas de negocio:** un pedido cancelado antes del pago no tiene factura, y entonces no hace nada.
- **Excepciones:** `BusinessRuleException` si la factura ya estaba anulada.
- **Estados que cambia:** `Invoice` de `ISSUED` a `VOIDED`.

### `ConsultInvoiceService`

- **Propósito:** consulta facturas.
- **Quién lo usa:** `AdministratorPort.findInvoice`, `AdministratorPort.findInvoices`, `BuyerPort.findInvoice`, `SupervisorPort.findInvoice`, `SupervisorPort.findInvoices`
- **Entradas:** `Invoice findByOrder(User actor, OrderId orderId)`; `List<Invoice> findAll(User actor)`
- **Reglas de negocio:** el comprador ve sus facturas. El administrador y el supervisor ven todas.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`.
- **Estados que cambia:** ninguno.
