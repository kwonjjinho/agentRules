# Enterprise MSA & Fullstack Project Agent Rules

이 저장소는 실리콘밸리 테크 기업(Big Tech / FAANG) 표준에 맞추어 설계된 **마이크로서비스 아키텍처(MSA) 및 풀스택 개발 규칙 모음**입니다.

Google DeepMind **Antigravity (AGY)** 및 **Cursor** AI 에이전트와 완벽히 호환되도록 구성되어 있습니다.

---

## 🧭 에이전트 호환성 구조 (Antigravity & Cursor)

```text
├── GEMINI.md                          # Antigravity 전역 워크스페이스 진입점 및 규칙
├── .agents/                           # Antigravity 네이티브 커스터마이징 루트
│   ├── rules/                         # 모듈별 세부 엔지니어링 가이드라인 (.md)
│   │   ├── 00-architecture-msa.md
│   │   ├── 01-spring-boot-java.md
│   │   ├── 02-spring-security.md
│   │   ├── 03-nextjs-frontend.md
│   │   ├── 04-eda-kafka.md
│   │   ├── 05-redis-caching.md
│   │   ├── 06-swagger-openapi.md
│   │   ├── 07-docker-kubernetes.md
│   │   ├── 08-github-actions-ci-cd.md
│   │   ├── 09-git-commit-standards.md
│   │   └── 10-github-pr-code-review.md
│   └── skills/
│       └── msa-workflow/SKILL.md      # Antigravity 전용 온디맨드 MSA 개발 스킬
├── .cursor/
│   └── rules/                         # Cursor 전용 규칙 파일 (.mdc, globs 매핑)
└── .github/
    └── pull_request_template.md       # GitHub 실무 PR 표준 템플릿
```

---

## 📁 룰 구성 요약

| 규칙 파일 | 핵심 기술 & 엔지니어링 표준 |
| :--- | :--- |
| **`00-architecture-msa`** | DDD, Database-per-service, Saga 패턴, Transactional Outbox, Netflix Eureka, OpenTelemetry 분산 트레이싱, Resilience4j |
| **`01-spring-boot-java`** | Java 21 (Record, Virtual Threads), 헥사고날/클린 아키텍처 계층 분리, RFC 7807 예외 처리, N+1 방지 JPA 규칙 |
| **`02-spring-security`** | Spring Security 6.x, 완전 무상태(Stateless), JWT 비대칭키(RS256) 검증, Gateway Token Relay & Header 전파 |
| **`03-nextjs-frontend`** | Next.js App Router, React Server Components (RSC 우선), BFF 패턴, TanStack Query, Zod 런타임 검증, HttpOnly 쿠키 인증 |
| **`04-eda-kafka`** | CloudEvents 표준 규격, Idempotent Producer/Consumer, Transactional Outbox, Exponential Backoff 및 DLT(Dead Letter Topic) |
| **`05-redis-caching`** | Cache-Aside 패턴, Thundering Herd 방지(TTL Jitter), Redisson 분산 락(RLock), BigKey 방지 및 JSON 직렬화 |
| **`06-swagger-openapi`** | Springdoc OpenAPI 3.0, Bearer JWT 글로벌 보안 스키마, HTTP 상태 코드별 응답 명세, DTO `@Schema` 필수화 |
| **`07-docker-kubernetes`** | Multi-Stage 빌드, Distroless/Non-root 보안, K8s Liveness/Readiness Probes, Requests/Limits 명시, Graceful Shutdown |
| **`08-github-actions-ci-cd`**| Path-based 워크플로우 분리, Docker Layer Caching(GHA), Trivy 취약점 스캔, GitOps(ArgoCD) 자동 동기화 |
| **`09-git-commit-standards`** | Conventional Commits 1.0, Atomic Commits(단일 책임 변경), Rebase 기반 선형 히스토리 관리, 브랜치 네이밍 |
| **`10-github-pr-code-review`** | Google 기준 Small PR 원칙(<400줄), Conventional Comments(blocking/suggestion/nit), PR 템플릿, 머지 정책 |

---

## 🚀 Antigravity 네이티브 기능 지원

1. **`GEMINI.md` 프로젝트 전역 규칙 자동 인덱싱**:
   - 에이전트가 세션을 시작할 때 워크스페이스의 루트 `GEMINI.md`를 즉시 로드하여 아키텍처 원칙을 준수합니다.
2. **Progressive Disclosure (`msa-workflow` 스킬)**:
   - 컨텍스트 윈도우 낭비 없이 필요한 시점에 `.agents/skills/msa-workflow/SKILL.md`를 활성화하여 기능 구현 및 검증 절차를 수행합니다.
3. **GitHub Pull Request 연동**:
   - `.github/pull_request_template.md`를 통해 PR 생성 시 실리콘밸리 엔지니어링 체크리스트가 자동 적용됩니다.
