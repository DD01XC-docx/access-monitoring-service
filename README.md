# Access Monitoring Service

Spring Boot service that records authentication events and exposes them as access analytics for a monitoring dashboard.

> **Status:** work in progress. Portfolio and learning project; not intended for production use.

## Overview

Every login and registration attempt is stored as an access event with:

| Field              | Description                         |
|--------------------|-------------------------------------|
| `usernameOrEmail`  | Identifier supplied by the client   |
| `status`           | `SUCCESS` or `FAILED`               |
| `ipAddress`        | Client IP (`X-Forwarded-For` aware) |
| `durationMs`       | Server-side processing time         |
| `createdAt`        | Event timestamp                     |

The dashboard aggregates these events into activity trends, failed-login rankings, latency distribution, and a recent-activity feed.

## Tech Stack

| Layer    | Technology                                                       |
|----------|------------------------------------------------------------------|
| Backend  | Java 21, Spring Boot 3.4, Spring Security, Spring Data JPA       |
| Auth     | Stateless JWT (jjwt 0.12), BCrypt password hashing               |
| Database | PostgreSQL 16 (Docker Compose)                                   |
| Frontend | Static HTML, CSS, vanilla JavaScript, ApexCharts                 |
| Build    | Maven Wrapper                                                    |

## Requirements

- JDK 21+
- Docker with Docker Compose

## Configuration

| Variable            | Required | Default    | Description                                            |
|---------------------|----------|------------|--------------------------------------------------------|
| `JWT_SECRET`        | yes      | —          | Base64-encoded HMAC key, at least 32 bytes when decoded |
| `JWT_EXPIRATION_MS` | no       | `86400000` | Token lifetime in milliseconds (24 h)                  |
| `DB_PASSWORD`       | no       | `root`     | PostgreSQL password                                    |

The application fails to start if `JWT_SECRET` is missing or too short.

Generate a key:

```bash
export JWT_SECRET=$(openssl rand -base64 32)
```

## Running Locally

```bash
docker compose up -d          # PostgreSQL on localhost:5444, database security_db
./mvnw spring-boot:run        # application on localhost:8080
```

| Page      | URL                                    |
|-----------|----------------------------------------|
| Landing   | http://localhost:8080/index.html       |
| Sign up   | http://localhost:8080/signup.html      |
| Login     | http://localhost:8080/login.html       |
| Dashboard | http://localhost:8080/dashboard.html   |
| Profile   | http://localhost:8080/myprofile.html   |

Optional seed data: `mock-data.sql`, `test-data.sql`.

## API

All endpoints except `/api/auth/**` require the header `Authorization: Bearer <token>`.

### Authentication

| Method | Path                 | Description                         |
|--------|----------------------|-------------------------------------|
| POST   | `/api/auth/register` | Create account                      |
| POST   | `/api/auth/login`    | Authenticate and receive a JWT      |
| POST   | `/api/auth/check`    | Verify that a token is valid        |

Validation rules: username 3–20 characters, valid email, password at least 8 characters.

### Profile

| Method | Path              | Description                     |
|--------|-------------------|---------------------------------|
| GET    | `/api/profile/me` | Current user's profile          |

### Access Analytics

| Method | Path                              | Parameters                       |
|--------|-----------------------------------|----------------------------------|
| GET    | `/api/access/stat/hourly`         | `range` = `1h` \| `24h` \| `7d`  |
| GET    | `/api/access/stat/alert-lvls`     | `range` = `1h` \| `24h` \| `7d`  |
| GET    | `/api/access/stat/sla`            | —                                |
| GET    | `/api/access/stat/top-failed`     | —                                |
| GET    | `/api/access/stat/agent-status`   | —                                |
| GET    | `/api/access/stat/health`         | —                                |
| GET    | `/api/access/stat/responce-time`  | —                                |
| GET    | `/api/access/logs/recent`         | —                                |

### Error Responses

| Status | Cause                                        |
|--------|----------------------------------------------|
| 400    | Request validation failed (field → message)  |
| 401    | Invalid credentials, missing or invalid token |
| 403    | Account is not active                        |
| 409    | Username or email already registered         |

## Project Structure

```text
src/main/java/com/dd01xc/service/
├── config/              Security configuration, JWT filter
├── controller/          REST controllers
├── init/                Startup data initialization
├── model/               JPA entities and DTOs
├── repository/          Repositories and analytics queries
└── service/             Business logic
    └── exception/       Domain exceptions, global handler
src/main/resources/
├── application.properties
└── static/              Frontend pages, scripts, styles, assets
```

## Known Limitations

- No automated tests yet.
- CORS is open to all origins.
- `X-Forwarded-For` is trusted without a proxy allow-list.
- Schema is managed by `ddl-auto=update`; no migrations.
- No role-based authorization beyond authenticated / anonymous.

## Roadmap

- [x] Registration and login with JWT
- [x] Access event persistence
- [x] Dashboard analytics and recent logs
- [x] Request validation and error handling
- [x] Profile endpoint and page
- [ ] Unit and integration tests
- [ ] Role-based authorization
- [ ] Database migrations (Flyway)
- [ ] Restricted CORS configuration

## License

Educational and portfolio use only. No license is granted for redistribution.
