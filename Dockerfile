FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY . .



EXPOSE 8080


ENTRYPOINT ["sh", "-c", "java -jar target/*.jar"]