## 📌 요약 및 배경 (Summary & Context)

<!-- 문제 상황, 작업 동기 및 관련 이슈 링크를 간략하게 설명해주세요. -->

- **관련 이슈**: Closes #<!-- 이슈 번호 -->
- **서비스/모듈**: `order-service` | `auth-service` | `frontend` | `gateway`

## 🛠️ 주요 변경 사항 (Key Changes)

<!-- 무엇을 변경했으며, 어떤 방식으로 해결했는지 작성해주세요. -->

- 
- 

## 🧪 검증 및 테스트 (Verification & Testing)

<!-- 변경 사항을 어떻게 테스트했는지 명령어나 테스트 시나리오를 작성해주세요. -->

- [ ]  
    
    **단위 테스트**: 테스트 코드 추가 및 수정 (`./gradlew test` / `pnpm test`)
    
- [ ]  
    
    **통합 테스트**: Testcontainers를 활용한 테스트 진행 (`Kafka`, `Redis`, `PostgreSQL`)
    
- [ ]  
    
    **로컬 검증**: Swagger UI (`/swagger-ui.html`) 또는 API Gateway를 통한 정상 동작 확인
    

## 🔒 보안 및 성능 점검 (Security & Performance Checklist)

- [ ]  
    
    API Key, 비밀번호 등 민감한 정보가 코드에 하드코딩되지 않았는지 확인
    
- [ ]  
    
    N+1 쿼리가 발생하지 않는지 확인 (Hibernate SQL 로그 / 실행 계획 확인)
    
- [ ]  
    
    Kafka Consumer의 멱등성 처리 여부 확인
    
- [ ]  
    
    Redis Key의 TTL 설정 여부 확인 (불필요한 메모리 누수 방지)
    

## 📸 스크린샷 및 화면 녹화 (UI Only)

<!-- Next.js에서 UI 변경이 발생한 경우, 변경 전후의 스크린샷 또는 GIF를 첨부해주세요. -->

| 변경 전 (Before) | 변경 후 (After) |
| --- | --- |
| *해당 없음* | *해당 없음* |

---

## 📋 병합 전 체크리스트 (Pre-Merge Checklist)

- [ ]  
    
    커밋 메시지가 Conventional Commits 규칙을 준수하는지 확인
    
- [ ]  
    
    최신 `main` 브랜치의 변경 사항이 현재 브랜치에 반영되었는지 확인 (Rebase)
    
- [ ]  
    
    PR의 변경 코드가 400줄 이하인지 확인 (초과 시 여러 개의 PR로 분리 권장)
