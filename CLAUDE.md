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
- Endpoint typo `/api/access/stat/responce-time` kept: `dashboard.js` uses it.

## Next
- React migration (branch `feat/react-frontend`): done scaffold, auth, login/signup, profile. Done: Maven build into jar, `SpaController` forwards SPA routes (add new routes there AND in `SPA_ROUTES`), Docker. Dashboard done (`features/dashboard/`, lazy-loaded, 30s polling). Landing done (`pages/LandingPage` + `features/landing/DashboardPreview`, plain SVG so recharts stays out of main bundle; no fake logos/testimonials, numbers mirror real config). Migration complete.
- Rate limit per IP on `/api/auth/**`, retention/purge for `access_events`, Flyway, ADMIN role, prod profile, CI, Testcontainers.
- Web tests pattern: `@WebMvcTest(X.class) @Import(SecurityConfig.class)` + `@MockitoBean` for `JwtService`, `UserDetailsService` (see `controller/ProtectedEndpointsTest`).
