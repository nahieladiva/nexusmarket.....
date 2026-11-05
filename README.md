# NexusMarket

Marketplace en Java 17 + Spring Boot 4 con arquitectura hexagonal.
MySQL guarda los datos del negocio y MongoDB la bitácora de auditoría, el historial de
stock y la base de tokens JWT. Todo corre en Docker Desktop.

## Cómo correrlo

Requisito: Docker Desktop abierto.

```bash
docker compose up -d --build
```

La primera vez tarda unos minutos porque descarga las dependencias de Maven.
La API queda en `http://localhost:8080`. Para ver los logs (incluidas las notificaciones):

```bash
docker compose logs -f api
```

Para apagar todo: `docker compose down` (agregue `-v` para borrar también los datos).

Al arrancar se crea un administrador: **admin@nexusmarket.com / Admin2026**
(se cambia con las variables `ADMIN_EMAIL` y `ADMIN_PASSWORD`).

### Pruebas

```bash
cd nexusm
./mvnw test
```

Las pruebas de `application.domain.services` usan repositorios en memoria y no necesitan bases
de datos. `NexusmarketApplicationTests` (contexto completo) está deshabilitada porque
necesita MySQL y MongoDB arriba.

## Arquitectura

```
nexusm/src/main/java/application
├── domain                ← núcleo: no depende de Spring
│   ├── models, valueobjects, enums, events, exceptions
│   ├── services          ← reglas que involucran varios agregados
│   └── ports
│       ├── in   (7)      ← lo que el sistema ofrece (casos de uso)
│       └── out  (19)     ← lo que el sistema necesita del exterior
├── adapters
│   ├── in/rest           ← controladores, DTOs, filtro JWT
│   ├── out               ← MySQL, MongoDB, JWT, BCrypt, reloj, IDs, eventos, notificaciones
│   └── useCases          ← implementación de los casos de uso (puertos de entrada)
└── infrastructure        ← configuración de Spring, seguridad, Mongo
```

El detalle de servicios, puertos y reglas de negocio está en `SDD/domain/Services.md`,
`SDD/domain/Input-ports.md` y `SDD/domain/Output-ports.md`.

## Flujo de prueba rápido

Todas las rutas protegidas llevan el header `Authorization: Bearer <token>`.

1. **Administrador inicia sesión**: `POST /api/auth/login` con `{"email":"admin@nexusmarket.com","password":"Admin2026"}`.
2. **Vendedor se registra**: `POST /api/auth/register/seller`
   ```json
   {"fullName":"Luisa Pérez","email":"luisa@tienda.com","phone":"+573001234567",
    "password":"Tienda2026","businessName":"Tienda Luisa"}
   ```
3. **Admin aprueba al vendedor**: `PATCH /api/users/{idVendedor}/approve`.
4. **Admin crea una bodega**: `POST /api/warehouses`
   ```json
   {"name":"Bodega Norte",
    "address":{"street":"Calle 10 # 43-20","city":"Medellín","state":"Antioquia","zipCode":"050021","country":"Colombia"},
    "location":{"aisle":"A","shelf":"1","bin":"1"}}
   ```
5. **Vendedor inicia sesión y publica un producto**: `POST /api/products`
   ```json
   {"code":"TEC-0001","name":"Audífonos","description":"Bluetooth","price":{"amount":"150000","currency":"COP"}}
   ```
6. **Vendedor registra stock**: `POST /api/inventory`
   ```json
   {"productId":"...","warehouseId":"...","onHand":20,"reorderThreshold":5,
    "location":{"aisle":"A","shelf":"1","bin":"1"}}
   ```
7. **Vendedor crea una promoción**: `POST /api/promotions`
   ```json
   {"productId":"...","discountPercent":20,"startDate":"2026-11-01","endDate":"2026-11-30"}
   ```
8. **Comprador se registra, inicia sesión y compra**: `POST /api/orders`
   ```json
   {"items":[{"productId":"...","quantity":2}]}
   ```
   El precio no se envía: lo calcula el sistema.
9. **Admin confirma** (`PATCH /api/orders/{id}/confirm`), **envía** (`/ship`) y **entrega** (`/deliver`).
10. **Historial de stock**: `GET /api/inventory/movements?productId=...&warehouseId=...`
11. **Cerrar sesión**: `POST /api/auth/logout` — el token queda revocado aunque no haya expirado.

## Endpoints (55)

| Recurso | Método y ruta | Rol |
|---|---|---|
| Auth | `POST /api/auth/register/buyer`, `POST /api/auth/register/seller`, `POST /api/auth/login` | Público |
| Auth | `POST /api/auth/logout` | Autenticado |
| Usuarios | `GET /api/users/me`, `PATCH /api/users/me`, `POST /api/users/me/addresses`, `DELETE /api/users/me/addresses` | Autenticado |
| Usuarios | `GET /api/users`, `GET /api/users/{id}`, `PATCH /api/users/{id}/approve` | ADMIN |
| Productos | `GET /api/products?keyword=`, `GET /api/products/{id}` | Público |
| Productos | `GET /api/products/mine`, `POST /api/products`, `PUT /api/products/{id}`, `PATCH /api/products/{id}/price`, `PATCH /api/products/{id}/availability` | SELLER (dueño) |
| Productos | `GET /api/products/all` | ADMIN |
| Bodegas | `GET /api/warehouses`, `GET /api/warehouses/{id}` | Autenticado |
| Bodegas | `POST /api/warehouses`, `PUT /api/warehouses/{id}`, `PATCH /api/warehouses/{id}/status` | ADMIN |
| Inventario | `POST /api/inventory`, `PATCH /api/inventory/restock`, `PATCH /api/inventory/adjust`, `PATCH /api/inventory/threshold`, `GET /api/inventory?productId=&warehouseId=`, `GET /api/inventory/availability`, `GET /api/inventory/warehouse/{id}`, `GET /api/inventory/all`, `GET /api/inventory/low-stock`, `GET /api/inventory/movements` | SELLER (dueño) o ADMIN |
| Pedidos | `POST /api/orders`, `GET /api/orders/mine` | BUYER |
| Pedidos | `GET /api/orders/seller` | SELLER |
| Pedidos | `GET /api/orders`, `PATCH /api/orders/{id}/confirm`, `/ship`, `/deliver` | ADMIN |
| Pedidos | `PATCH /api/orders/{id}/cancel` | BUYER (dueño) o ADMIN |
| Pedidos | `GET /api/orders/{id}` | Comprador dueño, vendedor con productos en el pedido, o ADMIN |
| Promociones | `GET /api/promotions/active`, `GET /api/promotions/product/{id}`, `GET /api/promotions/price/{id}` | Público |
| Promociones | `POST /api/promotions`, `PATCH /api/promotions/{id}/end` | SELLER (dueño) |

## Códigos de error

| Código | Cuándo |
|---|---|
| 400 | Datos con formato inválido (email, teléfono E.164, SKU `XXX-0000`, JSON mal formado) |
| 401 | Sin token, token inválido, expirado o revocado; credenciales incorrectas |
| 403 | El rol no tiene permiso, o el recurso es de otro usuario |
| 404 | El recurso no existe |
| 409 | Se viola una regla de negocio (stock insuficiente, transición de estado inválida, duplicado, etc.) |
