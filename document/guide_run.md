# Hướng dẫn chạy dự án

## Yêu cầu hệ thống
- **Java 25**: `C:\Program Files\Java\jdk-25.0.2`
- **Maven 3.9.9**: `D:\DATA_DELL_PC\setup\Maven\apache-maven-3.9.9`
- **Node.js 16+**: cho Angular frontend

## Cấu trúc dự án

```
App_without_message_broker/
├── sender-app/          # Spring Boot - gửi message (port 8081)
├── receiver-app/        # Spring Boot - nhận message (port 8082)
├── frontend-app/        # Angular 16 - dashboard UI (port 4200)
├── data/                # H2 database files (shared)
└── document/            # Tài liệu dự án
```

## Chạy Backend

### 1. Sender App (port 8081)
```powershell
cmd /c "set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2&& D:\DATA_DELL_PC\setup\Maven\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run"
```
Chạy trong thư mục `sender-app/`

### 2. Receiver App (port 8082)
```powershell
cmd /c "set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2&& D:\DATA_DELL_PC\setup\Maven\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run"
```
Chạy trong thư mục `receiver-app/`

> **Lưu ý**: Phải start sender-app trước, đợi khởi động xong rồi mới start receiver-app.

## Chạy Frontend

### 3. Angular Dashboard (port 4200)
```powershell
cd frontend-app
npx ng serve --open
```

Mở trình duyệt tại: http://localhost:4200

## API Endpoints

### Sender App (http://localhost:8081)
| Method | Endpoint | Mô tả |
|--------|----------|--------|
| POST | `/api/messages/send?content=...` | Gửi 1 message |
| POST | `/api/messages/send-batch?count=5&prefix=Hello` | Gửi batch message |
| GET | `/api/messages/all` | Lấy tất cả message đã gửi |
| GET | `/api/messages/status` | Trạng thái app + map size |

### Receiver App (http://localhost:8082)
| Method | Endpoint | Mô tả |
|--------|----------|--------|
| GET | `/api/messages/all` | Lấy tất cả message đã nhận/xử lý |
| GET | `/api/messages/status` | Trạng thái: total, processed, pending, failed |

## Cách hoạt động

1. **Sender** gửi message vào Hazelcast IMap (`shared-messages`)
2. Hazelcast **MapStore** tự động persist xuống H2 database
3. **Receiver** lắng nghe qua `EntryAddedListener`, nhận message mới
4. Receiver xử lý message (PENDING → PROCESSING → PROCESSED)
5. **Angular dashboard** polling mỗi 2 giây để hiển thị log realtime
