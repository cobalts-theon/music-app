package com.example.cinderssoul.local

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CachedSongEntity::class,
        CachedArtistEntity::class,
        CachedAlbumEntity::class,
        FavoriteSongEntity::class,
        ListeningHistoryEntity::class,
        DownloadedSongEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CinderDatabase : RoomDatabase() {
    abstract fun musicCacheDao(): MusicCacheDao

    companion object {
        @Volatile
        private var INSTANCE: CinderDatabase? = null

        fun getInstance(context: Context): CinderDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CinderDatabase::class.java,
                    "cinders_soul_cache.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS favorite_songs (
                        songId INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        PRIMARY KEY(songId)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS listening_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        songId INTEGER NOT NULL,
                        playedAt INTEGER NOT NULL,
                        positionMs INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS downloaded_songs (
                        songId INTEGER NOT NULL,
                        localUri TEXT,
                        downloadedAt INTEGER NOT NULL,
                        PRIMARY KEY(songId)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE favorite_songs RENAME TO favorite_songs_old")
                db.execSQL(
                    """
                    CREATE TABLE favorite_songs (
                        userId INTEGER NOT NULL,
                        songId INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        PRIMARY KEY(userId, songId)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO favorite_songs(userId, songId, createdAt)
                    SELECT 0, songId, createdAt FROM favorite_songs_old
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE favorite_songs_old")

                db.execSQL("ALTER TABLE listening_history RENAME TO listening_history_old")
                db.execSQL(
                    """
                    CREATE TABLE listening_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        songId INTEGER NOT NULL,
                        playedAt INTEGER NOT NULL,
                        positionMs INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO listening_history(id, userId, songId, playedAt, positionMs)
                    SELECT id, 0, songId, playedAt, positionMs FROM listening_history_old
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE listening_history_old")

                db.execSQL("ALTER TABLE downloaded_songs RENAME TO downloaded_songs_old")
                db.execSQL(
                    """
                    CREATE TABLE downloaded_songs (
                        userId INTEGER NOT NULL,
                        songId INTEGER NOT NULL,
                        localUri TEXT,
                        downloadedAt INTEGER NOT NULL,
                        PRIMARY KEY(userId, songId)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO downloaded_songs(userId, songId, localUri, downloadedAt)
                    SELECT 0, songId, localUri, downloadedAt FROM downloaded_songs_old
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE downloaded_songs_old")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE listening_history_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        songId INTEGER NOT NULL,
                        playedAt INTEGER NOT NULL,
                        positionMs INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO listening_history_new(id, userId, songId, playedAt, positionMs)
                    SELECT id, userId, songId, playedAt, positionMs FROM listening_history
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE listening_history")
                db.execSQL("ALTER TABLE listening_history_new RENAME TO listening_history")
            }
        }
    }
}
