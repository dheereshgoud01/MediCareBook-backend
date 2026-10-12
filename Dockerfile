# Multi-Stage Dockerfile for MediCareBook AI Spring Boot Backend

# Stage 1: Build the Maven artifact
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build production jar without tests (tests run in CI pipeline)
RUN mvn clean package -DskipTests

# Stage 2: Minimal Runtime Environment
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security best practice
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy jar artifact from build stage
COPY --from=build /app/target/medicarebook-backend-1.0.0.jar app.jar

# Expose backend REST API port
EXPOSE 8000

# Health check using Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8000/actuator/health || exit 1

# Launch Spring Boot with optimized JVM parameters
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
