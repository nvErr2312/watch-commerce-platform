# Stage 1: Nhào nặn mã nguồn với Maven và Java 21 (Alpine)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# --- BƯỚC 1: CÀI ĐẶT COMMONSERVICE ---
# Copy file pom.xml trước để lưu cache các thư viện
COPY ./commonservice/pom.xml ./commonservice/
RUN mvn -f ./commonservice/pom.xml dependency:go-offline -B

# Copy mã nguồn và đưa commonservice vào kho chứa cục bộ (local m2)
COPY ./commonservice/src ./commonservice/src
RUN mvn -f ./commonservice/pom.xml clean install -DskipTests

# --- BƯỚC 2: ĐÓNG GÓI notificationservice ---
# Copy file pom.xml của notificationservice để lưu cache thư viện riêng của nó
COPY ./notificationservice/pom.xml ./notificationservice/
RUN mvn -f ./notificationservice/pom.xml dependency:go-offline -B

# Copy mã nguồn và đóng gói ra file .jar
COPY ./notificationservice/src ./notificationservice/src
RUN mvn -f ./notificationservice/pom.xml clean package -DskipTests

# Stage 2: Môi trường chạy siêu nhẹ (Chỉ dùng JRE 21 Alpine)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Lấy thành phẩm từ bước build chuyển sang
COPY --from=build /app/notificationservice/target/*.jar notificationservice.jar

EXPOSE 9004
ENTRYPOINT ["java", "-jar", "notificationservice.jar"]