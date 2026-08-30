---
name: create-service-module
description: >-
  Use this skill when the user asks to create a new microservice module
  in the ticket-booking project. Guides through creating the Gradle build file,
  Spring Boot application class, configuration, security, and package structure.
---

# Create New Service Module

Follow these steps to create a new microservice module in the GYP Ticket Booking system.

## 1. Create Gradle Build File

Create `{service-name}/{service-name}.gradle` following this template:

```groovy
// =============================================
// {service-name} - {Description}
// =============================================

plugins {
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}

configurations {
    tsGenerator
}

dependencies {
    // Spring Boot Starters
    implementation "org.springframework.boot:spring-boot-starter-actuator:${springBootVersion}"
    implementation "org.springframework.boot:spring-boot-starter-web:${springBootVersion}"
    implementation "org.springframework.boot:spring-boot-starter-data-jpa:${springBootVersion}"
    implementation "org.springframework.boot:spring-boot-starter-validation:${springBootVersion}"

    // Eureka Client
    implementation "org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:${eurekaClientVersion}"

    // Database
    implementation "org.flywaydb:flyway-core:${flywayCoreVersion}"
    implementation "org.flywaydb:flyway-mysql:${flywayMysqlVersion}"
    runtimeOnly "com.mysql:mysql-connector-j:${mysqlConnectorVersion}"

    // MapStruct
    implementation "org.mapstruct:mapstruct:${mapstructVersion}"
    annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
    annotationProcessor "org.projectlombok:lombok-mapstruct-binding:${lombokMapstructBindingVersion}"

    // Local dependency
    implementation project(':common-service')

    // TypeScript Generator
    tsGenerator "cz.habarta.typescript-generator:typescript-generator-core:${typescriptGeneratorVersion}"

    // Test
    testImplementation "org.springframework.boot:spring-boot-starter-test:${springBootVersion}"
    testImplementation 'com.h2database:h2'
}

tasks.register('generateTypeScript', JavaExec) {
    description = 'Generate TypeScript definitions from DTOs'
    group = 'code generation'
    dependsOn classes

    mainClass = 'cz.habarta.typescript.generator.TypeScriptGenerator'
    classpath = configurations.tsGenerator + sourceSets.main.output + sourceSets.main.runtimeClasspath

    args = [
            '--outputFileType', 'implementationFile',
            '--outputKind', 'module',
            '--jsonLibrary', 'jackson2',
            '--classPatterns', 'com.gyp.{servicename}.dtos.**',
            '--excludeClassPatterns', 'java.io.** java.lang.**',
            '--mapClasses', 'asInterfaces',
            '--mapDate', 'asString',
            '--optionalProperties', 'all',
            '--outputFile', "${project.projectDir}/../frontend/gyp-core-ui/src/models/generated/{service-name}-models.d.ts"
    ]
}
```

Add optional dependencies as needed:
- **Redis**: `implementation "org.springframework.boot:spring-boot-starter-data-redis:${springBootVersion}"`
- **Kafka**: `implementation "org.springframework.kafka:spring-kafka:${kafkaVersion}"`

## 2. Register in settings.gradle

The `settings.gradle` auto-discovers `.gradle` files. No manual registration needed.

## 3. Create Package Structure

```
{service-name}/src/main/java/com/gyp/{servicename}/
├── {ServiceName}Application.java
├── configurations/
│   ├── AppConfiguration.java
│   ├── SecurityConfiguration.java
│   └── SwaggerConfiguration.java
├── controllers/
├── services/
│   └── criteria/          # Search criteria classes
├── repositories/
├── entities/
│   └── AbstractEntity.java  # (inherited from common-service)
├── dtos/
│   └── AbstractDto.java     # Service-level DTO base
├── mappers/
│   └── AbstractMapper.java  # (inherited from common-service)
└── messages/               # If using Kafka
    ├── producers/
    └── consumers/
```

## 4. Application Class

```java
package com.gyp.{servicename};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class {ServiceName}Application {
    public static void main(String[] args) {
        SpringApplication.run({ServiceName}Application.class, args);
    }
}
```

## 5. SecurityConfiguration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    private static final String[] PUBLIC_ENDPOINTS = {
            "/v3/api-docs/**", "/v3/api-docs**", "/swagger-ui/**",
            "/swagger-ui.html", "/swagger-resources/**", "/webjars/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).cors(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(request ->
                request.requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated());
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder)));
        return http.build();
    }
}
```

## 6. application.properties

```properties
spring.application.name={service-name}
server.port={port}  # Use next available in 9000-9010 range

# Database
spring.datasource.url=jdbc:mysql://localhost:13306/{service_name}_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Eureka
eureka.client.service-url.defaultZone=http://localhost:9761/eureka/

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

## 7. Add to API Gateway

Add a route in `api-gateway/src/main/resources/application.yml`:

```yaml
- id: {service-name}-route
  uri: lb://{SERVICE-NAME}
  predicates:
    - Path=/{resource-path}/**
```

## Verification

```bash
.\gradlew.bat :{service-name}:build -x test
```
