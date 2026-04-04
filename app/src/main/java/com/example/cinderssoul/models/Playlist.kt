package com.example.cinderssoul.models

import com.google.firebase.Timestamp

data class Playlist(
    val playlistId: String = "",
    val name: String = "",
    val description: String = "",
    val coverImageUrl: String = "",
    val userId: String = "", // Owner
    val isPublic: Boolean = false,
    val songIds: List<String> = emptyList(),
    val songCount: Int = 0,
    val followers: Long = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
