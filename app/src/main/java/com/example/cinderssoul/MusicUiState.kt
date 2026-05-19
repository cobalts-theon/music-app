package com.example.cinderssoul

import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Playlist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.models.User

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
    val authMessage: String? = null
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
