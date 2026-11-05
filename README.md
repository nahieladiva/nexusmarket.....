# NexusMarket

API REST de un marketplace, hecha en Java 17 y Spring Boot 4 con arquitectura hexagonal. Vendedores,
compradores, operadores logísticos, administradores y supervisores participan en un mismo flujo: catálogo,
inventario, carrito, pago, factura, envío, devolución y reembolso.

MySQL guarda los datos del negocio. MongoDB guarda la bitácora de auditoría, el historial de stock y los
tokens JWT emitidos. Todo corre con Docker.

## Arquitectura

```
nexusm/src/main/java/application
├── domain                ← núcleo en Java puro: no depende de Spring, Lombok ni Jakarta
│   ├── models, valueobjects, enums, events, exceptions
│   ├── services          ← un servicio por caso de uso
│   └── ports
│       ├── in   (7)      ← lo que el sistema ofrece, un puerto por rol
│       └── out  (19)     ← lo que el sistema necesita del exterior
├── adapters
│   ├── in/rest           ← controladores por puerto, DTOs, filtro JWT, errores, OpenAPI
│   ├── useCases          ← implementan los puertos de entrada y definen las transacciones
│   └── out               ← MySQL, MongoDB, JWT, BCrypt, reloj, IDs, eventos, notificaciones
└── infrastructure        ← configuración de Spring, seguridad, OpenAPI y Mongo
```

Una petición entra por un controlador, pasa a su puerto de entrada, el caso de uso delega en los servicios
de dominio, y estos usan los puertos de salida. Las dependencias siempre apuntan hacia el dominio.

El diseño completo está en el [SDD](SDD/Software%20Architecture/Software%20Architecture.md).

## Roles

| Rol | Prefijo | Qué hace |
|---|---|---|
| Anónimo | `/api/v1/public` | Ve el catálogo, se registra como comprador e inicia sesión |
| Cualquier usuario autenticado | `/api/v1/account` | Maneja su perfil, sus direcciones y su contraseña, y cierra sesión |
| `BUYER` | `/api/v1/buyer` | Compra, paga, consulta sus pedidos y pide devoluciones |
| `SELLER` | `/api/v1/seller` | Maneja sus productos, sus bodegas y su stock |
| `LOGISTICS_OPERATOR` | `/api/v1/logistics` | Maneja stock, envíos y devoluciones recibidas |
| `ADMINISTRATOR` | `/api/v1/admin` | Registra usuarios, modera productos, aprueba devoluciones y procesa reembolsos |
| `SUPERVISOR` | `/api/v1/supervisor` | Consulta todo, sin modificar nada |

Cada usuario tiene un solo rol, y cada prefijo solo admite el suyo. Las rutas de "lo propio" no llevan el id
del usuario: sale del JWT.

## Cómo ejecutar con Docker

Requisito: Docker Desktop abierto. Desde la raíz del repositorio:

```bash
docker compose up -d --build
```

La primera vez tarda unos minutos porque descarga las dependencias. La API queda en
`http://localhost:8080`.

Al arrancar se crea un administrador: **admin@nexusmarket.com / Admin2026**. Se cambia con las variables
`ADMIN_EMAIL` y `ADMIN_PASSWORD`.

| Acción | Comando |
|---|---|
| Ver los logs | `docker compose logs -f api` |
| Apagar y conservar los datos | `docker compose stop` |
| Apagar y borrar los datos | `docker compose down -v` |

## Cómo usar Swagger

1. Abra `http://localhost:8080/swagger-ui.html`.
2. Arriba a la derecha elija el grupo, por ejemplo `public`.
3. Ejecute `POST /api/v1/public/login` con el administrador y copie el `token` de la respuesta.
4. Pulse **Authorize**, pegue el token sin la palabra `Bearer` y confirme.
5. Elija el grupo del rol, por ejemplo `admin`, y pruebe sus rutas.

La especificación OpenAPI está en `http://localhost:8080/v3/api-docs`, con un grupo por rol, por ejemplo
`/v3/api-docs/buyer`.

## Cómo usar Postman

En [postman/](postman) están la colección y el environment. Cada petición guarda los tokens y los ids que
necesitan las siguientes, y cada ejecución crea sus propios usuarios y productos, así que se puede repetir.

**Desde Postman:** importe los dos archivos, elija el environment `NexusMarket local` y ejecute la colección
con el Collection Runner, en orden.

**Con newman en Docker,** sin instalar nada. Desde la raíz, en PowerShell:

```powershell
docker run --rm -v "${PWD}/postman:/etc/newman" postman/newman:6-alpine run NexusMarket.postman_collection.json -e NexusMarket.postman_environment.json --env-var baseUrl=http://host.docker.internal:8080
```

`host.docker.internal` es la forma de llegar al `localhost:8080` de su equipo desde el contenedor de newman.

La colección recorre el flujo completo: el administrador crea usuarios; el vendedor prepara bodega, producto
e inventario; el comprador paga; se factura, se envía y se entrega; hay devolución, reembolso, cancelación,
bloqueo de producto y cambio de contraseña con revocación de sesiones; y al final los casos 400, 401, 403,
404, 409 y 422.

## Endpoints por rol

La lista completa, con el código de éxito y la operación de cada ruta, está en
[REST-API](SDD/domain/REST-API.md).

### Público: `/api/v1/public`

| Método y ruta | Qué hace |
|---|---|
| `POST /buyers` | Registra un comprador |
| `POST /login` | Inicia sesión y devuelve el JWT |
| `GET /products?keyword=` | Busca en el catálogo |
| `GET /products/{productId}` | Consulta un producto publicado |

### Cuenta: `/api/v1/account`

| Método y ruta | Qué hace |
|---|---|
| `GET`, `PATCH /profile` | Consulta y actualiza el perfil |
| `POST /addresses`, `POST /addresses/remove` | Agrega y quita direcciones |
| `PUT /password` | Cambia la contraseña y cierra todas las sesiones |
| `POST /logout` | Cierra la sesión actual |

### Comprador: `/api/v1/buyer`

| Método y ruta | Qué hace |
|---|---|
| `GET`, `DELETE /cart` | Consulta y vacía el carrito |
| `POST /cart/items`, `PATCH`, `DELETE /cart/items/{productId}` | Maneja las líneas del carrito |
| `POST /orders`, `GET /orders`, `GET /orders/{orderId}` | Crea un pedido desde el carrito y lo consulta |
| `POST /orders/{orderId}/pay`, `/cancel` | Paga y cancela |
| `GET /orders/{orderId}/invoice`, `/shipments`, `/refunds` | Consulta factura, envíos y reembolsos |
| `POST`, `GET /orders/{orderId}/returns` | Pide y consulta devoluciones |

### Vendedor: `/api/v1/seller`

| Método y ruta | Qué hace |
|---|---|
| `POST`, `GET /products`, `PATCH /products/{productId}` | Registra, lista y edita productos |
| `PUT /products/{productId}/price` | Cambia el precio |
| `POST /products/{productId}/publish`, `/suspend`, `/discontinue` | Cambia el estado del producto |
| `POST`, `GET /warehouses`, `GET`, `PUT /warehouses/{warehouseId}` | Maneja sus bodegas |
| `POST /warehouses/{warehouseId}/activate`, `/deactivate` | Activa y desactiva una bodega |
| `POST`, `GET /inventory`, `GET /inventory/{productId}/{warehouseId}` | Crea y consulta inventario |
| `POST /inventory/restock`, `/adjust`, `PUT /inventory/threshold` | Reabastece, ajusta y cambia el punto de reorden |
| `GET /inventory/{productId}/{warehouseId}/movements` | Consulta el historial de stock |
| `GET /orders`, `GET /orders/{orderId}` | Consulta los pedidos con sus productos |

### Operador logístico: `/api/v1/logistics`

| Método y ruta | Qué hace |
|---|---|
| `POST`, `GET /shipments` | Prepara y lista envíos |
| `POST /shipments/{shipmentId}/pack`, `/dispatch`, `/deliver` | Avanza el envío |
| `GET /orders/{orderId}/shipments` | Consulta los envíos de un pedido |
| `POST /returns/{returnId}/receive`, `GET /returns` | Recibe y lista devoluciones |
| `POST`, `GET /inventory`, `GET /inventory/low-stock` | Crea, lista y revisa inventario |
| `POST /inventory/restock`, `/adjust`, `PUT /inventory/threshold` | Maneja el stock |
| `GET /inventory/{productId}/{warehouseId}`, `.../movements` | Consulta inventario e historial |
| `GET /warehouses`, `GET /warehouses/{warehouseId}`, `.../inventory` | Consulta bodegas y su inventario |
| `GET /orders`, `GET /orders/{orderId}`, `GET /products` | Consulta pedidos y productos |

### Administrador: `/api/v1/admin`

| Método y ruta | Qué hace |
|---|---|
| `POST /sellers`, `POST /staff` | Registra vendedores y personal interno |
| `POST /sellers/{sellerId}/approve`, `/suspend` | Aprueba y suspende vendedores |
| `GET /users`, `GET /users/{userId}` | Consulta usuarios |
| `POST`, `GET /warehouses`, `GET`, `PUT /warehouses/{warehouseId}` | Maneja las bodegas del marketplace |
| `POST /warehouses/{warehouseId}/activate`, `/deactivate` | Activa y desactiva bodegas |
| `POST /products/{productId}/block`, `/unblock`, `/discontinue` | Modera productos |
| `GET /products`, `GET /inventory`, `GET /inventory/low-stock` | Consulta catálogo e inventario |
| `GET /inventory/{productId}/{warehouseId}`, `.../movements` | Consulta inventario e historial |
| `GET /orders`, `GET /orders/{orderId}`, `POST /orders/{orderId}/cancel` | Consulta y cancela pedidos |
| `GET /orders/{orderId}/invoice`, `GET /invoices`, `GET /shipments` | Consulta facturas y envíos |
| `POST /returns/{returnId}/approve`, `/reject`, `GET /returns` | Resuelve devoluciones |
| `POST /refunds/{refundId}/process`, `/reject`, `GET /refunds` | Resuelve reembolsos |

### Supervisor: `/api/v1/supervisor`

Solo consulta: `GET /users`, `/users/{userId}`, `/products`, `/warehouses`, `/warehouses/{warehouseId}`,
`/inventory`, `/inventory/low-stock`, `/inventory/{productId}/{warehouseId}`, `.../movements`, `/orders`,
`/orders/{orderId}`, `/orders/{orderId}/invoice`, `/invoices`, `/shipments`, `/returns` y `/refunds`.

## Errores

Todos los errores tienen el mismo formato JSON: `timestamp`, `status`, `error`, `message` y `path`.

| Código | Significado |
|---|---|
| 400 | Falta un dato o tiene un formato inválido |
| 401 | Falta el token, es inválido o está revocado |
| 403 | El rol no puede entrar, o el recurso es de otro usuario |
| 404 | El recurso no existe |
| 409 | El recurso está duplicado, o su estado o su stock no permiten la operación |
| 422 | La operación rompe una regla de negocio |

## Pruebas

```bash
cd nexusm
./mvnw clean test
```

No necesitan bases de datos externas: las de dominio usan repositorios en memoria y las de la API usan el
contexto real de Spring con H2.

Las pruebas `*IT` usan el MySQL y el MongoDB reales y no corren con `./mvnw test`:

```bash
docker compose up -d mysql mongo
cd nexusm
./mvnw test -Dtest=*IT
```

`./mvnw clean test` también genera el informe de cobertura de JaCoCo en
`nexusm/target/site/jacoco/index.html`.

[tools/VerifySdd.java](tools/README.md) comprueba que el SDD coincide con el código compilado.

## Documentación

| Documento | Contenido |
|---|---|
| [Software Architecture](SDD/Software%20Architecture/Software%20Architecture.md) | Arquitectura, persistencia, seguridad y despliegue |
| [Domain Model](SDD/domain/Domain%20Model.md) | Entidades, agregados y reglas |
| [Domain Value Objects](SDD/domain/Domain%20Value%20Objects.md) | Value objects y enums |
| [Services](SDD/domain/Services.md) | Matriz de trazabilidad de los servicios de dominio |
| [Input-ports](SDD/domain/Input-ports.md), [Output-ports](SDD/domain/Output-ports.md) | Puertos |
| [REST-API](SDD/domain/REST-API.md) | Rutas, roles y códigos de error |
