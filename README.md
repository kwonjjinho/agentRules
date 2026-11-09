# Enterprise MSA & Fullstack Project AI Rules (.mdc)

????μ냼???ㅻ━肄섎갭由??뚰겕 湲곗뾽(Big Tech / FAANG) ?쒖???留욎텛???ㅺ퀎??**留덉씠?щ줈?쒕퉬???꾪궎?띿쿂(MSA) 諛???ㅽ깮 媛쒕컻 洹쒖튃(`.cursor/rules/*.mdc`)** 紐⑥쓬?낅땲??

AI ?먯씠?꾪듃(Cursor, Antigravity ??媛 ?꾨줈?앺듃 而⑦뀓?ㅽ듃瑜??뺥솗???댄빐?섍퀬, 肄붾뱶 ?앹꽦 ???뷀꽣?꾨씪?댁쫰湲??덉젙?? 蹂댁븞, 媛愿痢≪꽦(Observability), ?깅뒫??以?섑븯?꾨줉 ?좊룄?⑸땲??

---

## ?뱚 猷??뚯씪 援ъ꽦 ?붿빟 (`.cursor/rules/`)

| 洹쒖튃 ?뚯씪 | ?곸슜 ???(Globs) | ?듭떖 湲곗닠 & ?붿??덉뼱留??쒖? |
| :--- | :--- | :--- |
| **`00-architecture-msa.mdc`** | `**/*` (Always Apply) | DDD, Database-per-service, Saga ?⑦꽩, Transactional Outbox, Netflix Eureka, OpenTelemetry 遺꾩궛 ?몃젅?댁떛, Resilience4j |
| **`01-spring-boot-java.mdc`** | `**/*.java`, `pom.xml`, `build.gradle*` | Java 21 (Record, Virtual Threads), ?μ궗怨좊궇/?대┛ ?꾪궎?띿쿂 怨꾩링 遺꾨━, RFC 7807 ?덉쇅 泥섎━, N+1 諛⑹? JPA 洹쒖튃 |
| **`02-spring-security.mdc`** | `**/security/**`, `**/auth/**` | Spring Security 6.x, ?꾩쟾 臾댁긽??Stateless), JWT 鍮꾨?移?궎(RS256) 寃利? Gateway Token Relay & Header ?꾪뙆 |
| **`03-nextjs-frontend.mdc`** | `**/*.tsx`, `**/*.ts`, `**/app/**` | Next.js App Router, React Server Components (RSC ?곗꽑), BFF ?⑦꽩, TanStack Query, Zod ?고???寃利? HttpOnly 荑좏궎 ?몄쬆 |
| **`04-eda-kafka.mdc`** | `**/kafka/**`, `**/*Producer*.java` ??| CloudEvents ?쒖? 洹쒓꺽, Idempotent Producer/Consumer, Transactional Outbox, Exponential Backoff 諛?DLT(Dead Letter Topic) |
| **`05-redis-caching.mdc`** | `**/redis/**`, `**/cache/**` | Cache-Aside ?⑦꽩, Thundering Herd 諛⑹?(TTL Jitter), Redisson 遺꾩궛 ??RLock), BigKey 諛⑹? 諛?JSON 吏곷젹??|
| **`06-swagger-openapi.mdc`** | `**/*Controller*.java`, `**/dto/**` | Springdoc OpenAPI 3.0, Bearer JWT 湲濡쒕쾶 蹂댁븞 ?ㅽ궎留? HTTP ?곹깭 肄붾뱶蹂??묐떟 紐낆꽭, DTO `@Schema` ?꾩닔??|
| **`07-docker-kubernetes.mdc`** | `**/Dockerfile*`, `**/*.yaml` | Multi-Stage 鍮뚮뱶, Distroless/Non-root 蹂댁븞, K8s Liveness/Readiness Probes, Requests/Limits 紐낆떆, Graceful Shutdown |
| **`08-github-actions-ci-cd.mdc`**| `.github/workflows/*.yml` | Path-based ?뚰겕?뚮줈??遺꾨━, Docker Layer Caching(GHA), Trivy 痍⑥빟???ㅼ틪, GitOps(ArgoCD) ?먮룞 ?숆린??|
| **`09-git-commit-standards.mdc`** | `.git/**`, `**/*` | Conventional Commits 1.0, Atomic Commits(?⑥씪 梨낆엫 蹂寃?, Rebase 湲곕컲 ?좏삎 ?덉뒪?좊━ 愿由? 釉뚮옖移??ㅼ씠諛?|
| **`10-github-pr-code-review.mdc`** | `.github/**`, `**/*` | Google 湲곗? Small PR ?먯튃(<400以?, Conventional Comments(blocking/suggestion/nit), PR ?쒗뵆由? 癒몄? ?뺤콉 |

---

## ?썱截?GitHub ?곕룞 ?쒗뵆由??쒓났
- **`.github/pull_request_template.md`**: ?ㅻ━肄섎갭由??쒖? PR ?쒗뵆由?(Context, Changes, Test checklist, Security & Perf, UI Screenshot)??湲곕낯 ?묒옱?섏뼱 ?덉뼱 GitHub ??μ냼??push ??諛붾줈 ?곸슜?⑸땲??

---

## ?? ?ㅻ━肄섎갭由??붿??덉뼱留??쒖???諛섏쁺??二쇱슂 ?ㅺ퀎 ?ъ씤??
1. **Transactional Outbox & Exactly-Once Semantics**:
   - DB ?몃옖??뀡怨?Kafka 諛쒗뻾 媛꾩쓽 ?먯옄??Atomicity)??蹂댁옣?섍린 ?꾪빐 Dual-write瑜?湲덉??섍퀬 Outbox ?⑦꽩??媛뺤젣?⑸땲??
2. **Zero-Trust & Gateway Token Relay**:
   - ?몃? ?붿껌? Spring Cloud Gateway?먯꽌 JWT瑜?寃利앺븯怨? ?대? ?쒕퉬?ㅻ줈??寃利앸맂 `X-User-Id`, `X-User-Roles` ?ㅻ뜑留??꾨떖?섏뿬 ?대? ?쒕퉬??媛?寃고빀?꾩? ?ㅻ쾭?ㅻ뱶瑜???땅?덈떎.
3. **App Router & BFF ?⑦꽩 (Next.js)**:
   - Next.js??Route Handlers瑜?寃쎈웾 BFF(Backend For Frontend)濡??쒖슜?섏뿬 ?щ윭 MSA ?쒕퉬???몄텧 吏묒빟, 蹂댁븞 荑좏궎 泥섎━, ?꾨줎?몄뿏??理쒖쟻???묐떟 蹂?섏쓣 ?섑뻾?⑸땲??
4. **Resilience & Chaos Engineering**:
   - 遺꾩궛 ?섍꼍?먯꽌???곗뇙 ?μ븷(Cascading Failure)瑜?李⑤떒?섍린 ?꾪빐 紐⑤뱺 ?숆린 ?몄텧????꾩븘?? ?쒗궥釉뚮젅?댁빱(Resilience4j), 踰뚰겕?ㅻ뱶瑜??섎Т?뷀빀?덈떎.
5. **Shift-Left Security & Container Hardening**:
   - Root ?좎? ?ㅽ뻾???먯쿇 李⑤떒(Distroless/Non-root)?섎ŉ, CI ?뚯씠?꾨씪?몄뿉??Trivy瑜??듯빐 諛고룷 ??痍⑥빟?먯쓣 ?ъ쟾??李⑤떒?⑸땲??
