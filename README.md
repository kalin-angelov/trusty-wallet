:moneybag:
Trusty Wallet is a Spring Boot web application for managing digital wallets. It features user registration, wallet management, peer-to-peer transactions, monthly credit handling, and an admin dashboard. The front-end is built with Thymeleaf, and Spring Security handles authentication and authorization.

---

## 🛠 Tech Stack
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Thymeleaf
- H2 / MySQL
- Maven

---

## 👤 User Features
### Register/Login
Users register via /register.
- On success, 3 wallets are created:
- Wallet A: Active, €10 balance
- Wallet B & C: Inactive

### Wallet Management
- Activate or deactivate any wallet.

### Transactions
- Send money to other users.
- Choose source wallet.
- Validate sufficient balance and wallet status.
- View detailed transaction history with success/failure status.

### Charging Wallets
- Charge your wallet with a custom amount.
- This increases your credit liability for the month.

### Monthly Credit
- At the end of each month, calculate debt based on charged amount.
- Charge records are summarized in a user report.

---

## 🛡️ Admin Features
### User Management
- List all users and their status.
- Activate or deactivate any user.

### Reports
- System-wide transaction report.
- Wallet and balance summaries.
- Per-user usage reports.

### Admin Access
- Admin-only endpoints protected via Spring Security.

---

## 🧪 Testing
- Unit tests for services
- Integration tests for controllers
- API tests for MVC

### Tools:
- Mockito
- Spring Boot Test
