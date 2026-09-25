# API Reference

Base URL for local development: `http://localhost:8080`.

Use `Content-Type: application/json` for JSON request bodies. In the routes below, `accountId` refers to the internal database ID. Transfers identify accounts by their generated account numbers.

## Endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/customers` | Create a customer |
| GET | `/api/customers` | Retrieve customers |
| GET | `/api/customers/{id}` | Retrieve a customer |
| PUT | `/api/customers/{id}` | Update customer details |
| DELETE | `/api/customers/{id}` | Delete a customer |
| POST | `/api/accounts` | Create an account |
| GET | `/api/accounts?customerId={customerId}` | Retrieve a customer's accounts |
| GET | `/api/accounts/{accountId}` | Retrieve an account |
| POST | `/api/accounts/{accountId}/deposit` | Deposit money |
| POST | `/api/accounts/{accountId}/withdraw` | Withdraw money |
| POST | `/api/accounts/transfer` | Transfer money between accounts |
| GET | `/api/transactions/account/{accountId}` | Retrieve account transaction history |

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

Transactions are returned newest first using `TransactionResponse` DTOs. Records include an ID, reference, type, amount, source account, destination account, status, and creation timestamp.

Supported transaction types are `DEPOSIT`, `WITHDRAWAL`, and `TRANSFER`. The currently supported transaction status is `SUCCESS`.

## Validation and errors

Jakarta Bean Validation checks request data. Custom exceptions cover duplicate or missing customers, missing accounts, insufficient funds, and invalid transfers. `GlobalExceptionHandler` centralizes HTTP error handling.

Optimistic locking failures return **HTTP 409 Conflict**.

Exact customer and account creation payloads, response JSON schemas, and other error status mappings should be documented from the corresponding controllers and DTOs. They are not specified here.
