---
name: msa-workflow
description: >-
  Use this skill when developing, refactoring, or reviewing code for the enterprise MSA fullstack platform
  (Next.js, Spring Boot, Spring Security, Eureka, Kafka, Redis, Swagger, Docker, Kubernetes, GitHub Actions).
  Provides end-to-end guidance for service communication, event schema creation, and deployment validation.
---

# Enterprise MSA Development Workflow

This skill guides the Antigravity agent through standard end-to-end development, testing, and deployment workflows across the microservices ecosystem.

---

## 1. Feature Implementation Flow

When implementing a new feature spanning frontend and backend:

1. **API & Contract First**:
   - Define or update the OpenAPI specification and Spring Boot controller DTOs (`record`).
   - Ensure DTO fields have `@Schema` descriptions and validation constraints (`@NotBlank`, `@NotNull`, etc.).
   - Verify error responses map to RFC 7807 (`ProblemDetail`).

2. **Frontend Integration (Next.js)**:
   - Check if a Route Handler BFF (`app/api/**/route.ts`) is needed for token isolation or aggregation.
   - Use TanStack Query hooks for server state management.
   - Define Zod schemas for runtime response parsing.

3. **Domain & Persistence (Spring Boot)**:
   - Keep domain entities decoupled from Spring/JPA annotations where possible.
   - Default all JPA associations to `FetchType.LAZY` and use `@EntityGraph` or `JOIN FETCH` to prevent N+1 queries.
   - Run transactions in `@Transactional(readOnly = true)` for pure queries.

---

## 2. Event-Driven Messaging Flow (Kafka)

When adding or modifying asynchronous events:

1. **Define CloudEvents Payload**:
   - Wrap the event data in the CloudEvents standard envelope (`id`, `source`, `type`, `time`, `datacontenttype`, `data`).
   - Assign an aggregate partition key (e.g. `orderId`, `customerId`).

2. **Publish via Transactional Outbox**:
   - Store the event inside the service's `outbox_events` table within the same DB transaction.
   - Never perform synchronous `kafkaTemplate.send()` inside a DB transaction boundary.

3. **Implement Idempotent Consumer**:
   - In the consumer service, check the event ID against `processed_events` table before applying business effects.
   - Configure a Dead Letter Topic (`<topic>.DLT`) with exponential backoff for unrecoverable failures.

---

## 3. Distributed State & Caching Flow (Redis)

1. **Cache-Aside Pattern**:
   - Read from Redis first. On miss, read DB and populate Redis with an explicit TTL + random Jitter.
   - On write/update, invalidate (delete) the Redis key; do not update in-place.
2. **Distributed Locks (Redisson)**:
   - Use `redissonClient.getLock("lock:<domain>:<id>")` with strict `tryLock(waitTime, leaseTime, unit)` timeouts.
   - Always release the lock in a `finally` block.

---

## 4. Verification & Testing Checklist

Before opening a PR:
- [ ] Run unit and integration tests (`./mvnw test` or `./gradlew test`).
- [ ] Verify Dockerfile builds cleanly with multi-stage non-root container.
- [ ] Ensure git commit messages adhere to Conventional Commits: `feat(...)`, `fix(...)`, `refactor(...)`.
- [ ] Check that PR diff is under 400 lines (Google Small PR rule).
