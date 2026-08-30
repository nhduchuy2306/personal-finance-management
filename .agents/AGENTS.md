# Personal Finance Manager — Project Overview

## What Is This Project?

A personal finance management system combining:
- **Budget tracking** — monthly/daily expense management with smart alerts
- **Group expense splitting** — Splitwise-style with AI/OCR receipt scanning
- **Recurring bills** — auto-remind electricity, water, internet payments
- **Savings goals** — save toward specific targets (gifts, iPhone, travel)

## Project Structure

```
personal-finance-project/
├── .agents/                    # AI skills & references
│   └── personal-finance/
│       ├── SKILL.md            # Architecture & coding patterns
│       └── references/
│           ├── architecture.md # Handler, gRPC, Kafka, Redis patterns
│           └── modules.md      # Business rules, DB, endpoints
│
├── AGENT.md                    # ← YOU ARE HERE — project overview
│
├── backend/                    # Java Spring Boot microservices
│   ├── build.gradle            # root Gradle build
│   ├── settings.gradle         # auto-discovery *.gradle
│   ├── gradle/
│   │   └── libs.versions.toml  # version catalog
│   ├── commons/                # shared libraries
│   │   ├── common-base/
│   │   ├── common-cache/
│   │   ├── common-event/
│   │   ├── common-grpc/
│   │   ├── common-security/
│   │   ├── common-notification/
│   │   └── common-web/
│   ├── services/               # microservices
│   │   ├── auth-service/
│   │   ├── budget-service/
│   │   ├── transaction-service/
│   │   ├── ocr-service/
│   │   ├── group-expense-service/
│   │   ├── notification-service/
│   │   ├── recurring-bill-service/
│   │   ├── saving-service/
│   │   ├── api-gateway/
│   │   ├── config-service/
│   │   └── discovery-service/
│   └── infrastructure/         # Docker, configs
│
└── frontend/                   # Frontend app (TBD)
```

## Routing Rules for AI

### When working on BACKEND code:
- All changes go inside the `backend/` folder.
- Read `.agents/personal-finance/SKILL.md` first for architecture patterns.
- Follow CQRS + Handler pattern (preHandle/doHandle/postHandle).
- Follow feature-based package structure (features/{name}/handler/command|query/).
- Use named Gradle files ({module-name}.gradle), NOT build.gradle.
- gRPC services use adapter sub-modules ({service}-adapter/).
- Never use traditional Controller → Service pattern.

### When working on FRONTEND code:
- All changes go inside the `frontend/` folder.
- (Frontend architecture TBD — to be defined when frontend development starts.)

### When working on INFRASTRUCTURE:
- Docker configs go in `backend/infrastructure/`.
- Gradle configs go in `backend/` root (build.gradle, settings.gradle, libs.versions.toml).

### When working on DOCUMENTATION or SKILLS:
- Project overview updates go in this file (AGENT.md).
- Skill updates go in `.agents/personal-finance/`.
- Business requirement changes go in `.agents/personal-finance/references/modules.md`.

## Tech Stack

### Backend
- **Language**: Java 17+
- **Framework**: Spring Boot 3.x
- **Build**: Gradle (Groovy DSL) with version catalog
- **Messaging**: Apache Kafka (event-driven, manual + auto commit)
- **RPC**: gRPC with Protobuf (service-to-service internal)
- **Cache**: Redis Sentinel (HA)
- **Discovery**: Spring Cloud Netflix Eureka
- **Database**: PostgreSQL (database-per-service)
- **Storage**: MinIO (S3-compatible, for OCR receipt images)
- **Container**: Docker + Docker Compose

### Frontend
- TBD

## Architecture Principles
1. **CQRS + Handler pattern** — each use case is one Handler class, not a Service method.
2. **Feature-based vertical slices** — each feature owns its controller, handlers, DTOs, repository.
3. **Event-driven** — services communicate async via Kafka events.
4. **gRPC for internal queries** — request-response between services uses gRPC, not REST.
5. **REST for external API** — only frontend/mobile calls use REST.
6. **Database-per-service** — each service has its own PostgreSQL schema.

## Design Principles
1. **Human-in-the-loop** — never auto-modify financial data without user confirmation.
2. **Privacy-first** — no bank connections, user self-inputs data.
3. **Notify, don't block** — alerts only, never prevent transactions.
4. **Don't annoy** — no duplicate alerts, allow pre-adjustment for known large expenses.

## Modules (8 total)
1. **User & Account** (auth-service) — auth, profile, Telegram integration
2. **Budget Period** (budget-service) — monthly budget, category allocation, auto-fill draft
3. **Daily/Monthly Expense** (transaction-service) — expense tracking, remaining calculation, threshold alerts
4. **OCR Receipt Scanning** (ocr-service) — AI-powered receipt reading, draft → confirm flow
5. **Group Expense Splitting** (group-expense-service) — Splitwise-style, debt simplification
6. **Notification** (notification-service) — Telegram Bot, anti-spam
7. **Recurring Bills** (recurring-bill-service) — periodic bill management & reminders
8. **Savings Goal** (saving-service) — save toward targets with daily/weekly contributions

## Currency & Timezone
- Currency: VNĐ (no multi-currency)
- Timezone: Asia/Ho_Chi_Minh (fixed)

## For Detailed Information
- **Architecture patterns, code templates**: `.agents/personal-finance/references/architecture.md`
- **Business rules, DB schemas, API endpoints**: `.agents/personal-finance/references/modules.md`