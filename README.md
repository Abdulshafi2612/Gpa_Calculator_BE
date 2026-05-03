<div align="center">

# 📊 GPA Calculator REST API

### A secure backend system for academic GPA and CGPA management

Built with **Spring Boot** · Secured with **JWT** · Powered by **PostgreSQL** · Deployed on **Azure** · Tested with **JUnit 5**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Azure](https://img.shields.io/badge/Deployed%20on-Azure-0078D4?style=for-the-badge&logo=microsoftazure&logoColor=white)](https://azure.microsoft.com/)
[![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://swagger.io/)
[![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![Mockito](https://img.shields.io/badge/Mockito-Testing-78A641?style=for-the-badge)](https://site.mockito.org/)

</div>

---

## 📌 Overview

**GPA Calculator REST API** is a secure Spring Boot backend for managing academic semesters, subjects, GPA, CGPA, and semester activation status.

What started as a simple GPA calculator evolved into a complete backend system with JWT authentication, refresh tokens, per-user data isolation, centralized GPA/CGPA calculation, Swagger documentation, service-layer unit testing, and cloud deployment on Microsoft Azure.

The API is currently deployed as a Java 21 Spring Boot application on **Azure App Service**, connected to **Azure Database for PostgreSQL Flexible Server**.

---

## 🌐 Live Deployment

### Production API Base URL

```text
https://gpa-calculator-api-mohamed-d5e8gqcgcffcg9gj.germanywestcentral-01.azurewebsites.net
```

### Production Swagger UI

```text
https://gpa-calculator-api-mohamed-d5e8gqcgcffcg9gj.germanywestcentral-01.azurewebsites.net/swagger-ui/index.html
```

### Production OpenAPI JSON

```text
https://gpa-calculator-api-mohamed-d5e8gqcgcffcg9gj.germanywestcentral-01.azurewebsites.net/v3/api-docs
```

### Deployment Stack

| Component | Service |
|---|---|
| Backend hosting | Azure App Service |
| Runtime | Java 21 SE / Embedded Web Server |
| Database | Azure Database for PostgreSQL Flexible Server |
| Deployment artifact | Spring Boot executable JAR |
| API documentation | Swagger UI / OpenAPI |
| Authentication | JWT Bearer tokens |

### Flutter Base URL

For the Flutter app, the backend URL should point to the deployed Azure API:

```dart
class ApiConstants {
  ApiConstants._();

  static const String baseUrl =
      'https://gpa-calculator-api-mohamed-d5e8gqcgcffcg9gj.germanywestcentral-01.azurewebsites.net';
}
```

> Use the Azure HTTPS URL for mobile devices, Android emulators, and real devices. `10.0.2.2` is only needed when the backend is running locally on the development machine.

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Live Deployment](#-live-deployment)
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
- [Deployment on Azure](#-deployment-on-azure)
- [Configuration](#-configuration)
- [Key Design Decisions](#-key-design-decisions)
- [What I Learned](#-what-i-learned)
- [Future Improvements](#-future-improvements)
- [Author](#-author)

---

## ✨ Features

### 🔐 Authentication & Security

- User registration and login with email and password
- Password hashing with **BCrypt**
- JWT access token generation and validation
- Refresh token support with database persistence
- Stateless authentication using **Spring Security**
- Protected routes with automatic current-user extraction from JWT
- Public endpoints explicitly whitelisted; all other routes require authentication

### 👤 User Management

- Retrieve the currently authenticated user using `GET /api/users/me`
- Password hashes are never exposed in API responses
- User data is protected and isolated per account
- User totals are stored and updated after GPA-impacting operations

### 📚 Semester Management

- Full CRUD operations for semesters
- Each semester belongs to the authenticated user
- Users can only access, update, and delete their own semesters
- Ownership is enforced through repository-level checks such as `findByIdAndUserId`
- Semester sequence can be manually provided or automatically generated
- Duplicate semester sequences are prevented per user
- Updating a semester replaces its subjects
- Deleting a semester also deletes its subjects
- Semesters can be activated or deactivated without being deleted
- Inactive semesters remain stored and visible, but are excluded from CGPA calculation

### 🔄 Semester Activation Toggle

- Users can toggle a semester between active and inactive states
- Toggle endpoint uses `PATCH /api/semesters/{id}/toggle-active`
- No request body is required
- The endpoint flips the current `active` value
- Inactive semesters do not affect:
  - CGPA
  - total credits
  - active semester count
- This allows users to exclude repeated, optional, or experimental semesters without permanently deleting academic records

### 📝 Subject / Course Management

- Subjects are saved under semesters
- Each subject has:
  - `name`
  - `grade`
  - `credit`
  - `sequence`
- Subject sequence is unique inside the same semester
- Single-semester responses include full subject details
- The all-semesters endpoint returns lightweight summaries without subjects

### 📈 GPA & CGPA Calculation

- GPA logic is handled by the backend instead of the frontend
- Grade letters are converted to grade points in `GpaCalculatorService`
- Semester GPA is calculated as a weighted average
- CGPA is calculated as a credit-weighted average across **active semesters only**
- Inactive semesters are excluded from CGPA, total credits, and semester count
- Subjects with grade `"-"` are ignored in GPA and credits calculation
- User totals are recalculated after semester create, update, delete, and activation status changes

### ☁️ Cloud Deployment

- Backend deployed to **Azure App Service**
- Runtime configured as **Java 21 SE**
- Application deployed as a Spring Boot executable JAR
- Production database hosted on **Azure Database for PostgreSQL Flexible Server**
- Secrets and database credentials are stored as Azure App Service environment variables
- Swagger UI is publicly available for testing the deployed API

### 🛡️ Error Handling

- Global exception handling using `@RestControllerAdvice`
- Custom exceptions for common business errors
- Meaningful HTTP status codes such as:
  - `201 Created`
  - `200 OK`
  - `204 No Content`
  - `400 Bad Request`
  - `401 Unauthorized`
  - `404 Not Found`
  - `409 Conflict`

### 📖 API Documentation

- Interactive Swagger UI using Springdoc OpenAPI
- JWT Bearer authorization configured directly in Swagger
- Controllers, endpoints, and schemas are documented automatically
- Swagger available locally and on the deployed Azure API

### 🧪 Unit Testing

- Service-layer unit tests using **JUnit 5** and **Mockito**
- Tests use mocked dependencies and do not require a real database
- Business logic is tested separately from infrastructure

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
| Web | Spring Web MVC |
| Security | Spring Security, JWT, BCrypt |
| Database | PostgreSQL |
| Cloud Database | Azure Database for PostgreSQL Flexible Server |
| Cloud Hosting | Azure App Service |
| ORM | Spring Data JPA, Hibernate |
| Mapping | MapStruct |
| Validation | Jakarta Validation |
| Boilerplate | Lombok |
| Documentation | Springdoc OpenAPI / Swagger UI |
| Testing | JUnit 5, Mockito |
| Build Tool | Maven |
| Deployment Artifact | Executable JAR |

---

## 🏗️ Architecture Overview

The project follows a layered architecture with clear separation of responsibilities.

```text
Client Request
      |
      v
Security Layer
JwtAuthenticationFilter, SecurityConfig, JwtService
      |
      v
Controller Layer
AuthController, SemesterController, UserController, GpaController
      |
      v
DTO + Mapper Layer
Request DTOs, Response DTOs, MapStruct Mappers
      |
      v
Service Layer
AuthService, SemesterService, GpaCalculatorService, UserService
      |
      v
Repository Layer
UserRepository, SemesterRepository, SubjectRepository, RefreshTokenRepository
      |
      v
PostgreSQL Database
```

### Deployed Architecture

```text
Flutter App / Swagger / Postman
      |
      v
Azure App Service
Spring Boot REST API
      |
      v
Azure Database for PostgreSQL Flexible Server
```

---

## 🔐 Authentication Flow

1. The user registers with name, email, and password.
2. The password is hashed using BCrypt before saving.
3. The user logs in with email and password.
4. The backend validates credentials.
5. The backend returns:
   - `accessToken`
   - `refreshToken`
6. Protected endpoints require:

```http
Authorization: Bearer <accessToken>
```

7. When the access token expires, the client can request a new one using the refresh token.

### Public Endpoints

| Method | Endpoint |
|---|---|
| POST | `/api/auth/register` |
| POST | `/api/auth/login` |
| POST | `/api/auth/refresh` |
| GET | `/swagger-ui/**` |
| GET | `/v3/api-docs/**` |

---

## 📈 GPA / CGPA Calculation Logic

The GPA calculation logic was moved from the Flutter frontend to the backend to make the system more reliable and consistent.

### Grade Mapping

| Grade | Points |
|---|---:|
| A+ | 4.0 |
| A | 4.0 |
| A- | 3.7 |
| B+ | 3.3 |
| B | 3.0 |
| B- | 2.7 |
| C+ | 2.3 |
| C | 2.0 |
| C- | 1.7 |
| D+ | 1.3 |
| D | 1.0 |
| F | 0.0 |
| - | Ignored |

### Semester GPA

```text
Semester GPA = Σ(gradePoint × credit) / Σ(credits)
```

Subjects with grade `"-"` are ignored in both grade points and credits.

### CGPA

```text
CGPA = Σ(activeSemesterGpa × activeSemesterCredits) / Σ(activeSemesterCredits)
```

CGPA is a **credit-weighted average**, not a simple average of semester GPAs.

Only active semesters are included in CGPA calculation. Deactivated semesters remain stored and visible in semester endpoints, but they do not affect CGPA, total credits, or active semester count.

---

## 📡 API Endpoints

### Base URLs

| Environment | Base URL |
|---|---|
| Local | `http://localhost:8080` |
| Android Emulator Local Backend | `http://10.0.2.2:8080` |
| Production Azure API | `https://gpa-calculator-api-mohamed-d5e8gqcgcffcg9gj.germanywestcentral-01.azurewebsites.net` |

### Authentication

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new user | No |
| POST | `/api/auth/login` | Login and receive tokens | No |
| POST | `/api/auth/refresh` | Refresh access token | No |

### User

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| GET | `/api/users/me` | Get current authenticated user | Yes |

### Semesters

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| POST | `/api/semesters` | Create a semester with subjects | Yes |
| GET | `/api/semesters` | Get all semesters as summaries | Yes |
| GET | `/api/semesters/{id}` | Get semester details with subjects | Yes |
| PUT | `/api/semesters/{id}` | Update semester subjects | Yes |
| PATCH | `/api/semesters/{id}/toggle-active` | Toggle semester active status | Yes |
| DELETE | `/api/semesters/{id}` | Delete semester and subjects | Yes |

### CGPA

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| GET | `/api/cgpa` | Get CGPA, total active credits, and active semester count | Yes |

---

## 📋 Example API Requests & Responses

### Register

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

### Login

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

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "refresh-token-value"
}
```

### Refresh Token

```http
POST /api/auth/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "refresh-token-value"
}
```

### Get Current User

```http
GET /api/users/me
Authorization: Bearer <accessToken>
```

```json
{
  "id": 1,
  "name": "Mohamed",
  "email": "mohamed@test.com",
  "totalGpa": 3.65,
  "totalCredits": 6
}
```

### Create Semester

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

```json
{
  "id": 1,
  "sequence": 1,
  "semesterGpa": 3.65,
  "semesterCredits": 6,
  "active": true,
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

### Get All Semesters

```http
GET /api/semesters
Authorization: Bearer <accessToken>
```

```json
[
  {
    "id": 1,
    "sequence": 1,
    "semesterGpa": 3.65,
    "semesterCredits": 6,
    "active": true
  }
]
```

### Get Semester By ID

```http
GET /api/semesters/1
Authorization: Bearer <accessToken>
```

```json
{
  "id": 1,
  "sequence": 1,
  "semesterGpa": 3.65,
  "semesterCredits": 6,
  "active": true,
  "subjects": [
    {
      "id": 1,
      "name": "Math",
      "grade": "A",
      "credit": 3,
      "sequence": 1
    }
  ]
}
```

### Update Semester

```http
PUT /api/semesters/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "subjects": [
    {
      "name": "Algorithms",
      "grade": "A",
      "credit": 3,
      "sequence": 1
    },
    {
      "name": "Database",
      "grade": "A-",
      "credit": 3,
      "sequence": 2
    }
  ]
}
```

### Toggle Semester Active Status

```http
PATCH /api/semesters/1/toggle-active
Authorization: Bearer <accessToken>
```

Request body is not required.

If the semester is currently active, it becomes inactive. If it is inactive, it becomes active again.

```json
{
  "id": 1,
  "sequence": 1,
  "semesterGpa": 3.65,
  "semesterCredits": 6,
  "active": false,
  "subjects": [
    {
      "id": 1,
      "name": "Algorithms",
      "grade": "A",
      "credit": 3,
      "sequence": 1
    },
    {
      "id": 2,
      "name": "Database",
      "grade": "A-",
      "credit": 3,
      "sequence": 2
    }
  ]
}
```

### Delete Semester

```http
DELETE /api/semesters/1
Authorization: Bearer <accessToken>
```

```http
204 No Content
```

### Get CGPA

```http
GET /api/cgpa
Authorization: Bearer <accessToken>
```

```json
{
  "cgpa": 3.65,
  "totalCredits": 6,
  "semesterCount": 1
}
```

The CGPA response only includes active semesters.

---

## 📖 Swagger Documentation

### Local Swagger UI

```text
http://localhost:8080/swagger-ui/index.html
```

### Local OpenAPI JSON

```text
http://localhost:8080/v3/api-docs
```

### Production Swagger UI

```text
https://gpa-calculator-api-mohamed-d5e8gqcgcffcg9gj.germanywestcentral-01.azurewebsites.net/swagger-ui/index.html
```

### Production OpenAPI JSON

```text
https://gpa-calculator-api-mohamed-d5e8gqcgcffcg9gj.germanywestcentral-01.azurewebsites.net/v3/api-docs
```

Swagger includes JWT Bearer authorization. Click **Authorize**, paste your access token using the following format, and test protected endpoints directly:

```text
Bearer <accessToken>
```

---

## 🗄️ Database Design

### Main Entities

| Entity | Description |
|---|---|
| User | Stores account data, total GPA, and total credits |
| Semester | Stores semester sequence, GPA, credits, active status, and owner |
| Subject | Stores subject name, grade, credits, and sequence |
| RefreshToken | Stores refresh token and expiry date |

### Relationships

| Relationship | Type |
|---|---|
| User → Semester | One-to-Many |
| Semester → Subject | One-to-Many |
| User → RefreshToken | One-to-One |

### Important Constraints

- `email` is unique per user
- `(user_id, sequence)` is unique for semesters
- `(semester_id, sequence)` is unique for subjects
- `active` defaults to `true` for newly created semesters

### Semester Active Field

The `Semester` entity includes an `active` column:

```java
@Column(nullable = false)
private Boolean active = true;
```

Inactive semesters are kept in the database but excluded from CGPA calculations.

---

## 🧪 Testing

Unit tests are written using **JUnit 5** and **Mockito**.

### Tested Services

| Test Class | Purpose |
|---|---|
| `AuthServiceImplTest` | Registration, login, token generation, refresh flow |
| `RefreshTokenServiceImplTest` | Refresh token creation, validation, and expiry |
| `UserServiceImplTest` | Current user retrieval |
| `CurrentUserServiceImplTest` | Security context user extraction |
| `GpaCalculatorServiceImplTest` | Grade mapping, semester GPA, CGPA calculation |
| `SemesterServiceImplTest` | Semester CRUD, ownership checks, sequence handling |

Run tests with:

```bash
./mvnw test
```

---

## 🚀 How to Run Locally

### Prerequisites

- Java 21+
- Maven
- PostgreSQL

### Steps

```bash
git clone https://github.com/your-username/GPA_Calculator.git
cd GPA_Calculator
```

Create the database:

```sql
CREATE DATABASE gpa;
```

Configure local environment variables or use a local development configuration.

Run the application:

```bash
./mvnw spring-boot:run
```

Open Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## ☁️ Deployment on Azure

The project is deployed on **Azure App Service** as a Java 21 Spring Boot executable JAR.

### Azure Resources

| Resource | Purpose |
|---|---|
| Azure App Service | Hosts the Spring Boot REST API |
| Azure Database for PostgreSQL Flexible Server | Production PostgreSQL database |
| Azure App Service Environment Variables | Stores database credentials and JWT secret |

### Production URLs

```text
Base URL:
https://gpa-calculator-api-mohamed-d5e8gqcgcffcg9gj.germanywestcentral-01.azurewebsites.net
```

```text
Swagger UI:
https://gpa-calculator-api-mohamed-d5e8gqcgcffcg9gj.germanywestcentral-01.azurewebsites.net/swagger-ui/index.html
```

### Build JAR

```bash
./mvnw clean package -DskipTests
```

Generated JAR example:

```text
target/GPA_Calculator-0.0.1-SNAPSHOT.jar
```

### Deploy JAR to Azure App Service

```bash
az webapp deploy \
  --resource-group gpa-calculator-rg \
  --name gpa-calculator-api-mohamed \
  --src-path target/GPA_Calculator-0.0.1-SNAPSHOT.jar \
  --type jar
```

### Useful Azure CLI Commands

Restart the app:

```bash
az webapp restart \
  --resource-group gpa-calculator-rg \
  --name gpa-calculator-api-mohamed
```

Stream logs:

```bash
az webapp log tail \
  --resource-group gpa-calculator-rg \
  --name gpa-calculator-api-mohamed
```

Enable application logging:

```bash
az webapp log config \
  --resource-group gpa-calculator-rg \
  --name gpa-calculator-api-mohamed \
  --application-logging filesystem \
  --level information
```

---

## ⚙️ Configuration

The application reads sensitive values from environment variables.

Example `application.properties`:

```properties
spring.application.name=GPA_Calculator

spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

server.port=${PORT:8080}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=86400000
```

### Required Environment Variables

| Variable | Description |
|---|---|
| `DATABASE_URL` | JDBC URL for PostgreSQL |
| `DATABASE_USERNAME` | PostgreSQL username |
| `DATABASE_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Secret key used to sign JWT tokens |

### Example Production Database URL Format

```text
jdbc:postgresql://<server-name>.postgres.database.azure.com:5432/postgres?sslmode=require
```

> Never commit real database passwords, JWT secrets, access tokens, refresh tokens, or cloud credentials.

---

## 🎯 Key Design Decisions

| Decision | Reason |
|---|---|
| DTOs instead of exposing entities | Keeps API clean and avoids leaking sensitive data |
| MapStruct mapping | Provides type-safe mapping with less boilerplate |
| JWT authentication | Enables stateless secured APIs |
| Refresh tokens in database | Allows persistent session management |
| Ownership checks | Prevents users from accessing another user's data |
| Centralized GPA service | Keeps calculation logic reusable and testable |
| Semester activation toggle | Allows users to exclude semesters from CGPA without permanently deleting academic records |
| Environment variables for deployment | Keeps secrets out of source code and supports cloud deployment |
| Azure App Service deployment | Provides a managed hosting environment for the Spring Boot API |
| Azure PostgreSQL database | Provides a managed PostgreSQL database for production usage |
| Unit tests with Mockito | Tests business logic without database dependency |

---

## 📚 What I Learned

- Building JWT authentication with Spring Security
- Implementing refresh tokens
- Protecting user-owned resources
- Designing layered backend architecture
- Moving business logic from frontend to backend
- Calculating weighted GPA and CGPA correctly
- Filtering CGPA calculations by active semesters only
- Adding a semester activation/deactivation toggle
- Using MapStruct for DTO mapping
- Documenting APIs with Swagger
- Writing unit tests with JUnit 5 and Mockito
- Preparing a Spring Boot application for cloud deployment
- Deploying a Java 21 Spring Boot JAR to Azure App Service
- Connecting Spring Boot to Azure Database for PostgreSQL
- Managing production configuration using environment variables
- Debugging cloud deployment issues using Azure logs

---

## 🔮 Future Improvements

- Improve validation error response format
- Add integration tests with Testcontainers
- Add Docker and Docker Compose for optional containerized deployment
- Add database migrations with Flyway or Liquibase
- Add CI/CD pipeline with GitHub Actions
- Add automated Azure deployment workflow
- Add frontend integration polish
- Add password reset flow
- Add role-based authorization if needed
- Add health check endpoint for production monitoring
- Add API versioning
- Add pagination for large semester/subject datasets

---

## 👤 Author

**Mohamed Abdul Shafi**

📧 **Email:** [mohamedsadik763@gmail.com](mailto:mohamedsadik763@gmail.com)

---

<div align="center">

Built with dedication and a passion for clean backend architecture.

⭐ If you found this project useful, consider giving it a star.

</div>
