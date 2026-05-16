package com.example.cinderssoul.models

data class Song(
    val id: Int,
    val title: String = "",
    val artistId: Int,
    val albumId: Int? = null,
    val duration: Int = 0, // in seconds
    val audioUrl: String = "",
    val coverUrl: String? = null,
    val genre: String? = null,
    val playCount: Int = 0,
    val lyrics: String? = null,
    val artist: Artist? = null,
    val album: Album? = null
) {
    val artistName: String
        get() = artist?.name ?: "Unknown artist"

    val albumTitle: String
        get() = album?.title ?: "Single"
}
