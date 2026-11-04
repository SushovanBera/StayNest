# ===================================================================
# Stage 1: Build Application with Maven
# ===================================================================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:resolve -B || true

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===================================================================
# Stage 2: Lightweight Runtime Environment
# ===================================================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Run as non-root user for cloud security best practices
RUN groupadd -r staynest && useradd -r -g staynest -m -d /home/staynest staynest
USER staynest

# Copy the built Spring Boot runnable JAR
COPY --from=build --chown=staynest:staynest /app/target/*.jar app.jar

# Render exposes the application port via the PORT environment variable
ENV PORT=8080
EXPOSE 8080

# Configure JVM for container environments (cgroups v2, memory limits)
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT} -jar app.jar"]
