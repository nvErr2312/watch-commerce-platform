FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY ./commonservice/pom.xml ./commonservice/
RUN mvn -f ./commonservice/pom.xml dependency:go-offline -B

COPY ./commonservice/src ./commonservice/src
RUN mvn -f ./commonservice/pom.xml clean install -DskipTests

COPY ./admin-service/pom.xml ./admin-service/
RUN mvn -f ./admin-service/pom.xml dependency:go-offline -B

COPY ./admin-service/src ./admin-service/src
RUN mvn -f ./admin-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/admin-service/target/*.jar admin-service.jar

EXPOSE 8090
ENTRYPOINT ["java", "-jar", "admin-service.jar"]