# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Wedding Management System — an admin-facing web app for managing wedding halls, bookings, invoicing, and reports. Monorepo with two sub-projects:

- `wedding-repository-backend/` — Spring Boot 3 / Java 21 / PostgreSQL 16
- `wedding-repository-frontend/` — Next.js 14 / React 18 / TypeScript

## Backend Commands

```bash
# Start PostgreSQL (required before running the app)
docker compose up -d

# Run application (port 8082)
mvn spring-boot:run

# Tests
mvn test
mvn test -Dtest=LoginServiceTest   # single test class

# Code formatting (Google Java Format via Spotless)
mvn spotless:check
mvn spotless:apply
```

Key URLs: `http://localhost:8082/swagger-ui/index.html`, `http://localhost:8082/actuator/health`

## Frontend Commands

```bash
npm install
npm run dev          # port 3000
npm run build
npm run lint
npm run format:write  # rewrite with Prettier
```

## Backend Architecture (Clean Architecture per module)

Each module under `src/main/java/com/uit/weddingmanagement/modules/` follows:

```
domain/model/           → pure Java, no framework deps
domain/exception/
application/model/      → Command/Result DTOs
application/port/in/    → use case interfaces (inbound)
application/port/out/   → persistence/external interfaces (outbound)
application/usecase/    → service implementations
infrastructure/persistence/  → JPA entities, repos, adapters
infrastructure/security/
presentation/controller/
presentation/dto/
```

Request flow: `Controller → port/in (UseCase) → Service → port/out (Adapter) → DB`

The `catalog` module (hall types CRUD) is the reference example for implementing new modules.

Database schema is managed exclusively by Flyway migrations (`src/main/resources/db/migration/`). JPA runs with `ddl-auto: validate` — never let JPA modify the schema.

## Frontend Architecture (Feature-Sliced Design)

```
src/
  app/           → Next.js App Router routes
  features/      → business interactions (auth, rbac, ...)
  entities/      → domain models (user, session, permission, user-group)
  widgets/       → large composite UI blocks (app-shell, dashboard-shell)
  shared/        → generic utilities, hooks, API client, constants
  components/    → layout + UI primitives
```

Dependency rule: `app/routes → features → entities → shared`. Layers must not import upward.

## Environment Setup

Backend requires a `.env` file — copy `.env.example` and fill in values. CORS is configured to allow `http://localhost:3000`.
