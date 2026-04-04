package com.example.cinderssoul.repository

import com.example.cinderssoul.models.Playlist
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PlaylistRepository {
    private val db = FirebaseFirestore.getInstance()
    private val playlistsCollection = db.collection("playlists")

    // Create playlist
    suspend fun createPlaylist(playlist: Playlist): Result<String> {
        return try {
            val docRef = playlistsCollection.document()
            val newPlaylist = playlist.copy(
                playlistId = docRef.id,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            docRef.set(newPlaylist).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user playlists
    suspend fun getUserPlaylists(userId: String): Result<List<Playlist>> {
        return try {
            val snapshot = playlistsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val playlists = snapshot.toObjects(Playlist::class.java)
            Result.success(playlists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get playlist by ID
    suspend fun getPlaylistById(playlistId: String): Result<Playlist?> {
        return try {
            val document = playlistsCollection.document(playlistId).get().await()
            val playlist = document.toObject(Playlist::class.java)
            Result.success(playlist)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Add song to playlist
    suspend fun addSongToPlaylist(playlistId: String, songId: String): Result<Unit> {
        return try {
            val docRef = playlistsCollection.document(playlistId)
            docRef.update(
                mapOf(
                    "songIds" to FieldValue.arrayUnion(songId),
                    "songCount" to FieldValue.increment(1),
                    "updatedAt" to Timestamp.now()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Remove song from playlist
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Result<Unit> {
        return try {
            val docRef = playlistsCollection.document(playlistId)
            docRef.update(
                mapOf(
                    "songIds" to FieldValue.arrayRemove(songId),
                    "songCount" to FieldValue.increment(-1),
                    "updatedAt" to Timestamp.now()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update playlist
    suspend fun updatePlaylist(playlistId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val updatesWithTimestamp = updates.toMutableMap().apply {
                put("updatedAt", Timestamp.now())
            }
            playlistsCollection.document(playlistId).update(updatesWithTimestamp).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete playlist
    suspend fun deletePlaylist(playlistId: String): Result<Unit> {
        return try {
            playlistsCollection.document(playlistId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
