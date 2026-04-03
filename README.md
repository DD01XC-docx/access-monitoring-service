# Access Monitoring Service

Access Monitoring Service is a full-stack portfolio project focused on authentication activity tracking, access analytics, and dashboard-driven monitoring.

The application combines a Spring Boot backend, PostgreSQL persistence, and a static frontend to simulate how login events can be collected, stored, and transformed into useful operational metrics.

## Overview

The project is centered around access events generated during authentication flows.  
Each event can capture data such as:

- username or email
- authentication status
- request duration
- IP address
- event timestamp

These events are then used to power a monitoring dashboard with visual summaries and recent activity logs.

## Features

- User registration and login
- JWT-based authentication flow
- Access event persistence in PostgreSQL
- Dashboard analytics for authentication activity
- Time-based access statistics (`1h`, `24h`, `7d`)
- SLA status card
- Alert level visualization
- Top failed accounts chart
- Agent status overview
- System health view
- Response time distribution chart
- Recent access logs feed on the dashboard
- Collapsible logs section
- Quick actions card with dashboard shortcuts

## Tech Stack

### Backend

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL

### Frontend

- HTML5
- CSS3
- Vanilla JavaScript
- ApexCharts

### Tooling

- Maven Wrapper
- Docker Compose

## Project Structure

```text
access-monitoring-service/
├─ src/main/java/com/dd01xc/service/
│  ├─ config/              # Security configuration and JWT filter
│  ├─ controller/          # REST controllers
│  ├─ init/                # Initial data bootstrapping
│  ├─ model/               # Entities and DTOs
│  ├─ repository/          # JPA repositories and analytics queries
│  ├─ service/             # Business logic
│  └─ service/exception/   # Exception handling
├─ src/main/resources/
│  ├─ application.properties
│  └─ static/              # Frontend pages, scripts, styles, images
├─ docker-compose.yml
└─ pom.xml
```

## Dashboard Highlights

The dashboard is designed as a compact monitoring surface for authentication-related activity.  
It currently includes:

- activity trends over multiple time ranges
- failed login account analysis
- latency distribution monitoring
- database health visibility
- recent access logs for quick inspection
- shortcut actions for common dashboard tasks

## Running the Project

### Prerequisites

- Java 21+
- Docker
- Git

### Start PostgreSQL

```bash
docker compose up -d
```

Default local database setup:

- database: `security_db`
- user: `postgres`
- password: `root`
- port: `5444`

### Run the application

```bash
./mvnw spring-boot:run
```

### Open in browser

- Landing page: `http://localhost:8080/index.html`
- Login: `http://localhost:8080/login.html`
- Sign up: `http://localhost:8080/signup.html`
- Dashboard: `http://localhost:8080/dashboard.html`

## API Overview

### Authentication

- `POST /api/auth/register`
- `POST /api/auth/login`

### Access Analytics

- `GET /api/access/stat/hourly?range=24h`
- `GET /api/access/stat/sla`
- `GET /api/access/stat/alert-lvls?range=24h`
- `GET /api/access/stat/top-failed`
- `GET /api/access/stat/agent-status`
- `GET /api/access/stat/health`
- `GET /api/access/stat/responce-time`
- `GET /api/access/logs/recent`

## Current Status

This repository is an active portfolio and learning project.  
The main goal is to demonstrate practical full-stack development with a focus on:

- backend architecture with Spring Boot
- relational data modeling and analytics queries
- frontend integration with authenticated APIs
- dashboard-oriented UI development
- iterative feature design and refinement

## Roadmap

- [x] Implement authentication flow
- [x] Persist access events
- [x] Add dashboard analytics
- [x] Add recent access logs feed
- [x] Add quick actions dashboard card
- [ ] Build profile/settings page
- [ ] Improve validation and error handling
- [ ] Expand automated testing
- [ ] Refine authorization rules
- [ ] Continue dashboard UX improvements

## Notes

- The frontend is intentionally implemented with static HTML, CSS, and JavaScript.
- The project is structured to stay understandable while still covering end-to-end functionality.
- Some areas are intentionally lightweight and will continue to evolve over time.

## License

This project is maintained for educational and portfolio purposes.
