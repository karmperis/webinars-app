# WebinarsApp

A full-stack webinar management platform developed as a final assignment for the **AUEB Coding Factory** program.

The application provides a complete workflow for authentication, webinar management, user administration, role and capability management, enrollments, and asynchronous report generation.

The project is organized as a monorepo with a Spring Boot REST API backend and an Angular frontend.

## Table of Contents

* [Project Overview](#project-overview)
* [Tech Stack](#tech-stack)
* [Repository Structure](#repository-structure)
* [Main Features](#main-features)
* [Architecture](#architecture)
* [Backend Overview](#backend-overview)
* [Frontend Overview](#frontend-overview)
* [Security Overview](#security-overview)
* [Database Overview](#database-overview)
* [Build & Run](#build--run)
* [Demo Credentials](#demo-credentials)
* [API Documentation](#api-documentation)
* [Development Notes](#development-notes)
* [Assignment Notes](#assignment-notes)
* [Future Improvements](#future-improvements)

## Project Overview

WebinarsApp is a full-stack application for managing webinars and user participation.

The system supports:

* User registration and login
* JWT-based authentication
* Role-based and capability-based authorization
* Webinar creation, editing, deletion, and enrollment
* User profile management
* Admin user management
* Role and capability management
* Assignment of capabilities to roles
* Asynchronous report generation
* Docker-based local development setup

The project was developed for educational purposes as part of the Coding Factory final project requirements.

## Tech Stack

### Backend

* Java 21
* Spring Boot 3
* Spring Security
* JWT Authentication
* Spring Data JPA
* Hibernate Validation / Jakarta Validation
* Flyway
* MS SQL Server 2022
* Lombok
* Swagger / OpenAPI
* Gradle
* Docker

### Testing & Quality

* JUnit 5
* Mockito
* MockMvc
* JaCoCo 

### Frontend

* Angular 21
* TypeScript
* Angular Router
* Angular Reactive Forms
* Angular Signals
* RxJS
* Bootstrap 5
* Font Awesome
* JWT Interceptor
* Route Guards

### DevOps / Tooling

* Docker
* Docker Compose
* Gradle Wrapper
* npm
* Git / GitHub

## Repository Structure

```text
webinars-app/
├── backend/       # Spring Boot REST API
├── frontend/      # Angular frontend application
├── docker-compose.yml
└── README.md
```

## Main Features

### Authentication

* User login with username and password
* User registration
* JWT token generation
* JWT token validation
* Logout functionality
* Remember Me support on the frontend

### Authorization

The application supports three main roles:

| Role          | Description                        |
| :------------ | :--------------------------------- |
| `ADMIN`       | Full administrative access         |
| `ORGANIZER`   | Can create and manage own webinars |
| `PARTICIPANT` | Can view webinars and enroll       |

The backend also includes a capability-based authorization model.

Examples of capabilities:

* `VIEW_WEBINARS`
* `CREATE_WEBINAR`
* `EDIT_WEBINAR`
* `DELETE_WEBINAR`
* `ENROLL_IN_WEBINAR`

### Webinars

* List all webinars
* View webinar details
* Create webinar
* Edit webinar
* Delete webinar
* Enroll in webinar
* View enrolled webinars
* View webinars organized by the current user

### Users

* Register new users
* View user profile
* Edit current user profile
* Admin user listing
* Admin user access management
* Role assignment
* Active/inactive user management
* Soft-delete users

### Roles & Capabilities

* Create, edit, list, and delete roles
* Create, edit, list, and delete capabilities
* Assign capabilities to roles
* View capabilities assigned to a specific role

### Reports

The application supports asynchronous report generation.

Available reports include:

* Popularity report — shows active webinars sorted by participant count
* Productive users report — shows active organizers with 4 or more active webinars and their total webinar duration
* Webinar and organizer status report — shows active or deleted webinars organized by active, inactive, or deleted users, highlighting both webinar status and organizer status

The frontend starts a report job and polls the backend until the result is available.

## Architecture

The project follows a layered full-stack architecture.

### Backend Architecture

```text
Controller Layer
    ↓
Service Layer
    ↓
Repository Layer
    ↓
Database
```

The backend follows clear separation of concerns:

* Controllers expose REST endpoints.
* Services contain business logic.
* Repositories handle database access.
* DTOs define API request and response contracts.
* Mappers convert between entities and DTOs.
* Security filters handle JWT authentication.
* Global exception handling provides structured error responses.

### Frontend Architecture

```text
Components
    ↓
Services
    ↓
HTTP Client / Interceptor
    ↓
Backend REST API
```

The frontend follows a service-oriented Angular architecture:

* Components handle UI rendering and user interaction.
* Services handle HTTP communication.
* Guards protect routes.
* Interceptors attach JWT tokens to API requests.
* Interfaces mirror backend DTOs.
* Signals manage local page state.

## Backend Overview

Backend source code is located under:

```text
backend/
```

Main backend modules:

```text
src/main/java/com/karmperis/webinarsapp/
├── api/             # REST controllers
├── authentication/  # JWT logic and authentication services
├── core/            # Exceptions, filters, OpenAPI config, shared infrastructure
├── dto/             # Request and response DTOs
├── mapper/          # Entity-DTO mappers
├── model/           # JPA entities
├── repository/      # Spring Data repositories
├── security/        # Security configuration and JWT filter
└── service/         # Business services
```

### Main Backend Endpoints

Base path:

```text
/api/v1
```

### Authentication

| Method | Endpoint             | Access | Description           |
| :----- | :------------------- | :----- | :-------------------- |
| POST   | `/auth/authenticate` | Public | Login and receive JWT |

### Users

| Method | Endpoint               | Access        | Description                        |
| :----- | :--------------------- | :------------ | :--------------------------------- |
| POST   | `/users`               | Public        | Register a new user                |
| GET    | `/users`               | Admin         | List users                         |
| GET    | `/users/{uuid}`        | Authenticated | Get user details                   |
| PUT    | `/users/{uuid}`        | Authenticated | Update user profile                |
| PATCH  | `/users/{uuid}/access` | Admin         | Update user role and active status |
| DELETE | `/users/{uuid}`        | Admin         | Soft-delete user                   |

### Webinars

| Method | Endpoint                                          | Access            | Description                          |
| :----- | :------------------------------------------------ | :---------------- | :----------------------------------- |
| GET    | `/webinars`                                       | Authenticated     | List webinars                        |
| GET    | `/webinars/{uuid}`                                | Authenticated     | Get webinar details                  |
| POST   | `/webinars`                                       | Admin / Organizer | Create webinar                       |
| PUT    | `/webinars/{uuid}`                                | Admin / Organizer | Update webinar                       |
| DELETE | `/webinars/{uuid}`                                | Admin / Organizer | Delete webinar                       |
| GET    | `/webinars/organizer/{organizerUuid}`             | Authenticated     | List webinars by organizer           |
| GET    | `/webinars/participants/{userUuid}`               | Authenticated     | List webinars where user is enrolled |
| POST   | `/webinars/{webinarUuid}/participants/{userUuid}` | Authenticated     | Enroll user in webinar               |

### Roles

| Method | Endpoint                                          | Access | Description                          |
| :----- | :------------------------------------------------ | :----- | :----------------------------------- |
| GET    | `/roles`                                          | Admin  | List roles                           |
| GET    | `/roles/{uuid}`                                   | Admin  | Get role details                     |
| POST   | `/roles`                                          | Admin  | Create role                          |
| PUT    | `/roles/{uuid}`                                   | Admin  | Update role                          |
| DELETE | `/roles/{uuid}`                                   | Admin  | Delete role                          |
| POST   | `/roles/{roleUuid}/capabilities/{capabilityUuid}` | Admin  | Assign capability to role            |
| GET    | `/roles/{uuid}/capabilities/view`                 | Admin  | View capabilities assigned to a role |

### Capabilities

| Method | Endpoint               | Access | Description            |
| :----- | :--------------------- | :----- | :--------------------- |
| GET    | `/capabilities`        | Admin  | List capabilities      |
| GET    | `/capabilities/{uuid}` | Admin  | Get capability details |
| POST   | `/capabilities`        | Admin  | Create capability      |
| PUT    | `/capabilities/{uuid}` | Admin  | Update capability      |
| DELETE | `/capabilities/{uuid}` | Admin  | Delete capability      |

### Reports

| Method | Endpoint                        | Access | Description                  |
| :----- | :------------------------------ | :----- | :--------------------------- |
| POST   | `/reports/generate?type={type}` | Admin  | Start report generation      |
| GET    | `/reports/report/{jobId}`       | Admin  | Get report job status/result |

## Frontend Overview

Frontend source code is located under:

```text
frontend/
```

Main frontend structure:

```text
src/app/
├── components/
│   ├── auth/
│   ├── webinars/
│   ├── users/
│   ├── roles/
│   ├── capabilities/
│   ├── reports/
│   └── layout/
│
├── shared/
│   ├── constants/
│   ├── guards/
│   ├── interceptors/
│   ├── interfaces/
│   └── services/
│
├── app.config.ts
├── app.routes.ts
└── app.ts
```

### Main Frontend Routes

| Route                            | Access            | Description               |
| :------------------------------- | :---------------- | :------------------------ |
| `/login`                         | Public            | User login                |
| `/register`                      | Public            | User registration         |
| `/webinars`                      | Authenticated     | List webinars             |
| `/webinars/create`               | Admin / Organizer | Create webinar            |
| `/webinars/:uuid/edit`           | Authenticated     | Edit webinar              |
| `/my-webinars`                   | Authenticated     | View enrolled webinars    |
| `/organizer-webinars`            | Admin / Organizer | View organized webinars   |
| `/users`                         | Admin             | User management           |
| `/users/:uuid/access`            | Admin             | Edit user access          |
| `/profile`                       | Authenticated     | Edit current user profile |
| `/roles`                         | Admin             | Role management           |
| `/roles/create`                  | Admin             | Create role               |
| `/roles/:uuid/edit`              | Admin             | Edit role                 |
| `/roles/:uuid/capabilities`      | Admin             | Assign capability to role |
| `/roles/:uuid/capabilities/view` | Admin             | View role capabilities    |
| `/capabilities`                  | Admin             | Capability management     |
| `/capabilities/create`           | Admin             | Create capability         |
| `/capabilities/:uuid/edit`       | Admin             | Edit capability           |
| `/reports`                       | Admin             | Generate reports          |

## Security Overview

The project implements authentication and authorization on both backend and frontend.

### Backend Security

* Stateless JWT authentication
* Spring Security filter chain
* JWT validation filter
* Role-based authorization
* Capability-based authorization
* Protected REST endpoints
* Public registration and login endpoints
* CORS configuration for Angular development server

### Frontend Security

* JWT token storage
* Auth guard for protected routes
* Role-aware route protection
* HTTP interceptor for Authorization header
* Role-based navigation visibility
* Logout handling

For educational simplicity, JWT tokens are stored in `localStorage` or `sessionStorage`.

## Database Overview

The backend uses MS SQL Server 2022.

Database schema is managed with Flyway migrations.

Main tables include:

* `users`
* `users_details`
* `roles`
* `capabilities`
* `roles_capabilities`
* `webinars`
* `users_webinars`
* `tokens`

The database design includes:

* UUID public identifiers
* Internal numeric primary keys
* Auditing timestamps
* Soft-delete support
* Many-to-many relationships for enrollments and role capabilities
* One-to-one relationship between user and user details
* One-to-many relationship between organizer and webinars

## Build & Run

### Prerequisites

To run the full project locally, you need:

* Docker
* Docker Compose
* Java 21, if running backend outside Docker
* Node.js and npm, if running frontend locally
* Angular CLI, if using `ng serve`

## Running the Backend with Docker

From the repository root:

```bash
docker compose up --build
```

This command starts:

1. SQL Server
2. Database initialization container
3. Spring Boot backend API

The backend will be available at:

```text
http://localhost:8080
```

Swagger UI will be available at:

```text
http://localhost:8080/swagger-ui/index.html
```

## Running the Frontend

Open a new terminal and navigate to the frontend folder:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm start
```

or:

```bash
ng serve
```

The frontend will be available at:

```text
http://localhost:4200
```

The frontend expects the backend API at:

```text
http://localhost:8080/api/v1
```

## Demo Credentials

The application automatically creates a default administrator account through the database migration process during initialization.
The administrator account is available immediately after running:

```bash
docker compose up --build
```

### Administrator

```text
ADMIN
Username: ADMIN
Password: SecureAdmin123!
```

### Organizer and participant accounts

Additional organizer and participant accounts are provided through the optional dummy data script located under:

```text
backend/src/main/resources/data/dummy_data.sql
```

If the dummy data script is loaded, the following demo accounts become available:

| Username | Role | Password |
|-----------|----------|----------|
| k.papadopoulos@test.gr | Organizer | SecureAdmin123! |
| m.pappa@test.gr | Organizer | SecureAdmin123! |
| n.alexiou@test.gr | Participant | SecureAdmin123! |
| g.panagoulis@test.gr | Participant | SecureAdmin123! |

These credentials are intended only for local development, testing, and academic evaluation.
> Note: Only the administrator account is created automatically through the database migration process.
>
> Organizer and participant accounts are available only when the dummy data dataset is loaded.
> 
> The dummy dataset also includes additional inactive and deleted organizer records used for report generation scenarios and soft-delete demonstrations.
> These records are not intended as primary demo login accounts.

## API Documentation

The backend exposes interactive API documentation through Swagger UI.

When the backend is running, visit:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
```

## Development Notes

### Backend

To run the backend locally without Docker:

```bash
cd backend
./gradlew bootRun
```

To run backend tests:

```bash
cd backend
./gradlew test
```

### Frontend

To build the frontend:

```bash
cd frontend
npm run build
```

The production build output is generated under:

```text
frontend/dist/
```

## Assignment Notes

This project was developed as a university assignment for the AUEB Coding Factory program.

The application is intended for educational and portfolio purposes. Security, deployment, and infrastructure decisions were made with simplicity and learning objectives in mind and should not be considered production-ready defaults.

For example:

* Demo credentials are included for academic evaluation.
* JWT tokens are stored in browser storage for frontend simplicity.
* Development database credentials are configured for local Docker execution.
* Production-grade secret management is outside the scope of this assignment.

## Future Improvements

Possible future improvements include:

### Authentication & Account Management
Implement user account activation using email verification tokens.
Implement forgot-password and password reset workflows using secure reset tokens.
Add email notifications for account activation and password recovery.

### Webinar Management
Add the ability for participants to unenroll from webinars.

### Testing
Increase backend integration test coverage.
Add frontend unit tests.

---

**Author:**  Nikolaos Karmperis  
**Assignment:**  AUEB Coding Factory - 2026  
**Backend:**  Spring Boot REST API  
**Frontend:**  Angular  
**Database:**  MS SQL Server  
