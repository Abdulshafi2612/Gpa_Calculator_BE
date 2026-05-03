<![CDATA[<div align="center">

# 📊 GPA Calculator REST API

**A secure, production-grade backend system for academic GPA and CGPA management**

Built with Spring Boot · Secured with JWT · Tested with JUnit 5

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://swagger.io/)
[![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![Mockito](https://img.shields.io/badge/Mockito-Testing-78A641?style=for-the-badge)](https://site.mockito.org/)

---

*What started as a simple GPA calculator evolved into a fully architected, secure backend system — complete with JWT authentication, refresh tokens, per-user data isolation, centralized GPA/CGPA computation, Swagger documentation, and comprehensive unit testing.*

</div>

---

## 📑 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture Overview](#-architecture-overview)
- [Authentication Flow](#-authentication-flow)
- [GPA / CGPA Calculation Logic](#-gpa--cgpa-calculation-logic)
- [API Endpoints](#-api-endpoints)
- [Example API Requests & Responses](#-example-api-requests--responses)
- [Swagger Documentation](#-swagger-documentation)
- [Database Design](#-database-design)
- [Testing](#-testing)
- [How to Run Locally](#-how-to-run-locally)
- [Configuration](#-configuration)
- [Key Design Decisions](#-key-design-decisions)
- [What I Learned](#-what-i-learned)
- [Future Improvements](#-future-improvements)
- [Author](#-author)

---

## ✨ Features

### 🔐 Authentication & Security
- User registration and login with email/password
- Password hashing with **BCrypt**
- **JWT access token** generation and validation
- **Refresh token** support with database persistence
- Stateless authentication via **Spring Security**
- Protected routes with automatic current-user extraction from JWT
- Public endpoints explicitly whitelisted; all others require authentication

### 📚 Semester Management
- Full **CRUD** operations for semesters
- Each semester is scoped to the authenticated user — strict ownership enforcement
- Semester sequence auto-generation when not provided
- Duplicate semester sequence prevention per user
- Cascade creation, update, and deletion of subjects within semesters

### 📝 Subject / Course Management
- Subjects are nested within semesters
- Each subject has a **name**, **grade**, **credit**, and **sequence**
- Subject sequence uniqueness enforced per semester
- Lightweight semester list endpoint (without subjects) for performance

### 📈 GPA & CGPA Calculation
- **Backend-driven** GPA computation (migrated from frontend)
- Semester GPA calculated as weighted average of grade points
- CGPA calculated as credit-weighted average across all semesters
- Subjects with grade `"-"` are excluded from all calculations
- User totals automatically recalculated on every semester create, update, and delete

### 🛡️ Error Handling
- Global exception handling with `@RestControllerAdvice`
- Custom exceptions: `EmailAlreadyExistsException`, `InvalidCredentialsException`, `DuplicateSemesterSequenceException`
- Consistent, meaningful HTTP status codes (`201`, `200`, `204`, `400`, `401`, `404`, `409`)

### 📖 API Documentation
- Interactive **Swagger UI** with Springdoc OpenAPI
- JWT Bearer authorization configured directly in Swagger
- Full schema and endpoint documentation

### 🧪 Unit Testing
- Service-layer testing with **JUnit 5** and **Mockito**
- All business logic tested in isolation — no database required
- Coverage across authentication, GPA calculation, semester operations, and user services

---

## 🛠️ Tech Stack

| Category | Technology |
|:---|:---|
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.0 |
| **Web** | Spring Web (MVC) |
| **Security** | Spring Security, JWT (jjwt 0.13), BCrypt |
| **Database** | PostgreSQL |
| **ORM** | Spring Data JPA, Hibernate |
| **Mapping** | MapStruct 1.6 |
| **Validation** | Jakarta Validation (Bean Validation) |
| **Boilerplate** | Lombok |
| **Documentation** | Springdoc OpenAPI (Swagger UI) |
| **Testing** | JUnit 5, Mockito |
| **Build Tool** | Maven |

---

## 🏗️ Architecture Overview

The project follows a **clean layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│                     Client Request                      │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│               🛡️  Security Layer                        │
│         JwtAuthenticationFilter · SecurityConfig         │
│           CustomUserDetailsService · JwtService          │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│               🎮  Controller Layer                      │
│    AuthController · SemesterController · UserController  │
│                    GpaController                         │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                📦  DTO Layer                            │
│           Request DTOs ←→ Response DTOs                  │
│                                                         │
│               🔄  Mapper Layer                          │
│     RegisterMapper · SemesterMapper · SubjectMapper      │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│               ⚙️  Service Layer                         │
│   AuthServiceImpl · SemesterServiceImpl · UserServiceImpl│
│   GpaCalculatorServiceImpl · RefreshTokenServiceImpl     │
│              CurrentUserServiceImpl                      │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│              💾  Repository Layer                       │
│  UserRepository · SemesterRepository · SubjectRepository │
│                RefreshTokenRepository                    │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│              🗄️  Database (PostgreSQL)                  │
│       users · semesters · subjects · refresh_tokens      │
└─────────────────────────────────────────────────────────┘
```

**Exception Handling** spans across all layers via `@RestControllerAdvice` in the `GlobalExceptionHandler`, catching custom and framework exceptions and returning structured error responses.

---

## 🔐 Authentication Flow

```
┌──────────┐         ┌──────────┐         ┌──────────┐
│  Client  │         │  Server  │         │    DB    │
└────┬─────┘         └────┬─────┘         └────┬─────┘
     │  POST /register    │                    │
     │───────────────────►│  Hash password     │
     │                    │───────────────────►│ Save user
     │◄───────────────────│  201 UserResponse  │
     │                    │                    │
     │  POST /login       │                    │
     │───────────────────►│  Verify password   │
     │                    │  Generate JWT      │
     │                    │  Generate Refresh  │
     │                    │───────────────────►│ Save token
     │◄───────────────────│  AuthResponse      │
     │  {accessToken,     │  (JWT + Refresh)   │
     │   refreshToken}    │                    │
     │                    │                    │
     │  GET /api/semesters│                    │
     │  Authorization:    │                    │
     │  Bearer <JWT>      │                    │
     │───────────────────►│  Validate JWT      │
     │                    │  Extract user      │
     │                    │───────────────────►│ Query data
     │◄───────────────────│  200 Response      │
     │                    │                    │
     │  POST /auth/refresh│                    │
     │  {refreshToken}    │                    │
     │───────────────────►│  Validate token    │
     │                    │  Generate new JWT  │
     │◄───────────────────│  New AuthResponse  │
```

**Public Endpoints** (no authentication required):
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `/swagger-ui/**`
- `/v3/api-docs/**`

**Protected Endpoints** require the header:
```
Authorization: Bearer <accessToken>
```

---

## 📈 GPA / CGPA Calculation Logic

The GPA calculation engine was migrated from the Flutter frontend to the backend, centralizing all academic computation in `GpaCalculatorService`.

### Grade Point Mapping

| Grade | Points | | Grade | Points |
|:-----:|:------:|---|:-----:|:------:|
| A+    | 4.0    | | C+    | 2.3    |
| A     | 4.0    | | C     | 2.0    |
| A-    | 3.7    | | C-    | 1.7    |
| B+    | 3.3    | | D+    | 1.3    |
| B     | 3.0    | | D     | 1.0    |
| B-    | 2.7    | | F     | 0.0    |

> **Note:** Subjects with grade `"-"` are completely excluded from GPA and credit calculations.

### Formulas

**Semester GPA:**
```
Semester GPA = Σ(gradePoint × credit) / Σ(credits)
```

**CGPA (Cumulative GPA):**
```
CGPA = Σ(semesterGpa × semesterCredits) / Σ(semesterCredits)
```

CGPA is a **credit-weighted average** — not a simple average of semester GPAs — ensuring semesters with more credits contribute proportionally more.

### Recalculation Triggers
User totals (`totalGpa`, `totalCredits`) are automatically recalculated whenever a semester is **created**, **updated**, or **deleted**.

---

## 📡 API Endpoints

### Authentication

| Method | Endpoint | Description | Auth |
|:------:|:---------|:------------|:----:|
| `POST` | `/api/auth/register` | Register a new user | ❌ |
| `POST` | `/api/auth/login` | Login and receive tokens | ❌ |
| `POST` | `/api/auth/refresh` | Refresh access token | ❌ |

### User

| Method | Endpoint | Description | Auth |
|:------:|:---------|:------------|:----:|
| `GET` | `/api/users/me` | Get current authenticated user | ✅ |

### Semesters

| Method | Endpoint | Description | Auth |
|:------:|:---------|:------------|:----:|
| `POST` | `/api/semesters` | Create a new semester with subjects | ✅ |
| `GET` | `/api/semesters` | Get all semesters (lightweight, no subjects) | ✅ |
| `GET` | `/api/semesters/{id}` | Get a semester by ID (includes subjects) | ✅ |
| `PUT` | `/api/semesters/{id}` | Update a semester (replaces subjects) | ✅ |
| `DELETE` | `/api/semesters/{id}` | Delete a semester and its subjects | ✅ |

### CGPA

| Method | Endpoint | Description | Auth |
|:------:|:---------|:------------|:----:|
| `GET` | `/api/cgpa` | Get cumulative GPA, total credits, and semester count | ✅ |

---

## 📋 Example API Requests & Responses

### 1. Register

**Request:**
```http
POST /api/auth/register
Content-Type: application/json
```
```json
{
  "name": "Mohamed",
  "email": "mohamed@test.com",
  "password": "12345678"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "Mohamed",
  "email": "mohamed@test.com"
}
```

---

### 2. Login

**Request:**
```http
POST /api/auth/login
Content-Type: application/json
```
```json
{
  "email": "mohamed@test.com",
  "password": "12345678"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "d4f8a1b2-3c4d-5e6f-7a8b-9c0d1e2f3a4b"
}
```

---

### 3. Refresh Token

**Request:**
```http
POST /api/auth/refresh
Content-Type: application/json
```
```json
{
  "refreshToken": "d4f8a1b2-3c4d-5e6f-7a8b-9c0d1e2f3a4b"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "d4f8a1b2-3c4d-5e6f-7a8b-9c0d1e2f3a4b"
}
```

---

### 4. Get Current User

**Request:**
```http
GET /api/users/me
Authorization: Bearer <accessToken>
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Mohamed",
  "email": "mohamed@test.com"
}
```

> Password hash is **never** included in API responses.

---

### 5. Create Semester

**Request:**
```http
POST /api/semesters
Authorization: Bearer <accessToken>
Content-Type: application/json
```
```json
{
  "sequence": 1,
  "subjects": [
    {
      "name": "Math",
      "grade": "A",
      "credit": 3,
      "sequence": 1
    },
    {
      "name": "Physics",
      "grade": "B+",
      "credit": 3,
      "sequence": 2
    }
  ]
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "sequence": 1,
  "semesterGpa": 3.65,
  "semesterCredits": 6,
  "subjects": [
    {
      "id": 1,
      "name": "Math",
      "grade": "A",
      "credit": 3,
      "sequence": 1
    },
    {
      "id": 2,
      "name": "Physics",
      "grade": "B+",
      "credit": 3,
      "sequence": 2
    }
  ]
}
```

---

### 6. Get All Semesters

**Request:**
```http
GET /api/semesters
Authorization: Bearer <accessToken>
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "sequence": 1,
    "semesterGpa": 3.65,
    "semesterCredits": 6
  },
  {
    "id": 2,
    "sequence": 2,
    "semesterGpa": 3.40,
    "semesterCredits": 9
  }
]
```

> The list endpoint returns **lightweight summaries** without subject details for performance.

---

### 7. Get Semester by ID

**Request:**
```http
GET /api/semesters/1
Authorization: Bearer <accessToken>
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "sequence": 1,
  "semesterGpa": 3.65,
  "semesterCredits": 6,
  "subjects": [
    {
      "id": 1,
      "name": "Math",
      "grade": "A",
      "credit": 3,
      "sequence": 1
    },
    {
      "id": 2,
      "name": "Physics",
      "grade": "B+",
      "credit": 3,
      "sequence": 2
    }
  ]
}
```

---

### 8. Update Semester

**Request:**
```http
PUT /api/semesters/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```
```json
{
  "sequence": 1,
  "subjects": [
    {
      "name": "Math",
      "grade": "A+",
      "credit": 3,
      "sequence": 1
    },
    {
      "name": "Physics",
      "grade": "A",
      "credit": 3,
      "sequence": 2
    },
    {
      "name": "Chemistry",
      "grade": "B",
      "credit": 4,
      "sequence": 3
    }
  ]
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "sequence": 1,
  "semesterGpa": 3.70,
  "semesterCredits": 10,
  "subjects": [
    {
      "id": 3,
      "name": "Math",
      "grade": "A+",
      "credit": 3,
      "sequence": 1
    },
    {
      "id": 4,
      "name": "Physics",
      "grade": "A",
      "credit": 3,
      "sequence": 2
    },
    {
      "id": 5,
      "name": "Chemistry",
      "grade": "B",
      "credit": 4,
      "sequence": 3
    }
  ]
}
```

> Updating a semester **replaces** all its subjects entirely.

---

### 9. Delete Semester

**Request:**
```http
DELETE /api/semesters/1
Authorization: Bearer <accessToken>
```

**Response:** `204 No Content`

> Deleting a semester also deletes all associated subjects and recalculates the user's CGPA.

---

### 10. Get CGPA

**Request:**
```http
GET /api/cgpa
Authorization: Bearer <accessToken>
```

**Response:** `200 OK`
```json
{
  "cgpa": 3.65,
  "totalCredits": 6,
  "semesterCount": 1
}
```

---

## 📖 Swagger Documentation

Interactive API documentation is available via **Swagger UI** once the application is running:

| Resource | URL |
|:---------|:----|
| **Swagger UI** | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |
| **OpenAPI JSON** | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |

Swagger is configured with **JWT Bearer authentication** — click the 🔒 **Authorize** button, paste your access token, and test protected endpoints directly from the browser.

---

## 🗄️ Database Design

### Entity Relationship Diagram

```
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│      users       │       │    semesters      │       │     subjects     │
├──────────────────┤       ├──────────────────┤       ├──────────────────┤
│ id          PK   │       │ id          PK   │       │ id          PK   │
│ name             │       │ sequence         │       │ name             │
│ email     UNIQUE │  1:N  │ semester_gpa     │  1:N  │ grade            │
│ password_hash    │◄─────►│ semester_credits │◄─────►│ credit           │
│ total_gpa        │       │ user_id     FK   │       │ sequence         │
│ total_credits    │       └──────────────────┘       │ semester_id FK   │
└──────┬───────────┘       UNIQUE(user_id,            └──────────────────┘
       │                          sequence)            UNIQUE(semester_id,
       │ 1:1                                                  sequence)
       ▼
┌──────────────────┐
│  refresh_tokens  │
├──────────────────┤
│ id          PK   │
│ token     UNIQUE │
│ expiry_date      │
│ user_id     FK   │
└──────────────────┘
```

### Entity Relationships

| Relationship | Type | Description |
|:-------------|:----:|:------------|
| User → Semester | `1:N` | A user can have many semesters |
| Semester → Subject | `1:N` | A semester contains many subjects |
| User → RefreshToken | `1:1` | A user has one refresh token |

### Constraints

- `users.email` — unique
- `(user_id, sequence)` on semesters — unique per user
- `(semester_id, sequence)` on subjects — unique per semester
- Cascade delete: deleting a user removes all semesters, subjects, and refresh tokens

---

## 🧪 Testing

Unit tests are written with **JUnit 5** and **Mockito**, targeting the service layer to verify business logic in complete isolation from infrastructure (database, HTTP, etc.).

### Tested Services

| Test Class | Coverage |
|:-----------|:---------|
| `AuthServiceImplTest` | Registration, login, password hashing, token generation, refresh flow |
| `RefreshTokenServiceImplTest` | Token creation, validation, expiry handling |
| `UserServiceImplTest` | Current user retrieval, user lookup |
| `CurrentUserServiceImplTest` | JWT-based user extraction from security context |
| `GpaCalculatorServiceImplTest` | Grade-to-point mapping, GPA calculation, CGPA aggregation, edge cases |
| `SemesterServiceImplTest` | CRUD operations, ownership validation, sequence handling, cascade behavior |

### Running Tests

```bash
./mvnw test
```

All tests use **mocked dependencies** — no PostgreSQL instance or application context is required.

---

## 🚀 How to Run Locally

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+** (or use the included Maven Wrapper)
- **PostgreSQL** installed and running

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/your-username/GPA_Calculator.git
cd GPA_Calculator
```

**2. Create a PostgreSQL database**
```sql
CREATE DATABASE gpa;
```

**3. Configure `application.properties`**

Update `src/main/resources/application.properties` with your database credentials (see [Configuration](#-configuration) below).

**4. Run the application**
```bash
./mvnw spring-boot:run
```

**5. Open Swagger UI**

Navigate to [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## ⚙️ Configuration

Create or update `src/main/resources/application.properties`:

```properties
# Application
spring.application.name=GPA_Calculator

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/gpa
spring.datasource.username=postgres
spring.datasource.password=your_password

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
app.jwt.secret=your_secure_secret_key_here
app.jwt.expiration-ms=86400000
```

> ⚠️ **Important:** Never commit real credentials. Use environment variables or a `.env` file for production secrets.

---

## 🎯 Key Design Decisions

| Decision | Rationale |
|:---------|:----------|
| **DTOs over Entities** | API responses never expose internal entity structure or sensitive fields like password hashes |
| **MapStruct for mapping** | Compile-time code generation for type-safe, performant object mapping — no reflection overhead |
| **Stateless JWT authentication** | No server-side session storage; horizontally scalable by design |
| **Refresh tokens in database** | Enables token revocation and persistent session management |
| **Ownership checks at repository level** | Queries like `findByIdAndUserId` prevent data leaks at the lowest possible layer |
| **Centralized GPA calculation** | All computation logic lives in `GpaCalculatorService`, keeping controllers and semester service focused |
| **Separated CRUD and calculation** | `SemesterService` handles data operations; `GpaCalculatorService` handles math — single responsibility |
| **Mockito-based unit tests** | Service logic is tested in isolation without database or Spring context, keeping tests fast and focused |
| **Swagger with JWT support** | Developers can authenticate and test protected endpoints directly from the browser |

---

## 📚 What I Learned

This project was a comprehensive exercise in backend engineering. Key takeaways include:

- **Designing a secure authentication system** from scratch using Spring Security, JWT, and refresh tokens — understanding the full lifecycle of stateless authentication
- **Architecting a layered backend** with proper separation between controllers, services, repositories, DTOs, and mappers
- **Implementing data isolation** where users can only access their own resources, enforced at the query level
- **Migrating business logic** from a mobile frontend (Flutter) to the backend, understanding why server-side computation improves consistency and security
- **Writing meaningful unit tests** that validate business rules without relying on infrastructure — learning to think in terms of behavior, not implementation
- **Handling edge cases** in GPA calculation: zero-credit semesters, ignored grades, weighted CGPA across unequal semesters
- **Configuring Swagger** for JWT-protected APIs, making the system self-documenting and easy to explore
- **Using MapStruct with Lombok** — understanding annotation processor ordering and the `lombok-mapstruct-binding` to make them work together seamlessly

---

## 🔮 Future Improvements

- [ ] Improve validation error response format with field-level detail
- [ ] Add integration tests with Testcontainers and PostgreSQL
- [ ] Containerize with Docker and `docker-compose`
- [ ] Add database migrations with Flyway or Liquibase
- [ ] Implement role-based authorization (e.g., admin, student)
- [ ] Build and connect a frontend client
- [ ] Set up CI/CD pipeline (GitHub Actions)
- [ ] Add rate limiting and request throttling
- [ ] Implement password reset flow

---

## 👤 Author

**Mohamed Abdul Shafi**

[![Email](https://img.shields.io/badge/Email-mohamedsadik763%40gmail.com-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:mohamedsadik763@gmail.com)

---

<div align="center">

*Built with dedication and a passion for clean backend architecture.*

⭐ If you found this project helpful, consider giving it a star!

</div>
]]>
