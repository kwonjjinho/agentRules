---
description: Global Microservices Architecture (MSA) standards, Domain-Driven Design (DDD), Resilience, and Service Discovery (Eureka).
globs: **/*
alwaysApply: true
---

# Global Microservices Architecture (MSA) Standards

## 1. Core Architectural Principles (Silicon Valley Enterprise Baseline)

- **Domain-Driven Design (DDD)**:
  - Each microservice owns its private database (Database-per-Service). Direct cross-service database access is strictly prohibited.
  - Model bounded contexts cleanly: separate Aggregates, Entities, and Value Objects.
  - Inter-service synchronous communication must strictly occur via API Gateway / OpenFeign with circuit breaking (Resilience4j).
  - Inter-service asynchronous communication must occur via Apache Kafka events.

- **12-Factor App & Cloud Native**:
  - Stateless execution: Store zero session/state data on service instances; offload shared state to Redis or persistent DB.
  - Configuration externalization: Inject environment-specific configs via Spring Cloud Config, K8s ConfigMaps, and Secrets. Never hardcode credentials, IP addresses, or endpoints.
  - Concurrency & Horizontal Scaling: Services must be design-safe for rapid scale-up and scale-down.

- **Service Registry & Discovery (Netflix Eureka)**:
  - Microservices register as Eureka Clients (`eureka.client.register-with-eureka=true`).
  - Spring Cloud Gateway routes requests based on Eureka virtual service names (`lb://SERVICE-NAME`).
  - Configure resilient client heartbeats (`lease-renewal-interval-in-seconds: 10`, `lease-expiration-duration-in-seconds: 30` in production).

## 2. Distributed Transactions & Consistency

- **Eventual Consistency over 2PC**:
  - Prefer Saga Pattern (Orchestration or Choreography) for cross-service transactions.
  - Never attempt distributed two-phase commits (XA).
- **Transactional Outbox Pattern**:
  - When publishing domain events triggered by DB mutations, persist the event into an `outbox` table within the same local ACID transaction.
  - Use Debezium CDC or an asynchronous background poller to relay events to Kafka to eliminate dual-write hazards.

## 3. Resilience, Fault Tolerance & Chaos Readiness

- **Resilience4j Integration**:
  - Every external sync call (Feign / WebClient) must be protected by:
    - **Timeout**: Strict p99 timeout limits (e.g., connect: 1s, read: 2s).
    - **Circuit Breaker**: Open circuit when error rate > 50% over sliding window.
    - **Fallback**: Graceful fallback or deterministic error response.
- **Bulkhead Pattern**: Separate thread pools/semaphores for distinct downstream calls.
- **Idempotency**: All mutating operations (HTTP POST/PUT/PATCH and Kafka Consumers) must handle duplicate requests safely using idempotency keys or unique event IDs.

## 4. Observability & Distributed Tracing (OpenTelemetry)

- **Correlation & Trace ID Propagation**:
  - All ingress requests must generate or capture `X-Correlation-Id` / `traceparent` (W3C standard) at the API Gateway.
  - Propagate trace IDs through Feign headers and Kafka Record Headers.
- **Structured JSON Logging**:
  - Use Logback / Log4j2 with JSON layout in production:
    ```json
    {
      "timestamp": "2026-11-09T10:45:00.000Z",
      "level": "INFO",
      "service": "order-service",
      "traceId": "4bf92f3577b34da6a3ce929d0e0e4736",
      "spanId": "00f067aa0ba902b7",
      "message": "Order processed successfully",
      "orderId": "ORD-98214"
    }
    ```
- **Health Checks & Metrics**:
  - Expose `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness`, and `/actuator/prometheus`.
