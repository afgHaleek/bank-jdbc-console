## **Bank Transaction Processing System (Console App)**

#### _A console-based banking system built with Java 21, JDBC, and PostgreSQL, designed to demonstrate clean architecture, transaction safety, and real-world backend concepts such as row locking, atomic transfers, and audit trails._

###### **This project focuses on correctness and backend fundamentals, not UI.**

#### Features
##### Customers
* Create customers
* List customers with pagination

##### Accounts
* Open bank accounts for existing customers
* System-generated unique account numbers
* Prevents duplicate account types per customer
* (e.g. one CHECKING + one SAVINGS only)

##### Transactions

* Deposit money
* Withdraw money (with insufficient funds protection)
* Transfer money between accounts
* Atomic (all-or-nothing)
* Deadlock-safe row locking
* Check current balance
* View transaction statements:
* Last N transactions
* By date range

##### Audit / Ledger
* Every operation is recorded in a transactions table
* Transfers generate two ledger entries (OUT + IN) with the same transferId

#### Key Backend Concepts Demonstrated
* JDBC with PreparedStatements
* Connection pooling with HikariCP
* Manual transaction management (commit / rollback)
* Row-level locking (SELECT … FOR UPDATE)
* Deadlock prevention via deterministic lock ordering
* Service / DAO separation
* Input validation in service layer
* Console UI separated from business logic

#### Tech Stack
* Java 21
* PostgreSQL
* JDBC
* HikariCP
* Maven
* (Planned) JUnit 5 + Mockito

#### Project Structure
* src/main/java
* ├── app            # Console menus (UI layer)
* ├── service        # Business logic & validation
* ├── dao            # JDBC data access
* ├── model          # Domain models (records & enums)
* ├── util           # Helpers (input, selectors, etc.)
* ├── config         # DB & DataSource configuration

#### How to Run
##### Prerequisites
* Java 21+
* PostgreSQL
* Maven

##### Create Database
`CREATE DATABASE dbname;`

##### Configure Database
_Edit application.properties:_

`db.url=jdbc:postgresql://localhost:5432/dbname
db.username=postgres
db.password=your_password`

##### Run Schema
_Execute the provided SQL schema to create tables:_

* customers
* accounts
* transactions

##### Run Application
`mvn clean compile
mvn exec:java`

_Or run the Main class from your IDE._

#### Testing Status
* ❌ DAO integration tests (planned)
* ❌ Service unit tests (planned)
* Tests were intentionally postponed to focus on core system design and correctness.
* The architecture is designed to support:
* Service unit tests (Mockito)
* DAO integration tests (Testcontainers + PostgreSQL)

#### Future Improvements (Roadmap)
* Add unit tests for services
* Add DAO integration tests with Testcontainers
* Convert console app to server–client architecture
* Authentication & authorization
* Account freezing / unfreezing
* REST API + React frontend
* Better console UX (search, navigation, formatting)