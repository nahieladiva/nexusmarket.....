# Puertos de salida — NexusMarket

Los puertos de salida están en `application.domain.ports.out`. Son lo que el dominio necesita del
exterior. Los adaptadores están en `application.adapters.out`.

| Puerto | Adaptador | Operaciones |
|---|---|---|
| `AuditLogPort` | `AuditLogPersistenceAdapter` | `record` |
| `CartRepository` | `CartPersistenceAdapter` | `save`, `findByBuyerId` |
| `ClockPort` | `SystemClockAdapter` | `now` |
| `DomainEventPublisherPort` | `SpringDomainEventPublisherAdapter` | `publish` |
| `IdGeneratorPort` | `UuidGeneratorAdapter` | `newId` |
| `InventoryRepository` | `InventoryPersistenceAdapter` | `save`, `findById`, `findByProductIdAndWarehouseId`, `findByWarehouseId`, `findByProductId`, `findAll` |
| `InvoiceRepository` | `InvoicePersistenceAdapter` | `save`, `findById`, `findByOrderId`, `findAll` |
| `NotificationPort` | `LoggingNotificationAdapter` | `notify` |
| `OrderRepository` | `OrderPersistenceAdapter` | `save`, `findById`, `findByBuyerId`, `findAll` |
| `PasswordEncoderPort` | `BCryptPasswordEncoderAdapter` | `encode`, `matches` |
| `ProductRepository` | `ProductPersistenceAdapter` | `save`, `findById`, `findByCode`, `findByNameContaining`, `findBySellerId`, `findAll` |
| `RefundRepository` | `RefundPersistenceAdapter` | `save`, `findById`, `findByOrderId`, `findAll` |
| `ReturnRequestRepository` | `ReturnRequestPersistenceAdapter` | `save`, `findById`, `findByOrderId`, `findAll` |
| `ShipmentRepository` | `ShipmentPersistenceAdapter` | `save`, `findById`, `findByOrderId`, `findAll` |
| `StockMovementLogPort` | `StockMovementLogAdapter` | `record`, `findByProductAndWarehouse` |
| `TokenProviderPort` | `JjwtTokenProviderAdapter` | `issue`, `parse` |
| `TokenStorePort` | `TokenStoreAdapter` | `save`, `findById`, `revokeAllForUser` |
| `UserRepository` | `UserPersistenceAdapter` | `save`, `findById`, `findByEmail`, `findAll` |
| `WarehouseRepository` | `WarehousePersistenceAdapter` | `save`, `findById`, `findByNameContaining`, `findAll` |
