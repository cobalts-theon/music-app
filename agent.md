# Cinder's Soul - Agent Guide

## Tổng quan
Cinder's Soul là dự án nghe nhạc gồm:
- **Android app** trong thư mục `app/`
- **Backend API** trong thư mục `backend/`

Backend dùng **Node.js + Express + Sequelize + MySQL + JWT**.

## Cấu trúc chính
- `app/`: client Android
- `backend/src/routes`: định nghĩa API routes
- `backend/src/controllers`: xử lý nghiệp vụ
- `backend/src/models`: Sequelize models và quan hệ dữ liệu
- `backend/src/middleware`: auth, validate, upload, error handler
- `backend/uploads`: nơi lưu ảnh upload

## Chạy backend nhanh
1. `cd backend`
2. `npm install`
3. Tạo/cập nhật file `.env` (DB, JWT, upload config)
4. `npm run dev`

Health check: `GET http://localhost:3000/health`

## Chức năng backend hiện có
### 1. Authentication
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/auth/me`

### 2. Users
- `GET /api/users/:id`
- `PUT /api/users/:id`
- `DELETE /api/users/:id`

### 3. Songs
- `GET /api/songs` (filter/search/pagination)
- `GET /api/songs/:id` (tăng `play_count`)
- `POST /api/songs`
- `PUT /api/songs/:id`
- `DELETE /api/songs/:id`

### 4. Artists
- `GET /api/artists`
- `GET /api/artists/:id`
- `POST /api/artists`
- `PUT /api/artists/:id`
- `DELETE /api/artists/:id`

### 5. Albums
- `GET /api/albums`
- `GET /api/albums/:id`
- `POST /api/albums`
- `PUT /api/albums/:id`
- `DELETE /api/albums/:id`

### 6. Playlists
- `GET /api/playlists` (cần đăng nhập)
- `GET /api/playlists/:id` (hỗ trợ public playlist với optional auth)
- `POST /api/playlists`
- `PUT /api/playlists/:id`
- `DELETE /api/playlists/:id`
- `POST /api/playlists/:id/songs`
- `DELETE /api/playlists/:id/songs/:songId`

### 7. Favorites
- `GET /api/favorites`
- `POST /api/favorites/:songId`
- `DELETE /api/favorites/:songId`

### 8. Upload
- `POST /api/upload/image` (Multer, chỉ nhận ảnh)

## Quy ước xử lý
- Response thường theo dạng: `success`, `message`, `data`
- Endpoint protected yêu cầu `Authorization: Bearer <access_token>`
- Dùng `AppError` + `errorHandler` để trả lỗi nhất quán

## Ghi chú cập nhật gần đây
- Sửa mismatch favorites: `songId` lấy từ param route (và vẫn tương thích body).
- Sửa users update/delete theo `:id`, có check quyền chỉ cho chính chủ.
- Mở truy cập playlist public ở `GET /api/playlists/:id` bằng optional auth.
