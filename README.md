# Access Monitoring Service (AMC)

A full-stack pet project focused on **authentication flows** and **security access analytics**.

This application combines a Spring Boot backend (REST API + PostgreSQL + JPA) with a static frontend (HTML/CSS/JavaScript) to simulate how login activity can be collected and visualized on a security-style dashboard.

## Features

- User registration and login endpoints
- Access event tracking for successful/failed login attempts
- Dashboard analytics:
  - Access statistics by time range (`1h`, `24h`, `7d`)
  - SLA success rate card
  - Top failed accounts (last 24h)
  - Heatmap-like alert level visualization
- PostgreSQL-backed persistence
- Docker Compose for local database setup

## Tech Stack

**Backend**
- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL

**Frontend**
- HTML5
- CSS3
- Vanilla JavaScript
- ApexCharts (for dashboard charts)

**DevOps / Tooling**
- Maven Wrapper (`mvnw`)
- Docker Compose

## Project Structure

```text
access-monitoring-service/
├─ src/main/java/com/dd01xc/service/
│  ├─ config/              # Security configuration
│  ├─ controller/          # REST endpoints (auth + access stats)
│  ├─ model/               # JPA entities + DTOs
│  ├─ repository/          # JPA repositories + native SQL analytics queries
│  └─ init/                # Initial data bootstrapping
├─ src/main/resources/
│  ├─ application.properties
│  └─ static/              # Frontend pages, scripts, styles, assets
├─ docker-compose.yml      # PostgreSQL container
└─ pom.xml
```

## Quick Start

### 1) Prerequisites

- Java 21+
- Docker Desktop (or Docker Engine)
- Git

### 2) Start PostgreSQL

```bash
docker compose up -d
```

This starts PostgreSQL with:
- DB: `security_db`
- User: `postgres`
- Password: `root`
- Host port: `5444`

### 3) Run the application

**Windows (PowerShell / CMD):**
```bash
./mvnw.cmd spring-boot:run
```

**Git Bash / Linux / macOS:**
```bash
./mvnw spring-boot:run
```

The app will start on the default Spring Boot port:
- `http://localhost:8080`

### 4) Open the UI

- Landing page: `http://localhost:8080/index.html`
- Login: `http://localhost:8080/login.html`
- Sign up: `http://localhost:8080/signup.html`
- Dashboard: `http://localhost:8080/dashboard.html`

## API Overview

### Auth

- `POST /api/auth/register`
  - Registers a new user
- `POST /api/auth/login`
  - Checks credentials, stores access event, returns user metadata

### Access Analytics

- `GET /api/access/stat/hourly?range=24h`
  - Returns time-bucketed successful/failed login stats
- `GET /api/access/stat/sla`
  - Returns success-rate percentage
- `GET /api/access/stat/top-failed`
  - Returns top failed accounts in the last 24h

## Current Limitations

This is a learning/pet project and currently includes simplified security behavior:

- Spring Security is configured with `permitAll()` for all routes
- Login response returns a placeholder token (`jwt-token-placeholder`)
- No role-based authorization guards on API endpoints yet
- Limited automated testing at this stage

## Roadmap

- [✓] Implement real JWT authentication
- [ ] Add request validation and global exception handling
- [ ] Add unit/integration tests for controllers and repositories
- [ ] Add role-based authorization for sensitive endpoints
- [ ] Add CI pipeline (build + test checks)
- [ ] Add production-ready deployment configuration

## Why This Project

This repository demonstrates my ability to:

- Build end-to-end full-stack functionality
- Design REST APIs and connect them to a frontend
- Model and query relational data for analytics dashboards
- Organize a Java/Spring project with clear package structure
- Use Docker for reproducible local infrastructure

## License

This project is distributed for educational and portfolio purposes.



