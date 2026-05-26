# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Backend (run from `backend/`)

```bash
# Compile
./mvnw compile --no-transfer-progress

# Run tests (uses H2 in-memory — no real DB needed)
./mvnw test --no-transfer-progress

# Run locally (requires application-local.properties, see below)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Java 17 is required. On Windows, set before running:
```
JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
```

### Frontend (run from `frontend/`)

```bash
npm ci                  # install (reproducible)
npm run build           # production build → dist/frontend/
npm start               # dev server → localhost:4200
ng test                 # unit tests (Karma + Jasmine)
ng test --include='**/some.component.spec.ts'  # single test file
```

## Architecture

### Monorepo structure
- `backend/` — Spring Boot 4, Java 17, Maven, SQL Server
- `frontend/` — Angular 19, Tailwind CSS 3, TypeScript 5.7

### Backend domain model
The `domain/entity/` layer is the only backend layer that exists so far. All entities use Lombok (`@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`) and JPA annotations.

The central entity is `Vehicle` (identified by `plate`). All records belong to a vehicle:
- `MaintenanceLog` → maintenance history with km tracking
- `Expense` → expenses by `ExpenseCategory`
- `Document` → vehicle documents by `DocumentType`
- `Alert` → alerts by `AlertType` + `UrgencyLevel`
- `TransferToken` + `TransferLog` → ownership transfer flow

`Vehicle.currentKm` is append-only — it only increases as maintenance/expense records are added.

Repositories, services, and controllers do not exist yet. They go under:
- `domain/repository/` — Spring Data JPA interfaces
- `application/service/` — business logic, operate on DTOs not entities
- `web/controller/` — REST controllers, never expose entities directly (use DTOs)

### Frontend architecture
Standalone Angular components with lazy-loaded feature modules. Routes are defined per feature in `*.routes.ts` files and loaded from `app.routes.ts`.

Authentication flow:
- `AuthService` (`core/services/`) stores JWT in `localStorage` under key `mycar_token`
- `authInterceptor` (`core/interceptors/`) attaches `Authorization: Bearer <token>` to all requests
- `authGuard` (`core/guards/`) protects all routes except `/auth`
- API base URL comes from `src/environments/environment.ts` → `apiUrl: 'http://localhost:8080'`

### Local database setup
Create `backend/src/main/resources/application-local.properties` (never committed):
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=mycar_db;encrypt=false
spring.datasource.username=YOUR_USER
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

Tests use H2 in-memory — config is in `backend/src/test/resources/application.properties`.

## CI / Git flow

Branch strategy: `feature/**` → `develop` → `main` via PRs.

- Push to `feature/**` triggers quick compile check (`.github/workflows/ci.yml`)
- PR/push to `develop` or `main` triggers full CI: `backend-ci.yml`, `frontend-ci.yml`, `secret-scan.yml`
- Dependabot creates weekly PRs for Maven and npm dependency updates

## Code conventions (from CodeRabbit config)

- Backend: use DTOs in controllers, never expose entities directly
- Backend: use `@ControllerAdvice` for exception handling
- Backend: `@Transactional` on service methods, not controllers
- Frontend: API URLs only via `environment.ts`, never hardcoded
- Frontend: always unsubscribe Observables (use `takeUntilDestroyed` or `async` pipe)
- Commits follow conventional commits: `feat:`, `fix:`, `chore:`, `refactor:`, `test:`, `docs:`
