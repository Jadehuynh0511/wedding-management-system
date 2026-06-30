# QD11 - Detailed Dynamic RBAC Plan for M1

## 1. Summary

This document defines the implementation plan for `QĐ11 - Phân quyền` in milestone `M1 - Authentication & Authorization`, based on:

- `docs/Nhom9QLTC.pdf`
- `docs/detail-plan.html`
- `dev/wedding-repository-backend/README.md`

Before implementation, the schema naming strategy is standardized from Vietnamese table names to English table names so the backend matches the project criteria and clean architecture direction more naturally.

Locked decisions:

- `QĐ11` is an administration rule, not a permission row in the permission catalog.
- Only `ADMIN` can assign or revoke permissions.
- Business permissions must not be hardcoded in code.
- Permissions must be loaded dynamically from the database.
- JWT carries identity only, not the permission list.
- Permission changes must take effect on the next request, even with the same token.
- This phase does not include unit tests or automated integration tests; only manual test planning.

## 2. Source of Truth

The source of truth for the permission catalog is:

- `BM11 - Phân quyền` in `docs/Nhom9QLTC.pdf`
- Rule note under the table: only Admin can assign/revoke permissions, and permissions must not be hardcoded in code.

Business conclusions that must stay intact:

- The system has exactly `16` assignable permissions.
- The 16 permissions include both operational business functions and system-management functions.
- The act of permission administration is an `ADMIN` capability, not a 17th permission in the catalog.

## 3. Schema Naming Standard

From this point onward, the plan uses English snake_case table names as the target schema naming convention.

### 3.1. Global table mapping

| Current Vietnamese table | Target English table |
| --- | --- |
| `loaisanh` | `hall_types` |
| `sanh` | `halls` |
| `ca` | `shifts` |
| `monan` | `menu_items` |
| `dichvu` | `services` |
| `chucnang` | `permissions` |
| `nhomnguoidung` | `user_groups` |
| `nguoidung` | `users` |
| `bangphanquyen` | `group_permissions` |
| `thamso` | `system_parameters` |
| `tieccuoi` | `wedding_bookings` |
| `ct_monan` | `booking_menu_items` |
| `ct_dichvu` | `booking_services` |
| `phieuthutiencoc` | `deposit_receipts` |
| `phieuphatsinh` | `incidental_receipts` |
| `ct_phatsinh` | `incidental_receipt_items` |
| `hoadon` | `invoices` |
| `phieuhuy` | `cancellation_receipts` |
| `baocaodoanhso` | `monthly_revenue_reports` |
| `ct_baocaods` | `monthly_revenue_report_items` |

### 3.2. RBAC table mapping used by this plan

| Legacy name | Target name | Role in RBAC |
| --- | --- | --- |
| `chucnang` | `permissions` | permission catalog |
| `nhomnguoidung` | `user_groups` | user group / role container |
| `nguoidung` | `users` | authenticated account |
| `bangphanquyen` | `group_permissions` | group-to-permission mapping |

### 3.3. Naming rules

- Table names use lowercase `snake_case`.
- Table names use English plural nouns.
- Business keys remain explicit and readable, for example `permission_code`, `group_name`, `username`.
- The plan will use only the English table names below; Vietnamese names are treated as legacy names that must be migrated before or together with implementation.

## 4. Permission Catalog Confirmed from PDF

Conventions:

- `functionalGroup` accepts only `NGHIEP_VU` or `HE_THONG`
- `permission_code` uses stable English snake case for code/API
- `display_name` keeps the original Vietnamese business wording from the PDF
- `module_key` groups permissions by backend module

| STT | Functional group | Display name | permission_code | module_key |
| --- | --- | --- | --- | --- |
| 1 | `NGHIEP_VU` | Tiếp nhận sảnh | `HALL_MANAGE` | `CATALOG` |
| 2 | `NGHIEP_VU` | Nhận đặt tiệc cưới | `WEDDING_BOOKING_CREATE` | `BOOKING` |
| 3 | `NGHIEP_VU` | Tra cứu tiệc cưới | `WEDDING_BOOKING_VIEW` | `BOOKING` |
| 4 | `NGHIEP_VU` | Lập hóa đơn thanh toán | `INVOICE_CREATE` | `BILLING` |
| 5 | `NGHIEP_VU` | Lập phiếu thu tiền cọc | `DEPOSIT_RECEIPT_CREATE` | `BILLING` |
| 6 | `NGHIEP_VU` | Lập phiếu ghi nhận dịch vụ phát sinh | `INCIDENTAL_RECEIPT_CREATE` | `BILLING` |
| 7 | `NGHIEP_VU` | Lập phiếu hủy tiệc cưới | `CANCELLATION_RECEIPT_CREATE` | `BILLING` |
| 8 | `NGHIEP_VU` | Lập báo cáo doanh số tháng | `MONTHLY_REVENUE_REPORT_GENERATE` | `REPORTING` |
| 9 | `NGHIEP_VU` | Quản lý danh mục loại sảnh | `HALL_TYPE_MANAGE` | `CATALOG` |
| 10 | `NGHIEP_VU` | Quản lý danh mục ca | `SHIFT_MANAGE` | `CATALOG` |
| 11 | `NGHIEP_VU` | Quản lý danh mục món ăn | `MENU_ITEM_MANAGE` | `CATALOG` |
| 12 | `NGHIEP_VU` | Quản lý danh mục dịch vụ | `SERVICE_MANAGE` | `CATALOG` |
| 13 | `HE_THONG` | Quản lý tài khoản nhân viên | `STAFF_ACCOUNT_MANAGE` | `SYSTEM` |
| 14 | `HE_THONG` | Quản lý nhóm người dùng | `USER_GROUP_MANAGE` | `SYSTEM` |
| 15 | `NGHIEP_VU` | Thay đổi quy định | `SYSTEM_RULE_MANAGE` | `SYSTEM` |
| 16 | `HE_THONG` | Quản lý nhật ký hệ thống | `AUDIT_LOG_VIEW` | `SYSTEM` |

Notes:

- `Thay đổi quy định` is still `NGHIEP_VU` because that is how the PDF classifies it, even though its `module_key` belongs to the system area.
- The codebase should not use BM codes as technical identifiers because the broader documents contain overlapping BM numbering across modules.

## 5. Target Backend Outcome

After implementation based on this plan, the backend must be able to:

- authenticate a user and resolve the current user context
- load permissions from `permissions` through `user_groups` and `group_permissions`
- protect endpoints with dynamic permission checks
- expose APIs for permission catalog and group permission state
- expose APIs for `ADMIN` to assign and revoke permissions
- reflect permission changes immediately on the next authenticated request

## 6. Clean Architecture Target

Implementation stays inside `modules/auth` and follows the structure already defined in `README.md`:

```text
modules/auth/
  domain/
  application/
    port/in/
    port/out/
    usecase/
  infrastructure/
    persistence/
    security/
  presentation/
    controller/
    dto/
```

### 6.1. Domain

Minimum domain models:

- `Permission`
- `UserGroup`
- `UserAccount`
- `GroupPermission`
- `PermissionCode`
- `UserStatus`

Rules that must live in domain-aware use cases:

- only `ADMIN` may assign or revoke permissions
- target group must exist
- target permission must exist
- assign is idempotent
- revoke is idempotent
- runtime authorization must not depend on permission claims embedded in JWT

### 6.2. Application

Required `port/in` interfaces:

- `LoginUseCase`
- `GetCurrentUserUseCase`
- `ListPermissionCatalogUseCase`
- `ListUserGroupsUseCase`
- `GetGroupPermissionsUseCase`
- `AssignPermissionToGroupUseCase`
- `RevokePermissionFromGroupUseCase`

Required `port/out` interfaces:

- `PermissionQueryPort`
- `GroupQueryPort`
- `GroupPermissionQueryPort`
- `GroupPermissionCommandPort`
- `UserAccountQueryPort`
- `CurrentUserPort`
- `PasswordHashPort`
- `TokenProviderPort`

### 6.3. Infrastructure

`persistence` responsibilities:

- JPA entity for `permissions`
- JPA entity for `user_groups`
- JPA entity for `users`
- JPA entity for `group_permissions`
- Spring Data repositories and adapters for the outbound ports

`security` responsibilities:

- `JwtAuthenticationFilter`
- `JwtService` or `JwtTokenProvider`
- `AuthenticatedUserPrincipal`
- `AuthorizationService`
- adapter that reads the current user from `SecurityContext`

### 6.4. Presentation

- REST controllers for auth and RBAC admin APIs
- request/response DTOs
- all responses wrapped in `ApiResponse<T>`

## 7. Detailed Phases

## Phase 1 - Lock the QD11 specification

Goal:

- finalize the exact 16 permissions from the PDF
- finalize `functionalGroup`, `permission_code`, and `module_key`
- finalize the rule that only `ADMIN` may assign/revoke permissions
- lock English table names before implementation

Work items:

- use the table in section 4 as the only source for seeding `permissions`
- use the English table mapping in section 3 as the only schema naming source
- state explicitly that `functionalGroup` is API/domain metadata and does not require a new DB column unless needed later
- state explicitly that endpoint authorization checks must use `permission_code`

Expected output:

- no ambiguity remains about permission catalog or table naming

## Phase 2 - RBAC data foundation

Goal:

- prepare the minimum schema and data required for dynamic RBAC

Work items:

- rename RBAC-related tables in the migration plan to English:
  - `permissions`
  - `user_groups`
  - `users`
  - `group_permissions`
- if schema refactor is done globally before RBAC, align all related foreign keys and indexes to the English names as well
- create the next migration after `V3`
- seed `permissions` with exactly 16 rows from section 4
- seed `user_groups` with:
  - `ADMIN`
  - `STAFF`
- seed one local development `admin` account in `users`
- seed full 16 permissions for `ADMIN` in `group_permissions`
- do not seed default permissions for `STAFF` unless another approved document requires it

Implementation constraints:

- seed data must be safe for a fresh Flyway-driven environment
- `permission_code` is the stable business key used by code and APIs

Expected output:

- after Flyway runs, the database contains enough data to manually test login and RBAC behavior

## Phase 3 - Authentication foundation for RBAC

Goal:

- provide the minimum authentication layer required for dynamic RBAC

Work items:

- create the `auth` module in clean architecture form
- implement login using `username` and password hash from `users`
- use `BCrypt` for password verification
- use stateless JWT for identity
- JWT contains only:
  - `userId`
  - `username`
  - `issuedAt`
  - `expiresAt`
- do not put `permissionCodes[]` inside JWT
- on every request:
  - read token
  - validate token
  - reload user from `users`
  - reload current group from `user_groups`
  - reload current permissions through `group_permissions`
  - build `AuthenticatedUserPrincipal`

Expected output:

- if a group permission changes, the same token reflects the new permission set on the next request

## Phase 4 - RBAC administration APIs

Goal:

- provide all backend APIs needed by admin UI or Postman for RBAC management

Required APIs:

- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/permissions`
- `GET /api/groups`
- `GET /api/groups/{groupId}/permissions`
- `POST /api/permissions/assign`
- `DELETE /api/permissions/revoke`

DTOs that must be locked:

- `LoginRequest { username, password }`
- `LoginResponse { accessToken, tokenType, expiresAt }`
- `PermissionResponse { id, code, name, moduleKey, functionalGroup, description }`
- `UserGroupResponse { id, name, systemGroup, description }`
- `GroupPermissionResponse { groupId, permissionCodes[] }`
- `AssignPermissionRequest { groupId, permissionCode }`
- `RevokePermissionRequest { groupId, permissionCode }`
- `CurrentUserResponse { id, username, groupName, permissionCodes[] }`

API protection rules:

- `POST /api/permissions/assign` is `ADMIN` only
- `DELETE /api/permissions/revoke` is `ADMIN` only
- `GET /api/permissions` and `GET /api/groups/{groupId}/permissions` require at least authenticated access; if admin-only UI is preferred, they may also be restricted to `ADMIN`
- `GET /api/auth/me` requires only a valid token

Important principle:

- do not use `hasRole()` to represent the 16 business/system permissions
- it is acceptable to use a dedicated system-group check for the two administration endpoints
- all future business endpoint protection must go through `AuthorizationService.hasPermission(permissionCode)`

## Phase 5 - Manual test and acceptance

This phase uses only manual verification. No unit tests or automated integration tests are planned here.

### 5.1. Seed verification

- start the app with a fresh database
- verify `permissions` has exactly `16` rows
- verify `user_groups` contains `ADMIN` and `STAFF`
- verify `group_permissions` grants all 16 permissions to `ADMIN`
- verify the local `admin` row exists in `users` and is active

### 5.2. Authentication verification

- log in with the `admin` account
- receive `accessToken`
- call `GET /api/auth/me` with a valid token
- verify response contains:
  - `username`
  - `groupName`
  - `permissionCodes[]`

### 5.3. Catalog and group permission reads

- call `GET /api/permissions`
- verify the response contains all 16 permissions with correct names and codes
- call `GET /api/groups`
- call `GET /api/groups/{groupId}/permissions`
- verify `ADMIN` has full permission coverage

### 5.4. Assign permission flow

- use `ADMIN` to call `POST /api/permissions/assign` for `STAFF`
- call `GET /api/groups/{groupId}/permissions` again
- verify the new permission appears
- use a `STAFF` user with an already-issued token to call the related protected endpoint on the next request
- verify the permission takes effect without issuing a new token

### 5.5. Revoke permission flow

- use `ADMIN` to call `DELETE /api/permissions/revoke`
- call `GET /api/groups/{groupId}/permissions` again
- verify the permission is removed
- use the same existing token for a user in that group
- verify the protected endpoint now returns `403`

### 5.6. Unauthorized access checks

- use a non-`ADMIN` user to call `POST /api/permissions/assign`
- verify access is denied
- use a non-`ADMIN` user to call `DELETE /api/permissions/revoke`
- verify access is denied
- use a user without a required permission to call a protected endpoint
- verify the response is `403`

## 8. Design rules that must stay consistent

- `permission_code` is the stable business key
- Vietnamese display names remain unchanged from the PDF
- `functionalGroup` is API/UI metadata with only:
  - `NGHIEP_VU`
  - `HE_THONG`
- `ADMIN` is a dedicated system group for permission administration
- `STAFF` is only a default operational group and has no hardcoded implicit permissions
- runtime permission decisions must always come from the database, not from cached token claims

## 9. Out of scope for this document

The following are not directly implemented by this plan:

- complete admin UI
- detailed audit-log storage for `QĐ13`
- a full refresh-token lifecycle if M1 does not yet require it
- unit tests and automated integration tests

## 10. Definition of done

This document is implementation-ready when:

- the 16-permission catalog is fully locked
- English table names are fully locked
- the path from schema to API is clear
- the DTO and API contracts are sufficient for backend work
- the manual verification checklist is enough for QA by Postman or temporary admin UI
- the implementer does not need to re-decide:
  - permission catalog
  - table naming
  - auth-foundation scope
  - `ADMIN` administration rule
  - manual acceptance flow
