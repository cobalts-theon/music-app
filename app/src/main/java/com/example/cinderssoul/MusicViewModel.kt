package com.example.cinderssoul

import android.app.DownloadManager
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.webkit.URLUtil
import android.webkit.MimeTypeMap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.cinderssoul.local.DownloadedSongEntity
import com.example.cinderssoul.local.FavoriteSongEntity
import com.example.cinderssoul.local.ListeningHistoryEntity
import com.example.cinderssoul.local.MusicCacheDao
import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Playlist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.models.User
import com.example.cinderssoul.network.ApiClient
import com.example.cinderssoul.network.ApiEnvelope
import com.example.cinderssoul.network.ForgotPasswordRequest
import com.example.cinderssoul.network.GoogleAuthRequest
import com.example.cinderssoul.network.LoginRequest
import com.example.cinderssoul.network.RefreshTokenRequest
import com.example.cinderssoul.network.RegisterRequest
import com.example.cinderssoul.network.ResetPasswordRequest
import com.example.cinderssoul.network.UpdateUserRequest
import com.example.cinderssoul.network.toDomainUser
import com.example.cinderssoul.repository.SongRepository
import com.example.cinderssoul.repository.PlaylistRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import kotlin.random.Random

class MusicViewModel(
    application: Application,
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository = PlaylistRepository(),
    private val musicCacheDao: MusicCacheDao? = null
) : AndroidViewModel(application) {
    private data class RestoredPlayback(
        val song: Song,
        val index: Int,
        val positionMs: Long
    )

    companion object {
        private const val PLAYER_PREFS = "player_state"
        private const val KEY_LAST_SONG_ID = "last_song_id"
        private const val KEY_LAST_POSITION_MS = "last_position_ms"
        private const val KEY_DOWNLOADED_SONG_IDS = "downloaded_song_ids"
        private const val KEY_API_ACCESS_TOKEN = "api_access_token"
        private const val KEY_API_REFRESH_TOKEN = "api_refresh_token"
        private const val KEY_APP_ACCOUNT_EMAIL = "app_account_email"
        private const val KEY_APP_ACCOUNT_PASSWORD = "app_account_password"
        private const val KEY_EXPLICIT_AUTH = "explicit_auth"
        private const val FAVORITE_PLAYLIST_ID = -100
        private const val HISTORY_LIMIT = 200
    }

    private val mediaHttpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(20_000)
        .setReadTimeoutMs(45_000)
        .setUserAgent("CindersSoul/1.0")

    private val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            30_000,
            120_000,
            2_500,
            5_000
        )
        .setPrioritizeTimeOverSizeThresholds(true)
        .build()

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application)
        .setMediaSourceFactory(DefaultMediaSourceFactory(mediaHttpDataSourceFactory))
        .setLoadControl(loadControl)
        .build()
        .apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            setHandleAudioBecomingNoisy(true)
            volume = 0.85f
        }

    private var downloadedSongIds: Set<Int> = emptySet()

    private val _uiState = mutableStateOf(MusicUiState(volume = exoPlayer.volume))
    val uiState: State<MusicUiState> = _uiState

    private var progressJob: Job? = null
    private var spinEffectJob: Job? = null
    private val playerPrefs = application.getSharedPreferences(PLAYER_PREFS, Context.MODE_PRIVATE)
    private var hasTriedRestorePlayback = false
    private var persistedPositionBucket = -1L

    private var backendPlaylists: List<Playlist> = emptyList()
    private var customPlaylists: List<Playlist> = emptyList()
    private val playlistOverrides = mutableMapOf<Int, Playlist>()
    private var favoriteSongIds: List<Int> = emptyList()

    init {
        ApiClient.setAccessToken(playerPrefs.getString(KEY_API_ACCESS_TOKEN, null))
        downloadedSongIds = loadDownloadedSongIds()
        _uiState.value = _uiState.value.copy(downloadedSongIds = downloadedSongIds)
        restoreOfflineLibraryState()
        restoreExplicitAuthUser()

        exoPlayer.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.value = _uiState.value.copy(isPlayerRunning = isPlaying)
                    persistCurrentSongState()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    syncPlayerState()
                    if (playbackState == Player.STATE_ENDED && _uiState.value.repeatMode == RepeatUiMode.OFF) {
                        playNextSong()
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    syncPlayerState()
                    persistCurrentSongState()
                }

                override fun onPlayerError(error: PlaybackException) {
                    reportPlaybackError("Unable to stream this song.", error)
                }
            }
        )

        startProgressUpdates()
        refreshData()
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            setAuthMessage("Email and password are required.")
            return
        }

        viewModelScope.launch {
            runAuthAction {
                val response = ApiClient.apiService.login(
                    LoginRequest(email = email.trim(), password = password)
                )
                val authData = response.data ?: throw IllegalStateException(response.message ?: "Login failed")
                applyAuthSession(authData, explicitUser = true)
                "Logged in successfully."
            }
        }
    }

    fun registerWithEmail(email: String, password: String, displayName: String) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            setAuthMessage("Name, email, and password are required.")
            return
        }

        viewModelScope.launch {
            runAuthAction {
                val response = ApiClient.apiService.register(
                    RegisterRequest(
                        email = email.trim(),
                        password = password,
                        displayName = displayName.trim()
                    )
                )
                val authData = response.data ?: throw IllegalStateException(response.message ?: "Registration failed")
                applyAuthSession(authData, explicitUser = true)
                "Account created."
            }
        }
    }

    fun signInWithGoogleIdToken(idToken: String) {
        if (idToken.isBlank()) {
            setAuthMessage("Google sign-in did not return a token.")
            return
        }

        viewModelScope.launch {
            runAuthAction {
                val response = ApiClient.apiService.googleAuth(GoogleAuthRequest(idToken = idToken))
                val authData = response.data ?: throw IllegalStateException(response.message ?: "Google login failed")
                applyAuthSession(authData, explicitUser = true)
                "Logged in with Google."
            }
        }
    }

    fun requestPasswordResetOtp(email: String) {
        if (email.isBlank()) {
            setAuthMessage("Email is required.")
            return
        }

        viewModelScope.launch {
            runAuthAction {
                val response = ApiClient.apiService.requestPasswordResetOtp(
                    ForgotPasswordRequest(email = email.trim())
                )
                val data = response.data ?: throw IllegalStateException(response.message ?: "Unable to send OTP")
                _uiState.value = _uiState.value.copy(passwordResetDevOtp = data.devOtp)
                data.devOtp?.let { "OTP sent. Dev code: $it" } ?: "OTP sent."
            }
        }
    }

    fun resetPasswordWithOtp(email: String, otp: String, newPassword: String) {
        if (email.isBlank() || otp.isBlank() || newPassword.isBlank()) {
            setAuthMessage("Email, OTP, and new password are required.")
            return
        }

        viewModelScope.launch {
            runAuthAction {
                val response = ApiClient.apiService.resetPassword(
                    ResetPasswordRequest(
                        email = email.trim(),
                        otp = otp.trim(),
                        newPassword = newPassword
                    )
                )
                val authData = response.data ?: throw IllegalStateException(response.message ?: "Unable to reset password")
                applyAuthSession(authData, explicitUser = true)
                "Password reset successfully."
            }
        }
    }

    fun updateProfile(displayName: String? = null, avatarUrl: String? = null) {
        val user = _uiState.value.authUser ?: return

        viewModelScope.launch {
            runAuthAction {
                val response = ApiClient.apiService.updateUser(
                    userId = user.id,
                    request = UpdateUserRequest(
                        displayName = displayName?.trim()?.takeIf { it.isNotBlank() },
                        avatarUrl = avatarUrl
                    )
                )
                val updatedUser = response.requireData().toDomainUser()
                _uiState.value = _uiState.value.copy(authUser = updatedUser)
                "Profile updated."
            }
        }
    }

    fun setProfileAvatarFromLibrary(imageUri: Uri?) {
        if (imageUri == null || _uiState.value.authUser == null) return

        viewModelScope.launch {
            runAuthAction {
                val uploadedUrl = uploadPlaylistCoverImage(imageUri).getOrThrow()
                val response = ApiClient.apiService.updateUser(
                    userId = _uiState.value.authUser?.id ?: throw IllegalStateException("Not logged in"),
                    request = UpdateUserRequest(avatarUrl = uploadedUrl)
                )
                val updatedUser = response.requireData().toDomainUser()
                _uiState.value = _uiState.value.copy(authUser = updatedUser)
                "Avatar updated."
            }
        }
    }

    fun logout() {
        clearStoredAuthSession()
    }

    fun clearAuthMessage() {
        _uiState.value = _uiState.value.copy(authMessage = null)
    }

    fun reportAuthMessage(message: String) {
        setAuthMessage(message)
    }

    fun refreshAuthUser() {
        restoreExplicitAuthUser(showLoading = false)
    }

    private fun restoreExplicitAuthUser(showLoading: Boolean = true) {
        val hasExplicitAuth = playerPrefs.getBoolean(KEY_EXPLICIT_AUTH, false)
        if (!hasExplicitAuth) return

        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = _uiState.value.copy(isAuthLoading = true, authMessage = null)
            }

            val userResult = fetchCurrentUserWithRetry()
            val user = userResult.getOrNull()
            val error = userResult.exceptionOrNull()

            if (showLoading) {
                _uiState.value = _uiState.value.copy(
                    isAuthLoading = false,
                    authUser = user ?: _uiState.value.authUser,
                    authMessage = if (user != null) null else error?.message
                )
            } else {
                if (user != null) {
                    _uiState.value = _uiState.value.copy(authUser = user, authMessage = null)
                } else if (error != null && !error.isUnauthorized()) {
                    _uiState.value = _uiState.value.copy(authMessage = error.message)
                }
            }
        }
    }

    private suspend fun fetchCurrentUserWithRetry(): Result<User> {
        val authReady = ensurePlaylistAuth()
        if (authReady.isFailure) {
            val exception = authReady.exceptionOrNull() ?: IllegalStateException("Please log in first.")
            return Result.failure(exception)
        }

        var result = runCatching {
            val response = ApiClient.apiService.getMe()
            response.requireData().user.toDomainUser()
        }

        if (result.isFailure && result.exceptionOrNull().isUnauthorized()) {
            val refreshed = refreshAccessToken()
            if (refreshed.isFailure) {
                clearStoredAuthSession()
                val exception = refreshed.exceptionOrNull() ?: IllegalStateException("Session expired. Please log in again.")
                return Result.failure(exception)
            }

            result = runCatching {
                val response = ApiClient.apiService.getMe()
                response.requireData().user.toDomainUser()
            }

            if (result.isFailure && result.exceptionOrNull().isUnauthorized()) {
                clearStoredAuthSession()
            }
        }

        return result
    }

    private suspend fun runAuthAction(action: suspend () -> String?) {
        _uiState.value = _uiState.value.copy(isAuthLoading = true, authMessage = null)
        val result = runCatching { action() }
        _uiState.value = _uiState.value.copy(
            isAuthLoading = false,
            authMessage = result.getOrElse { it.message ?: "Authentication failed" }
        )
    }

    private fun setAuthMessage(message: String) {
        _uiState.value = _uiState.value.copy(authMessage = message)
    }

    private fun applyAuthSession(authData: com.example.cinderssoul.network.AuthDataDto, explicitUser: Boolean) {
        saveAuthSession(authData.accessToken, authData.refreshToken, explicitUser = explicitUser)
        _uiState.value = _uiState.value.copy(
            authUser = authData.user.toDomainUser(),
            passwordResetDevOtp = null
        )
    }

    private fun restoreOfflineLibraryState() {
        val dao = musicCacheDao ?: return
        val legacyDownloadedIds = downloadedSongIds

        viewModelScope.launch {
            val (roomFavoriteIds, roomDownloadedIds) = withContext(Dispatchers.IO) {
                legacyDownloadedIds.forEach { songId ->
                    dao.upsertDownloadedSong(DownloadedSongEntity(songId = songId))
                }
                dao.getFavoriteSongIds() to dao.getDownloadedSongIds()
            }

            favoriteSongIds = roomFavoriteIds
            downloadedSongIds = (roomDownloadedIds + legacyDownloadedIds).toSet()
            _uiState.value = _uiState.value.copy(
                downloadedSongIds = downloadedSongIds,
                isCurrentLiked = isFavoriteSong(_uiState.value.currentSong)
            )
            updatePlaylistState()
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val songsDeferred = async { songRepository.getAllSongs(limit = 80) }
            val artistsDeferred = async { songRepository.getAllArtists(limit = 20) }
            val albumsDeferred = async { songRepository.getAllAlbums(limit = 20) }
            val playlistsDeferred = async {
                withPlaylistAuth {
                    playlistRepository.getUserPlaylists()
                }
            }

            val songsResult = songsDeferred.await()
            val artistsResult = artistsDeferred.await()
            val albumsResult = albumsDeferred.await()
            val playlistsResult = playlistsDeferred.await()

            val error = listOf(songsResult, artistsResult, albumsResult)
                .firstOrNull { it.isFailure }
                ?.exceptionOrNull()
                ?.message

            val songs = songsResult.getOrDefault(emptyList())
            playlistsResult.getOrNull()?.let { fetchedPlaylists ->
                backendPlaylists = fetchedPlaylists
            }
            val playlists = composePlaylists(songs)
            val currentSongId = _uiState.value.currentSong?.id
            val refreshedCurrentSong = songs.firstOrNull { it.id == currentSongId }
            val refreshedCurrentIndex = songs.indexOfFirst { it.id == currentSongId }
            val restoredPlayback = if (currentSongId == null) restoreSavedPlayback(songs) else null
            val currentSong = refreshedCurrentSong ?: restoredPlayback?.song ?: _uiState.value.currentSong
            val currentSongIndex = when {
                refreshedCurrentIndex >= 0 -> refreshedCurrentIndex
                restoredPlayback != null -> restoredPlayback.index
                else -> _uiState.value.currentSongIndex
            }
            val positionMs = restoredPlayback?.positionMs ?: _uiState.value.positionMs

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = error,
                songs = songs,
                artists = artistsResult.getOrDefault(emptyList()),
                albums = albumsResult.getOrDefault(emptyList()),
                playlists = playlists,
                currentSong = currentSong,
                currentSongIndex = currentSongIndex,
                positionMs = positionMs,
                isCurrentLiked = isFavoriteSong(currentSong),
                downloadedSongIds = downloadedSongIds
            )

            if (restoredPlayback != null) {
                prepareRestoredSong(restoredPlayback)
            }
        }
    }

    fun createPlaylist(
        name: String,
        description: String?,
        imageUri: Uri? = null,
        onResult: (Playlist?) -> Unit = {}
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            onResult(null)
            return
        }

        viewModelScope.launch {
            val uploadedCoverUrl = imageUri?.let { selectedImage ->
                withPlaylistAuth {
                    uploadPlaylistCoverImage(selectedImage)
                }.getOrNull()
            }

            val result = withPlaylistAuth {
                playlistRepository.createPlaylist(
                    name = trimmedName,
                    description = description?.trim()?.takeIf { it.isNotBlank() },
                    coverUrl = uploadedCoverUrl,
                    isPublic = true
                )
            }

            val created = result.getOrNull()
            if (created != null) {
                backendPlaylists = listOf(created) + backendPlaylists.filterNot { it.id == created.id }
                updatePlaylistState()
            }
            onResult(created)
        }
    }

    fun addSongToPlaylist(song: Song, playlistId: Int): Boolean {
        if (playlistId == FAVORITE_PLAYLIST_ID) {
            return addSongToFavorites(song)
        }

        val existing = _uiState.value.playlists.firstOrNull { it.id == playlistId } ?: return false
        if (existing.songs.any { it.id == song.id }) return false

        val updated = existing.copy(
            coverUrl = existing.coverUrl ?: song.coverUrl,
            songs = existing.songs + song
        )

        applyPlaylistUpdate(updated)
        if (playlistId > 0) {
            viewModelScope.launch {
                val result = withPlaylistAuth {
                    playlistRepository.addSongToPlaylist(playlistId, song.id)
                }

                if (result.isSuccess) {
                    syncPlaylistFromBackend(playlistId)
                } else {
                    applyPlaylistUpdate(existing)
                }
            }
        }
        return true
    }

    fun removeSongFromPlaylist(song: Song, playlistId: Int): Boolean {
        if (playlistId == FAVORITE_PLAYLIST_ID) {
            return removeSongFromFavorites(song)
        }

        val existing = _uiState.value.playlists.firstOrNull { it.id == playlistId } ?: return false
        if (!existing.songs.any { it.id == song.id }) return false

        val updated = existing.copy(
            songs = existing.songs.filterNot { it.id == song.id }
        )

        applyPlaylistUpdate(updated)
        if (playlistId > 0) {
            viewModelScope.launch {
                val result = withPlaylistAuth {
                    playlistRepository.removeSongFromPlaylist(playlistId, song.id)
                }

                if (result.isSuccess) {
                    syncPlaylistFromBackend(playlistId)
                } else {
                    applyPlaylistUpdate(existing)
                }
            }
        }
        return true
    }

    private fun addSongToFavorites(song: Song): Boolean {
        if (song.id in favoriteSongIds) return false

        favoriteSongIds = listOf(song.id) + favoriteSongIds.filterNot { it == song.id }
        _uiState.value = _uiState.value.copy(
            isCurrentLiked = if (_uiState.value.currentSong?.id == song.id) {
                true
            } else {
                _uiState.value.isCurrentLiked
            }
        )
        updatePlaylistState()
        persistFavoriteSong(song.id, isFavorite = true)
        return true
    }

    private fun removeSongFromFavorites(song: Song): Boolean {
        if (song.id !in favoriteSongIds) return false

        favoriteSongIds = favoriteSongIds.filterNot { it == song.id }
        _uiState.value = _uiState.value.copy(
            isCurrentLiked = if (_uiState.value.currentSong?.id == song.id) {
                false
            } else {
                _uiState.value.isCurrentLiked
            }
        )
        updatePlaylistState()
        persistFavoriteSong(song.id, isFavorite = false)
        return true
    }

    private fun persistFavoriteSong(songId: Int, isFavorite: Boolean) {
        val dao = musicCacheDao ?: return
        viewModelScope.launch(Dispatchers.IO) {
            if (isFavorite) {
                dao.upsertFavoriteSong(FavoriteSongEntity(songId = songId))
            } else {
                dao.deleteFavoriteSong(songId)
            }
        }
    }

    fun setPlaylistCoverFromLibrary(playlistId: Int, imageUri: Uri?): Boolean {
        val existing = _uiState.value.playlists.firstOrNull { it.id == playlistId } ?: return false
        val selectedImageUri = imageUri ?: return false
        val localCover = selectedImageUri.toString()
        val updated = existing.copy(coverUrl = localCover)

        applyPlaylistUpdate(updated)
        if (playlistId > 0) {
            viewModelScope.launch {
                val uploadResult = withPlaylistAuth {
                    uploadPlaylistCoverImage(selectedImageUri)
                }

                val uploadedUrl = uploadResult.getOrNull() ?: return@launch
                val updateResult = withPlaylistAuth {
                    playlistRepository.updatePlaylist(playlistId = playlistId, coverUrl = uploadedUrl)
                }
                updateResult.getOrNull()?.let { synced ->
                    val hydrated = synced.copy(
                        songs = synced.songs.takeIf { it.isNotEmpty() } ?: updated.songs
                    )
                    applyPlaylistUpdate(hydrated)
                }
            }
        }
        return true
    }

    fun updatePlaylistDetails(
        playlistId: Int,
        name: String,
        description: String?,
        imageUri: Uri? = null
    ): Boolean {
        if (playlistId <= 0) return false

        val existing = _uiState.value.playlists.firstOrNull { it.id == playlistId } ?: return false
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return false

        val requestDescription = description?.trim().orEmpty()
        val displayDescription = requestDescription.takeIf { it.isNotBlank() }
        val localCover = imageUri?.toString()
        val optimistic = existing.copy(
            name = trimmedName,
            description = displayDescription,
            coverUrl = localCover ?: existing.coverUrl
        )

        applyPlaylistUpdate(optimistic)
        viewModelScope.launch {
            val uploadedCoverUrl = imageUri?.let { selectedImage ->
                val uploadResult = withPlaylistAuth {
                    uploadPlaylistCoverImage(selectedImage)
                }
                if (uploadResult.isFailure) {
                    applyPlaylistUpdate(existing)
                    return@launch
                }
                uploadResult.getOrNull()
            }

            val updateResult = withPlaylistAuth {
                playlistRepository.updatePlaylist(
                    playlistId = playlistId,
                    name = trimmedName,
                    description = requestDescription,
                    coverUrl = uploadedCoverUrl
                )
            }

            updateResult
                .onSuccess { synced ->
                    val hydrated = synced.copy(
                        songs = synced.songs.takeIf { it.isNotEmpty() } ?: optimistic.songs
                    )
                    backendPlaylists = backendPlaylists.map { if (it.id == hydrated.id) hydrated else it }
                    applyPlaylistUpdate(hydrated)
                }
                .onFailure {
                    applyPlaylistUpdate(existing)
                }
        }
        return true
    }

    fun deletePlaylist(playlistId: Int): Boolean {
        if (playlistId == FAVORITE_PLAYLIST_ID) return false

        val existing = _uiState.value.playlists.firstOrNull { it.id == playlistId } ?: return false
        
        val remainingPlaylists = _uiState.value.playlists.filterNot { it.id == playlistId }
        _uiState.value = _uiState.value.copy(playlists = remainingPlaylists)
        
        if (playlistId > 0) {
            viewModelScope.launch {
                val result = withPlaylistAuth {
                    playlistRepository.deletePlaylist(playlistId)
                }
                
                if (result.isFailure) {
                    val allPlaylists = _uiState.value.playlists + existing
                    _uiState.value = _uiState.value.copy(playlists = allPlaylists)
                }
            }
        }
        return true
    }

    fun downloadSong(song: Song): Boolean {
        if (song.audioUrl.isBlank()) return false

        val uri = Uri.parse(song.audioUrl)
        val scheme = uri.scheme?.lowercase() ?: return false
        if (scheme == "file" || scheme == "content") {
            markSongDownloaded(song.id, song.audioUrl)
            return true
        }
        if (scheme != "http" && scheme != "https") return false

        val fileName = URLUtil.guessFileName(song.audioUrl, null, null)
            .ifBlank { "song_${song.id}.mp3" }
        val request = DownloadManager.Request(uri)
            .setTitle(song.title)
            .setDescription(song.artistName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(getApplication(), Environment.DIRECTORY_MUSIC, fileName)

        val downloadManager = getApplication<Application>()
            .getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        markSongDownloaded(song.id)
        return true
    }

    fun downloadPlaylist(playlist: Playlist): Int {
        var successCount = 0
        playlist.songs.forEach { song ->
            if (downloadSong(song)) successCount++
        }
        return successCount
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun playOrToggleSong(song: Song) {
        try {
            val currentSong = _uiState.value.currentSong
            val isSameSong = currentSong?.id == song.id

            if (isSameSong) {
                val index = _uiState.value.songs.indexOfFirst { it.id == song.id }
                if (exoPlayer.playerError != null || exoPlayer.playbackState == Player.STATE_IDLE) {
                    if (index >= 0) {
                        playSongAt(index = index, startPositionMs = _uiState.value.positionMs)
                    }
                } else {
                    togglePlayback()
                }
                return
            }

            val index = _uiState.value.songs.indexOfFirst { it.id == song.id }
            if (index >= 0) playSongAt(index)
        } catch (exception: Exception) {
            reportPlaybackError("Unable to play this song.", exception)
        }
    }

    fun playNextSong() {
        val songs = _uiState.value.songs
        if (songs.isEmpty()) return

        val currentIndex = _uiState.value.currentSongIndex.takeIf { it in songs.indices } ?: -1
        val nextIndex = if (_uiState.value.isShuffleEnabled && songs.size > 1) {
            var randomIndex = currentIndex
            while (randomIndex == currentIndex) randomIndex = Random.nextInt(songs.size)
            randomIndex
        } else {
            (currentIndex + 1).mod(songs.size)
        }

        playSongAt(nextIndex)
    }

    fun playPreviousSong() {
        val currentPosition = exoPlayer.currentPosition
        if (currentPosition > 5_000L) {
            seekTo(0L)
            return
        }

        val songs = _uiState.value.songs
        if (songs.isEmpty()) return

        val currentIndex = _uiState.value.currentSongIndex.takeIf { it in songs.indices } ?: 0
        val previousIndex = (currentIndex - 1).mod(songs.size)
        playSongAt(previousIndex)
    }

    fun togglePlayback() {
        try {
            if (exoPlayer.mediaItemCount == 0) {
                val currentSongId = _uiState.value.currentSong?.id ?: return
                val currentIndex = _uiState.value.songs.indexOfFirst { it.id == currentSongId }
                if (currentIndex >= 0) playSongAt(currentIndex)
                return
            }

            if (exoPlayer.playerError != null || exoPlayer.playbackState == Player.STATE_IDLE) {
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            } else if (exoPlayer.playbackState == Player.STATE_ENDED) {
                exoPlayer.seekTo(0L)
                exoPlayer.play()
            } else if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                exoPlayer.play()
            }
            syncPlayerState()
        } catch (exception: Exception) {
            reportPlaybackError("Unable to control playback.", exception)
        }
    }

    fun seekBy(deltaMs: Long) {
        runSeekSpinEffect(forward = deltaMs >= 0)
        seekTo(exoPlayer.currentPosition + deltaMs)
    }

    fun seekTo(positionMs: Long) {
        val max = _uiState.value.durationMs.takeIf { it > 0L } ?: Long.MAX_VALUE
        val target = positionMs.coerceIn(0L, max)
        runSeekSpinEffect(forward = target >= exoPlayer.currentPosition)
        exoPlayer.seekTo(target)
        syncPlayerState()
    }

    fun setVolume(volume: Float) {
        val normalized = volume.coerceIn(0f, 1f)
        exoPlayer.volume = normalized
        _uiState.value = _uiState.value.copy(volume = normalized)
    }

    fun toggleShuffle() {
        val enabled = !exoPlayer.shuffleModeEnabled
        exoPlayer.shuffleModeEnabled = enabled
        _uiState.value = _uiState.value.copy(isShuffleEnabled = enabled)
    }

    fun cycleRepeatMode() {
        val nextMode = when (exoPlayer.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        exoPlayer.repeatMode = nextMode
        _uiState.value = _uiState.value.copy(repeatMode = nextMode.toUiRepeatMode())
    }

    fun toggleCurrentSongLiked() {
        val song = _uiState.value.currentSong ?: return
        if (song.id in favoriteSongIds) {
            removeSongFromFavorites(song)
        } else {
            addSongToFavorites(song)
        }
    }

    private fun playSongAt(index: Int, startPositionMs: Long = 0L) {
        val songs = _uiState.value.songs
        if (index !in songs.indices) return

        val song = songs[index]
        val playableUri = playableUriFor(song)
        if (playableUri == null) {
            reportPlaybackError("This song does not have a playable audio URL.")
            return
        }

        try {
            val mediaItem = MediaItem.Builder()
                .setUri(playableUri)
                .setMediaId(song.id.toString())
                .setMimeType(mimeTypeFor(playableUri))
                .build()

            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            if (startPositionMs > 0L) {
                exoPlayer.seekTo(startPositionMs)
            }
            exoPlayer.playWhenReady = true
            persistedPositionBucket = -1L

            _uiState.value = _uiState.value.copy(
                currentSong = song,
                currentSongIndex = index,
                isPlayerRunning = true,
                positionMs = startPositionMs.coerceAtLeast(0L),
                durationMs = 0L,
                isCurrentLiked = isFavoriteSong(song),
                spinSpeedMultiplier = 1f,
                errorMessage = null
            )
            persistCurrentSongState(song = song, positionOverrideMs = startPositionMs.coerceAtLeast(0L))
            recordListeningHistory(song, startPositionMs.coerceAtLeast(0L))
        } catch (exception: Exception) {
            reportPlaybackError("Unable to play this song.", exception)
        }
    }

    private fun playableUriFor(song: Song): Uri? {
        val rawUrl = song.audioUrl.trim()
        if (rawUrl.isBlank()) return null

        return runCatching {
            val uri = Uri.parse(rawUrl)
            val scheme = uri.scheme?.lowercase()
            val playableUri = if (scheme == "http" && uri.host?.contains("cloudinary.com") == true) {
                uri.buildUpon().scheme("https").build()
            } else {
                uri
            }
            val playableScheme = playableUri.scheme?.lowercase()
            if (playableScheme == "http" ||
                playableScheme == "https" ||
                playableScheme == "file" ||
                playableScheme == "content" ||
                playableScheme == "android.resource"
            ) {
                playableUri
            } else {
                null
            }
        }.getOrNull()
    }

    private fun mimeTypeFor(uri: Uri): String? {
        val path = uri.path?.lowercase().orEmpty()
        return when {
            path.endsWith(".mp3") -> MimeTypes.AUDIO_MPEG
            path.endsWith(".m4a") || path.endsWith(".mp4") -> MimeTypes.AUDIO_MP4
            path.endsWith(".aac") -> MimeTypes.AUDIO_AAC
            path.endsWith(".wav") -> MimeTypes.AUDIO_WAV
            path.endsWith(".ogg") || path.endsWith(".oga") -> MimeTypes.AUDIO_OGG
            path.endsWith(".flac") -> MimeTypes.AUDIO_FLAC
            else -> null
        }
    }

    private fun reportPlaybackError(message: String, exception: Throwable? = null) {
        runCatching {
            exoPlayer.stop()
        }
        _uiState.value = _uiState.value.copy(
            isPlayerRunning = false,
            errorMessage = exception?.message?.takeIf { it.isNotBlank() } ?: message
        )
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                syncPlayerState()
                delay(350L)
            }
        }
    }

    private fun syncPlayerState() {
        val duration = exoPlayer.duration.safeDuration()
        val position = exoPlayer.currentPosition
            .coerceAtLeast(0L)
            .coerceAtMost(if (duration > 0L) duration else Long.MAX_VALUE)

        _uiState.value = _uiState.value.copy(
            isPlayerRunning = exoPlayer.isPlaying,
            positionMs = position,
            durationMs = duration,
            volume = exoPlayer.volume.coerceIn(0f, 1f),
            isShuffleEnabled = exoPlayer.shuffleModeEnabled,
            repeatMode = exoPlayer.repeatMode.toUiRepeatMode()
        )

        val currentSong = _uiState.value.currentSong
        if (currentSong != null) {
            val positionBucket = position / 2000L
            if (positionBucket != persistedPositionBucket) {
                persistedPositionBucket = positionBucket
                persistCurrentSongState(song = currentSong, positionOverrideMs = position)
            }
        }
    }

    private fun runSeekSpinEffect(forward: Boolean) {
        spinEffectJob?.cancel()
        spinEffectJob = viewModelScope.launch {
            if (forward) {
                _uiState.value = _uiState.value.copy(spinSpeedMultiplier = 2.8f)
                delay(340L)
            } else {
                _uiState.value = _uiState.value.copy(spinSpeedMultiplier = 0.55f)
                delay(220L)
                _uiState.value = _uiState.value.copy(spinSpeedMultiplier = 2.5f)
                delay(280L)
            }
            _uiState.value = _uiState.value.copy(spinSpeedMultiplier = 1f)
        }
    }

    private fun updatePlaylistState() {
        _uiState.value = _uiState.value.copy(playlists = composePlaylists(_uiState.value.songs))
    }

    private fun composePlaylists(songs: List<Song>): List<Playlist> {
        val serverPlaylists = backendPlaylists.filterNot { it.isLegacyFavoritesPlaylist() }
        val base = (listOf(buildFavoritePlaylist(songs)) + serverPlaylists + customPlaylists + buildQuickPlaylists(songs))
            .distinctBy { it.id }
        return base.map { playlistOverrides[it.id] ?: it }
    }

    private fun buildFavoritePlaylist(songs: List<Song>): Playlist {
        val songsById = songs.associateBy { it.id }
        val favoriteSongs = favoriteSongIds.mapNotNull { songsById[it] }

        return Playlist(
            id = FAVORITE_PLAYLIST_ID,
            userId = 0,
            name = "Favorite",
            description = "Saved on this device",
            coverUrl = favoriteSongs.firstOrNull()?.coverUrl,
            isPublic = false,
            songs = favoriteSongs
        )
    }

    private fun buildQuickPlaylists(songs: List<Song>): List<Playlist> {
        if (songs.isEmpty()) return emptyList()

        val recentlyAdded = songs.take(12)
        val mostPlayed = songs.sortedByDescending { it.playCount }.take(12)
        val chillMix = songs.shuffled().take(12)

        return listOf(
            Playlist(
                id = -101,
                userId = 0,
                name = "Recently Added",
                description = "Latest added songs",
                coverUrl = recentlyAdded.firstOrNull()?.coverUrl,
                isPublic = true,
                songs = recentlyAdded
            ),
            Playlist(
                id = -102,
                userId = 0,
                name = "Most Played",
                description = "Most played tracks",
                coverUrl = mostPlayed.firstOrNull()?.coverUrl,
                isPublic = true,
                songs = mostPlayed
            ),
            Playlist(
                id = -103,
                userId = 0,
                name = "Chill Mix",
                description = "Randomly curated mix",
                coverUrl = chillMix.firstOrNull()?.coverUrl,
                isPublic = true,
                songs = chillMix
            )
        )
    }

    private fun Playlist.isLegacyFavoritesPlaylist(): Boolean {
        return name.equals("Favorite", ignoreCase = true) || name.equals("Favorites", ignoreCase = true)
    }

    private fun isFavoriteSong(song: Song?): Boolean {
        return song?.id?.let { it in favoriteSongIds } == true
    }

    private fun recordListeningHistory(song: Song, positionMs: Long) {
        val dao = musicCacheDao ?: return
        viewModelScope.launch(Dispatchers.IO) {
            dao.addListeningHistory(
                ListeningHistoryEntity(
                    songId = song.id,
                    positionMs = positionMs
                ),
                maxRows = HISTORY_LIMIT
            )
        }
    }

    private fun applyPlaylistUpdate(updated: Playlist) {
        if (updated.id == FAVORITE_PLAYLIST_ID) {
            return
        }
        backendPlaylists = backendPlaylists.map { if (it.id == updated.id) updated else it }
        customPlaylists = customPlaylists.map { if (it.id == updated.id) updated else it }
        playlistOverrides[updated.id] = updated
        _uiState.value = _uiState.value.copy(
            playlists = _uiState.value.playlists.map { if (it.id == updated.id) updated else it }
        )
    }

    private suspend fun syncPlaylistFromBackend(playlistId: Int) {
        val result = withPlaylistAuth { playlistRepository.getPlaylistById(playlistId) }
        val synced = result.getOrNull() ?: return
        backendPlaylists = backendPlaylists.map { if (it.id == synced.id) synced else it }
        applyPlaylistUpdate(synced)
    }

    private suspend fun <T> withPlaylistAuth(call: suspend () -> Result<T>): Result<T> {
        val authResult = ensurePlaylistAuth()
        if (authResult.isFailure) {
            val exception = authResult.exceptionOrNull() ?: IllegalStateException("Authentication failed")
            return Result.failure(exception)
        }

        var result = call()
        if (result.isFailure && result.exceptionOrNull().isUnauthorized()) {
            val refreshed = refreshAccessToken()
            if (refreshed.isFailure) {
                clearStoredAuthSession()
                val exception = refreshed.exceptionOrNull() ?: IllegalStateException("Session expired. Please log in again.")
                return Result.failure(exception)
            }

            result = call()
            if (result.isFailure && result.exceptionOrNull().isUnauthorized()) {
                clearStoredAuthSession()
            }
        }
        return result
    }

    private suspend fun ensurePlaylistAuth(): Result<Unit> {
        val hasExplicitAuth = playerPrefs.getBoolean(KEY_EXPLICIT_AUTH, false)
        if (!hasExplicitAuth) {
            return Result.failure(IllegalStateException("Please log in to sync playlists."))
        }

        val storedToken = playerPrefs.getString(KEY_API_ACCESS_TOKEN, null)
        if (!storedToken.isNullOrBlank()) {
            ApiClient.setAccessToken(storedToken)
            return Result.success(Unit)
        }

        val refreshed = refreshAccessToken()
        if (refreshed.isSuccess) {
            return Result.success(Unit)
        }

        clearStoredAuthSession()
        val exception = refreshed.exceptionOrNull() ?: IllegalStateException("Session expired. Please log in again.")
        return Result.failure(exception)
    }

    private suspend fun loginWithEmail(email: String, password: String): Result<Unit> = runCatching {
        val response = ApiClient.apiService.login(
            LoginRequest(email = email, password = password)
        )
        val authData = response.data ?: throw IllegalStateException(response.message ?: "Login failed")
        saveAuthSession(authData.accessToken, authData.refreshToken)
    }

    private suspend fun registerWithEmail(credentials: GuestCredentials): Result<Unit> = runCatching {
        val response = ApiClient.apiService.register(
            RegisterRequest(
                email = credentials.email,
                password = credentials.password,
                displayName = credentials.displayName
            )
        )
        val authData = response.data ?: throw IllegalStateException(response.message ?: "Registration failed")
        saveAuthSession(authData.accessToken, authData.refreshToken)
    }

    private fun loadOrCreateGuestCredentials(): GuestCredentials {
        val storedEmail = playerPrefs.getString(KEY_APP_ACCOUNT_EMAIL, null)
        val storedPassword = playerPrefs.getString(KEY_APP_ACCOUNT_PASSWORD, null)
        if (!storedEmail.isNullOrBlank() && !storedPassword.isNullOrBlank()) {
            return GuestCredentials(
                email = storedEmail,
                password = storedPassword,
                displayName = "Cinder Guest"
            )
        }

        val androidId = Settings.Secure.getString(
            getApplication<Application>().contentResolver,
            Settings.Secure.ANDROID_ID
        ).orEmpty().ifBlank { "guest" }
        val suffix = androidId.takeLast(10).lowercase()
        val credentials = GuestCredentials(
            email = "guest_${suffix}@cinderssoul.local",
            password = "guest_${suffix}_123456",
            displayName = "Guest ${suffix.takeLast(4)}"
        )

        playerPrefs.edit()
            .putString(KEY_APP_ACCOUNT_EMAIL, credentials.email)
            .putString(KEY_APP_ACCOUNT_PASSWORD, credentials.password)
            .apply()

        return credentials
    }

    private fun saveAuthSession(accessToken: String, refreshToken: String, explicitUser: Boolean = false) {
        playerPrefs.edit()
            .putString(KEY_API_ACCESS_TOKEN, accessToken)
            .putString(KEY_API_REFRESH_TOKEN, refreshToken)
            .putBoolean(KEY_EXPLICIT_AUTH, explicitUser)
            .apply()
        ApiClient.setAccessToken(accessToken)
    }

    private suspend fun refreshAccessToken(): Result<Unit> = runCatching {
        val refreshToken = playerPrefs.getString(KEY_API_REFRESH_TOKEN, null)
        if (refreshToken.isNullOrBlank()) {
            throw IllegalStateException("Session expired. Please log in again.")
        }

        val response = ApiClient.apiService.refreshToken(RefreshTokenRequest(refreshToken = refreshToken))
        val refreshedToken = response.requireData().accessToken
        if (refreshedToken.isBlank()) {
            throw IllegalStateException("Session refresh returned an empty access token.")
        }

        playerPrefs.edit()
            .putString(KEY_API_ACCESS_TOKEN, refreshedToken)
            .apply()
        ApiClient.setAccessToken(refreshedToken)
    }

    private fun clearStoredAuthSession() {
        playerPrefs.edit()
            .remove(KEY_API_ACCESS_TOKEN)
            .remove(KEY_API_REFRESH_TOKEN)
            .remove(KEY_EXPLICIT_AUTH)
            .apply()
        ApiClient.setAccessToken(null)
        _uiState.value = _uiState.value.copy(
            authUser = null,
            authMessage = null,
            passwordResetDevOtp = null
        )
    }

    private suspend fun uploadPlaylistCoverImage(imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val application = getApplication<Application>()
            val contentResolver = application.contentResolver
            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
            val tempImage = File.createTempFile("playlist_cover_", ".$extension", application.cacheDir)

            try {
                val inputStream = contentResolver.openInputStream(imageUri)
                    ?: throw IllegalStateException("Cannot read selected image")
                inputStream.use { input ->
                    tempImage.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val imageBody = tempImage.asRequestBody(mimeType.toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData(
                    "image",
                    tempImage.name,
                    imageBody
                )

                val response = ApiClient.apiService.uploadImage(imagePart)
                val uploadData = response.data ?: throw IllegalStateException(response.message ?: "Upload failed")
                normalizeBackendUrl(uploadData.url)
                    ?: throw IllegalStateException("Invalid uploaded image URL")
            } finally {
                if (tempImage.exists()) {
                    tempImage.delete()
                }
            }
        }
    }

    private data class GuestCredentials(
        val email: String,
        val password: String,
        val displayName: String
    )

    private fun Throwable?.isUnauthorized(): Boolean {
        return this is HttpException && code() == 401
    }

    private fun Throwable?.isConflict(): Boolean {
        return this is HttpException && code() == 409
    }

    private fun normalizeBackendUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null

        val isEmulator = (android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator"))

        return if (isEmulator) {
            url.replace("localhost", "10.0.2.2").replace("127.0.0.1", "10.0.2.2")
        } else {
            // Trên máy thật, nếu dùng adb reverse thì localhost là chuẩn nhất, không được đổi thành 10.0.2.2
            url.replace("10.0.2.2", "localhost").replace("127.0.0.1", "localhost")
        }
    }

    override fun onCleared() {
        progressJob?.cancel()
        spinEffectJob?.cancel()
        persistCurrentSongState()
        exoPlayer.release()
        super.onCleared()
    }

    private fun restoreSavedPlayback(songs: List<Song>): RestoredPlayback? {
        if (hasTriedRestorePlayback) return null
        hasTriedRestorePlayback = true

        val songId = playerPrefs.getInt(KEY_LAST_SONG_ID, Int.MIN_VALUE)
        if (songId == Int.MIN_VALUE) return null

        val index = songs.indexOfFirst { it.id == songId }
        if (index !in songs.indices) return null

        val positionMs = playerPrefs.getLong(KEY_LAST_POSITION_MS, 0L).coerceAtLeast(0L)
        return RestoredPlayback(song = songs[index], index = index, positionMs = positionMs)
    }

    private fun prepareRestoredSong(restoredPlayback: RestoredPlayback) {
        val song = restoredPlayback.song
        val playableUri = playableUriFor(song) ?: return

        try {
            val mediaItem = MediaItem.Builder()
                .setUri(playableUri)
                .setMediaId(song.id.toString())
                .setMimeType(mimeTypeFor(playableUri))
                .build()

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.seekTo(restoredPlayback.positionMs)
            exoPlayer.playWhenReady = false
            persistedPositionBucket = restoredPlayback.positionMs / 2000L
        } catch (exception: Exception) {
            reportPlaybackError("Unable to restore the last song.", exception)
        }
    }

    private fun persistCurrentSongState(song: Song? = _uiState.value.currentSong, positionOverrideMs: Long? = null) {
        if (song == null) return

        val editor = playerPrefs.edit()

        val duration = exoPlayer.duration.safeDuration()
        val position = (positionOverrideMs ?: exoPlayer.currentPosition)
            .coerceAtLeast(0L)
            .coerceAtMost(if (duration > 0L) duration else Long.MAX_VALUE)

        editor.putInt(KEY_LAST_SONG_ID, song.id)
            .putLong(KEY_LAST_POSITION_MS, position)
            .apply()
    }

    private fun loadDownloadedSongIds(): Set<Int> {
        val raw = playerPrefs.getString(KEY_DOWNLOADED_SONG_IDS, "").orEmpty()
        if (raw.isBlank()) return emptySet()
        return raw.split(',')
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
    }

    private fun markSongDownloaded(songId: Int, localUri: String? = null) {
        if (songId in downloadedSongIds) return
        downloadedSongIds = downloadedSongIds + songId
        playerPrefs.edit()
            .putString(KEY_DOWNLOADED_SONG_IDS, downloadedSongIds.joinToString(","))
            .apply()
        _uiState.value = _uiState.value.copy(downloadedSongIds = downloadedSongIds)

        val dao = musicCacheDao ?: return
        viewModelScope.launch(Dispatchers.IO) {
            dao.upsertDownloadedSong(
                DownloadedSongEntity(
                    songId = songId,
                    localUri = localUri
                )
            )
        }
    }
}
