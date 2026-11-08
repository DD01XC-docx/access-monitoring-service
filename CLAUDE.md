# CLAUDE.md

Spring Boot 3.4 / Java 21 / Maven. JWT auth + access-event analytics, static frontend in `src/main/resources/static/`.
README.md already covers API, config, structure, limitations — read it only when needed, do not duplicate here.

## Commands
- Run DB: `docker compose up -d` (Postgres 16 on `127.0.0.1:5444`, db `security_db`, postgres/root)
- Run app: `JWT_SECRET=... ./mvnw spring-boot:run` (`JWT_SECRET` required, no default)
- Tests: `./mvnw test` (H2 in test scope)
- Single test: `./mvnw test -Dtest=AuthServiceTest`

## Code map (main package `com.dd01xc.service`)
- Auth flow: `controller/AuthController` -> `service/AuthService` -> `service/JwtService`; filter `config/JwtAuthenticationFilter`, rules `config/SecurityConfig`
- Errors: domain exceptions in `service/exception/`, mapped in `GlobalExceptionHandler` (401 bad creds, 403 inactive, 409 duplicate)
- Analytics: `controller/AccessController` -> `service/AccessStatService` -> `repository/AccessRepository` (native queries)
- Seed data: `init/DataInitializer`, `mock-data.sql`, `test-data.sql`

## Working rules
- Learning project: explain each fix before editing.
- Comments: English only, minimal.
- Branch per topic (`fix/...`), conventional commits (`fix(security): ...`, `docs: ...`).
- Ignore `target/`, `.DS_Store`, `static/frontend/images/` when searching.

## Subagents (`.claude/agents/`, model sonnet)
- `security-reviewer` — read-only audit of diff vs main or a named area
- `test-writer` — writes JUnit5/Mockito/MockMvc tests and runs them, never touches main code

## Session notes (append only what saves time next session; drop stale lines)
- JDK 21 + 25 installed; Mockito breaks on 25. Use `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./mvnw test` unless JAVA_HOME already points to 21.
- 2026-11-08: login hardened (dummy BCrypt hash `$2a$10$...` in AuthService, inactive account -> 401, password max 72). `AccountDisabledException` removed. 13 tests green.
- New agents in `.claude/agents/` need a Claude Code restart to be callable by name.
- Endpoint typo kept as-is: `/api/access/stat/responce-time` (frontend depends on it).
