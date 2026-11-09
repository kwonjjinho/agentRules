---
description: Java 21 & Spring Boot 3.x coding standards, Hexagonal/Clean Architecture, DTOs, Validation, and JPA persistence rules.
globs: **/*.java, **/pom.xml, **/build.gradle*, **/src/main/**, **/src/test/**
alwaysApply: false
---

# Spring Boot 3.x & Modern Java Engineering Rules

## 1. Java 21+ Language Standards

- **Records**: Use `record` for all immutable DTOs, Value Objects, and Event Payloads:
  ```java
  public record CreateOrderRequest(
      @NotBlank(message = "Customer ID is required") String customerId,
      @NotEmpty(message = "Order must have at least one item") List<OrderItemDto> items,
      @Positive(message = "Total amount must be greater than zero") BigDecimal totalAmount
  ) {}
  ```
- **Pattern Matching & Switch Expressions**: Utilize enhanced switch and pattern matching over deep `if-else` or `instanceof` casts.
- **Virtual Threads (Project Loom)**: Enable `spring.threads.virtual.enabled=true` for high-throughput I/O-bound microservices. Ensure no `synchronized` locks pin carrier threads (prefer `ReentrantLock`).
- **Null Safety**: Avoid `null` returns. Use `Optional<T>` strictly for query return types, never for method arguments, fields, or collections.

## 2. Layered & Hexagonal Architecture

Enforce strict boundary separation:
```
com.company.service
├── adapter
│   ├── in.web          // REST Controllers, Request/Response DTOs
│   ├── in.kafka        // Kafka Listeners / Consumers
│   ├── out.persistence // JPA Entities, Spring Data Repositories, DB mappers
│   └── out.client      // OpenFeign clients, Kafka producers
├── application
│   ├── port.in         // Use Case interfaces
│   ├── port.out        // Persistence / External client ports
│   └── service         // Business logic orchestrators
└── domain
    ├── model           // Pure Domain entities, Enums, Value Objects (Zero framework dependencies)
    └── exception       // Domain-specific business exceptions
```

- **Rules**:
  - Domain models must never import Spring, Hibernate, or Jackson packages.
  - Entities (JPA) must NOT be exposed to Web/API layer. Always map to API DTOs using MapStruct or explicit mapper records.
  - Controllers must only inject Application Use Cases / Services.

## 3. REST Controller & API Conventions

- **RESTful Endpoints**:
  - Use plural nouns for resources: `/api/v1/orders`, `/api/v1/users/{userId}/addresses`.
  - Avoid verbs in URLs (`/api/v1/create-order` ❌ -> `POST /api/v1/orders` ✅).
- **Global Error Handling**:
  - Implement a central `@RestControllerAdvice` conforming to RFC 7807 (Problem Details for HTTP APIs) or unified ApiResponse envelope:
  ```java
  @RestControllerAdvice
  public class GlobalExceptionHandler {
      @ExceptionHandler(BusinessException.class)
      public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException ex) {
          ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
          problem.setProperty("errorCode", ex.getErrorCode());
          problem.setProperty("timestamp", Instant.now());
          return ResponseEntity.status(ex.getStatus()).body(problem);
      }
  }
  ```

## 4. JPA & Database Access Best Practices

- **Avoid N+1 Query Traps**:
  - Always analyze fetch plans: Use `JOIN FETCH` or `@EntityGraph` for `@ManyToOne` / `@OneToMany` relationships.
  - Default all relationships to `FetchType.LAZY`. Never use `FetchType.EAGER`.
- **Soft Deletes & Auditing**:
  - Use `@EntityListeners(AuditingEntityListener.class)` with `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`.
- **Batch Processing**:
  - Enable Hibernate batching: `spring.jpa.properties.hibernate.jdbc.batch_size=50`.
- **Transaction Boundaries**:
  - Mark read-only services/methods with `@Transactional(readOnly = true)` to disable Hibernate dirty checking and route to DB read replicas.
