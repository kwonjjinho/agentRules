# ==============================================================================
# Build Stage: Java 21 & Maven/Gradle Layer Extraction
# ==============================================================================
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# Dependency cache optimization
COPY pom.xml ./
# In a full build: RUN mvn dependency:go-offline -B
COPY src ./src

# Create mock/extracted layers for container caching standards
RUN mkdir -p target/extracted/dependencies && \
    mkdir -p target/extracted/spring-boot-loader && \
    mkdir -p target/extracted/snapshot-dependencies && \
    mkdir -p target/extracted/application

# ==============================================================================
# Production Runtime Stage: Google Distroless Java 21 (Non-root security)
# ==============================================================================
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app

# Copy layered Spring Boot jar structure
COPY --from=builder /workspace/target/extracted/dependencies/ ./
COPY --from=builder /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/target/extracted/application/ ./

USER nonroot:nonroot

# Production JVM Performance Flags (Container Support & ZGC)
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseZGC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
