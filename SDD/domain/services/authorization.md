# Servicios de autorización — NexusMarket

Paquete: `application.domain.services.authorization`. La vista general está en [Services](../Services.md).

La validación de rol y de propiedad está en las entidades, con `User.requireRole`, `User.requireAnyRole`,
`Order.requireBelongsTo`, `Product.requireOwnedBy` y `Warehouse.requireManagedBy`. Por eso, los demás
servicios reciben al usuario como actor y este paquete se ocupa de obtenerlo y de la sesión.

### `ResolveActorService`

- **Propósito:** obtiene el usuario autenticado que los demás servicios reciben como actor.
- **Quién lo usa:** todos los casos de uso con actor; `AccountPort.findProfile` lo expone directamente
- **Entradas:** `User execute(UserId userId)`
- **Reglas de negocio:** el usuario debe existir.
- **Excepciones:** `ResourceNotFoundException`.
- **Estados que cambia:** ninguno.

### `LoginService`

- **Propósito:** valida las credenciales y abre una sesión.
- **Quién lo usa:** `PublicAccessPort.login`
- **Entradas:** `AuthSession execute(Email email, String rawPassword)`
- **Reglas de negocio:** el token dura `TOKEN_LIFETIME` (1 hora) y se guarda para poder revocarlo. El error es el mismo si falla el correo o la contraseña.
- **Excepciones:** `InvalidCredentialsException`.
- **Estados que cambia:** crea el `IssuedToken` en `ACTIVE`.

### `ValidateTokenService`

- **Propósito:** valida un JWT.
- **Quién lo usa:** `AccountPort.validateToken`; también `LogoutService`
- **Entradas:** `TokenClaims execute(String token)`
- **Reglas de negocio:** verifica la firma, la expiración y que el token no esté revocado.
- **Excepciones:** `InvalidTokenException`.
- **Estados que cambia:** ninguno.

### `LogoutService`

- **Propósito:** cierra una sesión.
- **Quién lo usa:** `AccountPort.logout`
- **Entradas:** `void execute(String token)`
- **Reglas de negocio:** el token revocado se rechaza aunque no haya expirado.
- **Excepciones:** `InvalidTokenException`.
- **Estados que cambia:** `IssuedToken` de `ACTIVE` a `REVOKED`.
