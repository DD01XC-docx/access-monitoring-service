# CLAUDE.md

Spring Boot 3.4 / Java 21 / Maven. JWT auth + access-event analytics, static frontend in `src/main/resources/static/`.
README.md covers API, config, structure, limitations — read only when needed, do not duplicate here.

## Commands
- Tests: `.claude/scripts/test.sh [-Dtest=Class]` (forces JDK 21, compact output). Never plain `./mvnw test`: default JDK is 25 and Mockito breaks on it.
- Run DB: `docker compose up -d` (Postgres 16, `127.0.0.1:5444`, db `security_db`, postgres/root)
- Run app: `JWT_SECRET=... ./mvnw spring-boot:run` (`JWT_SECRET` required)

## Code map (`com.dd01xc.service`)
- Auth: `controller/AuthController` -> `service/AuthService` -> `service/JwtService`; `config/JwtAuthenticationFilter`, `config/SecurityConfig`
- Errors: `service/exception/` + `GlobalExceptionHandler` (401 bad creds or inactive, 409 duplicate, 400 validation)
- Analytics: `controller/AccessController` -> `service/AccessStatService` -> `repository/AccessRepository` (native SQL)
- Seed: `init/DataInitializer`, `mock-data.sql`, `test-data.sql`

## Rules
- Learning project: explain each change before editing.
- Comments: English only, minimal.
- Branch per topic, conventional commits.
- Skip `target/`, `.DS_Store`, `static/frontend/images/` when searching.
- Subagents (`security-reviewer`, `test-writer`) cost ~40k tokens each: use for sizable work only.

## Decisions (do not revisit)
- Login is email-only; `findByUsername` stays for JWT filter and profile.
- Inactive account returns same 401 as bad password (no enumeration).
- Endpoint typo `/api/access/stat/responce-time` kept: `dashboard.js` uses it.

## Next
- Lockout after N failed logins, rate limit, Flyway, ADMIN role, prod profile, CI, Testcontainers.
- Web tests pattern: `@WebMvcTest(X.class) @Import(SecurityConfig.class)` + `@MockitoBean` for `JwtService`, `UserDetailsService` (see `controller/ProtectedEndpointsTest`).
