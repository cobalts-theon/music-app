package com.example.cinderssoul.repository

import com.example.cinderssoul.models.Song
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

open class SongRepository {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val songsCollection by lazy { db.collection("songs") }

    // Get all songs
    open suspend fun getAllSongs(): Result<List<Song>> {
        return try {
            val snapshot = songsCollection.get().await()
            val songs = snapshot.toObjects(Song::class.java)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get song by ID
    open suspend fun getSongById(songId: String): Result<Song?> {
        return try {
            val document = songsCollection.document(songId).get().await()
            val song = document.toObject(Song::class.java)
            Result.success(song)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get songs by artist
    open suspend fun getSongsByArtist(artistId: String): Result<List<Song>> {
        return try {
            val snapshot = songsCollection
                .whereEqualTo("artistId", artistId)
                .get()
                .await()
            val songs = snapshot.toObjects(Song::class.java)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get songs by album
    open suspend fun getSongsByAlbum(albumId: String): Result<List<Song>> {
        return try {
            val snapshot = songsCollection
                .whereEqualTo("albumId", albumId)
                .get()
                .await()
            val songs = snapshot.toObjects(Song::class.java)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get popular songs
    open suspend fun getPopularSongs(limit: Long = 20): Result<List<Song>> {
        return try {
            val snapshot = songsCollection
                .orderBy("playCount", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            val songs = snapshot.toObjects(Song::class.java)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Search songs
    open suspend fun searchSongs(query: String): Result<List<Song>> {
        return try {
            val snapshot = songsCollection
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()
            val songs = snapshot.toObjects(Song::class.java)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update play count
    open suspend fun incrementPlayCount(songId: String): Result<Unit> {
        return try {
            val docRef = songsCollection.document(songId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentCount = snapshot.getLong("playCount") ?: 0
                transaction.update(docRef, "playCount", currentCount + 1)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
