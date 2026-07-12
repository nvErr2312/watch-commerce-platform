FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY ./commonservice/pom.xml ./commonservice/
RUN mvn -f ./commonservice/pom.xml dependency:go-offline -B

COPY ./commonservice/src ./commonservice/src
RUN mvn -f ./commonservice/pom.xml clean install -DskipTests

COPY ./order-service/pom.xml ./order-service/
RUN mvn -f ./order-service/pom.xml dependency:go-offline -B

COPY ./order-service/src ./order-service/src
RUN mvn -f ./order-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/data
VOLUME ["/app/data"]

COPY --from=build /app/order-service/target/*.jar order-service.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "order-service.jar"]