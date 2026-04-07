# Judge Service

## Overview

Judge Service là worker chấm bài bất đồng bộ cho hệ thống Online Judge.

- Nhận job chấm từ RabbitMQ queue `judge.jobs`
- Lấy testcases từ `problem-service` qua Feign + Eureka discovery
- Chạy compile/run trong Docker sandbox riêng cho từng submission
- So sánh output và trả verdict (`AC`, `WA`, `CE`, `TLE`, `MLE`, `RTE`, `SE`)
- Publish kết quả sang queue `judge.results`

## Tech Stack

| Component | Choice |
|-----------|--------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.13 |
| Messaging | RabbitMQ |
| Discovery | Eureka Client |
| HTTP Client | OpenFeign |
| Resilience | Resilience4j Circuit Breaker |
| Sandbox | Docker (`judge-sandbox`) |
| Metrics | Spring Actuator + Prometheus |

## Queue Topology

| Queue | Purpose |
|-------|---------|
| `judge.jobs` | Main job queue |
| `judge.jobs.retry.30s` | Retry tier 1 (TTL 30s -> dead-letter back to main) |
| `judge.jobs.retry.2m` | Retry tier 2 (TTL 2m -> dead-letter back to main) |
| `judge.jobs.retry.10m` | Retry tier 3 (TTL 10m -> dead-letter back to main) |
| `judge.jobs.dlq` | Job queue for retry-exhausted messages |
| `judge.results` | Verdict/result queue |
| `judge.results.dlq` | Dead-letter queue for result publishing failures |

## Resilience Behavior

- `ProblemServiceClient#getTestCasesByProblemId` được bọc Circuit Breaker.
- Khi dependency lỗi hoặc circuit mở, fallback ném `RetryableJudgeException`.
- `JudgeJobListener` route retry theo `x-retry-count`:
  - retry #1 -> `judge.jobs.retry.30s`
  - retry #2 -> `judge.jobs.retry.2m`
  - retry #3 -> `judge.jobs.retry.10m`
  - >3 -> `judge.jobs.dlq`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| GET | `/actuator/health` | Detailed health (actuator) |
| GET | `/actuator/prometheus` | Prometheus metrics |

## Running Locally

```bash
# From project root
docker compose up judge-service --build
```

## Project Structure

```text
judge-service/
├── Dockerfile
├── pom.xml
├── readme.md
├── sandbox/
│   └── Dockerfile                  # Base image used by runtime sandbox
└── src/main/java/com/shanwinx/judge_service/
    ├── client/                     # Feign clients
    ├── config/                     # RabbitMQ config, message converter
    ├── controller/                 # Health endpoint
    ├── dto/                        # Job/result payloads
    ├── exception/                  # Retryable exception types
    ├── listener/                   # Job consumer + retry routing
    ├── service/                    # Execution, testcase prep, sandbox orchestration
    └── service/executor/           # Language-specific compile/run commands
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `RABBITMQ_HOST` | RabbitMQ host | `localhost` |
| `RABBITMQ_USER` | RabbitMQ username | `guest` |
| `RABBITMQ_PASS` | RabbitMQ password | `guest` |
| `JUDGE_WORKSPACE_ROOT` | Workspace root directory | `/tmp/judge-jobs` |
| `JUDGE_WORKSPACE_HOST_ROOT` | Host root mapped for nested docker runs | _(empty)_ |
| `JUDGE_WORKSPACE_CLEANUP` | Cleanup workspace after each job | `true` |
| `JUDGE_SANDBOX_IMAGE` | Sandbox image name | `judge-sandbox:latest` |
| `EUREKA_DEFAULT_ZONE` | Eureka server URL | `http://discovery:8761/eureka/` |

## Notes

- `problemId` là bắt buộc trong job payload. Nếu thiếu, service trả `SE` với `Missing problem id`.
- So sánh output dùng normalize newline + trim trailing whitespace.
