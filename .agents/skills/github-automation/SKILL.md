---
name: github-automation
description: >-
  Use this skill whenever managing GitHub issues, pull requests, labels, releases, or project boards.
  Guides the agent through creating high-quality GitHub issues, branching, linking PRs, automated reviews,
  and executing the Silicon Valley full-cycle GitOps workflow.
---

# GitHub Full-Cycle Automation Skill (Silicon Valley Standard)

This skill enables the Antigravity agent to autonomously manage the complete GitHub project lifecycle, from initial issue triage to automated PR merging and release tagging.

> [!IMPORTANT]
> **Mandatory Rule**: Whenever the user requests any task (feature implementation, bug fix, rule refactoring, documentation update), the agent MUST proactively execute the full pipeline:
> **1. Create Issue -> 2. Create Branch (`feat/<issue-id>-...`) -> 3. Implement & Commit -> 4. Open PR (`Closes #<issue-id>`)**

---

## 1. Issue Triage & Creation Workflow

When the user asks to plan a feature, report a bug, or track technical debt:

1. **Classify the Request**:
   - `enhancement` / `feat`: New capability, API endpoint, or UI enhancement.
   - `bug` / `fix`: System defect, data inconsistency, 500 error, or broken contract.
   - `tech-debt` / `refactor`: Architectural refactoring, test coverage, dependency upgrades.

2. **Generate the Issue Body**:
   - Use the repository's issue templates in `.github/ISSUE_TEMPLATE/`.
   - Structure every issue with:
     - **Title**: `<type>(<scope>): <clear description in imperative mood>` (e.g. `feat(order): add idempotency key check to checkout api`)
     - **User Story / Problem**: Who needs this and why.
     - **Acceptance Criteria**: Verifiable checklist (Given-When-Then or clear bullet points).
     - **Technical Boundaries**: Microservices affected, Kafka event schemas, Redis keys, DB migrations.
     - **Labels**: `enhancement`, `backend`, `p1-high`, etc.

3. **Execution via GitHub MCP or CLI**:
   - If GitHub MCP is active, call `create_issue(title, body, labels, assignees)`.
   - Alternatively, execute via GitHub REST API / `curl`.
   - Return the created issue URL and issue number (e.g. `#42`) to the user.

---

## 2. Feature Branching & Implementation

1. **Branch Naming Standard**:
   - Always derive the branch name from the issue: `<type>/<issue-number>-<short-description>`
   - Examples:
     - `feat/42-idempotent-checkout`
     - `fix/89-jwt-refresh-race`
2. **Checkout Branch**:
   - `git checkout -b feat/42-idempotent-checkout`
3. **Atomic Development**:
   - Implement domain changes following `.agents/rules/`.
   - Write tests first or concurrently.

---

## 3. Pull Request Creation & Review

1. **Commit Message**:
   - Strict Conventional Commits: `feat(order): implement idempotent checkout (#42)`
2. **Push to Remote**:
   - `git push -u origin feat/42-idempotent-checkout`
3. **Create PR**:
   - Populate description using `.github/pull_request_template.md`.
   - Link the issue: `Closes #42` in the PR description so GitHub automatically closes the issue upon merge.
   - Verify PR size: Keep diff under 400 lines (Google Small PR rule).

---

## 4. PR Review & Merge Policy

1. **Automated Review Checks**:
   - CI build green (tests, SonarQube, Trivy container scan).
   - No breaking API changes without version bump.
2. **Merge Method**:
   - **Squash and Merge**: Preserve clean, linear git history on `main`.
   - Delete the remote feature branch upon merge to avoid branch clutter.
