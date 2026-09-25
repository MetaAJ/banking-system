# Banking System API

A REST API for customer management, bank accounts, deposits, withdrawals, transfers, and transaction history, built with **Java and Spring Boot**.

This is a hands-on backend engineering project developed incrementally to learn Java and Spring concepts while building a banking domain. Core banking operations are implemented; authentication, automated testing, persistent PostgreSQL storage, and production configuration are planned.

## Implemented features

- Customer CRUD operations, email uniqueness checks, and request validation.
- Multiple accounts per customer, with automatically generated account numbers.
- `SAVINGS` and `CURRENT` account types; `ACTIVE`, `BLOCKED`, and `CLOSED` account statuses.
- Deposits, withdrawals with balance checks, and transfers between accounts.
- Transaction records and account transaction history, returned newest first.
- DTO-based API models and centralized exception handling.
- Database transactions for balance-changing operations and optimistic locking for account updates.

## Technology stack

| Technology | Purpose |
| --- | --- |
| Java | Programming language |
| Spring Boot | Application framework |
| Spring Web | REST API |
| Spring Data JPA | Repository-based data access |
| Hibernate | Object-relational mapping |
| H2 | In-memory development database |
| Maven | Build and dependency management |
| Jakarta Bean Validation | Request validation |
| Postman | Manual API testing |

## Documentation

- [API reference](docs/API.md): endpoints, example requests, transaction history, and error handling.
- [Architecture](docs/ARCHITECTURE.md): application layers, domain model, transfer flow, DTOs, exceptions, and project structure.
- [Engineering approach](docs/APPROACH.md): design decisions, consistency, concurrency, manual testing, and learning goals.

## Run locally

### Prerequisites

- A JDK compatible with the version configured in `pom.xml`.
- Maven.
- Git.

Clone your repository and enter its directory. Replace both placeholders with your repository's actual values:

```sh
git clone <repository-url>
cd <repository-directory>
mvn spring-boot:run
```

The default local base URL is `http://localhost:8080`.

### Development database

The current H2 configuration is:

```properties
spring.datasource.url=jdbc:h2:mem:bankdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true

spring.h2.console.enabled=true
```

Data is lost when the application restarts because H2 runs in memory. Schema creation is configured with `ddl-auto=create`. PostgreSQL persistence and database migrations are planned.

## Testing

API behavior is currently checked manually using Postman. Coverage includes customer and account operations, successful and invalid financial requests, insufficient funds, missing accounts, and transaction history ordering. See the [testing approach](docs/APPROACH.md#testing-approach) for the scenario list.

Automated unit and integration tests are planned.

## Roadmap

### Implemented

- [x] Customer management
- [x] Account management
- [x] Deposits and withdrawals
- [x] Account transfers
- [x] Transaction recording and history
- [x] Request validation and global exception handling
- [x] Database transactions
- [x] Optimistic locking
- [x] DTO-based API

### Planned

- [ ] Authentication and JWT
- [ ] Authorization and roles
- [ ] Idempotency keys
- [ ] Pagination
- [ ] Transaction filtering and search
- [ ] Automated unit tests
- [ ] Integration tests
- [ ] PostgreSQL persistence
- [ ] Database migrations
- [ ] Swagger / OpenAPI documentation
- [ ] Docker
- [ ] Production configuration
- [ ] Advanced concurrency handling
- [ ] Audit logging
- [ ] Security hardening
