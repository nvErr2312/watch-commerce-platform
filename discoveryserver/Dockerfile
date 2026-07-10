# Stage 1: Build ứng dụng với Maven và Java 21 (Bản Alpine siêu nhẹ)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app/discoveryserver

# Tối ưu Cache: Copy file pom.xml từ thư mục con discoveryserver vào container
COPY discoveryserver/pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn từ thư mục con discoveryserver vào container
COPY discoveryserver/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Môi trường chạy JRE Java 21 siêu tinh gọn
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Lấy file jar thành phẩm từ bước build ở trên
# Vì WORKDIR ở Stage 1 là /app/discoveryserver nên target sẽ nằm ở đó
COPY --from=build /app/discoveryserver/target/*.jar discoveryserver.jar

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "discoveryserver.jar"]