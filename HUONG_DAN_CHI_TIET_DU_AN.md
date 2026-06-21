# Huong dan chi tiet du an Cinder's Soul

Tai lieu nay dung de hieu nhanh cau truc code, vai tro tung thanh phan va cac luong su kien chinh cua du an Cinder's Soul. Du an gom 2 phan:

- Ung dung Android: `app/`, viet bang Kotlin, Jetpack Compose, Retrofit, Room va Media3 ExoPlayer.
- Backend API: `backend/`, viet bang Node.js, Express, Sequelize va MySQL.

## 1. Tong quan kien truc

Luong tong quat:

```text
Nguoi dung
  -> UI Jetpack Compose
  -> MusicViewModel / AdminViewModel
  -> Repository
  -> Retrofit ApiService
  -> Express routes
  -> Controller / Service
  -> Sequelize model
  -> MySQL
```

Rieng cac du lieu phat nhac/offline:

```text
MusicViewModel
  -> ExoPlayer / MediaSession / Notification
  -> Room cache
  -> SharedPreferences
```

Trong do:

- UI chi nen goi ham cua ViewModel va hien thi `uiState`.
- ViewModel giu state, xu ly su kien UI, goi repository/API va dieu khien player.
- Repository boc cac API call thanh `Result<T>` va map DTO sang model domain.
- Backend route nhan request, middleware xu ly auth/validate/upload, controller thuc hien logic, model lam viec voi database.

## 2. Cau truc thu muc quan trong

```text
app/src/main/java/com/example/cinderssoul/
  MainActivity.kt                 Entry point man hinh user
  AuthenticationActivity.kt       Man hinh dang nhap/dang ky rieng
  OtpVerificationActivity.kt      Man hinh OTP neu can
  CindersApplication.kt           Application class
  MusicViewModel.kt               Trung tam logic user app va player
  MusicUiState.kt                 State tong cua ung dung nghe nhac
  PlaybackNotificationManager.kt  Notification dieu khien phat nhac

  ui/app/
    MusicApp.kt                   Dieu huong tab, dialog, detail screen
    AppUiModels.kt                Enum/model UI route, tab, theme

  ui/home/                        Man hinh Home
  ui/browse/                      Search, Discover, Profile
  ui/library/                     Library, playlist detail, collection detail
  ui/player/                      Now playing
  ui/dialogs/                     Dialog dang nhap, playlist, edit...
  ui/navigation/                  Bottom bar
  ui/components/                  Component dung chung

  network/
    ApiClient.kt                  Retrofit client, base URL, token interceptor
    ApiService.kt                 Danh sach endpoint backend
    ApiModels.kt                  DTO request/response va mapper

  repository/
    SongRepository.kt             Lay songs/artists/albums, cache Room
    PlaylistRepository.kt         CRUD playlist
    LibraryRepository.kt          Favorite/library songs
    AdminRepository.kt            API cho admin

  local/
    CinderDatabase.kt             Room database
    MusicCacheDao.kt              DAO cache/offline
    CachedMusicEntities.kt        Entity Room

  admin/
    AdminActivity.kt              Entry point man hinh admin
    AdminViewModel.kt             Logic admin
    AdminUiState.kt               State admin

backend/src/
  server.js                       Entry point Express
  routes/                         Dinh nghia API endpoints
  controllers/                    Xu ly request/response
  services/                       Logic phuc tap: auth, email, Google
  middleware/                     auth, validator, upload, error handler
  models/                         Sequelize models va relation
  validators/                     Rule validate request
  config/database.js              Ket noi MySQL
```

## 3. Android app: luong khoi dong

File chinh: `app/src/main/java/com/example/cinderssoul/MainActivity.kt`

Khi app mo:

1. `MainActivity.onCreate()` tao `CinderDatabase`.
2. Tao `SongRepository` voi `musicCacheDao`.
3. Kiem tra `SharedPreferences` xem user hien tai co role `admin` khong.
4. Neu la admin thi mo `AdminActivity` va dong `MainActivity`.
5. Neu la user binh thuong thi set Compose content.
6. Doc theme mode tu `SharedPreferences`.
7. Tao `MusicViewModel` bang custom `ViewModelProvider.Factory`.
8. Goi `MusicApp(...)` de render toan bo UI.

```text
MainActivity
  -> CinderDatabase.getInstance()
  -> SongRepository(...)
  -> MusicViewModel(...)
  -> MusicApp(...)
```

## 4. Android app: luong UI va navigation

File chinh: `app/src/main/java/com/example/cinderssoul/ui/app/MusicApp.kt`

`MusicApp` la noi dieu phoi UI:

- Quan ly tab hien tai: Home, Search, Discover, Library, Profile.
- Quan ly route detail: playlist, artist, album, genre, home collection, now playing.
- Mo/dong dialog: tao playlist, them bai vao playlist, sua playlist, account dialog.
- Nhan `uiState` tu `MusicViewModel`.
- Truyen callback xuong cac screen con.

Vi du luong bam phat bai hat:

```text
User bam bai hat tren HomeTab
  -> HomeTab goi onPlaySong(song)
  -> MusicApp truyen ve viewModel.playOrToggleSong(song)
  -> MusicViewModel cap nhat ExoPlayer va uiState
  -> UI tu dong recomposition theo uiState moi
```

Vi du luong mo playlist:

```text
User bam playlist trong LibraryTab
  -> openPlaylist(playlist)
  -> selectedTab = Library
  -> openedPlaylistId = playlist.id
  -> contentRoute = PlaylistDetail
  -> render PlaylistDetailScreen
```

## 5. MusicViewModel: trung tam logic app

File chinh: `app/src/main/java/com/example/cinderssoul/MusicViewModel.kt`

`MusicViewModel` phu trach:

- Dang nhap, dang ky, Google sign-in, quen mat khau, reset password.
- Luu token, refresh token, restore session.
- Lay danh sach songs, artists, albums, playlists, library.
- Dieu khien ExoPlayer: play, pause, next, previous, seek, volume, shuffle, repeat.
- Dong bo favorite/library songs len backend neu da dang nhap.
- Luu cache Room cho songs/artists/albums, lich su nghe, downloaded songs.
- Luu `SharedPreferences` cho token, user, theme, bai hat cuoi, vi tri phat cuoi.
- Upload anh avatar/playlist cover.
- Share bai hat/album/artist/playlist/profile.

State chinh:

- `_uiState`: mutable state noi bo.
- `uiState`: state expose cho UI doc.
- `exoPlayer`: player phat nhac.
- `backendPlaylists`, `customPlaylists`, `favoriteSongIds`: state phu de compose playlist.
- `activeLibraryUserId`: user dang so huu library hien tai, guest la `0`.

## 6. Luong tai du lieu ban dau

Khi `MusicViewModel` duoc tao:

1. Set access token cho `ApiClient` tu `SharedPreferences`.
2. Thu restore cached auth user.
3. Neu chua co user thi restore offline library state.
4. Goi `restoreExplicitAuthUser()`.
5. Gan listener cho `exoPlayer`.
6. Bat dau job cap nhat progress.
7. Goi `refreshData()`.

Luong `refreshData()` ve mat y tuong:

```text
refreshData()
  -> SongRepository.getAllSongs()
  -> SongRepository.getAllArtists()
  -> SongRepository.getAllAlbums()
  -> neu co auth: PlaylistRepository.getUserPlaylists()
  -> neu co auth: LibraryRepository.getLibrarySongs()
  -> merge songs tu backend + library/download cache
  -> compose playlists:
       Favorite
       Recently Added
       Most Played
       Chill Mix
       playlist backend/custom
  -> cap nhat uiState
  -> restore bai dang nghe gan nhat neu co
```

`SongRepository` co fallback cache:

```text
API thanh cong
  -> map DTO sang domain model
  -> ghi Room cache
  -> tra ve du lieu moi

API loi
  -> doc Room cache
  -> neu co cache thi tra ve cache
  -> neu khong co cache thi tra loi
```

## 7. Luong dang nhap va phien lam viec

File lien quan:

- Android: `MusicViewModel.kt`, `ApiClient.kt`, `ApiService.kt`
- Backend: `auth.routes.js`, `auth.controller.js`, `auth.service.js`, `middleware/auth.js`

Luong dang nhap email:

```text
AccountAuthDialog
  -> viewModel.signInWithEmail(email, password)
  -> validateLoginInput()
  -> ApiClient.apiService.login(LoginRequest)
  -> POST /api/auth/login
  -> backend auth controller/service kiem tra user + password
  -> tra accessToken, refreshToken, user
  -> applyAuthSession()
  -> saveAuthSession()
  -> cacheAuthUser()
  -> switchLibraryOwner(user.id)
  -> refreshData()
```

Token duoc luu trong `SharedPreferences`:

- `api_access_token`
- `api_refresh_token`
- `explicit_auth`
- thong tin user: id, email, display name, avatar, role, created at

`ApiClient.AuthInterceptor` tu dong them header:

```text
Authorization: Bearer <accessToken>
```

Neu API tra `401` hoac `403`, cac ham nhu `withPlaylistAuth()` se thu:

```text
refreshAccessToken()
  -> POST /api/auth/refresh
  -> luu access token moi
  -> goi lai API ban dau
```

Neu refresh fail thi xoa session va quay ve guest mode.

## 8. Luong guest user

App co co che guest/no profile:

1. Neu chua dang nhap ro rang, app co the tao credential guest theo `ANDROID_ID`.
2. Email guest co dang `guest_<suffix>@cinderssoul.local`.
3. Guest library co `userId = 0` tren local Room.
4. Cac du lieu nhu downloaded songs, listening history duoc tach theo `userId`.

Y nghia:

- User chua dang nhap van co the nghe nhac va co state local.
- Khi dang nhap that, library owner chuyen sang id user backend.
- Favorite/library cua account duoc dong bo qua backend.

## 9. Luong phat nhac

File lien quan:

- `MusicViewModel.kt`
- `PlaybackNotificationManager.kt`
- `ui/player/NowPlayingScreen.kt`
- `ui/navigation/BottomBar.kt`

Luong bam play:

```text
User bam song
  -> viewModel.playOrToggleSong(song)
  -> neu song dang phat: togglePlayback()
  -> neu song moi: playSongAt(index)
  -> playableUriFor(song)
  -> buildMediaItem(song, uri)
  -> exoPlayer.setMediaItem()
  -> exoPlayer.prepare()
  -> exoPlayer.play()
  -> uiState.currentSong = song
  -> persistCurrentSongState()
```

Player listener xu ly:

- `onIsPlayingChanged`: cap nhat `isPlayerRunning`.
- `onPlaybackStateChanged`: neu het bai va repeat off thi next.
- `onMediaItemTransition`: dong bo state va luu bai hien tai.
- `onPlayerError`: hien playback error.

Progress:

```text
startProgressUpdates()
  -> job lap moi 1s khi dang play, 2.5s khi idle
  -> syncPlayerState()
  -> luu position theo bucket
```

Cache audio:

- Dung `SimpleCache` trong `cacheDir/audio_stream_cache`.
- Gioi han cache audio la 160 MB.
- `CacheDataSource` doc tu cache truoc, loi thi bo qua cache va lay network.

Notification/media session:

- `MediaSession` boc `ForwardingPlayer`.
- Override next/previous de goi `playNextSong()` va `playPreviousSong()`.
- `PlaybackNotificationManager` hien notification dieu khien phat nhac.

## 10. Luong favorite/library

Co 2 khai niem can phan biet:

- Favorite playlist trong UI: playlist dac biet id `-100`, tao tu `favoriteSongIds`.
- Account library tren backend: bang `user_library_songs`.

Luong like bai hat:

```text
User bam like
  -> toggleCurrentSongLiked()
  -> addSongToFavorites(song) hoac removeSongFromFavorites(song)
  -> cap nhat favoriteSongIds va uiState ngay
  -> neu user dang nhap:
       LibraryRepository.addSongToLibrary(songId)
       hoac LibraryRepository.removeSongFromLibrary(songId)
  -> neu fail thi rollback/thong bao tuy logic hien tai
```

Backend API:

- `GET /api/library/songs`
- `POST /api/library/songs`
- `DELETE /api/library/songs/:songId`

Room van luu favorite cu de migration va guest/local state. Backend moi la nguon chinh cho account library khi da login.

## 11. Luong playlist

File lien quan:

- UI: `MusicApp.kt`, `LibraryScreens.kt`, `Dialogs.kt`
- Android data: `PlaylistRepository.kt`
- Backend: `playlist.routes.js`, `playlist.controller.js`, `Playlist.js`, `PlaylistSong.js`

Tao playlist:

```text
User mo CreatePlaylistDialog
  -> viewModel.createPlaylist(name, description, imageUri)
  -> neu co imageUri: uploadPlaylistCoverImage()
  -> POST /api/upload/image
  -> lay image URL
  -> PlaylistRepository.createPlaylist(...)
  -> POST /api/playlists
  -> backend tao playlist cho req.user.id
  -> cap nhat backendPlaylists/customPlaylists/uiState
```

Them bai vao playlist:

```text
User chon Add to playlist
  -> viewModel.addSongToPlaylist(song, playlistId)
  -> cap nhat UI tam thoi
  -> PlaylistRepository.addSongToPlaylist(playlistId, song.id)
  -> POST /api/playlists/:id/songs
  -> syncPlaylistFromBackend(playlistId)
```

Sua playlist:

```text
EditPlaylistDialog
  -> viewModel.updatePlaylistDetails(...)
  -> upload image neu co
  -> PUT /api/playlists/:id
  -> applyPlaylistUpdate()
```

Xoa playlist:

```text
PlaylistDetailScreen
  -> viewModel.deletePlaylist(playlistId)
  -> DELETE /api/playlists/:id
  -> xoa khoi state local
```

## 12. Luong search, discover, home

Search:

```text
SearchTab
  -> onQueryChange
  -> viewModel.onSearchQueryChange(query)
  -> uiState.searchQuery cap nhat
  -> UI filter songs/artists/albums theo query
```

Home va Discover chu yeu dung du lieu san co trong `uiState`:

- `songs`
- `artists`
- `albums`
- playlist duoc compose tu `composePlaylists()`

Detail artist/album/genre khong goi API rieng trong UI. `MusicApp` loc tu danh sach da tai:

```text
openedArtistSongs = detailSongs.filter { it.artistId == artist.id }
openedAlbumSongs = detailSongs.filter { it.albumId == album.id }
openedGenreSongs = detailSongs.filter { it.genre equals genre }
```

## 13. Luong download bai hat

File chinh: `MusicViewModel.kt`

Luong:

```text
User bam download song
  -> viewModel.downloadSong(song)
  -> lay playable URL
  -> DownloadManager enqueue request
  -> file luu vao thu muc Music/...
  -> markSongDownloaded(song.id, localUri?)
  -> luu downloadedSongIds trong SharedPreferences
  -> upsert DownloadedSongEntity vao Room
```

Download playlist:

```text
downloadPlaylist(playlist)
  -> lap qua playlist.songs
  -> goi downloadSong(song)
  -> tra ve so bai duoc enqueue
```

## 14. Luong upload anh

Upload anh avatar/playlist cover:

```text
UI mo picker image/*
  -> Android tra Uri
  -> viewModel uploadPlaylistCoverImage(imageUri)
  -> copy Uri vao temp file trong cacheDir
  -> tao MultipartBody.Part ten field "image"
  -> POST /api/upload/image
  -> backend multer luu file vao backend/uploads
  -> tra URL
  -> Android normalizeBackendUrl(url)
  -> dung URL de update user/playlist
```

Endpoint:

- `POST /api/upload/image`

Static files:

- `/uploads` map toi `backend/uploads`
- `/assets` map toi `backend/src/assets`

## 15. Luong admin

File Android:

- `admin/AdminActivity.kt`
- `admin/AdminViewModel.kt`
- `admin/AdminUiState.kt`
- `repository/AdminRepository.kt`

Dieu kien vao admin:

- `MainActivity.hasAdminSession()` doc role trong `SharedPreferences`.
- Neu role la `admin`, app mo `AdminActivity`.
- Trong `MusicApp`, neu `uiState.authUser?.isAdmin == true`, cung chuyen sang `AdminActivity`.

Admin API:

- `GET /api/admin/summary`
- `GET /api/admin/users`
- `POST /api/admin/users`
- `PUT /api/admin/users/:id`
- `DELETE /api/admin/users/:id`

Admin repository cung goi CRUD songs, artists, albums:

- `GET/POST/PUT/DELETE /api/songs`
- `GET/POST/PUT/DELETE /api/artists`
- `GET/POST/PUT/DELETE /api/albums`

Backend can middleware auth/admin guard trong `admin.routes.js` de bao ve cac endpoint nay.

## 16. Backend: Express request lifecycle

File chinh: `backend/src/server.js`

Khi server start:

1. Load `.env`.
2. Tao Express app.
3. Set `trust proxy`.
4. Gan middleware:
   - `helmet`
   - `cors`
   - `morgan`
   - `express.json`
   - `express.urlencoded`
5. Serve static:
   - `/assets`
   - `/uploads`
6. Mount routes:
   - `/api/auth`
   - `/api/admin`
   - `/api/users`
   - `/api/songs`
   - `/api/playlists`
   - `/api/library`
   - `/api/albums`
   - `/api/artists`
   - `/api/upload`
   - `/share`
7. Health check: `/health`.
8. 404 handler.
9. Global error handler.
10. Truoc khi listen, goi `ensureAccountLibrarySchema()`.

```text
Request
  -> Express middleware
  -> route
  -> auth/validator/upload middleware neu co
  -> controller
  -> service/model
  -> response JSON
  -> errorHandler neu throw/next(error)
```

## 17. Backend models va quan he database

File chinh: `backend/src/models/index.js`

Models:

- `User`
- `Artist`
- `Album`
- `Song`
- `Playlist`
- `PlaylistSong`
- `UserLibrarySong`
- `RefreshToken`

Quan he:

```text
User 1-n Playlist
User 1-n RefreshToken
User n-n Song qua UserLibrarySong

Artist 1-n Album
Artist 1-n Song

Album n-1 Artist
Album 1-n Song

Song n-1 Artist
Song n-1 Album
Song n-n Playlist qua PlaylistSong

Playlist n-1 User
Playlist n-n Song qua PlaylistSong
```

`ensureAccountLibrarySchema()` dam bao:

- Bang `users` co cot `role`.
- User `admin@example.com` co role `admin`.
- Tao bang `user_library_songs` neu chua co.
- Neu co bang cu `favorites`, copy du lieu sang `user_library_songs`.

## 18. API Android dang goi

File: `app/src/main/java/com/example/cinderssoul/network/ApiService.kt`

Auth:

- `POST api/auth/login`
- `POST api/auth/register`
- `POST api/auth/google`
- `POST api/auth/refresh`
- `POST api/auth/forgot-password`
- `POST api/auth/reset-password`
- `GET api/auth/me`

User:

- `PUT api/users/{id}`

Admin:

- `GET api/admin/summary`
- `GET api/admin/users`
- `POST api/admin/users`
- `PUT api/admin/users/{id}`
- `DELETE api/admin/users/{id}`

Songs:

- `GET api/songs?search=&genre=&limit=&offset=`
- `POST api/songs`
- `PUT api/songs/{id}`
- `DELETE api/songs/{id}`

Artists:

- `GET api/artists`
- `POST api/artists`
- `PUT api/artists/{id}`
- `DELETE api/artists/{id}`

Albums:

- `GET api/albums`
- `POST api/albums`
- `PUT api/albums/{id}`
- `DELETE api/albums/{id}`

Playlists:

- `GET api/playlists`
- `GET api/playlists/{id}`
- `POST api/playlists`
- `PUT api/playlists/{id}`
- `DELETE api/playlists/{id}`
- `POST api/playlists/{id}/songs`
- `DELETE api/playlists/{id}/songs/{songId}`

Library:

- `GET api/library/songs`
- `POST api/library/songs`
- `DELETE api/library/songs/{songId}`

Upload:

- `POST api/upload/image`

## 19. Cau hinh base URL va moi truong

Android doc bien tu `backend/.env`, Gradle property hoac environment:

- `BACKEND_BASE_URL`
- `SHARE_BASE_URL`
- `APP_PUBLIC_URL`
- `GOOGLE_SERVER_CLIENT_ID`
- `GOOGLE_CLIENT_ID`

Neu khong cau hinh `BACKEND_BASE_URL`, `ApiClient` chon mac dinh:

- Emulator Android: `http://10.0.2.2:3000/`, sau do `http://10.0.3.2:3000/`
- May that/khac: `http://localhost:3000/`

`ApiClient` co interceptor thu nhieu base URL. Neu request fail do ket noi, no thu candidate tiep theo va cap nhat `activeBaseUrl`.

## 20. Cach chay du an

Backend:

```bash
cd backend
npm install
npm run dev
```

Hoac:

```bash
cd backend
npm start
```

Kiem tra backend:

```bash
curl http://localhost:3000/health
```

Android:

```bash
./gradlew assembleDebug
```

Tren Windows:

```bash
gradlew.bat assembleDebug
```

Neu chay emulator Android va backend o may host, nen de:

```env
BACKEND_BASE_URL=http://10.0.2.2:3000
```

Neu chay tren dien thoai that, can thay bang IP LAN cua may chay backend, vi `localhost` tren dien thoai la chinh dien thoai:

```env
BACKEND_BASE_URL=http://<IP-LAN-cua-may>:3000
```

## 21. Checklist khi sua code

Khi them API moi:

1. Them route backend trong `backend/src/routes`.
2. Them controller/service/model neu can.
3. Them method vao `ApiService.kt`.
4. Them DTO vao `ApiModels.kt`.
5. Them repository method neu UI can goi qua repository.
6. Goi tu ViewModel, cap nhat `uiState`.
7. Render trong Compose screen.

Khi them state UI moi:

1. Them field vao `MusicUiState.kt` hoac `AdminUiState.kt`.
2. Chi cap nhat state trong ViewModel.
3. Truyen state xuong Composable can dung.
4. Truyen callback tu UI len ViewModel, khong de UI goi API truc tiep.

Khi them bang MySQL moi:

1. Tao Sequelize model trong `backend/src/models`.
2. Import model vao `models/index.js`.
3. Dinh nghia relationship.
4. Tao route/controller.
5. Neu can migration tu dong, them vao ham dam bao schema rieng.

Khi them du lieu cache local:

1. Them Room entity trong `CachedMusicEntities.kt`.
2. Them DAO method trong `MusicCacheDao.kt`.
3. Tang version `CinderDatabase`.
4. Them migration.
5. Goi DAO tu repository/ViewModel trong `Dispatchers.IO`.

## 22. Nhung diem can chu y

- `MusicViewModel.kt` dang la file rat lon, nen khi sua nen giu pham vi nho, tranh refactor lan rong.
- `SharedPreferences` dang luu ca token va user cache. Khi doi key phai co migration/logic fallback.
- `Room` dang tach du lieu theo `userId`, guest la `0`.
- Favorite UI id `-100` la playlist ao, khong phai id backend.
- Quick playlists `Recently Added`, `Most Played`, `Chill Mix` co id am va tao local.
- Backend account library nam trong `user_library_songs`, khong nen dung bang `favorites` cu cho logic moi.
- Khi chay tren emulator, backend host phai la `10.0.2.2`, khong phai `localhost`.
- Upload image tra URL tu backend, Android can `normalizeBackendUrl()` de dung duoc tren emulator/device.
- Cac API can auth phu thuoc `Authorization: Bearer ...`; neu token het han phai refresh truoc khi retry.

## 23. Ban do luong nhanh

Dang nhap:

```text
AccountAuthDialog -> MusicViewModel.signInWithEmail
  -> ApiService.login -> /api/auth/login
  -> saveAuthSession -> refreshData -> UI update
```

Tai home:

```text
MusicViewModel.init -> refreshData
  -> SongRepository + PlaylistRepository + LibraryRepository
  -> Room fallback/cache
  -> uiState.songs/artists/albums/playlists
  -> HomeTab/SearchTab/DiscoverTab/LibraryTab
```

Phat nhac:

```text
Song card -> playOrToggleSong -> playSongAt
  -> ExoPlayer.setMediaItem/prepare/play
  -> Player.Listener -> syncPlayerState
  -> NowPlaying + BottomBar + Notification
```

Them playlist:

```text
CreatePlaylistDialog -> createPlaylist
  -> optional upload image
  -> POST /api/playlists
  -> update uiState.playlists
```

Like bai hat:

```text
NowPlaying/song row -> toggleCurrentSongLiked
  -> add/remove favorites locally
  -> POST/DELETE /api/library/songs
  -> Favorite playlist ao duoc compose lai
```

Admin:

```text
Login admin -> cache role admin
  -> MainActivity/MusicApp chuyen AdminActivity
  -> AdminViewModel -> AdminRepository
  -> /api/admin + CRUD songs/artists/albums
```

