---
name: personal-finance
description: |
  Architecture and coding skill for the Personal Finance Manager project — a Spring Boot microservices system 
  with CQRS + Handler pattern, Kafka, gRPC, Redis Sentinel, and Eureka. Use this skill whenever implementing 
  any module, feature, handler, adapter, or infrastructure for this project. Also use when asked to generate 
  code, create a new feature, add a handler, set up a new service, configure Kafka/gRPC/Redis, or scaffold 
  project structure. Triggers include: any mention of "personal finance", "budget service", "transaction service",
  "OCR receipt", "group expense", "splitwise", "recurring bill", "savings goal", "auth service", or any 
  reference to the CQRS handler pattern, gRPC adapter pattern, or feature-based package structure used in 
  this project. Even if the user just says "tạo feature mới", "thêm handler", "tạo adapter", or 
  "implement module X" — use this skill.
---

# Personal Finance Manager — Architecture & Coding Skill

## Project Overview

Personal Finance Manager is a microservices system combining:
- Personal budget tracking (monthly/daily expense management)
- Group expense splitting (Splitwise-style with debt simplification)
- AI/OCR receipt scanning (reduce manual data entry)
- Recurring bills management (electricity, water, internet)
- Savings goals (save toward specific targets)

**Tech Stack**: Java 17+, Spring Boot 3.x, Kafka, gRPC, Redis Sentinel, Eureka, PostgreSQL, Gradle

## CRITICAL: File Location Rule

This project has two main folders at the root:
- `backend/` — all Java/Spring Boot microservices code lives here
- `frontend/` — frontend app (TBD)

**When implementing any backend feature, service, handler, adapter, or infrastructure:**
→ All files MUST be created/modified inside the `backend/` folder.
→ `backend/` IS the Gradle root project (build.gradle, settings.gradle, libs.versions.toml are in `backend/`).
→ Example paths:
  - `backend/commons/common-base/common-base.gradle`
  - `backend/services/auth-service/auth-service.gradle`
  - `backend/services/auth-service/src/main/java/com/personalfinance/auth/features/authen/...`
  - `backend/infrastructure/docker-compose.yml`

**The project-level `AGENT.md`** at the repo root contains the full project overview and routing rules. Read it if you need context about the overall project structure.

## Before Writing Any Code

Read the relevant reference files based on the task:

| Task | Read first |
|------|-----------|
| Creating/modifying any handler or feature | `references/architecture.md` (Sections 1-4) |
| Working with gRPC adapters | `references/architecture.md` (Section 5) |
| Working with Kafka events | `references/architecture.md` (Section 6) |
| Working with Redis cache | `references/architecture.md` (Section 7) |
| Setting up Gradle build files | `references/architecture.md` (Section 8) |
| Implementing a specific module's business logic | `references/modules.md` |
| Need the full Kafka event catalog or gRPC service map | `references/modules.md` (Sections 9-10) |

## Core Architecture Decisions (always follow these)

### 1. CQRS + Handler Pattern (NOT Controller → Service)
Every use case is a **Handler** class with `preHandle/doHandle/postHandle`. No traditional `@Service` classes for business logic. A `HandlerRegistry` dispatches requests by DTO class type.

### 2. Feature-based Package Structure
Each feature is a vertical slice: `features/{feature-name}/` containing its own controller, handlers, DTOs, repository, events. Shared code lives outside features.

### 3. Named Gradle Files
Each module uses `{module-name}.gradle` (e.g., `auth-service.gradle`), NOT `build.gradle`. Only the root project uses `build.gradle`. Auto-discovery in `settings.gradle`.

### 4. gRPC Adapter Sub-modules
Services exposing gRPC create an `{name}-adapter/` sub-module with proto files, server impl, client helper, and **bidirectional mapper**. Other services depend on the adapter as a library.

### 5. Kafka Event Publishing Template
Abstract `EventPublisher<S, E>` with mapper method — never publish domain model directly. Supports both manual and auto commit modes.

### 6. Redis Common Service
All Redis operations go through `common-cache` module's `CacheService` interface — never use `RedisTemplate` directly in business code.

### 7. Design Principles
- **Human-in-the-loop**: Never auto-modify financial data without user confirm
- **Privacy-first**: No bank connections, user self-inputs data
- **Notify, don't block**: Alerts only, never prevent transactions
- **Don't annoy**: No duplicate alerts, allow pre-adjustment

## Quick Reference: Project Structure

```
personal-finance-project/                     # repo root
├── AGENT.md                                  # project overview & AI routing
├── .agents/personal-finance/                 # this skill
├── frontend/                                 # frontend app (TBD)
└── backend/                                  # ← ALL backend code here
    ├── build.gradle                          # root only
    ├── settings.gradle                       # auto-discovery *.gradle
    ├── gradle/libs.versions.toml             # version catalog
    ├── commons/
    │   ├── common-base/common-base.gradle
    │   ├── common-cache/common-cache.gradle
    │   ├── common-event/common-event.gradle
    │   ├── common-grpc/common-grpc.gradle
    │   ├── common-security/common-security.gradle
    │   ├── common-notification/common-notification.gradle
    │   └── common-web/common-web.gradle
    ├── services/
    │   ├── auth-service/
    │   │   ├── auth-service.gradle
    │   │   ├── auth-adapter/auth-adapter.gradle
    │   │   └── src/.../features/{authen,profile,telegram}/
    │   ├── budget-service/
    │   ├── transaction-service/
    │   ├── ocr-service/                      # no adapter
    │   ├── group-expense-service/
    │   ├── notification-service/             # no adapter
    │   ├── recurring-bill-service/
    │   ├── saving-service/                   # no adapter
    │   ├── api-gateway/
    │   ├── config-service/                   # Spring Cloud Config Server
    │   └── discovery-service/                # Eureka Server (renamed from eureka-server)
    └── infrastructure/
```

## Quick Reference: Feature Structure Inside a Service

```
{service}/src/main/java/com/personalfinance/{service}/
├── features/
│   └── {feature-name}/
│       ├── controller/
│       │   └── {Feature}Controller.java      # thin, only dispatches
│       ├── handler/
│       │   ├── command/
│       │   │   └── Create{X}Handler.java
│       │   └── query/
│       │       └── Get{X}Handler.java
│       ├── dto/
│       │   ├── request/
│       │   └── response/
│       ├── repository/
│       ├── event/
│       └── consumer/                         # Kafka consumers for this feature
├── config/                                   # service-level configs
├── model/                                    # JPA entities shared cross-feature
└── {Service}Application.java
```

For detailed implementation patterns, code templates, and all business rules, read the reference files.