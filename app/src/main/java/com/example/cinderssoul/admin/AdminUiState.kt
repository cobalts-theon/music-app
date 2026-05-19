package com.example.cinderssoul.admin

import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.models.User
import com.example.cinderssoul.network.AdminSummaryDto

internal enum class AdminSection(val title: String) {
    Dashboard("Dashboard"),
    Songs("Songs"),
    Artists("Artists"),
    Albums("Albums"),
    Users("Users")
}

internal data class AdminSongForm(
    val id: Int? = null,
    val title: String = "",
    val artistId: String = "",
    val albumId: String = "",
    val duration: String = "",
    val audioUrl: String = "",
    val coverUrl: String = "",
    val genre: String = "",
    val lyrics: String = ""
) {
    val isEditing: Boolean get() = id != null
}

internal data class AdminArtistForm(
    val id: Int? = null,
    val name: String = "",
    val bio: String = "",
    val avatarUrl: String = ""
) {
    val isEditing: Boolean get() = id != null
}

internal data class AdminAlbumForm(
    val id: Int? = null,
    val title: String = "",
    val artistId: String = "",
    val coverUrl: String = "",
    val releaseDate: String = ""
) {
    val isEditing: Boolean get() = id != null
}

internal data class AdminUserForm(
    val id: Int? = null,
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val role: String = "user",
    val password: String = ""
) {
    val isEditing: Boolean get() = id != null
}

internal data class AdminUiState(
    val activeSection: AdminSection = AdminSection.Dashboard,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
    val summary: AdminSummaryDto = AdminSummaryDto(),
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val users: List<User> = emptyList(),
    val artistDetailId: Int? = null,
    val albumDetailId: Int? = null,
    val songForm: AdminSongForm? = null,
    val artistForm: AdminArtistForm? = null,
    val albumForm: AdminAlbumForm? = null,
    val userForm: AdminUserForm? = null
)
