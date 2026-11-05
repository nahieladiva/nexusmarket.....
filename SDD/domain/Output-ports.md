# Puertos de salida — NexusMarket

Los puertos de salida están en `application.domain.ports.out`. Son lo que el dominio necesita del
exterior. Los adaptadores están en `application.adapters.out`.

`CartRepository`, `InvoiceRepository`, `ShipmentRepository`, `ReturnRequestRepository` y
`RefundRepository` todavía no tienen adaptador: se implementan en la Fase 3.

| Puerto | Adaptador | Operaciones |
|---|---|---|
| `AuditLogPort` | `AuditLogPersistenceAdapter` | `record` |
| `CartRepository` | Pendiente (Fase 3) | `save`, `findByBuyerId` |
| `ClockPort` | `SystemClockAdapter` | `now` |
| `DomainEventPublisherPort` | `SpringDomainEventPublisherAdapter` | `publish` |
| `IdGeneratorPort` | `UuidGeneratorAdapter` | `newId` |
| `InventoryRepository` | `InventoryPersistenceAdapter` | `save`, `findById`, `findByProductIdAndWarehouseId`, `findByWarehouseId`, `findByProductId`, `findAll` |
| `InvoiceRepository` | Pendiente (Fase 3) | `save`, `findById`, `findByOrderId`, `findAll` |
| `NotificationPort` | `LoggingNotificationAdapter` | `notify` |
| `OrderRepository` | `OrderPersistenceAdapter` | `save`, `findById`, `findByBuyerId`, `findAll` |
| `PasswordEncoderPort` | `BCryptPasswordEncoderAdapter` | `encode`, `matches` |
| `ProductRepository` | `ProductPersistenceAdapter` | `save`, `findById`, `findByCode`, `findByNameContaining`, `findBySellerId`, `findAll` |
| `RefundRepository` | Pendiente (Fase 3) | `save`, `findById`, `findByOrderId`, `findAll` |
| `ReturnRequestRepository` | Pendiente (Fase 3) | `save`, `findById`, `findByOrderId`, `findAll` |
| `ShipmentRepository` | Pendiente (Fase 3) | `save`, `findById`, `findByOrderId`, `findAll` |
| `StockMovementLogPort` | `StockMovementLogAdapter` | `record`, `findByProductAndWarehouse` |
| `TokenProviderPort` | `JjwtTokenProviderAdapter` | `issue`, `parse` |
| `TokenStorePort` | `TokenStoreAdapter` | `save`, `findById` |
| `UserRepository` | `UserPersistenceAdapter` | `save`, `findById`, `findByEmail`, `findAll` |
| `WarehouseRepository` | `WarehousePersistenceAdapter` | `save`, `findById`, `findByNameContaining`, `findAll` |
