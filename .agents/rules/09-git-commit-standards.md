---
description: 실리콘밸리 빅테크 Git 커밋 표준, Conventional Commits 규격, 원바이원 기능단위 커밋 및 브랜치 전략.
globs: .git/**, **/*
alwaysApply: false
---

# Git 커밋 표준 및 원바이원(One-by-One) 기능단위 원칙

## 1. Conventional Commits 1.0.0 규격

모든 커밋 메시지는 Conventional Commits 규격을 엄격히 준수해야 합니다:

```text
<type>(<scope>): <명령조 요약 (한국어 또는 영어 권장)>

[선택적 본문: 무엇을 왜 바꿨는지 설명 (HOW가 아닌 WHAT과 WHY 위주)]

[선택적 바닥글: 연관 이슈 번호 (Closes #123)]
```

### 허용되는 커밋 타입 (Types)
- **`feat`**: 새로운 사용자 기능, API 엔드포인트 추가.
- **`fix`**: 버그 수정 및 결함 패치.
- **`refactor`**: 기능 추가나 버그 수정이 없는 순수 코드 구조 개선 및 리팩토링.
- **`perf`**: 실행 속도, 메모리 사용량, 쿼리 성능 개선.
- **`test`**: 누락된 테스트 추가 또는 기존 테스트 수정 (운영 코드 수정 없음).
- **`docs`**: 문서 수정 (Swagger 설명, README, 설계 문서 등).
- **`style`**: 비즈니스 로직에 영향을 주지 않는 코드 포맷팅, 세미콜론 누락 등.
- **`chore`**: 빌드 설정, 의존성 라이브러리 업데이트, 도구 설정 변경.
- **`ci`**: CI/CD 설정 파일 및 배포 스크립트 수정.

### 스코프 규칙 (Scopes)
소문자 모듈/서비스명 사용:
- 서비스 스코프: `gateway`, `auth`, `order`, `payment`, `inventory`, `notification`
- 프론트엔드 스코프: `ui`, `web`, `cart`, `checkout`
- 인프라 스코프: `kafka`, `redis`, `k8s`, `security`, `deps`, `rules`

---

## 2. 🚨 원바이원(One-by-One) 기능 단위 분할 커밋 (엄격 준수)

- **일괄 덩어리 커밋(Omnibus Bulk Commit) 전면 금지**:
  - DTO, 도메인 엔티티, 서비스 로직, 컨트롤러, 프론트엔드를 한 번에 묶어서 커밋하는 행위를 엄격히 금지합니다.
- **기능 단위 순차 분할 (One-by-One)**:
  - 작업을 최소 실행/검증 단위로 쪼개어 단계별로 커밋합니다:
    1. **단위 1 (계약/DTO)**: Request/Response Record, 검증 정규식, ErrorCode 정의
    2. **단위 2 (도메인/영속성)**: 순수 도메인 엔티티, Outbox 감사 모델, JPA Repository
    3. **단위 3 (서비스/캐시)**: Use Case 인바운드 포트, 비즈니스 서비스, Redis 세션/캐시 제어
    4. **단위 4 (웹 어댑터)**: REST Controller, Gateway 헤더 바인딩, Swagger/OpenAPI 명세, GlobalExceptionHandler
    5. **단위 5 (프론트엔드)**: Next.js BFF Route Handler, TanStack Query 훅, UI 컴포넌트

---

## 3. 원바이원 브랜치 생성 및 PR 수명주기

1. **단위별 이슈 발행**: 세부 기능 단위마다 GitHub Issue를 각각 개별 발행합니다 (예: `#11 DTO 정의`, `#12 도메인 엔티티 구현`).
2. **이슈 전용 브랜치 분기**:
   - `main` 브랜치에서 분기: `feat/<issue-id>-<short-description>`
   - 예시: `feat/11-auth-dtos`, `feat/12-user-entity`
3. **단위 구현 & 커밋**:
   - 해당 기능만 구현 후 커밋: `feat(auth): add change password request dtos (#11)`
4. **원격 푸시 & PR 발행**:
   - `git push -u origin feat/<issue-id>-<short-description>`
   - PR 본문에 `Closes #<issue-id>` 명시 후 리뷰 및 Squash & Merge
5. **다음 기능 단위 진행**:
   - `main` 브랜치를 최신으로 pull한 뒤, 다음 기능 단위 이슈를 기반으로 새 브랜치를 분기하여 반복합니다.
