# Discovery Service — Eureka Server

Service registry trung tâm cho hệ thống. Các microservice tự đăng ký vào đây khi khởi động, API Gateway tra cứu để biết địa chỉ thực của từng service.

## Tech Stack

- Java 17 + Spring Boot
- Spring Cloud Netflix Eureka Server

## Port

| Port | Mô tả |
|------|-------|
| `8761` | Eureka Server + Dashboard UI |

## Eureka Dashboard

```
http://localhost:8761
```

Xem danh sách các service đang đăng ký, trạng thái UP/DOWN và số instance.

## Vai trò trong hệ thống

```
Mỗi service khi khởi động
    → tự đăng ký vào Eureka (service-id, host, port)

API Gateway khi nhận request
    → tra cứu Eureka để resolve địa chỉ lb://problem-service
    → forward request đến đúng instance
```

Nhờ đó Gateway không cần hardcode host/port của từng service. Khi scale thêm instance Judge Service, Eureka tự cân bằng tải (client-side load balancing).

## Cấu hình

Service này **không tự đăng ký vào chính nó** (`registerWithEureka: false`) và **không fetch registry** (`fetchRegistry: false`) vì nó là server, không phải client.

## Khởi động

```bash
docker compose up discovery
```
