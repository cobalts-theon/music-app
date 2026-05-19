package com.example.cinderssoul.local

import android.annotation.SuppressLint
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@SuppressLint("AndroidUnresolvedRoomSqlReference")
@Dao
interface MusicCacheDao {
    @Query(
        """
        SELECT * FROM cached_songs
        WHERE (:search IS NULL
            OR LOWER(title) LIKE '%' || LOWER(:search) || '%'
            OR LOWER(IFNULL(artistName, '')) LIKE '%' || LOWER(:search) || '%'
            OR LOWER(IFNULL(albumTitle, '')) LIKE '%' || LOWER(:search) || '%')
        AND (:genre IS NULL OR LOWER(IFNULL(genre, '')) = LOWER(:genre))
        ORDER BY cachedAt DESC, id ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getSongs(
        search: String?,
        genre: String?,
        limit: Int,
        offset: Int
    ): List<CachedSongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSongs(songs: List<CachedSongEntity>)

    @Query("DELETE FROM cached_songs")
    suspend fun clearSongs()

    @Transaction
    suspend fun replaceSongs(songs: List<CachedSongEntity>) {
        clearSongs()
        upsertSongs(songs)
    }

    @Query(
        """
        SELECT * FROM cached_artists
        ORDER BY cachedAt DESC, id ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getArtists(limit: Int, offset: Int): List<CachedArtistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArtists(artists: List<CachedArtistEntity>)

    @Query("DELETE FROM cached_artists")
    suspend fun clearArtists()

    @Transaction
    suspend fun replaceArtists(artists: List<CachedArtistEntity>) {
        clearArtists()
        upsertArtists(artists)
    }

    @Query(
        """
        SELECT * FROM cached_albums
        ORDER BY cachedAt DESC, id ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getAlbums(limit: Int, offset: Int): List<CachedAlbumEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlbums(albums: List<CachedAlbumEntity>)

    @Query("DELETE FROM cached_albums")
    suspend fun clearAlbums()

    @Transaction
    suspend fun replaceAlbums(albums: List<CachedAlbumEntity>) {
        clearAlbums()
        upsertAlbums(albums)
    }

    @Query("SELECT songId FROM favorite_songs WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getFavoriteSongIds(userId: Int): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavoriteSong(favorite: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs WHERE userId = :userId AND songId = :songId")
    suspend fun deleteFavoriteSong(userId: Int, songId: Int)

    @Query("SELECT songId FROM downloaded_songs WHERE userId = :userId ORDER BY downloadedAt DESC")
    suspend fun getDownloadedSongIds(userId: Int): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDownloadedSong(downloadedSong: DownloadedSongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListeningHistory(history: ListeningHistoryEntity)

    @Query(
        """
        DELETE FROM listening_history
        WHERE id NOT IN (
            SELECT id FROM listening_history
            WHERE userId = :userId
            ORDER BY playedAt DESC
            LIMIT :maxRows
        )
        AND userId = :userId
        """
    )
    suspend fun trimListeningHistory(userId: Int, maxRows: Int)

    @Transaction
    suspend fun addListeningHistory(history: ListeningHistoryEntity, maxRows: Int = 200) {
        insertListeningHistory(history)
        trimListeningHistory(history.userId, maxRows)
    }
}
