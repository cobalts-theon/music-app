# 🔥 Firebase Migration Plan

## ✅ Đã hoàn thành:
- [x] Xóa Firebase config files (firebase.json, .firebaserc, firestore.rules, firestore.indexes.json)
- [x] Xóa google-services.json
- [x] Comment out Firebase dependencies trong Gradle
- [x] Thêm Retrofit cho REST API calls

## 📋 Cần làm tiếp:

### 1. **Migrate Authentication** (Firebase Auth → JWT từ Node.js backend)
**Files cần sửa:**
- `app/src/main/java/com/example/cinderssoul/repository/FavoriteRepository.kt`
  - Xóa: `FirebaseAuth.getInstance()`
  - Thay bằng: `AuthRepository` gọi API `/api/auth/login`, `/api/auth/register`
  - Lưu JWT token vào `DataStore` hoặc `SharedPreferences`

**Backend đã có:**
- ✅ `backend/src/routes/auth.js` - JWT authentication
- ✅ `jsonwebtoken` package

**Cần tạo trong app:**
```kotlin
// AuthRepository.kt
class AuthRepository(private val apiService: ApiService) {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(email: String, password: String): Result<AuthResponse>
    fun saveToken(token: String)
    fun getToken(): String?
}

// ApiService.kt (Retrofit)
interface ApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body credentials: LoginRequest): Response<AuthResponse>
    
    @POST("/api/auth/register")
    suspend fun register(@Body data: RegisterRequest): Response<AuthResponse>
}
```

---

### 2. **Migrate Firestore → MySQL API calls**
**Files cần sửa:**
- `app/src/main/java/com/example/cinderssoul/repository/SongRepository.kt`
- `app/src/main/java/com/example/cinderssoul/repository/PlaylistRepository.kt`
- `app/src/main/java/com/example/cinderssoul/repository/FavoriteRepository.kt`

**Thay thế:**
```kotlin
// Cũ (Firebase):
private val db = FirebaseFirestore.getInstance()
db.collection("songs").get()

// Mới (REST API):
interface ApiService {
    @GET("/api/songs")
    suspend fun getSongs(): Response<List<Song>>
    
    @GET("/api/songs/{id}")
    suspend fun getSongById(@Path("id") id: Int): Response<Song>
    
    @POST("/api/playlists")
    suspend fun createPlaylist(@Body playlist: Playlist): Response<Playlist>
}
```

**Backend đã có:**
- ✅ MySQL database với Sequelize
- ✅ Models: User, Song, Playlist, Artist, Album
- Cần tạo: Controllers & Routes cho các API endpoints

---

### 3. **Migrate Firebase Storage → Node.js file upload**
**File cần sửa:**
- `app/src/main/java/com/example/cinderssoul/MusicViewModel.kt`

**Thay thế:**
```kotlin
// Cũ (Firebase Storage):
FirebaseStorage.getInstance().getReference(song.audioUrl)
    .downloadUrl.addOnSuccessListener { uri -> ... }

// Mới (Direct URL từ backend):
val directUrl = song.audioUrl // http://localhost:3000/uploads/song.mp3
mediaItem = MediaItem.fromUri(directUrl)
exoPlayer.setMediaItem(mediaItem)
```

**Backend đã có:**
- ✅ `multer` package cho file upload
- ✅ `/uploads/` folder
- Cần: Static file serving trong Express

```javascript
// backend/src/server.js
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));
```

---

### 4. **Migrate data types**
**Timestamp → Long/String:**
```kotlin
// Cũ:
import com.google.firebase.Timestamp
data class Song(
    val createdAt: Timestamp
)

// Mới:
data class Song(
    val createdAt: Long // Unix timestamp hoặc ISO String
)
```

---

### 5. **Setup Retrofit + OkHttp**
**Tạo file mới:**
```kotlin
// network/RetrofitClient.kt
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000" // Android emulator
    // private const val BASE_URL = "http://localhost:3000" // Real device
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val authInterceptor = Interceptor { chain ->
        val token = TokenManager.getToken()
        val request = chain.request().newBuilder()
            .apply {
                if (token != null) {
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .build()
        chain.proceed(request)
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .build()
    
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

---

## 🔧 Các bước thực hiện (khuyến nghị):

1. **Setup backend API endpoints trước**
   ```bash
   cd backend
   npm run dev
   ```
   - Tạo controllers cho Songs, Playlists, Favorites
   - Test với Postman/Thunder Client

2. **Tạo Retrofit client trong app**
   - `ApiService.kt` interface
   - `RetrofitClient.kt` singleton

3. **Migrate từng Repository một**
   - Bắt đầu với `SongRepository` (đơn giản nhất)
   - Sau đó `PlaylistRepository`
   - Cuối cùng `FavoriteRepository` + Authentication

4. **Update ViewModels**
   - Replace Firebase calls với Repository calls
   - Handle error states (network errors)

5. **Testing**
   - Test với backend chạy local
   - Test offline mode (Room cache)

---

## 📦 Dependencies cần giữ lại:
```kotlin
// Networking
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// JSON parsing
implementation("com.google.code.gson:gson:2.11.0")

// Coroutines (async network calls)
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

// Room (offline caching)
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")

// DataStore (save JWT token)
implementation("androidx.datastore:datastore-preferences:1.1.1")

// ExoPlayer (music playback)
implementation("androidx.media3:media3-exoplayer:1.5.0")
```

---

## ⚠️ Lưu ý:
- **Không xóa** code Firebase hiện tại cho đến khi migration xong
- Test từng phần nhỏ trước khi merge
- Backend phải running trước khi test app
- Xử lý offline mode với Room database

---

## 🎯 Kết quả mong đợi:
```
Android App (Kotlin) 
    ↓ Retrofit/OkHttp
Node.js Backend (Express)
    ↓ Sequelize ORM
MySQL Database
```

**Lợi ích:**
- ✅ Kiểm soát hoàn toàn logic backend
- ✅ Không bị giới hạn quota Firebase
- ✅ Chi phí thấp (free tier hosting)
- ✅ Học được fullstack development
- ✅ Dễ scale và customize

---

**Cần hỗ trợ migration?** Hỏi tôi từng phần cụ thể! 🚀
