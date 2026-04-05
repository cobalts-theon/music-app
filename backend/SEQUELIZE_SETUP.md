# 🎵 Cinder's Soul Backend - Sequelize Models

## ✅ Đã tạo xong

### 📁 Cấu trúc Models

```
backend/src/
├── config/
│   └── database.js          # Sequelize configuration (đã update)
└── models/
    ├── index.js            # Model relationships & exports
    ├── User.js             # User model
    ├── Artist.js           # Artist model  
    ├── Album.js            # Album model
    ├── Song.js             # Song model
    ├── Playlist.js         # Playlist model
    ├── PlaylistSong.js     # Junction table (Playlist ↔ Song)
    ├── Favorite.js         # User favorites
    ├── RefreshToken.js     # JWT refresh tokens
    └── README.md           # Documentation & usage examples
```

## 🚀 Cách sử dụng

### 1. Cài đặt dependencies (nếu chưa có)

```bash
cd backend
npm install
```

Dependencies đã có:
- ✅ sequelize@^6.37.8
- ✅ mysql2@^3.20.0

### 2. Tạo file .env

Tạo file `.env` trong thư mục `backend`:

```env
PORT=3000
NODE_ENV=development

DB_HOST=localhost
DB_USER=root
DB_PASSWORD=
DB_NAME=cinders_soul
DB_PORT=3306

JWT_SECRET=cinders_soul_secret_key_2026
JWT_REFRESH_SECRET=cinders_soul_refresh_secret_2026
JWT_EXPIRES_IN=1h
JWT_REFRESH_EXPIRES_IN=7d

UPLOAD_PATH=uploads
MAX_FILE_SIZE=5242880
```

### 3. Tạo database trong XAMPP

Đã có sẵn các file SQL:
- `database/schema.sql` - Tạo cấu trúc tables
- `database/seed_real_data.sql` - Import dữ liệu thực (2 artists, 2 songs)

**Trong phpMyAdmin:**
1. Tạo database: `CREATE DATABASE cinders_soul CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
2. Chọn database `cinders_soul`
3. Import `schema.sql`
4. Import `seed_real_data.sql`

### 4. Test Models

Chạy file test:

```bash
node test-models.js
```

Output sẽ hiển thị:
- Danh sách artists
- Albums với artist
- Songs với relationships
- Users
- Playlists
- Search functionality

### 5. Sử dụng trong Controllers

```javascript
// Import models
const { Song, Artist, Album } = require('../models');

// Example: Get all songs with artist info
const songs = await Song.findAll({
  include: [
    { model: Artist, as: 'artist' },
    { model: Album, as: 'album' }
  ]
});
```

## 📊 Models và Relationships

### User Model
- Fields: id, email, password_hash, display_name, avatar_url
- Relations: hasMany Playlist, hasMany Favorite

### Artist Model  
- Fields: id, name, bio, avatar_url
- Relations: hasMany Album, hasMany Song

### Album Model
- Fields: id, title, artist_id, cover_url, release_date
- Relations: belongsTo Artist, hasMany Song

### Song Model
- Fields: id, title, artist_id, album_id, audio_url, duration, genre, lyrics, play_count
- Relations: belongsTo Artist, belongsTo Album, belongsToMany Playlist

### Playlist Model
- Fields: id, user_id, name, description, cover_url, is_public
- Relations: belongsTo User, belongsToMany Song

## 💡 Features

✅ **Sequelize ORM** - Modern database management
✅ **Auto-sync** - Database schema synchronization (dev mode)
✅ **Relationships** - Eager loading support
✅ **Indexes** - Optimized queries
✅ **Timestamps** - Auto createdAt/updatedAt
✅ **Validation** - Built-in data validation
✅ **Type-safe** - Proper data types

## 📚 Documentation

Chi tiết về cách sử dụng models xem tại:
👉 `backend/src/models/README.md`

Bao gồm:
- CRUD operations
- Relationships usage
- Search & filtering
- Aggregations
- Best practices
- Example controllers

## 🔥 Dữ liệu thực có sẵn

Sau khi import `seed_real_data.sql`:

**Artists:**
1. Sơn Tùng M-TP (Việt Nam)
2. San Holo (Netherlands)

**Albums:**
1. Sky Tour - Sơn Tùng M-TP
2. Album I - San Holo

**Songs:**
1. "Hãy Trao Cho Anh" - Sơn Tùng M-TP (240s, Pop)
2. "I WANNA SHOW YOU" - San Holo (210s, Electronic)

**Users:**
- test@example.com (password: password123)
- admin@example.com (password: password123)

## 🎯 Next Steps

1. ✅ Đã tạo xong tất cả models
2. ⏭️ Update controllers để dùng Sequelize thay vì raw SQL
3. ⏭️ Test API endpoints
4. ⏭️ Kết nối với Android app

Bạn muốn tôi giúp update controllers để dùng Sequelize không? 🚀
