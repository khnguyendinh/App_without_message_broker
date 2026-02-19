# 🧪 Hướng dẫn giả lập Receiver bị die & Recovery 5000 messages

## Mục tiêu

Chứng minh rằng khi **Receiver App bị tắt (die)**, Sender vẫn gửi được messages vào Hazelcast + H2 Database. Khi Receiver **bật lại**, nó sẽ tự động nhận và xử lý toàn bộ messages đang pending.

## Yêu cầu

- Java 25, Maven 3.9.9, Node.js 16+
- Cả 3 app đã build thành công (`mvn clean` ít nhất 1 lần)

---

## Các bước thực hiện

### Bước 1: Khởi động Sender App

Mở terminal 1, chạy:

```bash
cd sender-app
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
mvn clean spring-boot:run
```

Chờ đến khi thấy: `Started SenderApplication in xx seconds`

### Bước 2: Khởi động Receiver App

Mở terminal 2, chạy:

```bash
cd receiver-app
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
mvn clean spring-boot:run
```

Chờ đến khi thấy: `Started ReceiverApplication in xx seconds`

### Bước 3: Khởi động Dashboard (tuỳ chọn)

Mở terminal 3, chạy:

```bash
cd frontend-app
npx ng serve --open
```

Mở trình duyệt: `http://localhost:4200`

### Bước 4: Clear toàn bộ messages cũ

Trên Dashboard, bấm nút **🗑️ Clear All** để xoá sạch dữ liệu cũ.

Hoặc dùng lệnh:

```powershell
# Clear Sender
Invoke-RestMethod -Uri "http://localhost:8081/api/messages/clear" -Method DELETE

# Clear Receiver
Invoke-RestMethod -Uri "http://localhost:8082/api/messages/clear" -Method DELETE
```

Kiểm tra đã sạch:

```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/messages/status"
# Kết quả: total = 0

Invoke-RestMethod -Uri "http://localhost:8082/api/messages/status"
# Kết quả: total = 0, processed = 0
```

### Bước 5: ❌ TẮT Receiver App (giả lập die)

Quay lại **terminal 2** (đang chạy receiver-app), nhấn `Ctrl + C` để tắt.

> **Lưu ý:** Sau bước này, Receiver đã die. Chỉ còn Sender đang chạy.

Kiểm tra Receiver đã chết:

```powershell
Invoke-RestMethod -Uri "http://localhost:8082/api/messages/status"
# Kết quả: Lỗi kết nối (Connection refused)
```

### Bước 6: 📤 Gửi 5000 messages khi Receiver đang die

Trên Dashboard: Nhập **Message Count = 5000**, bấm **🚀 Send Messages**.

Hoặc dùng lệnh:

```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/messages/send-batch?count=5000&prefix=FailoverTest" -Method POST
```

> ⏱️ Quá trình gửi 5000 messages mất khoảng **10-30 giây** tuỳ cấu hình máy.

Kiểm tra Sender đã gửi thành công:

```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/messages/status"
# Kết quả mong đợi: {"app":"sender-app","total":5000}
```

> **Giải thích:** 5000 messages đã được lưu vào **Hazelcast IMap** và đồng bộ xuống **H2 Database** qua MapStore. Tuy Receiver chết nhưng dữ liệu vẫn an toàn.

### Bước 7: ✅ BẬT LẠI Receiver App

Quay lại **terminal 2**, chạy lại:

```bash
cd receiver-app
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
mvn spring-boot:run
```

> **Không cần `mvn clean`** vì code không thay đổi.

Chờ đến khi thấy: `Started ReceiverApplication in xx seconds`

### Bước 8: 🔍 Kiểm tra Receiver đã recovery

Sau khi Receiver khởi động, nó sẽ:
1. **Tham gia lại Hazelcast cluster**
2. **Recovery tự động:** `MessageProcessorService.recoverPendingMessages()` quét DB, tìm messages có status `PENDING` và xử lý từng cái

Chờ khoảng **30-60 giây** (xử lý 5000 messages), sau đó kiểm tra:

```powershell
Invoke-RestMethod -Uri "http://localhost:8082/api/messages/status"
```

**Kết quả mong đợi:**

```json
{
  "app": "receiver-app",
  "total": 5000,
  "processed": 5000,
  "pending": 0,
  "failed": 0
}
```

🎉 **Tất cả 5000 messages đều đã được xử lý thành công!**

---

## Tóm tắt luồng hoạt động

```
┌──────────────────────────────────────────────────────────┐
│ 1. Sender gửi 5000 msg → Hazelcast IMap → H2 Database   │
│    (status: PENDING)                                     │
│                                                          │
│ 2. Receiver đã die → Không xử lý message nào             │
│                                                          │
│ 3. Receiver bật lại → Quét DB tìm PENDING messages       │
│    → Xử lý từng message → Cập nhật status: PROCESSED     │
│                                                          │
│ 4. Kết quả: 5000/5000 PROCESSED, 0 PENDING, 0 FAILED    │
└──────────────────────────────────────────────────────────┘
```

## Cơ chế Recovery

Recovery hoạt động nhờ `MessageProcessorService.recoverPendingMessages()`:

```java
@PostConstruct
public void recoverPendingMessages() {
    List<SharedMessage> pendingMessages = repository.findByStatus("PENDING");
    log.info("Found {} pending messages to recover", pendingMessages.size());
    for (SharedMessage message : pendingMessages) {
        processMessage(message, messageMap);
    }
}
```

Khi Receiver khởi động, `@PostConstruct` tự động chạy, quét database tìm tất cả messages có `status = PENDING` và xử lý lại chúng.
