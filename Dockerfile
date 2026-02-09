# Multi-stage build for HotResvib Spring Boot application
# Stage 1: Build
FROM gradle:8-jdk17 AS builder

WORKDIR /app

# Copy gradle configuration files
COPY gradle/ gradle/
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradlew .
COPY gradlew.bat .

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Create non-root user for security
RUN useradd -m -u 1000 hotresvib && \
    mkdir -p /var/log/hotresvib && \
    chown -R hotresvib:hotresvib /var/log/hotresvib

# Copy JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar
COPY --chown=hotresvib:hotresvib src/main/resources/logback-spring.xml /app/

# Set JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx2g \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+ParallelRefProcEnabled \
    -Dspring.profiles.active=prod"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD java -cp app.jar org.springframework.boot.loader.JarLauncher &>/dev/null && \
        wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Switch to non-root user
USER hotresvib

# Expose ports
EXPOSE 8080 9090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
