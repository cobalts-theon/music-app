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

enum class RepeatUiMode {
    OFF, ALL, ONE
}

data class MusicUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val searchQuery: String = "",
    val currentSong: Song? = null,
    val currentSongIndex: Int = -1,
    val isPlayerRunning: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Float = 0.85f,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatUiMode = RepeatUiMode.OFF,
    val isCurrentLiked: Boolean = false,
    val spinSpeedMultiplier: Float = 1f,
    val downloadedSongIds: Set<Int> = emptySet(),
    val authUser: User? = null,
    val isAuthLoading: Boolean = false,
    val authMessage: String? = null,
    val passwordResetDevOtp: String? = null
) {
    val filteredSongs: List<Song>
        get() {
            if (searchQuery.isBlank()) return songs
            val normalizedQuery = searchQuery.trim().lowercase()
            return songs.filter { song ->
                song.title.lowercase().contains(normalizedQuery) ||
                    song.artistName.lowercase().contains(normalizedQuery) ||
                    song.albumTitle.lowercase().contains(normalizedQuery) ||
                    song.genre.orEmpty().lowercase().contains(normalizedQuery) ||
                    song.lyrics.orEmpty().lowercase().contains(normalizedQuery)
            }
        }
}
