## 📌 Summary & Context
<!-- Provide a brief explanation of the problem, motivation, and relevant issue links. -->
- **Issue**: Closes #<!-- issue number -->
- **Service/Module**: `order-service` | `auth-service` | `frontend` | `gateway`

## 🛠️ Key Changes
<!-- Bullet points explaining WHAT changed and HOW you approached the solution. -->
- 
- 

## 🧪 Verification & Testing
<!-- Detail how this change was tested. Include commands or test scenarios. -->
- [ ] **Unit Tests**: Added/updated test coverage (`./gradlew test` / `pnpm test`)
- [ ] **Integration Tests**: Tested with Testcontainers (`Kafka`, `Redis`, `PostgreSQL`)
- [ ] **Local Verification**: Verified via Swagger UI (`/swagger-ui.html`) or API Gateway

## 🔒 Security & Performance Checklist
- [ ] No hardcoded secrets, API keys, or private credentials
- [ ] No N+1 queries introduced (Checked Hibernate SQL logs / Execution Plan)
- [ ] Kafka consumer idempotency handled
- [ ] Redis key TTL explicitly defined (No unbounded memory leaks)

## 📸 Screenshots / Screen Recordings (UI Only)
<!-- If this PR introduces UI changes in Next.js, attach before/after screenshots or GIFs. -->
| Before | After |
| :---: | :---: |
| _N/A_ | _N/A_ |

---
## 📋 Pre-Merge Checklist
- [ ] Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/)
- [ ] Branch is rebased onto the latest `main`
- [ ] PR size is under 400 lines (or split into stacked PRs)
