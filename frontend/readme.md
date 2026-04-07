# Frontend

## Overview

Giao diện người dùng cho hệ thống Online Judge. Cho phép người dùng xem danh sách bài toán, nộp bài và theo dõi kết quả chấm điểm theo thời gian thực qua cơ chế Polling.

Mọi API call đều đi qua **API Gateway** (`gateway:8000`) — không gọi trực tiếp đến các service.

## Tech Stack

| Component       | Choice                    |
|-----------------|---------------------------|
| Framework       | React 18 + TypeScript     |
| Build Tool      | Vite 5                    |
| Styling         | Tailwind CSS 3            |
| Routing         | React Router v6           |
| Animation       | Framer Motion             |
| Icons           | Lucide React              |
| Package Manager | npm                       |
| Web Server      | Nginx (production)        |

## Pages

| Route | Description |
|-------|-------------|
| `/` | Landing page |
| `/login` | Đăng nhập — gọi User Service |
| `/signup` | Đăng ký — gọi User Service |
| `/problems` | Danh sách bài toán — gọi Problem Service |
| `/problems/:id` | Chi tiết bài + Code Editor + Submit |
| `/submissions` | Lịch sử bài nộp — gọi Submission Service |

## API Integration

Tất cả API calls được centralize trong `src/utils/api.ts`. JWT token được lưu vào `localStorage` và tự động đính kèm vào mọi request.

```
Browser → Nginx (:80) → /api/* → Gateway (:8000) → Services
```

## Running

```bash
# From project root
docker compose up frontend --build
```

Truy cập: `http://localhost:3000`

## Project Structure

```
frontend/
├── Dockerfile
├── nginx.conf        # Serve static + proxy /api/* to gateway
├── readme.md
└── src/
    ├── App.tsx
    ├── pages/        # Login, SignUp, Problems, ProblemDetail, Submissions
    ├── components/   # Reusable UI components
    ├── hooks/
    └── utils/
        ├── api.ts    # Centralized API client
        └── mockData.ts
```

## Environment Variables

| Variable       | Description            | Default                 |
|----------------|------------------------|-------------------------|
| `FRONTEND_PORT`| Host port mapping      | `3000`                  |
