# Casos de uso, puertos y reglas de negocio — NexusMarket

## 1. Propósito

Este documento lista los puertos de la arquitectura hexagonal de NexusMarket:

- los **7 puertos de entrada**, que son los casos de uso que el sistema ofrece;
- los **15 puertos de salida**, que son lo que el dominio necesita del exterior.

También reúne las reglas de negocio que cada caso de uso aplica.

El dominio (`application.domain`) no depende de Spring. Los casos de uso están en
`application.services`, que implementa los puertos de entrada. Los adaptadores están en
`application.adapters` e implementan los puertos de salida o llaman a los de entrada.

## 2. Puertos de entrada (7)

| # | Puerto | Implementación | Adaptador REST |
|---|---|---|---|
| 1 | `AuthenticationPort` | `AuthenticationApplicationService` | `AuthController` |
| 2 | `UserManagementPort` | `UserApplicationService` | `UserController` |
| 3 | `ProductManagementPort` | `ProductApplicationService` | `ProductController` |
| 4 | `WarehouseManagementPort` | `WarehouseApplicationService` | `WarehouseController` |
| 5 | `InventoryManagementPort` | `InventoryApplicationService` | `InventoryController` |
| 6 | `OrderManagementPort` | `OrderApplicationService` | `OrderController` |
| 7 | `PromotionManagementPort` | `PromotionApplicationService` | `PromotionController` |

### 2.1 AuthenticationPort
- **Registrar comprador / vendedor.** El email debe ser único. La contraseña debe tener al menos 8 caracteres, con letras y números, y se guarda con BCrypt. El vendedor queda pendiente de aprobación.
- **Iniciar sesión.** Valida las credenciales y emite un JWT de 1 hora (HS256). El token queda registrado en la base de tokens.
- **Cerrar sesión.** Revoca el token, que deja de servir aunque no haya expirado.
- **Validar token.** Revisa la firma, la expiración, que el token esté registrado y que no esté revocado.

### 2.2 UserManagementPort
- **Consultar y actualizar perfil.** Se pueden cambiar el nombre y el teléfono. El email y el rol no cambian.
- **Agregar o quitar direcciones.** Máximo 5 por usuario y sin duplicados.
- **Aprobar vendedor (admin).** Solo aplica a vendedores no aprobados y genera una notificación al vendedor.
- **Listar usuarios (admin).**

### 2.3 ProductManagementPort
- **Publicar.** Solo pueden hacerlo vendedores aprobados. El código SKU es único y el precio debe ser mayor que cero.
- **Editar, cambiar precio y activar/desactivar.** Solo el vendedor dueño. El precio no puede cambiar de moneda.
- **Buscar (público).** Solo muestra productos activos.
- **Listar mis productos (vendedor) y catálogo completo (admin).**

### 2.4 WarehouseManagementPort (admin)
- **Crear y editar.** El nombre es único.
- **Activar o desactivar.** Una bodega inactiva no recibe stock nuevo ni despacha pedidos.
- **Consultar y listar.**

### 2.5 InventoryManagementPort (vendedor dueño del producto o admin)
- **Registrar stock inicial.** Un solo registro por producto y bodega, y solo en bodegas activas.
- **Reabastecer.** La cantidad debe ser mayor que cero.
- **Ajustar.** El ajuste es positivo o negativo, requiere un motivo y el stock nunca puede quedar negativo.
- **Cambiar el umbral de reorden.**
- **Consultar.** Disponibilidad, por bodega, todo el inventario, bajo el umbral e historial de movimientos.
- **Historial.** Todo movimiento (INITIAL, RESTOCK, ADJUSTMENT, RESERVATION, RELEASE) queda registrado con su motivo.
- **Stock bajo.** Cuando una salida deja el stock bajo el umbral, se publica `LowStockEvent` y se notifica al vendedor.

### 2.6 OrderManagementPort
- **Crear pedido (comprador).**
  - Solo con productos activos y al menos un ítem.
  - Máximo 10 unidades por producto, sin productos repetidos.
  - Debe existir una bodega activa con stock suficiente.
  - El precio lo calcula el sistema; el cliente nunca lo envía.
- **Precio del pedido.** La promoción vigente y el descuento por volumen (5 % desde 15 unidades) **no se acumulan**: se cobra el menor de los dos totales.
- **Confirmar (admin).** Asigna a cada ítem la bodega activa con más stock y **reserva** el stock.
- **Enviar y entregar (admin).** Transiciones válidas: PENDING → CONFIRMED → SHIPPED → DELIVERED.
- **Cancelar.**
  - Solo desde PENDING o CONFIRMED.
  - Lo hace el comprador dueño o un admin.
  - Si el pedido estaba confirmado, el stock **vuelve** a la misma bodega.
- **Consultar.**
  - El comprador ve sus pedidos.
  - El vendedor ve los pedidos que incluyen sus productos.
  - El admin ve todos.
- Cada cambio de estado publica `OrderStatusChangedEvent` y notifica al comprador.

### 2.7 PromotionManagementPort
- **Crear promoción (vendedor dueño).**
  - El descuento va de 1 % a 70 %.
  - La fecha de fin debe ser posterior a la de inicio.
  - No puede empezar en el pasado.
  - Un producto no puede tener dos promociones con periodos que se crucen.
- **Finalizar promoción.** Solo el vendedor que la creó. Deja de aplicar de inmediato.
- **Consultar (público).** Promociones vigentes, promociones por producto y precio final del día.

## 3. Puertos de salida (15)

| # | Puerto | Adaptador | Tecnología |
|---|---|---|---|
| 1 | `UserRepository` | `UserPersistenceAdapter` | MySQL (`users`, `user_addresses`) |
| 2 | `ProductRepository` | `ProductPersistenceAdapter` | MySQL (`products`) |
| 3 | `WarehouseRepository` | `WarehousePersistenceAdapter` | MySQL (`warehouses`) |
| 4 | `InventoryRepository` | `InventoryPersistenceAdapter` | MySQL (`inventory`) |
| 5 | `OrderRepository` | `OrderPersistenceAdapter` | MySQL (`orders`, `order_items`) |
| 6 | `AuditLogPort` | `AuditLogPersistenceAdapter` | MongoDB (`audit_logs`) |
| 7 | `PromotionRepository` | `PromotionPersistenceAdapter` | MySQL (`promotions`) |
| 8 | `StockMovementLogPort` | `StockMovementLogAdapter` | MongoDB (`stock_movements`) |
| 9 | `TokenProviderPort` | `JjwtTokenProviderAdapter` | jjwt, HMAC-SHA256 |
| 10 | `TokenStorePort` | `TokenStoreAdapter` | MongoDB (`issued_tokens`, índice TTL) |
| 11 | `PasswordEncoderPort` | `BCryptPasswordEncoderAdapter` | BCrypt |
| 12 | `DomainEventPublisherPort` | `SpringDomainEventPublisherAdapter` | Eventos de Spring |
| 13 | `NotificationPort` | `LoggingNotificationAdapter` | Log de la aplicación |
| 14 | `ClockPort` | `SystemClockAdapter` | Reloj del sistema |
| 15 | `IdGeneratorPort` | `UuidGeneratorAdapter` | UUID |

### 3.1 Base de datos de JWT
Cada inicio de sesión guarda en `issued_tokens` los siguientes datos:

- el id del token (`jti`);
- el usuario;
- las fechas de emisión y de expiración;
- si fue revocado.

En cada petición se valida contra esa colección, lo que permite cerrar sesión de verdad. Un índice TTL sobre `expiresAt` hace que MongoDB borre los tokens vencidos automáticamente.

### 3.2 Auditoría
Los casos de uso publican eventos de dominio por `DomainEventPublisherPort`:

- `ProductAddedEvent`
- `InventoryUpdatedEvent`
- `LowStockEvent`
- `OrderCreatedEvent`
- `OrderStatusChangedEvent`

`DomainEventAuditListener` los guarda en `audit_logs` solo cuando la transacción de MySQL se confirma.

## 4. Servicios de dominio

| Servicio | Responsabilidad |
|---|---|
| `PricingDomainService` | Precio con promoción, descuento por volumen y elección del total más favorable |
| `PromotionPolicy` | Validación de promociones y búsqueda de la vigente |
| `PasswordPolicy` | Política de contraseñas |
| `InventoryDomainService` | Reserva y devolución de stock |
| `WarehouseAllocationService` | Elección de la bodega activa que despacha cada ítem |
| `OrderFulfillmentDomainService` | Coordinación del stock con la confirmación y la cancelación de pedidos |

## 5. Pruebas

Las pruebas de `src/test/java/application/services` arman todos los casos de uso con
adaptadores en memoria (`application.support`). Así verifican cada regla de negocio de
este documento sin necesitar bases de datos.
