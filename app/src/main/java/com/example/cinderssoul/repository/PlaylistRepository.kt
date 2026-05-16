package com.example.cinderssoul.repository

import com.example.cinderssoul.models.Playlist
import com.example.cinderssoul.network.AddSongRequest
import com.example.cinderssoul.network.ApiClient
import com.example.cinderssoul.network.ApiEnvelope
import com.example.cinderssoul.network.ApiService
import com.example.cinderssoul.network.CreatePlaylistRequest
import com.example.cinderssoul.network.UpdatePlaylistRequest
import com.example.cinderssoul.network.toDomainPlaylist

class PlaylistRepository(
    private val apiService: ApiService = ApiClient.apiService
) {
    suspend fun getUserPlaylists(): Result<List<Playlist>> = runCatching {
        apiService.getPlaylists()
            .requireData()
            .map { it.toDomainPlaylist() }
    }

    suspend fun getPlaylistById(playlistId: Int): Result<Playlist> = runCatching {
        apiService.getPlaylist(playlistId)
            .requireData()
            .toDomainPlaylist()
    }

    suspend fun createPlaylist(
        name: String,
        description: String? = null,
        coverUrl: String? = null,
        isPublic: Boolean = false
    ): Result<Playlist> = runCatching {
        apiService.createPlaylist(
            CreatePlaylistRequest(
                name = name,
                description = description,
                coverUrl = coverUrl,
                isPublic = isPublic
            )
        ).requireData().toDomainPlaylist()
    }

    suspend fun addSongToPlaylist(playlistId: Int, songId: Int): Result<Unit> = runCatching {
        apiService.addSongToPlaylist(playlistId, AddSongRequest(songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Int, songId: Int): Result<Unit> = runCatching {
        apiService.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun updatePlaylist(
        playlistId: Int,
        name: String? = null,
        description: String? = null,
        coverUrl: String? = null,
        isPublic: Boolean? = null
    ): Result<Playlist> = runCatching {
        apiService.updatePlaylist(
            playlistId,
            UpdatePlaylistRequest(
                name = name,
                description = description,
                coverUrl = coverUrl,
                isPublic = isPublic
            )
        ).requireData().toDomainPlaylist()
    }

    suspend fun deletePlaylist(playlistId: Int): Result<Unit> = runCatching {
        apiService.deletePlaylist(playlistId)
    }
}

private fun <T> ApiEnvelope<T>.requireData(): T {
    return data ?: throw IllegalStateException(message ?: "Backend returned no data")
}
