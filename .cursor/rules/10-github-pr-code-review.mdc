---
description: Silicon Valley / Big Tech Pull Request (PR) lifecycle, Code Review etiquette, Conventional Comments, and Merge policies.
globs: .github/**, **/*
alwaysApply: false
---

# Pull Request (PR) & Code Review Standards (Silicon Valley Enterprise)

## 1. Small Pull Request Rule (Google / Chromium Engineering Standard)

- **PR Size Boundary**:
  - **Ideal**: < 200 lines of diff (excluding lockfiles and generated code).
  - **Maximum**: 400 lines of diff. Any PR exceeding 400 lines must be split into stacked PRs or feature-flagged increments.
- **Why**:
  - Review speed drops by 70% and defect detection drops by 90% when PRs exceed 400 lines.
  - Small PRs enable fast feedback loops (< 4 hours turnaround time) and trivial rollbacks.

---

## 2. PR Structure & Description Requirements

Every PR must contain a high-signal description structured with the following sections:

### Required PR Sections
1. **Context & Motivation (Why)**:
   - What business problem or bug does this solve? Link Jira ticket or GitHub issue.
2. **Key Changes (What & How)**:
   - High-level architectural or algorithmic overview.
   - Mention any database migrations, new Kafka topics, or breaking API changes.
3. **Verification & Testing (How to Test)**:
   - List unit, integration, and load tests executed.
   - Include CLI commands to reproduce or verify locally.
4. **Security & Performance Considerations**:
   - Query efficiency (e.g., analyzed execution plan for JPA queries).
   - Redis memory footprint or Kafka partition impact.
5. **Visual Evidence (Frontend / Next.js)**:
   - Mandatory before/after screenshots or short video for UI mutations.

---

## 3. Code Review Culture: Conventional Comments Standard

Reviewers must use **Conventional Comments** to label feedback clearly and separate blocking issues from suggestions:

| Label | Meaning & Severity | Action Required |
| :--- | :--- | :--- |
| **`blocking:`** or **`p1:`** | Critical bug, security flaw, data loss risk, or violation of architectural boundary. | Must be fixed before merge. |
| **`suggestion:`** or **`p2:`** | Recommended improvement or clean-code enhancement. | Author strongly encouraged to adopt or explain alternative. |
| **`question:`** | Inquiry to understand logic or architectural intent. | Author must answer; no code change strictly required. |
| **`nit:`** (Nitpick) | Trivial preference (formatting, naming, minor comment typo). | Non-blocking. Author may accept or ignore. |
| **`kudos:`** | Genuine appreciation for elegant code, thorough tests, or clever design. | Morale booster. No action needed. |

### Example Review Comments
```text
blocking: This Kafka listener performs synchronous REST calls without a circuit breaker or timeout. If downstream fails, partitions will get blocked. Let's wrap this with Resilience4j or offload to an async event queue.
```

```text
suggestion: Since this JPA query fetches a collection, let's use `@EntityGraph` or `JOIN FETCH` here to avoid an N+1 query issue when rendering order items.
```

```text
nit: Consider renaming `calcTot()` to `calculateTotalAmount()` to match our domain naming conventions.
```

---

## 4. CI/CD Gating & Merge Policy

- **Merge Gates (All Required)**:
  1. **Green CI Build**: All unit tests and integration tests (`Testcontainers`) pass.
  2. **Zero Critical Vulnerabilities**: Trivy container scan and Snyk/Dependabot reports clean.
  3. **Code Coverage**: Minimum 80% line and branch coverage on newly introduced code.
  4. **Approvals**: At least **1 peer review approval** (2 for core security/payment services).
  5. **Branch Up-to-Date**: Branch must be rebased onto latest `main`.

- **Merge Method**:
  - Default: **Squash and Merge** (ensures a pristine, linear `main` git history).
  - Sanitized commit title: Must match Conventional Commit format (e.g., `feat(order): implement saga orchestrator (#142)`).
