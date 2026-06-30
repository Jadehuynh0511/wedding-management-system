# Kong Gateway Chuẩn Production Cho Project Wedding Management

## 1. Mục tiêu của tài liệu này

File trước đã nói về `PoC` và cách bắt đầu học Kong theo hướng an toàn.

File này trả lời câu hỏi lớn hơn:

> Nếu sau này team muốn chạy Kong Gateway theo kiểu **chuẩn production**, thì nên setup như thế nào?

Tài liệu này không cố biến bạn thành chuyên gia Kong ngay lập tức. Mục tiêu là giúp bạn hiểu:

- production-grade Kong khác gì so với PoC
- một kiến trúc production hợp lý nên có những thành phần nào
- với project này thì nên chọn kiểu triển khai nào
- checklist thực tế để không bỏ sót các phần quan trọng

---

## 2. "Chuẩn production" nghĩa là gì?

Một Kong Gateway chạy được ở local chưa có nghĩa là đã sẵn sàng cho production.

`Production-ready` thường có các đặc điểm sau:

- không có 1 điểm chết duy nhất (`single point of failure`)
- có bảo mật cho luồng quản trị
- có quan sát hệ thống: log, metrics, health check, alert
- có cách quản lý cấu hình ổn định và có thể lặp lại
- có backup và kế hoạch khôi phục
- có chiến lược scale
- có chiến lược upgrade
- có ranh giới trách nhiệm rõ ràng giữa gateway, frontend, và backend

Nói ngắn gọn: production không chỉ là "chạy được", mà là "chạy ổn, vận hành được, sửa được, nâng cấp được".

---

## 3. Với Kong, production có mấy kiểu triển khai?

Theo tài liệu chính thức của Kong Gateway, có 4 mode lớn:

- `Konnect`
- `Hybrid`
- `Traditional (database mode)`
- `DB-less`

Nhưng nếu nhìn dưới góc độ production cho project này, bạn có thể đơn giản hóa như sau:

### 1. Konnect

Kong host giúp bạn phần control plane. Team của bạn chỉ cần chạy data plane.

Phù hợp khi:

- muốn giảm gánh nặng vận hành
- muốn vào production nhanh hơn
- chấp nhận dùng control plane managed bởi Kong

### 2. Self-managed Hybrid

Bạn tự chạy:

- `Control Plane (CP)`
- `Data Plane (DP)`
- `PostgreSQL` cho CP

Phù hợp khi:

- muốn tự host toàn bộ
- cần kiểm soát hạ tầng sâu hơn
- team chấp nhận vận hành nhiều thành phần hơn

### 3. Traditional mode

Các node Kong cùng đọc/ghi database.

Mode này vẫn dùng được, nhưng nếu team đang nghĩ theo hướng production hiện đại, phân tách control plane và data plane thường dễ quản trị hơn về lâu dài.

### 4. DB-less

Dùng file cấu hình khai báo thay vì database.

Phù hợp với:

- cấu hình nhỏ
- ít thay đổi
- use case đơn giản

Với project này, nếu đi production nghiêm túc và muốn mở rộng dần, `DB-less` thường không phải lựa chọn mình ưu tiên đầu tiên.

---

## 4. Khuyến nghị cho project này

## Khuyến nghị ngắn

Nếu muốn production "đúng bài", có 2 hướng hợp lý:

1. `Konnect` nếu muốn giảm chi phí vận hành.
2. `Self-managed Hybrid` nếu muốn tự host hoàn toàn.

### Mình khuyến nghị thế nào?

Với team mới tiếp cận Kong, cách dễ hiểu nhất là:

- `PoC / staging`: có thể bắt đầu local và môi trường test đơn giản
- `production thật`: ưu tiên nghĩ theo `Hybrid` hoặc `Konnect`, không nên dừng ở mô hình 1 container Kong đứng một mình

### Vì sao không nên lấy PoC làm production?

PoC thường chỉ có:

- 1 node Kong
- 1 file config
- ít hoặc không có monitoring
- chưa khóa chặt Admin API
- chưa có HA

Nếu đưa nguyên xi lên production, chỉ cần:

- node chết
- config lỗi
- admin port lộ ra ngoài
- không có backup

là bạn sẽ gặp rủi ro lớn ngay.

---

## 5. Kiến trúc production dễ hiểu nhất cho project này

Nếu tự host, kiến trúc nên nghĩ theo kiểu:

```text
Internet / Internal Clients
  -> Load Balancer
      -> Kong Data Plane 1
      -> Kong Data Plane 2
      -> Kong Data Plane N

Kong Data Planes
  -> nhận cấu hình từ Kong Control Plane

Kong Control Plane 1
Kong Control Plane 2
  -> dùng PostgreSQL cho metadata/config

PostgreSQL
  -> backup định kỳ

Observability stack
  -> logs
  -> metrics
  -> alerts
```

Nếu map về project Wedding Management, luồng có thể là:

```text
Browser
  -> Next.js frontend
      -> Kong Data Plane cluster
          -> Spring Boot backend
```

Trong đó:

- Browser vẫn đi vào frontend như hiện tại
- frontend vẫn giữ BFF/auth flow
- Kong chịu trách nhiệm gateway layer
- backend vẫn giữ business logic + JWT/RBAC

Đây là điểm rất quan trọng:

> Production-grade Kong không nên phá vỡ luồng auth hiện tại của project này chỉ để "đúng gateway".

---

## 6. Tại sao production nên ưu tiên Hybrid hoặc Konnect?

### Lý do 1: Tách quản trị khỏi luồng traffic

Trong `Hybrid mode`, Kong tách:

- `Control Plane`: nơi quản lý cấu hình, có Admin API
- `Data Plane`: nơi nhận traffic thật của user

Điều này tốt hơn production vì:

- Admin API không cần nằm trên node đang trực tiếp hứng traffic
- team dễ kiểm soát bề mặt tấn công hơn
- scale traffic chỉ cần scale data plane

### Lý do 2: Data plane vẫn phục vụ traffic khi control plane tạm lỗi

Hybrid mode cho phép data plane tiếp tục phục vụ với cấu hình đã nhận gần nhất, ngay cả khi control plane tạm thời không kết nối được.

Đây là một đặc tính rất hợp production.

### Lý do 3: Dễ mở rộng

Khi traffic tăng, bạn chủ yếu scale `DP`.

Bạn không cần nghĩ kiểu:

- mọi node vừa proxy vừa quản trị vừa nói chuyện trực tiếp với database

### Lý do 4: Phù hợp với tương lai nhiều client

Sau này nếu có:

- web admin
- mobile app
- đối tác tích hợp
- public API

thì gateway layer tách riêng sẽ có giá trị rõ hơn nhiều.

---

## 7. Một production setup tối thiểu nên có gì?

Đây là baseline mình khuyến nghị nếu bạn tự host Kong production.

### Tầng traffic

- 1 load balancer đứng trước Kong DP
- tối thiểu 2 Kong DP để có HA
- TLS ở edge hoặc tại load balancer

### Tầng quản trị

- tối thiểu 2 Kong CP nếu cần HA cho quản trị
- Admin API không public ra Internet
- chỉ cho phép mạng nội bộ hoặc VPN/bastion truy cập

### Tầng cấu hình

- PostgreSQL riêng cho Kong CP
- không dùng database chung bừa bãi với ứng dụng business
- có backup định kỳ

### Tầng vận hành

- metrics
- logs tập trung
- health probes
- alert khi DP chết, CP mất kết nối, DB lỗi, hoặc latency tăng mạnh

### Tầng cấu hình triển khai

- dùng `decK` hoặc GitOps-style config management
- không chỉnh tay production quá nhiều qua UI/Admin API rồi quên đồng bộ lại

---

## 8. Những thành phần quan trọng trong production

## 8.1. Load Balancer trước Data Plane

Production nên có load balancer đứng trước các DP.

Vai trò:

- phân phối traffic
- health check node Kong
- loại node lỗi khỏi rotation
- hỗ trợ scale ngang

Nếu không có LB, chỉ cần 1 DP chết là traffic lỗi ngay.

## 8.2. Nhiều Data Plane

Ít nhất nên có:

- `2 DP` cho môi trường production nhỏ

Nếu traffic lớn hơn thì scale thêm.

DP là nơi trực tiếp proxy request, nên đây là phần thường được scale đầu tiên.

## 8.3. Control Plane tách riêng

Trong self-managed hybrid:

- CP giữ Admin API và cấu hình
- DP không phơi Admin API để nhận traffic công khai

Đây là một trong những khác biệt lớn nhất giữa production-grade setup và PoC setup.

## 8.4. PostgreSQL cho Kong

Nếu dùng hybrid/traditional:

- Kong cần PostgreSQL để lưu cấu hình và metadata quản trị

Nên xem PostgreSQL này là một thành phần production thực thụ:

- có backup
- có monitoring
- có giới hạn truy cập
- có kế hoạch restore

## 8.5. mTLS giữa Control Plane và Data Plane

Trong hybrid mode, Kong dùng `mTLS` để bảo vệ giao tiếp giữa CP và DP.

Điểm này rất quan trọng, vì production không nên để CP/DP nói chuyện "trần" với nhau.

Bạn nên chuẩn bị:

- CA hoặc quy trình cấp cert
- vòng đời cert
- quy trình rotate cert

## 8.6. Admin API phải được khóa chặt

Admin API là nơi có toàn quyền với Kong.

Production chuẩn cần áp nguyên tắc:

- không public Admin API ra Internet
- nếu buộc phải expose thì phải có auth rất chặt
- giới hạn truy cập bằng network policy, firewall, security group, private subnet, VPN, bastion

Nói đơn giản:

> Proxy port có thể public cho client.
> Admin port thì mặc định nên xem là private.

## 8.7. Status API và Health Check

Production nên bật `Status API` theo cách an toàn để:

- hệ monitoring scrape metrics
- load balancer / orchestrator kiểm tra sống-chết
- readiness check trước khi đưa node vào phục vụ traffic

---

## 9. Cấu hình production nên được quản lý như thế nào?

Một lỗi rất thường gặp là:

- ban đầu cấu hình bằng file
- sau đó chỉnh tay qua Admin API
- rồi không biết cấu hình thật ở đâu

Với production, bạn nên chọn một nguồn sự thật chính (`source of truth`).

## Khuyến nghị thực tế

Ưu tiên:

- lưu cấu hình Kong trong Git
- dùng `decK` để `validate`, `diff`, `sync`

Lợi ích:

- biết ai đổi gì
- dễ review
- dễ dựng lại môi trường
- dễ rollback

### Một flow đơn giản, lành mạnh

```text
Git repo cấu hình Kong
  -> CI/CD
      -> decK validate
      -> decK diff
      -> decK sync
          -> Control Plane Admin API
              -> Data Planes nhận config mới
```

Với team mới, đây là cách rất đáng học sớm vì nó giúp tránh tình trạng production drift.

---

## 10. Bảo mật production cần chú ý gì?

## 10.1. Không public Admin API

Đây là nguyên tắc số 1.

Nếu ai đó có quyền gọi Admin API, họ gần như có quyền điều khiển gateway.

## 10.2. TLS ở mọi chỗ quan trọng

Ít nhất nên có:

- TLS cho traffic client -> gateway
- TLS hoặc private network cho gateway -> upstream
- mTLS cho control plane <-> data plane trong hybrid mode

## 10.3. Tách mạng

Nên tách rõ:

- public subnet / edge cho DP
- private network cho CP
- private network cho PostgreSQL

## 10.4. Quyền truy cập tối thiểu

Chỉ cấp quyền cho đúng người, đúng service:

- CI/CD được quyền apply config
- operator được quyền quản trị
- monitoring chỉ được scrape endpoint cần thiết

## 10.5. Secrets management

Không hardcode:

- token
- password
- certificate key
- DB credential

Nên lấy từ:

- secret manager
- vault
- Kubernetes secret
- hoặc cơ chế secret an toàn tương đương

---

## 11. Monitoring và observability production nên có gì?

Production mà không có observability thì rất khó vận hành.

Tối thiểu nên có:

- access log tập trung
- error log tập trung
- metrics từ Kong
- health check node
- alerting

### Những gì cần theo dõi

- request rate
- latency
- tỷ lệ lỗi `4xx/5xx`
- upstream health
- CP/DP connectivity
- tình trạng PostgreSQL
- số node DP đang sẵn sàng

### Hướng làm dễ hiểu

- bật Prometheus plugin hoặc metrics scraping phù hợp
- gom log về ELK, Loki, Datadog, hoặc hệ log tập trung team đang dùng
- dựng dashboard cơ bản trước, không cần quá cầu kỳ

Production tốt không nhất thiết phải có dashboard đẹp ngay. Nhưng nhất định phải biết:

- gateway có đang sống không
- traffic có đang lỗi không
- lỗi nằm ở Kong hay upstream

---

## 12. Backup và disaster recovery

Production chuẩn không chỉ nghĩ đến chạy, mà còn nghĩ đến lúc hỏng.

## Tối thiểu nên backup gì?

- PostgreSQL của Kong
- cấu hình khai báo của Kong trong Git
- certificate / key materials theo quy trình an toàn

## Vì sao cần cả database backup lẫn declarative/Git backup?

Vì chúng phục vụ 2 kiểu khôi phục khác nhau:

- database backup: khôi phục nhanh trạng thái dữ liệu
- config trong Git / decK dump: tái dựng có kiểm soát, dễ audit

## Cần có runbook

Team nên viết sẵn:

- nếu mất 1 DP thì làm gì
- nếu mất toàn bộ DP thì scale lại thế nào
- nếu CP lỗi thì traffic còn phục vụ được bao lâu và khôi phục ra sao
- nếu PostgreSQL lỗi thì restore thế nào

Nếu chưa có runbook, production vẫn đang thiếu một mảnh quan trọng.

---

## 13. Scale production như thế nào?

### Scale traffic

Scale `Data Plane`.

Đây là scale tự nhiên nhất khi:

- request tăng
- nhiều route hơn
- latency tăng do proxy load cao

### Scale quản trị

Scale `Control Plane` khi:

- cần HA cho control plane
- cần nhiều admin/config operations hơn
- cần khả năng chịu lỗi tốt hơn ở tầng quản trị

### Scale upstream awareness

Đừng chỉ scale Kong mà quên backend Spring Boot.

Nếu Kong khỏe hơn nhưng backend vẫn là nút cổ chai, user vẫn sẽ thấy lỗi.

Với project này, Kong luôn phải được nhìn như 1 phần trong chuỗi:

```text
Frontend -> Gateway -> Backend -> Database
```

Không có mắt xích nào được bỏ quên.

---

## 14. Upgrade production nên nghĩ như thế nào?

Upgrade Kong không nên làm kiểu:

- SSH vào server
- thay version
- restart
- cầu nguyện

Production chuẩn nên có:

- review changelog và breaking changes
- backup trước upgrade
- môi trường staging giống production đủ để test
- rollout có kiểm soát

### Tư duy an toàn

- nâng cấp từng bước nhỏ nếu có thể
- giữ version gap ngắn
- test config bằng staging trước
- chuẩn bị rollback plan

Nếu bạn dùng self-managed hybrid:

- control plane upgrade cần được chuẩn bị kỹ
- data plane rollout nên theo cụm và có health check

---

## 15. Kong production trong project này nên giữ trách nhiệm đến đâu?

Đây là phần rất quan trọng để không bị over-engineer.

### Kong nên chịu trách nhiệm cho

- routing
- TLS termination hoặc edge proxying
- rate limiting cơ bản
- logging / observability ở lớp gateway
- request policies dùng chung

### Kong không nên ôm quá sớm

- business authorization chi tiết của wedding domain
- logic đăng nhập / refresh token vốn đang hợp lý ở Next.js BFF
- domain validation của backend

### Với project hiện tại

Cách phân vai hợp lý nhất vẫn là:

- `Next.js`: UI + BFF + auth flow phía web
- `Kong`: gateway/edge layer
- `Spring Boot`: business logic + security nghiệp vụ + RBAC

---

## 16. Một blueprint production dễ hình dung

Nếu team muốn một blueprint ngắn gọn để hình dung, có thể dùng mô hình sau:

```text
Public traffic
  -> External Load Balancer
      -> Kong DP 1
      -> Kong DP 2

Private management network
  -> Kong CP 1
  -> Kong CP 2
  -> PostgreSQL

Monitoring network
  -> Prometheus / Grafana
  -> Log aggregation

Application network
  -> Spring Boot backend instances
```

Nếu đặt trong cloud, thường sẽ có:

- public LB
- private subnet cho CP và DB
- auto scaling hoặc rolling deployment cho DP
- security group / firewall rule tách lớp

---

## 17. Một checklist production-ready dễ dùng

Trước khi nói "Kong này đã lên production", hãy tự check:

### Kiến trúc

- Có ít nhất 2 DP chưa?
- Có load balancer trước DP chưa?
- Nếu self-managed hybrid, CP và DP đã tách vai trò chưa?

### Bảo mật

- Admin API đã private chưa?
- CP/DP đã bảo vệ bằng mTLS chưa?
- TLS client-facing đã sẵn sàng chưa?
- Secrets có nằm ngoài source code không?

### Vận hành

- Có metrics chưa?
- Có log tập trung chưa?
- Có health check và alert chưa?
- Có dashboard cơ bản chưa?

### Cấu hình

- Có Git làm source of truth chưa?
- Có `decK validate/diff/sync` chưa?
- Có tránh chỉnh tay production lung tung chưa?

### Độ bền hệ thống

- Có backup PostgreSQL chưa?
- Có backup cấu hình chưa?
- Có runbook restore chưa?
- Có test failover tối thiểu chưa?

### Release

- Có staging để test chưa?
- Có plan upgrade/rollback chưa?
- Có kiểm tra tương thích version chưa?

Nếu còn thiếu nhiều ô ở trên, thì hệ thống chưa nên được gọi là production-grade.

---

## 18. Nên bắt đầu từ đâu nếu muốn đi từ PoC lên production?

Mình khuyến nghị theo thứ tự này:

1. Hoàn tất PoC local như file trước.
2. Dựng một môi trường `staging` gần production hơn.
3. Chuyển từ 1 Kong node sang mô hình có `LB + nhiều DP`.
4. Chọn giữa `Konnect` và `self-managed hybrid`.
5. Khóa Admin API và chuẩn hóa TLS/secrets.
6. Đưa cấu hình vào Git + `decK`.
7. Bật monitoring, logging, health probes.
8. Viết backup/restore runbook.
9. Test rollout, restart, và failover.
10. Chỉ sau đó mới cân nhắc production thật.

Đi theo hướng này sẽ chắc hơn rất nhiều so với việc nhảy thẳng từ local demo lên production.

---

## 19. Kết luận ngắn gọn

Một Kong Gateway "chuẩn production" không phải là:

- 1 container Kong chạy được
- có route hoạt động
- test `curl` thấy trả `200`

Mà phải là:

- có kiến trúc đúng
- có HA
- có bảo mật
- có observability
- có quản lý cấu hình chuẩn
- có backup và upgrade plan

Với project Wedding Management, nếu đi production nghiêm túc thì hướng dễ hiểu và đúng đắn nhất là:

- vẫn giữ `Next.js BFF` cho web auth flow
- dùng Kong như lớp gateway riêng
- ưu tiên `Konnect` hoặc `self-managed hybrid`
- không mang nguyên mô hình PoC một node lên production

Nếu cần chốt một câu duy nhất:

> PoC giúp bạn học Kong.
> Production bắt bạn học cả vận hành, bảo mật, HA, và kỷ luật quản lý cấu hình.

---

## 20. Tài liệu chính thức nên đọc thêm

Nếu muốn đi sâu hơn, nên đọc các tài liệu chính thức sau của Kong:

- Deployment topologies overview
- Hybrid mode overview và setup
- Securing the Admin API
- Configuration reference
- decK gateway management
- Prometheus monitoring
- Health check probes
- Backup and restore
- Upgrade guides
