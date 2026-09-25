# Banking System API

A REST API for customer management, bank accounts, deposits, withdrawals, transfers, and transaction history, built with **Java and Spring Boot**.

This is a hands-on backend engineering project developed incrementally to learn Java and Spring concepts while building a banking domain. Core banking operations are implemented; authentication, automated business tests, persistent PostgreSQL storage, and production configuration are planned.

## Implemented features

- Customer CRUD operations, email uniqueness checks, and request validation.
- Multiple accounts per customer, with automatically generated account numbers.
- `SAVINGS` and `CURRENT` account types. Accounts are created with `ACTIVE` status; `BLOCKED` and `CLOSED` are modeled but not yet enforced by financial operations.
- Deposits, withdrawals with balance checks, and transfers between accounts.
- Transaction records and account transaction history, returned newest first.
- Account and transaction response DTOs, plus centralized exception handling. Customer endpoints currently return the customer entity.
- Database transactions for balance-changing operations and optimistic locking for account updates.

## Technology stack

| Technology | Purpose |
| --- | --- |
| Java 27 | Configured Java version in `pom.xml` |
| Spring Boot 4.1.1 | Configured application framework version |
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

- JDK 27, matching the version configured in `pom.xml`.
- Git.
- Use the included Maven wrapper, or install Maven separately.

The versions above reflect the build configuration; they are not a claim that the application was built or tested during documentation review.

Clone the repository and enter its directory:

```sh
git clone https://github.com/MetaAJ/banking-system.git
cd banking-system
```

Run with the included Maven wrapper on Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```sh
./mvnw spring-boot:run
```

Alternatively, with Maven installed, use `mvn spring-boot:run`.

The default local base URL is `http://localhost:8080`.

Check `GET /status` after startup. It returns:

```text
Banking API is running!
```

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

An application-context smoke test (`BankApplicationTests.contextLoads`) exists. Automated business unit and integration tests are planned.

Run the existing test with `./mvnw test` or `.\mvnw.cmd test` on Windows.

## Current limitations

- Authentication and authorization are not implemented.
- Financial operations do not yet enforce account status.
- Customer APIs still expose the persistence entity.
- Transfer account-number fields and customer PATCH requests need stronger request validation.
- Monetary request scale and database precision/scale are not explicitly defined.
- H2 data is temporary and is lost on restart.

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
- [x] Account and transaction response DTOs

### Planned

- [ ] Account-status enforcement
- [ ] Customer request and response DTOs
- [ ] Consistent customer update validation and duplicate-email handling
- [ ] Monetary precision and decimal-place rules

- [ ] Authentication and JWT
- [ ] Authorization and roles
- [ ] Idempotency keys
- [ ] Pagination
- [ ] Transaction filtering and search
- [ ] Automated business unit tests
- [ ] Integration tests
- [ ] PostgreSQL persistence
- [ ] Database migrations
- [ ] Swagger / OpenAPI documentation
- [ ] Docker
- [ ] Production configuration
- [ ] Advanced concurrency handling
- [ ] Audit logging
- [ ] Security hardening
