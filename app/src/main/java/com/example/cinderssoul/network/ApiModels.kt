package com.example.cinderssoul.network

import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Playlist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.models.User
import com.google.gson.annotations.SerializedName

data class ApiEnvelope<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

data class GoogleAuthRequest(
    val idToken: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)

data class UpdateUserRequest(
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val currentPassword: String? = null,
    val newPassword: String? = null
)

data class CreateArtistRequest(
    val name: String,
    val bio: String? = null,
    val avatarUrl: String? = null
)

data class UpdateArtistRequest(
    val name: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
)

data class CreateAlbumRequest(
    val title: String,
    val artistId: Int,
    val coverUrl: String? = null,
    val releaseDate: String? = null
)

data class UpdateAlbumRequest(
    val title: String? = null,
    val coverUrl: String? = null,
    val releaseDate: String? = null
)

data class CreateSongRequest(
    val title: String,
    val artistId: Int,
    val albumId: Int? = null,
    val audioUrl: String,
    val duration: Int,
    val coverUrl: String? = null,
    val genre: String? = null,
    val lyrics: String? = null
)

data class UpdateSongRequest(
    val title: String? = null,
    val albumId: Int? = null,
    val audioUrl: String? = null,
    val duration: Int? = null,
    val coverUrl: String? = null,
    val genre: String? = null,
    val lyrics: String? = null
)

data class CreatePlaylistRequest(
    val name: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val isPublic: Boolean = false
)

data class UpdatePlaylistRequest(
    val name: String? = null,
    val description: String? = null,
    val coverUrl: String? = null,
    val isPublic: Boolean? = null
)

data class AddSongRequest(
    val songId: Int
)

data class UploadImageDataDto(
    val filename: String,
    val url: String,
    val size: Long,
    val mimetype: String
)

data class AuthDataDto(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String
)

data class AuthProfileDataDto(
    val user: UserDto
)

data class PasswordResetOtpDataDto(
    val email: String,
    val otpExpiresInSeconds: Int
)

data class RefreshTokenDataDto(
    val accessToken: String
)

data class AdminSummaryDto(
    val usersCount: Int = 0,
    val songsCount: Int = 0,
    val artistsCount: Int = 0,
    val albumsCount: Int = 0,
    val playlistsCount: Int = 0
)

data class AdminCreateUserRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val role: String = "user"
)

data class AdminUpdateUserRequest(
    val email: String? = null,
    val password: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val role: String? = null
)

data class UserDto(
    val id: Int,
    val email: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val role: String? = null,
    @SerializedName("display_name") val displayNameSnake: String? = null,
    @SerializedName("avatar_url") val avatarUrlSnake: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    val createdAtCamel: String? = null
)

data class ArtistDto(
    val id: Int,
    val name: String,
    val bio: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

data class AlbumDto(
    val id: Int,
    val title: String,
    @SerializedName("artist_id") val artistId: Int? = null,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    val artist: ArtistDto? = null
)

data class SongDto(
    val id: Int,
    val title: String,
    @SerializedName("artist_id") val artistId: Int,
    @SerializedName("album_id") val albumId: Int? = null,
    @SerializedName("audio_url") val audioUrl: String,
    val duration: Int,
    @SerializedName("cover_url") val coverUrl: String? = null,
    val genre: String? = null,
    val lyrics: String? = null,
    @SerializedName("play_count") val playCount: Int = 0,
    val artist: ArtistDto? = null,
    val album: AlbumDto? = null
)

data class PlaylistDto(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val name: String,
    val description: String? = null,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("is_public") val isPublic: Boolean = false,
    val songs: List<SongDto>? = null
)

fun UserDto.toDomainUser(): User = User(
    id = id,
    email = email,
    displayName = displayName ?: displayNameSnake.orEmpty(),
    avatarUrl = normalizeBackendUrl(avatarUrl ?: avatarUrlSnake),
    role = role ?: "user",
    createdAt = createdAt ?: createdAtCamel
)

fun ArtistDto.toDomainArtist(): Artist = Artist(
    id = id,
    name = name,
    bio = bio,
    avatarUrl = normalizeBackendUrl(avatarUrl)
)

fun AlbumDto.toDomainAlbum(): Album = Album(
    id = id,
    title = title,
    artistId = artistId,
    coverUrl = normalizeBackendUrl(coverUrl),
    releaseDate = releaseDate,
    artist = artist?.toDomainArtist()
)

fun SongDto.toDomainSong(): Song = Song(
    id = id,
    title = title,
    artistId = artistId,
    albumId = albumId,
    duration = duration,
    audioUrl = normalizeBackendUrl(audioUrl).orEmpty(),
    coverUrl = normalizeBackendUrl(coverUrl),
    genre = genre,
    playCount = playCount,
    lyrics = lyrics,
    artist = artist?.toDomainArtist(),
    album = album?.toDomainAlbum()
)

fun PlaylistDto.toDomainPlaylist(): Playlist = Playlist(
    id = id,
    userId = userId,
    name = name,
    description = description,
    coverUrl = normalizeBackendUrl(coverUrl),
    isPublic = isPublic,
    songs = songs?.map { it.toDomainSong() }.orEmpty()
)

private fun normalizeBackendUrl(url: String?): String? {
    return ApiClient.normalizeBackendUrl(url)
}
