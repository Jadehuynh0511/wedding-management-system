# Tự động refresh access token (BFF proxy)

> Tài liệu này tóm tắt thay đổi xử lý việc **access token hết hạn** ở frontend.
> Mục tiêu: khi token hết hạn, hệ thống tự lấy token mới ở phía dưới, người dùng **không** bị văng lỗi, **không** bị đá ra trang login.

---

## 1. Nỗi đau lúc trước 😣

Access token có hạn **1 giờ**. Khi nó hết hạn mà người dùng vẫn đang thao tác:

- Người dùng đang ở 1 trang (vd: Nhật ký hệ thống), ngồi đủ lâu cho token hết hạn.
- Bấm "Tìm kiếm" / phân trang → request gọi backend với token cũ → backend trả **401**.
- Code ném lỗi → màn hình hiện **"Unhandled Runtime Error: Invalid or expired access token"** (màn đỏ).
- Người dùng buộc phải **F5**, rồi bị **đá ra trang đăng nhập** → phải đăng nhập lại từ đầu.

### Vì sao lại lỗi?

```
Server Component (page.tsx)              Client Component (table.tsx)
  đọc token từ cookie                       giữ token "đóng băng"
        │                                          │
        │  truyền token làm prop  ───────────────► │  fetch THẲNG tới backend
        ▼                                          ▼     kèm token cũ
   (token tươi)                              ❌ 401 khi token hết hạn → ném lỗi đỏ
```

2 vấn đề cốt lõi:

1. **Token bị "đóng băng"**: token được đọc lúc render trang rồi truyền xuống client. Người dùng ngồi lâu → token trong tay client đã hết hạn nhưng vẫn đem đi gọi.
2. **Bỏ qua cơ chế refresh**: cơ chế refresh cũ chỉ chạy khi **chuyển trang** (middleware), không chạy cho các thao tác fetch ngay trong trang.
3. (Phụ) Token bị **lộ ra trình duyệt** — mất ý nghĩa của cookie `httpOnly`.

---

## 2. Giải quyết như thế nào 🛠️

> **Backend không phải sửa gì** — nó đã có sẵn `POST /api/auth/refresh` chuẩn production (xoay vòng token, chống tái sử dụng). Toàn bộ xử lý nằm ở **frontend**.

Áp dụng mô hình **BFF proxy (Backend-for-Frontend)**:

> Trình duyệt **không bao giờ** cầm token. Mọi lời gọi dữ liệu đi qua một "trạm trung chuyển" chạy trên server của Next.js. Trạm này đọc cookie, gắn token, gọi backend. Nếu backend báo token hết hạn (401), trạm **tự làm mới token + gọi lại** — tất cả diễn ra trong suốt, người dùng không hề hay biết.

### Luồng mới

```
Client Component                BFF Proxy (server Next.js)              Backend
 fetch /api/backend/...   ──►   đọc cookie httpOnly
 (KHÔNG cầm token)              gắn Bearer → gọi backend  ──────────►
                                                            ◄────────  401 (token hết hạn)
                                gọi /api/auth/refresh     ──────────►
                                                            ◄────────  token mới
                                gọi lại request với token mới ───────►
                                                            ◄────────  ✅ 200 + dữ liệu
                          ◄───  trả dữ liệu + set cookie mới
   nhận dữ liệu bình thường (không lỗi, không reload)
```

Chỉ khi **refresh token (hạn 7 ngày)** cũng hết hạn / bị thu hồi thì mới điều hướng về `/login` — đây là hành vi đúng ("phiên thực sự kết thúc").

### Điểm tinh tế quan trọng: "single-flight refresh"

Backend **xoay vòng** refresh token và **phát hiện tái sử dụng** (`REUSE_DETECTED`): nếu 2 request cùng dùng 1 refresh token cũ để làm mới, request thứ 2 bị coi là tấn công → **thu hồi cả phiên** (đá người dùng ra ngoài).

Proxy xử lý bằng cách **gom nhiều request refresh đồng thời thành đúng 1 lần gọi backend** (single-flight) + cache kết quả 15 giây. Nhờ vậy việc bấm lung tung nhiều nút cùng lúc khi token vừa hết hạn vẫn an toàn.

---

## 3. Hiện trạng sau khi fix ✅

| Tình huống                             | Trước                               | Sau                                    |
| -------------------------------------- | ----------------------------------- | -------------------------------------- |
| Access token hết hạn khi đang thao tác | Lỗi đỏ → F5 → đá ra login           | Tự refresh, thao tác chạy bình thường  |
| Token có lộ ra trình duyệt không?      | Có (truyền làm prop)                | Không (chỉ nằm trong cookie httpOnly)  |
| Khi nào bị về trang login?             | Bất cứ khi nào access token hết hạn | Chỉ khi refresh token (7 ngày) hết hạn |
| Nhiều request 401 cùng lúc             | Có nguy cơ bị thu hồi cả phiên      | An toàn (single-flight)                |

---

## 4. Những gì đã thay đổi 📂

### File mới (phần lõi)

| File                                     | Vai trò                                                                                                                                            |
| ---------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| `src/app/api/backend/[...path]/route.ts` | **BFF proxy**: forward request, gặp 401 thì refresh + retry 1 lần + set cookie mới. Có single-flight chống `REUSE_DETECTED`.                       |
| `src/shared/api/backend-client.ts`       | Hàm `backendRequest` dùng chung: client → đi qua proxy; server component → gọi thẳng backend kèm token. Client gặp 401 thật → điều hướng `/login`. |

### File refactor (theo quy ước mới)

- **11 file `*-api.ts`** (halls, hall-types, menu-items, services, shifts, user-groups, system-rules, audit-logs, booking, invoice, incidental): dùng `backendRequest` thay vì tự ghép URL + Bearer.
  - Quy ước hàm: `fn(...thamSốNghiệpVụ, accessToken?)`
  - Hàm **mutation** (tạo/sửa/xóa — chỉ client gọi): **bỏ** tham số token.
  - Hàm **read** (đọc — server + client gọi): giữ `accessToken?` ở cuối (server truyền, client bỏ trống).
- **14 client component**: bỏ prop `accessToken`, sửa lại các lời gọi API.
- **~18 page (server component)**: bỏ truyền prop `accessToken` xuống client; vẫn đọc token cho lần fetch đầu (SSR).
- 1 sửa phụ không liên quan: bỏ 2 biến setter dead-code có sẵn trong `service-page-form.tsx` để build qua.

### Không đổi

- Backend (đã có sẵn refresh endpoint).
- `middleware.ts`, các route `/api/auth/*` (login/me/refresh/logout) — giữ nguyên.

---

## 5. Test như thế nào 🧪

### Test trải nghiệm thực tế (khuyến nghị)

Để khỏi chờ 1 giờ, tạm rút ngắn hạn access token:

1. Ở **backend**, mở `.env`, đặt: `JWT_ACCESS_TOKEN_TTL=60s` rồi khởi động lại backend.
2. Đăng nhập vào app.
3. Mở 1 trang bất kỳ (vd: **Nhật ký hệ thống** `/dashboard/audit-logs`).
4. **Ngồi chờ > 60 giây** (cho access token hết hạn).
5. Bấm "Tìm kiếm" hoặc chuyển trang.

**Kết quả mong đợi:**

- ✅ Dữ liệu tải bình thường, **không** hiện lỗi đỏ, **không** bị reload, **không** bị đá ra login.
- ✅ Mở DevTools → tab Network: thấy request `/api/backend/...` trả 200; cookie `wm_access_token` được cập nhật (giá trị mới).

6. (Tùy chọn) Test phiên hết hạn thật: xóa cookie `wm_refresh_token` rồi thao tác → lúc này **mới** bị điều hướng về `/login` (đúng như mong đợi).

7. Nhớ trả lại `JWT_ACCESS_TOKEN_TTL=1h` sau khi test xong.
