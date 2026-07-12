FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY ./commonservice/pom.xml ./commonservice/
RUN mvn -f ./commonservice/pom.xml dependency:go-offline -B

COPY ./commonservice/src ./commonservice/src
RUN mvn -f ./commonservice/pom.xml clean install -DskipTests

COPY ./payment-service/pom.xml ./payment-service/
RUN mvn -f ./payment-service/pom.xml dependency:go-offline -B

COPY ./payment-service/src ./payment-service/src
RUN mvn -f ./payment-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/data
VOLUME ["/app/data"]

COPY --from=build /app/payment-service/target/*.jar payment-service.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "payment-service.jar"]