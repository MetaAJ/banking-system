# API Reference

Base URL for local development: `http://localhost:8080`.

Use `Content-Type: application/json` for JSON request bodies. In the routes below, `accountId` refers to the internal database ID. Transfers identify accounts by their generated account numbers.

## Endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/status` | Basic application status message |
| POST | `/api/customers` | Create a customer |
| GET | `/api/customers` | Retrieve customers |
| GET | `/api/customers/{id}` | Retrieve a customer |
| PUT | `/api/customers/{id}` | Update customer details |
| PATCH | `/api/customers/{id}` | Update supplied customer fields |
| DELETE | `/api/customers/{id}` | Delete a customer |
| POST | `/api/accounts` | Create an account |
| GET | `/api/accounts?customerId={customerId}` | Retrieve a customer's accounts |
| GET | `/api/accounts/{accountId}` | Retrieve an account |
| POST | `/api/accounts/{accountId}/deposit` | Deposit money |
| POST | `/api/accounts/{accountId}/withdraw` | Withdraw money |
| POST | `/api/accounts/transfer` | Transfer money between accounts |
| GET | `/api/transactions/account/{accountId}` | Retrieve account transaction history |

## Customer requests

### Create a customer

`POST /api/customers` returns **201 Created** with the customer entity.

```json
{
  "name": "Alex Morgan",
  "email": "alex@example.com",
  "phone": "+919876543210"
}
```

Name is required and must contain 2–50 characters. Email is required, must be valid, and has a maximum length of 50 characters. Phone is required and must match `^\+[1-9]\d{7,14}$` (international format with a leading `+`). Omit the generated `id` when creating a customer.

### Update a customer

`PUT /api/customers/{id}` accepts the same fields as creation and returns **200 OK**. `PATCH /api/customers/{id}` accepts a subset:

```json
{
  "name": "Alex Taylor"
}
```

PATCH updates non-null fields and returns **200 OK**. Missing or null fields are left unchanged. The current PATCH endpoint does not apply request-level Bean Validation.

Customer reads and updates return `id`, `name`, `email`, and `phone`. The customer list returns an array. Successful deletion returns **204 No Content**.

## Create an account

`POST /api/accounts` returns **201 Created**. Both fields are required, and `customerId` must identify an existing customer.

```json
{
  "customerId": 1,
  "accountType": "SAVINGS"
}
```

Supported types are `SAVINGS` and `CURRENT`. New accounts start with a zero balance and `ACTIVE` status.

Account responses contain `id`, `accountNumber`, `accountType`, `balance`, `status`, and `customerId`. Account reads, deposits, and withdrawals return **200 OK**; the customer-account listing returns an array.

## Deposit

```http
POST /api/accounts/{accountId}/deposit
Content-Type: application/json
```

```json
{
  "amount": 1000.00
}
```

The application finds the account, validates the request, updates the balance, and creates a transaction record. The balance update and transaction record are committed atomically.

## Withdrawal

```http
POST /api/accounts/{accountId}/withdraw
Content-Type: application/json
```

```json
{
  "amount": 200.00
}
```

The application finds the account, validates the request, checks the available balance, deducts the amount, and creates a transaction record. Insufficient funds trigger an exception handled by the global exception handler.

## Transfer

```http
POST /api/accounts/transfer
Content-Type: application/json
```

```json
{
  "fromAccountNumber": "ACC123456789",
  "toAccountNumber": "ACC987654321",
  "amount": 300.00
}
```

Replace the illustrative account numbers with existing account numbers. The application resolves both accounts, rejects a transfer to the same account, checks available funds, debits the source, credits the destination, and creates a transaction record within one database transaction.

## Transaction history

```http
GET /api/transactions/account/{accountId}
```

Transactions are returned newest first using `TransactionResponse` DTOs. The response is an array with these fields: `transactionReference`, `transactionType`, `amount`, `fromAccountNumber`, `toAccountNumber`, `transactionStatus`, and `createdAt`. The internal transaction ID is not exposed. Deposits have a null source account number; withdrawals have a null destination account number.

Supported transaction types are `DEPOSIT`, `WITHDRAWAL`, and `TRANSFER`. The currently supported transaction status is `SUCCESS`.

## Response and lookup behavior

- Successful transfers return **200 OK** with `fromAccountNumber`, `toAccountNumber`, `amount`, and `fromAccountBalance`.
- Transaction history returns **200 OK**. The lookup does not check account existence separately, so an unknown account ID returns an empty array.
- The customer-account lookup similarly returns an empty array when no accounts match, including for an unknown customer ID.
- `GET /status` returns **200 OK** with `Banking API is running!`.

## Validation and errors

Jakarta Bean Validation checks request data. Custom exceptions cover duplicate or missing customers, missing accounts, insufficient funds, and invalid transfers. `GlobalExceptionHandler` centralizes HTTP error handling.

Optimistic locking failures return **HTTP 409 Conflict**.

| Condition handled by the application | HTTP status | Response body |
| --- | --- | --- |
| `CustomerAlreadyExistsException` | `409 Conflict` | Message string |
| `CustomerNotFoundException` | `404 Not Found` | Message string |
| `AccountNotFoundException` | `404 Not Found` | Message string |
| `InsufficientFundsException` | `409 Conflict` | Message string |
| `InvalidTransferException` | `400 Bad Request` | Message string |
| Optimistic locking failure | `409 Conflict` | Message string |
| Request Bean Validation failure | `400 Bad Request` | JSON object mapping fields to error messages |

Deposit, withdrawal, and transfer amounts are required and must be at least `0.01`. There is currently no request constraint limiting decimal places. Transfer account-number strings do not currently have nonblank constraints.

The explicit duplicate-email exception is raised during customer creation. PUT and PATCH do not perform the same duplicate-email lookup, and database constraint failures do not have a dedicated handler. Do not assume every duplicate-email failure returns the documented custom `409` response.

Financial operations currently do not check whether an account is `BLOCKED` or `CLOSED`. Authentication and authorization are planned.
