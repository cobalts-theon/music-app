package com.example.cinderssoul.models

data class Album(
    val id: Int,
    val title: String = "",
    val artistId: Int? = null,
    val coverUrl: String? = null,
    val releaseDate: String? = null,
    val artist: Artist? = null
)
