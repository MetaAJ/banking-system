# API Reference

Local base URL: `http://localhost:8080`. Send JSON bodies with `Content-Type: application/json`.

## Authentication

Only `/api/auth/**` is explicitly public. Other routes require a bearer token:

```http
Authorization: Bearer <token>
```

### Signup

`POST /api/auth/signup`

```json
{
  "name": "Alex Morgan",
  "email": "alex@example.com",
  "phone": "+919876543210",
  "password": "ExamplePass123!"
}
```

Signup creates a customer and a linked login account with the `CUSTOMER` role in one database transaction. Passwords are stored as BCrypt hashes. The current controller returns an empty **200 OK** response; signup does not return a token.

Validation: name 2–50 characters; valid email up to 50 characters; international phone matching `^\+[1-9]\d{7,14}$`; password 6–20 characters. All fields are required and nonblank.

### Login

`POST /api/auth/login`

```json
{
  "email": "alex@example.com",
  "password": "ExamplePass123!"
}
```

Successful login returns **200 OK** with the raw JWT string, not a JSON token object. Email must be valid and nonblank; password must be nonblank. Tokens contain the email subject, a role claim, issue time, and expiration time. Configured token lifetime is one hour.

No public endpoint creates an administrator. Admin routes require an existing user with the `ADMIN` role.

## Endpoints and current access rules

“Authenticated” means the security filter requires a token; additional ownership checks are listed separately. Admin users do not automatically bypass service ownership checks or satisfy routes restricted to `CUSTOMER`.

| Method | Route | Current access / behavior |
| --- | --- | --- |
| GET | `/status` | Authenticated; status text |
| POST | `/api/auth/signup` | Public; creates customer login |
| POST | `/api/auth/login` | Public; returns token |
| POST | `/api/customers` | Authenticated; creates customer entity only |
| GET | `/api/customers` | `ADMIN` |
| GET | `/api/customers/{id}` | Authenticated; ID must match caller's customer |
| PUT | `/api/customers/{id}` | Authenticated; ownership check not implemented |
| PATCH | `/api/customers/{id}` | Authenticated; ownership check not implemented |
| DELETE | `/api/customers/{id}` | Authenticated; ownership check not implemented |
| POST | `/api/accounts` | `CUSTOMER`; creates account for caller |
| POST | `/api/admin/accounts` | `ADMIN`; creates account for requested customer |
| GET | `/api/accounts?customerId={customerId}` | Authenticated; customer ID must match caller |
| GET | `/api/accounts/{accountNumber}` | Authenticated; account must belong to caller |
| POST | `/api/accounts/{accountNumber}/deposit` | `CUSTOMER`; account must belong to caller |
| POST | `/api/accounts/{accountNumber}/withdraw` | `CUSTOMER`; account must belong to caller |
| POST | `/api/accounts/transfer` | `CUSTOMER`; source must belong to caller |
| GET | `/api/transactions/account/{accountId}` | Authenticated; ownership check not implemented |

Account read/deposit/withdrawal routes now use **account numbers**, not internal IDs. Transaction history continues to use the internal account ID.

## Customers

`POST /api/customers` and `PUT /api/customers/{id}` accept:

```json
{
  "name": "Alex Morgan",
  "email": "alex@example.com",
  "phone": "+919876543210"
}
```

These fields follow the customer name, email, and phone constraints described for signup. POST returns **201 Created**; PUT returns **200 OK**. Direct customer creation does not create login credentials; use signup for customer onboarding.

PATCH accepts any subset of these fields, updates only non-null values, and returns **200 OK**. Request-level validation is not configured for PATCH. Reads return **200 OK**, and successful deletion returns **204 No Content**. Responses expose the customer entity fields `id`, `name`, `email`, and `phone`; list responses are arrays.

Changing a customer's email does not change the separate user login email. Deleting a customer with related records can encounter database constraints; there is no dedicated linked-record deletion policy or custom constraint-error handler.

## Create an account

Customer request: `POST /api/accounts`

```json
{
  "accountType": "SAVINGS"
}
```

The owner is resolved from the authenticated user's email. Do not include a `customerId` in this request.

Admin request: `POST /api/admin/accounts`

```json
{
  "customerId": 1,
  "accountType": "CURRENT"
}
```

Both return **201 Created** with an `AccountResponse`. Account type is required and accepts `SAVINGS` or `CURRENT`; the admin request also requires an existing customer ID. New accounts have zero balance and `ACTIVE` status.

Account responses contain `id`, `accountNumber`, `accountType`, `balance`, `status`, and `customerId`. Account reads return **200 OK**, and customer-account listings return arrays. An authorized customer with no accounts receives an empty array; a different requested customer ID is rejected.

## Deposit and withdrawal

```http
POST /api/accounts/{accountNumber}/deposit
POST /api/accounts/{accountNumber}/withdraw
```

Both accept:

```json
{
  "amount": 200.00
}
```

The amount is required and must be at least `0.01`. Both operations check ownership and reject `BLOCKED` or `CLOSED` accounts. Withdrawal also checks available funds. A successful operation records a transaction and returns **200 OK** with the updated `AccountResponse`.

## Transfer

`POST /api/accounts/transfer`

```json
{
  "fromAccountNumber": "ACC1234567890",
  "toAccountNumber": "ACC9876543210",
  "amount": 300.00
}
```

Replace the example account numbers with actual values. The source must belong to the caller; the destination can belong to another customer. Accounts must be different, neither may be blocked or closed, and the source must have sufficient funds. Debit, credit, and transaction recording run within one database transaction.

Success returns **200 OK** with `fromAccountNumber`, `toAccountNumber`, `amount`, and `fromAccountBalance`.

## Transaction history

```http
GET /api/transactions/account/1?page=0&size=10
GET /api/transactions/account/1?transactionType=TRANSFER&page=0&size=10
```

`page` is zero-based and `size` controls page size through Spring's `Pageable` binding. Optional `transactionType` accepts `DEPOSIT`, `WITHDRAWAL`, or `TRANSFER`. Repository queries explicitly order by `createdAt DESC`.

The endpoint returns **200 OK** with a Spring `Page<TransactionResponse>` rather than a plain array. Transaction items are in `content`; the page also carries pagination metadata. Exact serialized page metadata is framework-managed rather than a custom response contract.

Each item contains:

- `transactionReference`
- `transactionType`
- `amount`
- `fromAccountNumber` (null for deposits)
- `toAccountNumber` (null for withdrawals)
- `transactionStatus` (currently `SUCCESS`)
- `createdAt`

The internal transaction ID is not exposed. There is no separate account-existence check; an unmatched account ID yields an empty page. Ownership enforcement is not implemented for this endpoint.

## Errors and validation limits

| Handled condition | Status | Body |
| --- | --- | --- |
| Explicit duplicate-customer exception | `409 Conflict` | Message string |
| Missing customer/user or account exception | `404 Not Found` | Message string |
| Insufficient funds | `409 Conflict` | Message string |
| Blocked or closed account | `409 Conflict` | Message string |
| Optimistic locking failure | `409 Conflict` | Message string |
| Same-account transfer | `400 Bad Request` | Message string |
| Request Bean Validation failure | `400 Bad Request` | Field-to-message JSON object |
| Service ownership rejection | `403 Forbidden` | Message string |

JWT authentication and endpoint-role failures are handled by Spring Security, separately from these domain handlers. No custom login-failure response contract is defined in the global exception handler.

Transfer account-number strings have no nonblank constraints. Monetary requests have no decimal-place limit. Customer updates lack explicit duplicate-email checks, and database constraint failures have no dedicated handler. The custom duplicate-customer `409` contract therefore does not cover every possible uniqueness failure.
