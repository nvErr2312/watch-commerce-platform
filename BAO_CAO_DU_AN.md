# Báo cáo dự án Watch Commerce Platform

## 1. Giới thiệu

Watch Commerce Platform là hệ thống thương mại điện tử cho sản phẩm đồng hồ, được xây dựng theo kiến trúc microservices. Hệ thống tách các chức năng lớn thành nhiều service độc lập như xác thực, người dùng, đơn hàng, tồn kho, vận chuyển, thanh toán và thông báo.

Mục tiêu của dự án là tạo nên một nền tảng bán hàng có khả năng mở rộng, dễ bảo trì và phù hợp với các nghiệp vụ thương mại điện tử hiện đại.

## 2. Mục tiêu

- Xây dựng hệ thống bán đồng hồ trực tuyến với các nghiệp vụ cơ bản.
- Tách module theo microservices để từng service có thể phát triển và triển khai độc lập.
- Hỗ trợ xác thực người dùng bằng JWT và đăng nhập Google.
- Quản lý đơn hàng theo hướng CQRS/Event Sourcing với Axon Framework.
- Tích hợp thanh toán PayOS.
- Gửi thông báo email thông qua Kafka và notification service.
- Sử dụng API Gateway làm cổng truy cập tập trung cho frontend.

## 3. Công nghệ sử dụng

### Backend

- Java và Spring Boot.
- Spring Cloud Gateway cho API Gateway.
- Eureka Server cho service discovery.
- Spring Security và JWT cho bảo mật.
- Spring Data JPA và H2 Database cho lưu trữ dữ liệu.
- Axon Framework và Axon Server cho CQRS, Event Sourcing và Saga.
- Kafka cho giao tiếp bất đồng bộ.
- Redis cho rate limiting và hỗ trợ xác thực.

### Frontend

- Angular 21.
- TypeScript.
- RxJS.
- Angular Router và Angular Forms.

### Hạ tầng

- Docker Compose.
- Redis 7.2.
- Kafka 3.7.0.
- Axon Server 2024.0.4.
- Mailpit cho kiểm thử email nội bộ.

## 4. Kiến trúc tổng quan

Hệ thống gồm nhiều service Spring Boot, được quản lý trong Maven multi-module project:

- `commonservice`: chứa command, event, query, response, cấu hình bảo mật và các tiện ích dùng chung.
- `discoveryserver`: Eureka Server để các service đăng ký và tìm thấy nhau.
- `apigateway`: cổng API tập trung, điều hướng request tới các service nội bộ.
- `identity-service`: đăng ký, đăng nhập, xác thực email, JWT và Google login.
- `user-service`: quản lý thông tin người dùng.
- `order-service`: tạo và truy vấn đơn hàng, điều phối quy trình checkout.
- `inventory-service`: quản lý và cập nhật tồn kho.
- `payment-service`: xử lý thanh toán, webhook và kết nối PayOS.
- `shipping-service`: xử lý thông tin vận chuyển.
- `notificationservice`: nhận sự kiện và gửi email thông báo.
- `frontend`: giao diện Angular cho người dùng.

Luồng truy cập chính:

1. Người dùng thao tác trên frontend Angular.
2. Frontend gọi API qua API Gateway.
3. API Gateway route request tới service phù hợp.
4. Các service đăng ký với Eureka để được phát hiện tự động.
5. Các nghiệp vụ bất đồng bộ được xử lý qua Kafka hoặc Axon.
6. Dữ liệu của từng service được lưu riêng trong database H2 file.

## 5. Chức năng chính

### Xác thực và phân quyền

Identity service đảm nhận đăng ký, đăng nhập, tạo access token, refresh token, xác thực email và đăng nhập bằng Google. Hệ thống dùng JWT để bảo vệ các API cần quyền truy cập.

### Quản lý người dùng

User service quản lý thông tin người dùng theo mô hình command/query. Các thao tác thêm, sửa, xoá và truy vấn người dùng được tách rõ để phù hợp với CQRS.

### Quản lý đơn hàng

Order service xử lý tạo đơn hàng và truy vấn thông tin đơn hàng. Service này có Saga checkout để điều phối các bước liên quan đến tồn kho, thanh toán và vận chuyển.

### Quản lý tồn kho

Inventory service tiếp nhận command liên quan đến việc giữ hàng, cập nhật số lượng và phát ra event khi tồn kho thay đổi.

### Thanh toán

Payment service xử lý giao dịch thanh toán, trạng thái thanh toán, webhook, return URL và cancel URL từ PayOS. Service cũng có cấu hình thời gian hết hạn thanh toán.

### Vận chuyển

Shipping service quản lý thông tin giao hàng và trạng thái vận chuyển của đơn hàng.

### Thông báo

Notification service lắng nghe sự kiện Kafka, đặc biệt là sự kiện xác thực email, sau đó gửi email thông báo. Môi trường local dùng Mailpit để kiểm tra email.

## 6. Bảo mật

Hệ thống áp dụng các lớp bảo mật sau:

- JWT cho xác thực người dùng.
- Internal token cho giao tiếp nội bộ giữa các service.
- API Gateway làm điểm vào duy nhất cho nhiều endpoint frontend.
- Rate limiting trên Gateway bằng Redis với giới hạn 10 request/giây và burst capacity 20.
- Spring Security trong các service cần bảo vệ API.

Lưu ý: một số secret và API key đang có giá trị mặc định trong file cấu hình. Khi triển khai thật, cần đưa các giá trị này vào biến môi trường hoặc secret manager.

## 7. Phân công công việc

| Thành viên | Phần phụ trách | Công việc thực hiện |
| --- | --- | --- |
| `nguyenngu2005` | Backend nghiệp vụ đơn hàng, tồn kho, thanh toán, vận chuyển | Xây dựng `order-service`, `inventory-service`, `payment-service`, `shipping-service`; thiết kế command/event dùng chung trong `commonservice`; triển khai luồng checkout bằng Axon Saga; cấu hình database, migration và các API nghiệp vụ liên quan đến đơn hàng, giữ hàng, phí vận chuyển và thanh toán PayOS. |
| `Nhi Bui` / `NhiBui2312` | Frontend, xác thực, người dùng và thông báo | Xây dựng giao diện Angular cho đăng nhập, đăng ký, trang chủ, hồ sơ người dùng và checkout; triển khai `identity-service` cho đăng ký, đăng nhập, JWT, Google login và xác thực email; phát triển `user-service`; cấu hình bảo mật dùng chung; xây dựng `notificationservice` để gửi email thông báo qua Kafka. |
| Giảng | Hạ tầng hệ thống | Cấu hình API Gateway, Eureka Discovery Server, Docker Compose, Redis, Kafka, Axon Server và Mailpit; chuẩn bị môi trường chạy local cho các service; hỗ trợ kết nối giữa frontend, gateway và các backend service. |
| Cả nhóm | Tích hợp và hoàn thiện | Kiểm thử luồng từ frontend qua gateway tới các service; rà soát lỗi tích hợp; thống nhất tài liệu và hoàn thiện báo cáo. |

## 8. Cài đặt và chạy dự án

### Chạy hạ tầng

```powershell
docker compose up -d
```

Lệnh này khởi động Redis, Mailpit, Kafka, Axon Server và Discovery Server.

### Build backend

```powershell
.\mvnw.cmd clean install
```

Nếu không có Maven Wrapper ở thư mục gốc, có thể dùng Maven đã cài sẵn:

```powershell
mvn clean install
```

### Chạy frontend

```powershell
cd frontend
npm install
npm start
```

Frontend mặc định chạy qua Angular dev server và dùng proxy config để gọi backend.

## 9. Đánh giá

### Ưu điểm

- Kiến trúc microservices rõ ràng, mỗi service có trách nhiệm riêng.
- Có API Gateway và Discovery Server, phù hợp với hệ thống phân tán.
- Áp dụng CQRS/Event Sourcing cho các nghiệp vụ như order, user, inventory, payment và shipping.
- Có Kafka cho xử lý bất đồng bộ và notification.
- Có Docker Compose giúp khởi động nhanh các thành phần hạ tầng.
- Frontend và backend tách riêng, dễ phát triển độc lập.

### Hạn chế

- Một số cấu hình secret đang nằm trong file properties, chưa phù hợp môi trường production.
- Database H2 phù hợp local/dev, chưa phù hợp cho triển khai thực tế.
- Docker Compose hiện mới build Discovery Server, các service còn lại cần bổ sung nếu muốn chạy toàn bộ bằng Docker.
- README hiện còn ngắn, cần bổ sung hướng dẫn chi tiết hơn cho người mới clone dự án.

## 10. Hướng phát triển

- Chuyển các secret sang biến môi trường hoặc secret manager.
- Bổ sung Dockerfile và docker-compose service cho toàn bộ backend.
- Chuyển database sang PostgreSQL/MySQL khi triển khai thật.
- Bổ sung test tự động cho các service quan trọng.
- Hoàn thiện tài liệu API bằng OpenAPI/Swagger.
- Thêm logging tập trung và monitoring cho microservices.
- Hoàn thiện giao diện quản trị đơn hàng, sản phẩm, người dùng và thanh toán.

## 11. Kết luận

Watch Commerce Platform là một dự án thương mại điện tử có cấu trúc khá đầy đủ, kết hợp Spring Boot microservices, Angular frontend và các công nghệ hạ tầng như Eureka, Gateway, Kafka, Redis và Axon Server. Dự án phù hợp để học tập và phát triển các kỹ năng về hệ thống phân tán, CQRS/Event Sourcing, bảo mật API và tích hợp thanh toán.

Với việc hoàn thiện thêm tài liệu, bảo mật secret, docker hoá đầy đủ và bổ sung test, hệ thống có thể tiếp tục phát triển thành nền tảng thương mại điện tử hoàn chỉnh hơn.
