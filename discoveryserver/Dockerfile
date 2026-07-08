# Stage 1: Build ứng dụng với Maven và Java 21 (Bản Alpine siêu nhẹ)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app/discoverserver

# Tối ưu Cache: Copy trực tiếp file pom.xml ngay tại thư mục hiện tại
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy trực tiếp thư mục src ngay tại thư mục hiện tại (Đã sửa lỗi gõ nhầm chữ y)
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Môi trường chạy JRE Java 21 siêu tinh gọn
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Lấy file jar thành phẩm từ bước build ở trên
COPY --from=build /app/discoverserver/target/*.jar discoverserver.jar

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "discoverserver.jar"]