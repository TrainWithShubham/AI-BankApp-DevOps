# # get the base image for Java 
# FROM eclipse-temurin:21-jdk-jammy

# #code in a working directory 
# WORKDIR /app

# #copy all the src code to the container
# COPY . .

# # install the libraries with maven 
# RUN chmod +x mvnw && ./mvnw clean package -DskipTests -B

# #Expose the port that the application will run on
# EXPOSE 8080

# # run the application
# # CMD ["java", "-jar", "target/*.jar"]
# ENTRYPOINT ["sh", "-c", "java -jar target/*.jar"]


# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY pom.xml ./
COPY .mvn ./.mvn
COPY mvnw ./
RUN dos2unix mvnw || sed -i 's/\r$//' mvnw
RUN chmod +x mvnw && ./mvnw dependency:resolve
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd -r appuser && useradd -r -g appuser appuser
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 CMD java -cp app.jar org.springframework.boot.loader.JarLauncher || exit 1
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]