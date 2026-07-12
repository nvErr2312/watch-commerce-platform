FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY ./commonservice/pom.xml ./commonservice/
RUN mvn -f ./commonservice/pom.xml dependency:go-offline -B

COPY ./commonservice/src ./commonservice/src
RUN mvn -f ./commonservice/pom.xml clean install -DskipTests

COPY ./inventory-service/pom.xml ./inventory-service/
RUN mvn -f ./inventory-service/pom.xml dependency:go-offline -B

COPY ./inventory-service/src ./inventory-service/src
RUN mvn -f ./inventory-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/inventory-service/target/*.jar inventory-service.jar

EXPOSE 8184
ENTRYPOINT ["java", "-jar", "inventory-service.jar"]