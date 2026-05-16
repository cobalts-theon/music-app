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
    version = 2,
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
                    .addMigrations(MIGRATION_1_2)
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
    }
}
