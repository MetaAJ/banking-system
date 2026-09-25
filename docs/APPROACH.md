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

Both transaction references and account numbers have `@Column(unique = true, nullable = false)` mappings. Generation uses the respective `TXN` or `ACC` prefix followed by ten uppercase hexadecimal characters from a UUID. A pre-save lookup checks for existing values, while the database constraint provides the uniqueness safeguard. Retrying a uniqueness conflict during concurrent inserts is not currently implemented.

## API and persistence separation

Account and transaction response DTOs separate those API contracts from persistence entities. Customer endpoints still return `Customer` directly, and customer POST and PUT accept the entity. Customer PATCH uses `UpdateCustomerRequest`. Repositories provide common database operations through Spring Data JPA, while services own business behavior and entity-to-DTO conversion.

## Testing approach

The APIs are currently tested manually using Postman. The following scenarios were reported as manually tested. The application and tests were not executed during this documentation review.

| Area | Manual scenarios |
| --- | --- |
| Customer | Create, duplicate email, invalid data, retrieve, update, delete |
| Account | Create, retrieve one account, retrieve a customer's accounts |
| Deposit | Successful deposit, invalid amount, transaction creation |
| Withdrawal | Successful withdrawal, insufficient funds, invalid amount, transaction creation |
| Transfer | Successful transfer, insufficient funds, same source and destination, missing source, missing destination, transaction creation |
| Transaction history | Retrieve account transactions, deposit/withdrawal/transfer history, newest-first ordering |

An application-context smoke test (`BankApplicationTests.contextLoads`) exists. Automated business unit and integration tests are planned.

## Next engineering priorities

1. Enforce account-status rules before financial operations.
2. Add nonblank validation to transfer account numbers and appropriate validation for optional customer PATCH fields.
3. Introduce customer DTOs and consistent duplicate-email handling during updates. The explicit duplicate-email lookup currently applies to creation; updates rely on the database uniqueness constraint.
4. Define accepted decimal places, database precision/scale, and any rounding policy for monetary values. `BigDecimal` alone does not define those rules.
5. Add business tests for transfer rollback, concurrent balance updates, invalid amounts, and duplicate-email updates.

Entity validation and request validation are distinct: the current PATCH endpoint has no `@Valid`, and `UpdateCustomerRequest` has no validation annotations. Persistence-level constraints may still reject invalid customer values, but they do not use the existing request-validation error path.

## Concepts practiced

- **Java:** classes, objects, constructors, constructor overloading, records, enums, interfaces, and `BigDecimal`.
- **Spring:** dependency injection, beans, REST controllers, service layers, and `ResponseEntity`.
- **Persistence:** repository pattern, Spring Data JPA, Hibernate, entity relationships, `@ManyToOne`, `@JoinColumn`, lazy loading, database constraints, and derived JPA queries.
- **API design:** DTOs, Bean Validation, custom exceptions, and `@RestControllerAdvice`.
- **Consistency:** `@Transactional` and optimistic locking.
