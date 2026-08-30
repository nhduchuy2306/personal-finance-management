---
name: build-and-deploy
description: >-
  Use this skill when the user asks to build, test, run, or deploy the project.
  Covers Gradle build commands, running individual services, Docker compose,
  and troubleshooting common build issues.
---

# Build & Deploy

## Build Commands

### Full Build (skip tests)
```bash
.\gradlew.bat clean build -x test
```

### Build Single Service
```bash
.\gradlew.bat :{service-name}:build -x test
```

### Run Tests
```bash
.\gradlew.bat test
.\gradlew.bat :{service-name}:test
```

### Generate TypeScript Definitions
```bash
.\gradlew.bat :{service-name}:generateTypeScript
```

## Running Locally

### Start Order (Important!)
1. **discovery-service** (port 9761) — must start first
2. **api-gateway** (port 9999) — routes all requests
3. **Other services** — any order

### Run Individual Service
```bash
cd {service-name}
..\gradlew.bat bootRun --args='--spring.profiles.active=dev'
```

### Infrastructure (Docker)
```bash
cd infrastructure/dockers
docker-compose up -d  # MySQL(13306), Kafka(9092), Redis(6379), Keycloak(18080), MinIO(19000)
```

## Service Port Map

| Service | Port |
|---------|------|
| discovery-service | 9761 |
| api-gateway | 9999 |
| auth-service | 9000 |
| event-service | 9001 |
| ticket-service | 9002 |
| order-service | 9003 |
| salechannel-service | 9004 |
| notification-service | 9005 |
| bff-service | 9010 |

## Swagger UI
Each service: `http://localhost:{port}/swagger-ui.html`
Kafka UI: `http://localhost:8089`

## Common Build Issues

### 1. "Unknown property" in MapStruct
**Cause:** Using `@Mapping(target = "auditField", ignore = true)` on fields from `AbstractEntity` 
**Fix:** Use `unmappedTargetPolicy = ReportingPolicy.IGNORE` on `@Mapper` instead

### 2. "@Builder will ignore the initializing expression"
**Cause:** Field has initializer (e.g., `= new ArrayList<>()`) but no `@Builder.Default`
**Fix:** Add `@Builder.Default` annotation before the field

### 3. PowerShell exit code 1 on BUILD SUCCESSFUL
**Cause:** PowerShell treats Java compiler `Note:` on stderr as errors
**Reality:** Build is actually successful; check for `BUILD SUCCESSFUL` in output

### 4. Eureka connection refused
**Cause:** discovery-service not started
**Fix:** Start discovery-service first on port 9761

## Profiles

| Profile | Description |
|---------|-------------|
| `dev` | Default. Localhost MySQL(13306), Kafka(9092), Redis(6379) |
| `prod` | Docker hostnames. Used in docker-compose |
| `no-kafka` | Disables Kafka auto-configuration |
| `no-cache` | Disables Redis caching |

## Environment Variables
- `JWT_SECRET_TOKEN` — JWT signing key
- `MOMO_PARTNER_CODE` — MoMo payment partner code
- `MOMO_ACCESS_KEY` — MoMo access key
- `MOMO_SECRET_KEY` — MoMo secret key
