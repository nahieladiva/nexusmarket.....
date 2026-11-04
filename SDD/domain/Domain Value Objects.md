# Domain Value Objects - NexusMarket

## 1. Introduction / Introducción
### 1.1 Purpose / Propósito
### 1.2 What is a Value Object? / ¿Qué es un Objeto de Valor?
### 1.3 Design Principles / Principios de Diseño
   - Immutability / Inmutabilidad
   - Self-validation / Auto-validación
   - Value Equality / Igualdad por Valor
   - No identity / Sin Identidad

## 2. Complete List / Lista Completa
[Tabla resumen con todos los VO]

## 3. Detailed Definitions / Definiciones Detalladas
### 3.1 Email
   - Structure / Estructura
   - Validation Rules / Reglas de Validación
   - Factory Methods / Métodos de Fábrica
   - Operations / Operaciones
   - Usage in Domain / Uso en el Dominio
   - Code Example / Ejemplo de Código

### 3.2 PhoneNumber
   - Structure / Estructura
   - Validation Rules / Reglas de Validación
   - Format / Formato (internacional, E.164)
   - Operations / Operaciones
   - Code Example / Ejemplo de Código

### 3.3 Address
   - Structure / Estructura
   - Components / Componentes (street, city, state, zip, country)
   - Validation Rules / Reglas de Validación
   - Operations / Operaciones
   - Code Example / Ejemplo de Código

### 3.4 Money
   - Structure / Estructura (amount + currency)
   - Validation Rules / Reglas de Validación
   - Operations / Operaciones (add, subtract, multiply, compare)
   - Currency handling / Manejo de Monedas
   - Rounding rules / Reglas de Redondeo
   - Code Example / Ejemplo de Código

### 3.5 Quantity
   - Structure / Estructura
   - Validation Rules / Reglas de Validación (positive, integer)
   - Operations / Operaciones (add, subtract, compare)
   - Code Example / Ejemplo de Código

### 3.6 OrderStatus
   - Enumeration / Enumeración
   - States / Estados (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, etc.)
   - State Transitions / Transiciones de Estado
   - Allowed transitions / Transiciones Permitidas
   - Business rules per state / Reglas por estado
   - Code Example / Ejemplo de Código

### 3.7 ProductCode
   - Structure / Estructura (SKU format)
   - Validation Rules / Reglas de Validación
   - Generation rules / Reglas de Generación
   - Code Example / Ejemplo de Código

### 3.8 WarehouseLocation
   - Structure / Estructura (aisle, shelf, bin)
   - Validation Rules / Reglas de Validación
   - Code Example / Ejemplo de Código

### 3.9 UserId
   - Structure / Estructura (UUID)
   - Generation / Generación
   - Code Example / Ejemplo de Código

### 3.10 OrderId
   - Structure / Estructura (UUID or custom format)
   - Generation / Generación
   - Code Example / Ejemplo de Código

### 3.11 ProductId
   - Structure / Estructura (UUID)
   - Generation / Generación
   - Code Example / Ejemplo de Código

### 3.12 Percentage (for discounts)
   - Structure / Estructura
   - Validation Rules / Reglas de Validación (0-100)
   - Operations / Operaciones
   - Code Example / Ejemplo de Código

### 3.13 DateRange
   - Structure / Estructura (startDate, endDate)
   - Validation Rules / Reglas de Validación
   - Operations / Operaciones (overlap, contains, duration)
   - Code Example / Ejemplo de Código

## 4. Value Objects Usage Map / Mapa de Uso
[Tabla que relaciona cada VO con las entidades que lo usan]

## 5. Validation Strategy / Estrategia de Validación
### 5.1 Constructor validation / Validación en Constructor
### 5.2 Factory methods / Métodos de Fábrica
### 5.3 Exception handling / Manejo de Excepciones
### 5.4 Custom validation annotations / Anotaciones de Validación Personalizadas

## 6. Performance Considerations / Consideraciones de Rendimiento
### 6.1 Immutability benefits / Beneficios de Inmutabilidad
### 6.2 Caching strategies / Estrategias de Caché
### 6.3 Serialization / Serialización (JSON, JPA)

## 7. Testing Strategy / Estrategia de Pruebas
### 7.1 Unit tests for each VO / Pruebas Unitarias por cada VO
### 7.2 Edge cases / Casos Borde
### 7.3 Property-based testing / Pruebas Basadas en Propiedades

## 8. Glossary / Glosario

## 9. Appendix / Apéndice
### 9.1 JPA Mapping Examples / Ejemplos de Mapeo JPA
### 9.2 JSON Serialization Examples / Ejemplos de Serialización JSON
### 9.3 Validation Annotations / Anotaciones de Validación