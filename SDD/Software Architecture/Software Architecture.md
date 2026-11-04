Overview
The Banking Information Management System follows a Hexagonal Architecture (Ports and Adapters) combined with Domain-Driven Design (DDD) principles.

The primary objective of this architecture is to isolate the business domain from external technologies, ensuring that business rules remain independent from frameworks, databases, communication protocols, and infrastructure concerns.

This approach promotes maintainability, scalability, testability, and technology independence.

Architectural Principles
The architecture is based on the following principles:

Domain-first design.
Separation of concerns.
Dependency inversion.
Technology independence.
High cohesion.
Low coupling.
Explicit boundaries between layers.
The domain contains all business rules and never depends on external technologies.

Architecture Layers
The application is organized into four major components:

Application
│
├── Adapters
│
├── Domain
│
└── Infrastructure
Each component has a clearly defined responsibility.

Package Structure
src/
└── main/
    └── java/
        └── application/
            │
            ├── App.java
            │
            ├── adapters/
            │   │
            │   ├── in/
            │   │   └── rest/
            │   │       ├── controllers/
            │   │       ├── requests/
            │   │       ├── responses/
            │   │       └── mappers/
            │   │
            │   └── out/
            │       └── persistence/
            │           ├── mysql/
            │           │   ├── adapters/
            │           │   ├── entities/
            │           │   ├── repositories/
            │           │   └── mappers/
            │           │
            │           └── mongodb/
            │               ├── adapters/
            │               ├── documents/
            │               ├── repositories/
            │               └── mappers/
            │
            ├── domain/
            │   ├── models/
            │   ├── valueobjects/
            │   ├── enums/
            │   ├── services/
            │   ├── exceptions/
            │   └── ports/
            │       ├── in/
            │       └── out/
            │
            └── infrastructure/
                ├── config/
                ├── database/
                └── security/
Layer Responsibilities
Application
The application package represents the root of the project.

It contains the application entry point and all architectural components.

Responsibilities
Application bootstrap.
Component organization.
Dependency composition.
App.java
Description
App.java is the application's entry point.

Responsibilities
Initialize the application.
Load the infrastructure.
Configure dependency injection.
Start the REST server.
Adapters
The adapters connect external technologies with the business domain.

Adapters translate external requests into domain operations and transform domain objects into technology-specific representations.

The domain never communicates directly with external systems.

Input Adapters
Input adapters expose the application to external clients.

Current implementation:

adapters/in/rest
Responsibilities
Receive HTTP requests.
Validate incoming data.
Convert Request DTOs into Domain Models.
Execute application use cases.
Convert domain results into Response DTOs.
Controllers
Controllers expose REST endpoints.

Responsibilities:

Receive HTTP requests.
Delegate execution to the domain.
Return HTTP responses.
Controllers must never implement business rules.

Requests
Request DTOs represent incoming HTTP payloads.

Responsibilities:

Receive client data.
Validate input.
Transport data into the application.
These objects must not contain business logic.

Responses
Response DTOs represent outgoing HTTP responses.

Responsibilities:

Return processed information.
Hide internal domain implementation.
Standardize API responses.
Mappers
Responsible for converting between:

Request DTO ↔ Domain Model
Domain Model ↔ Response DTO
This prevents the domain from depending on transport objects.

Output Adapters
Output adapters connect the domain with external resources.

Examples:

Databases
Notification services
External APIs
Messaging systems
Current implementation:

Persistence
├── MySQL
└── MongoDB
MySQL Adapter
Responsible for relational persistence.

Components
Entities
Represent relational database tables.

Repositories
Implement persistence operations.

Mappers
Convert Domain Models into database entities.

Adapters
Implement Domain Output Ports.

MongoDB Adapter
Responsible for storing audit information.

Components
Documents
Represent MongoDB collections.

Repositories
Provide document persistence.

Mappers
Convert domain objects into MongoDB documents.

Adapters
Implement audit persistence ports.

Domain
The Domain layer is the core of the application.

It contains all business rules and must remain independent from any external technology.

No class inside the domain may depend on:

Spring
JPA
MongoDB
HTTP
REST
JSON
SQL
