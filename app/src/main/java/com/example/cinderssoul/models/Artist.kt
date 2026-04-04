package com.example.cinderssoul.models

import com.google.firebase.Timestamp

data class Artist(
    val artistId: String = "",
    val name: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val genres: List<String> = emptyList(),
    val followers: Long = 0,
    val verified: Boolean = false,
    val createdAt: Timestamp? = null
)
