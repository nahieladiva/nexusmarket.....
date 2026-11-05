# Servicios de producto — NexusMarket

Paquete: `application.domain.services.product`. La vista general está en [Services](../Services.md).

### `RegisterProductService`

- **Propósito:** un vendedor registra un producto.
- **Quién lo usa:** `SellerPort.registerProduct`
- **Entradas:** `Product execute(User seller, ProductCode code, ProductType type, String name, String description, Money price)`
- **Reglas de negocio:** solo un `SELLER`, aunque aún no esté aprobado. El código es único. El producto queda fuera del catálogo hasta publicarlo.
- **Excepciones:** `ForbiddenActionException`, `DuplicateResourceException`, `IllegalArgumentException` si el precio no es mayor que cero.
- **Estados que cambia:** crea el `Product` en `DRAFT`. Publica `ProductAddedEvent`.

### `UpdateProductService`

- **Propósito:** el vendedor dueño cambia el nombre o la descripción.
- **Quién lo usa:** `SellerPort.updateProduct`
- **Entradas:** `Product execute(User seller, ProductId productId, String name, String description)`
- **Reglas de negocio:** un valor `null` conserva el dato actual. Un producto `DISCONTINUED` no se edita.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `ResourceStateConflictException`.
- **Estados que cambia:** ninguno.

### `ChangeProductPriceService`

- **Propósito:** el vendedor dueño cambia el precio.
- **Quién lo usa:** `SellerPort.changeProductPrice`
- **Entradas:** `Product execute(User seller, ProductId productId, Money newPrice)`
- **Reglas de negocio:** el precio es mayor que cero y conserva su moneda. Los carritos y pedidos existentes mantienen su precio.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `ResourceStateConflictException`, `IllegalArgumentException`.
- **Estados que cambia:** ninguno.

### `PublishProductService`

- **Propósito:** el vendedor dueño publica un producto.
- **Quién lo usa:** `SellerPort.publishProduct`
- **Entradas:** `Product execute(User actor, ProductId productId)`
- **Reglas de negocio:** el vendedor debe estar `APPROVED` (`Seller.requireApprovedToPublish`). Un producto `BLOCKED` no se publica.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `ResourceStateConflictException`.
- **Estados que cambia:** `Product` de `DRAFT` o `SUSPENDED` a `PUBLISHED`.

### `SuspendProductService`

- **Propósito:** el vendedor dueño retira temporalmente un producto del catálogo.
- **Quién lo usa:** `SellerPort.suspendProduct`
- **Entradas:** `Product execute(User seller, ProductId productId)`
- **Reglas de negocio:** solo el vendedor. Un producto `BLOCKED` no se suspende, para que el vendedor no pueda levantar el bloqueo.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `ResourceStateConflictException`.
- **Estados que cambia:** `Product` de `PUBLISHED` a `SUSPENDED`.

### `DiscontinueProductService`

- **Propósito:** retira definitivamente un producto de la venta.
- **Quién lo usa:** `AdministratorPort.discontinueProduct`, `SellerPort.discontinueProduct`
- **Entradas:** `Product execute(User actor, ProductId productId)`
- **Reglas de negocio:** lo hace el vendedor dueño o un administrador. Un producto `BLOCKED` primero debe desbloquearse.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `ResourceStateConflictException`.
- **Estados que cambia:** `Product` a `DISCONTINUED`.

### `BlockProductService`

- **Propósito:** un administrador bloquea un producto.
- **Quién lo usa:** `AdministratorPort.blockProduct`
- **Entradas:** `Product execute(User administrator, ProductId productId)`
- **Reglas de negocio:** solo un `ADMINISTRATOR`, sobre productos `PUBLISHED` o `SUSPENDED`. Se notifica al vendedor.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `ResourceStateConflictException`.
- **Estados que cambia:** `Product` a `BLOCKED`.

### `UnblockProductService`

- **Propósito:** un administrador levanta el bloqueo.
- **Quién lo usa:** `AdministratorPort.unblockProduct`
- **Entradas:** `Product execute(User administrator, ProductId productId)`
- **Reglas de negocio:** solo un `ADMINISTRATOR`. El vendedor decide después si lo publica. Se notifica al vendedor.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `ResourceStateConflictException` si no está bloqueado.
- **Estados que cambia:** `Product` de `BLOCKED` a `SUSPENDED`.

### `ConsultProductService`

- **Propósito:** consulta productos.
- **Quién lo usa:** `AdministratorPort.findProducts`, `LogisticsOperatorPort.findProducts`, `PublicAccessPort.findProduct`, `PublicAccessPort.searchProducts`, `SellerPort.findProducts`, `SupervisorPort.findProducts`
- **Entradas:** `Product findPublished(ProductId productId)`; `List<Product> searchPublished(String keyword)`; `List<Product> findBySeller(User seller)`; `List<Product> findAll(User actor)`
- **Reglas de negocio:** el catálogo público muestra solo productos disponibles. El vendedor ve todos los suyos. El personal interno ve todos.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException` también para un producto no disponible.
- **Estados que cambia:** ninguno.

### `ValidateProductAvailabilityService`

- **Propósito:** decide si un producto se puede comprar.
- **Quién lo usa:** `AddItemToCartService`, `ConsultProductService`, `PayOrderService`, `PlaceOrderService`
- **Entradas:** `boolean isAvailable(Product product)`; `void execute(Product product)`
- **Reglas de negocio:** el producto debe estar `PUBLISHED` y su vendedor `APPROVED`. Suspender a un vendedor oculta sus productos sin cambiarlos, y al reactivarlo vuelven.
- **Excepciones:** `BusinessRuleException` en `execute`.
- **Estados que cambia:** ninguno.
