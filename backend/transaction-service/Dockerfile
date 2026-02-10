# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

# Install Maven
RUN apk add --no-cache maven

# Copy entire backend for proper Maven multi-module build
COPY pom.xml ./pom.xml
COPY auth-service ./auth-service
COPY account-service ./account-service
COPY transaction-service ./transaction-service
COPY analytics-service ./analytics-service
COPY api-gateway ./api-gateway

# Build only this service and its dependencies
RUN mvn clean package -DskipTests -pl transaction-service -am

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy JAR from build stage
COPY --from=build /workspace/transaction-service/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
