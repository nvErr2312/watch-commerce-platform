# Bàn giao phiên làm việc — Người 3 (Admin, Product, Inventory Service)

> File này ghi lại toàn bộ trạng thái công việc để một phiên Claude Code mới có thể tiếp tục ngay, không cần dò lại từ đầu. Cập nhật lần cuối: sau commit `3a0d5b4` trên nhánh `feature/nguoi3-axon-setup` (xem `git log -1` để xác nhận).

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
| Ai đó | `frontend` (Angular 21 standalone) | Login/Register/Home, **Admin Console hoàn chỉnh (mới, xem mục 3)**, Danh Sách Sản Phẩm + Chi Tiết Sản Phẩm |

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

### Frontend (`frontend/`) — 3 trang storefront + Admin Console hoàn chỉnh
- `features/admin/products/product-management.page.*` — trang Quản lý Sản phẩm (Admin), route `/admin/products`. Badge trạng thái giờ **join tồn kho thật** (Còn Hàng / Sắp Hết / Hết Hàng + số lượng) thay vì hardcode "Hoạt Động", category hiện dạng chip.
- `features/products/product-list.page.*` — trang **Danh Sách Sản Phẩm** (storefront công khai), route `/products`. Sidebar lọc thương hiệu/giá (VND), sắp xếp, phân trang, card có category chip + hover "xem chi tiết", skeleton loading.
- `features/products/product-detail.page.*` — trang **Chi Tiết Sản Phẩm** (mới), route `/products/:id`. Ảnh lớn, giá, mô tả, mã sản phẩm, **badge tồn kho thật** lấy từ Inventory Service (Còn X sản phẩm / Hết hàng), nút thêm giỏ hàng (chỉ UI, chưa nối logic giỏ hàng thật).
- API services: `core/api/products/product-catalog-api.service.ts` (public, gọi thẳng `/api/products`), `core/api/inventory/inventory-catalog-api.service.ts` (public, gọi thẳng `/api/inventory/{id}`) — khác với `products-api.service.ts` (proxy admin-only, gọi `/api/admin/products`).
- Home page: liên kết "Sản phẩm" + nút "Xem sản phẩm" trỏ `routerLink="/products"`. Menu tài khoản (avatar góc phải) giờ hiện thêm mục **"Admin Console"** nếu role JWT là `ADMIN` (ẩn với `USER` thường).
- `proxy.conf.json` trỏ `localhost:8080` (Gateway) — **đừng commit bản trỏ port lẻ nếu có tự sửa tạm để test**.

#### Admin Console — mới xây dựng trọn vẹn trong phiên này (dựa theo design mẫu Stitch "Obsidian & Silver" — glassmorphism, nền đen `#131315`, vàng `#e9c176`/`#c5a059`, font Inter)
- `features/admin/shell/admin-shell.component.*` — layout dùng chung cho toàn bộ `/admin/**`: sidebar 6 mục (Dashboard/Sản phẩm/Khách hàng/Vận chuyển/Doanh thu + Bảo mật/Đăng xuất), link **"← Về trang chủ"** dưới logo, active-state theo route, logout thật gọi `AuthStore.logout()`. Trước đây sidebar bị nhúng cứng lặp lại trong từng page — giờ đã tách ra, các page con chỉ còn `<router-outlet>` render vào.
- `features/admin/dashboard/dashboard.page.*`, route `/admin/dashboard` — 4 KPI card (Tổng sản phẩm, Tổng tồn kho, **Giá trị tồn kho** = giá×số lượng tính thật, Hết hàng), bar chart dựng đứng "Phân bổ theo thương hiệu", danh sách "Sắp hết hàng" (≤5 đơn vị) có thumbnail — toàn bộ join client-side từ `GET /api/admin/dashboard` (endpoint **đã tồn tại sẵn từ trước**, không cần thêm backend). Header có chip avatar admin thật (decode từ JWT).
- `features/admin/coming-soon/coming-soon.page.*` — stub dùng chung cho 4 route chưa có backend: `/admin/customers`, `/admin/logistics`, `/admin/revenue`, `/admin/security` (xem mục 4 vì sao chưa làm được).
- `core/guards/admin.guard.ts` — **`adminGuard`** thật, kiểm tra `AuthStore.isAdmin()` (decode claim `role` từ JWT, không gọi thêm API), redirect về `/home` nếu không phải Admin. Áp cùng `authGuard` trên route `/admin` trong `app.routes.ts`. TODO cũ về việc này đã xóa — **guard đã enforce role thật**, không còn "ai đăng nhập cũng vào được `/admin/**`".
- `core/auth/auth.store.ts` — thêm `role`/`email`/`isAdmin` computed signal, decode trực tiếp payload JWT (base64), không gọi thêm API.
- `core/api/admin/dashboard-api.service.ts`, `core/api/inventory/inventory-admin-api.service.ts` — API service mới, gọi `/api/admin/dashboard` và `/api/admin/inventory` (cả hai đã tồn tại sẵn ở backend, chỉ thiếu phía frontend).
- **Tài khoản Admin test đã tạo sẵn**: `admin.test@watchcommerce.local` / `AdminPass123!` (role `ADMIN` trong DB `watch_identity_db.accounts`). Cách promote role cho account khác — xem mục 8.

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
3. **Bug nhỏ còn treo**: token JWT hết hạn/hỏng vẫn trả 500 thay vì 401 (do `JwtAuthenticationFilter` không try/catch quanh `validate()` — đã fix phần null-secret nhưng chưa fix phần bắt exception tổng quát). Không chặn công việc hiện tại nhưng nên fix cho chuẩn REST. **Gặp lại nhiều lần trong phiên này** vì access token TTL chỉ 5 phút (`expiresInSeconds: 300`) — dev/test dài hơn 5 phút sẽ tự dính bug này, phải đăng nhập lại. Không có refresh-token tự động phía frontend (silent refresh) — cân nhắc thêm nếu bug #3 chưa fix sớm.
4. Báo người phụ trách `commonservice` xóa `CommonserviceApplication.java` + `application.properties` + `CommonserviceApplicationTests.java` thừa (vẫn còn, gây bug `COMMONSERVICE` nếu ai chạy thiếu `--spring.application.name=...`).
5. Cân nhắc thêm "giỏ hàng" thật cho nút "Thêm vào giỏ" ở trang Chi Tiết Sản Phẩm (hiện chỉ là UI, chưa nối logic — tuỳ phạm vi PRD có yêu cầu hay không).
6. Team chưa có bảng phân bổ port thống nhất — từng xảy ra trùng port `order-service`/`product-service` (8083), workaround bằng `ORDER_SERVICE_PORT=8087`.
7. **3 trang Admin Console còn là stub "Sắp ra mắt"** (`/admin/customers`, `/admin/logistics`, `/admin/revenue`) — đã điều tra kỹ, **bị chặn thật vì thiếu API bên team khác**, không phải việc Người 3 tự làm được:
   - **Khách hàng** → cần Người 2 thêm endpoint "list tất cả user" trong `user-service`. Hiện chỉ có `GET /users/me` (xem info bản thân) và 2 endpoint nội bộ tra 1 user theo id/email (`UserQueryController.java`, có `X-Internal-Token`) — không có endpoint liệt kê.
   - **Vận chuyển** → cần Người 4 thêm cả query side cho `shipping-service`. Hiện chỉ có `MockShippingController` (command side, tạo mock shipment), không có endpoint đọc/list nào.
   - **Doanh thu** → cần Người 4 thêm endpoint list/summary cho `order-service` (hiện `OrderQueryController` chỉ có `GET /api/orders/{id}` tra 1 đơn) và/hoặc `payment-service`.
   - **`/admin/security`** khác nhóm 3 trang trên — không phải thiếu API mà **chưa ai định nghĩa trang này làm gì** (audit log? quản lý session? đổi mật khẩu admin?). Cần quyết định sản phẩm trước khi code, không phải vấn đề kỹ thuật.
   - Nếu muốn làm tiếp các trang này, soạn sẵn yêu cầu API cụ thể (tên endpoint, response shape) gửi Người 2/Người 4 thay vì tự đoán.

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
13. **Không có cơ chế tạo tài khoản Admin** — `identity-service` có sẵn `enum Role { USER, ADMIN }` (`identity-service/.../model/Role.java`) và `Account.role` (cột `role` varchar trong `watch_identity_db.accounts`, giá trị `USER`/`ADMIN`), JWT claim `role` phản ánh đúng giá trị DB (`TokenServiceImpl` đọc thẳng `account.role()`, không hardcode). Nhưng **mọi luồng đăng ký đều hardcode `Role.USER`** (`IdentityServiceImpl.java` dòng ~96 và ~227, cả nhánh register thường lẫn Google login) — không có endpoint/CLI/seed nào tạo được `ADMIN`. Cách duy nhất hiện tại: đăng ký account thường rồi `UPDATE` thẳng cột `role` trong MySQL (xem mục 8). Nếu team cần nhiều Admin hơn, đây là việc Người 2 nên làm (thêm endpoint promote-role có bảo vệ, hoặc seed admin mặc định) — không thuộc phạm vi Người 3.

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
- Khi cần test qua preview cần đăng nhập, có thể tạo tài khoản test qua API (`POST /api/v1/auth/register` → lấy link xác minh qua Mailpit API `GET localhost:8025/api/v1/messages` → `GET /api/v1/auth/verify-email?token=...` (lưu ý là **GET**, không phải POST) → `POST /api/v1/auth/login`) thay vì token giả (token giả từng gây nhầm lẫn với bug #11 thật).
- **Cách promote một account lên role `ADMIN`** (xem bug #13 mục 5): đăng ký/xác minh account bình thường trước, sau đó:
  ```bash
  docker exec watch-mysql mysql -uroot -p11111111 -e "
  UPDATE watch_identity_db.accounts SET role='ADMIN' WHERE email='<email>';"
  ```
  Rồi đăng nhập lại (access token cũ không tự cập nhật role, phải lấy token mới). Tài khoản Admin test đã có sẵn: `admin.test@watchcommerce.local` / `AdminPass123!`.
- Preview tool `preview_screenshot` từng bị treo/timeout liên tục trong phiên này (không phải lỗi app) — nếu gặp lại, dùng `preview_snapshot` (đọc accessibility tree, xác nhận nội dung/text) + `preview_inspect` (đọc computed style cụ thể) để verify thay vì phụ thuộc ảnh chụp.
- Login qua `preview_click`/`preview_fill` đôi khi không trigger được Angular event handler đúng cách (click "thành công" theo tool nhưng không có network request nào xảy ra) — nếu nghi ngờ, gọi trực tiếp qua `preview_eval`: `document.querySelector('selector').click()` để chắc chắn handler chạy.
