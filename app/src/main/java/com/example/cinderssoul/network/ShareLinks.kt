package com.example.cinderssoul.network

object ShareLinks {
    private val baseUrl: String
        get() = ApiClient.shareBaseUrl.trim().trimEnd('/')

    fun song(songId: Int): String = "$baseUrl/share/songs/$songId"

    fun album(albumId: Int): String = "$baseUrl/share/albums/$albumId"

    fun artist(artistId: Int): String = "$baseUrl/share/artists/$artistId"

    fun user(userId: Int): String = "$baseUrl/share/users/$userId"

    fun playlist(playlistId: Int): String = "$baseUrl/share/playlists/$playlistId"
}
