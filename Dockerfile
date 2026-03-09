FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

RUN ls -la

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar target/*.jar"]

#CMD ["java", "-jar", 
