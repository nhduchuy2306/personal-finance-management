# GYP Ticket Booking - Agent Rules

## Code Style & Conventions

### Java
- **Java 21** with Spring Boot 3.4.5
- Use **tab indentation** (not spaces)
- Package convention: `com.gyp.{servicename}` (e.g., `com.gyp.eventservice`, `com.gyp.authservice`)
- Service naming: kebab-case for module dirs (e.g., `event-service`), no separator for package (e.g., `eventservice`)

### Lombok Annotations
- Entity/DTO standard stack: `@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor`
- **CRITICAL**: Any field with an initializer (e.g., `new ArrayList<>()`, `"local"`, enum constant) MUST use `@Builder.Default`
- Use `@RequiredArgsConstructor` on `@Service` and `@RestController` classes for constructor injection
- Use `@Slf4j` for logging

### MapStruct Conventions
- All mappers MUST include `unmappedTargetPolicy = ReportingPolicy.IGNORE` in `@Mapper` annotation
- All mappers that map to entities MUST extend `AbstractMapper` (from common-service) for audit field handling
- Use `@AfterMapping` with `mapAbstractFieldsToEntity()` for entity targets and `mapAbstractFields()` for DTO targets
- **NEVER** use explicit `@Mapping(target = "createUser/changeUser/createTimestamp/changeTimestamp", ignore = true)` — these fields are on `AbstractEntity` (superclass) and not accessible via Lombok's `@Builder` since it doesn't expose parent fields
- Use `qualifiedByName` for complex field mappings with `@Named` helper methods
- Include `mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG` when applicable

### Entity Rules
- All entities extend `AbstractEntity` (from common-service) which provides audit fields
- Use `@Builder` (NOT `@SuperBuilder`) — audit fields are handled via `@AfterMapping` in mappers
- Primary key: `@Id @GeneratedValue(strategy = GenerationType.UUID)` with `String id`
- Include `@Serial private static final long serialVersionUID` 
- Collection fields initialized with `new ArrayList<>()` must have `@Builder.Default`

### Controller Pattern
- Extend `AbstractController` (from common-service)
- Define resource path as `public static final String` constant
- Use `@PreAuthorize("@permissionEvaluator.hasPermission(authentication, #AppPerm.XXX, #ActionPerm.YYY)")` for authorization
- Use `getCurrentOrganizationId()` from AbstractController for multi-tenant queries
- Return `ResponseEntity<?>` from all endpoints

### Service Pattern
- `@Service @RequiredArgsConstructor`
- Inject repositories, mappers, and other services via `private final` fields
- Business logic goes in service layer, NOT in controllers

### Repository Pattern
- Extend `JpaRepository<EntityClass, String>`
- Use `@Specification` for complex filtering/searching
- Custom queries with `@Query` annotation when needed

## Build System (Gradle)
- Root: `build.gradle` + `settings.gradle` + `gradle.properties`
- Each service: `{service-name}/{service-name}.gradle` (e.g., `event-service/event-service.gradle`)
- All version numbers centralized in `gradle.properties`
- Build command: `.\gradlew.bat clean build -x test`
- All services depend on `:common-service` via `implementation project(':common-service')`

## Architecture
- Microservices communicate via:
  - **Kafka** for async events (TopicConstants in common-service)
  - **Eureka** for service discovery (port 9761)
  - **Feign/WebClient** for sync REST calls
- API Gateway on port 9999 routes all external requests
- Each service has its own MySQL schema
- Flyway for database migrations
- Redis for caching and seat holds

## Security
- JWT with HS512 symmetric key
- Permission model: `{appId: [actions]}` embedded in JWT claims
- `CustomPermissionEvaluator` validates permissions via `@PreAuthorize`

## Common Pitfalls
1. Don't use `@Mapping(target="auditField", ignore=true)` for AbstractEntity fields — they break with @Builder
2. Always use `@Builder.Default` on fields with initializers when class has `@Builder`
3. Kafka consumers have no DLQ — handle errors carefully
4. `RestTemplate.exchange()` is deprecated in Spring 6.x — prefer `RestClient` for new code
