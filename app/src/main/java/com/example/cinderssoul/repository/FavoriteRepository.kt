package com.example.cinderssoul.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoriteRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Add song to favorites
    suspend fun addToFavorites(songId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val favoriteRef = db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(songId)
            
            favoriteRef.set(
                mapOf(
                    "songId" to songId,
                    "addedAt" to Timestamp.now()
                )
            ).await()
            
            // Increment like count on song
            db.collection("songs").document(songId)
                .update("likeCount", FieldValue.increment(1))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Remove from favorites
    suspend fun removeFromFavorites(songId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(songId)
                .delete()
                .await()
            
            // Decrement like count on song
            db.collection("songs").document(songId)
                .update("likeCount", FieldValue.increment(-1))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Check if song is favorite
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun isFavorite(songId: String): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val doc = db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(songId)
                .get()
                .await()
            Result.success(doc.exists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get all favorite song IDs
    suspend fun getFavoriteSongIds(): Result<List<String>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val snapshot = db.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .await()
            val songIds = snapshot.documents.map { it.id }
            Result.success(songIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
