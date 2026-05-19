package com.example.cinderssoul.repository

import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.models.User
import com.example.cinderssoul.network.AdminSummaryDto
import com.example.cinderssoul.network.AdminCreateUserRequest
import com.example.cinderssoul.network.AdminUpdateUserRequest
import com.example.cinderssoul.network.ApiClient
import com.example.cinderssoul.network.ApiEnvelope
import com.example.cinderssoul.network.ApiService
import com.example.cinderssoul.network.CreateAlbumRequest
import com.example.cinderssoul.network.CreateArtistRequest
import com.example.cinderssoul.network.CreateSongRequest
import com.example.cinderssoul.network.UpdateAlbumRequest
import com.example.cinderssoul.network.UpdateArtistRequest
import com.example.cinderssoul.network.UpdateSongRequest
import com.example.cinderssoul.network.toDomainAlbum
import com.example.cinderssoul.network.toDomainArtist
import com.example.cinderssoul.network.toDomainSong
import com.example.cinderssoul.network.toDomainUser

class AdminRepository(
    private val apiService: ApiService = ApiClient.apiService
) {
    suspend fun getSummary(): Result<AdminSummaryDto> = runCatching {
        apiService.getAdminSummary().requireData()
    }

    suspend fun getUsers(): Result<List<User>> = runCatching {
        apiService.getAdminUsers().requireData().map { it.toDomainUser() }
    }

    suspend fun createUser(request: AdminCreateUserRequest): Result<User> = runCatching {
        apiService.createAdminUser(request).requireData().toDomainUser()
    }

    suspend fun updateUser(userId: Int, request: AdminUpdateUserRequest): Result<User> = runCatching {
        apiService.updateAdminUser(userId, request).requireData().toDomainUser()
    }

    suspend fun deleteUser(userId: Int): Result<Unit> = runCatching {
        apiService.deleteAdminUser(userId).requireSuccess()
    }

    suspend fun getSongs(): Result<List<Song>> = runCatching {
        apiService.getSongs(limit = 200).requireData().map { it.toDomainSong() }
    }

    suspend fun getArtists(): Result<List<Artist>> = runCatching {
        apiService.getArtists(limit = 200).requireData().map { it.toDomainArtist() }
    }

    suspend fun getAlbums(): Result<List<Album>> = runCatching {
        apiService.getAlbums(limit = 200).requireData().map { it.toDomainAlbum() }
    }

    suspend fun createArtist(request: CreateArtistRequest): Result<Artist> = runCatching {
        apiService.createArtist(request).requireData().toDomainArtist()
    }

    suspend fun updateArtist(artistId: Int, request: UpdateArtistRequest): Result<Artist> = runCatching {
        apiService.updateArtist(artistId, request).requireData().toDomainArtist()
    }

    suspend fun deleteArtist(artistId: Int): Result<Unit> = runCatching {
        apiService.deleteArtist(artistId).requireSuccess()
    }

    suspend fun createAlbum(request: CreateAlbumRequest): Result<Album> = runCatching {
        apiService.createAlbum(request).requireData().toDomainAlbum()
    }

    suspend fun updateAlbum(albumId: Int, request: UpdateAlbumRequest): Result<Album> = runCatching {
        apiService.updateAlbum(albumId, request).requireData().toDomainAlbum()
    }

    suspend fun deleteAlbum(albumId: Int): Result<Unit> = runCatching {
        apiService.deleteAlbum(albumId).requireSuccess()
    }

    suspend fun createSong(request: CreateSongRequest): Result<Song> = runCatching {
        apiService.createSong(request).requireData().toDomainSong()
    }

    suspend fun updateSong(songId: Int, request: UpdateSongRequest): Result<Song> = runCatching {
        apiService.updateSong(songId, request).requireData().toDomainSong()
    }

    suspend fun deleteSong(songId: Int): Result<Unit> = runCatching {
        apiService.deleteSong(songId).requireSuccess()
    }
}

private fun <T> ApiEnvelope<T>.requireData(): T {
    return data ?: throw IllegalStateException(message ?: "Backend returned no data")
}

private fun ApiEnvelope<Unit>.requireSuccess() {
    if (!success) {
        throw IllegalStateException(message ?: "Request failed")
    }
}
