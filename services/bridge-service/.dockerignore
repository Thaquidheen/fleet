# Bridge Service Dockerfile
# Location: services/bridge-service/Dockerfile

FROM openjdk:17-jdk-alpine

LABEL maintainer="Fleet Management Team"
LABEL version="1.0.0"
LABEL description="Bridge Service for Fleet Management System - Traccar Integration"

# Create application directory
WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S bridgeuser && \
    adduser -S bridgeuser -u 1001 -G bridgeuser

# Install curl for health checks and timezone data
RUN apk add --no-cache curl tzdata

# Set timezone
ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Copy Maven wrapper and pom.xml first (for better Docker layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application
RUN ./mvnw clean package -DskipTests -B && \
    cp target/*.jar app.jar && \
    rm -rf target/ src/ .mvn/ mvnw pom.xml

# Create logs directory
RUN mkdir -p /app/logs && \
    chown -R bridgeuser:bridgeuser /app

# Switch to non-root user
USER bridgeuser

# Expose port
EXPOSE 8090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8090/bridge/actuator/health || exit 1

# JVM options for production
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication"

# Spring profile
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]