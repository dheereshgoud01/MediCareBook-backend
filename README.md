# MediCareBook - Backend REST API & AI Service

Enterprise-grade clinical appointment reservation API built with **Java 21 Spring Boot 3.3.4**, **MySQL 8.0**, and **Spring Security**.

---

## 🛠️ Technology Stack
* **Language & Framework**: Java 21, Spring Boot 3.3.4
* **Database & ORM**: MySQL 8.0, Spring Data JPA, Hibernate 6
* **Security & Auth**: Spring Security 6, JWT (JSON Web Tokens), BCrypt Password Encoder
* **AI & Clinical Engine**: Integrated Rule-Grounded Triage & RAG Navigation Engine
* **Testing**: JUnit 5, Mockito (100% test pass rate)
* **Containerization**: Docker & Dockerfile

---

## 🚀 Getting Started

### Prerequisites
* **Java 21** or later (`java -version`)
* **MySQL 8.0** running locally or via Docker
* Git

### Database Setup
1. Create a MySQL database:
   ```sql
   CREATE DATABASE medicarebook_db;
   ```
2. Configure credentials in `src/main/resources/application.properties` (or via environment variables):
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/medicarebook_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=root
   ```

### Running Locally
```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```
The server will start on **`http://localhost:8000`**.

### Running Tests
```powershell
# Execute JUnit 5 & Mockito test suite
.\mvnw.cmd test
```

---

## 📡 Core API Endpoints

### 🔐 Authentication (`/api/auth`)
* `POST /api/auth/register` - Register Patient, Doctor (with specialization), or Admin.
* `POST /api/auth/login` - Authenticate and obtain JWT Bearer token.
* `GET /api/auth/users` - List all registered user accounts (Admin only).
* `PUT /api/auth/user/{id}` - Update user details or role (Admin only).
* `DELETE /api/auth/user/{id}` - Cascade delete user and associated records (Admin only).

### 🩺 Doctors & Appointments (`/api/doctors`)
* `GET /api/doctors/list` - Public verified doctor directory.
* `POST /api/doctors/apply` - Submit/update doctor application.
* `PUT /api/doctors/approve/{id}` - Approve doctor profile into active directory (Admin only).
* `POST /api/doctors/book` - Book appointment slot with double-booking prevention.
* `GET /api/doctors/history/user/{id}` - Patient appointment history.
* `GET /api/doctors/history/doctor/{id}` - Doctor appointment schedule.
* `PUT /api/doctors/appointment/{id}` - Update booking status (`APPROVED`, `COMPLETED`, `CANCELLED`).
* `DELETE /api/doctors/appointment/{id}` - Remove appointment record.

### 🤖 AI Healthcare Assistant (`/api/ai`)
* `POST /api/ai/chat` - Query symptom-to-specialty guidance, available practitioners, and policies.
* `GET /api/ai/health` - Health check status of AI navigation engine.
