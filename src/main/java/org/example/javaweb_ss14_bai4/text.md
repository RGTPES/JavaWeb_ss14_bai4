# He thong Quan ly Gio hang tam va Dong bo Kho

## Phan 1: Phan tich va De xuat giai phap

## 1. Phan tich bai toan I/O

### Input

- customerId: ma khach hang
- productId: ma san pham
- quantity: so luong san pham muon mua
- checkoutTime: thoi diem khach hang checkout
- expiredTime: thoi gian het han giu hang, mac dinh la 15 phut

### Output

Truong hop thanh cong:

- Tao don hang tam voi trang thai PENDING
- Tru so luong san pham khoi stock kha dung
- Giu hang trong 15 phut

Truong hop thanh toan thanh cong trong 15 phut:

- Cap nhat don hang thanh PAID
- So luong da giu duoc tru vinh vien khoi kho

Truong hop qua 15 phut hoac khach huy don:

- Cap nhat don hang thanh CANCELLED hoac EXPIRED
- Hoan lai so luong san pham vao kho

Truong hop loi:

- Neu khong du hang thi bao "Het hang"
- Neu san pham khong ton tai thi bao "San pham khong ton tai"
- Neu don hang da het han thi bao "Don hang da het han"

---

## 2. Van de can xu ly

Neu dung Transaction keo dai 15 phut de giu hang thi se gay nguy hiem:

- Database bi khoa qua lau
- Tang nguy co deadlock
- Lam cham he thong
- Khong phu hop voi he thong E-commerce nhieu nguoi dung

Vi vay, khong nen giu Transaction trong 15 phut.

Cach dung dung la:

- Transaction chi chay ngan trong luc tao don tam
- Luu trang thai don hang la PENDING
- Luu thoi gian het han
- Sau do dung co che quet tu dong de hoan kho neu qua han

---

## 3. Giai phap 1: Database Transaction ket hop trang thai Pending

### Y tuong

Khi khach checkout:

1. Bat dau Transaction
2. Kiem tra ton kho
3. Neu du hang thi tru stock tam thoi
4. Tao order voi status = PENDING
5. Luu expiredAt = now + 15 phut
6. Commit Transaction

Neu khach thanh toan thanh cong:

1. Bat dau Transaction
2. Kiem tra order con PENDING va chua het han
3. Cap nhat order thanh PAID
4. Commit

Neu khach huy:

1. Bat dau Transaction
2. Cap nhat order thanh CANCELLED
3. Hoan stock
4. Commit

### Uu diem

- Don gian, de hieu
- Dam bao tinh toan ven du lieu
- Khong giu Transaction qua lau

### Nhuoc diem

- Neu khach khong thanh toan va khong bam huy, can co co che khac de hoan kho
- Can them cot status va expiredAt

---

## 4. Giai phap 2: Scheduled Task quet don het han va hoan kho

### Y tuong

He thong tao don tam voi status = PENDING va expiredAt.

Sau do, mot Scheduled Task chay dinh ky, vi du moi 1 phut:

1. Tim cac order co status = PENDING
2. Dieu kien expiredAt < now
3. Voi moi order het han:
  - Bat dau Transaction
  - Kiem tra product con ton tai
  - Neu product con ton tai thi cong lai stock
  - Cap nhat order thanh EXPIRED
  - Commit

### Xu ly bay du lieu

Neu san pham da bi xoa khoi danh muc:

- Khong nen xoa cung ban ghi product trong database
- Nen dung soft delete, vi du status = DELETED
- Van co the tim product de hoan kho

Neu session khach hang timeout:

- Khong phu thuoc vao session
- Dua vao order.expiredAt trong database
- Scheduled Task van tu dong hoan kho duoc

### Uu diem

- Phu hop he thong lon
- Tu dong xu ly don het han
- Khong phu thuoc vao session nguoi dung
- Tranh deadlock do khong giu transaction lau

### Nhuoc diem

- Code phuc tap hon
- Can viet job quet dinh ky
- Co the co do tre nho, vi du het han 15 phut nhung job chay sau do 1 phut moi xu ly

---

## 5. Giai phap ve Isolation Level

Co the can nhac:

### Read Committed

- Chi doc du lieu da commit
- Toc do nhanh
- Phu hop he thong co nhieu truy cap
- Can ket hop locking de tranh ban lo

### Repeatable Read

- Dam bao du lieu doc trong transaction khong thay doi
- An toan hon Read Committed
- Co the lam tang lock va giam hieu nang

Trong bai toan nay, nen uu tien:

- Read Committed
- Ket hop Pessimistic Lock hoac Optimistic Lock khi cap nhat stock