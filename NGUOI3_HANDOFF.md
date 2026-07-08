# Bàn giao phiên làm việc — Người 3 (Admin, Product, Inventory Service)

> File này ghi lại toàn bộ trạng thái công việc để một phiên Claude Code mới có thể tiếp tục ngay, không cần dò lại từ đầu. Cập nhật lần cuối: xem `git log -1` trên nhánh `feature/nguoi3-axon-setup`.

## 1. Bối cảnh dự án

- Đồ án nhóm 4 người, watch e-commerce microservices (Spring Boot + Axon Framework + Eureka + API Gateway).
- Repo: `D:\Microservice project\watch-commerce-platform`, remote `origin` = `github.com/nvErr2312/watch-commerce-platform`.
- **Bạn (user) là Người 3**, phụ trách: **Admin Service, Inventory Service (phần Query), Product Service**.
- Nhánh làm việc: **`feature/nguoi3-axon-setup`** — luôn commit + push vào đây, **không bao giờ push vào `main`**.
- PRD/Architecture doc gốc (từ BMAD workflow): `_bmad-output/planning-artifacts/` trong project `BMAD-METHOD-main` (khác thư mục repo này) — `architecture.md`, `prd.md`.

## 2. Phân công team (đã thay đổi so với dự kiến ban đầu)

| Người | Phụ trách | Trạng thái |
|---|---|---|
| Người 1 | `discoveryserver` (Eureka), `apigateway` | Đã xong Eureka + Gateway cơ bản (route cho employeeservice/identity/user), **CHƯA có route cho `/api/products`, `/api/admin`, `/api/inventory`** |
| Người 2 | `identity-service`, `user-service`, notification | Auth khá đầy đủ (register/login/Google OAuth/refresh/logout). Notification Service thật vẫn chưa có, chỉ có 1 command trong commonservice |
| **Người 3 (bạn)** | `admin-service`, `product-service`, `inventory-service` (Query side) | Xem mục 3 |
| Người 4 | `order-service`, `payment-service`, `shipping-service`, **Command side của `inventory-service`** | Đã code xong Order Saga + Payment + Shipping + Inventory Command side (PostgreSQL + Flyway, không dùng Event Sourcing) |
| Ai đó | `frontend` (Angular 21 standalone) | Có Login/Register/Home hoàn chỉnh, Admin Product Management (do phiên này build) |

## 3. Trạng thái công việc của Người 3 — CHI TIẾT

### Product Service (`product-service/`) — Đầy đủ, đã test qua Postman thật
- **Command side**: `ProductAggregate` (Axon Event Sourcing) — Create/Update/ChangePrice/Delete.
- **Query side**: `ProductProjection` ghi vào `product_view` (H2 in-memory), trả `ProductResponse`/`ProductListResult` (KHÔNG dùng `ResponseTypes.multipleInstancesOf()` — xem mục 5, bug đã fix).
- Port: **8083**.

### Inventory Service (`inventory-service/`) — ĐÃ VIẾT LẠI HOÀN TOÀN giữa chừng
- **Command side thuộc về Người 4** (không phải bạn nữa): dùng PostgreSQL + Flyway (`InventoryItem`, `InventoryReservation`, `InventoryCommandHandler` — plain `@CommandHandler`, KHÔNG Event Sourcing, chỉ publish event thường qua `EventGateway`).
- **Query side thuộc về bạn**: `InventoryQueryHandler` + `InventoryQueryController`, đọc trực tiếp từ `InventoryItemRepository` (bảng Postgres của Người 4, không có projection/event replay riêng).
- **⚠️ productId ở đây là kiểu `Long`** (mock data 10, 11, 12 — seed bằng Flyway `V2__seed_mock_inventory.sql`), **HOÀN TOÀN KHÁC** với `productId` kiểu `String/UUID` của Product Service. Đây là gap kiến trúc CHƯA GIẢI QUYẾT — 2 catalog không liên kết được. Xem mục 6.
- Config: `application.properties` (không phải `.yml`) — port **8184** (KHÔNG phải 8084 như trước), Postgres tại `localhost:5433/watch_inventory_db`.
- Tính năng "Admin tự sửa tồn kho tay" (FR18) **không còn tồn tại** — Command side mới của Người 4 chỉ có reserve/release, không có endpoint chỉnh tay.

### Admin Service (`admin-service/`) — Đầy đủ
- Không có DB riêng (đúng FR35), gọi Product/Inventory qua Axon `QueryGateway`/`CommandGateway`, có Resilience4j circuit breaker.
- `AdminDashboardController` trả **2 danh sách riêng** (Product summaries + Inventory items) — KHÔNG join vì productId 2 bên không khớp (xem mục 6).
- `AdminInventoryController` chỉ còn GET (list/by-id) — đã bỏ endpoint "adjust" vì Command side không còn hỗ trợ.

### Frontend (`frontend/`) — Mới thêm 1 trang
- `frontend/src/app/features/admin/products/product-management.page.{ts,html,scss}` — trang Quản lý Sản phẩm, route `/admin/products`, style theo mockup Stitch "Obsidian & Silver" (glassmorphism, dark/gold).
- Gọi `frontend/src/app/core/api/products/products-api.service.ts` → `/api/admin/products` (qua Gateway `localhost:8080`, cấu hình proxy tại `frontend/proxy.conf.json`).
- **Không load được dữ liệu thật cho tới khi Người 1 thêm route Gateway** (xem mục 4).

### `commonservice/bmad/nguoi3/` — package riêng của bạn
Chỉ chứa Command/Event/Query/DTO còn hợp lệ (đã dọn hết đồ cũ của Inventory kiểu Event Sourcing):
```
command/product/    (Create/Update/ChangePrice/Delete ProductCommand)
event/product/      (ProductCreated/Updated/PriceChanged/DeletedEvent)
query/product/      (FindProductByIdQuery, FindAllProductsQuery, FindAllProductSummariesQuery, SearchProductQuery)
dto/product/        (ProductSummaryDto, ProductSummaryListResult)
query/inventory/    (FindInventoryItemByProductIdQuery, FindAllInventoryItemsQuery — Long productId!)
dto/inventory/      (InventoryItemDto, InventoryItemListResult — Long productId!)
```
**Nguyên tắc bất di bất dịch**: không sửa code có sẵn của người khác trong `commonservice`, chỉ thêm mới trong package `bmad/nguoi3`.

## 4. Việc CẦN LÀM tiếp (theo độ ưu tiên)

1. **Xin Người 1 thêm route Gateway** cho `/api/products/**`, `/api/admin/**`, `/api/inventory/**` trong `apigateway/src/main/resources/application.yml` — đang chặn cả test Postman lẫn trang frontend mới build.
2. **Thêm `product-service`, `admin-service` vào `<modules>` của `pom.xml` gốc** (hiện chỉ có `inventory-service` trong danh sách module của Người 4, thiếu 2 service kia).
3. **Bàn với team về gap productId (String UUID vs Long)** giữa Product Service và Inventory Service — không thể ghép dashboard cho tới khi thống nhất.
4. **Thêm `SecurityConfig` thật** (dùng `JwtAuthenticationFilter` có sẵn trong `commonservice/security`) cho cả 3 service — hiện đang tạm `permitAll()` (xem `config/SecurityConfig.java` mỗi service, có comment TODO rõ ràng).
5. Báo người phụ trách `commonservice` xóa `CommonserviceApplication.java` + `application.properties` + `CommonserviceApplicationTests.java` thừa (gây bug đăng ký sai tên service `COMMONSERVICE` lên Eureka nếu không có `--spring.application.name=...` khi chạy — vẫn chưa ai fix).
6. Chốt route Gateway xong thì test lại toàn bộ luồng qua Postman + trang frontend mới.

## 5. Các BUG THẬT đã tìm và fix trong phiên này (đáng nhớ để tránh lặp lại)

1. **`spring.application.name` sai** — `commonservice` có `application.properties` thừa đè lên `application.yml` của service dùng nó → tất cả đăng ký Eureka nhầm tên `COMMONSERVICE`. Workaround: luôn chạy `mvn spring-boot:run -Dspring-boot.run.arguments="--spring.application.name=<tên-đúng>"`.
2. **Axon Server thiếu `AXONIQ_AXONSERVER_STANDALONE=true`** → lỗi `AXONIQ-1302: default not found in any replication group`. Đã fix trong `docker-compose.yml`.
3. **`spring-boot-starter-security` khóa hết mọi endpoint (401)** khi `commonservice` thêm dependency này cho JWT — vì `scanBasePackages="com.fullstack"` quét luôn `SecurityAutoConfiguration`. Đã vá tạm bằng `SecurityConfig` permitAll ở mỗi service.
4. **Exception nghiệp vụ trả 500 thay vì 400** — Axon bọc exception qua `ExecutionException` → `CommandExecutionException`, code cũ không unwrap. Fix: class `common/AxonExceptions.java` (lặp lại ở cả 3 service) unwrap về `IllegalArgumentException` trước khi ném ra ngoài.
5. **`ResponseTypes.multipleInstancesOf(...)` LUÔN lỗi** convert list response qua Axon distributed query bus ("Retrieved response [ArrayList] is not convertible..."). Fix: bọc list vào 1 wrapper object (`ProductListResult`, `InventoryItemListResult`...) và dùng `ResponseTypes.instanceOf(...)` thay vì `multipleInstancesOf(...)`. **Luôn áp dụng pattern này cho mọi query trả về List trong tương lai.**
6. **`@Valid` bị thiếu** trên mọi `@RequestBody` — annotation validate (`@NotBlank`, `@PositiveOrZero`...) tồn tại trên DTO nhưng không bao giờ chạy vì thiếu `@Valid` ở tham số controller. Đã fix toàn bộ.
7. **`InventoryProjection` không idempotent** — replay event từ Axon Server (không bao giờ mất dữ liệu) vào H2 (mất khi restart) gặp `InventoryCreatedEvent` trùng `productId` → vi phạm unique constraint → **toàn bộ TrackingEventProcessor bị treo vĩnh viễn**, event mới không bao giờ tới bảng đọc. (Bug này giờ hết liên quan vì Inventory Command side đã đổi sang PostgreSQL/Flyway của Người 4, nhưng **nguyên tắc "projection phải idempotent" vẫn áp dụng cho Product Service** nếu gặp tình huống tương tự.)

## 6. Gap kiến trúc CHƯA GIẢI QUYẾT (quan trọng nhất, cần bàn với team)

**`productId` không thống nhất giữa Product Service (String/UUID) và Inventory Service (Long, mock 10/11/12).** Đây không phải bug code — là quyết định kiến trúc cần cả team chốt: hoặc đổi Inventory sang String UUID khớp Product thật, hoặc Product Service cần một trường "SKU number" riêng để khớp Inventory. Cho tới khi chốt, Admin Dashboard không thể hiển thị đúng tồn kho theo từng sản phẩm thật.

## 7. Hạ tầng chạy local

```bash
cd "D:\Microservice project\watch-commerce-platform"
docker compose up -d          # Axon Server, MySQL, Postgres, Redis, Mailpit
```
Sau đó mở từng terminal (Eureka → rồi mới tới các service khác):
```bash
cd discoveryserver && mvn spring-boot:run
cd product-service && mvn spring-boot:run -Dspring-boot.run.arguments="--spring.application.name=product-service"
cd inventory-service && mvn spring-boot:run   # đã dùng application.properties đúng, không cần override tên
cd admin-service && mvn spring-boot:run -Dspring-boot.run.arguments="--spring.application.name=admin-service"
cd frontend && npm start      # http://localhost:4200, proxy /api -> localhost:8080 (Gateway)
```
Lưu ý: nếu `docker info` báo lỗi kết nối, mở Docker Desktop bằng tay trước (`C:\Program Files\Docker\Docker\Docker Desktop.exe`).

## 8. Quy ước làm việc đã thống nhất trong phiên

- Sau mỗi việc hoàn thành: tự động `git commit` + `git push` vào `feature/nguoi3-axon-setup`, không bao giờ vào `main`.
- Không sửa code có sẵn của người khác trong `commonservice`, chỉ thêm mới trong `bmad/nguoi3`.
- Luôn test thật (curl/Postman/build) trước khi báo "xong", không suy đoán.
