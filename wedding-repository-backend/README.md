# Wedding Repository Backend

Backend service for the Wedding Management System.

This repository contains the `Spring Boot 3` application that will serve REST APIs for:

- authentication and authorization
- catalog management
- wedding booking
- invoice and incidental processing
- reporting and system settings

## Tech Stack

- Java 21
- Spring Boot 3.3.x
- Maven 3.9.x
- Spring Web
- Spring Data JPA
- Flyway
- PostgreSQL 16
- Lombok
- MapStruct
- Spring Boot Actuator

## Architecture

This backend will follow **full Clean Architecture**.

Why this architecture is selected:

- the project has many business rules that should not be mixed directly with framework code
- core rules like booking conflict, deposit validation, cancellation refund, invoice penalty, and RBAC should remain independent from Spring MVC and JPA details
- it makes the code easier to test at use-case level
- it keeps the dependency direction clean as the system grows across later milestones

Clean Architecture rule for this repository:

- `domain` does not depend on Spring, JPA, controllers, or database code
- `application` depends on `domain`
- `infrastructure` implements technical details like persistence, security, and external integrations
- `presentation` exposes HTTP APIs and maps requests/responses to use cases

Target backend layers:

- `domain`: enterprise business rules
- `application`: use cases and ports
- `infrastructure`: database, security, and framework adapters
- `presentation`: REST controllers and API DTOs

Planned dependency direction:

```text
presentation -> application -> domain
infrastructure -> application -> domain
domain -> no outer layer
```

## Target Backend Structure

Target structure for future milestones:

```text
src/main/java/com/uit/weddingmanagement/
  common/
  modules/
    auth/
      domain/
      application/
        port/in/
        port/out/
        usecase/
      infrastructure/
        persistence/
        security/
      presentation/
        controller/
        dto/
    catalog/
      domain/
      application/
      infrastructure/
      presentation/
    booking/
      domain/
      application/
      infrastructure/
      presentation/
    billing/
      domain/
      application/
      infrastructure/
      presentation/
    reporting/
      domain/
      application/
      infrastructure/
      presentation/
```

How these layers are intended to work:

- `domain`: entities, value objects, domain rules, domain services
- `application/port/in`: use case interfaces that the outer world can call
- `application/port/out`: interfaces for persistence or external dependencies needed by use cases
- `application/usecase`: implementation of business flows
- `infrastructure/persistence`: JPA entities, Spring Data repositories, adapter implementations
- `infrastructure/security`: JWT, Spring Security, auth adapter code
- `presentation/controller`: REST endpoints
- `presentation/dto`: request and response models for HTTP

## What Lives Here

- `src/main/java`: application source code
- `src/main/resources`: app config and Flyway SQL migrations
- `src/test/java`: test source code
- `docker-compose.yml`: local PostgreSQL setup
- `.env.example`: sample local environment variables
- `pom.xml`: Maven build configuration

## Prerequisites

Install these tools before running the backend:

- Java 21 JDK
- Maven 3.9+
- Docker Desktop
- DBeaver (optional, for database inspection)

Quick verification:

```bash
java -version
mvn -version
docker -v
```

## Local Setup

### 1. Create local env file

Copy `.env.example` to `.env`.

Default values already match the local setup:

- database: `wedding_management`
- username: `wedding_app`
- password: `wedding_password`
- port: `5432`

### 2. Start PostgreSQL

```bash
docker compose up -d
```

### 3. Optional: connect with DBeaver

Use these connection values:

- Host: `localhost`
- Port: `5432`
- Database: `wedding_management`
- Username: `wedding_app`
- Password: `wedding_password`

### 4. Run the backend

```bash
mvn spring-boot:run
```

`spring-boot-devtools` is enabled for local development. When compiled classes or resources change, the backend restarts automatically, so Swagger UI only needs a browser refresh to load the updated OpenAPI document.

For Java source changes, make sure your IDE is compiling on save or otherwise updating `target/classes`; devtools restarts on classpath changes, not on raw `.java` file edits alone.

The API will start at:

- `http://localhost:8082`

Health endpoint:

- `http://localhost:8082/actuator/health`

API documentation:

- `http://localhost:8082/swagger-ui/index.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8082/v3/api-docs`
- `http://localhost:8082/v3/api-docs.yaml`

To try secured endpoints in Swagger UI:

1. call `POST /api/auth/login`
2. copy the returned `accessToken`
3. click `Authorize` and paste `Bearer <accessToken>`

### 5. Compile or test manually

```bash
mvn compile
mvn test
```

## Database and Flyway

Flyway migrations are stored in:

- `src/main/resources/db/migration`

Current migrations:

- `V1__create_tables.sql`
- `V2__seed_data.sql`
- `V3__seed_thamso.sql`
- `V4__seed_auth_rbac.sql`
- `V5__rename_tables_to_english.sql`
- `V6__rename_columns_to_english.sql`
- `V7__normalize_vietnamese_data_and_rbac_catalog.sql`
- `V8__normalize_local_admin_password.sql`

When the app starts, Flyway will:

1. check the current database version
2. run any missing migrations in order
3. keep the schema consistent across machines

JPA is configured with `ddl-auto=validate`, which means:

- Hibernate will check whether tables/columns match the entity mapping
- Hibernate will not create or modify tables automatically
- database structure must be managed by Flyway

How to read the current scaffold:

- `WeddingManagementApplication.java`: Spring Boot entry point
- `common/api`: shared API response models
- `common/config`: framework-level configuration
- `common/entity`: shared JPA base classes
- `common/exception`: global error handling
- `resources/application.yml`: runtime configuration
- `resources/db/migration`: database migration scripts

## Development Notes

- CORS is currently opened for `http://localhost:3000`
- timestamps use UTC in backend config
- database schema is versioned with Flyway, not generated by Hibernate
- local PostgreSQL data is persisted in Docker volume
- Swagger UI and OpenAPI docs are enabled by default for local development
- OpenAPI caching is disabled locally so `/v3/api-docs` is regenerated on refresh
- devtools can auto-restart the backend after compiled code changes; no manual rerun is required
- set `SPRINGDOC_API_DOCS_ENABLED=false` and `SPRINGDOC_SWAGGER_UI_ENABLED=false` to disable docs in stricter environments

## Branching Strategy

- `main`: production-ready branch
- `develop`: integration branch
- `feature/*`: short-lived feature branches

Examples:

- `feature/m0-spring-boot-base`
- `feature/m1-auth-jwt`
- `feature/m2-catalog-module`
