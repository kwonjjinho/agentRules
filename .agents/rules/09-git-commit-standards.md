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
- Infrastructure Scopes: `kafka`, `redis`, `k8s`, `security`, `deps`, `rules`

### Subject Line Rules (Strict)
1. **Imperative Mood**: Use "add", "fix", "implement", "change" (NOT "added", "fixes", "implementing").
2. **No Capitalization & No Period**: Start with a lowercase letter and never end with a period (`.`).
3. **Character Limit**: Keep the subject under **50-72 characters**.

---

## 2. Atomic Commits & Functional-Unit Granularity (Strictly Enforced)

- **Functional-Unit Commit Granularity (기능 단위 분할 커밋)**:
  - **Omnibus Bulk Commits Forbidden**: Never lump multiple layers, models, DTOs, services, and configs into a single giant commit.
  - Divide work into incremental, testable, and reviewable functional units:
    1. **DTO & Contract**: API Request/Response records, Validation constraints, ErrorCode enums.
    2. **Domain & Persistence**: Pure domain entities, Value Objects, Outbox models, Repositories.
    3. **Application & Service**: Use Cases, Service orchestration, Redis distributed caching/locking.
    4. **Web & Docs**: Controller endpoints, OpenAPI `@Operation` / `@ApiResponse` annotations.
    5. **Frontend / BFF**: Route Handlers, TanStack Query hooks, UI components.
- **Single Responsibility (Atomic Commits)**:
  - Each commit represents one logical change.
  - Never mix automated refactoring/formatting (e.g., Spotless/Prettier) with business logic.
- **Clean Linear History**:
  - Keep history clean using `git rebase` against `main` before opening or updating a PR.
  - Avoid noisy merge commits (`Merge branch 'main' into feat/...`) in feature branches.

---

## 3. Branching & PR Lifecycle (Trunk-Based / GitHub Flow)

- **Dedicated Branching**:
  - Always create a dedicated branch off latest `main`: `<type>/<issue-number>-<slug>` (e.g. `feat/42-member-credential-management`).
  - Never commit directly to `main`.
- **Push & Automated PR**:
  - Push feature branch to origin: `git push -u origin <branch-name>`.
  - Open a Pull Request using `.github/pull_request_template.md` with `Closes #<issue-number>`.
