# Banking System

A complete banking system built with **Java Spring Boot** (backend) and **HTML/CSS/JavaScript** (frontend). Supports both **H2 in-memory** (zero-config development) and **MySQL** (production) databases.

---

## Table of Contents

- [Project Structure](#project-structure)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Setup Guide](#setup-guide)
  - [1. Clone the Repository](#1-clone-the-repository)
  - [2. Install Java 17+](#2-install-java-17)
  - [3. Install Maven](#3-install-maven)
  - [4. Configure the Database](#4-configure-the-database)
  - [5. Build and Run the Backend](#5-build-and-run-the-backend)
  - [6. Serve the Frontend](#6-serve-the-frontend)
- [Default Credentials](#default-credentials)
- [API Overview](#api-overview)
- [Tech Stack](#tech-stack)
- [Troubleshooting](#troubleshooting)

---

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
│   │   ├── config/                   # Security & CORS configuration
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

---

## Features

### User Features
- **Authentication** — Register and login with bcrypt-encrypted passwords
- **Account Management** — Create SAVINGS, CHECKING, or FIXED_DEPOSIT accounts
- **Transactions** — Deposit, withdraw, and transfer funds between accounts
- **Loan Management** — Apply for PERSONAL, HOME, AUTO, EDUCATION, or BUSINESS loans; track repayments
- **Statements** — View transaction history with date-range filtering and CSV export
- **Profile** — Update personal info and change password

### Admin Features
- Dashboard with system-wide statistics
- User management (activate, suspend accounts)
- Account management and oversight
- Loan approval / rejection workflow

---

## Prerequisites

| Software | Minimum Version | Required For |
|----------|----------------|--------------|
| **Java (JDK)** | 17 | Backend (Spring Boot) |
| **Maven** | 3.8+ | Building and running the backend |
| **MySQL** | 8.0+ | Production database (optional — H2 is used by default) |
| **Node.js** | 14+ | Serving the frontend via `npx http-server` (optional) |
| **Git** | Any | Cloning the repository |

> **Tip:** If you don't have MySQL installed, the project is pre-configured to use an **H2 in-memory database** out of the box — no database setup needed.

---

## Setup Guide

### 1. Clone the Repository

```bash
git clone https://github.com/VoGuEz/banking-system.git
cd banking-system
```

---

### 2. Install Java 17+

You need a Java Development Kit (JDK) version 17 or higher.

#### Windows

1. Download the JDK from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [Adoptium (Eclipse Temurin)](https://adoptium.net/).
2. Run the installer. During installation, check **"Set JAVA_HOME variable"** if the option is available.
3. **Set environment variables manually** (if not done by the installer):
   - Open **Settings → System → About → Advanced system settings → Environment Variables**.
   - Under **System variables**, click **New**:
     - Variable name: `JAVA_HOME`
     - Variable value: `C:\Program Files\Java\jdk-17` (adjust to your install path)
   - Edit the **Path** variable and add: `%JAVA_HOME%\bin`
4. Verify:
   ```powershell
   java -version
   ```

#### macOS

Using [Homebrew](https://brew.sh/):
```bash
brew install openjdk@17
```
Add to your shell profile (`~/.zshrc` or `~/.bash_profile`):
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
```
Reload and verify:
```bash
source ~/.zshrc
java -version
```

#### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install openjdk-17-jdk -y
```
Verify:
```bash
java -version
```

For other distros (Fedora, Arch, etc.):
```bash
# Fedora
sudo dnf install java-17-openjdk-devel

# Arch
sudo pacman -S jdk17-openjdk
```

---

### 3. Install Maven

#### Windows

1. Download the **Binary zip archive** from [Apache Maven](https://maven.apache.org/download.cgi) (e.g., `apache-maven-3.9.6-bin.zip`).
2. Extract to a directory, e.g., `C:\tools\apache-maven-3.9.6`.
3. **Set environment variables:**
   - Add a new system variable:
     - Variable name: `MAVEN_HOME`
     - Variable value: `C:\tools\apache-maven-3.9.6`
   - Edit the **Path** variable and add: `%MAVEN_HOME%\bin`
4. Verify:
   ```powershell
   mvn -version
   ```

#### macOS

```bash
brew install maven
mvn -version
```

#### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install maven -y
mvn -version
```

For other distros:
```bash
# Fedora
sudo dnf install maven

# Arch
sudo pacman -S maven
```

---

### 4. Configure the Database

The project supports two database modes:

#### Option A: H2 In-Memory Database (Default — No Setup Needed)

The application is pre-configured to use H2. No installation or configuration is required. Simply proceed to step 5.

The current `backend/src/main/resources/application.properties` is set to:
```properties
spring.datasource.url=jdbc:h2:mem:banking_system;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
```

> **Note:** H2 is in-memory — all data is lost when the backend stops. This is ideal for development and testing.

You can browse the database at **http://localhost:8080/h2-console** while the app is running (JDBC URL: `jdbc:h2:mem:banking_system`, user: `sa`, no password).

#### Option B: MySQL (Persistent / Production)

1. **Install MySQL 8.0+:**

   | Platform | Command / Download |
   |----------|-------------------|
   | Windows | Download from [mysql.com/downloads](https://dev.mysql.com/downloads/installer/) and run the installer |
   | macOS | `brew install mysql && brew services start mysql` |
   | Ubuntu/Debian | `sudo apt install mysql-server -y && sudo systemctl start mysql` |
   | Fedora | `sudo dnf install mysql-server && sudo systemctl start mysqld` |

2. **Create the database:**
   ```bash
   mysql -u root -p
   ```
   ```sql
   CREATE DATABASE banking_system;
   EXIT;
   ```
   Optionally, run the full schema:
   ```bash
   mysql -u root -p banking_system < backend/src/main/resources/schema.sql
   ```

3. **Update `backend/src/main/resources/application.properties`:**
   Replace the H2 configuration block with:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/banking_system?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
   ```
   Or use environment variables to avoid hardcoding credentials:
   ```bash
   # Linux / macOS
   export DB_URL="jdbc:mysql://localhost:3306/banking_system?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
   export DB_USERNAME="root"
   export DB_PASSWORD="your_password"
   ```
   ```powershell
   # Windows (PowerShell)
   $env:DB_URL = "jdbc:mysql://localhost:3306/banking_system?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
   $env:DB_USERNAME = "root"
   $env:DB_PASSWORD = "your_password"
   ```
   Then update `application.properties` to use the variables:
   ```properties
   spring.datasource.url=${DB_URL}
   spring.datasource.username=${DB_USERNAME}
   spring.datasource.password=${DB_PASSWORD}
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
   ```

4. **Remove or comment out the H2 console lines** in `application.properties`:
   ```properties
   # spring.h2.console.enabled=true
   # spring.h2.console.path=/h2-console
   ```

---

### 5. Build and Run the Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

You should see output ending with:
```
Started BankingSystemApplication in X.XX seconds
```

The REST API is now available at **http://localhost:8080**.

**Quick smoke test:**
```bash
curl http://localhost:8080/api/admin/dashboard
```

> **Windows PowerShell users:** If `mvn` is not recognized, make sure Maven's `bin` directory is in your `PATH` (see step 3) or run:
> ```powershell
> $env:PATH = "C:\tools\apache-maven-3.9.6\bin;$env:PATH"
> mvn spring-boot:run
> ```

---

### 6. Serve the Frontend

The frontend is a static site that calls the backend API at `http://localhost:8080`. You can serve it in several ways:

#### Option A: Using Node.js / npx (Recommended)

```bash
cd frontend
npx -y http-server -p 5500 --cors
```
Open **http://localhost:5500** in your browser.

#### Option B: Using Python

```bash
cd frontend

# Python 3
python -m http.server 5500

# Python 2
python -m SimpleHTTPServer 5500
```
Open **http://localhost:5500** in your browser.

#### Option C: Using VS Code Live Server Extension

1. Install the **Live Server** extension in VS Code.
2. Right-click `frontend/index.html` → **Open with Live Server**.
3. It will open automatically at `http://127.0.0.1:5500`.

#### Option D: Open Directly in Browser

Simply double-click `frontend/index.html`. Note: some browsers may block API requests from `file://` URLs due to CORS. If you experience issues, use one of the server-based options above.

> **CORS Note:** The backend is configured to accept requests from `http://localhost:5500`, `http://127.0.0.1:5500`, and `http://localhost:3000`. If you serve the frontend on a different port, update the allowed origins in `backend/src/main/java/com/bankingsystem/config/SecurityConfig.java`.

---

## Default Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@bank.com` | `admin123` |

> The admin user is created automatically when using MySQL with `schema.sql`. When using H2, register a new user through the UI and promote them via the H2 console if needed.

---

## API Overview

| Resource | Endpoint | Methods |
|----------|----------|---------|
| Users | `/api/users` | Register, Login, Get, Update, Delete |
| Accounts | `/api/accounts` | Create, Get by user/number, Update status, Close |
| Transactions | `/api/transactions` | Deposit, Withdraw, Transfer, History |
| Loans | `/api/loans` | Apply, Approve, Reject, Repay, History |
| Admin | `/api/admin` | Dashboard stats, User/Account/Loan management |

See [backend/README.md](backend/README.md) for the full API reference.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17+, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Database | H2 (development) / MySQL 8.0 (production), Hibernate ORM |
| Password Security | BCrypt (strength 12) |
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Build | Maven |

---

## Troubleshooting

### `mvn` is not recognized

Maven is not in your system `PATH`. See [Install Maven](#3-install-maven) for your platform. On Windows you can temporarily set it:
```powershell
$env:PATH = "C:\path\to\apache-maven-3.x.x\bin;$env:PATH"
```

### `java` is not recognized

JDK is not installed or not in `PATH`. See [Install Java 17+](#2-install-java-17) for your platform.

### Port 8080 is already in use

Another application is using port 8080. Either stop that application or change the port in `application.properties`:
```properties
server.port=8081
```
If you change the backend port, also update `API_BASE_URL` in `frontend/js/script.js`:
```javascript
const API_BASE_URL = 'http://localhost:8081';
```

### CORS errors in the browser console

The frontend's origin must be listed in the backend's CORS configuration. Edit `backend/src/main/java/com/bankingsystem/config/SecurityConfig.java` and add your origin to the `setAllowedOrigins` list.

### Lombok compilation errors with newer Java versions (21+)

If you see "cannot find symbol" errors for getters/setters, update Lombok to the latest version in `pom.xml`:
```xml
<properties>
    <lombok.version>1.18.36</lombok.version>
</properties>
```

### H2 database data is lost after restart

This is expected — H2 runs in-memory by default. To persist data across restarts, switch to a file-based H2 URL:
```properties
spring.datasource.url=jdbc:h2:file:./data/banking_system;DB_CLOSE_DELAY=-1
```
Or switch to MySQL for full persistence (see [Option B: MySQL](#option-b-mysql-persistent--production)).