package com.example.cinderssoul.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.models.User
import com.example.cinderssoul.network.AdminCreateUserRequest
import com.example.cinderssoul.network.AdminUpdateUserRequest
import com.example.cinderssoul.network.CreateAlbumRequest
import com.example.cinderssoul.network.CreateArtistRequest
import com.example.cinderssoul.network.CreateSongRequest
import com.example.cinderssoul.network.UpdateAlbumRequest
import com.example.cinderssoul.network.UpdateArtistRequest
import com.example.cinderssoul.network.UpdateSongRequest
import com.example.cinderssoul.repository.AdminRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class AdminViewModel(
    private val repository: AdminRepository = AdminRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun setSection(section: AdminSection) {
        _uiState.value = _uiState.value.copy(
            activeSection = section,
            searchQuery = "",
            artistDetailId = null,
            albumDetailId = null,
            statusMessage = null,
            errorMessage = null
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val summaryDeferred = async { repository.getSummary() }
            val usersDeferred = async { repository.getUsers() }
            val songsDeferred = async { repository.getSongs() }
            val artistsDeferred = async { repository.getArtists() }
            val albumsDeferred = async { repository.getAlbums() }

            val summaryResult = summaryDeferred.await()
            val usersResult = usersDeferred.await()
            val songsResult = songsDeferred.await()
            val artistsResult = artistsDeferred.await()
            val albumsResult = albumsDeferred.await()
            val firstError = listOf(summaryResult, usersResult, songsResult, artistsResult, albumsResult)
                .firstOrNull { it.isFailure }
                ?.exceptionOrNull()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                summary = summaryResult.getOrDefault(_uiState.value.summary),
                users = usersResult.getOrDefault(_uiState.value.users),
                songs = songsResult.getOrDefault(_uiState.value.songs),
                artists = artistsResult.getOrDefault(_uiState.value.artists),
                albums = albumsResult.getOrDefault(_uiState.value.albums),
                errorMessage = firstError?.message
            )
        }
    }

    fun openNewSong() {
        _uiState.value = _uiState.value.copy(songForm = AdminSongForm(), statusMessage = null, errorMessage = null)
    }

    fun openNewSongForArtist(artist: Artist) {
        _uiState.value = _uiState.value.copy(
            songForm = AdminSongForm(artistId = artist.id.toString()),
            statusMessage = null,
            errorMessage = null
        )
    }

    fun openNewSongForAlbum(album: Album) {
        val artistId = album.artistId ?: album.artist?.id
        _uiState.value = _uiState.value.copy(
            songForm = AdminSongForm(
                artistId = artistId?.toString().orEmpty(),
                albumId = album.id.toString(),
                coverUrl = album.coverUrl.orEmpty()
            ),
            statusMessage = null,
            errorMessage = null
        )
    }

    fun editSong(song: Song) {
        _uiState.value = _uiState.value.copy(
            songForm = AdminSongForm(
                id = song.id,
                title = song.title,
                artistId = song.artistId.toString(),
                albumId = song.albumId?.toString().orEmpty(),
                duration = song.duration.toString(),
                audioUrl = song.audioUrl,
                coverUrl = song.coverUrl.orEmpty(),
                genre = song.genre.orEmpty(),
                lyrics = song.lyrics.orEmpty()
            ),
            statusMessage = null,
            errorMessage = null
        )
    }

    fun updateSongForm(transform: (AdminSongForm) -> AdminSongForm) {
        _uiState.value = _uiState.value.copy(songForm = _uiState.value.songForm?.let(transform))
    }

    fun closeSongForm() {
        _uiState.value = _uiState.value.copy(songForm = null)
    }

    fun saveSong() {
        val form = _uiState.value.songForm ?: return
        val title = form.title.trim()
        val audioUrl = form.audioUrl.trim()
        val duration = form.duration.trim().toIntOrNull()
        val artistId = form.artistId.trim().toIntOrNull()
        val albumId = form.albumId.trim().takeIf { it.isNotBlank() }?.toIntOrNull()

        if (title.isBlank() || audioUrl.isBlank() || duration == null || (!form.isEditing && artistId == null)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Title, artist, audio URL, and detected duration are required.")
            return
        }

        runSaving("Song saved.") {
            if (form.isEditing) {
                repository.updateSong(
                    form.id!!,
                    UpdateSongRequest(
                        title = title,
                        albumId = albumId,
                        audioUrl = audioUrl,
                        duration = duration,
                        coverUrl = form.coverUrl.trim().takeIf { it.isNotBlank() },
                        genre = form.genre.trim().takeIf { it.isNotBlank() },
                        lyrics = form.lyrics.trim().takeIf { it.isNotBlank() }
                    )
                )
            } else {
                repository.createSong(
                    CreateSongRequest(
                        title = title,
                        artistId = artistId!!,
                        albumId = albumId,
                        audioUrl = audioUrl,
                        duration = duration,
                        coverUrl = form.coverUrl.trim().takeIf { it.isNotBlank() },
                        genre = form.genre.trim().takeIf { it.isNotBlank() },
                        lyrics = form.lyrics.trim().takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }

    fun deleteSong(song: Song) {
        runSaving("Song deleted.") {
            repository.deleteSong(song.id)
        }
    }

    fun openArtistDetails(artist: Artist) {
        _uiState.value = _uiState.value.copy(
            artistDetailId = artist.id,
            albumDetailId = null,
            statusMessage = null,
            errorMessage = null
        )
    }

    fun closeArtistDetails() {
        _uiState.value = _uiState.value.copy(artistDetailId = null)
    }

    fun openNewArtist() {
        _uiState.value = _uiState.value.copy(artistForm = AdminArtistForm(), statusMessage = null, errorMessage = null)
    }

    fun editArtist(artist: Artist) {
        _uiState.value = _uiState.value.copy(
            artistForm = AdminArtistForm(
                id = artist.id,
                name = artist.name,
                bio = artist.bio.orEmpty(),
                avatarUrl = artist.avatarUrl.orEmpty()
            ),
            statusMessage = null,
            errorMessage = null
        )
    }

    fun updateArtistForm(transform: (AdminArtistForm) -> AdminArtistForm) {
        _uiState.value = _uiState.value.copy(artistForm = _uiState.value.artistForm?.let(transform))
    }

    fun closeArtistForm() {
        _uiState.value = _uiState.value.copy(artistForm = null)
    }

    fun saveArtist() {
        val form = _uiState.value.artistForm ?: return
        val name = form.name.trim()
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Artist name is required.")
            return
        }

        runSaving("Artist saved.") {
            if (form.isEditing) {
                repository.updateArtist(
                    form.id!!,
                    UpdateArtistRequest(
                        name = name,
                        bio = form.bio.trim().takeIf { it.isNotBlank() },
                        avatarUrl = form.avatarUrl.trim().takeIf { it.isNotBlank() }
                    )
                )
            } else {
                repository.createArtist(
                    CreateArtistRequest(
                        name = name,
                        bio = form.bio.trim().takeIf { it.isNotBlank() },
                        avatarUrl = form.avatarUrl.trim().takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }

    fun deleteArtist(artist: Artist) {
        runSaving("Artist deleted.") {
            repository.deleteArtist(artist.id)
        }
    }

    fun openAlbumDetails(album: Album) {
        _uiState.value = _uiState.value.copy(
            albumDetailId = album.id,
            artistDetailId = null,
            statusMessage = null,
            errorMessage = null
        )
    }

    fun closeAlbumDetails() {
        _uiState.value = _uiState.value.copy(albumDetailId = null)
    }

    fun openNewAlbum() {
        _uiState.value = _uiState.value.copy(albumForm = AdminAlbumForm(), statusMessage = null, errorMessage = null)
    }

    fun editAlbum(album: Album) {
        _uiState.value = _uiState.value.copy(
            albumForm = AdminAlbumForm(
                id = album.id,
                title = album.title,
                artistId = album.artistId?.toString().orEmpty(),
                coverUrl = album.coverUrl.orEmpty(),
                releaseDate = album.releaseDate.orEmpty()
            ),
            statusMessage = null,
            errorMessage = null
        )
    }

    fun updateAlbumForm(transform: (AdminAlbumForm) -> AdminAlbumForm) {
        _uiState.value = _uiState.value.copy(albumForm = _uiState.value.albumForm?.let(transform))
    }

    fun closeAlbumForm() {
        _uiState.value = _uiState.value.copy(albumForm = null)
    }

    fun saveAlbum() {
        val form = _uiState.value.albumForm ?: return
        val title = form.title.trim()
        val artistId = form.artistId.trim().toIntOrNull()
        if (title.isBlank() || (!form.isEditing && artistId == null)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Album title and artist are required.")
            return
        }

        runSaving("Album saved.") {
            if (form.isEditing) {
                repository.updateAlbum(
                    form.id!!,
                    UpdateAlbumRequest(
                        title = title,
                        coverUrl = form.coverUrl.trim().takeIf { it.isNotBlank() },
                        releaseDate = form.releaseDate.trim().takeIf { it.isNotBlank() }
                    )
                )
            } else {
                repository.createAlbum(
                    CreateAlbumRequest(
                        title = title,
                        artistId = artistId!!,
                        coverUrl = form.coverUrl.trim().takeIf { it.isNotBlank() },
                        releaseDate = form.releaseDate.trim().takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }

    fun deleteAlbum(album: Album) {
        runSaving("Album deleted.") {
            repository.deleteAlbum(album.id)
        }
    }

    fun openNewUser() {
        _uiState.value = _uiState.value.copy(userForm = AdminUserForm(), statusMessage = null, errorMessage = null)
    }

    fun editUser(user: User) {
        _uiState.value = _uiState.value.copy(
            userForm = AdminUserForm(
                id = user.id,
                email = user.email,
                displayName = user.displayName,
                avatarUrl = user.avatarUrl.orEmpty(),
                role = user.role,
                password = ""
            ),
            statusMessage = null,
            errorMessage = null
        )
    }

    fun updateUserForm(transform: (AdminUserForm) -> AdminUserForm) {
        _uiState.value = _uiState.value.copy(userForm = _uiState.value.userForm?.let(transform))
    }

    fun closeUserForm() {
        _uiState.value = _uiState.value.copy(userForm = null)
    }

    fun saveUser() {
        val form = _uiState.value.userForm ?: return
        val email = form.email.trim()
        val displayName = form.displayName.trim()
        val password = form.password.trim()
        val normalizedRole = form.role.trim().lowercase()

        if (email.isBlank() || displayName.isBlank() || (!form.isEditing && password.isBlank())) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email, display name, and password are required.")
            return
        }

        if (normalizedRole !in setOf("user", "admin")) {
            _uiState.value = _uiState.value.copy(errorMessage = "Role must be user or admin.")
            return
        }

        runSaving("User saved.") {
            if (form.isEditing) {
                repository.updateUser(
                    form.id!!,
                    AdminUpdateUserRequest(
                        email = email,
                        displayName = displayName,
                        avatarUrl = form.avatarUrl.trim().takeIf { it.isNotBlank() },
                        role = normalizedRole,
                        password = password.takeIf { it.isNotBlank() }
                    )
                )
            } else {
                repository.createUser(
                    AdminCreateUserRequest(
                        email = email,
                        password = password,
                        displayName = displayName,
                        avatarUrl = form.avatarUrl.trim().takeIf { it.isNotBlank() },
                        role = normalizedRole
                    )
                )
            }
        }
    }

    fun deleteUser(user: User) {
        runSaving("User deleted.") {
            repository.deleteUser(user.id)
        }
    }

    private fun <T> runSaving(successMessage: String, block: suspend () -> Result<T>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, statusMessage = null)
            val result = block()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    songForm = null,
                    artistForm = null,
                    albumForm = null,
                    userForm = null,
                    statusMessage = successMessage
                )
                refresh()
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Request failed."
                )
            }
        }
    }
}
