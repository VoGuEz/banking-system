# Banking System

A complete banking system built with **Java Spring Boot** (backend) and **HTML/CSS/JavaScript** (frontend), backed by **MySQL**.

## Project Structure

```
banking-system/
├── backend/                          # Spring Boot REST API
│   ├── src/main/java/com/bankingsystem/
│   │   ├── model/                    # JPA entities (User, Account, Transaction, Loan, AuditLog)
│   │   ├── service/                  # Business logic layer
│   │   ├── controller/               # REST API controllers
│   │   ├── repository/               # Spring Data JPA repositories
│   │   ├── util/                     # Utility classes (password encoder, ID generators)
│   │   ├── exception/                # Custom exceptions and global handler
│   │   ├── config/                   # Security configuration
│   │   └── BankingSystemApplication.java
│   ├── src/main/resources/
│   │   ├── application.properties    # App configuration (DB, JPA, logging)
│   │   └── schema.sql                # MySQL schema with initial data
│   ├── pom.xml
│   └── README.md
├── frontend/                         # HTML/CSS/JS user interface
│   ├── index.html                    # Login / Registration
│   ├── dashboard.html                # Account overview & recent transactions
│   ├── transactions.html             # Deposit & Withdrawal
│   ├── transfer.html                 # Fund transfers between accounts
│   ├── loans.html                    # Loan application & management
│   ├── statements.html               # Transaction history & statements
│   ├── profile.html                  # User profile & password change
│   ├── admin.html                    # Admin dashboard & management
│   ├── css/
│   │   ├── style.css                 # Main stylesheet with dark mode support
│   │   └── responsive.css            # Mobile-responsive breakpoints
│   └── js/
│       └── script.js                 # Shared JS utilities & API helpers
└── README.md
```

## Features

### User Features
- **Authentication**: Register and login with bcrypt-encrypted passwords
- **Account Management**: Create SAVINGS, CHECKING, or FIXED_DEPOSIT accounts
- **Transactions**: Deposit, withdraw, and transfer funds between accounts
- **Loan Management**: Apply for PERSONAL, HOME, AUTO, EDUCATION, or BUSINESS loans; track repayments
- **Statements**: View transaction history with date-range filtering and CSV export
- **Profile**: Update personal info and change password

### Admin Features
- Dashboard with system-wide statistics
- User management (activate, suspend accounts)
- Account management and oversight
- Loan approval / rejection workflow

## Quick Start

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.8+

### 1. Set up the database
```sql
CREATE DATABASE banking_system;
```
Or run `backend/src/main/resources/schema.sql` to initialize the schema and seed the default admin user.

### 2. Configure the backend

Edit `backend/src/main/resources/application.properties` (or set environment variables):

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:mysql://localhost:3306/banking_system?...` | JDBC URL |
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | `password` | MySQL password |

### 3. Run the backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
The API will be available at `http://localhost:8080`.

### 4. Open the frontend
Open `frontend/index.html` in your browser.

**Default admin credentials:**
- Email: `admin@bank.com`
- Password: `admin123`

## API Overview

| Resource | Endpoint | Methods |
|----------|----------|---------|
| Users | `/api/users` | Register, Login, Get, Update, Delete |
| Accounts | `/api/accounts` | Create, Get by user/number, Update status, Close |
| Transactions | `/api/transactions` | Deposit, Withdraw, Transfer, History |
| Loans | `/api/loans` | Apply, Approve, Reject, Repay, History |
| Admin | `/api/admin` | Dashboard stats, User/Account/Loan management |

See `backend/README.md` for the full API reference.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Database | MySQL 8.0, Hibernate ORM |
| Password Security | BCrypt (strength 12) |
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Build | Maven |