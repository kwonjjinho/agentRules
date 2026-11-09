# Antigravity Workspace Guidelines (MSA Fullstack Project)

This project is an enterprise-grade Microservices Architecture (MSA) platform built with Silicon Valley engineering standards. Antigravity must strictly adhere to the domain boundaries, resilience patterns, security models, and coding standards outlined below.

---

## 🏛️ System Architecture Overview

- **Frontend**: Next.js 14/15 App Router, React Server Components (RSC), TanStack Query, Strict TypeScript, HttpOnly Cookie Auth.
- **Backend Services**: Java 21, Spring Boot 3.x, Spring Cloud Netflix Eureka, Spring Security (Stateless JWT).
- **Communication & Event-Driven**:
  - Synchronous: OpenFeign with Resilience4j (Timeout, Circuit Breaker, Bulkhead).
  - Asynchronous: Apache Kafka with CloudEvents 1.0, Transactional Outbox Pattern, Idempotent Consumers, DLT.
- **Caching & State**: Redis (Cache-Aside, TTL Jitter, Redisson Distributed Locks).
- **Documentation**: Springdoc OpenAPI 3.0 / Swagger UI with global Bearer JWT auth.
- **Infrastructure & CI/CD**: Docker (Distroless multi-stage), Kubernetes (Actuator Probes, Non-root), GitHub Actions (Matrix, Trivy, GitOps).

---

## 🧭 Core Architectural Guidelines

### 🚨 Mandatory Workflow: Issue -> Branch -> Commit -> PR (Strictly Enforced)
- **Every change must start with an Issue**: No code, configuration, rule, or documentation change may be implemented without an active GitHub issue.
- **Traceable Branching**: Never work directly on `main`. Always create a branch tied to the issue: `<type>/<issue-id>-<slug>` (e.g., `feat/42-change-password-api`).
- **Atomic Conventional Commits**: Every commit must follow Conventional Commits and reference the issue number.
- **Automated PR Lifecycle**: Open a PR using `.github/pull_request_template.md` with `Closes #<issue-id>` to ensure end-to-end traceability before merging.

### 1. Zero-Trust & Gateway Authentication
- External traffic must always ingress through Spring Cloud Gateway.
- The Gateway validates JWT signatures and injects trusted internal headers: `X-User-Id`, `X-User-Roles`.
- Downstream microservices trust these headers and reject external traffic bypassing the Gateway.

### 2. Transactional Outbox Pattern (Dual-Write Prevention)
- Never publish directly to Kafka inside a database transaction method.
- Persist domain events into the local `outbox_events` table within the same ACID transaction, and let CDC (Debezium) or an async outbox poller relay messages to Kafka.

### 3. Kafka Idempotent Consumption
- All Kafka consumers must be idempotent.
- Check processed event IDs against an `idempotency_keys` table before executing business logic.
- Route non-retryable failures to the Dead Letter Topic (`<topic-name>.DLT`).

### 4. Redis Caching & Stampede Defense
- Always specify an explicit TTL on every cached key.
- Apply random TTL Jitter (base TTL + random offset) to prevent mass cache expiration (Thundering Herd).
- Use Redisson `RLock` for distributed locking (e.g. stock deduction); never use raw `SETNX` commands.

### 5. Next.js App Router & BFF
- Components in `app/` are React Server Components (RSC) by default. Keep `'use client'` strictly at leaf nodes.
- Use Route Handlers (`app/api/**/route.ts`) as a Backend-For-Frontend (BFF) to aggregate microservices and safeguard auth cookies.

### 6. Git & Code Review Standards
- Follow Conventional Commits: `<type>(<scope>): <short summary in imperative mood>`.
- Keep Pull Requests small: diff < 200 lines recommended, 400 lines max.
- Use Conventional Comments in reviews: `blocking:`, `suggestion:`, `question:`, `nit:`, `kudos:`.

---

## 📂 Detailed Modular Rule References

Antigravity references the detailed domain rules located in `.agents/rules/`:

1. [MSA Architecture & Resilience](.agents/rules/00-architecture-msa.md)
2. [Spring Boot & Java 21 Clean Architecture](.agents/rules/01-spring-boot-java.md)
3. [Spring Security & Gateway Token Relay](.agents/rules/02-spring-security.md)
4. [Next.js App Router & TypeScript](.agents/rules/03-nextjs-frontend.md)
5. [Event-Driven Architecture & Apache Kafka](.agents/rules/04-eda-kafka.md)
6. [Redis Caching & Distributed Synchronization](.agents/rules/05-redis-caching.md)
7. [Swagger & OpenAPI 3.x Documentation](.agents/rules/06-swagger-openapi.md)
8. [Docker & Kubernetes Production Deployment](.agents/rules/07-docker-kubernetes.md)
9. [GitHub Actions CI/CD & GitOps Pipeline](.agents/rules/08-github-actions-ci-cd.md)
10. [Git Commit Standards & Linear History](.agents/rules/09-git-commit-standards.md)
11. [Pull Request & Code Review Culture](.agents/rules/10-github-pr-code-review.md)
12. [GitHub Full Lifecycle Management & MCP](.agents/rules/11-github-lifecycle-management.md)

---

## 🤖 GitHub Automation via Model Context Protocol (MCP)

Antigravity interacts with GitHub directly via `mcp_config.json` (`@modelcontextprotocol/server-github`):
- **Issue Creation**: User requests are transformed into structured GitHub issues (`.github/ISSUE_TEMPLATE/`).
- **Traceability**: Branches and PRs automatically bind to issue numbers (`feat/<issue-id>-<slug>`).
- **Autonomous Review**: Antigravity automates PR creation, status check monitoring, and Conventional Comments code reviews.
