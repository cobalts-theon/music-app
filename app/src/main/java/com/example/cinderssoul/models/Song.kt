package com.example.cinderssoul.models

import com.google.firebase.Timestamp

data class Song(
    val songId: String = "",
    val title: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val albumId: String = "",
    val albumName: String = "",
    val duration: Int = 0, // in seconds
    val audioUrl: String = "",
    val coverImageUrl: String = "",
    val genre: List<String> = emptyList(),
    val releaseDate: Timestamp? = null,
    val playCount: Long = 0,
    val likeCount: Long = 0,
    val lyrics: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
