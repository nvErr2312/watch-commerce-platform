FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# =========================
# Build commonservice
# =========================

COPY ./commonservice/pom.xml ./commonservice/

RUN mvn -f ./commonservice/pom.xml dependency:go-offline -B

COPY ./commonservice/src ./commonservice/src

RUN mvn -f ./commonservice/pom.xml clean install -DskipTests -B


# =========================
# Build identity-service
# =========================

COPY ./identity-service/pom.xml ./identity-service/

RUN mvn -f ./identity-service/pom.xml dependency:go-offline -B

COPY ./identity-service/src ./identity-service/src

RUN mvn -f ./identity-service/pom.xml clean package -DskipTests -B


# =========================
# Runtime
# =========================

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Lấy file JAR từ bước build
COPY --from=build /app/identity-service/target/*.jar identity-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "identity-service.jar"]