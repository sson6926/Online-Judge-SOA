# Submission Service

## Overview
Tiếp nhận bài nộp mã nguồn từ người dùng, quản lý trạng thái chấm điểm và trả kết quả qua cơ chế Polling.

- Lưu bài nộp với trạng thái `PENDING` vào PostgreSQL
- Cache trạng thái vào Redis để phục vụ Polling nhanh
- Đẩy job vào RabbitMQ `judge.jobs` queue để Judge Worker xử lý
- Lắng nghe kết quả từ `judge.results` queue và cập nhật DB

## Tech Stack

| Component  | Choice             |
|------------|--------------------|
| Language   | Java 17 |
| Framework  | Spring Boot 3.4.4 |
| Database   | PostgreSQL |
| Cache      | Redis |
| Messaging  | RabbitMQ |
| Discovery  | Eureka Client |

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| POST | `/submissions` | Nộp bài mới (202 Accepted) |
| GET | `/submissions/{id}` | Polling kết quả chấm |

> Full API specification: [`docs/api-specs/submission-service.yaml`](../../docs/api-specs/submission-service.yaml)

## Running Locally

```bash
# From project root
docker compose up submission-service --build
```

## Project Structure

```
submission-service/
├── Dockerfile
├── pom.xml
├── readme.md
└── src/main/java/com/shawnix/submission/
    ├── SubmissionApplication.java
    ├── config/         # RabbitMQ, Redis config
    ├── controller/     # REST endpoints
    ├── dto/            # JudgeResultDTO
    ├── listener/       # RabbitMQ result consumer
    ├── model/          # Submission entity
    ├── repository/     # JPA repository
    └── service/        # Business logic
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SUBMISSION_DB_NAME` | Database name | `submission_db` |
| `DB_USER` | Database username | `admin` |
| `DB_PASSWORD` | Database password | `{password}` |
| `RABBITMQ_HOST` | RabbitMQ hostname | `rabbitmq` |
| `RABBITMQ_USER` | RabbitMQ username | `guest` |
| `RABBITMQ_PASS` | RabbitMQ password | `{password}` |
| `REDIS_HOST` | Redis hostname | `redis` |
