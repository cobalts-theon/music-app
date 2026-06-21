# Lo trinh hieu code Cinder's Soul trong nua ngay

Gia dinh "nua ngay" la khoang 4 gio tap trung. Muc tieu thuc te la nam duoc kien truc, luong du lieu, diem vao chinh, noi xu ly nghiep vu va noi can sua khi co bug/feature moi. Khong nen co gang thuoc tung dong UI Compose ngay trong lan doc dau tien.

## Ban do tong quan

Du an gom 2 phan chinh:

- `app/`: Android app viet bang Kotlin, Jetpack Compose, Retrofit, Room va Media3 ExoPlayer.
- `backend/`: REST API viet bang Node.js, Express, Sequelize, MySQL, JWT, Multer.

Luong chay lon nhat:

```text
AndroidManifest.xml
  -> AuthenticationActivity hoac MainActivity
  -> MusicApp
  -> MusicViewModel
  -> Repository
  -> ApiClient / ApiService
  -> backend/src/server.js
  -> routes
  -> controllers / services
  -> Sequelize models
  -> MySQL
```

Luong cache/offline:

```text
SongRepository
  -> ApiService thanh cong: luu vao Room
  -> ApiService loi: fallback sang MusicCacheDao
```

Luong phat nhac:

```text
UI click bai hat
  -> MusicApp callback
  -> MusicViewModel.playOrToggleSong()
  -> ExoPlayer + MediaSession + PlaybackNotificationManager
  -> MusicUiState cap nhat UI
```

## 0-15 phut: Mo ban do repo truoc khi doc code

Doc nhanh cac file:

- `settings.gradle.kts`: repo chi co module Android `:app`.
- `build.gradle.kts`: plugin Android/Kotlin/KSP.
- `app/build.gradle.kts`: dependency quan trong la Compose, Retrofit, Room, Media3, Coil, OkHttp.
- `backend/package.json`: backend dung Express, Sequelize, MySQL, JWT, Google auth, email, upload.
- `agent.md`: tom tat backend va cac endpoint da co.

Lenh nen dung khi muon tim nhanh:

```powershell
rg --files app\src\main\java backend\src
rg -n "class|data class|interface|fun " app\src\main\java\com\example\cinderssoul
rg -n "router\.|app\.use|exports\." backend\src
```

Checkpoint:

- Biet `app/` la client Android.
- Biet `backend/` la API server.
- Biet client khong con dung Firebase, hien dang di qua REST API.

## 15-45 phut: Hieu diem vao cua Android va backend

Doc theo thu tu:

1. `app/src/main/AndroidManifest.xml`
   - Launcher activity la `AuthenticationActivity`.
   - `MainActivity` la man app cho user thuong.
   - `AdminActivity` la man admin.

2. `app/src/main/java/com/example/cinderssoul/AuthenticationActivity.kt`
   - Kiem tra session da luu trong `SharedPreferences`.
   - Login/register/Google/forgot password goi `ApiClient.apiService`.
   - Neu user la admin thi mo `AdminActivity`, nguoc lai mo `MainActivity`.

3. `app/src/main/java/com/example/cinderssoul/MainActivity.kt`
   - Tao `CinderDatabase`.
   - Tao `SongRepository`.
   - Tao `MusicViewModel`.
   - Truyen ViewModel vao `MusicApp`.

4. `backend/src/server.js`
   - Cau hinh middleware.
   - Mount route: `/api/auth`, `/api/songs`, `/api/playlists`, `/api/library`, `/api/admin`, `/share`.
   - Chay `ensureAccountLibrarySchema()` truoc khi listen.

Checkpoint:

- Ve lai duoc "mo app -> auth -> main/admin".
- Ve lai duoc "request Android -> Express route -> controller".

## 45-90 phut: Hieu database va API contract

Doc backend truoc:

- `backend/src/config/database.js`: ket noi MySQL, mac dinh database `cinders_soul`, port `3307`.
- `backend/src/models/index.js`: quan he giua `User`, `Artist`, `Album`, `Song`, `Playlist`, `PlaylistSong`, `UserLibrarySong`, `RefreshToken`.
- `backend/cinders_soul.sql`: schema that va seed data.
- `backend/src/routes/*.routes.js`: map URL sang controller.
- `backend/src/middleware/auth.js`: `protect`, `optionalAuth`, `requireAdmin`.

Quan he can nho:

```text
User 1-n Playlist
Artist 1-n Album
Artist 1-n Song
Album 1-n Song
Playlist n-n Song qua PlaylistSong
User n-n Song qua UserLibrarySong
User 1-n RefreshToken
```

Doc Android API contract:

- `app/src/main/java/com/example/cinderssoul/network/ApiService.kt`: danh sach endpoint Android goi.
- `app/src/main/java/com/example/cinderssoul/network/ApiModels.kt`: DTO request/response va ham map DTO -> domain model.
- `app/src/main/java/com/example/cinderssoul/network/ApiClient.kt`: base URL, token Bearer, OkHttp interceptor, fallback localhost/emulator.

Checkpoint:

- Chon duoc 1 endpoint, vi du `GET /api/songs`, va chi ra duong di:

```text
SongRepository.getAllSongs()
  -> ApiService.getSongs()
  -> backend routes/song.routes.js
  -> song.controller.js getAllSongs()
  -> Sequelize Song.findAll()
```

## 90-135 phut: Hieu model, repository va cache local

Doc Android domain model:

- `models/Song.kt`
- `models/Artist.kt`
- `models/Album.kt`
- `models/Playlist.kt`
- `models/User.kt`
- `MusicUiState.kt`

Doc repository:

- `repository/SongRepository.kt`
  - Lay songs/artists/albums tu API.
  - Thanh cong thi cache vao Room.
  - Loi API thi doc cache local.

- `repository/PlaylistRepository.kt`
  - CRUD playlist cua user dang login.

- `repository/LibraryRepository.kt`
  - Saved/favorite songs tren backend qua `/api/library/songs`.

- `repository/AdminRepository.kt`
  - CRUD user/song/artist/album cho admin.

Doc Room:

- `local/CinderDatabase.kt`: database local `cinders_soul_cache.db`, version 4, migrations.
- `local/MusicCacheDao.kt`: query cache songs/artists/albums, favorites, downloads, listening history.
- `local/CachedMusicEntities.kt`: entity local va mapper sang domain model.

Checkpoint:

- Phan biet duoc 2 loai luu tru:
  - MySQL backend: account, library, playlist, catalog.
  - Room local: cache catalog, downloaded songs, listening history, mot phan legacy favorites.

## 135-190 phut: Hieu UI shell va MusicViewModel

Doc UI shell truoc, dung doc het component nho:

- `ui/app/AppUiModels.kt`: enum tab, route, section, theme mode.
- `ui/app/MusicApp.kt`: dieu phoi man hinh, selected tab, detail route, dialog, bottom bar, callback vao ViewModel.
- `ui/navigation/BottomBar.kt`: mini player va tab bar.
- `ui/home/HomeScreen.kt`: man Home.
- `ui/browse/SearchDiscoverProfileScreens.kt`: Search, Discover, Profile.
- `ui/library/LibraryScreens.kt`: Library va detail playlist/collection.
- `ui/player/NowPlayingScreen.kt`: man dang phat nhac.
- `ui/dialogs/Dialogs.kt`: dialog auth/profile/playlist.
- `ui/components/CommonComponents.kt`: component dung chung.

Sau do doc `MusicViewModel.kt` theo cum, khong doc tu dau den cuoi lien tuc:

1. Khoi tao va player
   - constructor
   - `exoPlayer`
   - `mediaSession`
   - `PlaybackNotificationManager`
   - `init`

2. Auth/session
   - `signInWithEmail`
   - `registerWithEmail`
   - `signInWithGoogleIdToken`
   - `applyAuthSession`
   - `refreshAccessToken`
   - `clearStoredAuthSession`
   - `switchLibraryOwner`

3. Load data
   - `refreshData`
   - cac bien `backendPlaylists`, `customPlaylists`, `favoriteSongIds`, `downloadedSongIds`.

4. Playback
   - `playOrToggleSong`
   - `playNextSong`
   - `playPreviousSong`
   - `togglePlayback`
   - `seekTo`
   - `cycleRepeatMode`
   - `syncPlayerState`
   - `persistCurrentSongState`

5. Library/playlist
   - `toggleCurrentSongLiked`
   - `createPlaylist`
   - `addSongToPlaylist`
   - `removeSongFromPlaylist`
   - `deletePlaylist`
   - `syncPlaylistFromBackend`

6. Download/share
   - `downloadSong`
   - `downloadPlaylist`
   - `shareSong`
   - `sharePlaylist`
   - `ShareLinks.kt`

Checkpoint:

- Giai thich duoc vi sao UI chi nhan `uiState`, con hanh dong thi goi function cua ViewModel.
- Trace duoc "bam play bai hat" tu UI den ExoPlayer.
- Trace duoc "like/save bai hat" tu UI den backend library va cap nhat playlist Favorite.

## 190-220 phut: Hieu admin, upload, share va auth backend

Admin Android:

- `admin/AdminUiState.kt`: state form/list/search.
- `admin/AdminViewModel.kt`: load data va CRUD qua `AdminRepository`.
- `admin/AdminActivity.kt`: UI dashboard, list, form dialog.

Admin backend:

- `backend/src/routes/admin.routes.js`: tat ca route admin deu qua `protect` va `requireAdmin`.
- `backend/src/controllers/admin.controller.js`: summary va CRUD user.
- CRUD song/artist/album admin dung chung endpoint public route nhung method POST/PUT/DELETE yeu cau admin.

Auth backend:

- `backend/src/controllers/auth.controller.js`: controller mong, day viec sang service.
- `backend/src/services/auth.service.js`: register, login, Google, refresh token, forgot/reset password OTP.
- `backend/src/utils/token.js`: tao access token/refresh token.
- `backend/src/services/email.service.js`: gui welcome va OTP email.
- `backend/src/services/googleAuth.service.js`: verify Google ID token.

Upload/share:

- `backend/src/routes/upload.routes.js`
- `backend/src/middleware/upload.js`
- `backend/src/controllers/upload.controller.js`
- `backend/src/routes/share.routes.js`
- `backend/src/controllers/share.controller.js`

Checkpoint:

- Biet route nao can token user, route nao can admin.
- Biet anh upload di qua Multer, tra ve URL cho Android luu vao profile/playlist/song.
- Biet share link la URL `/share/...`, khong phai API JSON thuong.

## 220-240 phut: Tu kiem tra bang 6 luong thuc te

Dung 20 phut cuoi de tu trace 6 luong nay, moi luong chi can 2-3 phut:

1. Login email

```text
AuthenticationActivity
  -> ApiService.login
  -> auth.routes.js
  -> auth.controller.js
  -> auth.service.js
  -> User/RefreshToken
```

2. Mo Home va load songs

```text
MainActivity
  -> MusicViewModel.init
  -> refreshData
  -> SongRepository.getAllSongs
  -> ApiService.getSongs
  -> song.controller.js
```

3. Search bai hat

```text
SearchTab
  -> onQueryChange
  -> MusicUiState.filteredSongs
```

4. Phat nhac

```text
Home/Search/Library click song
  -> MusicApp callback
  -> MusicViewModel.playOrToggleSong
  -> ExoPlayer
  -> MusicUiState currentSong/isPlayerRunning
  -> BottomBar/NowPlayingScreen
```

5. Luu bai hat vao library

```text
NowPlayingScreen toggle like
  -> MusicViewModel.toggleCurrentSongLiked
  -> LibraryRepository.addSongToLibrary/removeSongFromLibrary
  -> library.routes.js
  -> library.controller.js
  -> UserLibrarySong
```

6. Admin tao/sua bai hat

```text
AdminActivity form
  -> AdminViewModel.saveSong
  -> AdminRepository.createSong/updateSong
  -> ApiService createSong/updateSong
  -> song.routes.js
  -> protect + requireAdmin
  -> song.controller.js
```

Neu trace duoc 6 luong tren ma khong mo nham file, ban da nam duoc khung toan bo du an.

## File nen doc ky va file chi can luot

Doc ky:

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/example/cinderssoul/AuthenticationActivity.kt`
- `app/src/main/java/com/example/cinderssoul/MainActivity.kt`
- `app/src/main/java/com/example/cinderssoul/MusicViewModel.kt`
- `app/src/main/java/com/example/cinderssoul/MusicUiState.kt`
- `app/src/main/java/com/example/cinderssoul/ui/app/MusicApp.kt`
- `app/src/main/java/com/example/cinderssoul/network/ApiClient.kt`
- `app/src/main/java/com/example/cinderssoul/network/ApiService.kt`
- `app/src/main/java/com/example/cinderssoul/network/ApiModels.kt`
- `app/src/main/java/com/example/cinderssoul/repository/*.kt`
- `app/src/main/java/com/example/cinderssoul/local/*.kt`
- `backend/src/server.js`
- `backend/src/routes/*.routes.js`
- `backend/src/controllers/auth.controller.js`
- `backend/src/controllers/song.controller.js`
- `backend/src/controllers/playlist.controller.js`
- `backend/src/controllers/library.controller.js`
- `backend/src/services/auth.service.js`
- `backend/src/models/index.js`
- `backend/cinders_soul.sql`

Doc luot sau khi da hieu luong:

- Cac file UI man hinh chi tiet trong `ui/home`, `ui/browse`, `ui/library`, `ui/player`.
- `ui/components/CommonComponents.kt`.
- `ui/theme/*`.
- Drawable, mipmap, font, assets.
- `backend/src/assets/*`.

## Dau hieu da hieu dung codebase

Ban nen tu tra loi duoc cac cau sau:

- App bat dau o activity nao va vi sao co luc vao `AdminActivity`?
- Access token duoc luu o dau va gan vao request bang cach nao?
- `MusicUiState` cap nhat UI nhu the nao?
- Khi backend chet, danh sach bai hat co fallback local khong?
- Favorite/library khac downloaded song nhu the nao?
- Playlist ca nhan nam o backend hay local?
- Admin CRUD song co route rieng hay dung endpoint song chung?
- MySQL model nao dai dien cho library/favorite songs?
- Share link tao o Android hay backend?
- ExoPlayer duoc tao, release va dong bo state o dau?

## Neu con them 1 gio sau nua ngay

Uu tien lam 3 viec:

1. Chay backend va goi health check/API bang Postman hoac curl.
2. Chay Android app, dat breakpoint o `refreshData`, `playOrToggleSong`, `toggleCurrentSongLiked`.
3. Viet mot so do nho rieng cho 3 luong: auth, playback, library.

## Ghi chu quan trong

- `MusicViewModel.kt` va `AdminActivity.kt` lon, dung doc tu dau den cuoi trong lan dau. Hay doc theo cum chuc nang.
- Backend controller tuong doi thang: route -> controller -> model/service.
- Android UI Compose co nhieu component, nhung luong nghiep vu nam chu yeu o ViewModel, Repository va ApiService.
- Sau nua ngay, muc tieu la hieu de sua va debug, khong phai nho tung style UI.
