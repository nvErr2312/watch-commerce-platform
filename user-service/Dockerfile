FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY ./commonservice/pom.xml ./commonservice/
RUN mvn -f ./commonservice/pom.xml dependency:go-offline -B

COPY ./commonservice/src ./commonservice/src
RUN mvn -f ./commonservice/pom.xml clean install -DskipTests

COPY ./user-service/pom.xml ./user-service/
RUN mvn -f ./user-service/pom.xml dependency:go-offline -B

COPY ./user-service/src ./user-service/src
RUN mvn -f ./user-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Lấy thành phẩm từ bước build chuyển sang
COPY --from=build /app/user-service/target/*.jar user-service.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "user-service.jar"]