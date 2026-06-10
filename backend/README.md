# WebinarsApp REST API

A Spring Boot REST API for managing webinars, enrollments, and users, developed as an assignment for the AUEB Coding
Factory. The system features JWT-based authentication, role-based access control (RBAC), soft-delete, and asynchronous
report generation.

## Table of Contents

- [Tech Stack](#tech-stack)
- [Requirements](#requirements)
- [Database Setup](#database-setup)
- [Build & Run](#build--run)
- [Configuration](#configuration)
- [API Overview](#api-overview)
    - [Authentication](#authentication)
    - [Users](#users)
    - [Webinars & Enrollments](#webinars--enrollments)
    - [Roles & Capabilities](#roles--capabilities)
        - [Roles](#roles)
        - [Capabilities](#capabilities)
    - [Reports (Async)](#reports-async)
- [Password Policy](#password-policy)
- [Error Responses](#error-responses)
- [API Documentation](#api-documentation)
- [Data Model](#data-model)
- [Internationalization](#internationalization)
- [Dummy Data](#dummy-data)
- [Project Structure](#project-structure)

## Tech Stack

- **Java 21** / **Spring Boot 3.4.5**
- **Spring Security** — stateless JWT authentication & RBAC
- **Spring Data JPA** + **Flyway** — schema migrations, no DDL auto-update
- **MS SQL Server 2022** — hosted via Docker
- **Lombok**, **Jakarta Validation**
- **OpenAPI / Swagger UI** — interactive API docs
- **Logback** — structured logging with MDC (request-scoped context)

## Requirements

- Java 21
- Docker & Docker Compose
- No additional software is required for running the application through Docker
- Gradle (wrapper included)

## Database Setup

The project uses MS SQL Server 2022 running in a Docker container.

### Manual Connection Details

If you wish to connect to the database manually:

- **Host**: `localhost`
- **Port**: `1433`
- **User**: `sa`
- **Password**: `Dev!Password2026`
- **Database**: `webinars_db`

## Build & Run

### 1. Start Services with Docker (Recommended)

The project includes a complete Docker setup.

```bash
docker compose up --build
```

The startup sequence automatically:

1. Starts SQL Server.
2. Creates the webinars_db database if it does not exist.
3. Executes Flyway migrations.
4. Starts the Spring Boot API.

The application will be available at: `http://localhost:8080`.

### 2. Local Development

If you prefer running the Spring Boot application locally:

```bash
./gradlew bootRun
```

The server will be available at `http://localhost:8080`.

### 3. Running Tests

```bash
./gradlew test
```

## Configuration

The application uses Spring profiles (`dev`, `staging`, `pro`). The `dev` profile is active by default.

| Property                      | Description          | Default (Dev)                        |
|:------------------------------|:---------------------|:-------------------------------------|
| `spring.datasource.url`       | SQL Server JDBC URL  | `jdbc:sqlserver://localhost:1433...` |
| `spring.datasource.username`  | Database User        | `sa`                                 |
| `spring.datasource.password`  | Database Password    | `Dev!Password2026`                   |
| `app.security.secret-key`     | JWT Signing Secret   | (predefined in dev)                  |
| `app.security.jwt-expiration` | JWT Token Validity   | `86400000` (24h)                     |
| `allowed.origins`             | CORS Allowed Origins | `http://localhost:4200`              |

## API Overview

Base Path: `/api/v1`

### Authentication

| Method | Endpoint             | Auth   | Description           |
|:-------|:---------------------|:-------|:----------------------|
| POST   | `/auth/authenticate` | Public | Login and receive JWT |

**Sample Request:**

```json
{
  "username": "ADMIN",
  "password": "Password123!"
}
```

### Users

| Method | Endpoint        | Auth          | Description                       |
|:-------|:----------------|:--------------|:----------------------------------|
| POST   | `/users`        | Public        | Register a new user               |
| GET    | `/users/{uuid}` | Authenticated | Get user profile details          |
| PUT    | `/users/{uuid}` | Authenticated | Update user profile               |
| DELETE | `/users/{uuid}` | Admin         | Delete user account (soft-delete) |

### Webinars & Enrollments

| Method | Endpoint                                          | Auth          | Description                                |
|:-------|:--------------------------------------------------|:--------------|:-------------------------------------------|
| GET    | `/webinars`                                       | Authenticated | List all active webinars (paginated)       |
| POST   | `/webinars`                                       | Organizer     | Create a new webinar                       |
| GET    | `/webinars/{uuid}`                                | Authenticated | Get detailed webinar information           |
| GET    | `/webinars/organizer/{uuid}`                      | Authenticated | List webinars organized by a specific user |
| GET    | `/webinars/participants/{uuid}`                   | Authenticated | List webinars where a user is enrolled     |
| PUT    | `/webinars/{uuid}`                                | Organizer     | Update webinar details                     |
| DELETE | `/webinars/{uuid}`                                | Organizer     | Soft-delete a webinar                      |
| POST   | `/webinars/{webinarUuid}/participants/{userUuid}` | Authenticated | Enroll a user in a webinar                 |

### Roles & Capabilities

Administrative endpoints are provided for managing roles and capabilities within the RBAC system.

#### Roles

| Method | Endpoint                                          | Auth  | Description                   |
|:-------|:--------------------------------------------------|:------|:------------------------------|
| POST   | `/roles`                                          | Admin | Create a new role             |
| GET    | `/roles`                                          | Admin | List all roles                |
| GET    | `/roles/{uuid}`                                   | Admin | Get role details              |
| PUT    | `/roles/{uuid}`                                   | Admin | Update role details           |
| DELETE | `/roles/{uuid}`                                   | Admin | Delete a role                 |
| POST   | `/roles/{roleUuid}/capabilities/{capabilityUuid}` | Admin | Assign a capability to a role |

#### Capabilities

| Method | Endpoint               | Auth  | Description               |
|:-------|:-----------------------|:------|:--------------------------|
| POST   | `/capabilities`        | Admin | Create a new capability   |
| GET    | `/capabilities`        | Admin | List all capabilities     |
| GET    | `/capabilities/{uuid}` | Admin | Get capability details    |
| PUT    | `/capabilities/{uuid}` | Admin | Update capability details |
| DELETE | `/capabilities/{uuid}` | Admin | Delete a capability       |

### Reports (Async)

| Method | Endpoint                  | Auth  | Description               |
|:-------|:--------------------------|:------|:--------------------------|
| POST   | `/reports/generate`       | Admin | Trigger report generation |
| GET    | `/reports/report/{jobId}` | Admin | Check job status/result   |

## Password Policy

The system enforces a strict password policy:

- Minimum **12 characters**
- At least one **uppercase** and one **lowercase** letter
- At least one **digit**
- At least one **special character** (e.g., `!`, `@`, `#`)

## Error Responses

The API returns structured error messages:

| HTTP Status        | Description                                       |
|:-------------------|:--------------------------------------------------|
| `400 Bad Request`  | Validation failed or invalid arguments            |
| `401 Unauthorized` | Missing or invalid JWT token                      |
| `403 Forbidden`    | Insufficient permissions for resource             |
| `404 Not Found`    | Resource does not exist                           |
| `409 Conflict`     | Resource already exists (e.g. duplicate username) |

**Error Body Example:**

```json
{
  "status": 404,
  "message": "User not found",
  "timestamp": "2026-06-05T17:00:00"
}
```

## API Documentation

The project uses **Springdoc OpenAPI** to generate interactive API documentation. When the application is running, you
can access the following:

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI Spec (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

*Note: Security requirements (JWT) are globally defined in `OpenApiConfig.java`.*

## Data Model

- **User**: Core entity for authentication.
- **UserDetail**: Profile information (One-to-One with User).
- **Role & Capability**: RBAC system (Many-to-Many).
- **Webinar**: Managed by Organizers (Users) and attended by Participants (Users).
  Participants can enroll in webinars and retrieve a personalized list of webinars in which they are enrolled.
- **Token**: Used for account-related workflows.
  (The backend already includes token infrastructure for account activation and password reset.
  Full email-based activation and password reset flows are planned as future enhancements.)

All persistent domain entities inherit from two common base classes:

- **AbstractEntity** provides:
    - Internal database identifier (`id`)
    - Auditing timestamps (`createdAt`, `updatedAt`)
    - Soft-delete support through the `deletedAt` field
    - Utility methods for soft-delete operations

- **AbstractUuidEntity** extends `AbstractEntity` and provides:
    - Public UUID identifier (`uuid`)
    - Automatic UUID generation before persistence
    - Equality and hash code implementations based on UUID values

*As a design principle, the REST API exposes UUIDs instead of internal database IDs, ensuring stable public identifiers
and preventing direct exposure of persistence-layer keys.*

*Soft-delete is enabled globally via `deleted_at` timestamps.*

## Internationalization

The application supports multiple languages for validation messages and system labels using Spring's `MessageSource`.

- **Default (English)**: `src/main/resources/messages.properties`
- **Greek**: `src/main/resources/messages_el.properties`

To request a specific language, use the `Accept-Language` header:

- `Accept-Language: el` for Greek.
- `Accept-Language: en` (or omit) for English.

## Dummy Data

To facilitate development and testing, a set of dummy data is provided. This data includes:

- Pre-configured Users (Admins, Organizers, Participants)
- Sample Webinars
- Enrollment records

The SQL script is located at:
`src/main/resources/data/dummy_data.sql`

*Note: These records are intended for the `dev` environment and are automatically managed if the corresponding database
initialization profile is active.*

## Project Structure

```text
src/main/java/com/karmperis/webinarsapp/
├── api/             # REST controllers
├── authentication/  # JWT logic + UserDetails
├── core/            # Error handler, exceptions, filters, OpenAPI config
├── dto/             # Request/response records (Java records + Jakarta validation)
├── mapper/          # Entity <-> DTO conversion
├── model/           # JPA entities
├── repository/      # Spring Data repositories
├── security/        # Filter chain, JWT filter, CORS, entry points
└── service/         # Business logic (interface + implementation)
```

---
**Author**: Nikolaos Karmperis  
**Assignment**: Coding Factory (AUEB) - 2026