# Bàn giao phiên làm việc — Người 3 (Admin, Product, Inventory Service)

> File này ghi lại toàn bộ trạng thái công việc để một phiên Claude Code mới có thể tiếp tục ngay, không cần dò lại từ đầu. Cập nhật lần cuối: sau commit `c482d0f` trên nhánh `feature/nguoi3-axon-setup` (xem `git log -1` để xác nhận).

## 1. Bối cảnh dự án

- Đồ án nhóm 4 người, watch e-commerce microservices (Spring Boot + Axon Framework + Eureka + API Gateway).
- Repo: `D:\Microservice project\watch-commerce-platform`, remote `origin` = `github.com/nvErr2312/watch-commerce-platform`.
- **Bạn (user) là Người 3**, phụ trách: **Admin Service, Inventory Service (phần Query), Product Service**.
- Nhánh làm việc: **`feature/nguoi3-axon-setup`** — luôn commit + push vào đây, **không bao giờ push vào `main`**.
- Máy chạy Windows, dùng Git Bash. JDK 21 đã cài tại `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`, `JAVA_HOME` đã set persistent qua `setx` — mở terminal mới sẽ tự có.
- PRD/Architecture doc gốc (từ BMAD workflow): `_bmad-output/planning-artifacts/` trong project `BMAD-METHOD-main` (khác thư mục repo này) — `architecture.md`, `prd.md`.

## 2. Phân công team (đã thay đổi so với dự kiến ban đầu)

| Người | Phụ trách | Trạng thái |
|---|---|---|
| Người 1 | `discoveryserver` (Eureka), `apigateway` | Eureka dockerize xong (tự chạy qua `docker compose up -d`, không cần chạy tay). Gateway: **route cho `/api/products`, `/api/admin/**`, `/api/inventory/**` đã có** (do Người 3 tự thêm, được Người 1 ủy quyền — xem mục 5). |
| Người 2 | `identity-service`, `user-service`, notification | Auth đầy đủ (register/login/Google OAuth/refresh/logout), email xác minh qua Kafka + Mailpit. **⚠️ Có Gmail App Password thật bị commit plaintext trong `commonservice/src/main/resources/application.yml` — CHƯA thu hồi, xem mục 6.** |
| **Người 3 (bạn)** | `admin-service`, `product-service`, `inventory-service` (Query side) | Xem mục 3 — phần lớn đã xong trong phiên này |
| Người 4 | `order-service`, `payment-service`, `shipping-service`, Command side của `inventory-service` | Order Saga cơ bản đã merge vào `main`. Có nhánh riêng **`feature/order-payment-shipping-security` (CHƯA merge)** chứa: `SecurityConfig` thật (JWT) cho cả 3 service, tích hợp PayOS (payment gateway thật) thay Mock Payment, trang Checkout frontend hoàn chỉnh — đáng tham khảo khi làm SecurityConfig thật cho Product/Inventory/Admin. |
| Ai đó | `frontend` (Angular 21 standalone) | Login/Register/Home, Admin Product Management, **Danh Sách Sản Phẩm + Chi Tiết Sản Phẩm (mới, xem mục 3)** |

## 3. Trạng thái công việc của Người 3 — CHI TIẾT (đã cập nhật)

### Product Service (`product-service/`) — Đầy đủ, đã test qua Gateway thật
- **Command side**: `ProductAggregate` (Axon Event Sourcing) — Create/Update/ChangePrice/Delete.
- **Query side**: `ProductProjection` ghi vào `product_view` (H2 in-memory), trả `ProductResponse`/`ProductListResult`.
- Port: **8083**.
- **9 sản phẩm demo thật** đã seed qua API (Rolex, Omega, Patek Philippe, Hublot, Tag Heuer, Seiko x2, Casio x2), giá **VND thực tế** (vd Rolex Submariner 310,000,000₫), ảnh thật từ Unsplash. Dữ liệu này sống trong Axon Server event store (persistent qua Docker volume) — mất H2 khi restart sẽ **tự động replay lại đủ**, không cần seed lại tay.
- `application.yml` đã thêm: `spring.kafka.*`, `spring.mail.*` (trỏ Mailpit), `app.security.jwt-secret`, và **`jdbc:h2:mem:productDB;DB_CLOSE_DELAY=-1`** (fix bug H2 tự xóa dữ liệu, xem mục 5).

### Inventory Service (`inventory-service/`) — productId đã chuyển sang UUID
- **Command side thuộc về Người 4**: PostgreSQL + Flyway (`InventoryItem`, `InventoryReservation`, `InventoryCommandHandler` — plain `@CommandHandler`, không Event Sourcing).
- **Query side thuộc về bạn**: `InventoryQueryHandler` + `InventoryQueryController`, đọc trực tiếp từ `InventoryItemRepository`.
- **✅ Gap `productId` (String/UUID vs Long) ĐÃ GIẢI QUYẾT** — xem mục 5, migration `V4__product_id_uuid.sql`. `productId` giờ là `String` UUID, khớp 1:1 với Product Service thật. 9 dòng tồn kho đã seed lại khớp UUID thật của 9 sản phẩm demo.
- Config: `application.properties`, port **8184**, Postgres tại `localhost:5433/watch_inventory_db`.
- Đã thêm: `spring.kafka.*`, `spring.mail.*`, `app.security.jwt-secret`, **`eureka.instance.prefer-ip-address=true`** (fix bug Gateway không gọi được, xem mục 5).
- Tính năng "Admin tự sửa tồn kho tay" (FR18) vẫn không tồn tại — Command side Người 4 chỉ có reserve/release.

### Admin Service (`admin-service/`) — Đầy đủ, dashboard giờ join được
- Không có DB riêng, gọi Product/Inventory qua Axon `QueryGateway`, có Resilience4j circuit breaker.
- `AdminDashboardController` trả 2 danh sách (Product + Inventory) — **giờ `productId` 2 bên đã khớp nhau thật sự** (join client-side khả thi, chưa implement join server-side vì ngoài phạm vi yêu cầu ban đầu).
- `AdminInventoryController` chỉ còn GET (list/by-id).
- Đã thêm: `app.security.jwt-secret` (fix bug mục 5).

### Frontend (`frontend/`) — 3 trang mới/đã cập nhật
- `features/admin/products/product-management.page.*` — trang Quản lý Sản phẩm (Admin), route `/admin/products`.
- `features/products/product-list.page.*` — trang **Danh Sách Sản Phẩm** (storefront công khai), route `/products`. Sidebar lọc thương hiệu/giá (VND), sắp xếp, phân trang, card có category chip + hover "xem chi tiết", skeleton loading.
- `features/products/product-detail.page.*` — trang **Chi Tiết Sản Phẩm** (mới), route `/products/:id`. Ảnh lớn, giá, mô tả, mã sản phẩm, **badge tồn kho thật** lấy từ Inventory Service (Còn X sản phẩm / Hết hàng), nút thêm giỏ hàng (chỉ UI, chưa nối logic giỏ hàng thật).
- API services: `core/api/products/product-catalog-api.service.ts` (public, gọi thẳng `/api/products`), `core/api/inventory/inventory-catalog-api.service.ts` (public, gọi thẳng `/api/inventory/{id}`) — khác với `products-api.service.ts` (proxy admin-only, gọi `/api/admin/products`).
- Home page: liên kết "Sản phẩm" + nút "Xem sản phẩm" trỏ `routerLink="/products"`.
- `proxy.conf.json` trỏ `localhost:8080` (Gateway) — **đừng commit bản trỏ port lẻ nếu có tự sửa tạm để test**.

### `commonservice/bmad/nguoi3/` — package riêng của bạn
```
command/product/    (Create/Update/ChangePrice/Delete ProductCommand)
event/product/      (ProductCreated/Updated/PriceChanged/DeletedEvent)
query/product/      (FindProductByIdQuery, FindAllProductsQuery, FindAllProductSummariesQuery, SearchProductQuery)
dto/product/        (ProductSummaryDto, ProductSummaryListResult)
query/inventory/    (FindInventoryItemByProductIdQuery — String productId; FindAllInventoryItemsQuery — có 1 field marker, xem mục 5)
dto/inventory/      (InventoryItemDto — String productId; InventoryItemListResult)
```
**Nguyên tắc bất di bất dịch**: không sửa code có sẵn của người khác trong `commonservice`, chỉ thêm mới trong package `bmad/nguoi3`. **Ngoại lệ đã xảy ra trong phiên này**: sửa `commonservice/order/OrderItemPayload.java` (Long→String productId) — đụng code Người 4 (Order Saga), nhưng đã **xin phép và được đồng ý rõ ràng** trước khi làm (quyết định "chốt UUID" của user). Nhắc Người 4 biết việc này nếu họ chưa rõ.

## 4. Việc CẦN LÀM tiếp (theo độ ưu tiên)

1. **`SecurityConfig` thật (JWT)** cho `product-service`/`inventory-service`/`admin-service` — hiện vẫn `permitAll()`. Tham khảo cách Người 4 làm ở nhánh `feature/order-payment-shipping-security` (`order-service`/`payment-service`/`shipping-service` đều đã có `SecurityConfig` dùng `JwtAuthenticationFilter` thật).
2. **Báo Người 2 thu hồi Gmail App Password** đã lộ trong `commonservice/src/main/resources/application.yml` (xem mục 6) — việc này không thuộc phạm vi Người 3, chỉ nhắc.
3. **Bug nhỏ còn treo**: token JWT hết hạn/hỏng vẫn trả 500 thay vì 401 (do `JwtAuthenticationFilter` không try/catch quanh `validate()` — đã fix phần null-secret nhưng chưa fix phần bắt exception tổng quát). Không chặn công việc hiện tại nhưng nên fix cho chuẩn REST.
4. Báo người phụ trách `commonservice` xóa `CommonserviceApplication.java` + `application.properties` + `CommonserviceApplicationTests.java` thừa (vẫn còn, gây bug `COMMONSERVICE` nếu ai chạy thiếu `--spring.application.name=...`).
5. Cân nhắc thêm "giỏ hàng" thật cho nút "Thêm vào giỏ" ở trang Chi Tiết Sản Phẩm (hiện chỉ là UI, chưa nối logic — tuỳ phạm vi PRD có yêu cầu hay không).
6. Team chưa có bảng phân bổ port thống nhất — từng xảy ra trùng port `order-service`/`product-service` (8083), workaround bằng `ORDER_SERVICE_PORT=8087`.

## 5. Các BUG THẬT đã tìm và fix (đáng nhớ để tránh lặp lại)

1. **`spring.application.name` sai** — `commonservice` có `application.properties` thừa đè lên `.yml` của service dùng nó → đăng ký Eureka nhầm tên `COMMONSERVICE`. Workaround: luôn chạy `mvn spring-boot:run -Dspring-boot.run.arguments="--spring.application.name=<tên-đúng>"` cho service chỉ dùng `.yml` (không cần cho service có sẵn `.properties` riêng như `identity-service`, `inventory-service`).
2. **Axon Server thiếu `AXONIQ_AXONSERVER_STANDALONE=true`** → lỗi `AXONIQ-1302`. Đã fix trong `docker-compose.yml`.
3. **`ResponseTypes.multipleInstancesOf(...)` LUÔN lỗi** convert list response qua Axon distributed query bus. Fix: bọc list vào 1 wrapper object (`ProductListResult`, `InventoryItemListResult`) và dùng `ResponseTypes.instanceOf(...)`. **Luôn áp dụng pattern này cho mọi query trả List trong tương lai.**
4. **MySQL80 (native Windows service) chiếm port 3306** khiến Docker `watch-mysql` không publish được ra host. Fix: `net stop MySQL80` (PowerShell Admin) rồi `docker compose up -d --force-recreate mysql`. **Nếu máy khởi động lại, MySQL80 có thể tự chạy lại chiếm port** — nên đặt Startup type = Manual trong `services.msc`.
5. **`commonservice`'s `KafkaConfig`/`EmailService` không có `@Value` default** — mọi service không tự khai báo `spring.kafka.*`/`spring.mail.*` sẽ crash lúc khởi động (bean creation fail). Đã thêm property fallback vào `product-service`/`inventory-service`/`admin-service` (không sửa `commonservice`).
6. **Port trùng `order-service` vs `product-service`** (cả hai default 8083). Workaround: chạy `order-service` với `$env:ORDER_SERVICE_PORT="8087"`.
7. **Postgres + JDK21 xung đột timezone** (`invalid value for parameter "TimeZone": "Asia/Saigon"`). Fix: chạy service dùng Postgres (inventory/order/payment/shipping) với `-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC`.
8. **`FindAllInventoryItemsQuery` là class rỗng (0 field)** → Jackson mặc định (`FAIL_ON_EMPTY_BEANS`) ném lỗi khi Axon serialize ("Unable to serialize object"). Fix: thêm 1 field `marker` (xem file, có giải thích trong Javadoc).
9. **`product-service`'s H2 thiếu `DB_CLOSE_DELAY=-1`** — khi HikariCP giải phóng hết connection cùng lúc (maxLifetime hết hạn đồng loạt sau chạy lâu), H2 in-memory bị xóa sạch, service báo "Table PRODUCT_VIEW not found". Fix: `jdbc:h2:mem:productDB;DB_CLOSE_DELAY=-1`.
10. **`inventory-service` thiếu `eureka.instance.prefer-ip-address=true`** — đăng ký Eureka bằng hostname máy ảo (`ZuyBawnsh.mshome.net`) thay vì IP, khiến Gateway (`lb://inventory-service`) không gọi được (500 khi gọi qua Gateway dù gọi trực tiếp port vẫn OK).
11. **🔴 Bug nghiêm trọng nhất: thiếu `app.security.jwt-secret` ở `product-service`/`inventory-service`/`admin-service`** — `JwtAuthenticationFilter` (tự động nạp vào MỌI service qua `scanBasePackages="com.fullstack"`) gọi `Decoders.BASE64.decode(null)` khi có header `Authorization: Bearer ...` (tức là **MỌI user đã đăng nhập thật**, không chỉ token hỏng), ném `IllegalArgumentException` không được bắt → 500 thẳng, vượt qua cả `permitAll()`. Đã fix bằng cách thêm `app.security.jwt-secret=${JWT_SECRET:MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=}` (giống `identity-service`) vào cả 3 service. **Đây là lý do mọi trang cần đăng nhập từng "chạy được lúc chưa login nhưng lỗi 500 lúc login" — nhớ pattern này nếu gặp lại ở service khác.**
12. **Gap kiến trúc `productId` (String/UUID vs Long)** — ĐÃ GIẢI QUYẾT (xem mục 6 cũ, giờ chuyển thành lịch sử). Migration `inventory-service/src/main/resources/db/migration/V4__product_id_uuid.sql` đổi cột `product_id` sang `varchar(36)` và reseed bằng UUID thật của Product Service. Cascade sang: `commonservice/order/OrderItemPayload.java` (Long→String, ảnh hưởng Order Saga của Người 4 — đã xin phép), `order-service/dto/request/OrderItemRequest.java`, toàn bộ `inventory-service` Command+Query side, `admin-service` Query client.

## 6. Vấn đề bảo mật CHƯA XỬ LÝ (không thuộc phạm vi Người 3, chỉ nhắc)

**Gmail App Password thật bị commit plaintext** trong `commonservice/src/main/resources/application.yml` (commit `d7b05f5`, do Người 2 thêm):
```yaml
spring.mail.username: huynhvangiang0504@gmail.com
spring.mail.password: eljt iwyq uuri uoek
```
Đã nằm trong git history, đã push lên GitHub. Người 2 cần thu hồi App Password tại [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords) và chuyển sang biến môi trường. Việc xóa dòng ở commit sau KHÔNG đủ — vẫn còn trong lịch sử git trừ khi rewrite history.

## 7. Hạ tầng chạy local (đã cập nhật lệnh chạy)

```bash
cd "D:\Microservice project\watch-commerce-platform"
docker compose up -d          # Axon Server, MySQL, Postgres, Redis, Mailpit, Kafka, Eureka (Eureka tự chạy, KHÔNG cần chạy tay discoveryserver nữa)
```

Nếu MySQL báo lỗi port 3306 hoặc container `watch-mysql` không có `0.0.0.0:3306->3306` khi `docker ps`, xem bug #4 mục 5.

Mở từng terminal PowerShell **mới** (để nhận `JAVA_HOME` đã set):
```powershell
cd identity-service; mvn spring-boot:run
cd user-service; mvn spring-boot:run
cd notificationservice; mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.kafka.bootstrap-servers=localhost:9092"
cd product-service; mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.application.name=product-service"
cd inventory-service; mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC"
cd admin-service; mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.application.name=admin-service"
cd order-service; $env:ORDER_SERVICE_PORT="8087"; mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC"
cd payment-service; mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC"
cd shipping-service; mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC"
cd apigateway; mvn spring-boot:run          # chạy SAU khi các service trên đã đăng ký Eureka
cd frontend; npm start                       # http://localhost:4200, proxy /api -> localhost:8080
```

Lưu ý: nếu `docker info` báo lỗi kết nối, mở Docker Desktop bằng tay trước.

## 8. Quy ước làm việc đã thống nhất trong phiên

- Sau mỗi việc hoàn thành: `git commit` + `git push` vào `feature/nguoi3-axon-setup`, không bao giờ vào `main`.
- Không sửa code có sẵn của người khác trong `commonservice`/service khác **trừ khi đã hỏi và được user xác nhận rõ ràng** (như trường hợp `OrderItemPayload` ở mục 5.12).
- Luôn test thật (curl qua Gateway thật, hoặc preview browser thật với đăng nhập thật) trước khi báo "xong", không suy đoán.
- Khi sửa file người khác sở hữu (Gateway route, jwt-secret ảnh hưởng chung...), hỏi xác nhận trước qua câu hỏi rõ ràng, nêu rõ rủi ro.
- Khi cần test qua preview cần đăng nhập, có thể tạo tài khoản test qua API (`POST /api/v1/auth/register` → lấy link xác minh qua Mailpit API `GET localhost:8025/api/v1/messages` → `POST /api/v1/auth/verify-email?token=...` → `POST /api/v1/auth/login`) thay vì token giả (token giả từng gây nhầm lẫn với bug #11 thật).
