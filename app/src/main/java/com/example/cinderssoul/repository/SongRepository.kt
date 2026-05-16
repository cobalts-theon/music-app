package com.example.cinderssoul.repository

import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.local.MusicCacheDao
import com.example.cinderssoul.local.toCachedAlbumEntity
import com.example.cinderssoul.local.toCachedArtistEntity
import com.example.cinderssoul.local.toCachedSongEntity
import com.example.cinderssoul.local.toDomainAlbum
import com.example.cinderssoul.local.toDomainArtist
import com.example.cinderssoul.local.toDomainSong
import com.example.cinderssoul.network.ApiClient
import com.example.cinderssoul.network.ApiEnvelope
import com.example.cinderssoul.network.ApiService
import com.example.cinderssoul.network.toDomainAlbum as toDomainAlbumFromApi
import com.example.cinderssoul.network.toDomainArtist as toDomainArtistFromApi
import com.example.cinderssoul.network.toDomainSong as toDomainSongFromApi

class SongRepository(
    private val apiService: ApiService = ApiClient.apiService,
    private val musicCacheDao: MusicCacheDao? = null
) {
    suspend fun getAllSongs(
        search: String? = null,
        genre: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Result<List<Song>> {
        val normalizedSearch = search?.takeIf { it.isNotBlank() }
        val normalizedGenre = genre?.takeIf { it.isNotBlank() }

        val apiResult = runCatching {
            apiService.getSongs(
                search = normalizedSearch,
                genre = normalizedGenre,
                limit = limit,
                offset = offset
            ).requireData().map { it.toDomainSongFromApi() }
        }

        return apiResult.fold(
            onSuccess = { songs ->
                cacheSongs(
                    songs = songs,
                    replaceCache = normalizedSearch == null && normalizedGenre == null && offset == 0
                )
                Result.success(songs)
            },
            onFailure = { apiError ->
                fallbackToCachedSongs(
                    apiError = apiError,
                    search = normalizedSearch,
                    genre = normalizedGenre,
                    limit = limit,
                    offset = offset
                )
            }
        )
    }

    suspend fun getAllArtists(limit: Int = 30, offset: Int = 0): Result<List<Artist>> {
        val apiResult = runCatching {
            apiService.getArtists(limit = limit, offset = offset)
                .requireData()
                .map { it.toDomainArtistFromApi() }
        }

        return apiResult.fold(
            onSuccess = { artists ->
                cacheArtists(artists = artists, replaceCache = offset == 0)
                Result.success(artists)
            },
            onFailure = { apiError ->
                fallbackToCachedArtists(apiError = apiError, limit = limit, offset = offset)
            }
        )
    }

    suspend fun getAllAlbums(limit: Int = 30, offset: Int = 0): Result<List<Album>> {
        val apiResult = runCatching {
            apiService.getAlbums(limit = limit, offset = offset)
                .requireData()
                .map { it.toDomainAlbumFromApi() }
        }

        return apiResult.fold(
            onSuccess = { albums ->
                cacheAlbums(albums = albums, replaceCache = offset == 0)
                Result.success(albums)
            },
            onFailure = { apiError ->
                fallbackToCachedAlbums(apiError = apiError, limit = limit, offset = offset)
            }
        )
    }

    private suspend fun cacheSongs(songs: List<Song>, replaceCache: Boolean) {
        val dao = musicCacheDao ?: return
        val cachedAt = System.currentTimeMillis()
        val entities = songs.map { it.toCachedSongEntity(cachedAt) }
        if (replaceCache) {
            dao.replaceSongs(entities)
        } else {
            dao.upsertSongs(entities)
        }
    }

    private suspend fun fallbackToCachedSongs(
        apiError: Throwable,
        search: String?,
        genre: String?,
        limit: Int,
        offset: Int
    ): Result<List<Song>> {
        val dao = musicCacheDao ?: return Result.failure(apiError)
        return runCatching {
            dao.getSongs(search = search, genre = genre, limit = limit, offset = offset)
                .map { it.toDomainSong() }
        }.fold(
            onSuccess = { cachedSongs ->
                if (cachedSongs.isNotEmpty()) Result.success(cachedSongs) else Result.failure(apiError)
            },
            onFailure = { cacheError -> Result.failure(cacheError) }
        )
    }

    private suspend fun cacheArtists(artists: List<Artist>, replaceCache: Boolean) {
        val dao = musicCacheDao ?: return
        val cachedAt = System.currentTimeMillis()
        val entities = artists.map { it.toCachedArtistEntity(cachedAt) }
        if (replaceCache) {
            dao.replaceArtists(entities)
        } else {
            dao.upsertArtists(entities)
        }
    }

    private suspend fun fallbackToCachedArtists(
        apiError: Throwable,
        limit: Int,
        offset: Int
    ): Result<List<Artist>> {
        val dao = musicCacheDao ?: return Result.failure(apiError)
        return runCatching {
            dao.getArtists(limit = limit, offset = offset).map { it.toDomainArtist() }
        }.fold(
            onSuccess = { cachedArtists ->
                if (cachedArtists.isNotEmpty()) Result.success(cachedArtists) else Result.failure(apiError)
            },
            onFailure = { cacheError -> Result.failure(cacheError) }
        )
    }

    private suspend fun cacheAlbums(albums: List<Album>, replaceCache: Boolean) {
        val dao = musicCacheDao ?: return
        val cachedAt = System.currentTimeMillis()
        val entities = albums.map { it.toCachedAlbumEntity(cachedAt) }
        if (replaceCache) {
            dao.replaceAlbums(entities)
        } else {
            dao.upsertAlbums(entities)
        }
    }

    private suspend fun fallbackToCachedAlbums(
        apiError: Throwable,
        limit: Int,
        offset: Int
    ): Result<List<Album>> {
        val dao = musicCacheDao ?: return Result.failure(apiError)
        return runCatching {
            dao.getAlbums(limit = limit, offset = offset).map { it.toDomainAlbum() }
        }.fold(
            onSuccess = { cachedAlbums ->
                if (cachedAlbums.isNotEmpty()) Result.success(cachedAlbums) else Result.failure(apiError)
            },
            onFailure = { cacheError -> Result.failure(cacheError) }
        )
    }
}

private fun <T> ApiEnvelope<T>.requireData(): T {
    return data ?: throw IllegalStateException(message ?: "Backend returned no data")
}
