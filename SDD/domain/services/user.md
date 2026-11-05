# Servicios de usuario — NexusMarket

Paquete: `application.domain.services.user`. La vista general está en [Services](../Services.md).

### `RegisterBuyerService`

- **Propósito:** registro público de un comprador.
- **Quién lo usa:** `PublicAccessPort.registerBuyer`
- **Entradas:** `User execute(String fullName, Email email, PhoneNumber phone, String rawPassword, Address defaultShippingAddress)`
- **Reglas de negocio:** es el único registro sin autenticación (RG-01). El correo es único y la contraseña cumple `PasswordPolicy`.
- **Excepciones:** `DuplicateResourceException`, `BusinessRuleException`.
- **Estados que cambia:** crea el `Buyer`.

### `RegisterSellerService`

- **Propósito:** un administrador registra un vendedor.
- **Quién lo usa:** `AdministratorPort.registerSeller`
- **Entradas:** `User execute(User administrator, String fullName, Email email, PhoneNumber phone, String rawPassword, String businessName)`
- **Reglas de negocio:** solo un `ADMINISTRATOR`. El correo es único y la contraseña cumple `PasswordPolicy`.
- **Excepciones:** `ForbiddenActionException`, `DuplicateResourceException`, `BusinessRuleException`.
- **Estados que cambia:** crea el `Seller` en `PENDING_APPROVAL`.

### `RegisterStaffUserService`

- **Propósito:** un administrador registra personal interno.
- **Quién lo usa:** `AdministratorPort.registerStaffUser`
- **Entradas:** `User execute(User administrator, UserRole role, String fullName, Email email, PhoneNumber phone, String rawPassword)`
- **Reglas de negocio:** solo un `ADMINISTRATOR`. El rol debe cumplir `UserRole.isInternalStaff` y define la subclase creada.
- **Excepciones:** `ForbiddenActionException`, `DuplicateResourceException`, `BusinessRuleException`.
- **Estados que cambia:** crea el `LogisticsOperator`, `Supervisor` o `Administrator`.

### `ApproveSellerService`

- **Propósito:** aprueba a un vendedor o levanta su suspensión.
- **Quién lo usa:** `AdministratorPort.approveSeller`
- **Entradas:** `Seller execute(User administrator, UserId sellerId)`
- **Reglas de negocio:** solo un `ADMINISTRATOR`. Se notifica al vendedor.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `BusinessRuleException` si no es vendedor o ya está aprobado, `ResourceStateConflictException` si el estado del recurso no lo permite.
- **Estados que cambia:** `Seller` de `PENDING_APPROVAL` o `SUSPENDED` a `APPROVED`.

### `SuspendSellerService`

- **Propósito:** suspende a un vendedor aprobado.
- **Quién lo usa:** `AdministratorPort.suspendSeller`
- **Entradas:** `Seller execute(User administrator, UserId sellerId)`
- **Reglas de negocio:** solo un `ADMINISTRATOR`. Sus productos dejan de estar disponibles. Se notifica al vendedor.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`, `BusinessRuleException`, `ResourceStateConflictException` si el estado del recurso no lo permite.
- **Estados que cambia:** `Seller` de `APPROVED` a `SUSPENDED`.

### `UpdateProfileService`

- **Propósito:** un usuario cambia su nombre o teléfono.
- **Quién lo usa:** `AccountPort.updateProfile`
- **Entradas:** `User execute(User user, String fullName, PhoneNumber phone)`
- **Reglas de negocio:** un valor `null` conserva el dato actual.
- **Excepciones:** `IllegalArgumentException` si el nombre está vacío.
- **Estados que cambia:** ninguno.

### `AddAddressService`

- **Propósito:** un usuario agrega una dirección.
- **Quién lo usa:** `AccountPort.addAddress`
- **Entradas:** `User execute(User user, Address address)`
- **Reglas de negocio:** máximo `MAX_ADDRESSES` (5) direcciones, sin repetir.
- **Excepciones:** `DuplicateResourceException`, `BusinessRuleException`.
- **Estados que cambia:** ninguno.

### `RemoveAddressService`

- **Propósito:** un usuario quita una dirección.
- **Quién lo usa:** `AccountPort.removeAddress`
- **Entradas:** `User execute(User user, Address address)`
- **Reglas de negocio:** la dirección debe estar registrada.
- **Excepciones:** `IllegalArgumentException`.
- **Estados que cambia:** ninguno.

### `ChangePasswordService`

- **Propósito:** un usuario cambia su contraseña.
- **Quién lo usa:** `AccountPort.changePassword`
- **Entradas:** `User execute(User user, String currentPassword, String newPassword)`
- **Reglas de negocio:** exige la contraseña actual, y la nueva cumple `PasswordPolicy`. Después revoca todos los tokens activos del usuario con `TokenStorePort.revokeAllForUser`, incluida la sesión actual, así que debe iniciar sesión otra vez. Si la contraseña no cambia, las sesiones siguen abiertas.
- **Excepciones:** `InvalidCredentialsException`, `BusinessRuleException`.
- **Estados que cambia:** los `IssuedToken` del usuario pasan de `ACTIVE` a `REVOKED`.

### `ConsultUserService`

- **Propósito:** consulta usuarios.
- **Quién lo usa:** `AdministratorPort.findUser`, `AdministratorPort.findUsers`, `SupervisorPort.findUser`, `SupervisorPort.findUsers`
- **Entradas:** `User execute(User actor, UserId userId)`; `List<User> findAll(User actor)`
- **Reglas de negocio:** solo el `ADMINISTRATOR` y el `SUPERVISOR`.
- **Excepciones:** `ForbiddenActionException`, `ResourceNotFoundException`.
- **Estados que cambia:** ninguno.

### `ValidateUserUniquenessService`

- **Propósito:** valida que ningún otro usuario tenga un correo.
- **Quién lo usa:** `RegisterBuyerService`, `RegisterSellerService`, `RegisterStaffUserService`
- **Entradas:** `void execute(Email email)`
- **Reglas de negocio:** `Email` ya está normalizado a minúsculas.
- **Excepciones:** `DuplicateResourceException`.
- **Estados que cambia:** ninguno.

### `PasswordPolicy`

- **Propósito:** define qué contraseña es válida.
- **Quién lo usa:** `ChangePasswordService`, `RegisterBuyerService`, `RegisterSellerService`, `RegisterStaffUserService`
- **Entradas:** `void validate(String rawPassword)`
- **Reglas de negocio:** mínimo `MIN_LENGTH` (8) caracteres, con al menos una letra y un número.
- **Excepciones:** `BusinessRuleException`.
- **Estados que cambia:** ninguno.
