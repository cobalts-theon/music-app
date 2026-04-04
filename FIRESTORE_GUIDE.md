# Hướng dẫn thiết kế Cloud Firestore cho Music App

## Tổng quan

Dự án "Cinder's Soul" là một ứng dụng nghe nhạc Android sử dụng Cloud Firestore làm cơ sở dữ liệu backend.

## Cấu trúc Database

### Collections chính:
1. **users** - Thông tin người dùng
2. **songs** - Danh sách bài hát
3. **albums** - Album nhạc
4. **artists** - Nghệ sĩ
5. **playlists** - Playlist của người dùng

### SubCollections:
- **users/{userId}/favorites** - Bài hát yêu thích
- **users/{userId}/listening_history** - Lịch sử nghe nhạc
- **songs/{songId}/comments** - Bình luận bài hát

## Bước triển khai

### Bước 1: Setup Firebase Console

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Chọn project của bạn
3. Vào **Firestore Database** → **Create database**
4. Chọn **Start in test mode** (sau đó sẽ cập nhật Security Rules)
5. Chọn **Location** gần người dùng nhất (asia-southeast1 cho Việt Nam)

### Bước 2: Deploy Security Rules

1. Cài đặt Firebase CLI:
```bash
npm install -g firebase-tools
```

2. Login và init project:
```bash
firebase login
firebase init firestore
```

3. Deploy rules và indexes:
```bash
firebase deploy --only firestore:rules
firebase deploy --only firestore:indexes
```

### Bước 3: Tạo dữ liệu mẫu

#### Tạo Artist mẫu:
```javascript
// Firebase Console → Firestore → Add document
Collection: artists
Document ID: artist_001

{
  artistId: "artist_001",
  name: "Sơn Tùng M-TP",
  bio: "Ca sĩ, rapper, nhạc sĩ người Việt Nam",
  photoUrl: "https://example.com/sontung.jpg",
  genres: ["Pop", "R&B"],
  followers: 0,
  verified: true,
  createdAt: [Firebase Timestamp - now]
}
```

#### Tạo Album mẫu:
```javascript
Collection: albums
Document ID: album_001

{
  albumId: "album_001",
  title: "Sky Tour",
  artistId: "artist_001",
  artistName: "Sơn Tùng M-TP",
  coverImageUrl: "https://example.com/skytour.jpg",
  releaseDate: [Firebase Timestamp],
  genre: ["Pop"],
  totalTracks: 12,
  songIds: ["song_001", "song_002"],
  createdAt: [Firebase Timestamp - now]
}
```

#### Tạo Song mẫu:
```javascript
Collection: songs
Document ID: song_001

{
  songId: "song_001",
  title: "Hãy Trao Cho Anh",
  artistId: "artist_001",
  artistName: "Sơn Tùng M-TP",
  albumId: "album_001",
  albumName: "Sky Tour",
  duration: 240,
  audioUrl: "https://storage.googleapis.com/your-bucket/song_001.mp3",
  coverImageUrl: "https://example.com/haytrao.jpg",
  genre: ["Pop", "R&B"],
  releaseDate: [Firebase Timestamp],
  playCount: 0,
  likeCount: 0,
  lyrics: "Hãy trao cho anh...",
  createdAt: [Firebase Timestamp - now],
  updatedAt: [Firebase Timestamp - now]
}
```

### Bước 4: Sử dụng trong Android Code

#### Khởi tạo Repository:
```kotlin
// In ViewModel or Activity
class MainActivity : ComponentActivity() {
    private val songRepository = SongRepository()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Lấy tất cả bài hát
        lifecycleScope.launch {
            songRepository.getAllSongs().onSuccess { songs ->
                // Hiển thị danh sách bài hát
                Log.d("MainActivity", "Songs: ${songs.size}")
            }.onFailure { error ->
                Log.e("MainActivity", "Error: ${error.message}")
            }
        }
    }
}
```

#### Tạo Playlist mới:
```kotlin
val playlistRepository = PlaylistRepository()

lifecycleScope.launch {
    val newPlaylist = Playlist(
        name = "My Favorite Songs",
        description = "Best songs ever",
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
        isPublic = true,
        coverImageUrl = "https://example.com/cover.jpg"
    )
    
    playlistRepository.createPlaylist(newPlaylist).onSuccess { playlistId ->
        Log.d("Playlist", "Created with ID: $playlistId")
    }
}
```

#### Thêm vào yêu thích:
```kotlin
val favoriteRepository = FavoriteRepository()

lifecycleScope.launch {
    favoriteRepository.addToFavorites("song_001").onSuccess {
        Log.d("Favorite", "Added to favorites")
    }
}
```

## Queries quan trọng

### 1. Lấy bài hát phổ biến nhất:
```kotlin
suspend fun getPopularSongs(limit: Long = 20): Result<List<Song>> {
    val snapshot = songsCollection
        .orderBy("playCount", Query.Direction.DESCENDING)
        .limit(limit)
        .get()
        .await()
    return Result.success(snapshot.toObjects(Song::class.java))
}
```

### 2. Tìm kiếm bài hát:
```kotlin
suspend fun searchSongs(query: String): Result<List<Song>> {
    val snapshot = songsCollection
        .orderBy("title")
        .startAt(query)
        .endAt(query + "\uf8ff")
        .get()
        .await()
    return Result.success(snapshot.toObjects(Song::class.java))
}
```

### 3. Lấy bài hát theo thể loại:
```kotlin
suspend fun getSongsByGenre(genre: String): Result<List<Song>> {
    val snapshot = songsCollection
        .whereArrayContains("genre", genre)
        .get()
        .await()
    return Result.success(snapshot.toObjects(Song::class.java))
}
```

## Security Best Practices

1. **Luôn validate user input** trước khi ghi vào Firestore
2. **Không lưu thông tin nhạy cảm** (passwords, tokens) vào Firestore
3. **Sử dụng Security Rules** để kiểm soát quyền truy cập
4. **Implement rate limiting** để tránh spam
5. **Sử dụng Firebase Storage** cho file audio và ảnh

## Performance Tips

1. **Sử dụng Indexes** cho queries phức tạp
2. **Giới hạn kích thước document** < 1MB
3. **Batch writes** khi cập nhật nhiều documents
4. **Cache dữ liệu** trên local với Room Database
5. **Pagination** cho danh sách dài

## Offline Support

Firestore hỗ trợ offline mode tự động:

```kotlin
// Enable offline persistence
FirebaseFirestore.getInstance().apply {
    firestoreSettings = firestoreSettings.toBuilder()
        .setPersistenceEnabled(true)
        .build()
}
```

## Next Steps

1. Thiết kế database schema
2. Setup Firebase Storage cho audio files
3. Implement audio player service
4. Tạo UI với Jetpack Compose
5. Add push notifications
6. Implement caching strategy

## Tài liệu tham khảo

- [Firestore Documentation](https://firebase.google.com/docs/firestore)
- [Firestore Data Modeling](https://firebase.google.com/docs/firestore/data-model)
- [Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

---

**Lưu ý quan trọng:**
- Đây chỉ là thiết kế cơ bản, bạn có thể mở rộng thêm collections như: genres, moods, featured_playlists...
- Cân nhắc sử dụng Cloud Functions để xử lý logic backend như cập nhật statistics, gửi notifications...
- Implement caching với Room Database cho trải nghiệm offline tốt hơn
