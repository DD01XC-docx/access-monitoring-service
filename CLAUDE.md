# CLAUDE.md

Spring Boot 3.4 / Java 21 / Maven. JWT auth + access-event analytics, React frontend in `frontend/` (built into the jar by frontend-maven-plugin).
README.md covers API, config, structure, limitations — read only when needed, do not duplicate here.

## Commands
- Tests: `.claude/scripts/test.sh [-Dtest=Class]` (forces JDK 21, compact output). Never plain `./mvnw test`: default JDK is 25 and Mockito breaks on it.
- Full stack: `docker compose up --build` (app :8080, `APP_PORT` to change). DB only: `docker compose up -d postgres` (Postgres 16, `127.0.0.1:5444`, db `security_db`, postgres/root)
- Run app: `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./mvnw spring-boot:run`; `JWT_SECRET` comes from gitignored `.env` (never print or commit it)
- Frontend (`frontend/`, React+TS+Vite+Tailwind v4+shadcn `base-nova` = Base UI, not Radix: use `render` prop, no `asChild`): `npm --prefix frontend run dev` (:5173, proxies `/api` to :8080), `npm --prefix frontend run build`. Background shells ignore `cd`: use `--prefix`.

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
- Lockout per email only (not email+IP), counted from `access_events`; locked attempts logged as `LOCKED` (not `FAILED`) so they do not extend the lock; response 429 + `Retry-After`. Duplicate signups log `REGISTER_FAILED` (must not count toward lockout). Login takes `pg_advisory_xact_lock(hashtext(email))` first (Postgres-only; H2 can't run it).
- Endpoint typo `/api/access/stat/responce-time` kept: frontend `lib/api.ts` uses it.
- UI copy in English; no fake testimonials/logos; landing numbers mirror real config.

## Next
- State 2026-11-08: React migration complete on `feat/react-frontend` (branched from `fix/auth-hardening`, neither merged to `main`). README rewritten. Merge order: `fix/auth-hardening` -> main, then `feat/react-frontend`.
- New SPA route: add to `SpaController` mapping AND `SPA_ROUTES`. Dashboard in `features/dashboard/` (lazy, 30s polling); landing preview is plain SVG to keep recharts out of main bundle.
- Todo: per-IP rate limit on `/api/auth/**`, `access_events` retention, Testcontainers, Flyway, ADMIN role, prod profile (CORS, trusted proxies, open-in-view off), CI. Optional: README screenshot `docs/dashboard.png`.
- Web tests pattern: `@WebMvcTest(X.class) @Import(SecurityConfig.class)` + `@MockitoBean` for `JwtService`, `UserDetailsService` (see `controller/ProtectedEndpointsTest`).
