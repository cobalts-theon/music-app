package com.example.cinderssoul.local

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Song

@Entity(tableName = "cached_songs")
data class CachedSongEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "artistId")
    val artistId: Int,
    @ColumnInfo(name = "albumId")
    val albumId: Int?,
    @ColumnInfo(name = "duration")
    val duration: Int,
    @ColumnInfo(name = "audioUrl")
    val audioUrl: String,
    @ColumnInfo(name = "coverUrl")
    val coverUrl: String?,
    @ColumnInfo(name = "genre")
    val genre: String?,
    @ColumnInfo(name = "playCount")
    val playCount: Int,
    @ColumnInfo(name = "lyrics")
    val lyrics: String?,
    @ColumnInfo(name = "artistName")
    val artistName: String?,
    @ColumnInfo(name = "artistBio")
    val artistBio: String?,
    @ColumnInfo(name = "artistAvatarUrl")
    val artistAvatarUrl: String?,
    @ColumnInfo(name = "albumTitle")
    val albumTitle: String?,
    @ColumnInfo(name = "albumCoverUrl")
    val albumCoverUrl: String?,
    @ColumnInfo(name = "albumReleaseDate")
    val albumReleaseDate: String?,
    @ColumnInfo(name = "cachedAt")
    val cachedAt: Long
)

@Entity(tableName = "cached_artists")
data class CachedArtistEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "bio")
    val bio: String?,
    @ColumnInfo(name = "avatarUrl")
    val avatarUrl: String?,
    @ColumnInfo(name = "cachedAt")
    val cachedAt: Long
)

@Entity(tableName = "cached_albums")
data class CachedAlbumEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "artistId")
    val artistId: Int?,
    @ColumnInfo(name = "coverUrl")
    val coverUrl: String?,
    @ColumnInfo(name = "releaseDate")
    val releaseDate: String?,
    @ColumnInfo(name = "artistName")
    val artistName: String?,
    @ColumnInfo(name = "artistBio")
    val artistBio: String?,
    @ColumnInfo(name = "artistAvatarUrl")
    val artistAvatarUrl: String?,
    @ColumnInfo(name = "cachedAt")
    val cachedAt: Long
)

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey
    @ColumnInfo(name = "songId")
    val songId: Int,
    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "listening_history")
data class ListeningHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "songId")
    val songId: Int,
    @ColumnInfo(name = "playedAt")
    val playedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "positionMs")
    val positionMs: Long = 0L
)

@Entity(tableName = "downloaded_songs")
data class DownloadedSongEntity(
    @PrimaryKey
    @ColumnInfo(name = "songId")
    val songId: Int,
    @ColumnInfo(name = "localUri")
    val localUri: String? = null,
    @ColumnInfo(name = "downloadedAt")
    val downloadedAt: Long = System.currentTimeMillis()
)

fun Song.toCachedSongEntity(cachedAt: Long = System.currentTimeMillis()): CachedSongEntity {
    return CachedSongEntity(
        id = id,
        title = title,
        artistId = artistId,
        albumId = albumId,
        duration = duration,
        audioUrl = audioUrl,
        coverUrl = coverUrl,
        genre = genre,
        playCount = playCount,
        lyrics = lyrics,
        artistName = artist?.name,
        artistBio = artist?.bio,
        artistAvatarUrl = artist?.avatarUrl,
        albumTitle = album?.title,
        albumCoverUrl = album?.coverUrl,
        albumReleaseDate = album?.releaseDate,
        cachedAt = cachedAt
    )
}

fun CachedSongEntity.toDomainSong(): Song {
    val cachedArtist = artistName
        ?.takeIf { it.isNotBlank() }
        ?.let { name ->
            Artist(
                id = artistId,
                name = name,
                bio = artistBio,
                avatarUrl = artistAvatarUrl
            )
        }

    val cachedAlbum = albumId
        ?.takeIf { !albumTitle.isNullOrBlank() }
        ?.let { id ->
            Album(
                id = id,
                title = albumTitle.orEmpty(),
                artistId = artistId,
                coverUrl = albumCoverUrl,
                releaseDate = albumReleaseDate,
                artist = cachedArtist
            )
        }

    return Song(
        id = id,
        title = title,
        artistId = artistId,
        albumId = albumId,
        duration = duration,
        audioUrl = audioUrl,
        coverUrl = coverUrl,
        genre = genre,
        playCount = playCount,
        lyrics = lyrics,
        artist = cachedArtist,
        album = cachedAlbum
    )
}

fun Artist.toCachedArtistEntity(cachedAt: Long = System.currentTimeMillis()): CachedArtistEntity {
    return CachedArtistEntity(
        id = id,
        name = name,
        bio = bio,
        avatarUrl = avatarUrl,
        cachedAt = cachedAt
    )
}

fun CachedArtistEntity.toDomainArtist(): Artist {
    return Artist(
        id = id,
        name = name,
        bio = bio,
        avatarUrl = avatarUrl
    )
}

fun Album.toCachedAlbumEntity(cachedAt: Long = System.currentTimeMillis()): CachedAlbumEntity {
    return CachedAlbumEntity(
        id = id,
        title = title,
        artistId = artistId,
        coverUrl = coverUrl,
        releaseDate = releaseDate,
        artistName = artist?.name,
        artistBio = artist?.bio,
        artistAvatarUrl = artist?.avatarUrl,
        cachedAt = cachedAt
    )
}

fun CachedAlbumEntity.toDomainAlbum(): Album {
    val cachedArtist = artistId
        ?.takeIf { !artistName.isNullOrBlank() }
        ?.let { id ->
            Artist(
                id = id,
                name = artistName.orEmpty(),
                bio = artistBio,
                avatarUrl = artistAvatarUrl
            )
        }

    return Album(
        id = id,
        title = title,
        artistId = artistId,
        coverUrl = coverUrl,
        releaseDate = releaseDate,
        artist = cachedArtist
    )
}
