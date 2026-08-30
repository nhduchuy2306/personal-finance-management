# Architecture Reference — Personal Finance Manager

## Table of Contents
1. [CQRS + Handler Pattern](#1-cqrs--handler-pattern)
2. [Feature-based Package Structure](#2-feature-based-package-structure)
3. [Common Modules Detail](#3-common-modules-detail)
4. [Controller Pattern](#4-controller-pattern)
5. [gRPC Adapter Pattern](#5-grpc-adapter-pattern)
6. [Kafka Event Publishing Pattern](#6-kafka-event-publishing-pattern)
7. [Redis Common Cache Pattern](#7-redis-common-cache-pattern)
8. [Gradle Build Structure](#8-gradle-build-structure)
9. [Docker Infrastructure](#9-docker-infrastructure)

---

## 1. CQRS + Handler Pattern

Every use case is one Handler class. No `@Service` classes for business logic.

### 1.1 Base Interfaces — Request/Response contracts (in common-base)

**All requests MUST implement `BaseRequest`. All responses MUST implement `BaseResponse`.**

```java
// Marker interfaces — enforce type safety in Handler/HandlerRegistry
public interface BaseRequest {}
public interface BaseResponse {}

// Auto-populates userId from UserContext via AbstractController.dispatch()
public interface UserAwareRequest extends BaseRequest {
    UUID getUserId();
    void setUserId(UUID userId);
}

// Pagination — includes toPageable() default helper
public interface PageableRequest extends BaseRequest {
    int getPage(); void setPage(int page);
    int getSize(); void setSize(int size);
    String getSortBy(); void setSortBy(String sortBy);
    String getSortDir(); void setSortDir(String sortDir);
    default Pageable toPageable() { /* safe defaults, max 100 */ }
}

// Singleton for handlers returning nothing (e.g., delete)
public final class VoidResponse implements BaseResponse {
    public static final VoidResponse INSTANCE = new VoidResponse();
}

// Paginated results wrapper with Page<T> factory
public class PageableResponse<T> implements BaseResponse {
    private List<T> content; int page; int size; long totalElements; int totalPages; boolean last; boolean first;
    public static <T> PageableResponse<T> from(Page<T> page) { /* ... */ }
}
```

### 1.2 Handler Interface (in common-base)

```java
public interface Handler<Req extends BaseRequest, Res extends BaseResponse> {
    void preHandle(Req request);
    Res doHandle(Req request);
    void postHandle(Req request, Res response);
    Class<Req> getRequestType();
}
```

### 1.3 Abstract Base Handler

- **Only `doHandle()` is mandatory** — `preHandle()` and `postHandle()` are optional no-ops.
- **`getRequestType()` is auto-resolved** via `GenericTypeResolver` + `ClassUtils.getUserClass()` (CGLIB-proxy safe).

```java
@Slf4j
public abstract class AbstractHandler<Req extends BaseRequest, Res extends BaseResponse>
        implements Handler<Req, Res> {

    public Res execute(Req request) {
        preHandle(request);
        Res response = doHandle(request);
        try { postHandle(request, response); }
        catch (Exception e) { log.error("postHandle failed: {}", e.getMessage(), e); }
        return response;
    }

    @Override public void preHandle(Req request) { /* no-op */ }
    @Override public void postHandle(Req request, Res response) { /* no-op */ }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Req> getRequestType() {
        Class<?> userClass = ClassUtils.getUserClass(this);
        Class<?>[] typeArgs = GenericTypeResolver.resolveTypeArguments(userClass, AbstractHandler.class);
        return (Class<Req>) typeArgs[0]; // auto-resolved — no override needed
    }
}
```

### 1.4 Handler Registry (Mediator)

```java
@Component
public class HandlerRegistry {
    private final Map<Class<?>, Handler<?, ?>> handlers;

    public HandlerRegistry(List<Handler<?, ?>> handlerList) { /* auto-discovery */ }

    @SuppressWarnings("unchecked")
    public <Req extends BaseRequest, Res extends BaseResponse> Res dispatch(Req request) {
        Handler<Req, Res> handler = (Handler<Req, Res>) handlers.get(request.getClass());
        if (handler == null) throw new HandlerNotFoundException(...);
        return ((AbstractHandler<Req, Res>) handler).execute(request);
    }
}
```

### 1.5 Example Handler Implementation

**Note**: No `getRequestType()` override needed — auto-resolved from generics.

```java
@Component
@RequiredArgsConstructor
public class CreateExpenseHandler extends AbstractHandler<CreateExpenseRequest, CreateExpenseResponse> {
    // CreateExpenseRequest implements UserAwareRequest → userId auto-populated
    // CreateExpenseResponse implements BaseResponse

    private final TransactionRepository repository;
    private final BudgetGrpcClient budgetClient;
    private final TransactionEventPublisher eventPublisher;

    @Override
    public void preHandle(CreateExpenseRequest request) {
        if (request.getAmount() <= 0) throw new BusinessException(ErrorCode.INVALID_AMOUNT);
        budgetClient.validateCategoryExists(request.getCategoryId());
    }

    @Override
    @Transactional
    public CreateExpenseResponse doHandle(CreateExpenseRequest request) {
        Transaction transaction = Transaction.builder()
            .userId(request.getUserId()) // ← from UserAwareRequest, not UserContext
            .categoryId(request.getCategoryId())
            .amount(request.getAmount())
            .build();
        return CreateExpenseResponse.from(repository.save(transaction));
    }

    @Override
    public void postHandle(CreateExpenseRequest request, CreateExpenseResponse response) {
        eventPublisher.publish(response.toEvent());
    }
}
```

### 1.6 Rules
- **One handler = one use case**. Never combine multiple operations in one handler.
- **Commands** change state (create, update, delete) → go in `handler/command/`
- **Queries** only read → go in `handler/query/`
- **preHandle**: validation + enrichment only. Throw exceptions to abort.
- **doHandle**: core logic + DB operations. This is where `@Transactional` goes.
- **postHandle**: Kafka events, cache invalidation. Failures logged, not thrown.
- **Request DTOs**: implement `BaseRequest`, or `UserAwareRequest` if userId needed, or `PageableRequest` for pagination.
- **Response DTOs**: implement `BaseResponse`. Use `VoidResponse.INSTANCE` for void handlers.

---

## 2. Feature-based Package Structure

### 2.1 Rules for Splitting Features
- Each feature has **1 controller** (or no controller if Kafka consumer / scheduled job only).
- Split by **domain context**, not CRUD. Example: `expense/` and `income/` are separate features even though both use `transactions` table — because business rules differ.
- If a handler in feature A needs logic from feature B **within the same service** → create an interface in the service's root `common/` package. Never import cross-feature directly.

### 2.2 Complete Feature Map

```
auth-service/features/
├── authen/          → LoginHandler, RegisterHandler, RefreshTokenHandler
├── profile/         → GetProfileHandler, UpdateProfileHandler
└── telegram/        → LinkTelegramHandler, UnlinkTelegramHandler

budget-service/features/
├── period/          → CreateBudgetPeriodHandler, ConfirmDraftHandler, GetActiveBudgetHandler
├── allocation/      → SetCategoryAllocationHandler, GetAllocationsHandler
├── category/        → CreateCategoryHandler, ListCategoriesHandler, UpdateCategoryHandler
└── auto-fill/       → AutoFillDraftHandler (scheduled + handler)

transaction-service/features/
├── expense/         → CreateExpenseHandler, UpdateExpenseHandler, DeleteExpenseHandler, ListExpensesHandler
├── income/          → RecordIncomeHandler, ListIncomesHandler
├── budget-check/    → CheckBudgetThresholdHandler, GetDailyRemainingHandler, GetMonthlyRemainingHandler
└── summary/         → GetDailySummaryHandler, GetMonthlySummaryHandler, GetCategoryBreakdownHandler

ocr-service/features/
├── upload/          → UploadReceiptHandler
├── processing/      → ProcessOCRHandler (async Kafka consumer)
├── review/          → GetParsedResultHandler, EditDraftHandler
└── confirm/         → ConfirmReceiptHandler, ConfirmAsGroupExpenseHandler, DiscardReceiptHandler

group-expense-service/features/
├── group/           → CreateGroupHandler, ListGroupsHandler, AddMemberHandler
├── expense/         → CreateSharedExpenseHandler, ListGroupExpensesHandler
├── balance/         → GetGroupBalancesHandler, GetSimplifiedDebtsHandler
└── settlement/      → SettleDebtHandler, ListSettlementsHandler

notification-service/features/
├── telegram/        → SendTelegramMessageHandler
├── dispatcher/      → Kafka consumers for all event types
└── dedup/           → CheckAndMarkSentHandler (anti-spam)

recurring-bill-service/features/
├── bill/            → CreateBillHandler, UpdateBillHandler, DeleteBillHandler, ListBillsHandler
├── payment/         → ConfirmPaymentHandler, GetPaymentHistoryHandler
├── reminder/        → CheckDueSoonHandler, CheckOverdueHandler (scheduled)
└── estimate/        → SuggestEstimateUpdateHandler

saving-service/features/
├── goal/            → CreateGoalHandler, UpdateGoalHandler, CancelGoalHandler, ListGoalsHandler
├── contribution/    → ContributeToGoalHandler, ListContributionsHandler
└── progress/        → GetProgressHandler, CheckBehindScheduleHandler (scheduled)
```

---

## 3. Common Modules Detail

### 3.1 common-base
Core foundation.

Contains:
- **Request interfaces**: `BaseRequest`, `UserAwareRequest`, `PageableRequest`
- **Response interfaces**: `BaseResponse`, `VoidResponse`, `PageableResponse<T>`
- **Handler pattern**: `Handler`, `AbstractHandler`, `HandlerRegistry`
- **Response wrapper**: `ApiResponse`
- **Entity**: `BaseEntity` (id, createdAt, updatedAt)
- **Exceptions**: `GlobalExceptionHandler`, `BusinessException`, `HandlerNotFoundException`, `ErrorCode` enum
- **Utilities**: `DateUtils` (VN timezone), `MoneyUtils` (VNĐ formatting), `AppConstants`

```java
// ApiResponse.java
@Getter
@Setter
@Builder
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorDetail error;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().success(true).data(data).build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .error(ErrorDetail.of(code, message))
            .build();
    }
}
```

```java
// BaseEntity.java
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### 3.2 common-cache
Redis Sentinel abstraction. See Section 7 for full detail.

### 3.3 common-event
Kafka abstraction + ALL event DTOs. See Section 6 for full detail.

### 3.4 common-grpc
Shared gRPC configs + interceptors. See Section 5 for detail on interceptors.

### 3.5 common-security
JWT + auth shared across all services.

Contains: `JwtTokenProvider` (generate), `JwtTokenValidator` (validate + extract claims), `JwtProperties` (configurable secret, expiry), `JwtAuthenticationFilter` (Spring Security filter), `UserContext` (ThreadLocal-based userId propagation), base `SecurityConfig`.

```java
// UserContext.java — ThreadLocal userId for downstream use
public class UserContext {
    private static final ThreadLocal<UUID> currentUserId = new ThreadLocal<>();

    public static void set(UUID userId) { currentUserId.set(userId); }
    public static UUID getCurrentUserId() { return currentUserId.get(); }
    public static void clear() { currentUserId.remove(); }
}
```

### 3.6 common-notification
Telegram Bot API client. Only used by notification-service.

### 3.7 common-web
CORS, Jackson config, Swagger/OpenAPI, request logging filter, `GlobalResponseAdvice`, **`AbstractController`**.

Depends on: common-base, common-security.

---

## 4. Controller Pattern

Controllers are **thin dispatchers only**. No business logic.
All controllers **extend `AbstractController`** which provides:
- Centralized `HandlerRegistry` injection
- `dispatch(request)` method that auto-wraps in `ApiResponse.success()`
- Auto-population of `userId` for `UserAwareRequest` DTOs from `UserContext`
- Overridable `enrichRequest()` hook for future cross-cutting concerns

```java
// AbstractController (in common-web)
public abstract class AbstractController {
    private final HandlerRegistry registry;

    protected AbstractController(HandlerRegistry registry) { this.registry = registry; }

    protected <Res extends BaseResponse> ApiResponse<Res> dispatch(BaseRequest request) {
        enrichRequest(request);
        return ApiResponse.success(registry.dispatch(request));
    }

    protected void enrichRequest(BaseRequest request) {
        if (request instanceof UserAwareRequest u && u.getUserId() == null) {
            u.setUserId(UserContext.getCurrentUserId());
        }
    }
}
```

```java
// Concrete controller example
@RestController
@RequestMapping("/api/v1/transactions")
public class ExpenseController extends AbstractController {

    public ExpenseController(HandlerRegistry registry) { super(registry); }

    @PostMapping
    public ApiResponse<CreateExpenseResponse> create(
            @Valid @RequestBody CreateExpenseRequest request) {
        return dispatch(request); // userId auto-populated if UserAwareRequest
    }

    @GetMapping
    public ApiResponse<PageableResponse<ExpenseResponse>> list(
            @Valid ListExpensesRequest request) {
        return dispatch(request); // paginated queries too
    }
}
```

---

## 5. gRPC Adapter Pattern

### 5.1 Adapter Sub-module Structure

**IMPORTANT**: The adapter module is a **shared library** (relay) — it contains only what consuming services need:
- Proto file (generates stubs)
- DTOs (domain objects shared with consumers)
- Client (convenience wrapper for consumers)

The gRPC **server** and its **handlers** live in the **service module** itself (not the adapter), because they need access to the database and repositories.

```
auth-service/
├── auth-adapter/                              # ← shared library for consumers
│   ├── auth-adapter.gradle
│   └── src/
│       ├── main/proto/
│       │   └── auth_service.proto
│       └── main/java/com/personalfinance/auth/adapter/
│           ├── client/
│           │   └── AuthGrpcClient.java         # ← consumers use this
│           └── dto/
│               └── UserDto.java               # ← shared domain DTO
├── src/main/java/com/personalfinance/auth/
│   └── grpc/server/                           # ← server lives in service, NOT adapter
│       ├── AuthGrpcServer.java                # ← @GrpcService, delegates to handlers
│       ├── GetUserByIdGrpcHandler.java        # ← uses UserRepository (DB access)
│       ├── GetUsersByIdsGrpcHandler.java
│       └── GetUserTelegramChatIdGrpcHandler.java
```

### 5.2 gRPC Handler Pattern (separate from REST handlers)

gRPC has its own handler base with **mandatory bidirectional mapper** — because gRPC models (protobuf generated) are different from domain models.

```java
// In common-grpc module
package com.personalfinance.common.grpc.handler;

/**
 * Base handler for gRPC service methods.
 * G_REQ = gRPC request (protobuf generated)
 * G_RES = gRPC response (protobuf generated)
 * D_REQ = Domain request (internal DTO)
 * D_RES = Domain response (internal DTO)
 */
public abstract class AbstractGrpcHandler<G_REQ, G_RES, D_REQ, D_RES> {

    /**
     * Convert gRPC request → domain request.
     * MUST be overridden — forces developer to explicitly define the mapping.
     */
    protected abstract D_REQ mapFromGrpc(G_REQ grpcRequest);

    /**
     * Convert domain response → gRPC response.
     * MUST be overridden — forces developer to explicitly define the mapping.
     */
    protected abstract G_RES mapToGrpc(D_RES domainResponse);

    /**
     * Core business logic using domain objects.
     */
    protected abstract D_RES handle(D_REQ domainRequest);

    /**
     * Template method — do not override.
     * Handles the full flow: map in → process → map out.
     */
    public final G_RES execute(G_REQ grpcRequest) {
        D_REQ domainRequest = mapFromGrpc(grpcRequest);
        D_RES domainResponse = handle(domainRequest);
        return mapToGrpc(domainResponse);
    }
}
```

### 5.3 Example: Auth gRPC Handler

```java
@Component
@RequiredArgsConstructor
public class GetUserByIdGrpcHandler 
    extends AbstractGrpcHandler<GetUserRequest, UserResponse, UUID, UserDto> {

    private final UserRepository userRepository;

    @Override
    protected UUID mapFromGrpc(GetUserRequest grpcRequest) {
        return UUID.fromString(grpcRequest.getUserId());
    }

    @Override
    protected UserResponse mapToGrpc(UserDto user) {
        return UserResponse.newBuilder()
            .setUserId(user.getId().toString())
            .setDisplayName(user.getDisplayName())
            .setEmail(user.getEmail())
            .setTelegramChatId(user.getTelegramChatId() != null ? user.getTelegramChatId() : "")
            .build();
    }

    @Override
    protected UserDto handle(UUID userId) {
        return userRepository.findById(userId)
            .map(UserDto::from)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
```

### 5.4 gRPC Server (delegates to handlers)

```java
@GrpcService
@RequiredArgsConstructor
public class AuthGrpcServer extends AuthServiceGrpc.AuthServiceImplBase {

    private final GetUserByIdGrpcHandler getUserByIdHandler;
    private final GetUsersByIdsGrpcHandler getUsersByIdsHandler;

    @Override
    public void getUserById(GetUserRequest request, StreamObserver<UserResponse> observer) {
        try {
            UserResponse response = getUserByIdHandler.execute(request);
            observer.onNext(response);
            observer.onCompleted();
        } catch (BusinessException e) {
            observer.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
```

### 5.5 gRPC Client (in adapter module, used by other services)

```java
@Component
@RequiredArgsConstructor
public class AuthGrpcClient {

    @GrpcClient("auth-service")
    private AuthServiceGrpc.AuthServiceBlockingStub stub;

    public UserDto getUserById(UUID userId) {
        GetUserRequest request = GetUserRequest.newBuilder()
            .setUserId(userId.toString())
            .build();
        UserResponse response = stub.getUserById(request);
        return mapToDomain(response);
    }

    private UserDto mapToDomain(UserResponse response) {
        return UserDto.builder()
            .id(UUID.fromString(response.getUserId()))
            .displayName(response.getDisplayName())
            .email(response.getEmail())
            .telegramChatId(response.getTelegramChatId().isEmpty() ? null : response.getTelegramChatId())
            .build();
    }
}
```

### 5.6 Key Rule
The **mandatory mapper** (`mapFromGrpc` + `mapToGrpc`) ensures:
- Developer always sees which protobuf fields map to which domain fields
- No accidental leaking of domain internals into gRPC contract
- When reading code, immediately clear what conversion is happening

### 5.7 Which Services Have Adapters

| Service | Has Adapter | Consumers |
|---------|-------------|-----------|
| auth-service | auth-adapter | budget, notification, group-expense |
| budget-service | budget-adapter | transaction |
| recurring-bill-service | recurring-bill-adapter | budget |
| group-expense-service | group-expense-adapter | (future BFF/dashboard) |
| transaction-service | transaction-adapter | (future) |
| ocr-service | NO | — |
| notification-service | NO | — |
| saving-service | NO | — |

---

## 6. Kafka Event Publishing Pattern

### 6.1 Base Event (in common-event)

```java
package com.personalfinance.common.event.model;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    private String eventId;       // UUID, generated automatically
    private String eventType;     // e.g. "transaction.created"
    private LocalDateTime timestamp;
    private String source;        // service name that produced this event
    private UUID userId;          // who triggered this event
}
```

### 6.2 Abstract Event Publisher (Template Method)

The key insight: domain models should NEVER be published directly to Kafka. Always map to a smaller, purpose-built event DTO.

```java
package com.personalfinance.common.event.publisher;

/**
 * Abstract event publisher with template method pattern.
 * S = Source object (domain model or response DTO)
 * E = Event object (what gets published to Kafka)
 * 
 * Forces developer to explicitly define:
 * 1. What topic to publish to
 * 2. How to map source object → event object
 * 3. What key to use for partitioning
 */
@Slf4j
public abstract class AbstractEventPublisher<S, E extends BaseEvent> {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    protected AbstractEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * MUST override: which Kafka topic to publish to.
     */
    protected abstract String getTopic();

    /**
     * MUST override: convert source object → event DTO.
     * This is where you select ONLY the fields needed for the event,
     * not the entire domain object.
     */
    protected abstract E mapToEvent(S source);

    /**
     * MUST override: partition key for ordering guarantee.
     * Typically userId or entityId.
     */
    protected abstract String getPartitionKey(E event);

    /**
     * Optional hook: enrich event before publishing.
     * Default fills eventId + timestamp + source.
     */
    protected E enrichEvent(E event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        }
        return event;
    }

    /**
     * Template method — do not override.
     * Maps source → event → enriches → publishes.
     */
    public final void publish(S source) {
        try {
            E event = mapToEvent(source);
            event = enrichEvent(event);
            String key = getPartitionKey(event);
            
            kafkaTemplate.send(getTopic(), key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event to {}: {}", getTopic(), ex.getMessage(), ex);
                    } else {
                        log.debug("Published event to {} [partition={}, offset={}]",
                            getTopic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    }
                });
        } catch (Exception e) {
            log.error("Error publishing to {}: {}", getTopic(), e.getMessage(), e);
        }
    }

    /**
     * Batch publish — for cases like OCR where multiple items are confirmed at once.
     */
    public final void publishAll(List<S> sources) {
        sources.forEach(this::publish);
    }
}
```

### 6.3 Example: Transaction Event Publisher

```java
@Component
public class TransactionEventPublisher 
    extends AbstractEventPublisher<Transaction, TransactionCreatedEvent> {

    public TransactionEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }

    @Override
    protected String getTopic() {
        return KafkaTopics.TRANSACTION_CREATED;
    }

    @Override
    protected TransactionCreatedEvent mapToEvent(Transaction source) {
        // Only publish what consumers need — NOT the entire Transaction entity
        return TransactionCreatedEvent.builder()
            .userId(source.getUserId())
            .categoryId(source.getCategoryId())
            .amount(source.getAmount())
            .type(source.getType().name())
            .transactionDate(source.getTransactionDate())
            .source(source.getSource().name())
            .build();
    }

    @Override
    protected String getPartitionKey(TransactionCreatedEvent event) {
        return event.getUserId().toString();  // all events for same user → same partition → ordering
    }
}
```

### 6.4 Kafka Topics Constant (in common-event)

```java
public final class KafkaTopics {
    private KafkaTopics() {}
    
    // Transaction
    public static final String TRANSACTION_CREATED = "transaction.created";
    public static final String TRANSACTION_CONFIRMED = "transaction.confirmed";
    
    // Budget
    public static final String BUDGET_WARNING = "budget.warning";
    public static final String BUDGET_CRITICAL = "budget.critical";
    public static final String BUDGET_DRAFT_CREATED = "budget.draft_created";
    
    // Receipt/OCR
    public static final String RECEIPT_UPLOADED = "receipt.uploaded";
    public static final String RECEIPT_PARSED = "receipt.parsed";
    public static final String RECEIPT_CONFIRMED = "receipt.confirmed";
    public static final String RECEIPT_CONFIRMED_GROUP = "receipt.confirmed_group";
    
    // Group Expense
    public static final String GROUP_EXPENSE_CREATED = "group_expense.created";
    public static final String SETTLEMENT_COMPLETED = "settlement.completed";
    public static final String GROUP_SETTLE_REMINDER = "group_settle.reminder";
    
    // Recurring Bills
    public static final String RECURRING_BILL_DUE_SOON = "recurring_bill.due_soon";
    public static final String RECURRING_BILL_OVERDUE = "recurring_bill.overdue";
    public static final String RECURRING_BILL_PAID = "recurring_bill.paid";
    
    // Savings
    public static final String SAVING_CONTRIBUTION = "saving.contribution";
    public static final String SAVING_GOAL_COMPLETED = "saving.goal_completed";
    public static final String SAVING_BEHIND_SCHEDULE = "saving.behind_schedule";
    public static final String SAVING_REMINDER = "saving.reminder";
}
```

### 6.5 Kafka Consumer Config — Manual vs Auto Commit

```java
// In common-event module

/**
 * Two consumer factory beans: one for auto-commit, one for manual.
 * Choose per-consumer based on use case.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * AUTO commit — for non-critical consumers where at-least-once is OK
     * and message loss on crash is acceptable.
     * Use case: notification sending, analytics, logging.
     */
    @Bean
    public ConsumerFactory<String, Object> autoCommitConsumerFactory() {
        Map<String, Object> props = commonProps();
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5000);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean("autoCommitListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> autoCommitListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(autoCommitConsumerFactory());
        return factory;
    }

    /**
     * MANUAL commit — for critical consumers where message must not be lost.
     * Consumer must call acknowledgment.acknowledge() after successful processing.
     * Use case: transaction creation, budget updates, settlement processing.
     */
    @Bean
    public ConsumerFactory<String, Object> manualCommitConsumerFactory() {
        Map<String, Object> props = commonProps();
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean("manualCommitListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> manualCommitListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(manualCommitConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    private Map<String, Object> commonProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.personalfinance.*");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
}
```

### 6.6 Consumer Usage Examples

```java
// MANUAL commit — critical: creating transactions from confirmed receipts
@Component
@RequiredArgsConstructor
public class ReceiptConfirmedConsumer {

    private final HandlerRegistry registry;

    @KafkaListener(
        topics = KafkaTopics.RECEIPT_CONFIRMED,
        groupId = "transaction-service",
        containerFactory = "manualCommitListenerFactory"   // ← manual
    )
    public void handle(ReceiptConfirmedEvent event, Acknowledgment ack) {
        try {
            CreateExpenseRequest request = mapFromEvent(event);
            registry.dispatch(request);
            ack.acknowledge();   // ← commit only after successful processing
        } catch (Exception e) {
            log.error("Failed to process receipt confirmed event: {}", event.getEventId(), e);
            // NOT acknowledging → message will be redelivered
        }
    }
}

// AUTO commit — non-critical: sending notifications
@Component
@RequiredArgsConstructor
public class BudgetWarningConsumer {

    private final SendTelegramMessageHandler handler;

    @KafkaListener(
        topics = KafkaTopics.BUDGET_WARNING,
        groupId = "notification-service",
        containerFactory = "autoCommitListenerFactory"    // ← auto
    )
    public void handle(BudgetWarningEvent event) {
        // if this fails, losing a notification is acceptable
        handler.execute(buildNotification(event));
    }
}
```

### 6.7 When to Use Manual vs Auto

| Scenario | Mode | Reason |
|----------|------|--------|
| Creating transactions from events | MANUAL | Data integrity — cannot lose a confirmed receipt |
| Updating budget remaining | MANUAL | Financial accuracy |
| Processing settlements | MANUAL | Money movement must be tracked |
| Sending notifications | AUTO | Missing 1 notification is acceptable |
| Logging/analytics | AUTO | Non-critical |
| Cache invalidation | AUTO | Cache will be rebuilt naturally |

---

## 7. Redis Common Cache Pattern

### 7.1 CacheService Interface (in common-cache)

```java
package com.personalfinance.common.cache.service;

import java.time.Duration;
import java.util.*;

public interface CacheService {

    // ── Basic operations ──
    <T> Optional<T> get(String key, Class<T> type);
    <T> void set(String key, T value, Duration ttl);
    void delete(String key);
    boolean exists(String key);

    // ── Numeric operations (for spending counters) ──
    Long increment(String key, long delta);
    Long decrement(String key, long delta);
    Long getCounter(String key);

    // ── Hash operations (for complex objects) ──
    <T> void hSet(String key, String field, T value);
    <T> Optional<T> hGet(String key, String field, Class<T> type);
    Map<String, Object> hGetAll(String key);
    void hDelete(String key, String... fields);

    // ── Set operations (for dedup/tracking) ──
    void sAdd(String key, String... values);
    boolean sIsMember(String key, String value);
    Set<String> sMembers(String key);

    // ── List operations ──
    <T> void lPush(String key, T value);
    <T> List<T> lRange(String key, long start, long end, Class<T> type);

    // ── TTL management ──
    void expire(String key, Duration ttl);
    Duration getTtl(String key);

    // ── Batch operations ──
    <T> Map<String, T> multiGet(Collection<String> keys, Class<T> type);
    void multiDelete(Collection<String> keys);

    // ── Locking (for distributed coordination) ──
    boolean tryLock(String key, Duration ttl);
    void unlock(String key);

    // ── Pattern operations ──
    Set<String> keys(String pattern);
    void deleteByPattern(String pattern);
}
```

### 7.2 Redis Implementation

```java
package com.personalfinance.common.cache.service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) return Optional.empty();
            return Optional.of(objectMapper.convertValue(value, type));
        } catch (Exception e) {
            log.warn("Redis GET failed for key {}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public <T> void set(String key, T value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("Redis SET failed for key {}: {}", key, e.getMessage());
        }
    }

    @Override
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.warn("Redis INCREMENT failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean tryLock(String key, Duration ttl) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent("lock:" + key, "1", ttl);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("Redis LOCK failed for key {}: {}", key, e.getMessage());
            return false;
        }
    }

    // ... implement remaining methods following same pattern:
    // always try-catch, log warnings, never throw from cache operations
    // cache failure should degrade gracefully, not break the app
}
```

### 7.3 Redis Sentinel Config

```java
@Configuration
public class RedisSentinelConfig {

    @Value("${spring.data.redis.sentinel.master}")
    private String master;

    @Value("${spring.data.redis.sentinel.nodes}")
    private String sentinelNodes;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
            .master(master);

        for (String node : sentinelNodes.split(",")) {
            String[] parts = node.trim().split(":");
            sentinelConfig.sentinel(parts[0], Integer.parseInt(parts[1]));
        }

        if (!password.isEmpty()) {
            sentinelConfig.setPassword(RedisPassword.of(password));
        }

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .readFrom(ReadFrom.REPLICA_PREFERRED)  // read from replicas when possible
            .commandTimeout(Duration.ofSeconds(3))
            .build();

        return new LettuceConnectionFactory(sentinelConfig, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

### 7.4 Cache Key Builder (in common-cache)

```java
public final class CacheKeyBuilder {
    private CacheKeyBuilder() {}
    
    // Budget
    public static String activeBudget(UUID userId) {
        return "budget:active:" + userId;
    }
    
    // Spending counters
    public static String dailySpending(UUID userId, UUID categoryId, LocalDate date) {
        return "spending:daily:" + userId + ":" + categoryId + ":" + date;
    }
    public static String monthlySpending(UUID userId, UUID categoryId, String yearMonth) {
        return "spending:monthly:" + userId + ":" + categoryId + ":" + yearMonth;
    }
    
    // Alert dedup
    public static String alertSent(UUID userId, UUID categoryId, String alertType, LocalDate date) {
        return "alert:sent:" + userId + ":" + categoryId + ":" + alertType + ":" + date;
    }
    
    // Notification dedup
    public static String notifSent(UUID userId, String type, String entityId, LocalDate date) {
        return "notif:sent:" + userId + ":" + type + ":" + entityId + ":" + date;
    }
    
    // User cache
    public static String userProfile(UUID userId) {
        return "user:" + userId;
    }
    
    // Group balances
    public static String groupBalance(UUID groupId) {
        return "group:balance:" + groupId;
    }
    
    // Savings progress
    public static String savingProgress(UUID goalId) {
        return "saving:progress:" + goalId;
    }
    
    // Upcoming bills
    public static String upcomingBills(UUID userId) {
        return "bills:upcoming:" + userId;
    }
    
    // Receipt OCR status
    public static String receiptStatus(UUID receiptId) {
        return "receipt:status:" + receiptId;
    }
    
    // Telegram OTP
    public static String telegramOtp(String otpCode) {
        return "telegram:otp:" + otpCode;
    }
}
```

### 7.5 Key Rule
- **Never use `RedisTemplate` directly in business code.** Always go through `CacheService`.
- **Cache failures never break the app.** All `CacheService` methods catch exceptions and degrade gracefully (return empty/null, log warning).
- **Use `CacheKeyBuilder`** for all key construction — no hardcoded key strings in handlers.

---

## 8. Gradle Build Structure

### 8.1 Version Catalog: `gradle/libs.versions.toml`

All versions managed here. Services reference as `libs.xxx`.

### 8.2 Root `build.gradle` (Groovy DSL)

```groovy
plugins {
    id 'java'
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

allprojects {
    group = 'com.personalfinance'
    version = '1.0.0'

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply plugin: 'java'

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType(JavaCompile) {
        options.encoding = 'UTF-8'
        options.compilerArgs.addAll(['-parameters'])
    }

    tasks.withType(Test) {
        useJUnitPlatform()
    }
}
```

### 8.3 Auto-discovery `settings.gradle`

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = 'personal-finance'

FileTree buildFiles = fileTree(rootDir) {
    List excludes = gradle.startParameter.projectProperties.get("excludeProjects")?.split(",")
    include '**/*.gradle'
    exclude 'build', '**/gradle', '**/settings.gradle', 'buildSrc', '/build.gradle', '.*', 'out'
    exclude 'frontend/**'
    exclude 'infrastructure/**'
    exclude 'performance-test/**'
    if (excludes) {
        exclude excludes
    }
}

String rootDirPath = rootDir.absolutePath + File.separator

buildFiles.each { File buildFile ->
    boolean isDefaultName = 'build.gradle'.equals(buildFile.name)
    if (isDefaultName) {
        String buildFilePath = buildFile.parentFile.absolutePath
        String projectPath = buildFilePath.replace(rootDirPath, '').replace(File.separator, ':')
        include projectPath
    } else {
        String projectName = buildFile.name.replace('.gradle', '')
        String projectPath = ':' + projectName
        include projectPath
        def project = findProject(projectPath)
        project.name = projectName
        project.projectDir = buildFile.parentFile
        project.buildFileName = buildFile.name
    }
}
```

### 8.4 Named Gradle File Examples

```groovy
// commons/common-base/common-base.gradle
plugins {
    id 'java-library'
}

dependencies {
    api libs.spring.boot.starter.validation
    compileOnly libs.lombok
    annotationProcessor libs.lombok
}
```

```groovy
// services/auth-service/auth-service.gradle
plugins {
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
}

dependencies {
    implementation project(':common-base')
    implementation project(':common-cache')
    implementation project(':common-event')
    implementation project(':common-security')
    implementation project(':common-web')
    implementation project(':auth-adapter')

    implementation libs.spring.boot.starter.web
    implementation libs.spring.boot.starter.data.jpa
    implementation libs.spring.cloud.starter.netflix.eureka.client
    implementation libs.bundles.flyway
    runtimeOnly libs.postgresql

    compileOnly libs.lombok
    annotationProcessor libs.lombok
    annotationProcessor libs.mapstruct.processor
    implementation libs.mapstruct

    testImplementation libs.spring.boot.starter.test
}
```

```groovy
// services/auth-service/auth-adapter/auth-adapter.gradle
plugins {
    id 'java-library'
    id 'com.google.protobuf'
}

dependencies {
    api project(':common-base')
    api project(':common-grpc')
    api libs.grpc.protobuf
    api libs.grpc.stub
    api libs.protobuf.java
    api libs.grpc.spring.boot.starter

    compileOnly libs.lombok
    annotationProcessor libs.lombok
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    plugins {
        grpc {
            artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}"
        }
    }
    generateProtoTasks {
        all().each { task ->
            task.plugins { grpc {} }
        }
    }
}
```

---

## 9. Docker Infrastructure

### 9.1 Key Ports

| Service | REST Port | gRPC Port |
|---------|-----------|-----------|
| api-gateway | 8080 | — |
| auth-service | 8081 | 28081 |
| budget-service | 8082 | 28082 |
| transaction-service | 8083 | 28083 |
| ocr-service | 8084 | — |
| group-expense-service | 8085 | 28085 |
| notification-service | 8086 | — |
| recurring-bill-service | 8087 | 28087 |
| saving-service | 8088 | — |
| eureka-server | 8761 | — |
| PostgreSQL | 5432 | — |
| Kafka | 9092 | — |
| Redis Master | 6379 | — |
| MinIO | 9000/9001 | — |

### 9.2 Infrastructure Components
- **PostgreSQL**: 1 instance, multiple databases (1 per service)
- **Kafka**: KRaft mode (no Zookeeper), 1 broker for dev
- **Redis Sentinel**: 1 master + 2 replicas + 3 sentinels
- **MinIO**: S3-compatible storage for OCR receipt images
- **Eureka**: 1 instance for service discovery
