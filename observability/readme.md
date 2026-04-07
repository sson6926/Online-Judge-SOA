# Observability Stack — PLG + Prometheus

Hệ thống giám sát tập trung: log tập trung (PLG) + metrics (Prometheus + Grafana).

## Thành phần

| Service | Vai trò | Port |
|---|---|---|
| **Promtail** | Thu thập log từ Docker container, đẩy lên Loki | - |
| **Loki** | Lưu trữ và index log theo label | 3100 |
| **Prometheus** | Scrape metrics từ `/actuator/prometheus` mỗi 15s | 9090 |
| **RabbitMQ Exporter** | Export RabbitMQ queue metrics sang Prometheus | 9419 |
| **Grafana** | UI query log (Loki) và vẽ biểu đồ metrics (Prometheus) | 3001 |

## Cấu trúc thư mục

```
observability/
├── loki-config.yaml                          # Cấu hình Loki
├── promtail-config.yaml                      # Scrape Docker logs → Loki
├── prometheus.yml                            # Scrape targets: 5 services + RabbitMQ
└── grafana/
    └── provisioning/
        ├── datasources/
        │   ├── loki.yaml                     # Auto-provision Loki datasource
        │   └── prometheus.yaml               # Auto-provision Prometheus datasource
        └── dashboards/
            ├── dashboards.yaml               # Khai báo folder dashboard
            └── microservices.json            # Dashboard: RabbitMQ + HTTP + JVM + DB
```

## Truy cập

| URL | Mô tả |
|---|---|
| `http://localhost:3001` | Grafana UI (admin/admin) |
| `http://localhost:9090` | Prometheus UI |
| `http://localhost:3100` | Loki API |
| `http://localhost:9419/metrics` | RabbitMQ raw metrics |

## Xem log (Grafana → Explore → Loki)

```logql
# Tất cả service (bỏ qua observability stack)
{container=~".+", container!~".*(loki|grafana|promtail).*"}

# Theo service cụ thể
{service="submission-service"}
{service="judge-service"}

# Chỉ lỗi
{container=~".+", container!~".*(loki|grafana|promtail).*"} |= "ERROR"

# Tìm theo submission ID
{container=~".+"} |= "your-submission-id-here"
```

## Xem metrics (Grafana → Dashboards → Microservices Overview)

Dashboard tự động load, gồm 4 section:
- RabbitMQ queue size, unacked messages
- HTTP request rate, error rate, P99 latency
- JVM heap memory, threads, GC
- DB connection pool

Xem file `grafana-dashboard-guide.md` ở root project để hiểu từng panel.

## Khởi động

```bash
docker compose up --build
```
