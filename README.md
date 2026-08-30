# 💰 Personal Finance Manager

Hệ thống quản lý tài chính cá nhân — microservices architecture với Spring Boot, Kafka, gRPC, Redis Sentinel.

## Tính năng

| Module | Service | Mô tả |
|--------|---------|-------|
| Đăng nhập & Tài khoản | `auth-service` | Xác thực, hồ sơ cá nhân, tích hợp Telegram |
| Ngân sách | `budget-service` | Ngân sách tháng, phân bổ theo danh mục |
| Chi tiêu | `transaction-service` | Theo dõi chi tiêu, cảnh báo ngưỡng |
| Quét hóa đơn | `ocr-service` | AI/OCR đọc hóa đơn, tạo nháp → xác nhận |
| Chia tiền nhóm | `group-expense-service` | Splitwise-style, tối ưu nợ |
| Thông báo | `notification-service` | Telegram Bot, chống spam |
| Hóa đơn định kỳ | `recurring-bill-service` | Nhắc điện, nước, internet |
| Mục tiêu tiết kiệm | `saving-service` | Tiết kiệm theo mục tiêu |

## Tech Stack

- **Java 21+** / **Spring Boot 4.1.0**
- **Gradle** (Groovy DSL) + version catalog
- **PostgreSQL** — shared database
- **Apache Kafka** — event-driven messaging
- **gRPC + Protobuf** — service-to-service communication
- **Redis Sentinel** — caching (HA)
- **Spring Cloud Eureka** — service discovery
- **Spring Cloud Config** — centralized configuration
- **MinIO** — S3-compatible storage (receipt images)
- **Docker + Docker Compose** — containerization

## Kiến trúc

```
                    ┌──────────────┐
                    │  API Gateway │ :8080
                    │  (WebFlux)   │
                    └──────┬───────┘
                           │ REST
        ┌──────────────────┼──────────────────┐
        │                  │                  │
  ┌─────┴─────┐    ┌──────┴──────┐    ┌──────┴──────┐
  │   auth    │    │   budget    │    │ transaction │  ...
  │  :8081    │    │   :8082     │    │   :8083     │
  └─────┬─────┘    └──────┬──────┘    └──────┬──────┘
        │     gRPC        │                  │
        ├─────────────────┤      Kafka       │
        │                 ├──────────────────┤
        └────────┬────────┴──────────────────┘
                 │
     ┌───────────┼───────────┐
     │           │           │
┌────┴────┐ ┌───┴───┐ ┌─────┴─────┐
│PostgreSQL│ │ Kafka │ │   Redis   │
│  :5432   │ │ :9092 │ │ Sentinel  │
└─────────┘ └───────┘ └───────────┘
```

## Cấu trúc dự án

```
personal-finance-management/
├── .gitignore
├── README.md
├── backend/
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradle/
│   │   └── libs.versions.toml
│   ├── commons/
│   │   ├── common-base/
│   │   ├── common-cache/
│   │   ├── common-event/
│   │   ├── common-grpc/
│   │   ├── common-security/
│   │   ├── common-notification/
│   │   └── common-web/
│   ├── services/
│   │   ├── api-gateway/          :8080
│   │   ├── auth-service/         :8081  gRPC :28081
│   │   ├── budget-service/       :8082  gRPC :28082
│   │   ├── transaction-service/  :8083  gRPC :28083
│   │   ├── ocr-service/          :8084
│   │   ├── group-expense-service/:8085  gRPC :28085
│   │   ├── notification-service/ :8086
│   │   ├── recurring-bill-service/:8087 gRPC :28087
│   │   ├── saving-service/       :8088
│   │   ├── config-service/       :8888
│   │   └── discovery-service/    :8761
│   └── infrastructure/
│       └── docker-compose.yml
└── frontend/                     (TBD)
```

## Khởi chạy

### Yêu cầu

- Java 21+
- Docker & Docker Compose

### 1. Khởi động infrastructure

```bash
cd backend/infrastructure
docker-compose up -d
```

### 2. Build project

```bash
cd backend
./gradlew build -x test
```

### 3. Khởi động services (theo thứ tự)

```bash
# 1. Discovery Service (Eureka)
./gradlew :discovery-service:bootRun

# 2. Config Service
./gradlew :config-service:bootRun

# 3. Các services còn lại (chạy song song)
./gradlew :auth-service:bootRun
./gradlew :budget-service:bootRun
./gradlew :transaction-service:bootRun
# ... các service khác

# 4. API Gateway (cuối cùng)
./gradlew :api-gateway:bootRun
```

### Profiles

| Profile | Mô tả | Cách dùng |
|---------|-------|-----------|
| `dev` | Local development (localhost) | Mặc định |
| `stag` | Docker staging | `SPRING_PROFILES_ACTIVE=stag` |

## Kiến trúc code

### CQRS + Handler Pattern

Mỗi use case là một Handler class riêng biệt, không dùng Service truyền thống:

```
features/
└── {feature-name}/
    ├── controller/
    │   └── {Feature}Controller.java
    ├── handler/
    │   ├── command/
    │   │   └── Create{Feature}Handler.java    # preHandle → doHandle → postHandle
    │   └── query/
    │       └── Get{Feature}Handler.java
    ├── dto/
    ├── entity/
    └── repository/
```

### Giao tiếp giữa services

- **REST** — frontend/mobile → API Gateway → services
- **gRPC** — service ↔ service (request-response)
- **Kafka** — service → service (event-driven, async)

## Config tập trung

Toàn bộ config nằm trong `config-service`:

```
configs/
├── db-config.yml            # JPA, Flyway (shared)
├── auth-service.yml          # Port, gRPC, JWT
├── budget-service.yml
├── ...
├── dev/
│   ├── cache-dev.yml         # Redis localhost
│   ├── db-config-dev.yml     # DB localhost
│   ├── kafka-dev.yml         # Kafka localhost
│   └── discovery-dev.yml     # Eureka localhost
└── stag/
    ├── cache-stag.yml        # Redis docker
    ├── db-config-stag.yml    # DB docker
    ├── kafka-stag.yml        # Kafka docker
    └── discovery-stag.yml    # Eureka docker
```

## Nguyên tắc thiết kế

1. **Human-in-the-loop** — không tự động thay đổi dữ liệu tài chính
2. **Privacy-first** — không kết nối ngân hàng, người dùng tự nhập
3. **Notify, don't block** — chỉ cảnh báo, không chặn giao dịch
4. **Don't annoy** — không spam thông báo trùng lặp

## Currency & Timezone

- **Currency**: VNĐ (không hỗ trợ đa tiền tệ)
- **Timezone**: Asia/Ho_Chi_Minh (cố định)
