# Banking System Backend

A Spring Boot REST API for the banking system.

## Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.8+

## Setup

1. Create the MySQL database:
```sql
CREATE DATABASE banking_system;
```

2. Update `src/main/resources/application.properties` with your MySQL credentials.

3. Build and run:
```bash
mvn clean install
mvn spring-boot:run
```

## API Endpoints

### Users
- `POST /api/users/register` - Register a new user
- `POST /api/users/login` - User login
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `PUT /api/users/{id}/password` - Change password
- `PUT /api/users/{id}/status` - Update user status (Admin)
- `DELETE /api/users/{id}` - Delete user

### Accounts
- `POST /api/accounts` - Create account
- `GET /api/accounts/{id}` - Get account by ID
- `GET /api/accounts/number/{accountNumber}` - Get account by number
- `GET /api/accounts/user/{userId}` - Get all accounts for user
- `PUT /api/accounts/{id}/status` - Update account status
- `PUT /api/accounts/{id}/close` - Close account

### Transactions
- `POST /api/transactions/deposit` - Make a deposit
- `POST /api/transactions/withdraw` - Make a withdrawal
- `POST /api/transactions/transfer` - Transfer funds
- `GET /api/transactions/account/{accountId}` - Get account transactions
- `GET /api/transactions/account/{accountId}/range` - Get transactions by date range

### Loans
- `POST /api/loans/apply` - Apply for a loan
- `PUT /api/loans/{id}/approve` - Approve a loan
- `PUT /api/loans/{id}/reject` - Reject a loan
- `POST /api/loans/{id}/repay` - Repay a loan
- `GET /api/loans/account/{accountId}` - Get loans for account
- `GET /api/loans/status/{status}` - Get loans by status

### Admin
- `GET /api/admin/dashboard` - Dashboard statistics
- `GET /api/admin/users` - All users
- `GET /api/admin/accounts` - All accounts
- `GET /api/admin/loans/pending` - Pending loans
