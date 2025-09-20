# services/device-service/Dockerfile
FROM openjdk:17-jdk-alpine

LABEL maintainer="Fleet Management Team"
LABEL version="1.0.0"
LABEL description="Device Service for Fleet Management System"

# Create application directory
WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S fleetuser && \
    adduser -S fleetuser -u 1001 -G fleetuser

# Copy Maven wrapper and pom.xml first (for better caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application
RUN ./mvnw clean package -DskipTests -B && \
    cp target/*.jar app.jar && \
    rm -rf target/ src/ .mvn/ mvnw pom.xml

# Install curl for health checks
RUN apk add --no-cache curl

# Create logs directory
RUN mkdir -p /app/logs && \
    chown -R fleetuser:fleetuser /app

# Switch to non-root user
USER fleetuser

# Expose port
EXPOSE 8085

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8085/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Default JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication"

# Run with JVM options
CMD java $JAVA_OPTS -jar /app/app.jar
