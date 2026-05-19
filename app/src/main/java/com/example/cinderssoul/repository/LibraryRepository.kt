package com.example.cinderssoul.repository

import com.example.cinderssoul.models.Song
import com.example.cinderssoul.network.AddSongRequest
import com.example.cinderssoul.network.ApiClient
import com.example.cinderssoul.network.ApiEnvelope
import com.example.cinderssoul.network.ApiService
import com.example.cinderssoul.network.toDomainSong

class LibraryRepository(
    private val apiService: ApiService = ApiClient.apiService
) {
    suspend fun getLibrarySongs(): Result<List<Song>> = runCatching {
        apiService.getLibrarySongs()
            .requireData()
            .map { it.toDomainSong() }
    }

    suspend fun addSongToLibrary(songId: Int): Result<Song> = runCatching {
        apiService.addSongToLibrary(AddSongRequest(songId))
            .requireData()
            .toDomainSong()
    }

    suspend fun removeSongFromLibrary(songId: Int): Result<Unit> = runCatching {
        apiService.removeSongFromLibrary(songId)
    }
}

private fun <T> ApiEnvelope<T>.requireData(): T {
    return data ?: throw IllegalStateException(message ?: "Backend returned no data")
}
