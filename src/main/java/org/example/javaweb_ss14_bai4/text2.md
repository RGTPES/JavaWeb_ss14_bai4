
# Phan 2: So sanh va Lua chon

## 1. Bang so sanh

| Tieu chi | Giai phap 1: Transaction + Pending | Giai phap 2: Scheduled Task |
|---|---|---|
| Toc do xu ly | Nhanh, vi transaction ngan | Kha nhanh, nhung phu thuoc tan suat quet |
| Do an toan du lieu | Tot neu co rollback va lock dung | Rat tot neu job xu ly dung transaction |
| Tai nguyen bo nho | It ton tai nguyen | Ton them tai nguyen cho job nen |
| Kha nang chong deadlock | Tot vi khong giu transaction lau | Tot vi xu ly tung don het han rieng |
| Xu ly don bi bo quen | Chua tot neu khong co job ho tro | Tot, tu dong quet don het han |
| Bao tri code | De bao tri hon | Phuc tap hon |
| Phu hop he thong truy cap cao | Trung binh den tot | Tot hon |

---

## 2. Lua chon giai phap

Giai phap phu hop nhat la:

Ket hop Giai phap 1 va Giai phap 2.

Cu the:

- Khi checkout, dung Transaction ngan de tru stock tam thoi va tao order PENDING
- Luu expiredAt = now + 15 phut
- Khi thanh toan thanh cong, cap nhat order thanh PAID
- Neu qua 15 phut, Scheduled Task tu dong cap nhat order thanh EXPIRED va hoan stock

---

## 3. Ly do lua chon

Day la giai phap phu hop voi E-commerce co luong truy cap cao vi:

- Khong giu transaction trong 15 phut
- Giam nguy co deadlock
- Dam bao hang duoc giu tam thoi
- Tu dong hoan kho neu khach khong thanh toan
- Khong phu thuoc vao session cua khach hang
- Xu ly duoc truong hop san pham bi xoa bang cach dung soft delete

---

## 4. Ket luan

Khong nen dung long-running transaction de giu hang trong 15 phut.

Thay vao do, nen thiet ke theo mo hinh:

Order PENDING + expiredAt + Scheduled Task hoan kho

Day la cach thiet ke an toan, de mo rong va phu hop voi he thong thuong mai dien tu thuc te.