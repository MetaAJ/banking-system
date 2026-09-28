# Engineering Approach

## Project goal

Develop a banking backend incrementally while learning Java, Spring, persistence, validation, concurrency, and security. The current implementation now includes identity-aware account operations and paginated history alongside core financial operations.

## Money and consistency

Amounts use `BigDecimal` to avoid binary floating-point representation errors. Request constraints require amounts of at least `0.01`; account balances have a nonnegative entity constraint. Accepted decimal places, explicit database precision/scale, currency, and rounding policy remain undefined.

Deposits, withdrawals, and transfers use `@Transactional`. Balance changes and successful transaction recording form one database unit of work, with rollback governed by the transaction rules. A financial `Transaction` entity is distinct from the database transaction that groups writes.

Accounts use `@Version` to detect stale writes. Conceptually, updates include the previously read version in the predicate. A conflicting update can fail instead of overwriting a newer balance; supported locking failures map to HTTP 409. Automatic retries and idempotency keys are not implemented.

Generated account numbers and transaction references are checked for existing values before saving and also have unique column mappings. The database constraint is the final uniqueness safeguard; automatic retry of concurrent uniqueness conflicts is not implemented.

## Identity and access decisions

Signup creates customer and login records atomically. BCrypt stores password hashes. Login uses Spring Security's authentication provider, and JWTs carry the authenticated email and role.

Customer account creation derives ownership from the authenticated user. A separate admin route allows account creation for a specified customer. Account reads and financial operations compare the source/target account owner to the caller where applicable. Transfers require ownership of the source but allow a destination owned by another customer.

Financial operations now reject blocked and closed accounts. A transfer checks both accounts. Status-change endpoints are not yet implemented.

Authorization remains incomplete: customer PUT/PATCH/DELETE and transaction-history retrieval require authentication but do not verify ownership. Direct customer creation is also available to any authenticated user. These rules should be completed before describing the API as enforcing ownership throughout.

## API boundaries and validation

Account and transaction responses use DTOs. Customer POST/PUT and responses still expose the customer entity. Request Bean Validation covers authentication DTOs, account creation, financial amounts, and customer POST/PUT fields.

Transfer account-number strings lack nonblank constraints. Customer PATCH has neither request constraints nor `@Valid`; non-null values are applied directly. Persistence validation may reject some values, but it does not use the same request-validation error path.

Explicit duplicate-email checks exist for signup and direct customer creation. Customer updates rely on database constraints instead, without a dedicated constraint-error handler. Updating `Customer.email` does not synchronize `UserAccount.email`, so profile and login email can diverge.

## Transaction history

Repository JPQL queries select transactions by source or destination account ID, optionally constrain transaction type, and order by descending creation time. Spring Data pagination is propagated to the response with `Page.map`.

This avoids loading all matching records into a response list. The endpoint currently uses internal account IDs, unlike account operations that use generated account numbers. It does not check account existence or caller ownership separately.

## Test status

| Test source | Intended coverage | Current status |
| --- | --- | --- |
| `BankApplicationTests` | Spring application context loads | Present; not executed during documentation review |
| `deposit_shouldIncreaseBalance` | Deposit updates balance and records transaction | Uses obsolete numeric-ID service call |
| `withdraw_shouldDecreaseBalance` | Withdrawal updates balance and records transaction | Uses obsolete numeric-ID service call |
| `withdraw_shouldThrowException_whenInsufficientFunds` | Withdrawal rejects insufficient balance | Uses obsolete numeric-ID service call |
| `transfer_shouldUpdateBalance` | Transfer updates both balances and records transaction | Needs authenticated-user setup |
| `transfer_shouldThrowException_whenSameAccount` | Same-account transfer rejection | Needs authenticated-user setup |
| `transfer_shouldThrowException_whenInsufficientFunds` | Transfer rejects insufficient balance | Needs authenticated-user setup |

The numeric-ID calls are incompatible with the current string account-number signatures. The service tests also omit the `UserAccountRepository` mock and security context now used by account verification. Test fixtures should include valid owners, account numbers, and statuses. This source review does not claim successful test execution.

Recommended next coverage includes signup/login, role restrictions, cross-customer access rejection, blocked/closed accounts, transaction pagination/filtering, atomic rollback, optimistic locking, and duplicate-email updates. Mockito tests alone do not demonstrate real database rollback or concurrency behavior; add integration tests for those guarantees.

## Next priorities

1. Complete ownership enforcement for customer mutation and history routes; decide which customer-management actions are admin-only.
2. Align and run the existing tests, then add security and database integration coverage.
3. Add customer DTOs, PATCH validation, transfer account-number constraints, and consistent error responses.
4. Define profile/login email synchronization and linked-record deletion behavior.
5. Define monetary precision, currency, and rounding rules; introduce idempotency for financial writes.
6. Externalize the JWT signing secret, explicitly define session policy, and design token refresh/revocation and admin provisioning.
7. Add PostgreSQL, migrations, OpenAPI, Docker, and production configuration.

The source currently defines `jwt.secret` in application properties. Use an external configuration value for deployments rather than publishing or reusing that value. No signing secret is reproduced in this documentation.

## Concepts practiced

- Java classes, constructors, records, enums, interfaces, and `BigDecimal`.
- Dependency injection, REST controllers, service layers, and response entities.
- JPA relationships, lazy loading, repository queries, DTO mapping, and pagination.
- Bean Validation, custom exceptions, transactions, and optimistic locking.
- Authentication providers, BCrypt, JWT claims, role mapping, and ownership authorization.
