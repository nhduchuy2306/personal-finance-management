---
name: trace-code
description: >-
  Use this skill when the user asks to trace, debug, or understand code flow
  in the microservices system. Covers tracing HTTP requests from gateway to database,
  Kafka event chains across services, authentication/authorization flow, and
  techniques for finding where specific features or business logic are implemented.
---

# Trace Code — Microservice Flow Analysis

## Quick Reference: Where to Start Tracing

| Want to trace... | Start from |
|---|---|
| API endpoint | `controllers/` → look for `@RequestMapping` / `@GetMapping` etc. |
| Business logic | `services/` → follow the service method called by controller |
| Database query | `repositories/` → `JpaRepository` methods or `@Query` |
| Kafka event flow | `messages/producers/` (sender) → `messages/consumers/` (receiver) |
| Auth/permissions | `@PreAuthorize` annotation on controller → `CustomPermissionEvaluator` |
| DTO ↔ Entity mapping | `mappers/` → MapStruct interface, `@Mapping` annotations |
| Cross-service call | Feign client interfaces or `WebClient`/`RestTemplate` calls |
| Request routing | `api-gateway/src/main/resources/application.yml` → route predicates |

## 1. Tracing an HTTP Request (Gateway → Service → DB)

### Step 1: API Gateway Route
```
File: api-gateway/src/main/resources/application.yml
```
Look for `spring.cloud.gateway.routes` to find which service handles which path:
```yaml
- id: event-service-route
  uri: lb://EVENT-SERVICE          # Eureka service name
  predicates:
    - Path=/events/**              # URL pattern
```
`lb://` means load-balanced via Eureka. The service name maps to `spring.application.name` in each service.

### Step 2: Controller (Entry Point)
```
File: {service}/src/main/java/com/gyp/{servicename}/controllers/{Resource}Controller.java
```
Pattern to grep:
```bash
# Find which controller handles a specific path
grep -r "@RequestMapping" --include="*.java" {service}/src/
grep -r "@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping" --include="*.java" {service}/src/
```

Key things to check:
- `@PreAuthorize` → what permissions are required
- `getCurrentOrganizationId()` → multi-tenant filtering
- `@Valid` → request validation via Jakarta Bean Validation
- The service method being called

### Step 3: Service (Business Logic)
```
File: {service}/src/main/java/com/gyp/{servicename}/services/{Resource}Service.java
```
Follow the method called by the controller. Look for:
- `repository.findBy*()` → database query
- `mapper.toEntity()` / `mapper.toResponseDto()` → DTO conversion
- `kafkaTemplate.send()` or producer calls → async events to other services
- `restTemplate` / `webClient` / `feignClient` → sync calls to other services

### Step 4: Repository (Database)
```
File: {service}/src/main/java/com/gyp/{servicename}/repositories/{Resource}Repository.java
```
- Spring Data derived queries: `findByOrganizationId(String orgId)`
- Custom queries: `@Query("SELECT e FROM EventEntity e WHERE ...")`
- Specification-based: `JpaSpecificationExecutor` with criteria classes

### Step 5: Entity ↔ DTO Mapping
```
File: {service}/src/main/java/com/gyp/{servicename}/mappers/{Resource}Mapper.java
```
- `toEntity(RequestDto)` → creates new entity from request
- `updateEntityFromDto(RequestDto, @MappingTarget Entity)` → updates existing entity
- `toResponseDto(Entity)` → converts entity to API response
- `@AfterMapping` → audit field population (createUser, timestamps)
- `@Named("methodName")` → helper methods for complex field mapping

## 2. Tracing Kafka Event Chains

### Find all topics
```
File: common-service/src/main/java/com/gyp/common/constants/TopicConstants.java
```

### Trace producer → consumer
```bash
# Find who produces to a topic
grep -r "TopicConstants.EVENT_CREATE" --include="*.java" */src/

# Find who consumes from a topic
grep -r "@KafkaListener" --include="*.java" */src/
```

### Known Event Chains

```
┌─────────────┐   event.command.create   ┌───────────────────┐
│ event-service├──────────────────────────►│salechannel-service│
│             ├──────────────────────────►│                   │
└──────┬──────┘   event.command.update    └───────────────────┘
       │
       │ generate.ticket.pdf.event
       ▼
┌──────────────┐   send-email.event   ┌──────────────────────┐
│ticket-service├─────────────────────►│notification-service  │
└──────────────┘                      └──────────────────────┘

┌─────────────┐   order.created.event   ┌─────────────┐
│order-service ├───────────────────────►│event-service │
└──────────────┘                        └─────────────┘
```

### Kafka consumer error handling
⚠️ No DLQ configured — check `try/catch` blocks in consumer methods for error handling behavior.

## 3. Tracing Authentication & Authorization

### JWT Token Flow
```
API Gateway → extracts JWT → forwards to service
Service → JwtDecoder validates token → SecurityContext populated
Controller → @PreAuthorize checks permissions
```

### Permission Model
```java
// In controller:
@PreAuthorize("@permissionEvaluator.hasPermission(authentication, #AppPerm.EVENT, #ActionPerm.READ)")

// This calls:
// File: common-service/.../security/CustomPermissionEvaluator.java
// JWT claim structure: { "permissions": { "EVENT": ["READ", "CREATE", "UPDATE", "DELETE"] } }
```

### Key Security Files
```
common-service/.../security/CustomPermissionEvaluator.java  → permission checking logic
common-service/.../security/JwtConfiguration.java           → JWT decoder bean
common-service/.../utils/SecurityUtils.java                 → getCurrentUserId(), getCurrentOrganizationId()
{service}/.../configurations/SecurityConfiguration.java     → PUBLIC_ENDPOINTS, filter chain
```

### Trace who can access what
```bash
# Find all permission-protected endpoints
grep -r "@PreAuthorize" --include="*.java" {service}/src/

# Find public endpoints (no auth required)
grep -r "PUBLIC_ENDPOINTS\|permitAll" --include="*.java" {service}/src/
```

## 4. Tracing Cross-Service Communication

### Sync Calls (Feign / WebClient / RestTemplate)
```bash
# Find Feign clients
grep -r "@FeignClient" --include="*.java" */src/

# Find RestTemplate calls
grep -r "restTemplate\." --include="*.java" */src/

# Find WebClient calls  
grep -r "webClient\." --include="*.java" */src/
```

### Service Discovery (Eureka)
Services register with `spring.application.name` (e.g., `EVENT-SERVICE`).
Other services call via Eureka name: `http://EVENT-SERVICE/events/...`

## 5. Useful Grep Patterns

```bash
# Find all entities in a service
grep -r "extends AbstractEntity" --include="*.java" {service}/src/

# Find all DTOs
grep -r "extends AbstractDto" --include="*.java" {service}/src/

# Find all mappers
grep -r "@Mapper" --include="*.java" {service}/src/

# Find all Kafka topics used by a service
grep -r "TopicConstants\." --include="*.java" {service}/src/

# Find where a specific field is set
grep -rn "\.setFieldName\|\.fieldName\s*=" --include="*.java" {service}/src/

# Find all scheduled tasks
grep -r "@Scheduled" --include="*.java" */src/

# Find all REST endpoints in entire project
grep -rn "@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping\|@RequestMapping" --include="*.java" */src/

# Find Flyway migrations for a service
ls {service}/src/main/resources/db/migration/

# Find application properties
cat {service}/src/main/resources/application.properties
cat {service}/src/main/resources/application.yml
```

## 6. Service-Specific Entry Points

| Service | Key Entry Points |
|---|---|
| **auth-service** | `AuthController` (login/register), `UserAccountController`, `OAuth2` configs |
| **event-service** | `EventController` (CRUD), `SeatInventoryController` (Redis seat holds), Kafka producers |
| **ticket-service** | `TicketController`, PDF/QR generation in services, Kafka consumer for ticket generation |
| **order-service** | `OrderController`, `CartController`, MoMo payment integration |
| **salechannel-service** | `SaleChannelController`, polymorphic config (BoxOffice/TicketShop/ApiPartner/MobileApp) |
| **notification-service** | `NotificationPollingService` (scheduled polling), `EmailService` |
| **bff-service** | BFF controllers aggregating multiple service calls via `WebClient` |

## 7. Debugging Checklist

When investigating a bug or unexpected behavior:

1. **Identify the endpoint** → which controller handles the request?
2. **Check permissions** → is `@PreAuthorize` blocking? Check JWT claims
3. **Check service logic** → add breakpoints or logs in the service method
4. **Check mapper** → is the DTO→Entity or Entity→DTO conversion correct?
5. **Check database** → use MySQL client on port 13306 to verify data
6. **Check Kafka** → if async, verify message was sent/received (Kafka UI on 8089)
7. **Check logs** → each service logs independently; check console output
8. **Check Eureka** → verify service registration at http://localhost:9761
