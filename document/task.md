# Angular Frontend for Hazelcast Sender/Receiver

## Backend Changes
- [x] Research existing API endpoints
- [x] Add REST controller to receiver-app for querying messages
- [x] Add CORS config to both sender-app and receiver-app
- [x] Add sent messages query endpoint to sender-app

## Frontend (Angular)
- [x] Create Angular project (Angular 16 standalone)
- [x] Build message dashboard UI with:
  - [x] Config input for message count
  - [x] Send button to trigger batch send
  - [x] Sender log panel (shows sent messages)
  - [x] Receiver log panel (shows received/processed messages)
  - [x] Auto-refresh / polling for logs
- [x] Fix HttpClient provider issue
- [x] Switch to light theme

## Verification
- [ ] Test send message from UI
- [ ] Verify logs appear in both panels
