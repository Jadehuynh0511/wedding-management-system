# M1 - QD13 - Kế Hoạch Triển Khai Nhật Ký Hệ Thống

## 1. Mục tiêu

Tài liệu này chốt kế hoạch backend cho `QĐ13 - Nhật ký hệ thống` trong `M1`, dựa trên:

- `docs/detail-plan.html`
- `docs/m1-qd11-phan-quyen-plan.md`
- source backend hiện tại trong `dev/wedding-repository-backend`

Mục tiêu của QĐ13 ở giai đoạn này là:

- có cơ chế ghi nhật ký hệ thống tự động cho các thao tác làm thay đổi dữ liệu
- dữ liệu nhật ký là `immutable`, chỉ cho `INSERT` và `READ`
- có API đọc nhật ký theo kiểu `read-only`, hỗ trợ filter và phân trang
- tái sử dụng nền auth/RBAC đã có từ QĐ11 thay vì dựng một cơ chế quyền riêng

## 2. Trạng thái nền hiện tại từ source

Những thứ đã có sẵn và sẽ được tận dụng trực tiếp:

- `modules/auth` đã hoàn thiện login JWT stateless và `GET /api/auth/me`
- mỗi request đã reload lại `group + permissions` từ DB
- `AuthorizationService` đã hỗ trợ:
  - `isAdmin()`
  - `hasPermission(permissionCode)`
- permission catalog đã có sẵn `AUDIT_LOG_VIEW`
- `ApiResponse<T>` đã là response envelope chung
- `GlobalExceptionHandler` đã chuẩn hóa `401/403/404/400/500`
- schema RBAC đã dùng English snake_case và Flyway hiện đang ở `V9`

Kết luận quan trọng:

- QĐ13 không nên nhét vào `modules/auth`
- QĐ13 nên là một module riêng, nhưng được phép phụ thuộc vào `CurrentUserPort` và `AuthorizationService` của auth để lấy user hiện tại và check quyền

## 3. Review QD11 trước khi đi tiếp

Sau khi đối chiếu `m1-qd11-phan-quyen-plan.md` với source hiện tại, phần QĐ11 có thể xem là đã đủ nền để đi tiếp QĐ13:

- permission catalog 16 quyền đã khóa đúng spec và đang đọc từ DB
- `AUDIT_LOG_VIEW` đã tồn tại trong catalog, nên QĐ13 đã có sẵn business key để bảo vệ API đọc log
- auth foundation đã đúng hướng dynamic RBAC: token chỉ mang identity, quyền được reload mỗi request
- RBAC administration APIs đã đủ để dùng cho admin/Postman
- clean architecture của `modules/auth` đã đủ rõ để làm mẫu cho module tiếp theo

Điểm cần hiểu đúng:

- M1 tổng thể trong `detail-plan.html` còn có BM13, BM14 và phần nhật ký
- nhưng riêng QĐ11 thì hiện tại đã "đủ đóng" để dùng làm nền cho QĐ13

## 4. Kết luận nghiệp vụ cần khóa cho QĐ13

Từ `detail-plan.html` và nền source hiện tại, các quyết định nghiệp vụ cho QĐ13 cần được khóa như sau:

- nhật ký hệ thống là dữ liệu bất biến
- backend không cung cấp API `UPDATE` hoặc `DELETE` cho nhật ký
- nhật ký được ghi tự động khi có thao tác `CUD` hoặc thao tác quản trị làm thay đổi trạng thái hệ thống
- API đọc nhật ký là `read-only`
- quyền truy cập API đọc nhật ký đi qua permission `AUDIT_LOG_VIEW`
- không hardcode `ADMIN` cho API đọc log nếu business đã có permission riêng

Giải thích áp dụng:

- `detail-plan.html` mô tả "Admin xem nhật ký", nhưng source hiện tại đã có permission `AUDIT_LOG_VIEW`
- vì QĐ11 đã xác lập quyền theo `permission_code`, nên với QĐ13 nên dùng `@authorizationService.hasPermission('AUDIT_LOG_VIEW')`
- ở seed hiện tại chỉ `ADMIN` có quyền này, nên runtime vẫn đang là admin-only mà không phải hardcode

## 5. Những gì áp dụng từ detail-plan và những gì cần điều chỉnh theo source

### 5.1. Những gì áp dụng nguyên

- có `AuditLogAspect`
- log được ghi tự động thay vì controller gọi thủ công
- log phải lưu được:
  - thời gian
  - người thao tác
  - hành động
  - đối tượng bị tác động
  - kết quả `SUCCESS/FAIL`
- có API `GET /api/audit-logs`
- màn đọc log là `read-only`

### 5.2. Những gì cần điều chỉnh để hợp với source hiện tại

#### A. Không dùng pointcut quá rộng kiểu `execution(* *..*Service.*(..))`

Lý do:

- source đang đi theo clean architecture
- về sau sẽ có rất nhiều `*Service` chỉ để đọc dữ liệu
- pointcut quá rộng sẽ log thừa, log trùng, và khó kiểm soát ý nghĩa nghiệp vụ

Hướng chốt:

- dùng custom annotation, ví dụ `@AuditAction(...)`
- chỉ annotate vào các use case làm thay đổi dữ liệu
- aspect chỉ bắt các method đã được đánh dấu rõ ràng

#### B. Không gắn QĐ13 vào module auth

Lý do:

- audit log là một capability riêng
- auth chỉ là nơi cung cấp current user context và permission check
- tách module riêng sẽ sạch dependency hơn khi M2, M3, M4 bắt đầu sinh thêm nhiều hành động cần log

Hướng chốt:

- tạo `modules/audit`

#### C. Không dùng `BaseEntity` cho bảng audit log

Lý do:

- `BaseEntity` hiện có `created_at` và `updated_at`
- audit log là immutable, nên cột `updated_at` không mang ý nghĩa đúng
- nếu dùng `BaseEntity`, thiết kế sẽ vô tình ngầm cho phép một lifecycle có update

Hướng chốt:

- entity audit log có cột thời điểm riêng, ví dụ `occurred_at`
- không kế thừa `BaseEntity`

#### D. Async logging chỉ xem là tối ưu hóa sau khi khóa được tính đúng

`detail-plan.html` có nhắc async logging để tránh block request. Tuy nhiên với source hiện tại, ưu tiên đúng thứ tự nên là:

1. log ghi đúng
2. log không mất khi transaction nghiệp vụ rollback
3. log không rò rỉ dữ liệu nhạy cảm
4. sau đó mới cân nhắc async nếu thực sự cần

Hướng chốt:

- phase đầu nên ghi qua một đường riêng có `REQUIRES_NEW` để giữ log `FAIL`
- async có thể để ở phase tối ưu hóa sau, không coi là bắt buộc của QĐ13 v1

## 6. Phạm vi backend của QĐ13

Phase này chỉ lên plan cho backend:

- schema audit log
- module audit theo clean architecture
- annotation + aspect để ghi log tự động
- query API đọc log
- rule permission cho API đọc log
- manual test checklist

Chưa nằm trong phạm vi tài liệu này:

- UI trang nhật ký ở frontend
- export Excel/CSV/PDF
- dashboard thống kê số lượng log
- SIEM/external log shipping
- audit cho authentication events như failed login brute-force

## 7. Kết quả backend mục tiêu

Sau khi implement theo plan này, backend cần đạt:

- có bảng `audit_logs` trong DB
- các thao tác quản trị hoặc CUD đã annotate sẽ tự ghi log
- log lưu được snapshot actor và thông tin hành động
- API `GET /api/audit-logs` đọc được danh sách log theo filter
- API đọc log chỉ truy cập được khi user có permission `AUDIT_LOG_VIEW`
- không có API sửa/xóa log

## 8. Clean Architecture target cho module audit

```text
modules/audit/
  domain/
  application/
    port/in/
    port/out/
    usecase/
  infrastructure/
    aspect/
    persistence/
  presentation/
    controller/
    dto/
```

### 8.1. Domain

Model tối thiểu:

- `AuditLog`
- `AuditResultStatus`
- `AuditActionCode`
- `AuditActorSnapshot`

Rule ở domain/application level:

- audit log là immutable
- không được lưu secret nhạy cảm vào log
- log phải phân biệt `SUCCESS` và `FAIL`
- actor snapshot phải giữ được dữ liệu ngay cả khi user đổi group hoặc đổi username về sau

### 8.2. Application

`port/in` tối thiểu:

- `SearchAuditLogsUseCase`
- `RecordAuditLogUseCase`

`port/out` tối thiểu:

- `AuditLogQueryPort`
- `AuditLogCommandPort`
- `CurrentUserPort`

Ghi chú:

- `CurrentUserPort` có thể tái sử dụng từ module auth
- không cần tạo cơ chế current-user mới cho QĐ13

### 8.3. Infrastructure

`persistence`:

- JPA entity cho `audit_logs`
- Spring Data repository
- adapter cho query và insert

`aspect`:

- annotation `@AuditAction`
- aspect bắt quanh method đã annotate
- mapper từ execution context sang command ghi log

### 8.4. Presentation

- `AuditLogController`
- query params cho filter
- response DTO có page metadata và list item

## 9. Chuẩn schema và migration

Migration tiếp theo nên là:

- `V10__create_audit_logs.sql`

Tên bảng dùng English snake_case để nhất quán với QĐ11:

- `audit_logs`

### 9.1. Cột đề xuất

| Cột | Kiểu | Ý nghĩa |
| --- | --- | --- |
| `id` | `bigint` identity | khóa chính |
| `occurred_at` | `timestamptz` | thời điểm log được ghi |
| `actor_user_id` | `bigint null` | id user nếu có |
| `actor_username` | `varchar(100)` | snapshot username lúc thao tác |
| `actor_group_name` | `varchar(100)` | snapshot group name lúc thao tác |
| `action_code` | `varchar(50)` | action nghiệp vụ/kỹ thuật |
| `module_key` | `varchar(50)` | module bị tác động, ví dụ `AUTH`, `CATALOG` |
| `target_type` | `varchar(100)` | loại đối tượng bị tác động |
| `target_id` | `varchar(100) null` | id hoặc business key của đối tượng |
| `target_label` | `varchar(255) null` | tên hiển thị nếu có |
| `result_status` | `varchar(10)` | `SUCCESS` hoặc `FAIL` |
| `description` | `text` | mô tả ngắn dễ đọc |
| `error_message` | `text null` | lỗi ngắn gọn nếu fail |
| `details_json` | `jsonb null` | metadata bổ sung đã sanitize |

### 9.2. Index đề xuất

- index `occurred_at desc`
- index `(actor_username, occurred_at desc)`
- index `(action_code, occurred_at desc)`
- index `(result_status, occurred_at desc)`
- index `(module_key, occurred_at desc)`

### 9.3. Constraint nghiệp vụ

- `result_status in ('SUCCESS', 'FAIL')`
- không tạo bất kỳ migration nào cho update/delete log
- không dùng `on delete cascade` để tránh log bị biến mất theo dữ liệu gốc

## 10. Dữ liệu nào được phép và không được phép lưu

### 10.1. Được phép lưu

- username thao tác
- group name thao tác
- tên action
- id hoặc business key của object
- kết quả `SUCCESS/FAIL`
- mô tả business ngắn gọn

### 10.2. Không được phép lưu

- password raw
- `password_hash`
- JWT access token
- refresh token
- secret key
- full request body nếu có dữ liệu nhạy cảm

Hướng áp dụng:

- `details_json` chỉ lưu dữ liệu đã được chọn lọc
- nếu action liên quan user/password thì chỉ log `target user`, không log secret

## 11. Chiến lược ghi log tự động

### 11.1. Annotation đề xuất

Định nghĩa annotation, ví dụ:

- `@AuditAction(action = "PERMISSION_ASSIGN", module = "AUTH", targetType = "GROUP_PERMISSION")`

Annotation này nên đặt ở các method use case ghi dữ liệu, không đặt ở controller.

Lý do:

- controller chỉ là HTTP adapter
- use case mới là nơi business action thực sự được thực hiện
- log bám use case sẽ ổn hơn khi sau này có thêm batch job hoặc adapter khác ngoài REST

### 11.2. Aspect flow đề xuất

Khi method có `@AuditAction` được gọi:

1. aspect lấy thời điểm bắt đầu
2. lấy current user từ `CurrentUserPort`
3. đọc metadata từ annotation
4. cố gắng suy ra `targetId`, `targetLabel`, `description` từ command/result
5. nếu method thành công thì ghi `SUCCESS`
6. nếu method ném exception thì ghi `FAIL`
7. rethrow exception gốc để không làm đổi business flow

### 11.3. Transaction boundary

Đây là điểm cần khóa ngay trong plan:

- ghi audit log nên đi qua transaction riêng
- mục tiêu là nếu nghiệp vụ chính fail và rollback, log `FAIL` vẫn còn

Hướng chốt:

- `RecordAuditLogUseCase` hoặc adapter ghi DB chạy với `REQUIRES_NEW`

## 12. API contract đề xuất

### 12.1. Endpoint

- `GET /api/audit-logs`

### 12.2. Quyền truy cập

- dùng `@PreAuthorize("@authorizationService.hasPermission('AUDIT_LOG_VIEW')")`

Giải thích:

- đúng tinh thần dynamic RBAC của QĐ11
- mặc định seed hiện tại chỉ `ADMIN` có quyền này
- nếu sau này business muốn giao quyền xem log cho group khác thì không cần sửa code rule

### 12.3. Query params tối thiểu

- `from`
- `to`
- `username`
- `actionCode`
- `resultStatus`
- `page`
- `size`

Có thể mở rộng sau nếu cần:

- `moduleKey`
- `targetType`

### 12.4. Response shape đề xuất

`ApiResponse<AuditLogPageResponse>`

Trong đó:

- `AuditLogPageResponse`
  - `items[]`
  - `page`
  - `size`
  - `totalElements`
  - `totalPages`

- `AuditLogItemResponse`
  - `id`
  - `occurredAt`
  - `actorUserId`
  - `actorUsername`
  - `actorGroupName`
  - `actionCode`
  - `moduleKey`
  - `targetType`
  - `targetId`
  - `targetLabel`
  - `resultStatus`
  - `description`
  - `errorMessage`

### 12.5. Sort mặc định

- `occurredAt desc`

## 13. Phân phase triển khai đề xuất

## Phase 1 - Lock QĐ13 specification

Mục tiêu:

- chốt semantic của nhật ký hệ thống
- chốt quyền truy cập API đọc log
- chốt boundary module

Việc cần làm:

- xác nhận QĐ13 là `insert-only + read-only`
- xác nhận đọc log dùng permission `AUDIT_LOG_VIEW`
- chốt `modules/audit` là module riêng
- chốt strategy `annotation + aspect`, không dùng pointcut quá rộng

Kết quả mong đợi:

- không còn mơ hồ về việc log cái gì và bảo vệ API bằng cách nào

## Phase 2 - Audit log data foundation

Mục tiêu:

- chuẩn bị schema DB cho audit log

Việc cần làm:

- tạo migration `V10__create_audit_logs.sql`
- tạo constraints và indexes
- xác nhận bảng không có `updated_at`
- xác nhận không có cascade delete làm mất lịch sử

Kết quả mong đợi:

- DB sẵn sàng nhận audit log immutable

## Phase 3 - Read side cho audit log

Mục tiêu:

- dựng module audit phần đọc trước

Việc cần làm:

- tạo domain model và query ports
- tạo JPA query adapter
- tạo `GET /api/audit-logs`
- thêm filter + pagination + sort mặc định
- bảo vệ endpoint bằng `AUDIT_LOG_VIEW`

Kết quả mong đợi:

- backend đọc được log theo filter qua API read-only

## Phase 4 - Write side qua annotation + aspect

Mục tiêu:

- dựng cơ chế tự ghi log

Việc cần làm:

- tạo annotation `@AuditAction`
- tạo aspect bắt các method đã annotate
- tạo `RecordAuditLogUseCase`
- ghi `SUCCESS/FAIL`
- dùng transaction riêng cho audit insert

Kết quả mong đợi:

- backend có thể auto-write log mà không cần controller/service tự save thủ công

## Phase 5 - Gắn QĐ13 vào những flow đang có thật trong source

Mục tiêu:

- có ít nhất một luồng hiện hữu được log end-to-end để verify

Việc cần làm:

- gắn audit vào `assign permission`
- gắn audit vào `revoke permission`
- nếu trong lúc làm có thêm BM13/BM14 thì tiếp tục annotate các use case write tương ứng

Giải thích:

- hiện source mới hoàn thiện rõ nhất ở `modules/auth`
- vì vậy RBAC administration flows là nơi verify QĐ13 tự nhiên nhất

Kết quả mong đợi:

- có log thực tế sinh ra từ flow đang chạy được ở local

## Phase 6 - Manual verification & acceptance

Mục tiêu:

- xác nhận QĐ13 chạy đúng nghiệp vụ

Checklist:

- gọi action `assign permission` bằng `ADMIN`
- verify log `SUCCESS` được tạo
- cố tình gọi action invalid để phát sinh `FAIL`
- verify log `FAIL` vẫn còn dù nghiệp vụ chính rollback
- gọi `GET /api/audit-logs` với filter `username`
- gọi `GET /api/audit-logs` với filter `actionCode`
- verify thứ tự mới nhất trước
- dùng user không có `AUDIT_LOG_VIEW` gọi API
- verify backend trả `403`
- verify log không chứa password/token/hash

## 14. Những điểm cần áp dụng nhất quán khi implement

- giữ response theo `ApiResponse<T>` giống module auth
- comment các chỗ logic quan trọng trực tiếp trong source bằng tiếng Việt
- naming DB và code dùng English schema giống QĐ11
- controller chỉ giữ HTTP contract, không ôm business logic audit
- permission check đi qua `AuthorizationService`
- log read API không được hard delete, soft delete, hay edit

## 15. Ngoài phạm vi tài liệu này

- không implement BM13 CRUD user trong plan này
- không implement BM14 CRUD group trong plan này
- không thêm refresh token hoặc logout flow chỉ vì QĐ13
- không log tất cả read-only query
- không log raw payload nhạy cảm

## 16. Definition of done

Plan QĐ13 được xem là implementation-ready khi:

- đã chốt module `audit` riêng
- đã chốt schema `audit_logs`
- đã chốt permission `AUDIT_LOG_VIEW` cho API đọc log
- đã chốt strategy `annotation + aspect`
- đã chốt transaction riêng cho audit insert
- đã có checklist manual verify rõ ràng
- implementer không cần phải quyết định lại:
  - bảng nào sẽ dùng
  - endpoint nào cần mở
  - quyền nào dùng để xem log
  - log write sẽ hook vào đâu
  - dữ liệu nhạy cảm nào phải loại khỏi log
