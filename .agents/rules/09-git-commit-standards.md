---
description: Silicon Valley / Big Tech Git commit standards, Conventional Commits, Atomic commits, and Git hygiene.
globs: .git/**, **/*
alwaysApply: false
---

# Git Commit & Branching Standards (Silicon Valley Enterprise)

## 1. Conventional Commits 1.0.0 Specification

Every commit message must adhere to the Conventional Commits structure:

```text
<type>(<scope>): <short summary in imperative mood>

[optional body: explain WHAT and WHY, not HOW]

[optional footer(s): Breaking Changes, Issue References]
```

### Allowed Types
- **`feat`**: A new user-facing feature or API capability.
- **`fix`**: A bug fix or patch.
- **`refactor`**: Code change that neither fixes a bug nor adds a feature (restructuring, clean code).
- **`perf`**: Code change that improves execution speed, memory usage, or query performance.
- **`test`**: Adding missing tests or correcting existing tests (zero production code modification).
- **`docs`**: Documentation changes only (e.g., Swagger descriptions, README, architecture ADRs).
- **`style`**: Changes that do not affect the meaning of the code (whitespace, formatting, semicolons).
- **`chore`**: Maintenance tasks, build system updates, dependency bumps, or tool configurations.
- **`ci`**: Changes to CI/CD configuration files and scripts (GitHub Actions, Dockerfiles, Helm).

### Scopes (Microservice & Layer Context)
Use lowercase service or module names for `<scope>`:
- Service Scopes: `gateway`, `auth`, `order`, `payment`, `inventory`, `notification`
- Frontend Scopes: `ui`, `web`, `cart`, `checkout`
- Infrastructure Scopes: `kafka`, `redis`, `k8s`, `security`, `deps`

### Subject Line Rules (Strict)
1. **Imperative Mood**: Use "add", "fix", "implement", "change" (NOT "added", "fixes", "implementing").
2. **No Capitalization & No Period**: Start with a lowercase letter and never end with a period (`.`).
3. **Character Limit**: Keep the subject under **50-72 characters**.

### Examples
```text
feat(order): implement idempotent checkout with redis distributed lock

Enforce distributed locking around stock deduction to eliminate
race conditions during high-concurrency flash sales.

Closes #142
```

```text
fix(auth): prevent race condition during jwt refresh token rotation

When multiple concurrent requests hit the refresh endpoint simultaneously,
validate the sliding window token before invalidating family chain.

Fixes #289
```

```text
feat(kafka)!: migrate order-event schema to cloudevents 1.0

BREAKING CHANGE: Event envelope format changed. Existing consumers must
upgrade to event-schema-registry >= 2.1.0 before consuming this topic.
```

---

## 2. Atomic Commits & Git Hygiene

- **Single Responsibility (Atomic Commits)**:
  - Each commit must represent one, and only one, logical change.
  - Never mix automated refactoring/formatting (e.g., Prettier/Spotless) with actual business logic changes. Separate them into two distinct commits.
- **Clean Linear History**:
  - Keep history clean using `git rebase` against `main` before opening or updating a PR.
  - Avoid noisy merge commits (`Merge branch 'main' into feat/...`) in feature branches.
- **Git Hook Enforcement (Husky / commitlint)**:
  - Commit messages should be automatically validated at pre-commit / commit-msg hook stages using `@commitlint/config-conventional`.

---

## 3. Branch Naming Strategy (Trunk-Based / GitHub Flow)

- Format: `<type>/<issue-number>-<short-description>` or `<type>/<short-description>`
- Examples:
  - `feat/102-order-saga-orchestrator`
  - `fix/88-redis-lock-leak`
  - `refactor/clean-hexagonal-ports`
  - `perf/kafka-batch-consumer`
- Lifespan: Branches must be short-lived (1 to 2 days maximum) to minimize merge friction.
