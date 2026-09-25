# Engineering Approach

## Project goal

Build the banking API incrementally, learning the Java and Spring concepts behind each feature. The longer-term goal is to evolve the application into a more production-oriented backend with security, durable persistence, automated tests, and stronger operational support.

## Money handling

Financial amounts use `BigDecimal` instead of `double` or `float` to avoid binary floating-point precision issues. Conceptually, Java's `BigDecimal` plays a similar role to Python's `Decimal`.

## Request validation

The application uses Jakarta Bean Validation annotations such as `@NotNull`, `@NotBlank`, `@Email`, `@Size`, `@Pattern`, and `@DecimalMin`.

For example:

```java
public record DepositRequest(
    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal amount
) {}
```

Request validation handles input constraints, while services implement business rules such as sufficient funds and different source and destination accounts. Errors are handled centrally through `@RestControllerAdvice`.

## Atomic balance changes

Deposits, withdrawals, and transfers use `@Transactional` so their database changes can commit together or roll back under the configured rollback rules.

For a transfer:

```text
Debit source + Credit destination + Create transaction record
                              ↓
                         Single commit
```

The application-level `Transaction` entity records a financial operation. It is distinct from the database transaction that makes the related writes atomic.

## Optimistic locking

Accounts carry a JPA version field:

```java
@Version
private Long version;
```

Conceptually, Hibernate updates an account with a version check:

```sql
UPDATE accounts
SET balance = ?, version = ?
WHERE id = ? AND version = ?;
```

If another transaction has changed the row, the stale update can fail instead of silently overwriting the newer balance. The API maps optimistic locking failures to **409 Conflict**. Advanced concurrency handling remains on the roadmap.

## Transaction references

Each financial transaction receives a reference such as `TXN7A91C42F10`, separate from its internal database ID. The application checks whether a reference already exists before saving it.

A pre-save lookup alone does not guarantee uniqueness under concurrent writes. A database uniqueness constraint is the appropriate safeguard; the supplied project description does not establish whether that constraint is present for transaction references.

## API and persistence separation

Request and response DTOs keep JPA entities out of the public API contract. Repositories provide common database operations through Spring Data JPA, while services own business behavior and entity-to-DTO conversion.

## Testing approach

The APIs are currently tested manually using Postman. The project description lists the following scenarios as tested; this documentation does not represent a new execution of those checks.

| Area | Manual scenarios |
| --- | --- |
| Customer | Create, duplicate email, invalid data, retrieve, update, delete |
| Account | Create, retrieve one account, retrieve a customer's accounts |
| Deposit | Successful deposit, invalid amount, transaction creation |
| Withdrawal | Successful withdrawal, insufficient funds, invalid amount, transaction creation |
| Transfer | Successful transfer, insufficient funds, same source and destination, missing source, missing destination, transaction creation |
| Transaction history | Retrieve account transactions, deposit/withdrawal/transfer history, newest-first ordering |

Automated unit and integration tests are planned.

## Concepts practiced

- **Java:** classes, objects, constructors, constructor overloading, records, enums, interfaces, and `BigDecimal`.
- **Spring:** dependency injection, beans, REST controllers, service layers, and `ResponseEntity`.
- **Persistence:** repository pattern, Spring Data JPA, Hibernate, entity relationships, `@ManyToOne`, `@JoinColumn`, lazy loading, database constraints, and derived JPA queries.
- **API design:** DTOs, Bean Validation, custom exceptions, and `@RestControllerAdvice`.
- **Consistency:** `@Transactional` and optimistic locking.
