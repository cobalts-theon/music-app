package com.example.cinderssoul.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiEnvelope<AuthDataDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiEnvelope<AuthDataDto>

    @POST("api/auth/google")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): ApiEnvelope<AuthDataDto>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): ApiEnvelope<RefreshTokenDataDto>

    @POST("api/auth/forgot-password")
    suspend fun requestPasswordResetOtp(
        @Body request: ForgotPasswordRequest
    ): ApiEnvelope<PasswordResetOtpDataDto>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ApiEnvelope<AuthDataDto>

    @GET("api/auth/me")
    suspend fun getMe(): ApiEnvelope<AuthProfileDataDto>

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Int,
        @Body request: UpdateUserRequest
    ): ApiEnvelope<UserDto>

    @GET("api/admin/summary")
    suspend fun getAdminSummary(): ApiEnvelope<AdminSummaryDto>

    @GET("api/admin/users")
    suspend fun getAdminUsers(): ApiEnvelope<List<UserDto>>

    @POST("api/admin/users")
    suspend fun createAdminUser(@Body request: AdminCreateUserRequest): ApiEnvelope<UserDto>

    @PUT("api/admin/users/{id}")
    suspend fun updateAdminUser(
        @Path("id") userId: Int,
        @Body request: AdminUpdateUserRequest
    ): ApiEnvelope<UserDto>

    @DELETE("api/admin/users/{id}")
    suspend fun deleteAdminUser(@Path("id") userId: Int): ApiEnvelope<Unit>

    @GET("api/songs")
    suspend fun getSongs(
        @Query("search") search: String? = null,
        @Query("genre") genre: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): ApiEnvelope<List<SongDto>>

    @GET("api/artists")
    suspend fun getArtists(
        @Query("search") search: String? = null,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0
    ): ApiEnvelope<List<ArtistDto>>

    @POST("api/artists")
    suspend fun createArtist(@Body request: CreateArtistRequest): ApiEnvelope<ArtistDto>

    @PUT("api/artists/{id}")
    suspend fun updateArtist(
        @Path("id") artistId: Int,
        @Body request: UpdateArtistRequest
    ): ApiEnvelope<ArtistDto>

    @DELETE("api/artists/{id}")
    suspend fun deleteArtist(@Path("id") artistId: Int): ApiEnvelope<Unit>

    @GET("api/albums")
    suspend fun getAlbums(
        @Query("artistId") artistId: Int? = null,
        @Query("search") search: String? = null,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0
    ): ApiEnvelope<List<AlbumDto>>

    @POST("api/albums")
    suspend fun createAlbum(@Body request: CreateAlbumRequest): ApiEnvelope<AlbumDto>

    @PUT("api/albums/{id}")
    suspend fun updateAlbum(
        @Path("id") albumId: Int,
        @Body request: UpdateAlbumRequest
    ): ApiEnvelope<AlbumDto>

    @DELETE("api/albums/{id}")
    suspend fun deleteAlbum(@Path("id") albumId: Int): ApiEnvelope<Unit>

    @POST("api/songs")
    suspend fun createSong(@Body request: CreateSongRequest): ApiEnvelope<SongDto>

    @PUT("api/songs/{id}")
    suspend fun updateSong(
        @Path("id") songId: Int,
        @Body request: UpdateSongRequest
    ): ApiEnvelope<SongDto>

    @DELETE("api/songs/{id}")
    suspend fun deleteSong(@Path("id") songId: Int): ApiEnvelope<Unit>

    @GET("api/playlists")
    suspend fun getPlaylists(): ApiEnvelope<List<PlaylistDto>>

    @GET("api/playlists/{id}")
    suspend fun getPlaylist(@Path("id") playlistId: Int): ApiEnvelope<PlaylistDto>

    @POST("api/playlists")
    suspend fun createPlaylist(@Body request: CreatePlaylistRequest): ApiEnvelope<PlaylistDto>

    @PUT("api/playlists/{id}")
    suspend fun updatePlaylist(
        @Path("id") playlistId: Int,
        @Body request: UpdatePlaylistRequest
    ): ApiEnvelope<PlaylistDto>

    @DELETE("api/playlists/{id}")
    suspend fun deletePlaylist(@Path("id") playlistId: Int): ApiEnvelope<Unit>

    @POST("api/playlists/{id}/songs")
    suspend fun addSongToPlaylist(
        @Path("id") playlistId: Int,
        @Body request: AddSongRequest
    ): ApiEnvelope<Unit>

    @DELETE("api/playlists/{id}/songs/{songId}")
    suspend fun removeSongFromPlaylist(
        @Path("id") playlistId: Int,
        @Path("songId") songId: Int
    ): ApiEnvelope<Unit>

    @GET("api/library/songs")
    suspend fun getLibrarySongs(): ApiEnvelope<List<SongDto>>

    @POST("api/library/songs")
    suspend fun addSongToLibrary(
        @Body request: AddSongRequest
    ): ApiEnvelope<SongDto>

    @DELETE("api/library/songs/{songId}")
    suspend fun removeSongFromLibrary(
        @Path("songId") songId: Int
    ): ApiEnvelope<Unit>

    @Multipart
    @POST("api/upload/image")
    suspend fun uploadImage(
        @Part image: okhttp3.MultipartBody.Part
    ): ApiEnvelope<UploadImageDataDto>
}
