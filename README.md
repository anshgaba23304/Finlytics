# Finlytics — Finance Dashboard Backend

Spring Boot backend for a finance dashboard with **JWT authentication**, **role-based access control (RBAC)**, **financial record CRUD** (with soft delete), **filtering and search**, **aggregated dashboard APIs**, **OpenAPI (Swagger)**, and **Actuator** health/info.

## Tech stack

- Java 21+, Spring Boot 4, Spring Security, Spring Data JPA  
- **PostgreSQL** at runtime (Supabase, RDS, Docker, etc.)  
- **H2** only on the **test** classpath (in-memory integration tests; no H2 in production JAR)  
- JWT (HS256) bearer tokens  
- springdoc OpenAPI 3  

## Configuration profiles

| Profile | Purpose |
|--------|---------|
| `dev` (default) | Local PostgreSQL (`docker compose up -d`), `ddl-auto=update`, Swagger enabled, optional demo seed |
| `prod` | `ddl-auto=validate`, Swagger off, demo seed off, **requires** `SPRING_DATASOURCE_*` and a strong `APP_JWT_SECRET` |
| `test` | Activated by `@ActiveProfiles("test")` — H2 in-memory, fast tests |

Active profile: `SPRING_PROFILES_ACTIVE` (defaults to `dev`).

## Local run (PostgreSQL)

1. Start Postgres (defaults in `application.properties` match `docker-compose.yml`):

```bash
docker compose up -d
```

2. Apply schema once (or let Hibernate `update` create tables in dev): see `schema/supabase.sql` for the canonical DDL (also suitable for Supabase).

3. Run the API:

```bash
./mvnw spring-boot:run
```

- API: `http://localhost:8080`  
- Swagger: `http://localhost:8080/swagger-ui.html`  
- Health: `http://localhost:8080/actuator/health`  

Default datasource in **dev** (override with env): `jdbc:postgresql://localhost:5432/finlytics`, user/password `finlytics` / `finlytics`.

**Secrets:** Do not commit real database URLs or passwords. Use `SPRING_DATASOURCE_*` environment variables, or copy `application-local.properties.example` to `application-local.properties` (gitignored). For Supabase, see `env.supabase.example`.

### Production / Supabase

Set at minimum:

- `SPRING_PROFILES_ACTIVE=prod`  
- `SPRING_DATASOURCE_URL` — e.g. `jdbc:postgresql://db.<ref>.supabase.co:5432/postgres?sslmode=require`  
- `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`  
- `APP_JWT_SECRET` — long random string (≥ 32 bytes); the app **refuses to start** in `prod` if the default placeholder is still used  

Apply `schema/supabase.sql` (or equivalent) before deploy; use `ddl-auto=validate` in prod.

Optional: `APP_SEED_ENABLED=false` (already default in prod via `application-prod.properties`).

## Roles and permissions

| Role    | Dashboard summary & trends | Recent activity (line items) | List/read records | Create/update/delete records | User management |
|--------|-----------------------------|------------------------------|-------------------|------------------------------|-----------------|
| VIEWER | Yes                         | No                           | No                | No                           | No              |
| ANALYST| Yes                         | Yes                          | Yes               | No                           | No              |
| ADMIN  | Yes                         | Yes                          | Yes               | Yes                          | Yes             |

Enforcement uses `@PreAuthorize` and a JWT filter.

## Assumptions

1. **Single-tenant ledger**: financial records are global.  
2. **JWT**: set `APP_JWT_SECRET` in production.  
3. **Soft delete**: `DELETE /api/records/{id}` sets `deleted = true`.  
4. **Demo seed** (`app.seed.enabled=true`): creates users + sample rows only when the `users` table is empty (disabled in `prod`).  

### Seeded users (when seed runs)

| Username  | Password    | Role    |
|-----------|------------|---------|
| admin     | Admin123!  | ADMIN   |
| analyst   | Analyst123!| ANALYST |
| viewer    | Viewer123! | VIEWER  |

### Authenticated requests

1. `POST /api/auth/login` with `{"username","password"}`  
2. `Authorization: Bearer <accessToken>`  

## API overview

### Auth

- `POST /api/auth/login` — public; returns `{ accessToken, tokenType, expiresInSeconds }`

### Users (`ADMIN` unless noted)

- `GET /api/users/me` — current user (any authenticated role)  
- `GET /api/users`, `GET /api/users/{id}`, `POST /api/users`, `PATCH /api/users/{id}`, `DELETE /api/users/{id}`  

### Financial records

- `GET /api/records` — filters: `from`, `to`, `category`, `type`, `q`, pagination  
- `GET /api/records/{id}`, `POST`, `PUT /api/records/{id}`, `DELETE` (soft delete)  

### Dashboard

- `GET /api/dashboard/summary`, `GET /api/dashboard/trends?granularity=monthly|weekly`, `GET /api/dashboard/recent?limit=`  

## Validation and errors

Bean Validation + `ApiErrorResponse` JSON. Status codes: `400`, `401`, `403`, `404`, `409`, `500`.

## Tests

```bash
./mvnw test
```

Uses **H2 in-memory** (test scope only). No Docker required for tests.

## Tradeoffs

- **Trends** use portable JPQL (`YEAR`/`MONTH`/`WEEK`); week numbering can differ slightly between H2 (tests) and PostgreSQL (production).  
- **Global exception handler** returns a generic message for unexpected `500` errors (details logged server-side).  
- **Pagination** uses Spring Data DTO page serialization (`@EnableSpringDataWebSupport` with `VIA_DTO`).  
