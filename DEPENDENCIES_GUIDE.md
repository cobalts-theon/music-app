# Dependencies Guide - Cinder's Soul Music App

## Danh sách Dependencies đã thêm

### Firebase (Backend Services)

```kotlin
// Firebase BOM - Quản lý phiên bản tự động
implementation(platform("com.google.firebase:firebase-bom:34.11.0"))

// Firebase services
implementation("com.google.firebase:firebase-analytics-ktx")    // Analytics
implementation("com.google.firebase:firebase-firestore-ktx")    // Database
implementation("com.google.firebase:firebase-auth-ktx")          // Authentication
implementation("com.google.firebase:firebase-storage-ktx")       // Lưu trữ file audio/ảnh
```

**Công dụng:**
- **Firestore**: Cơ sở dữ liệu NoSQL để lưu songs, playlists, users
- **Auth**: Đăng nhập/đăng ký người dùng
- **Storage**: Lưu trữ file nhạc MP3, ảnh cover, avatar
- **Analytics**: Theo dõi hành vi người dùng

---

### Media3 ExoPlayer (Audio Player)

```kotlin
val media3Version = "1.5.0"
implementation("androidx.media3:media3-exoplayer:$media3Version")
implementation("androidx.media3:media3-exoplayer-dash:$media3Version")
implementation("androidx.media3:media3-ui:$media3Version")
implementation("androidx.media3:media3-session:$media3Version")
```

**Công dụng:**
- Phát nhạc từ URL hoặc local storage
- Hỗ trợ streaming audio
- Background playback (phát nhạc nền)
- Media controls (play/pause/skip)
- Notification controls

**Ví dụ sử dụng:**
```kotlin
val player = ExoPlayer.Builder(context).build()
val mediaItem = MediaItem.fromUri("https://storage.googleapis.com/song.mp3")
player.setMediaItem(mediaItem)
player.prepare()
player.play()
```

---

### Room Database (Offline Cache)

```kotlin
val roomVersion = "2.6.1"
implementation("androidx.room:room-runtime:$roomVersion")
implementation("androidx.room:room-ktx:$roomVersion")
ksp("androidx.room:room-compiler:$roomVersion")
```

**Công dụng:**
- Cache dữ liệu offline (songs, playlists)
- Tăng tốc độ load dữ liệu
- Hoạt động khi không có internet
- Đồng bộ với Firestore

**Ví dụ Entity:**
```kotlin
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val artistName: String,
    val audioUrl: String,
    val isDownloaded: Boolean = false
)
```

---

### Coil (Image Loading)

```kotlin
implementation("io.coil-kt.coil3:coil-compose:3.0.4")
implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")
```

**Công dụng:**
- Load ảnh cover album, artist từ URL
- Cache ảnh tự động
- Placeholder và error handling
- Tích hợp tốt với Jetpack Compose

**Ví dụ sử dụng:**
```kotlin
AsyncImage(
    model = song.coverImageUrl,
    contentDescription = song.title,
    modifier = Modifier.size(200.dp),
    placeholder = painterResource(R.drawable.placeholder),
    error = painterResource(R.drawable.error)
)
```

---

### Navigation Compose

```kotlin
implementation("androidx.navigation:navigation-compose:2.8.5")
```

**Công dụng:**
- Điều hướng giữa các màn hình
- Deep linking
- Arguments passing
- Back stack management

**Ví dụ:**
```kotlin
NavHost(navController, startDestination = "home") {
    composable("home") { HomeScreen() }
    composable("player/{songId}") { backStackEntry ->
        PlayerScreen(songId = backStackEntry.arguments?.getString("songId"))
    }
    composable("playlist/{playlistId}") { 
        PlaylistDetailScreen() 
    }
}
```

---

### Kotlin Coroutines

```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
```

**Công dụng:**
- Xử lý bất đồng bộ (async operations)
- Network calls không block UI
- Tích hợp với Firebase

**Ví dụ:**
```kotlin
viewModelScope.launch {
    val result = songRepository.getAllSongs()
    result.onSuccess { songs ->
        _songsState.value = songs
    }
}
```

---

### ViewModel & Lifecycle

```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
```

**Công dụng:**
- Quản lý state của UI
- Survive configuration changes
- Separation of concerns

**Ví dụ:**
```kotlin
class HomeViewModel : ViewModel() {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()
    
    fun loadSongs() {
        viewModelScope.launch {
            songRepository.getAllSongs().onSuccess {
                _songs.value = it
            }
        }
    }
}
```

---

### DataStore (Preferences)

```kotlin
implementation("androidx.datastore:datastore-preferences:1.1.1")
```

**Công dụng:**
- Lưu settings người dùng
- Theme preference (Light/Dark)
- Audio quality settings
- Last played song

**Ví dụ:**
```kotlin
val Context.dataStore by preferencesDataStore("settings")

object PreferencesKeys {
    val THEME = stringPreferencesKey("theme")
    val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
}

suspend fun saveTheme(theme: String) {
    context.dataStore.edit { prefs ->
        prefs[PreferencesKeys.THEME] = theme
    }
}
```

---

### Material Icons Extended

```kotlin
implementation("androidx.compose.material:material-icons-extended:1.7.6")
```

**Công dụng:**
- Icons cho UI: PlayArrow, Pause, Favorite, QueueMusic...
- Hơn 2000+ icons

**Ví dụ:**
```kotlin
Icon(
    imageVector = Icons.Filled.PlayArrow,
    contentDescription = "Play"
)
```

---

### Utilities

#### Timber (Logging)
```kotlin
implementation("com.jakewharton.timber:timber:5.0.1")
```

```kotlin
// Setup in Application class
Timber.plant(Timber.DebugTree())

// Usage
Timber.d("Song loaded: ${song.title}")
Timber.e(exception, "Error loading songs")
```

#### Gson (JSON Parsing)
```kotlin
implementation("com.google.code.gson:gson:2.11.0")
```

```kotlin
val gson = Gson()
val json = gson.toJson(song)
val song = gson.fromJson(json, Song::class.java)
```

---

## Build & Sync

Sau khi thêm dependencies, chạy lệnh:

```bash
# Sync Gradle
./gradlew build

# Hoặc trong Android Studio:
# File > Sync Project with Gradle Files
```

---

## Permissions cần thêm vào AndroidManifest.xml

```xml
<manifest>
    <!-- Internet để load nhạc từ Firebase -->
    <uses-permission android:name="android.permission.INTERNET" />
    
    <!-- Phát nhạc nền -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    
    <!-- Thông báo -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    
    <!-- Lưu nhạc offline (optional) -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    
    <!-- Wake lock để phát nhạc khi màn hình tắt -->
    <uses-permission android:name="android.permission.WAKE_LOCK" />
</manifest>
```

---

## Kiến trúc đề xuất

```
app/
├── data/
│   ├── local/          # Room Database
│   ├── remote/         # Firebase Repositories
│   └── model/          # Data models
├── ui/
│   ├── screens/        # Compose screens
│   ├── components/     # Reusable UI components
│   └── theme/          # Material Theme
├── service/
│   └── MusicService.kt # ExoPlayer service
└── viewmodel/          # ViewModels
```

---

## Checklist

- [x] Firebase dependencies
- [x] ExoPlayer cho phát nhạc
- [x] Room Database cho offline
- [x] Coil cho load ảnh
- [x] Navigation Compose
- [x] Coroutines
- [x] ViewModel & Lifecycle
- [x] DataStore

---

## Next Steps

1. **Setup Application class** với Timber
2. **Tạo MusicService** với ExoPlayer
3. **Implement Repository pattern** hoàn chỉnh
4. **Build UI screens** với Jetpack Compose
5. **Add notification controls**
6. **Implement offline mode** với Room

Happy coding! 🎵
