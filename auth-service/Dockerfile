# ==============================================================================
# Build Stage: Java 21 & Maven Compilation
# ==============================================================================
FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /workspace

COPY pom.xml ./
RUN mvn dependency:go-offline -B || true

COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================================
# Production Runtime Stage: Java 21 JRE Jammy
# ==============================================================================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder /workspace/target/*.jar app.jar

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseZGC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
