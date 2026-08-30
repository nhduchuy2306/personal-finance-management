# Design Patterns Reference — Personal Finance Manager

This project uses **20 design patterns**. Each pattern solves a real problem in the system — none are forced.

## Table of Contents
1. [CQRS](#1-cqrs)
2. [Mediator](#2-mediator)
3. [Template Method](#3-template-method)
4. [Strategy](#4-strategy)
5. [Adapter](#5-adapter)
6. [Builder](#6-builder)
7. [Repository](#7-repository)
8. [Observer](#8-observer)
9. [Factory Method](#9-factory-method)
10. [Specification](#10-specification)
11. [State](#11-state)
12. [Chain of Responsibility](#12-chain-of-responsibility)
13. [Decorator](#13-decorator)
14. [Saga](#14-saga)
15. [Circuit Breaker](#15-circuit-breaker)
16. [Prototype](#16-prototype)
17. [Composite](#17-composite)
18. [Proxy](#18-proxy)
19. [Null Object](#19-null-object)
20. [Singleton](#20-singleton)

---

## 1. CQRS

**Problem**: Traditional Service classes grow fat — one class handles both reads and writes with different concerns.
**Where**: Every service. Command handlers (create/update/delete) and Query handlers (read-only) are separate classes.
**Location**: `{service}/features/{feature}/handler/command/` and `handler/query/`

Queries have no side effects (postHandle is no-op). Commands may publish events, invalidate cache in postHandle.

---

## 2. Mediator

**Problem**: Controllers shouldn't know which handler class processes which request.
**Where**: `common-base/handler/HandlerRegistry.java`
**How**: HandlerRegistry injects `List<Handler>`, builds `Map<RequestDTOClass, Handler>`, dispatches by request type.

```java
@Component
public class HandlerRegistry {
    private final Map<Class<?>, Handler<?, ?>> handlers;

    public HandlerRegistry(List<Handler<?, ?>> handlerList) {
        this.handlers = handlerList.stream()
            .collect(Collectors.toMap(Handler::getRequestType, Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <Req, Res> Res dispatch(Req request) {
        Handler<Req, Res> handler = (Handler<Req, Res>) handlers.get(request.getClass());
        if (handler == null) throw new HandlerNotFoundException(request.getClass().getSimpleName());
        return ((AbstractHandler<Req, Res>) handler).execute(request);
    }
}
```

Controllers become thin dispatchers: `return ApiResponse.success(registry.dispatch(request));`

---

## 3. Template Method

**Problem**: Multiple handler types (REST, gRPC, Kafka publisher) share the same execution skeleton but differ in steps.
**Where**: Three separate template hierarchies:

### 3a. AbstractHandler (REST business logic)
```java
// Skeleton: preHandle → doHandle → postHandle
// Subclass overrides the steps, never the skeleton (execute)
public abstract class AbstractHandler<Req, Res> implements Handler<Req, Res> {
    public Res execute(Req request) {
        preHandle(request);          // validation, enrichment
        Res response = doHandle(request);  // core logic, @Transactional
        try { postHandle(request, response); }  // side effects
        catch (Exception e) { log.error("postHandle failed", e); }
        return response;
    }
    @Override public void preHandle(Req request) {}
    @Override public void postHandle(Req request, Res response) {}
}
```

### 3b. AbstractGrpcHandler (gRPC with bidirectional mapping)
```java
// Skeleton: mapFromGrpc → handle → mapToGrpc
// Forces explicit mapping between protobuf and domain models
public abstract class AbstractGrpcHandler<G_REQ, G_RES, D_REQ, D_RES> {
    protected abstract D_REQ mapFromGrpc(G_REQ grpcRequest);
    protected abstract G_RES mapToGrpc(D_RES domainResponse);
    protected abstract D_RES handle(D_REQ domainRequest);

    public final G_RES execute(G_REQ grpcRequest) {
        D_REQ domainRequest = mapFromGrpc(grpcRequest);
        D_RES domainResponse = handle(domainRequest);
        return mapToGrpc(domainResponse);
    }
}
```

### 3c. AbstractEventPublisher (Kafka with source-to-event mapping)
```java
// Skeleton: mapToEvent → enrichEvent → send
// Forces explicit mapping from domain model to lightweight event DTO
public abstract class AbstractEventPublisher<S, E extends BaseEvent> {
    protected abstract String getTopic();
    protected abstract E mapToEvent(S source);
    protected abstract String getPartitionKey(E event);

    protected E enrichEvent(E event) {
        if (event.getEventId() == null) event.setEventId(UUID.randomUUID().toString());
        if (event.getTimestamp() == null) event.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        return event;
    }

    public final void publish(S source) {
        E event = mapToEvent(source);
        event = enrichEvent(event);
        kafkaTemplate.send(getTopic(), getPartitionKey(event), event);
    }

    public final void publishAll(List<S> sources) { sources.forEach(this::publish); }
}
```

---

## 4. Strategy

**Problem**: OCR can use different providers (Google Vision, LLM Vision). Need to switch without changing business logic.
**Where**: `ocr-service/features/processing/`

```java
public interface OcrProvider {
    OcrResult parse(byte[] imageBytes);
    String getProviderName();
}

@Component
@ConditionalOnProperty(name = "ocr.provider", havingValue = "google-vision")
public class GoogleVisionOcrProvider implements OcrProvider {
    @Override
    public OcrResult parse(byte[] imageBytes) { /* call Google Cloud Vision API */ }
}

@Component
@ConditionalOnProperty(name = "ocr.provider", havingValue = "llm-vision")
public class LlmVisionOcrProvider implements OcrProvider {
    @Override
    public OcrResult parse(byte[] imageBytes) { /* call LLM Vision API with structured prompt */ }
}

// Handler doesn't care which provider:
@Component
@RequiredArgsConstructor
public class ProcessOCRHandler {
    private final OcrProvider ocrProvider;  // injected by Spring based on config
}
```

**Extend to**: notification channels (Telegram now, email/push later), split calculation methods.

---

## 5. Adapter

**Problem**: gRPC protobuf models ≠ domain models. Need explicit conversion layer.
**Where**: Every `{service}-adapter/` sub-module.

The `AbstractGrpcHandler` (Pattern #3b) enforces this — `mapFromGrpc` and `mapToGrpc` are abstract methods that MUST be overridden, making the conversion explicit and visible.

```java
@Component
public class GetUserByIdGrpcHandler
    extends AbstractGrpcHandler<GetUserRequest, UserResponse, UUID, UserDto> {

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
            .build();
    }

    @Override
    protected UserDto handle(UUID userId) {
        return userRepository.findById(userId).map(UserDto::from)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
```

---

## 6. Builder

**Problem**: Constructing complex objects (entities, DTOs, events) with many optional fields.
**Where**: Everywhere — Lombok `@Builder` on entities, DTOs, events.

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Transaction extends BaseEntity {
    private UUID userId;
    private UUID categoryId;
    private long amount;
    // ...
}

// Usage:
Transaction tx = Transaction.builder()
    .userId(userId).categoryId(categoryId).amount(50000)
    .type(TransactionType.EXPENSE).source(TransactionSource.MANUAL)
    .transactionDate(LocalDate.now()).build();
```

---

## 7. Repository

**Problem**: Decouple data access from business logic.
**Where**: Every service — Spring Data JPA repositories.

```java
public interface TransactionRepository extends JpaRepository<Transaction, UUID>,
    JpaSpecificationExecutor<Transaction> {  // for Specification pattern
    
    List<Transaction> findByUserIdAndTransactionDate(UUID userId, LocalDate date);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.userId = :userId AND t.categoryId = :categoryId AND t.transactionDate = :date")
    long sumDailySpending(@Param("userId") UUID userId, @Param("categoryId") UUID categoryId, 
                          @Param("date") LocalDate date);
}
```

---

## 8. Observer

**Problem**: Services need to react to events without tight coupling. Transaction-service shouldn't call notification-service directly.
**Where**: Kafka event-driven architecture throughout the system.

- **Producer** (Observable): publishes events to Kafka topics via `AbstractEventPublisher`.
- **Consumer** (Observer): listens to topics and reacts independently.
- **Decoupling**: producer doesn't know who consumes. Multiple consumers can react to same event.

```
transaction.created → [budget-service updates cache, notification-service checks threshold]
settlement.completed → [transaction-service creates expense, notification-service notifies]
```

---

## 9. Factory Method

**Problem**: Need different Kafka consumer configurations (manual vs auto commit) for different use cases.
**Where**: `common-event/config/KafkaConsumerConfig.java`

```java
@Configuration
public class KafkaConsumerConfig {

    @Bean("autoCommitListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> autoCommitListenerFactory() {
        // enable.auto.commit=true — for non-critical consumers (notifications, logging)
    }

    @Bean("manualCommitListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> manualCommitListenerFactory() {
        // enable.auto.commit=false, AckMode.MANUAL_IMMEDIATE
        // for critical consumers (creating transactions, settlements)
    }
}

// Usage — consumer chooses which factory:
@KafkaListener(topics = "receipt.confirmed", containerFactory = "manualCommitListenerFactory")
public void handle(ReceiptConfirmedEvent event, Acknowledgment ack) {
    processAndCreateTransaction(event);
    ack.acknowledge();  // commit only after success
}
```

---

## 10. Specification

**Problem**: Complex query filters with multiple optional criteria — lots of if-else or dynamic query building.
**Where**: `common-base/specification/`, used in transaction-service, group-expense-service, recurring-bill-service.

```java
// common-base
public abstract class BaseSpecification<T> implements org.springframework.data.jpa.domain.Specification<T> {
    
    public BaseSpecification<T> and(BaseSpecification<T> other) {
        return new AndSpecification<>(this, other);
    }
    
    public BaseSpecification<T> or(BaseSpecification<T> other) {
        return new OrSpecification<>(this, other);
    }
    
    public BaseSpecification<T> not() {
        return new NotSpecification<>(this);
    }
}

// transaction-service/features/expense/specification/
public class TransactionSpecifications {

    public static BaseSpecification<Transaction> byUserId(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static BaseSpecification<Transaction> byCategory(UUID categoryId) {
        return (root, query, cb) -> cb.equal(root.get("categoryId"), categoryId);
    }

    public static BaseSpecification<Transaction> byDateRange(LocalDate from, LocalDate to) {
        return (root, query, cb) -> cb.between(root.get("transactionDate"), from, to);
    }

    public static BaseSpecification<Transaction> byType(TransactionType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static BaseSpecification<Transaction> byAmountGreaterThan(long amount) {
        return (root, query, cb) -> cb.greaterThan(root.get("amount"), amount);
    }
}

// Usage in ListExpensesHandler:
BaseSpecification<Transaction> spec = byUserId(userId)
    .and(byDateRange(request.getFrom(), request.getTo()))
    .and(byType(EXPENSE));

if (request.getCategoryId() != null) {
    spec = spec.and(byCategory(request.getCategoryId()));
}

Page<Transaction> results = repository.findAll(spec, pageable);
```

---

## 11. State

**Problem**: Entities have status transitions with rules about which transitions are valid. Scattered if-else checking status is error-prone.
**Where**: ocr-service (receipt lifecycle), budget-service (period lifecycle), saving-service (goal lifecycle).

```java
// common-base/state/
public interface EntityState<E> {
    String getStatus();
    default EntityState<E> onProcess(E entity)  { throw invalidTransition("process"); }
    default EntityState<E> onConfirm(E entity)  { throw invalidTransition("confirm"); }
    default EntityState<E> onDiscard(E entity)  { throw invalidTransition("discard"); }
    default EntityState<E> onFail(E entity)     { throw invalidTransition("fail"); }
    default EntityState<E> onComplete(E entity) { throw invalidTransition("complete"); }
    default EntityState<E> onCancel(E entity)   { throw invalidTransition("cancel"); }
    default EntityState<E> onActivate(E entity) { throw invalidTransition("activate"); }

    private BusinessException invalidTransition(String action) {
        return new BusinessException(ErrorCode.INVALID_STATE_TRANSITION,
            "Cannot " + action + " when status is " + getStatus());
    }
}

// ocr-service/features/processing/state/
public class ProcessingState implements EntityState<Receipt> {
    @Override public String getStatus() { return "PROCESSING"; }

    @Override
    public EntityState<Receipt> onProcess(Receipt receipt) {
        // call OCR provider, parse result
        receipt.setParsedData(parsedResult);
        return new ParsedState();
    }

    @Override
    public EntityState<Receipt> onFail(Receipt receipt) {
        receipt.setErrorMessage("OCR processing failed");
        return new FailedState();
    }
}

public class ParsedState implements EntityState<Receipt> {
    @Override public String getStatus() { return "PARSED"; }

    @Override
    public EntityState<Receipt> onConfirm(Receipt receipt) {
        receipt.setConfirmedData(receipt.getParsedData());
        return new ConfirmedState();
    }

    @Override
    public EntityState<Receipt> onDiscard(Receipt receipt) {
        return new DiscardedState();
    }
}

// Usage in handler:
EntityState<Receipt> currentState = StateFactory.fromStatus(receipt.getStatus());
EntityState<Receipt> newState = currentState.onConfirm(receipt);
receipt.setStatus(newState.getStatus());
```

**State machines in this project:**
- Receipt: PROCESSING → PARSED → CONFIRMED/DISCARDED, PROCESSING → FAILED
- BudgetPeriod: DRAFT → ACTIVE → COMPLETED
- SavingsGoal: ACTIVE → COMPLETED/CANCELLED

---

## 12. Chain of Responsibility

**Problem**: Validation logic in preHandle becomes long and tangled. Hard to reuse validators across handlers.
**Where**: `common-base/validation/`, used in handlers with complex validation.

```java
// common-base/validation/
public interface ValidationHandler<T> {
    void validate(T request);
    ValidationHandler<T> setNext(ValidationHandler<T> next);
}

public abstract class AbstractValidationHandler<T> implements ValidationHandler<T> {
    private ValidationHandler<T> next;

    @Override
    public ValidationHandler<T> setNext(ValidationHandler<T> next) {
        this.next = next;
        return next;  // enable chaining: a.setNext(b).setNext(c)
    }

    protected void validateNext(T request) {
        if (next != null) next.validate(request);
    }
}

// transaction-service validators:
public class AmountPositiveValidator extends AbstractValidationHandler<CreateExpenseRequest> {
    @Override
    public void validate(CreateExpenseRequest req) {
        if (req.getAmount() <= 0) throw new ValidationException("Amount must be positive");
        validateNext(req);
    }
}

public class CategoryExistsValidator extends AbstractValidationHandler<CreateExpenseRequest> {
    private final BudgetGrpcClient budgetClient;
    @Override
    public void validate(CreateExpenseRequest req) {
        if (!budgetClient.categoryExists(req.getCategoryId()))
            throw new ValidationException("Category not found");
        validateNext(req);
    }
}

public class DateNotFutureValidator extends AbstractValidationHandler<CreateExpenseRequest> {
    @Override
    public void validate(CreateExpenseRequest req) {
        if (req.getDate().isAfter(LocalDate.now()))
            throw new ValidationException("Transaction date cannot be in the future");
        validateNext(req);
    }
}

// Usage — build chain helper in common-base:
public class ValidationChain {
    @SafeVarargs
    public static <T> void validate(T request, AbstractValidationHandler<T>... validators) {
        if (validators.length == 0) return;
        for (int i = 0; i < validators.length - 1; i++) {
            validators[i].setNext(validators[i + 1]);
        }
        validators[0].validate(request);
    }
}

// In handler preHandle:
@Override
public void preHandle(CreateExpenseRequest request) {
    ValidationChain.validate(request,
        new AmountPositiveValidator(),
        new CategoryExistsValidator(budgetClient),
        new DateNotFutureValidator()
    );
}
```

---

## 13. Decorator

**Problem**: Want to add cross-cutting behavior (logging, caching, metrics, retry) to handlers without modifying them.
**Where**: `common-base/handler/decorator/`

```java
public abstract class HandlerDecorator<Req, Res> extends AbstractHandler<Req, Res> {
    protected final Handler<Req, Res> delegate;

    protected HandlerDecorator(Handler<Req, Res> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Class<Req> getRequestType() { return delegate.getRequestType(); }
}

// Logging
@Slf4j
public class LoggingHandlerDecorator<Req, Res> extends HandlerDecorator<Req, Res> {
    public LoggingHandlerDecorator(Handler<Req, Res> delegate) { super(delegate); }

    @Override
    public Res doHandle(Req request) {
        log.info("[HANDLER] {} started", delegate.getRequestType().getSimpleName());
        long start = System.currentTimeMillis();
        Res response = delegate.doHandle(request);
        log.info("[HANDLER] {} completed in {}ms",
            delegate.getRequestType().getSimpleName(), System.currentTimeMillis() - start);
        return response;
    }
}

// Caching — wraps query handlers
public class CachingHandlerDecorator<Req, Res> extends HandlerDecorator<Req, Res> {
    private final CacheService cacheService;
    private final Function<Req, String> keyExtractor;
    private final Class<Res> responseType;
    private final Duration ttl;

    @Override
    public Res doHandle(Req request) {
        String key = keyExtractor.apply(request);
        Optional<Res> cached = cacheService.get(key, responseType);
        if (cached.isPresent()) return cached.get();
        Res response = delegate.doHandle(request);
        cacheService.set(key, response, ttl);
        return response;
    }
}

// Retry — wraps handlers calling external services
public class RetryHandlerDecorator<Req, Res> extends HandlerDecorator<Req, Res> {
    private final int maxRetries;
    private final Duration delay;

    @Override
    public Res doHandle(Req request) {
        int attempt = 0;
        while (true) {
            try {
                return delegate.doHandle(request);
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxRetries) throw e;
                log.warn("Retry {}/{} for {}", attempt, maxRetries,
                    delegate.getRequestType().getSimpleName());
                try { Thread.sleep(delay.toMillis()); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
    }
}
```

---

## 14. Saga

**Problem**: Cross-service operations (OCR confirm → create transaction → check budget) need compensation if a step fails.
**Where**: `common-base/saga/`, used in settlement flow, OCR confirm flow.

```java
// common-base/saga/
public interface SagaStep<T> {
    String getStepName();
    void execute(T context);
    void compensate(T context);
}

@Slf4j
public class SagaOrchestrator<T> {
    private final List<SagaStep<T>> steps;

    public SagaOrchestrator(List<SagaStep<T>> steps) {
        this.steps = steps;
    }

    public void execute(T context) {
        List<SagaStep<T>> executed = new ArrayList<>();
        try {
            for (SagaStep<T> step : steps) {
                log.info("Saga step executing: {}", step.getStepName());
                step.execute(context);
                executed.add(step);
            }
        } catch (Exception e) {
            log.error("Saga failed at step, compensating...", e);
            Collections.reverse(executed);
            for (SagaStep<T> step : executed) {
                try {
                    log.info("Compensating: {}", step.getStepName());
                    step.compensate(context);
                } catch (Exception ce) {
                    log.error("Compensation failed for {}", step.getStepName(), ce);
                }
            }
            throw new BusinessException(ErrorCode.SAGA_FAILED, e.getMessage());
        }
    }
}

// Settlement saga example:
// Step 1: CreateSettlementRecord — compensate: delete record
// Step 2: CreateExpenseForDebtor — compensate: delete transaction
// Step 3: UpdateGroupBalanceCache — compensate: recalculate from DB
```

---

## 15. Circuit Breaker

**Problem**: gRPC calls and external API calls can fail. Repeated failures should short-circuit to prevent cascading failure.
**Where**: `common-base/resilience/`, used in all gRPC clients, OCR API, Telegram API.

```java
// common-base/resilience/
public class CircuitBreaker {
    private enum State { CLOSED, OPEN, HALF_OPEN }

    private final String name;
    private final int failureThreshold;
    private final Duration resetTimeout;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private volatile State state = State.CLOSED;
    private volatile Instant lastFailureTime;

    public <T> T execute(Supplier<T> action, Supplier<T> fallback) {
        if (state == State.OPEN) {
            if (Duration.between(lastFailureTime, Instant.now()).compareTo(resetTimeout) > 0) {
                state = State.HALF_OPEN;
            } else {
                log.warn("Circuit breaker {} is OPEN, using fallback", name);
                return fallback.get();
            }
        }
        try {
            T result = action.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            log.warn("Circuit breaker {} caught failure ({}/{}), using fallback",
                name, failureCount.get(), failureThreshold);
            return fallback.get();
        }
    }

    private void onSuccess() {
        failureCount.set(0);
        state = State.CLOSED;
    }

    private void onFailure() {
        lastFailureTime = Instant.now();
        if (failureCount.incrementAndGet() >= failureThreshold) {
            state = State.OPEN;
        }
    }
}

// Usage in gRPC client:
@Component
public class BudgetGrpcClient {
    private final CircuitBreaker breaker = new CircuitBreaker("budget-grpc", 5, Duration.ofSeconds(30));

    public AllocationDto getAllocation(UUID categoryId) {
        return breaker.execute(
            () -> mapToDomain(stub.getCategoryAllocation(buildRequest(categoryId))),
            () -> getFromCache(categoryId)  // fallback: serve stale cache
        );
    }
}
```

---

## 16. Prototype

**Problem**: Auto-fill budget draft needs to deep-clone the previous period's structure.
**Where**: budget-service (auto-fill), also applicable to recurring bills template cloning.

```java
// common-base/model/
public interface DeepCloneable<T> {
    T deepClone();
}

// budget-service
public class BudgetPeriod extends BaseEntity implements DeepCloneable<BudgetPeriod> {
    private long totalAmount;
    private BudgetStatus status;
    private List<MonthlyBudget> monthlyBudgets;

    @Override
    public BudgetPeriod deepClone() {
        BudgetPeriod clone = new BudgetPeriod();
        clone.setTotalAmount(this.totalAmount);
        clone.setStatus(BudgetStatus.DRAFT);  // always DRAFT for new clone
        clone.setMonthlyBudgets(
            this.monthlyBudgets.stream()
                .map(MonthlyBudget::deepClone)
                .collect(Collectors.toList())
        );
        return clone;
    }
}

public class MonthlyBudget extends BaseEntity implements DeepCloneable<MonthlyBudget> {
    @Override
    public MonthlyBudget deepClone() {
        MonthlyBudget clone = new MonthlyBudget();
        clone.setAllocatedAmount(this.allocatedAmount);
        clone.setCategoryAllocations(
            this.categoryAllocations.stream()
                .map(CategoryAllocation::deepClone)
                .collect(Collectors.toList())
        );
        return clone;
    }
}
```

---

## 17. Composite

**Problem**: Group expenses compose multiple items into a hierarchical structure — need uniform handling of single items and groups.
**Where**: group-expense-service (expense item aggregation), budget-service (allocation aggregation).

```java
// group-expense-service
public interface ExpenseComponent {
    long getAmount();
    String getDescription();
    List<ExpenseComponent> getChildren();
}

public class SingleExpenseItem implements ExpenseComponent {
    private final String name;
    private final long price;
    @Override public long getAmount() { return price; }
    @Override public String getDescription() { return name; }
    @Override public List<ExpenseComponent> getChildren() { return Collections.emptyList(); }
}

public class GroupedExpense implements ExpenseComponent {
    private final String groupName;
    private final List<ExpenseComponent> items = new ArrayList<>();

    public void addItem(ExpenseComponent item) { items.add(item); }

    @Override
    public long getAmount() {
        return items.stream().mapToLong(ExpenseComponent::getAmount).sum();
    }

    @Override
    public String getDescription() { return groupName; }

    @Override
    public List<ExpenseComponent> getChildren() { return Collections.unmodifiableList(items); }
}

// OCR parsed items → compose into grouped expense:
GroupedExpense receipt = new GroupedExpense("Hóa đơn BigC");
receipt.addItem(new SingleExpenseItem("Thịt bò", 85000));
receipt.addItem(new SingleExpenseItem("Rau muống", 15000));
receipt.addItem(new SingleExpenseItem("Nước mắm", 25000));
long total = receipt.getAmount();  // 125000 — calculated recursively
```

---

## 18. Proxy

**Problem**: Cache-first access pattern — check Redis before hitting DB. Don't want cache logic polluting handlers.
**Where**: `common-cache/proxy/`, used for frequently accessed data (budget, user profile, group balance).

```java
// common-cache/proxy/
public abstract class CachingProxy<ID, T> {
    private final CacheService cacheService;
    private final Duration ttl;

    protected abstract String buildCacheKey(ID id);
    protected abstract Optional<T> loadFromSource(ID id);
    protected abstract Class<T> getType();

    public Optional<T> get(ID id) {
        String key = buildCacheKey(id);

        // 1. Try cache first
        Optional<T> cached = cacheService.get(key, getType());
        if (cached.isPresent()) return cached;

        // 2. Cache miss — load from source (DB)
        Optional<T> fromSource = loadFromSource(id);
        fromSource.ifPresent(value -> cacheService.set(key, value, ttl));
        return fromSource;
    }

    public void evict(ID id) {
        cacheService.delete(buildCacheKey(id));
    }
}

// budget-service
@Component
public class ActiveBudgetProxy extends CachingProxy<UUID, BudgetPeriodDto> {
    private final BudgetPeriodRepository repository;

    @Override protected String buildCacheKey(UUID userId) { return CacheKeyBuilder.activeBudget(userId); }
    @Override protected Optional<BudgetPeriodDto> loadFromSource(UUID userId) {
        return repository.findByUserIdAndStatus(userId, BudgetStatus.ACTIVE).map(BudgetPeriodDto::from);
    }
    @Override protected Class<BudgetPeriodDto> getType() { return BudgetPeriodDto.class; }
}

// Handler uses proxy instead of repository:
BudgetPeriodDto budget = activeBudgetProxy.get(userId)
    .orElseThrow(() -> new BusinessException(ErrorCode.NO_ACTIVE_BUDGET));
```

---

## 19. Null Object

**Problem**: Checking `if (telegramChatId != null)` everywhere is error-prone and clutters code.
**Where**: notification-service (Telegram channel), also applicable to optional features.

```java
// common-notification
public interface NotificationChannel {
    void send(String message);
    boolean isAvailable();
}

public class TelegramChannel implements NotificationChannel {
    private final String chatId;
    private final TelegramNotificationSender sender;

    @Override public void send(String message) { sender.send(chatId, message); }
    @Override public boolean isAvailable() { return true; }
}

public class NullNotificationChannel implements NotificationChannel {
    private static final NullNotificationChannel INSTANCE = new NullNotificationChannel();
    public static NullNotificationChannel getInstance() { return INSTANCE; }

    @Override public void send(String message) { /* no-op — logged at higher level */ }
    @Override public boolean isAvailable() { return false; }
}

// Factory:
@Component
@RequiredArgsConstructor
public class NotificationChannelFactory {
    private final AuthGrpcClient authClient;
    private final TelegramNotificationSender sender;

    public NotificationChannel getChannel(UUID userId) {
        String chatId = authClient.getTelegramChatId(userId);
        return chatId != null
            ? new TelegramChannel(chatId, sender)
            : NullNotificationChannel.getInstance();
    }
}

// Usage — no null checks needed:
NotificationChannel channel = channelFactory.getChannel(userId);
channel.send(message);  // works whether Telegram is linked or not
if (!channel.isAvailable()) {
    logAsSkipped(notification);
}
```

---

## 20. Singleton

**Problem**: Ensure single instance of shared resources (Spring beans, circuit breakers, channel instances).
**Where**: All `@Component`, `@Service`, `@Repository` are Spring-managed singletons by default.

Also explicit in:
- `NullNotificationChannel.getInstance()` (eager singleton via static field)
- `CircuitBreaker` instances (one per external service, shared across threads)
- `HandlerRegistry` (single registry per service, holds the handler map)

---

## Pattern Location Map

```
common-base/
├── handler/
│   ├── Handler.java                    # [CQRS] interface
│   ├── AbstractHandler.java            # [Template Method] REST handlers
│   ├── HandlerRegistry.java            # [Mediator] dispatch
│   └── decorator/
│       ├── HandlerDecorator.java        # [Decorator] base
│       ├── LoggingHandlerDecorator.java
│       ├── CachingHandlerDecorator.java
│       └── RetryHandlerDecorator.java
├── validation/
│   ├── ValidationHandler.java           # [Chain of Responsibility] interface
│   ├── AbstractValidationHandler.java
│   └── ValidationChain.java            # helper to build chains
├── specification/
│   ├── BaseSpecification.java           # [Specification] composable queries
│   ├── AndSpecification.java
│   ├── OrSpecification.java
│   └── NotSpecification.java
├── state/
│   └── EntityState.java                 # [State] interface with default invalid transitions
├── saga/
│   ├── SagaStep.java                    # [Saga] step interface
│   └── SagaOrchestrator.java
├── resilience/
│   └── CircuitBreaker.java              # [Circuit Breaker]
├── model/
│   ├── BaseEntity.java                  # [Builder] via Lombok
│   └── DeepCloneable.java              # [Prototype] interface

common-cache/
├── proxy/
│   └── CachingProxy.java               # [Proxy] cache-first access

common-event/
├── publisher/
│   └── AbstractEventPublisher.java      # [Template Method] + [Observer] Kafka events

common-grpc/
├── handler/
│   └── AbstractGrpcHandler.java         # [Template Method] + [Adapter] gRPC mapping

common-notification/
├── service/
│   ├── NotificationChannel.java         # [Strategy] + [Null Object]
│   ├── TelegramChannel.java
│   └── NullNotificationChannel.java     # [Null Object] + [Singleton]
```