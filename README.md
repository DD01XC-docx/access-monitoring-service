# Security Audit & Access Monitoring Platform

A mini‑SIEM (Security Information and Event Management) platform for centralized security event collection, analysis, and alerting across multiple applications.

## Overview

This project focuses on security audit logging, access monitoring, and suspicious activity detection in a realistic, production‑like environment. It is designed as a portfolio‑ready full‑stack security project that demonstrates backend, database, frontend, and Docker skills. 

## Features

- Centralized **audit logging** from multiple client applications via a REST ingestion API. 
- **Role‑based access control** (ADMIN, ANALYST, VIEWER) for secure access to the platform. 
- Powerful **search and filtering** of events by date range, user, application, IP address, type, and severity. 
- Interactive **analytics dashboard** with charts (timeline, top users, event type distribution, severity summary).
- Configurable **alert rules** and scheduled evaluation for suspicious activity detection.
- **AOP‑based automatic audit logging** via a custom `@Audited` annotation.
- **Dockerized** multi‑service setup (backend, database, frontend, demo app) with `docker-compose`. 

## Architecture

- Backend: Java 17, Spring Boot 3 (Web, Spring Data JPA, Spring Security, AOP).
- Database: PostgreSQL 15 with JSONB for event details and indexes for analytical queries.
- Frontend: Vanilla JavaScript, HTML5, CSS3 served as static assets.
- Containerization: Docker + docker‑compose, multi‑stage builds, Nginx for frontend.
- Integration: Optional `demo-app` that simulates a real client application sending events to the platform.

### High‑Level Flow

1. Client applications send security events to the `/ingest` endpoint using an API key.
2. The backend validates and stores events in PostgreSQL (`audit_events` table).
3. Scheduled jobs evaluate alert rules against recent events and generate alerts when conditions match.
4. Analysts use the web UI to search events, review alerts, and monitor dashboards.

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.x, Spring Web, Spring Data JPA, Spring Security, Lombok.
- **Database:** PostgreSQL 15 (JSONB, indexes, aggregation queries).
- **Frontend:** HTML5, CSS3, Vanilla JavaScript, Chart.js (or similar) for charts.
- **Containerization:** Docker, docker‑compose.
- **Version Control:** Git, GitHub.

## Core Domain Models

- `User` – platform user with email, password hash, and role (`ADMIN`, `ANALYST`, `VIEWER`).
- `Application` – external application registered in the platform, identified by an API key.
- `AuditEvent` – individual security/audit event (type, severity, user, application, IP, user agent, JSON details).
- `AlertRule` – configuration for alert conditions and thresholds.
- `Alert` – generated alert instance when a rule is triggered.

## Key Backend Capabilities

### Authentication & Authorization

- Spring Security with JWT or session‑based authentication.
- Endpoints:
  - `POST /auth/register` – register a new user.
  - `POST /auth/login` – authenticate and obtain token.
  - `GET /auth/me` – get current user info.
- Role‑based restrictions:
  - `/admin/**` – ADMIN only.
  - Event analytics and configuration endpoints protected for ADMIN/ANALYST.

### Event Ingestion API

- Endpoint: `POST /ingest`.
- Authentication via `X-API-KEY` header mapped to a registered `Application`.
- Example payload:
  ```json
  {
    "userId": "123",
    "username": "alice",
    "eventType": "LOGIN_FAILED",
    "severity": "HIGH",
    "ip": "1.2.3.4",
    "userAgent": "Mozilla/5.0...",
    "details": {
      "reason": "wrong_password",
      "attempt": 3
    }
  }
