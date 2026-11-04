# Domain Model - NexusMarket

## 1. Introduction / Introducción
### 1.1 Purpose / Propósito
### 1.2 Scope / Alcance
### 1.3 Relationship to Business Specification / Relación con la Especificación Funcional
### 1.4 Key Concepts / Conceptos Clave

## 2. Domain Entities / Entidades de Dominio
### 2.1 User (Base Entity)
   - Attributes / Atributos
   - Methods / Métodos
   - Business Rules / Reglas de Negocio
   - Validations / Validaciones
### 2.2 Buyer (extends User)
   - Attributes / Atributos
   - Methods / Métodos
   - Business Rules / Reglas de Negocio
   - Validations / Validaciones
### 2.3 Seller (extends User)
   - Attributes / Atributos
   - Methods / Métodos
   - Business Rules / Reglas de Negocio
   - Validations / Validaciones
### 2.4 Warehouse
   - Attributes / Atributos
   - Methods / Métodos
   - Business Rules / Reglas de Negocio
   - Validations / Validaciones
### 2.5 Product
   - Attributes / Atributos
   - Methods / Métodos
   - Business Rules / Reglas de Negocio
   - Validations / Validaciones
### 2.6 Inventory
   - Attributes / Atributos
   - Methods / Métodos
   - Business Rules / Reglas de Negocio
   - Validations / Validaciones
### 2.7 Order (Aggregate Root)
   - Attributes / Atributos
   - Methods / Métodos
   - Business Rules / Reglas de Negocio
   - Validations / Validaciones
### 2.8 OrderItem (Entity inside Order)
   - Attributes / Atributos
   - Methods / Métodos
   - Business Rules / Reglas de Negocio
   - Validations / Validaciones

## 3. Aggregates / Agregados
### 3.1 Order Aggregate
   - Root: Order
   - Internal Entities: OrderItem
   - Invariants / Invariantes
   - Consistency Rules / Reglas de Consistencia
### 3.2 Product Aggregate
   - Root: Product
   - Internal Entities: Inventory (as value object or entity?)
   - Invariants / Invariantes
### 3.3 User Aggregate
   - Root: User
   - Specializations: Buyer, Seller
   - Invariants / Invariantes

## 4. Domain Services / Servicios de Dominio
### 4.1 InventoryDomainService
   - Responsibilities / Responsabilidades
   - Methods / Métodos
   - When to use / Cuándo usar
### 4.2 OrderFulfillmentDomainService
   - Responsibilities / Responsabilidades
   - Methods / Métodos
   - When to use / Cuándo usar
### 4.3 PricingDomainService
   - Responsibilities / Responsabilidades
   - Methods / Métodos
### 4.4 WarehouseAllocationService
   - Responsibilities / Responsabilidades
   - Methods / Métodos

## 5. Repository Interfaces / Interfaces de Repositorio
### 5.1 UserRepository
### 5.2 ProductRepository
### 5.3 OrderRepository
### 5.4 InventoryRepository
### 5.5 WarehouseRepository

## 6. Domain Events / Eventos de Dominio
### 6.1 OrderCreatedEvent
### 6.2 OrderStatusChangedEvent
### 6.3 InventoryUpdatedEvent
### 6.4 ProductAddedEvent
### 6.5 LowStockEvent

## 7. Relationships Diagram / Diagrama de Relaciones
### 7.1 Entity-Relationship Diagram (Mermaid)
### 7.2 Aggregate Boundaries Diagram (Mermaid)
### 7.3 Domain Services Interactions (Mermaid)

## 8. Business Rules Summary / Resumen de Reglas de Negocio
[Tabla con todas las reglas extraídas de la especificación funcional]

## 9. Glossary / Glosario
[Términos en inglés con su traducción y definición en español]

## 10. Appendix / Apéndice
### 10.1 Mapping to Functional Specification / Mapeo a la Especificación Funcional
### 10.2 Decision Records / Registros de Decisiones