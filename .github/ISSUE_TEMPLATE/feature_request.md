---
name: "🚀 기능 요청 (Feature Request)"
about: 새로운 기능, API 엔드포인트 또는 마이크로서비스 기능 제안
title: "feat(<scope>): <기능 요약>"
labels: ["enhancement", "needs-triage"]
assignees: []
---

## 🎯 사용자 스토리 / 비즈니스 가치
**<사용자/역할>** 으로서,
나는 **<수행하고자 하는 동작/기능>** 을 원하며,
이를 통해 **<얻고자 하는 비즈니스 가치/결과>** 를 달성하고자 합니다.

---

## 📋 인수 조건 (Acceptance Criteria)
<!-- 기능 완료 여부를 판단할 수 있는 체크리스트를 작성해 주세요. -->
- [ ] 인수 조건 1: 
- [ ] 인수 조건 2: 
- [ ] 인수 조건 3: 

---

## 🏛️ 아키텍처 및 세부 기술 사양
- **영향받는 서비스**: `gateway` | `auth-service` | `order-service` | `frontend` | `kafka` | `redis`
- **API 변경사항 (OpenAPI)**:
- **이벤트 스키마 (CloudEvents)**:
- **데이터베이스 영속성 (JPA Entity / Migration)**:

---

## 🔒 보안 및 비기능 요구사항
- [ ] 인증/인가(RBAC) 권한 정의
- [ ] Redis 키 TTL 및 메모리 한도 검토
- [ ] 목표 p99 응답 지연시간 및 처리량 검토
