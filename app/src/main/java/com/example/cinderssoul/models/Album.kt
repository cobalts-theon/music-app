package com.example.cinderssoul.models

import com.google.firebase.Timestamp

data class Album(
    val albumId: String = "",
    val title: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val coverImageUrl: String = "",
    val releaseDate: Timestamp? = null,
    val genre: List<String> = emptyList(),
    val totalTracks: Int = 0,
    val songIds: List<String> = emptyList(),
    val createdAt: Timestamp? = null
)
