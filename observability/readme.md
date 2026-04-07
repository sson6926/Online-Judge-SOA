# Observability Stack — PLG (Promtail + Loki + Grafana)

Hệ thống log tập trung cho toàn bộ microservices. Không cần sửa code service nào — tự động thu thập stdout của tất cả container.

## Thành phần

| Service | Vai trò | Port |
|---|---|---|
| **Promtail** | Thu thập log từ Docker container, gắn label, đẩy lên Loki | - |
| **Loki** | Lưu trữ và index log theo label | 3100 |
| **Grafana** | UI query và visualize log | 3001 |

## Cấu trúc thư mục

```
observability/
├── loki-config.yaml                        # Cấu hình Loki: nơi lưu data, schema, giới hạn
├── promtail-config.yaml                    # Cấu hình Promtail: scrape Docker logs, push lên Loki
└── grafana/
    └── provisioning/
        └── datasources/
            └── loki.yaml                   # Auto-provision Loki làm datasource mặc định
```

## Truy cập

```
http://localhost:3001
```

Đăng nhập bằng `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD` trong `.env` (mặc định: `admin/admin`).

## Cách xem log

Vào **Explore → Loki**, dùng LogQL:

```logql
# Tất cả service (bỏ qua observability stack)
{container=~".+", container!~".*(loki|grafana|promtail).*"}

# Theo service cụ thể
{service="submission-service"}
{service="problem-service"}
{service="judge-service"}
{service="user-service"}
{service="gateway"}

# Chỉ lỗi
{container=~".+", container!~".*(loki|grafana|promtail).*"} |= "ERROR"

# Tìm theo submission ID
{container=~".+"} |= "your-submission-id-here"
```

## Khởi động

Chạy cùng toàn bộ hệ thống:
```bash
docker compose up --build
```

Hoặc chỉ riêng observability stack:
```bash
docker compose up loki promtail grafana
```
