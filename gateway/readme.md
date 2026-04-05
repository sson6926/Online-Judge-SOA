# API Gateway

## Overview

Single entry point cho toàn bộ hệ thống Online Judge. Mọi request từ client đều đi qua đây trước khi được forward đến service tương ứng.

## Tech Stack

| Component         | Choice                              |
|-------------------|-------------------------------------|
| Framework         | Spring Boot 3.4.4                   |
| Gateway Engine    | Spring Cloud Gateway 2024.0.0       |
| Service Discovery | Spring Cloud Netflix Eureka Client  |
| Auth              | JWT (jjwt 0.12.6)                   |
| Rate Limiting     | Redis-backed Token Bucket           |
| Port              | 8000 (internal) / 8080 (host)       |

## Responsibilities

- **Routing**: Forward request đến đúng service qua Eureka (`lb://service-name`)
- **JWT Validation**: Validate Bearer token, inject `X-User-Id` và `X-User-Role` vào header cho downstream
- **Rate Limiting**: Giới hạn `POST /submissions` — 5 req/s, burst 10 (Token Bucket via Redis)
- **Security**: Disable Spring Security default auth, nhường hoàn toàn cho `JwtAuthenticationFilter`

## Routing Table

| External Path         | Target Service     | Auth Required |
|-----------------------|--------------------|---------------|
| `POST /api/users/signup` | user-service    | No            |
| `POST /api/users/login`  | user-service    | No            |
| `GET /api/users/**`      | user-service    | Yes           |
| `GET /api/problems/**`   | problem-service | No (public)   |
| `POST /api/problems/**`  | problem-service | Yes           |
| `POST /api/submissions/**` | submission-service | Yes + Rate Limited |
| `GET /api/submissions/**`  | submission-service | Yes           |

## Public vs Protected Routes

```
Public  (no token needed):
  POST /api/users/login
  POST /api/users/signup
  GET  /api/problems/**
  GET  /health

Protected (Bearer token required → 401 if missing/invalid):
  tất cả routes còn lại
```

## Header Injection

Sau khi validate JWT, gateway inject vào request trước khi forward:
```
X-User-Id:   <uuid từ JWT subject>
X-User-Role: <ADMIN | STUDENT từ JWT claim>
```
Downstream services dùng các header này mà không cần decode JWT lại.

## Running

```bash
# Từ thư mục mid-project-119695456/
docker compose up --build gateway
```

## Environment Variables

| Variable             | Default                              | Description              |
|----------------------|--------------------------------------|--------------------------|
| `JWT_SECRET`         | (required)                           | Shared secret với user-service |
| `REDIS_HOST`         | `redis`                              | Redis host               |
| `REDIS_PORT`         | `6379`                               | Redis port               |
| `EUREKA_DEFAULT_ZONE`| `http://discovery:8761/eureka`       | Eureka server URL        |

## Project Structure

```
gateway/
├── Dockerfile
├── pom.xml
└── src/main/
    ├── java/com/shawnix/api_gateway/
    │   ├── GatewayApplication.java
    │   ├── config/
    │   │   ├── SecurityConfig.java       # Disable default Spring Security
    │   │   └── RateLimiterConfig.java    # userKeyResolver bean
    │   ├── filter/
    │   │   └── JwtAuthenticationFilter.java  # GlobalFilter: validate + inject headers
    │   └── util/
    │       └── JwtUtil.java              # JWT parse/validate/generate
    └── resources/
        └── application.yaml             # Routes, Redis, Eureka config
```
