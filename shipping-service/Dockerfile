FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY ./commonservice/pom.xml ./commonservice/
RUN mvn -f ./commonservice/pom.xml dependency:go-offline -B

COPY ./commonservice/src ./commonservice/src
RUN mvn -f ./commonservice/pom.xml clean install -DskipTests

COPY ./shipping-service/pom.xml ./shipping-service/
RUN mvn -f ./shipping-service/pom.xml dependency:go-offline -B

COPY ./shipping-service/src ./shipping-service/src
RUN mvn -f ./shipping-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/data
VOLUME ["/app/data"]

COPY --from=build /app/shipping-service/target/*.jar shipping-service.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "shipping-service.jar"]