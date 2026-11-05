# Servicios de carrito — NexusMarket

Paquete: `application.domain.services.cart`. La vista general está en [Services](../Services.md).

### `ConsultCartService`

- **Propósito:** devuelve el carrito de un comprador.
- **Quién lo usa:** `BuyerPort.findCart`; también `AddItemToCartService`, `ChangeCartItemQuantityService`, `ClearCartService`, `PlaceOrderService`, `RemoveItemFromCartService`
- **Entradas:** `Cart execute(User buyer)`
- **Reglas de negocio:** solo un `BUYER` tiene carrito. Si aún no existe, se crea vacío con `Cart.createFor`.
- **Excepciones:** `ForbiddenActionException` si el usuario no es `BUYER`.
- **Estados que cambia:** ninguno. Puede crear un `Cart`.

### `AddItemToCartService`

- **Propósito:** agrega un producto al carrito.
- **Quién lo usa:** `BuyerPort.addCartItem`
- **Entradas:** `Cart execute(User buyer, ProductId productId, Quantity quantity)`
- **Reglas de negocio:** el producto debe estar disponible según `ValidateProductAvailabilityService`. El precio se toma del catálogo, nunca del cliente. Si el producto ya está en el carrito, se suma la cantidad.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException` si el producto no existe, `BusinessRuleException` si no está disponible, `IllegalArgumentException` si la cantidad es cero.
- **Estados que cambia:** ninguno. Modifica las líneas del `Cart`.

### `ChangeCartItemQuantityService`

- **Propósito:** cambia la cantidad de un producto del carrito.
- **Quién lo usa:** `BuyerPort.changeCartItemQuantity`
- **Entradas:** `Cart execute(User buyer, ProductId productId, Quantity newQuantity)`
- **Reglas de negocio:** una cantidad de cero elimina el producto.
- **Excepciones:** `ForbiddenActionException`, `BusinessRuleException` si el producto no está en el carrito.
- **Estados que cambia:** ninguno. Modifica las líneas del `Cart`.

### `RemoveItemFromCartService`

- **Propósito:** quita un producto del carrito.
- **Quién lo usa:** `BuyerPort.removeCartItem`
- **Entradas:** `Cart execute(User buyer, ProductId productId)`
- **Reglas de negocio:** el producto debe estar en el carrito.
- **Excepciones:** `ForbiddenActionException`, `BusinessRuleException` si el producto no está en el carrito.
- **Estados que cambia:** ninguno. Modifica las líneas del `Cart`.

### `ClearCartService`

- **Propósito:** vacía el carrito.
- **Quién lo usa:** `BuyerPort.clearCart`
- **Entradas:** `Cart execute(User buyer)`
- **Reglas de negocio:** se quitan todas las líneas.
- **Excepciones:** `ForbiddenActionException`.
- **Estados que cambia:** ninguno. Modifica las líneas del `Cart`.
