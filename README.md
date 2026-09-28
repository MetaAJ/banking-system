# Banking System API

A Java Spring Boot REST API for customer management, bank accounts, deposits, withdrawals, transfers, and transaction history, with JWT authentication and role-based access rules.

This project is developed incrementally to learn backend engineering through a banking domain. Authentication, account ownership checks, and financial operations are implemented; authorization coverage, test maintenance, and production readiness remain active work.

## Features

- Customer signup and login, BCrypt password hashing, and JWT bearer tokens.
- `CUSTOMER` and `ADMIN` roles with endpoint-specific access rules.
- Customer management and multiple accounts per customer.
- Customer account creation using the authenticated identity; separate admin account creation for a specified customer.
- `SAVINGS` and `CURRENT` accounts, created with a zero balance and `ACTIVE` status.
- Account ownership checks for account reads, deposits, withdrawals, and transfer sources.
- Deposits, withdrawals, and transfers that reject blocked or closed accounts.
- Atomic financial operations, transaction records, and optimistic locking.
- Paginated transaction history with optional transaction-type filtering and newest-first queries.
- Request validation, custom exceptions, and account/transaction response DTOs.

## Documentation

- [API reference](docs/API.md): authentication, access rules, requests, responses, pagination, and errors.
- [Architecture](docs/ARCHITECTURE.md): layers, security flow, entities, and project structure.
- [Engineering approach](docs/APPROACH.md): design decisions, test status, limitations, and next steps.

## Technology stack

| Technology | Purpose |
| --- | --- |
| Java 27 | Java version configured in `pom.xml` |
| Spring Boot 4.1.1 | Application framework version configured in `pom.xml` |
| Spring Web MVC | REST endpoints |
| Spring Security / OAuth2 Resource Server | Authentication, JWT validation, and endpoint authorization |
| Spring Data JPA / Hibernate | Persistence and ORM |
| H2 | In-memory development database |
| Jakarta Bean Validation | Request and entity constraints |
| Maven / Maven wrapper | Build and dependency management |
| JUnit Jupiter / Mockito | Existing automated test source |
| Postman | Manual API exploration and testing |

## Run locally

Prerequisites: Git and JDK 27, matching the current POM. Use the included Maven wrapper or an installed Maven distribution.

Clone the repository and enter its directory:

```sh
git clone https://github.com/MetaAJ/banking-system.git
cd banking-system
```

Set `JWT_SECRET` to a private, randomly generated signing secret of at least 32 bytes. Spring's environment configuration can override the existing `jwt.secret` property. The configured `jwt.expiration` is `3600000` milliseconds (one hour); `JWT_EXPIRATION` can override it. Do not commit your signing secret.

Run on Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```sh
./mvnw spring-boot:run
```

With Maven installed, the equivalent command is `mvn spring-boot:run`.

The default local base URL is `http://localhost:8080`.

### First API requests

1. Sign up through `POST /api/auth/signup`.
2. Log in through `POST /api/auth/login` and copy the raw token returned in the response body.
3. Send `Authorization: Bearer <token>` on subsequent requests.
4. Create an account with `POST /api/accounts` and `{"accountType":"SAVINGS"}`.
5. Use the returned `accountNumber` for reads, deposits, and withdrawals. Transaction history still uses the internal account `id`.

See the [API examples](docs/API.md) for complete payloads. `/status` also requires authentication. Signup creates only `CUSTOMER` users; there is no implemented public admin-signup or admin-bootstrap flow.

### Database configuration

The application uses `jdbc:h2:mem:bankdb`, with `spring.jpa.hibernate.ddl-auto=create` and SQL logging enabled. Data, including registered users, is lost on restart. PostgreSQL and schema migrations are planned.

The H2 console property is enabled, but the security configuration does not explicitly permit console access or configure it for browser use.

## Tests

The repository contains a Spring Boot context smoke test and six Mockito account-service tests for balance updates and transfer failures.

**The service tests need updating:** deposit and withdrawal tests still pass numeric IDs while the service now accepts account-number strings. The tests also lack the authenticated-user setup and `UserAccountRepository` mock required by the current service. They should not be described as passing tests for the current implementation.

After updating the tests, run:

```powershell
.\mvnw.cmd test
```

Or use `./mvnw test` on macOS/Linux. This documentation was checked against source code; the application and tests were not executed as part of the documentation update.

## Current limitations

- Ownership checks are not yet applied to transaction-history reads or customer PUT, PATCH, and DELETE operations. Those routes require authentication but are not fully scoped to the caller.
- Customer endpoints still expose the `Customer` entity. Customer email updates do not synchronize the separate login email in `UserAccount`.
- Transfer account-number fields and customer PATCH input need stronger request validation.
- Monetary decimal-place rules and explicit database precision/scale are not defined.
- There are no account-status management endpoints, token refresh/revocation endpoints, or admin provisioning flow.

## Roadmap

- [x] Customer and account management
- [x] Deposits, withdrawals, transfers, and transaction recording
- [x] Database transactions and optimistic locking
- [x] Account and transaction response DTOs
- [x] Signup, login, BCrypt, and JWT authentication
- [x] Role-based endpoint rules and account ownership checks
- [x] Blocked/closed account checks for financial operations
- [x] Transaction pagination and type filtering
- [ ] Complete ownership enforcement across customer and transaction APIs
- [ ] Update existing service tests and expand security/integration coverage
- [ ] Customer DTOs, consistent validation, and email-update rules
- [ ] Monetary precision and rounding policy
- [ ] Idempotency keys
- [ ] Admin provisioning and account-status management
- [ ] Token lifecycle improvements
- [ ] PostgreSQL and database migrations
- [ ] Swagger / OpenAPI
- [ ] Docker and production configuration
- [ ] Audit logging and further security hardening
