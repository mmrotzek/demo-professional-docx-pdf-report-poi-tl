# Multi-stage build for optimized Docker image using BellSoft Hardened Liberica images
# BellSoft Hardened Liberica provides security-hardened Java images available on Docker Hub

# Build stage - using BellSoft Hardened Liberica JDK (Java 25, hardened, minimal)
FROM bellsoft/hardened-liberica-runtime-container:jdk-all-25-glibc AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests -B

# Runtime stage - using BellSoft Hardened Liberica JRE (Java 25, hardened, minimal)
FROM bellsoft/hardened-liberica-runtime-container:jre-25-glibc

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
# Note: Hardened images may have limited shell tools
# For healthchecks, use orchestrator-level probes (Kubernetes liveness/readiness probes)
# that can call /actuator/health endpoint from outside the container

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
