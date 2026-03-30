# ---------- Stage 1: Build ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests


# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jdk-alpine

# Create non-root user
RUN addgroup -S springgroup && adduser -S springuser -G springgroup

WORKDIR /app

# Copy jar from builder stage
COPY --chown=springuser:springgroup --from=builder /build/target/*.jar app.jar



# Switch to non-root user
USER springuser

EXPOSE 8083

ENTRYPOINT ["java","-jar","app.jar"]
