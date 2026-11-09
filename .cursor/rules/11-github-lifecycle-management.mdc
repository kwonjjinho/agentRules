---
description: End-to-end GitHub Lifecycle Management, Issue creation standards, Branching, PR automation, and MCP integration.
globs: .github/**, **/*
alwaysApply: false
---

# GitHub Full Lifecycle Management Rules

## 1. Issue-Driven Development (IDD)

- **Zero Untracked Changes**:
  - No code changes or PRs should be introduced without an associated GitHub Issue (excluding trivial hotfixes).
- **Issue Naming & Taxonomy**:
  - Use Conventional Commit syntax for issue titles:
    - `feat(service): <description>`
    - `fix(service): <description>`
    - `refactor(module): <description>`
- **Mandatory Issue Sections**:
  - **Context / User Story**: Business reason and user value.
  - **Acceptance Criteria**: Verifiable checkboxes before marking done.
  - **Technical Impact**: Target microservices, API contract, Kafka events, DB schema changes.

## 2. Issue to PR Traceability

- **Branch-Issue Binding**:
  - Branch names must reference the issue number: `<type>/<issue-number>-<slug>` (e.g., `feat/102-order-outbox`).
- **Automated Issue Closing**:
  - Every PR description must contain `Closes #<issue-number>` or `Fixes #<issue-number>`.

## 3. GitHub MCP Server Integration

- Antigravity uses the Model Context Protocol (`mcp_config.json`) with `@modelcontextprotocol/server-github` to interact directly with GitHub.
- Supported agent actions:
  - `create_issue`: Draft and publish structured issues with labels and milestones.
  - `create_pull_request`: Open PRs populated with standard templates.
  - `add_issue_comment`: Leave progress notes, architectural trade-offs, or review feedback.
  - `get_issue` / `list_issues`: Fetch current backlog priorities and sprint tasks.
