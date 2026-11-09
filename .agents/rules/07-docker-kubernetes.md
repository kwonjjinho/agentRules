---
description: Production Multi-stage Docker builds, Distroless images, Kubernetes (K8s) manifests, Probes, and Resource allocation rules.
globs: **/Dockerfile*, **/k8s/**, **/helm/**, **/deploy/**, **/*.yaml, **/*.yml
alwaysApply: false
---

# Docker & Kubernetes (K8s) Production Engineering Rules

## 1. Production Multi-Stage Dockerfile Standards

- **Spring Boot Multi-Stage (Distroless / Eclipse Temurin JRE)**:
  - Never run as `root`. Use unprivileged non-root users (`nonroot:65532`).
  - Extract Spring Boot layers (`layertools`) for optimal Docker layer caching.

```dockerfile
# Build Stage
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw clean package -DskipTests && \
    java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# Final Runtime Stage
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
COPY --from=builder /workspace/target/extracted/dependencies/ ./
COPY --from=builder /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/target/extracted/application/ ./

USER nonroot:nonroot
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseZGC"
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

- **Next.js Standalone Build**:
  - Enable `output: 'standalone'` in `next.config.js`.
  - Copy `.next/standalone` and `.next/static` to a minimal `node:20-alpine` image as `nodejs` user.

## 2. Kubernetes Deployment Standards

- **Probes (Actuator Integration)**:
  - Always configure `livenessProbe`, `readinessProbe`, and `startupProbe` using Spring Boot Actuator:
  ```yaml
  livenessProbe:
    httpGet:
      path: /actuator/health/liveness
      port: 8080
    initialDelaySeconds: 15
    periodSeconds: 10
    failureThreshold: 3
  readinessProbe:
    httpGet:
      path: /actuator/health/readiness
      port: 8080
    initialDelaySeconds: 10
    periodSeconds: 5
    failureThreshold: 2
  ```
- **Explicit Resource Requests & Limits**:
  - Never deploy a pod without `resources.requests` and `resources.limits`:
  ```yaml
  resources:
    requests:
      cpu: "250m"
      memory: "512Mi"
    limits:
      cpu: "1000m"
      memory: "1024Mi"
  ```
- **Horizontal Pod Autoscaling (HPA)**:
  - Target CPU utilization around 70% or custom metrics (e.g. Kafka consumer lag).
- **Graceful Shutdown**:
  - Set `terminationGracePeriodSeconds: 30` (or 60).
  - Configure `server.shutdown=graceful` in `application.yml` to allow inflight requests to complete.
- **Security Context**:
  ```yaml
  securityContext:
    runAsNonRoot: true
    runAsUser: 65532
    readOnlyRootFilesystem: true
    allowPrivilegeEscalation: false
  ```
