# PHẦN 1 - PHÂN TÍCH & ĐỀ XUẤT

## 1. Phân tích I/O

### 1.1. Input
Hệ thống nhận các dữ liệu đầu vào sau:

- `productId`: mã sản phẩm
- `customerId`: mã khách hàng
- `quantity`: số lượng khách muốn mua
- `checkoutTime`: thời điểm bắt đầu checkout
- `paymentStatus`: trạng thái thanh toán (`SUCCESS`, `FAILED`, `PENDING`, `CANCELLED`)
- `orderStatus`: trạng thái đơn hàng (`PENDING`, `PAID`, `EXPIRED`, `CANCELLED`)
- `reserveExpiredAt`: thời điểm hết hạn giữ hàng (checkoutTime + 15 phút)

### 1.2. Output mong đợi

#### Trường hợp 1: Thanh toán thành công trong 15 phút
- Đơn hàng chuyển từ `PENDING` → `PAID`
- Số lượng giữ tạm được chuyển thành trừ kho vĩnh viễn
- Không hoàn kho

#### Trường hợp 2: Quá 15 phút chưa thanh toán
- Đơn hàng chuyển từ `PENDING` → `EXPIRED`
- Số lượng sản phẩm được hoàn lại kho
- Người khác có thể tiếp tục mua

#### Trường hợp 3: Khách hàng hủy đơn
- Đơn hàng chuyển từ `PENDING` → `CANCELLED`
- Hệ thống hoàn kho ngay

#### Trường hợp 4: Sản phẩm đã bị xóa khỏi danh mục bán
- Nếu đơn đang giữ hàng mà hết hạn:
    - Không đưa sản phẩm quay lại trạng thái bán bình thường
    - Chỉ hoàn lại số lượng vào kho nội bộ nếu hệ thống còn quản lý tồn kho
    - Nếu sản phẩm bị xóa mềm (`inactive`, `deleted`) thì vẫn có thể cộng lại stock nhưng không hiển thị bán
    - Nếu xóa cứng khỏi database thì cần log lỗi hoặc bỏ qua hoàn kho và đánh dấu đơn xử lý ngoại lệ

#### Trường hợp 5: Session người dùng timeout
- Không phụ thuộc session để hoàn kho
- Hệ thống vẫn phải dựa vào `orderStatus`, `paymentStatus`, `reserveExpiredAt` trong database để xử lý

---

## 2. Phân tích vấn đề

Trong bài toán này, hệ thống cần giữ hàng trong 15 phút cho khách khi checkout.  
Nếu dùng một transaction kéo dài suốt 15 phút để giữ lock trên database thì sẽ gây ra các vấn đề:

- Giữ lock quá lâu
- Làm giảm hiệu năng hệ thống
- Tăng nguy cơ deadlock
- Nhiều người dùng cùng checkout sẽ gây nghẽn database
- Không phù hợp với hệ thống E-commerce có lượng truy cập cao

Vì vậy không nên dùng long-running transaction.

Giải pháp đúng là:
- Chỉ dùng transaction ngắn cho từng thao tác nhỏ
- Trạng thái giữ hàng phải được lưu bằng dữ liệu
- Việc hoàn kho phải được xử lý bằng cơ chế độc lập với session người dùng

---

## 3. Đề xuất giải pháp 1
## Sử dụng Database Transaction + trạng thái đơn hàng tạm thời (PENDING)

### 3.1. Ý tưởng
Khi khách nhấn Checkout:
- Mở transaction ngắn
- Kiểm tra stock hiện tại
- Trừ tạm stock ngay
- Tạo đơn hàng với trạng thái `PENDING`
- Lưu thời điểm hết hạn `reserveExpiredAt = now + 15 phút`
- Commit transaction ngay

Sau đó:
- Nếu thanh toán thành công trong thời gian giữ hàng:
    - Chuyển `PENDING` → `PAID`
- Nếu quá hạn hoặc hủy:
    - Chuyển `PENDING` → `EXPIRED` hoặc `CANCELLED`
    - Hoàn stock lại

### 3.2. Quy trình xử lý

#### Bước 1: Reserve hàng
- Begin transaction
- Kiểm tra sản phẩm còn đủ stock không
- Nếu đủ:
    - giảm stock
    - tạo order status = `PENDING`
    - lưu `reserveExpiredAt`
- Commit

#### Bước 2: Thanh toán thành công
- Begin transaction
- Kiểm tra order còn trạng thái `PENDING` và chưa hết hạn
- Cập nhật `orderStatus = PAID`
- Commit

#### Bước 3: Hết hạn hoặc hủy đơn
- Begin transaction
- Kiểm tra order còn `PENDING`
- Cộng lại stock
- Cập nhật `orderStatus = EXPIRED` hoặc `CANCELLED`
- Commit

### 3.3. Ưu điểm
- Dễ hiểu
- Dễ triển khai
- Không giữ transaction quá lâu
- Phù hợp với nguyên tắc ACID cho từng bước xử lý ngắn

### 3.4. Nhược điểm
- Cần thêm cơ chế khác để quét đơn quá hạn
- Nếu chỉ tạo `PENDING` mà không có job quét, hàng có thể bị giữ mãi

---

## 4. Đề xuất giải pháp 2
## Sử dụng Scheduled Task hoặc Hibernate Interceptor để quét và hoàn kho

### 4.1. Ý tưởng
Thay vì giữ transaction lâu, hệ thống:
- Lưu đơn hàng `PENDING`
- Lưu thời điểm hết hạn `reserveExpiredAt`
- Dùng Scheduled Task chạy định kỳ (ví dụ mỗi 1 phút) để quét các đơn:
    - `orderStatus = PENDING`
    - `reserveExpiredAt < now`
- Sau đó tự động hoàn kho và cập nhật trạng thái đơn

Có thể dùng:
- `@Scheduled` trong Spring
- Quartz Scheduler
- Job nền
- Hoặc Interceptor/Event Listener của Hibernate để hỗ trợ một số điểm lifecycle  
  Tuy nhiên trong thực tế, **Scheduled Task phù hợp hơn Hibernate Interceptor** cho bài toán quét hết hạn theo thời gian.

### 4.2. Quy trình xử lý

#### Bước 1: Khi checkout
- Transaction ngắn
- Trừ tạm stock
- Tạo order = `PENDING`
- Lưu thời gian hết hạn

#### Bước 2: Scheduled Task chạy nền
- Quét danh sách order:
    - `status = PENDING`
    - `reserveExpiredAt <= now`
- Với từng order:
    - kiểm tra sản phẩm còn tồn tại không
    - nếu còn thì cộng stock lại
    - cập nhật order thành `EXPIRED`
    - ghi log nếu dữ liệu bất thường

#### Bước 3: Nếu khách thanh toán trước khi job chạy
- Thanh toán đổi `PENDING` → `PAID`
- Job sẽ bỏ qua đơn này vì không còn `PENDING`

### 4.3. Trường hợp sản phẩm đã bị xóa
- Nếu xóa mềm:
    - vẫn có thể hoàn stock vào bản ghi sản phẩm
    - nhưng không cho hiển thị bán
- Nếu xóa cứng:
    - không thể cộng stock trực tiếp
    - cần ghi log lỗi
    - đánh dấu đơn thuộc nhóm dữ liệu bất thường để admin xử lý

### 4.4. Trường hợp session khách timeout
- Không ảnh hưởng
- Job quét dựa trên dữ liệu database, không phụ thuộc session web

### 4.5. Ưu điểm
- Không khóa dữ liệu lâu
- Phù hợp hệ thống lớn, nhiều người dùng
- Tự động hóa cao
- Dễ mở rộng cho nhiều máy chủ nếu dùng scheduler tập trung

### 4.6. Nhược điểm
- Phức tạp hơn giải pháp 1
- Cần xử lý race condition giữa thanh toán thành công và job hoàn kho
- Cần thêm log, retry, idempotent để tránh xử lý trùng

---

## 5. Gợi ý thêm về Isolation Level

### Read Committed
- Giảm lock
- Hiệu năng tốt hơn
- Phù hợp hệ thống có nhiều giao dịch
- Nhưng phải kiểm soát logic cập nhật cẩn thận

### Repeatable Read
- An toàn hơn khi đọc lặp lại trong transaction
- Nhưng dễ tăng lock và giảm hiệu năng hơn

### Kết luận về isolation
Trong thương mại điện tử tải cao:
- Thường ưu tiên `Read Committed`
- Kết hợp transaction ngắn + kiểm tra điều kiện cập nhật rõ ràng
- Chỉ dùng mức cô lập cao hơn khi thực sự cần

---

## 6. Kết luận phần 1

Không nên dùng long-running transaction để giữ hàng 15 phút vì sẽ gây nghẽn và deadlock.

Hai hướng hợp lý là:
1. Dùng transaction ngắn + trạng thái `PENDING`
2. Dùng transaction ngắn + Scheduled Task để quét đơn hết hạn và hoàn kho

Trong đó:
- `PENDING` là trạng thái bắt buộc gần như phải có
- Scheduled Task là cơ chế mạnh để tự động giải phóng hàng