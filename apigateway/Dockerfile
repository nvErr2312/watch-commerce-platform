# Stage 1: Build ứng dụng với Maven và Java 21 (Bản Alpine siêu nhẹ)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app/apigateway

# Tối ưu Cache: Copy file pom.xml từ thư mục con apigateway vào container
COPY apigateway/pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn từ thư mục con apigateway vào container
COPY apigateway/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Môi trường chạy JRE Java 21 siêu tinh gọn
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Lấy file jar thành phẩm từ bước build ở trên
# Vì WORKDIR ở Stage 1 là /app/apigateway nên target sẽ nằm ở đó
COPY --from=build /app/apigateway/target/*.jar apigateway.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "apigateway.jar"]