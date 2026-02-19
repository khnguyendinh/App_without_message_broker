# Angular Frontend Dashboard for Hazelcast Sender/Receiver

Tạo giao diện Angular để quản lý và giám sát việc gửi/nhận message giữa sender-app (8081) và receiver-app (8082) qua Hazelcast.

## Proposed Changes

### Backend - Sender App (port 8081)

#### [NEW] WebConfig.java
- Add CORS configuration to allow Angular dev server (`http://localhost:4200`)

#### [MODIFY] MessageController.java
- Add `GET /api/messages/all` endpoint to return all sent messages from DB (for log panel)

---

### Backend - Receiver App (port 8082)

#### [NEW] WebConfig.java
- Add CORS configuration to allow Angular dev server

#### [NEW] MessageController.java
- `GET /api/messages/all` - return all processed messages from DB
- `GET /api/messages/status` - return app status and counts

---

### Frontend - Angular App

#### [NEW] `frontend-app/` - Angular 16 standalone project

Kiến trúc:
- **Single-page dashboard** với light theme design
- **Config section**: input số lượng message + prefix + nút Send
- **2 log panels**: Sender logs (left) + Receiver logs (right)
- **Auto-polling** mỗi 2 giây để cập nhật logs
- **Status bar**: hiển thị map size và trạng thái connection

Key files:
- `src/app/app.component.ts` - Main dashboard component
- `src/app/services/api.service.ts` - HTTP client cho cả 2 backend APIs
- `src/styles.css` - Global styles
