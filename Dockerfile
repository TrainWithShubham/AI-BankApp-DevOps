# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Run — alpine has significantly fewer CVEs than ubuntu/jammy
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Pull latest security patches for OS libraries
RUN apk update && apk upgrade --no-cache

# Create a non-root user for security
RUN addgroup -S devsecops && adduser -S -G devsecops devsecops
USER devsecops

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
