package com.example.cinderssoul.models

data class Playlist(
    val id: Int,
    val userId: Int,
    val name: String = "",
    val description: String? = null,
    val coverUrl: String? = null,
    val isPublic: Boolean = false,
    val songs: List<Song> = emptyList()
)
