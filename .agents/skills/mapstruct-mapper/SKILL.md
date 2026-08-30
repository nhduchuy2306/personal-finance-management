---
name: mapstruct-mapper
description: >-
  Use this skill when the user asks to create or fix a MapStruct mapper, 
  resolve MapStruct compilation warnings/errors, or understand the mapper patterns
  used in this project. Critical for avoiding the AbstractEntity builder trap.
---

# MapStruct Mapper Patterns

## The AbstractEntity Builder Trap (CRITICAL)

All entities extend `AbstractEntity` which has audit fields:
- `createUser`, `changeUser`, `createTimestamp`, `changeTimestamp`

Entities use Lombok `@Builder` (NOT `@SuperBuilder`). This means:
- The generated builder class only includes fields declared in the entity itself
- **Parent class fields (`AbstractEntity` fields) are NOT in the builder**
- MapStruct cannot find these fields when using the builder strategy

### ❌ WRONG — Causes compilation error
```java
@Mapping(target = "createUser", ignore = true)     // ERROR: Unknown property
@Mapping(target = "changeUser", ignore = true)      // ERROR: Unknown property
@Mapping(target = "createTimestamp", ignore = true)  // ERROR: Unknown property
@Mapping(target = "changeTimestamp", ignore = true)  // ERROR: Unknown property
ResourceEntity toEntity(ResourceRequestDto dto);
```

### ✅ CORRECT — Use unmappedTargetPolicy
```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface ResourceMapper extends AbstractMapper {
    // No need to mention audit fields — they're handled by @AfterMapping
}
```

## Standard Mapper Template

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface {Resource}Mapper extends AbstractMapper {

    // Entity → Response DTO
    {Resource}ResponseDto toResponseDto({Resource}Entity entity);
    List<{Resource}ResponseDto> toResponseDtoList(List<{Resource}Entity> entities);

    // Request DTO → Entity (create)
    @Mapping(target = "id", ignore = true)
    {Resource}Entity toEntity({Resource}RequestDto dto);

    // Request DTO → Entity (update)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto({Resource}RequestDto dto, @MappingTarget {Resource}Entity entity);

    // Audit field handling via AbstractMapper
    @AfterMapping
    default void afterMapping(@MappingTarget {Resource}Entity entity) {
        mapAbstractFieldsToEntity(entity);  // Sets createUser, changeUser, timestamps
    }

    @AfterMapping
    default void afterMapping(@MappingTarget {Resource}ResponseDto dto, {Resource}Entity entity) {
        mapAbstractFields(entity, dto);  // Copies audit fields to DTO
    }
}
```

## Relationship Mapping

### Foreign Key → Entity (using @Named)
```java
@Mapping(target = "parentEntity", source = "parentId", qualifiedByName = "parentIdToEntity")
{Resource}Entity toEntity({Resource}RequestDto dto);

@Named("parentIdToEntity")
default ParentEntity parentIdToEntity(String id) {
    if (id == null) return null;
    return ParentEntity.builder().id(id).build();
}
```

### Entity → ID (in response DTO)
```java
@Mapping(target = "parentId", source = "parentEntity.id")
{Resource}ResponseDto toResponseDto({Resource}Entity entity);
```

### Collection of IDs → Collection of Entities
```java
@Named("idsToEntities")
default List<ChildEntity> idsToEntities(List<String> ids) {
    if (ids == null) return new ArrayList<>();
    return ids.stream()
            .map(id -> ChildEntity.builder().id(id).build())
            .collect(Collectors.toList());
}
```

## Calculated Fields in Response DTO
```java
@Mapping(target = "isActive", expression = "java(isActive(entity))")
{Resource}ResponseDto toResponseDto({Resource}Entity entity);

default boolean isActive({Resource}Entity entity) {
    // business logic
}
```

## Required Imports
```java
import org.mapstruct.*;
// ReportingPolicy for unmappedTargetPolicy
import org.mapstruct.ReportingPolicy;
```

## Checklist for New Mapper
- [ ] `unmappedTargetPolicy = ReportingPolicy.IGNORE` in `@Mapper`
- [ ] Extends `AbstractMapper`
- [ ] `@AfterMapping` for entity target (`mapAbstractFieldsToEntity`)
- [ ] `@AfterMapping` for DTO target (`mapAbstractFields`)
- [ ] `@Mapping(target = "id", ignore = true)` on `toEntity` and `updateEntityFromDto`
- [ ] NO explicit ignore for audit fields (createUser, changeUser, etc.)
- [ ] Collection relationship fields ignored: `@Mapping(target = "childList", ignore = true)`
