package com.example.cinderssoul.ui.app

import android.R
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cinderssoul.ui.dialogs.AccountAuthDialog
import com.example.cinderssoul.ui.dialogs.AddSongToPlaylistDialog
import com.example.cinderssoul.AuthenticationActivity
import com.example.cinderssoul.ui.dialogs.CreatePlaylistDialog
import com.example.cinderssoul.ui.browse.DiscoverTab
import com.example.cinderssoul.ui.dialogs.EditPlaylistDialog
import com.example.cinderssoul.ui.home.HomeTab
import com.example.cinderssoul.LibraryTab
import com.example.cinderssoul.MusicViewModel
import com.example.cinderssoul.ui.player.NowPlayingScreen
import com.example.cinderssoul.PlaylistDetailScreen
import com.example.cinderssoul.ui.browse.ProfileTab
import com.example.cinderssoul.ui.browse.SearchTab
import com.example.cinderssoul.SongCollectionDetailScreen
import com.example.cinderssoul.ui.components.TopBarAccountAction
import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Playlist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.ui.dialogs.requestGoogleIdToken
import com.example.cinderssoul.ui.navigation.AppBottomBar
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun MusicApp(viewModel: MusicViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableStateOf(MusicTab.Home) }
    var showNowPlaying by rememberSaveable { mutableStateOf(false) }
    var compactBottomBar by rememberSaveable { mutableStateOf(false) }
    var activeLibrarySection by rememberSaveable { mutableStateOf(LibrarySection.Overview) }
    var openedPlaylistId by rememberSaveable { mutableStateOf<Int?>(null) }
    var openedArtistId by rememberSaveable { mutableStateOf<Int?>(null) }
    var openedAlbumId by rememberSaveable { mutableStateOf<Int?>(null) }
    var openedGenreName by rememberSaveable { mutableStateOf<String?>(null) }
    var showCreatePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var showAccountDialog by rememberSaveable { mutableStateOf(false) }
    var pendingSongForPlaylist by remember { mutableStateOf<Song?>(null) }
    var playlistEditTargetId by rememberSaveable { mutableStateOf<Int?>(null) }

    val uiState by viewModel.uiState
    val nowPlayingSong = uiState.currentSong
    val openedPlaylist = uiState.playlists.firstOrNull { it.id == openedPlaylistId }
    val openedArtist = uiState.artists.firstOrNull { it.id == openedArtistId }
    val openedAlbum = uiState.albums.firstOrNull { it.id == openedAlbumId }
    val openedArtistSongs = openedArtist?.let { artist -> uiState.songs.filter { it.artistId == artist.id } } ?: emptyList()
    val openedAlbumSongs = openedAlbum?.let { album -> uiState.songs.filter { it.albumId == album.id } } ?: emptyList()
    val openedGenreSongs = openedGenreName?.let { genre ->
        uiState.songs.filter { it.genre.equals(genre, ignoreCase = true) }
    } ?: emptyList()
    val playlistEditTarget = uiState.playlists.firstOrNull { it.id == playlistEditTargetId }
    val hasNowPlayingSong = nowPlayingSong != null
    val libraryTopBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Black,
        scrolledContainerColor = Color.Black,
        navigationIconContentColor = AppleMusicRed,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val authActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshAuthUser()
    }
    val accountAction: () -> Unit = {
        if (uiState.authUser == null) {
            authActivityLauncher.launch(Intent(context, AuthenticationActivity::class.java))
        } else {
            selectedTab = MusicTab.Profile
            activeLibrarySection = LibrarySection.Overview
            openedPlaylistId = null
            openedArtistId = null
            openedAlbumId = null
            openedGenreName = null
            if (compactBottomBar) compactBottomBar = false
        }
    }
    val profileAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { imageUri ->
        viewModel.setProfileAvatarFromLibrary(imageUri)
    }
    val launchGoogleSignIn: () -> Unit = {
        coroutineScope.launch {
            requestGoogleIdToken(context)
                .onSuccess(viewModel::signInWithGoogleIdToken)
                .onFailure { error ->
                    viewModel.reportAuthMessage(error.message ?: "Google sign-in failed.")
                }
        }
    }
    val logoutAndOpenAuth: () -> Unit = {
        viewModel.logout()
        showAccountDialog = false
        context.startActivity(Intent(context, AuthenticationActivity::class.java))
        (context as? ComponentActivity)?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        (context as? ComponentActivity)?.finish()
    }
    fun clearLibraryDetailState() {
        activeLibrarySection = LibrarySection.Overview
        openedPlaylistId = null
        openedArtistId = null
        openedAlbumId = null
        openedGenreName = null
    }
    fun selectTab(tab: MusicTab) {
        selectedTab = tab
        clearLibraryDetailState()
        if (compactBottomBar) compactBottomBar = false
    }
    fun openPlaylist(playlist: Playlist) {
        selectedTab = MusicTab.Library
        openedPlaylistId = playlist.id
        openedArtistId = null
        openedAlbumId = null
        openedGenreName = null
        activeLibrarySection = LibrarySection.Playlists
        if (compactBottomBar) compactBottomBar = false
    }
    fun openArtist(artist: Artist) {
        selectedTab = MusicTab.Library
        openedArtistId = artist.id
        openedAlbumId = null
        openedGenreName = null
        openedPlaylistId = null
        activeLibrarySection = LibrarySection.Artists
        if (compactBottomBar) compactBottomBar = false
    }
    fun openAlbum(album: Album) {
        selectedTab = MusicTab.Library
        openedAlbumId = album.id
        openedArtistId = null
        openedGenreName = null
        openedPlaylistId = null
        activeLibrarySection = LibrarySection.Albums
        if (compactBottomBar) compactBottomBar = false
    }
    fun openGenre(genre: String) {
        selectedTab = MusicTab.Library
        openedGenreName = genre
        openedArtistId = null
        openedAlbumId = null
        openedPlaylistId = null
        activeLibrarySection = LibrarySection.Genres
        if (compactBottomBar) compactBottomBar = false
    }
    val contentRoute = when {
        showNowPlaying && nowPlayingSong != null -> MusicContentRoute.NowPlaying
        selectedTab == MusicTab.Library && openedPlaylist != null -> MusicContentRoute.PlaylistDetail
        selectedTab == MusicTab.Library && openedArtist != null -> MusicContentRoute.ArtistDetail
        selectedTab == MusicTab.Library && openedAlbum != null -> MusicContentRoute.AlbumDetail
        selectedTab == MusicTab.Library && !openedGenreName.isNullOrBlank() -> MusicContentRoute.GenreDetail
        selectedTab == MusicTab.Home -> MusicContentRoute.Home
        selectedTab == MusicTab.Search -> MusicContentRoute.Search
        selectedTab == MusicTab.Discover -> MusicContentRoute.Discover
        selectedTab == MusicTab.Profile -> MusicContentRoute.Profile
        else -> MusicContentRoute.Library
    }

    Scaffold(
        modifier = Modifier,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            if (!showNowPlaying) {
                if (selectedTab == MusicTab.Library && openedPlaylist != null) {
                    TopAppBar(
                        title = { Text(openedPlaylist.name, style = MaterialTheme.typography.headlineSmall) },
                        navigationIcon = {
                            IconButton(onClick = { openedPlaylistId = null }) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            TopBarAccountAction(
                                user = uiState.authUser,
                                onClick = accountAction
                            )
                        },
                        colors = libraryTopBarColors
                    )
                } else if (selectedTab == MusicTab.Library && openedArtist != null) {
                    TopAppBar(
                        title = { Text(openedArtist.name, style = MaterialTheme.typography.headlineSmall) },
                        navigationIcon = {
                            IconButton(onClick = { openedArtistId = null }) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            TopBarAccountAction(
                                user = uiState.authUser,
                                onClick = accountAction
                            )
                        },
                        colors = libraryTopBarColors
                    )
                } else if (selectedTab == MusicTab.Library && openedAlbum != null) {
                    TopAppBar(
                        title = { Text(openedAlbum.title, style = MaterialTheme.typography.headlineSmall) },
                        navigationIcon = {
                            IconButton(onClick = { openedAlbumId = null }) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            TopBarAccountAction(
                                user = uiState.authUser,
                                onClick = accountAction
                            )
                        },
                        colors = libraryTopBarColors
                    )
                } else if (selectedTab == MusicTab.Library && !openedGenreName.isNullOrBlank()) {
                    TopAppBar(
                        title = { Text(openedGenreName.orEmpty(), style = MaterialTheme.typography.headlineSmall) },
                        navigationIcon = {
                            IconButton(onClick = { openedGenreName = null }) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            TopBarAccountAction(
                                user = uiState.authUser,
                                onClick = accountAction
                            )
                        },
                        colors = libraryTopBarColors
                    )
                } else if (selectedTab == MusicTab.Library && activeLibrarySection != LibrarySection.Overview) {
                    TopAppBar(
                        title = { Text(activeLibrarySection.title) },
                        navigationIcon = {
                            IconButton(onClick = { activeLibrarySection = LibrarySection.Overview }) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            TopBarAccountAction(
                                user = uiState.authUser,
                                onClick = accountAction
                            )
                        },
                        colors = libraryTopBarColors
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = contentRoute,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(180)) +
                        slideInHorizontally(animationSpec = tween(220)) { it / 8 })
                        .togetherWith(
                            fadeOut(animationSpec = tween(140)) +
                                slideOutHorizontally(animationSpec = tween(180)) { -it / 10 }
                        )
                },
                label = "main-content"
            ) { route ->
                when (route) {
                MusicContentRoute.NowPlaying -> {
                    if (nowPlayingSong != null) {
                        NowPlayingScreen(
                            modifier = Modifier.padding(paddingValues),
                            uiState = uiState,
                            onDismiss = { showNowPlaying = false },
                            onSeekTo = viewModel::seekTo,
                            onSeekBy = viewModel::seekBy,
                            onPlayPause = viewModel::togglePlayback,
                            onNext = viewModel::playNextSong,
                            onPrevious = viewModel::playPreviousSong,
                            onVolumeChange = viewModel::setVolume,
                            onCycleRepeat = viewModel::cycleRepeatMode,
                            onToggleLike = viewModel::toggleCurrentSongLiked,
                            onAddSongToPlaylist = { song -> pendingSongForPlaylist = song }
                        )
                    }
                }

                MusicContentRoute.PlaylistDetail -> {
                    openedPlaylist?.let { playlist ->
                        PlaylistDetailScreen(
                            modifier = Modifier.padding(paddingValues),
                            playlist = playlist,
                            onPlaySong = { viewModel.playOrToggleSong(it) },
                            onAddSongToPlaylist = { song -> pendingSongForPlaylist = song },
                            onDownloadSong = viewModel::downloadSong,
                            onEditPlaylist = { playlistEditTargetId = it.id },
                            onDownloadPlaylist = viewModel::downloadPlaylist,
                            onDeleteSong = { song ->
                                viewModel.removeSongFromPlaylist(
                                    song,
                                    playlist.id
                                )
                            },
                            onDeletePlaylist = {
                                if (viewModel.deletePlaylist(it.id)) {
                                    openedPlaylistId = null
                                    activeLibrarySection = LibrarySection.Playlists
                                }
                            },
                            onCollapseBottomBar = {
                                if (!compactBottomBar) compactBottomBar = true
                            },
                            onExpandBottomBar = { if (compactBottomBar) compactBottomBar = false }
                        )
                    }
                }

                MusicContentRoute.ArtistDetail -> {
                    openedArtist?.let { artist ->
                        SongCollectionDetailScreen(
                            modifier = Modifier.padding(paddingValues),
                            coverUrl = artist.avatarUrl,
                            title = artist.name,
                            subtitle = "${openedArtistSongs.size} songs",
                            songs = openedArtistSongs,
                            emptyText = "No songs available for this artist.",
                            onPlaySong = { viewModel.playOrToggleSong(it) },
                            onAddSongToPlaylist = { song -> pendingSongForPlaylist = song },
                            onDownloadSong = viewModel::downloadSong,
                            onCollapseBottomBar = {
                                if (!compactBottomBar) compactBottomBar = true
                            },
                            onExpandBottomBar = { if (compactBottomBar) compactBottomBar = false }
                        )
                    }
                }

                MusicContentRoute.AlbumDetail -> {
                    openedAlbum?.let { album ->
                        SongCollectionDetailScreen(
                            modifier = Modifier.padding(paddingValues),
                            coverUrl = album.coverUrl,
                            title = album.title,
                            subtitle = "${openedAlbumSongs.size} songs",
                            songs = openedAlbumSongs,
                            emptyText = "No songs available for this album.",
                            onPlaySong = { viewModel.playOrToggleSong(it) },
                            onAddSongToPlaylist = { song -> pendingSongForPlaylist = song },
                            onDownloadSong = viewModel::downloadSong,
                            onCollapseBottomBar = {
                                if (!compactBottomBar) compactBottomBar = true
                            },
                            onExpandBottomBar = { if (compactBottomBar) compactBottomBar = false }
                        )
                    }
                }

                MusicContentRoute.GenreDetail -> {
                    SongCollectionDetailScreen(
                        modifier = Modifier.padding(paddingValues),
                        coverUrl = null,
                        title = openedGenreName.orEmpty(),
                        subtitle = "${openedGenreSongs.size} songs",
                        songs = openedGenreSongs,
                        emptyText = "No songs available for this genre.",
                        onPlaySong = { viewModel.playOrToggleSong(it) },
                        onAddSongToPlaylist = { song -> pendingSongForPlaylist = song },
                        onDownloadSong = viewModel::downloadSong,
                        onCollapseBottomBar = { if (!compactBottomBar) compactBottomBar = true },
                        onExpandBottomBar = { if (compactBottomBar) compactBottomBar = false }
                    )
                }

                MusicContentRoute.Home -> HomeTab(
                    modifier = Modifier.padding(paddingValues),
                    uiState = uiState,
                    authUser = uiState.authUser,
                    onRetry = viewModel::refreshData,
                    onPlaySong = { viewModel.playOrToggleSong(it) },
                    onAddSongToPlaylist = { song -> pendingSongForPlaylist = song },
                    onDownloadSong = viewModel::downloadSong,
                    onArtistClick = { openArtist(it) },
                    onAlbumClick = { openAlbum(it) },
                    onAccountClick = accountAction,
                    onCollapseBottomBar = { if (!compactBottomBar) compactBottomBar = true },
                    onExpandBottomBar = { if (compactBottomBar) compactBottomBar = false }
                )

                MusicContentRoute.Search -> SearchTab(
                    modifier = Modifier.padding(paddingValues),
                    query = uiState.searchQuery,
                    songs = uiState.songs,
                    artists = uiState.artists,
                    albums = uiState.albums,
                    authUser = uiState.authUser,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onPlaySong = { viewModel.playOrToggleSong(it) },
                    onAddSongToPlaylist = { song -> pendingSongForPlaylist = song },
                    onDownloadSong = viewModel::downloadSong,
                    onArtistClick = { openArtist(it) },
                    onAlbumClick = { openAlbum(it) },
                    onGenreClick = { openGenre(it) },
                    onAccountClick = accountAction,
                    onCollapseBottomBar = { if (!compactBottomBar) compactBottomBar = true },
                    onExpandBottomBar = { if (compactBottomBar) compactBottomBar = false }
                )

                MusicContentRoute.Discover -> DiscoverTab(
                    modifier = Modifier.padding(paddingValues),
                    songs = uiState.songs,
                    artists = uiState.artists,
                    albums = uiState.albums,
                    authUser = uiState.authUser,
                    onPlaySong = { viewModel.playOrToggleSong(it) },
                    onAddSongToPlaylist = { song -> pendingSongForPlaylist = song },
                    onDownloadSong = viewModel::downloadSong,
                    onArtistClick = { openArtist(it) },
                    onAlbumClick = { openAlbum(it) },
                    onGenreClick = { openGenre(it) },
                    onAccountClick = accountAction,
                    onCollapseBottomBar = { if (!compactBottomBar) compactBottomBar = true },
                    onExpandBottomBar = { if (compactBottomBar) compactBottomBar = false }
                )

                MusicContentRoute.Library -> LibraryTab(
                    modifier = Modifier.padding(paddingValues),
                    activeSection = activeLibrarySection,
                    albums = uiState.albums,
                    artists = uiState.artists,
                    songs = uiState.songs,
                    playlists = uiState.playlists,
                    downloadedSongIds = uiState.downloadedSongIds,
                    authUser = uiState.authUser,
                    onPlaySong = { viewModel.playOrToggleSong(it) },
                    onAddSongToPlaylist = { song -> pendingSongForPlaylist = song },
                    onDownloadSong = viewModel::downloadSong,
                    onPlaylistClick = { openPlaylist(it) },
                    onArtistClick = { openArtist(it) },
                    onAlbumClick = { openAlbum(it) },
                    onGenreClick = { openGenre(it) },
                    onSectionChange = {
                        activeLibrarySection = it
                        openedPlaylistId = null
                        openedArtistId = null
                        openedAlbumId = null
                        openedGenreName = null
                    },
                    onCreatePlaylist = { showCreatePlaylistDialog = true },
                    onAccountClick = accountAction,
                    onCollapseBottomBar = { if (!compactBottomBar) compactBottomBar = true },
                    onExpandBottomBar = { if (compactBottomBar) compactBottomBar = false }
                )

                MusicContentRoute.Profile -> ProfileTab(
                    modifier = Modifier.padding(paddingValues),
                    user = uiState.authUser,
                    isLoading = uiState.isAuthLoading,
                    message = uiState.authMessage,
                    onLogin = accountAction,
                    onEditProfile = { showAccountDialog = true },
                    onLogout = logoutAndOpenAuth,
                    onCollapseBottomBar = { if (!compactBottomBar) compactBottomBar = true },
                    onExpandBottomBar = { if (compactBottomBar) compactBottomBar = false }
                )
                }
            }

            if (!showNowPlaying) {
                AppBottomBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    selectedTab = selectedTab,
                    compact = compactBottomBar && hasNowPlayingSong,
                    song = nowPlayingSong,
                    isPlaying = uiState.isPlayerRunning,
                    onOpenNowPlaying = { showNowPlaying = true },
                    onPlayPause = viewModel::togglePlayback,
                    onNext = viewModel::playNextSong,
                    onTabSelected = { selectTab(it) },
                    onExpand = { compactBottomBar = false }
                )
            }
        }
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name, description, imageUri ->
                viewModel.createPlaylist(name, description, imageUri) { created ->
                    if (created != null && pendingSongForPlaylist != null) {
                        viewModel.addSongToPlaylist(pendingSongForPlaylist!!, created.id)
                        pendingSongForPlaylist = null
                    }
                    showCreatePlaylistDialog = false
                    openedPlaylistId = created?.id ?: openedPlaylistId
                    openedArtistId = null
                    openedAlbumId = null
                    openedGenreName = null
                    selectedTab = MusicTab.Library
                    activeLibrarySection = LibrarySection.Playlists
                }
            }
        )
    }

    pendingSongForPlaylist?.let { song ->
        AddSongToPlaylistDialog(
            song = song,
            playlists = uiState.playlists,
            onDismiss = { pendingSongForPlaylist = null },
            onCreateNew = { showCreatePlaylistDialog = true },
            onAdd = { playlistId ->
                viewModel.addSongToPlaylist(song, playlistId)
                pendingSongForPlaylist = null
                selectedTab = MusicTab.Library
                openedPlaylistId = playlistId
                openedArtistId = null
                openedAlbumId = null
                openedGenreName = null
                activeLibrarySection = LibrarySection.Playlists
            }
        )
    }

    playlistEditTarget?.let { playlist ->
        EditPlaylistDialog(
            playlist = playlist,
            onDismiss = { playlistEditTargetId = null },
            onSave = { name, description, imageUri ->
                viewModel.updatePlaylistDetails(playlist.id, name, description, imageUri)
                playlistEditTargetId = null
            }
        )
    }

    if (showAccountDialog) {
        AccountAuthDialog(
            user = uiState.authUser,
            isLoading = uiState.isAuthLoading,
            message = uiState.authMessage,
            resetDevOtp = uiState.passwordResetDevOtp,
            onDismiss = {
                viewModel.clearAuthMessage()
                showAccountDialog = false
            },
            onLogin = viewModel::signInWithEmail,
            onRegister = viewModel::registerWithEmail,
            onGoogleSignIn = launchGoogleSignIn,
            onRequestResetOtp = viewModel::requestPasswordResetOtp,
            onResetPassword = viewModel::resetPasswordWithOtp,
            onUpdateProfile = viewModel::updateProfile,
            onChangeAvatar = { profileAvatarLauncher.launch("image/*") },
            onLogout = logoutAndOpenAuth
        )
    }
}
