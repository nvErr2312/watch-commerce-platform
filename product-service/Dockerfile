FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY ./commonservice/pom.xml ./commonservice/
RUN mvn -f ./commonservice/pom.xml dependency:go-offline -B

COPY ./commonservice/src ./commonservice/src
RUN mvn -f ./commonservice/pom.xml clean install -DskipTests

COPY ./product-service/pom.xml ./product-service/
RUN mvn -f ./product-service/pom.xml dependency:go-offline -B

COPY ./product-service/src ./product-service/src
RUN mvn -f ./product-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Lấy thành phẩm từ bước build chuyển sang
COPY --from=build /app/product-service/target/*.jar product-service.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "product-service.jar"]