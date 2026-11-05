# Servicios de pedido — NexusMarket

Paquete: `application.domain.services.order`. La vista general está en [Services](../Services.md).

### `PlaceOrderService`

- **Propósito:** convierte el carrito de un comprador en un pedido pendiente de pago.
- **Quién lo usa:** `BuyerPort.placeOrder`
- **Entradas:** `Order execute(User buyer)`
- **Reglas de negocio:** cada producto debe seguir disponible y debe haber stock en una bodega activa. El stock todavía no se reserva. El pedido conserva los precios del carrito. El carrito se vacía solo si el pedido se crea.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `BusinessRuleException` si un producto no está disponible o el carrito está vacío, `InsufficientStockException` si no hay stock.
- **Estados que cambia:** crea el `Order` en `PENDING_PAYMENT`. Publica `OrderCreatedEvent`.

### `PayOrderService`

- **Propósito:** registra el pago de un pedido.
- **Quién lo usa:** `BuyerPort.payOrder`
- **Entradas:** `Order execute(User buyer, OrderId orderId)`
- **Reglas de negocio:** solo paga el comprador dueño. Cada producto debe seguir disponible; si no, el pago se rechaza y el pedido sigue en `PENDING_PAYMENT`. Se asignan bodegas, se reserva el stock, se registran los movimientos `RESERVATION` y se emite la factura.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `BusinessRuleException` si un producto no está disponible o el pedido no está en `PENDING_PAYMENT`, `InsufficientStockException` si no hay stock (se comprueba antes que el estado del pedido).
- **Estados que cambia:** `PENDING_PAYMENT` a `PAID`, o a `DELIVERED` si ningún ítem requiere envío. Crea el `Invoice` en `ISSUED`.

### `CancelOrderService`

- **Propósito:** cancela un pedido.
- **Quién lo usa:** `AdministratorPort.cancelOrder`, `BuyerPort.cancelOrder`
- **Entradas:** `Order execute(User actor, OrderId orderId)`
- **Reglas de negocio:** lo cancela el comprador dueño o un administrador. No se cancela un pedido con envíos. El stock reservado vuelve a su bodega con movimientos `RELEASE`. Si el pedido estaba pagado, se anula la factura y se crea un reembolso.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `BusinessRuleException` si tiene envíos, `OrderStateTransitionException` si el estado no permite cancelarlo.
- **Estados que cambia:** `PENDING_PAYMENT` o `PAID` a `CANCELLED`. `Invoice` de `ISSUED` a `VOIDED`. Crea el `Refund` en `PENDING` si estaba pagado.

### `ConsultOrderService`

- **Propósito:** consulta pedidos según el rol (RG-03).
- **Quién lo usa:** `AdministratorPort.findOrder`, `AdministratorPort.findOrders`, `BuyerPort.findOrder`, `BuyerPort.findOrders`, `LogisticsOperatorPort.findOrder`, `LogisticsOperatorPort.findOrders`, `SellerPort.findOrder`, `SellerPort.findOrders`, `SupervisorPort.findOrder`, `SupervisorPort.findOrders`; también `ConsultRefundService`, `ConsultReturnService`, `ConsultShipmentService`
- **Entradas:** `Order execute(User actor, OrderId orderId)`; `List<Order> findByBuyer(User buyer)`; `List<Order> findBySeller(User seller)`; `List<Order> findAll(User actor)`
- **Reglas de negocio:** el comprador ve sus pedidos, el vendedor los que contienen sus productos y el personal interno todos. `ConsultRefundService`, `ConsultReturnService` y `ConsultShipmentService` lo usan para saber quién puede ver un pedido.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`.
- **Estados que cambia:** ninguno.

### `OrderFulfillmentDomainService`

- **Propósito:** coordina el stock con el ciclo del pedido.
- **Quién lo usa:** `CancelOrderService`, `PayOrderService`
- **Entradas:** `List<Inventory> confirm(Order order, LocalDateTime now)`; `List<Inventory> cancel(Order order)`
- **Reglas de negocio:** al pagar, cada ítem que requiere envío recibe una bodega y se reserva su stock. Al cancelar un pedido pagado, el stock vuelve a la misma bodega. Cancelar un pedido sin pagar no toca el stock. No guarda el pedido: lo hace quien lo llama.
- **Excepciones:** `InsufficientStockException` si no hay stock, `ResourceNotFoundException` si falta el inventario, `BusinessRuleException` si se confirma un pedido que no está en `PENDING_PAYMENT`, `OrderStateTransitionException` si se cancela un pedido que no se puede cancelar.
- **Estados que cambia:** aplica `Order.pay` y `Order.cancel`.

### `NotifyOrderStatusChangeService`

- **Propósito:** informa un cambio de estado del pedido.
- **Quién lo usa:** `ApplyShipmentProgressService`, `CancelOrderService`, `PayOrderService`
- **Entradas:** `void execute(Order order, OrderStatus previous)`
- **Reglas de negocio:** publica `OrderStatusChangedEvent` y notifica al comprador con `NotificationPort`.
- **Excepciones:** ninguna propia.
- **Estados que cambia:** ninguno.
