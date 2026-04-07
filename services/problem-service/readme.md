# Problem Service (`problem-service`)

## Overview

`problem-service` chịu trách nhiệm **quản lý bài toán (Problem)** và **testcases** trong hệ thống Online Judge.

- **Dữ liệu sở hữu**:
  - Metadata của bài toán (title/description/time_limit/memory_limit/difficulty)
  - Metadata testcase (gắn với problem) + object key trỏ tới file input/output trên MinIO
- **Tích hợp hạ tầng**:
  - **PostgreSQL** (container `db-problem`) để lưu metadata
  - **MinIO** (container `minio`) để lưu file testcase (input/output)
  - **Eureka** (container `discovery`) để service discovery
- **Operations chính**:
  - CRUD Problems
  - Upload/Xoá testcase (kèm xoá object trên MinIO)
  - Endpoint nội bộ trả về **presigned URL** để Judge tải testcases khi chấm

## Luồng hoạt động (High-level flow)

### 1) Startup trong Docker Compose

- Container `problem-service` chạy Spring Boot trên port `5001` và **register lên Eureka**.
- Kết nối DB qua DNS Compose: `db-problem:5432` và schema `${PROBLEM_DB_NAME}`.
- Kết nối MinIO qua `${MINIO_ENDPOINT}` (thường trỏ `http://minio:9000`) và bucket `${MINIO_BUCKET}`.

### 2) Luồng request chính

- **GET `/health`**
  - Trả JSON trạng thái để gateway/compose healthcheck hoặc monitoring kiểm tra.

- **GET `/problems`**
  - Đọc danh sách problems từ PostgreSQL → trả mảng `Problem`.

- **POST `/problems`**
  - Validate payload `ProblemRequest` → insert vào PostgreSQL → trả `201` + `Problem` vừa tạo.

- **GET `/problems/{id}`**
  - Query theo `id` trong PostgreSQL → trả `Problem` hoặc `404`.

- **DELETE `/problems/{id}`**
  - Xoá problem trong PostgreSQL.
  - Đồng thời xoá toàn bộ testcase liên quan:
    - Xoá record testcase trong PostgreSQL
    - Xoá object file input/output trên MinIO (đảm bảo không rò “rác” storage)

### 3) Luồng testcase (file + metadata)

- **POST `/problems/{id}/testcases`** (multipart)
  - Validate `id` problem tồn tại.
  - Nhận file `input` và `output`.
  - Upload 2 file lên MinIO (theo “object key” được service quản lý).
  - Lưu metadata testcase (gồm object key) vào PostgreSQL.
  - Trả `201` + `TestCaseResponse`.

- **DELETE `/problems/testcases/{testCaseId}`**
  - Tìm testcase theo `testCaseId` trong PostgreSQL.
  - Xoá object input/output tương ứng trên MinIO.
  - Xoá record testcase trong PostgreSQL.
  - Trả `204` hoặc `404`.

### 4) Luồng nội bộ cho Judge (presigned URL)

- **GET `/problems/internal/{id}/testcases`** *(internal)*
  - Lấy danh sách testcase của problem từ PostgreSQL.
  - Với mỗi testcase: generate **presigned URL (TTL ~ 1 giờ)** từ MinIO dựa trên object key.
  - Trả về danh sách `TestCaseResponse` chứa `input_url`/`output_url` để Judge Worker tải trực tiếp từ MinIO.

## Tech Stack

| Component  | Choice |
|------------|--------|
| Language   | Java 17 |
| Framework  | Spring Boot 3 (Web, Data JPA, Actuator) |
| Database   | PostgreSQL |
| Object Storage | MinIO (presigned URL) |
| Service Discovery | Netflix Eureka (Spring Cloud) |
| Observability | Micrometer + Prometheus registry |

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| GET | `/problems` | Lấy danh sách bài toán |
| POST | `/problems` | Tạo bài toán mới |
| GET | `/problems/{id}` | Lấy chi tiết một bài toán |
| DELETE | `/problems/{id}` | Xóa bài toán + toàn bộ testcase (kèm file trên MinIO) |
| POST | `/problems/{id}/testcases` | Upload testcase (multipart: input/output) |
| DELETE | `/problems/testcases/{testCaseId}` | Xóa một testcase (kèm file trên MinIO) |
| GET | `/problems/internal/{id}/testcases` | *(Internal)* List testcase + presigned URL cho Judge |

> Full API specification: [`docs/api-specs/problem-service.yaml`](../../docs/api-specs/problem-service.yaml)

## Running Locally

```bash
# From project root
docker compose up problem-service --build
```

## Project Structure

```
problem-service/
├── Dockerfile
├── pom.xml
├── readme.md
└── src/
    └── main/
        ├── java/                 # Spring Boot source
        └── resources/
            └── application.yml
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PROBLEM_SERVICE_PORT` | Port map ra host (Compose) | `5001` |
| `PROBLEM_DB_NAME` | Tên database cho problem-service | `problem_db` *(fallback trong code)* |
| `DB_USER` | DB username | `admin` *(fallback trong code)* |
| `DB_PASSWORD` | DB password | `changeme` *(fallback trong code)* |
| `MINIO_ENDPOINT` | MinIO endpoint (nội bộ Compose) | *(required)* |
| `MINIO_ROOT_USER` | MinIO access key | *(required)* |
| `MINIO_ROOT_PASSWORD` | MinIO secret key | *(required)* |
| `MINIO_BUCKET` | Bucket lưu testcases | *(required)* |



