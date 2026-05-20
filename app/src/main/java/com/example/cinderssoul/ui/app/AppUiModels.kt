package com.example.cinderssoul.ui.app

import androidx.compose.ui.graphics.Color
import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Playlist

internal enum class MusicTab {
    Home, Search, Discover, Library, Profile
}

internal enum class MusicContentRoute {
    NowPlaying,
    PlaylistDetail,
    ArtistDetail,
    AlbumDetail,
    GenreDetail,
    HomeCollectionDetail,
    Home,
    Search,
    Discover,
    Library,
    Profile
}

internal enum class LibrarySection(val title: String) {
    Overview("Library"),
    Playlists("Playlists"),
    Artists("Artists"),
    Albums("Albums"),
    Songs("Songs"),
    Genres("Genres"),
    Downloaded("Downloaded")
}

internal enum class HomeCollection(val title: String) {
    MadeForYou("Made For You"),
    Artists("Artists You Follow"),
    Albums("New Albums"),
    RecentlyAdded("Recently Added"),
    MostPlayed("Most Played")
}

internal sealed class RecentLibraryItem(val stableKey: String) {
    class PlaylistItem(val playlist: Playlist) : RecentLibraryItem("playlist-${playlist.id}")
    class AlbumItem(val album: Album) : RecentLibraryItem("album-${album.id}")
}

internal enum class AuthDialogMode {
    SignIn, Register, ForgotPassword, Profile
}

internal enum class ThemeMode(val storageValue: String) {
    System("system"),
    Light("light"),
    Dark("dark");

    companion object {
        fun fromStorageValue(value: String?): ThemeMode {
            return entries.firstOrNull { it.storageValue == value } ?: System
        }
    }
}

internal val AppleMusicRed = Color(0xFFFF2D55)
internal const val HomeCollapsedItemLimit = 10
internal const val HomeSongGridRows = 4
internal const val HomeSongGridCollapsedColumns = 4
internal const val HomeSongGridCollapsedItemLimit = HomeSongGridRows * HomeSongGridCollapsedColumns
