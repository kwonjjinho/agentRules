## 📌 개요 및 배경 (Context)
<!-- 해결하려는 문제, 배경 설명 및 연관된 GitHub 이슈 링크를 작성해 주세요. -->
- **연관 이슈**: Closes #<!-- 이슈 번호 입력 -->
- **대상 서비스/모듈**: `auth-service` | `order-service` | `gateway` | `frontend`

---

## 🛠️ 주요 변경사항 (Key Changes)
<!-- 무엇을 어떻게 변경했는지 핵심 사항을 불릿 포인트로 작성해 주세요. -->
- 
- 

---

## 🧪 테스트 및 검증 결과 (Verification)
<!-- 로컬 또는 CI 환경에서 수행한 테스트 항목을 체크해 주세요. -->
- [ ] 단위 테스트(Unit Tests) 작성 및 전체 통과 (`./gradlew test` 또는 `pnpm test`)
- [ ] Testcontainers 기반 통합 테스트 검증 (`PostgreSQL`, `Kafka`, `Redis`)
- [ ] Swagger UI(`/swagger-ui.html`) 또는 게이트웨이를 통한 로컬 동작 확인

---

## 🔒 보안 및 성능 점검 체크리스트
- [ ] 하드코딩된 API 키, 패스워드 등 민감 정보가 포함되지 않음
- [ ] 본인 확인 API에 URL {userId} PathVariable 대신 헤더/토큰 식별자 바인딩 (IDOR 취약점 방어)
- [ ] JPA N+1 쿼리 방지 확인 (JOIN FETCH 또는 @EntityGraph 적용)
- [ ] Redis 캐시 키에 명시적 TTL 부여 (메모리 누수 방지)
- [ ] Kafka 컨슈머 멱등성(Idempotency) 처리 확인

---

## 📸 UI 변경 스크린샷 (프론트엔드 작업 시)
| 변경 전 (Before) | 변경 후 (After) |
| :---: | :---: |
| _해당 없음_ | _해당 없음_ |

---

## 📋 머지 전 최종 확인
- [ ] 기능 단위(Functional Unit)로 나누어진 원자적 커밋 구성
- [ ] 커밋 메시지가 Conventional Commits 규격을 준수함
- [ ] 변경 코드 분량(diff)이 400줄 이하인지 확인
