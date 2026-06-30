# WebinarsApp Frontend

An Angular frontend for the WebinarsApp platform, developed as an assignment for the AUEB Coding Factory.

The application provides a complete user interface for authentication, webinar management, enrollments, user administration, role and capability management, and asynchronous report generation through integration with the WebinarsApp REST API.

## Table of Contents

- [Tech Stack](#tech-stack)
- [Requirements](#requirements)
- [Build & Run](#build--run)
- [Configuration](#configuration)
- [Application Overview](#application-overview)
  - [Authentication](#authentication)
  - [Webinars & Enrollments](#webinars--enrollments)
  - [Users](#users)
  - [Roles & Capabilities](#roles--capabilities)
  - [Reports](#reports)

- [Security](#security)
- [UI & State Management](#ui--state-management)
- [Project Structure](#project-structure)

## Tech Stack

- **Angular 21.2**
- **TypeScript 5.9**
- **Angular Router**
- **Angular Reactive Forms**
- **Angular Signals**
- **RxJS**
- **Bootstrap 5.3**
- **Font Awesome 7**
- **JWT-based authentication**

## Requirements

- Node.js
- npm 11+
- Angular CLI 21+
- Running WebinarsApp backend API

The frontend expects the backend API to be available at:

```text
http://localhost:8080/api/v1
```

## Build & Run

### 1. Install Dependencies

```bash
npm install
```

### 2. Start Development Server

```bash
npm start
```

or:

```bash
ng serve
```

The application will be available at:

```text
http://localhost:4200
```

### 3. Production Build

```bash
npm run build
```

The build output will be generated in the `dist/` directory.

## Configuration

The application uses Angular environment files.

### Development Environment

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
};
```

### Production Environment

```typescript
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080/api/v1',
};
```

The API base URL is consumed by Angular services under:

```text
src/app/shared/services/
```

## Application Overview

The frontend is organized around standalone Angular components and feature-based folders.

### Authentication

| Route       | Access | Description           |
| :---------- | :----- | :-------------------- |
| `/login`    | Public | User login            |
| `/register` | Public | New user registration |

Authentication features include:

- Username/password login
- User registration
- JWT token storage
- Remember Me support
- Password visibility toggle
- Token expiration validation
- Logout functionality

### Webinars & Enrollments

| Route                  | Required capability | Description                                 |
| :--------------------- | :------------------ | :------------------------------------------ |
| `/webinars`            | `VIEW_WEBINARS`     | List all webinars                           |
| `/webinars/create`     | `CREATE_WEBINAR`    | Create a webinar                            |
| `/webinars/:uuid/edit` | `EDIT_WEBINAR`      | Edit a webinar                              |
| `/my-webinars`         | `ENROLL_IN_WEBINAR` | View enrolled webinars                      |
| `/organizer-webinars`  | `CREATE_WEBINAR`    | View webinars organized by the current user |

Supported webinar actions:

- View active webinars
- Create webinars
- Edit webinars
- Delete webinars
- Enroll in webinars
- Unenroll from webinars
- View the current user's enrolled webinars
- View organizer webinars
- Server-side pagination and sorting for webinar listings, enrolled webinars, and organizer webinars

Default webinar behavior by role:

| Role          | Permissions                                                                                          |
| :------------ | :--------------------------------------------------------------------------------------------------- |
| `ADMIN`       | Can manage all webinars and manage enrollments where backend rules allow                             |
| `ORGANIZER`   | Can create webinars, manage their own webinars and enroll/unenroll from webinars organized by others |
| `PARTICIPANT` | Can view webinars and enroll/unenroll in webinars                                                    |

> **Note:** The frontend authorizes routes and navigation entries based on capabilities extracted from the JWT. The role descriptions above reflect the default capabilities assigned to each role by the backend.

### Users

| Route                 | Required capability | Description                      |
| :-------------------- | :------------------ | :------------------------------- |
| `/users`              | `MANAGE_USERS`      | List users                       |
| `/users/:uuid/access` | `MANAGE_USERS`      | Edit user role and active status |
| `/profile`            | Authenticated       | Edit current user profile        |

User features include:

- User listing
- Profile editing
- User access management
- Role assignment
- Active/inactive status management
- User deletion

### Roles & Capabilities

| Route                            | Required capability   | Description                       |
| :------------------------------- | :-------------------- | :-------------------------------- |
| `/roles`                         | `MANAGE_ROLES`        | List roles                        |
| `/roles/create`                  | `MANAGE_ROLES`        | Create role                       |
| `/roles/:uuid/edit`              | `MANAGE_ROLES`        | Edit role                         |
| `/roles/:uuid/capabilities`      | `MANAGE_ROLES`        | Assign capability to role         |
| `/roles/:uuid/capabilities/view` | `MANAGE_ROLES`        | View and manage role capabilities |
| `/capabilities`                  | `MANAGE_CAPABILITIES` | List capabilities                 |
| `/capabilities/create`           | `MANAGE_CAPABILITIES` | Create capability                 |
| `/capabilities/:uuid/edit`       | `MANAGE_CAPABILITIES` | Edit capability                   |

Role and capability features include:

- Role CRUD operations
- Capability CRUD operations
- Capability assignment to roles
- Capability removal from roles
- Viewing capabilities assigned to a role

### Reports

| Route      | Required capability | Description                      |
| :--------- | :------------------ | :------------------------------- |
| `/reports` | `VIEW_REPORTS`      | Generate and view system reports |

Supported report types:

- Popularity report — shows active webinars sorted by participant count
- Productive users report — shows active organizers with 4 or more active webinars and their total webinar duration
- Webinar and organizer status report — shows active or deleted webinars organized by active, inactive, or deleted users, highlighting both webinar status and organizer status

Reports are generated asynchronously by the backend. The frontend starts a report job and polls the backend until the result is available.

## Security

The frontend implements client-side authentication and authorization support.

### JWT Authentication

After successful login, the JWT token is stored either in:

- `localStorage`, when Remember Me is selected
- `sessionStorage`, when Remember Me is not selected

The token is used to identify the authenticated user and extract the user UUID, role, and assigned capabilities for client-side authorization.

### Auth Guard

Protected routes use an Angular route guard.

The guard checks:

- whether a valid JWT token exists
- whether the token is not expired
- whether the user has the required capabilities for protected routes

Unauthorized users are redirected to the login page.

### HTTP Interceptor

The application registers an HTTP interceptor that automatically attaches the JWT token to outgoing API requests.

```http
Authorization: Bearer <jwt-token>
```

## UI & State Management

The frontend uses a clean, consistent UI structure based on Bootstrap and custom CSS.

Common UI patterns:

- Page-level layout with navbar/sidebar
- Reusable empty states
- Error states
- Loading states
- Success and warning alerts
- Form validation feedback
- Disabled submit buttons during requests
- Capability-based navigation visibility
- Webinar list pages use server-side pagination and sorting with pagination controls displayed in the table footer

State management is handled with Angular Signals for page state such as:

- `isLoading`
- `isSubmitting`
- `errorMessage`
- `successMessage`
- `warningMessage`

Reactive Forms are used for all form-based screens.

## Project Structure

```text
src/app/
├── components/
│   ├── auth/
│   │   ├── login/
│   │   └── register/
│   │
│   ├── webinars/
│   │   ├── webinars/
│   │   ├── create-webinar/
│   │   ├── edit-webinar/
│   │   ├── my-webinars/
│   │   └── organizer-webinars/
│   │
│   ├── users/
│   │   ├── users/
│   │   ├── edit-profile/
│   │   └── edit-user-access/
│   │
│   ├── roles/
│   │   ├── roles/
│   │   ├── create-role/
│   │   ├── edit-role/
│   │   ├── assign-capability/
│   │   └── role-capabilities/
│   │
│   ├── capabilities/
│   │   ├── capabilities/
│   │   ├── create-capability/
│   │   └── edit-capability/
│   │
│   ├── reports/
│   │   └── reports/
│   │
│   └── layout/
│       └── navbar/
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

## Shared Layer

The `shared` folder contains reusable application logic.

### Services

```text
src/app/shared/services/
├── auth.ts
├── webinar.ts
├── user.ts
├── role.ts
├── capability.ts
└── report.ts
```

### Interfaces

The frontend mirrors backend DTOs using TypeScript interfaces, including:

- Authentication DTOs
- User DTOs
- Webinar DTOs
- Role DTOs
- Capability DTOs
- Report DTOs
- Error response DTOs
- Paginated response DTOs

## Backend Integration

The frontend integrates with the Spring Boot REST API through the following base path:

```text
/api/v1
```

Main backend resources consumed by the frontend:

| Resource       | Base Endpoint   |
| :------------- | :-------------- |
| Authentication | `/auth`         |
| Users          | `/users`        |
| Webinars       | `/webinars`     |
| Roles          | `/roles`        |
| Capabilities   | `/capabilities` |
| Reports        | `/reports`      |

## Design Notes

The application follows a service-oriented frontend architecture.

Components are responsible for:

- UI rendering
- user interaction
- form handling
- local page state

Services are responsible for:

- HTTP communication
- API endpoint integration
- typed request/response handling

Guards and interceptors are responsible for:

- route protection based on capabilities
- token validation
- attaching authorization headers to outgoing requests

This separation keeps the application maintainable, testable, and aligned with the backend REST API design.

---

**Author:** Nikolaos Karmperis
**Assignment:** AUEB Coding Factory - 2026
**Backend:** Spring Boot REST API
**Frontend:** Angular 21
**Database:** MS SQL Server