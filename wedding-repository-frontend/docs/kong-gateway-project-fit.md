# Kong Gateway Trong Project Wedding Management

## 1. Tài liệu này giúp gì cho bạn?

Nếu bạn mới tiếp cận Kong Gateway, thường sẽ có 3 câu hỏi rất tự nhiên:

1. Kong Gateway là gì?
2. Với project này thì có nên dùng không?
3. Nếu muốn thử, nên bắt đầu từ đâu để không làm rối hệ thống?

File này trả lời đúng 3 câu hỏi đó, dựa trên hiện trạng thật của workspace hiện tại chứ không nói lý thuyết chung chung.

---

## 2. Hiện trạng thật của project này

Ở workspace hiện tại, hệ thống đang được tách thành 2 repository chính:

- `wedding-repository-frontend`: `Next.js 14`
- `wedding-repository-backend`: `Spring Boot 3`

Luồng hiện tại đang là:

```text
Browser
  -> Next.js frontend (:3000)
      -> route handlers / BFF nội bộ
          -> Spring Boot backend (:8082)
```

Điểm quan trọng:

- Frontend không chỉ là UI thuần.
- Frontend đang có lớp `BFF`/proxy riêng trong `src/app/api/*`.
- Luồng auth hiện tại đang dùng cookie `httpOnly` và refresh token qua phía server của Next.js.
- Backend hiện tại là **một service chính duy nhất**, chưa phải hệ nhiều microservice.

Nói ngắn gọn: project này **đã có một lớp gateway nhỏ ở phía frontend** cho auth và proxy business API.

---

## 3. Kong Gateway là gì?

`Kong Gateway` là một **API Gateway**. Bạn có thể hiểu đơn giản nó là một "cổng vào chung" đứng trước backend service.

Thay vì client gọi trực tiếp từng backend, client hoặc BFF sẽ gọi qua Kong trước. Kong sẽ làm các việc hạ tầng chung như:

- định tuyến request đến đúng service
- áp chính sách bảo mật chung
- rate limiting
- logging
- quan sát request tập trung
- chuẩn hóa entry point cho nhiều backend phía sau

### Kong không phải là gì

Kong **không phải**:

- nơi viết business logic của hệ thống cưới hỏi
- nơi thay thế hoàn toàn backend Spring Boot
- nơi thay thế frontend Next.js
- nơi nên nhét toàn bộ phân quyền nghiệp vụ nếu backend đã có RBAC rõ ràng

Kong nên được xem là **lớp hạ tầng đứng trước backend**, không phải nơi xử lý nghiệp vụ lõi.

---

## 4. Một vài khái niệm rất cơ bản trong Kong

### Service

`Service` là backend đích mà Kong sẽ forward request tới.

Ví dụ trong project này:

- Spring Boot backend ở `http://host.docker.internal:8082`

### Route

`Route` là luật để Kong biết request nào sẽ đi vào `service` nào.

Ví dụ:

- request `/api/halls`
- request `/api/bookings`
- request `/api/auth/me`

đều có thể được route về backend Spring Boot.

### Plugin

`Plugin` là phần mở rộng để Kong xử lý các concern chung như:

- rate limiting
- request/response transform
- logging
- authentication ở mức gateway
- correlation id

### Consumer

`Consumer` thường dùng khi Kong cần nhận diện client gọi vào, ví dụ:

- mobile app
- đối tác bên ngoài
- public API client

Trong giai đoạn hiện tại của project này, khái niệm `consumer` chưa phải phần quan trọng nhất để bắt đầu học.

---

## 5. Kong có phù hợp với project này không?

## Kết luận ngắn

`Có phù hợp`, nhưng `chưa bắt buộc phải ưu tiên ngay`.

### Khi nói "phù hợp"

Kong phù hợp nếu team muốn:

- học đúng mô hình API Gateway
- chuẩn hóa một entry point chung trước backend
- chuẩn bị cho tương lai có nhiều client hơn ngoài web admin
- gom logging, rate limiting, observability ở một chỗ
- tạo nền cho việc tách backend thành nhiều service sau này

### Khi nói "chưa bắt buộc ngay"

Ở thời điểm hiện tại:

- backend mới chỉ là 1 Spring Boot service
- frontend đã có BFF/proxy cho auth và business API
- auth flow hiện tại đang hoạt động hợp lý với cookie `httpOnly`
- thêm Kong sẽ làm tăng độ phức tạp vận hành

Vì vậy, nếu team còn đang ưu tiên hoàn thiện:

- chức năng nghiệp vụ
- API business
- màn hình quản trị
- độ ổn định auth/RBAC

thì Kong **không nên là ưu tiên số 1**.

---

## 6. Trong project này, Kong nên đứng ở đâu?

Với hệ thống hiện tại, cách đặt Kong an toàn nhất là:

```text
Browser
  -> Next.js frontend (:3000)
      -> Kong Gateway (:8000)
          -> Spring Boot backend (:8082)
```

Tức là:

- Browser vẫn gọi vào Next.js như hiện tại
- Next.js vẫn giữ vai trò BFF/auth proxy
- chỉ đổi điểm backend đích của Next.js từ `:8082` sang Kong `:8000`

### Vì sao đây là cách ghép phù hợp nhất?

Vì nó giữ được những thứ project đang làm tốt:

- không phá luồng cookie `httpOnly`
- không cần viết lại auth flow
- không bắt frontend client-side cầm token
- không buộc thay đổi lớn ở code hiện tại
- cho phép học Kong theo kiểu "bọc ngoài backend trước"

### Điều nên giữ nguyên ở giai đoạn đầu

- Vai trò của `/api/auth/*` trong Next.js
- Vai trò của `/api/backend/*` trong Next.js
- Luồng refresh token hiện tại
- Phân quyền nghiệp vụ ở backend Spring Security + JWT + RBAC

Nói cách khác: **ban đầu Kong không thay thế BFF của Next.js**. Kong chỉ đứng giữa Next.js server và Spring Boot backend.

---

## 7. Vậy có nên bỏ BFF của Next.js và cho Browser gọi thẳng Kong không?

Hiện tại **không nên**.

Lý do:

- project đang dùng auth cookie `httpOnly` khá đúng hướng
- Next.js route handlers đang xử lý refresh token và retry rất thực tế
- nếu cho browser gọi thẳng Kong, bạn sẽ phải thiết kế lại khá nhiều chuyện về auth, cookie, CORS, và session flow
- cái bạn muốn học lúc này là `API Gateway`, không phải cùng lúc lật lại toàn bộ kiến trúc auth

Với project này, hướng tiếp cận ít rủi ro nhất là:

1. Giữ nguyên frontend/BFF.
2. Đặt Kong trước backend.
3. Chỉ khi nào hệ thống lớn hơn mới cân nhắc dịch chuyển thêm trách nhiệm sang gateway.

---

## 8. So sánh trước và sau khi thêm Kong

### Trước khi có Kong

```text
Browser
  -> Next.js (:3000)
      -> /api/auth/* và /api/backend/*
          -> Spring Boot (:8082)
```

### Sau khi thêm Kong

```text
Browser
  -> Next.js (:3000)
      -> /api/auth/* và /api/backend/* vẫn giữ nguyên vai trò
          -> Kong (:8000)
              -> Spring Boot (:8082)
```

Khác biệt chính:

- frontend gần như không đổi cách tổ chức
- backend gần như không đổi business logic
- Kong được thêm vào như một lớp hạ tầng trung gian

---

## 9. Một vài route thực tế trong project này có thể đi qua Kong

Backend hiện tại có nhiều endpoint phù hợp để đi qua gateway, ví dụ:

- `GET /api/halls`
- `GET /api/bookings`
- `GET /api/auth/me`
- `POST /api/auth/login`
- `POST /api/auth/refresh`

Trong giai đoạn PoC, bạn có thể đơn giản route toàn bộ `/api/*` sang backend Spring Boot.

Điều đó nghĩa là Kong chưa cần hiểu sâu từng nghiệp vụ. Nó chỉ cần:

- nhận request
- forward đúng nơi
- log lại
- áp policy chung nếu cần

---

## 10. Lộ trình áp dụng hợp lý theo phase

## Phase 0: Hiểu đúng vai trò trước khi cài

Mục tiêu:

- hiểu Kong là lớp hạ tầng, không phải business layer
- xác định team thật sự cần gì ở gateway

Câu hỏi nên tự trả lời ở phase này:

- team chỉ muốn học Kong hay chuẩn bị production thật?
- có dự định có mobile app hoặc public API không?
- có dự định tách backend thành nhiều service không?
- có cần logging/rate limiting/observability tập trung không?

Nếu câu trả lời phần lớn là "chưa", thì chưa cần đẩy Kong lên ưu tiên cao.

## Phase 1: Dựng local PoC đơn giản

Mục tiêu:

- dựng Kong local
- forward toàn bộ `/api/*` sang Spring Boot backend
- đổi `BACKEND_API_BASE_URL` của frontend sang Kong

Ở phase này, bạn chỉ cần chứng minh:

- request từ Next.js đi qua Kong thành công
- Kong forward đúng sang backend
- không phá auth flow hiện tại

Đây là phase phù hợp nhất để bạn bắt đầu học.

## Phase 2: Thêm concern chung có giá trị thật

Sau khi PoC chạy ổn, có thể thêm dần:

- request logging
- correlation id
- rate limiting cho một số nhóm API
- basic monitoring hoặc tracing integration

Điểm cần nhớ:

- chỉ thêm thứ nào team thật sự dùng
- không cần bật hàng loạt plugin chỉ vì Kong hỗ trợ

## Phase 3: Chỉ nâng vai trò Kong khi hệ thống lớn hơn

Chỉ nên cân nhắc dùng Kong sâu hơn khi có một trong các tình huống sau:

- có thêm mobile app
- có public API cho bên ngoài
- backend bắt đầu tách thành nhiều service
- cần quản lý policy tập trung
- cần security boundary rõ hơn ở lớp edge

Nếu chưa có các nhu cầu đó, giữ Kong ở mức gateway cơ bản là đủ.

---

## 11. Khuyến nghị rõ ràng cho project hiện tại

Ở hiện trạng bây giờ, mình khuyến nghị:

- **nên thử Kong ở local PoC**
- **chưa cần đưa Kong thành hạng mục ưu tiên cao hơn feature business**
- **chưa cần chuyển auth từ Next BFF sang Kong**
- **chưa cần nhét business authorization vào Kong**

Lý do:

- auth hiện tại đã có luồng khá tốt ở Next.js + backend
- backend đã có Spring Security + JWT + RBAC
- nếu ép Kong gánh quá nhiều quá sớm, bạn sẽ tăng complexity trước khi có lợi ích tương ứng

---

## 12. Ví dụ flow request trong project này

### Flow hiện tại

Ví dụ người dùng mở màn hình danh sách sảnh cưới:

```text
Browser
  -> gọi frontend
  -> frontend Next.js xử lý route /api/backend/halls
  -> Next.js đọc cookie, gắn token nếu cần
  -> gọi Spring Boot /api/halls
  -> trả dữ liệu về UI
```

### Flow sau khi thêm Kong

```text
Browser
  -> gọi frontend
  -> frontend Next.js xử lý route /api/backend/halls
  -> Next.js đọc cookie, gắn token nếu cần
  -> gọi Kong /api/halls
  -> Kong forward sang Spring Boot /api/halls
  -> trả dữ liệu về Next.js
  -> trả về UI
```

Điểm mấu chốt là:

- UI không cần biết Kong tồn tại
- auth flow ở browser không bị đảo lộn
- Kong được học và áp dụng theo kiểu ít rủi ro

---

## 13. PoC local nên làm như thế nào?

Mục tiêu PoC là đơn giản hóa tối đa để bạn hiểu được đường đi của request.

## 13.1. Docker Compose minh họa

Ví dụ tối giản:

```yaml
version: "3.9"

services:
  kong:
    image: kong:3.7
    container_name: wedding-kong
    environment:
      KONG_DATABASE: "off"
      KONG_DECLARATIVE_CONFIG: /usr/local/kong/declarative/kong.yml
      KONG_PROXY_ACCESS_LOG: /dev/stdout
      KONG_ADMIN_ACCESS_LOG: /dev/stdout
      KONG_PROXY_ERROR_LOG: /dev/stderr
      KONG_ADMIN_ERROR_LOG: /dev/stderr
      KONG_ADMIN_LISTEN: 0.0.0.0:8001
    ports:
      - "8000:8000"
      - "8001:8001"
    volumes:
      - ./kong.yml:/usr/local/kong/declarative/kong.yml:ro
```

## 13.2. File `kong.yml` minh họa

```yaml
_format_version: "3.0"

services:
  - name: wedding-backend
    url: http://host.docker.internal:8082
    routes:
      - name: wedding-api
        paths:
          - /api
        strip_path: false
```

Ý nghĩa:

- Kong lắng nghe ở `http://localhost:8000`
- mọi request bắt đầu bằng `/api`
- sẽ được forward sang backend `http://localhost:8082`

Với local Windows + Docker Desktop, `host.docker.internal` thường là cách dễ nhất để container Kong gọi vào backend chạy trên máy host.

---

## 14. Frontend sẽ đổi gì nếu muốn thử?

Ở repo frontend, hiện tại base URL backend đang trỏ về `http://localhost:8082`.

Nếu muốn thử Kong theo hướng an toàn, về mặt ý tưởng bạn chỉ cần đổi:

```text
BACKEND_API_BASE_URL=http://localhost:8000
```

Khi đó:

- Next.js vẫn gọi `BACKEND_API_BASE_URL`
- nhưng đích đến sẽ là Kong thay vì Spring Boot trực tiếp

Đây là lý do cách tiếp cận này rất hợp để PoC:

- ít thay đổi code
- dễ rollback
- dễ so sánh trước/sau

---

## 15. Checklist bắt đầu từ đâu

Nếu bạn muốn bắt đầu ngay mà không bị ngợp, cứ đi theo đúng thứ tự này:

1. Dựng Kong local bằng Docker.
2. Tạo `kong.yml` route toàn bộ `/api/*` sang Spring Boot `:8082`.
3. Đổi `BACKEND_API_BASE_URL` của frontend sang `http://localhost:8000`.
4. Chạy lại frontend và backend.
5. Test một API đọc dữ liệu như `GET /api/halls` hoặc `GET /api/bookings`.
6. Kiểm tra log của Kong để thấy request đã đi qua gateway.
7. Xác nhận auth flow hiện tại vẫn hoạt động bình thường.

Đừng bắt đầu bằng:

- tách nhiều service ngay
- chuyển hết auth sang Kong ngay
- cài quá nhiều plugin ngay từ đầu

---

## 16. Ưu điểm và chi phí trong project này

| Góc nhìn | Nội dung |
| --- | --- |
| Ưu điểm | Có entry point chung, dễ thêm logging/rate limiting, chuẩn bị cho tương lai nhiều service hoặc nhiều client |
| Chi phí | Thêm một thành phần phải chạy, phải config, phải monitor, phải debug khi request lỗi |
| Khi nào nên dùng | Khi team muốn học đúng API Gateway, cần edge policy chung, hoặc chuẩn bị scale kiến trúc |
| Khi nào chưa cần | Khi backend vẫn là 1 service, team đang dồn lực cho business feature, và auth/BFF hiện tại đã đủ tốt |

---

## 17. Kong trong project này nên là gì và không nên là gì

### Nên là

- lớp gateway hạ tầng đứng trước backend
- nơi gom concern dùng chung
- bước chuẩn bị cho tương lai hệ thống lớn hơn
- công cụ học kiến trúc edge/gateway một cách an toàn

### Không nên là

- nơi chứa business logic
- nơi thay thế toàn bộ auth flow hiện tại quá sớm
- nơi thay backend chịu trách nhiệm RBAC nghiệp vụ
- hạng mục khiến team chậm delivery tính năng cốt lõi

---

## 18. Kết luận cuối cùng

Với workspace hiện tại, Kong Gateway **có tính phù hợp**, nhưng phù hợp nhất theo hướng:

- dựng PoC local
- đặt Kong giữa Next.js server và Spring Boot backend
- dùng để học, chuẩn hóa entry point, và chuẩn bị cho tương lai

Nó **chưa phải** thứ cần ưu tiên cao hơn các chức năng business nếu team vẫn đang hoàn thiện core frontend/backend.

Nếu bạn là người mới, cách bắt đầu tốt nhất là:

1. hiểu vai trò Kong
2. dựng một PoC route `/api/*`
3. cho Next.js gọi qua Kong
4. quan sát log, request flow, và tác động thực tế

Đi như vậy sẽ giúp bạn học đúng bản chất của API Gateway mà không phá kiến trúc đang hoạt động ổn của project này.
