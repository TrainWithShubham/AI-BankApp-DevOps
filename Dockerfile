FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw clean package -DskipTests -build

#Run stage -alpine has significantly fewer CVEs than ubuntu/jammy
FROM eclipse-eclipse-temurin:21-jre-alpine
WORKDIR /app

#Pull latest security patches for OS libraries
RUN apk update && apk upgrade --no-cache

# create a non-root user for security (alpine uses addgroup/adduser for groupadd/useradd)
RUN addgroup -S devsecops && adduser -S -G devsecops devsecops
USER devsecops

COPY --from=build /app/target/*.jar app.jar 

EXPOSE 80

ENTRYPOINT [ "Java","-jar","app.jar" ]
