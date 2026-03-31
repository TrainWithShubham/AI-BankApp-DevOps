# get the base image for Java 
FROM eclipse-temurin:21-jdk-jammy

#code in a working directory 
WORKDIR /app

#copy all the src code to the container
COPY . .

# install the libraries with maven 
RUN chmod +x mvnw && ./mvnw clean package -DskipTests -B

#Expose the port that the application will run on
EXPOSE 8080

# run the application
# CMD ["java", "-jar", "target/*.jar"]
ENTRYPOINT ["sh", "-c", "java -jar target/*.jar"]


