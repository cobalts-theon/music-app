package com.example.cinderssoul.ui.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.cinderssoul.ui.dialogs.AccountAuthDialog
import com.example.cinderssoul.ui.dialogs.AddSongsToPlaylistDialog
import com.example.cinderssoul.ui.dialogs.AddSongToPlaylistDialog
import com.example.cinderssoul.AuthenticationActivity
import com.example.cinderssoul.admin.AdminActivity
import com.example.cinderssoul.ui.dialogs.CreatePlaylistDialog
import com.example.cinderssoul.ui.browse.DiscoverTab
import com.example.cinderssoul.ui.dialogs.EditPlaylistDialog
import com.example.cinderssoul.ui.home.HomeTab
import com.example.cinderssoul.ui.home.HomeCollectionDetailScreen
import com.example.cinderssoul.ui.library.LibraryTab
import com.example.cinderssoul.MusicViewModel
import com.example.cinderssoul.ui.player.NowPlayingScreen
import com.example.cinderssoul.ui.library.PlaylistDetailScreen
import com.example.cinderssoul.ui.browse.ProfileTab
import com.example.cinderssoul.ui.browse.SearchTab
import com.example.cinderssoul.ui.library.SongCollectionDetailScreen
import com.example.cinderssoul.ui.components.TopBarAccountAction
import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Playlist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.ui.dialogs.requestGoogleIdToken
import com.example.cinderssoul.ui.navigation.AppBottomBar
import kotlinx.coroutines.launch

internal val LocalBottomBarContentPadding = staticCompositionLocalOf { 0.dp }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun MusicApp(
    viewModel: MusicViewModel,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableStateOf(MusicTab.Home) }
    var showNowPlaying by rememberSaveable { mutableStateOf(false) }
    var compactBottomBar by rememberSaveable { mutableStateOf(false) }
    var activeLibrarySection by rememberSaveable { mutableStateOf(LibrarySection.Overview) }
    var openedPlaylistId by rememberSaveable { mutableStateOf<Int?>(null) }
    var openedArtistId by rememberSaveable { mutableStateOf<Int?>(null) }
    var openedAlbumId by rememberSaveable { mutableStateOf<Int?>(null) }
    var openedGenreName by rememberSaveable { mutableStateOf<String?>(null) }
    var openedHomeCollection by rememberSaveable { mutableStateOf<HomeCollection?>(null) }
    var usePersonalLibraryForDetail by rememberSaveable { mutableStateOf(false) }
    var showCreatePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var showAccountDialog by rememberSaveable { mutableStateOf(false) }
    var pendingSongForPlaylist by remember { mutableStateOf<Song?>(null) }
    var addSongsTargetPlaylistId by rememberSaveable { mutableStateOf<Int?>(null) }
    var playlistEditTargetId by rememberSaveable { mutableStateOf<Int?>(null) }
    var bottomBarHeightPx by remember { mutableStateOf(0) }
    var hasRequestedNotificationPermission by rememberSaveable { mutableStateOf(false) }

    val uiState by viewModel.uiState
    val nowPlayingSong = uiState.currentSong
    val librarySongIds = (
        uiState.playlists.flatMap { playlist -> playlist.songs.map { it.id } } +
            uiState.downloadedSongIds
        ).toSet()
    val librarySongs = uiState.songs.filter { it.id in librarySongIds }
    val libraryArtistIds = librarySongs.map { it.artistId }.toSet()
    val libraryAlbumIds = librarySongs.mapNotNull { it.albumId }.toSet()
    val libraryArtists = uiState.artists.filter { it.id in libraryArtistIds }
    val libraryAlbums = uiState.albums.filter { it.id in libraryAlbumIds }
    val detailSongs = if (usePersonalLibraryForDetail) librarySongs else uiState.songs
    val openedPlaylist = uiState.playlists.firstOrNull { it.id == openedPlaylistId }
    val openedArtist = uiState.artists.firstOrNull { it.id == openedArtistId }
    val openedAlbum = uiState.albums.firstOrNull { it.id == openedAlbumId }
    val openedArtistSongs = openedArtist?.let { artist -> detailSongs.filter { it.artistId == artist.id } } ?: emptyList()
    val openedAlbumSongs = openedAlbum?.let { album -> detailSongs.filter { it.albumId == album.id } } ?: emptyList()
    val openedGenreSongs = openedGenreName?.let { genre ->
        detailSongs.filter { it.genre.equals(genre, ignoreCase = true) }
    } ?: emptyList()
    val playlistEditTarget = uiState.playlists.firstOrNull { it.id == playlistEditTargetId }
    val hasNowPlayingSong = nowPlayingSong != null
    val bottomBarContentPadding = if (showNowPlaying) {
        0.dp
    } else {
        with(density) { bottomBarHeightPx.toDp() }
    }
    val libraryTopBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        scrolledContainerColor = MaterialTheme.colorScheme.background,
        navigationIconContentColor = AppleMusicRed,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val authActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshAuthUser()
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        hasRequestedNotificationPermission = true
    }
    val openAuthenticationActivity: () -> Unit = {
        authActivityLauncher.launch(Intent(context, AuthenticationActivity::class.java))
    }
    val openAdminActivity: () -> Unit = {
        context.startActivity(Intent(context, AdminActivity::class.java))
    }
    LaunchedEffect(uiState.authUser?.id, uiState.authUser?.role) {
        if (uiState.authUser?.isAdmin == true) {
            openAdminActivity()
            (context as? Activity)?.finish()
        }
    }
    LaunchedEffect(uiState.isPlayerRunning) {
        if (!uiState.isPlayerRunning) return@LaunchedEffect
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@LaunchedEffect
        if (hasRequestedNotificationPermission) return@LaunchedEffect
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) {
            hasRequestedNotificationPermission = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val accountAction: () -> Unit = {
        selectedTab = MusicTab.Profile
        activeLibrarySection = LibrarySection.Overview
        openedPlaylistId = null
        openedArtistId = null
        openedAlbumId = null
        openedGenreName = null
        openedHomeCollection = null
        usePersonalLibraryForDetail = false
        if (compactBottomBar) compactBottomBar = false
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
    val logoutToGuestMode: () -> Unit = {
        viewModel.logout()
        showAccountDialog = false
        if (selectedTab == MusicTab.Profile) {
            selectedTab = MusicTab.Home
        }
    }
    fun clearLibraryDetailState() {
        activeLibrarySection = LibrarySection.Overview
        openedPlaylistId = null
        openedArtistId = null
        openedAlbumId = null
        openedGenreName = null
        openedHomeCollection = null
        usePersonalLibraryForDetail = false
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
        openedHomeCollection = null
        activeLibrarySection = LibrarySection.Playlists
        usePersonalLibraryForDetail = true
        if (compactBottomBar) compactBottomBar = false
    }
    fun openArtist(artist: Artist, personalLibrary: Boolean) {
        openedArtistId = artist.id
        openedAlbumId = null
        openedGenreName = null
        openedPlaylistId = null
        openedHomeCollection = null
        if (personalLibrary) {
            selectedTab = MusicTab.Library
            activeLibrarySection = LibrarySection.Artists
        }
        usePersonalLibraryForDetail = personalLibrary
        if (compactBottomBar) compactBottomBar = false
    }
    fun openAlbum(album: Album, personalLibrary: Boolean) {
        openedAlbumId = album.id
        openedArtistId = null
        openedGenreName = null
        openedPlaylistId = null
        openedHomeCollection = null
        if (personalLibrary) {
            selectedTab = MusicTab.Library
            activeLibrarySection = LibrarySection.Albums
        }
        usePersonalLibraryForDetail = personalLibrary
        if (compactBottomBar) compactBottomBar = false
    }
    fun openGenre(genre: String, personalLibrary: Boolean) {
        openedGenreName = genre
        openedArtistId = null
        openedAlbumId = null
        openedPlaylistId = null
        openedHomeCollection = null
        if (personalLibrary) {
            selectedTab = MusicTab.Library
            activeLibrarySection = LibrarySection.Genres
        }
        usePersonalLibraryForDetail = personalLibrary
        if (compactBottomBar) compactBottomBar = false
    }
    fun openHomeCollection(collection: HomeCollection) {
        openedHomeCollection = collection
        openedPlaylistId = null
        openedArtistId = null
        openedAlbumId = null
        openedGenreName = null
        usePersonalLibraryForDetail = false
        if (compactBottomBar) compactBottomBar = false
    }
    fun closeDetailRoute() {
        openedPlaylistId = null
        openedArtistId = null
        openedAlbumId = null
        openedGenreName = null
        openedHomeCollection = null
        usePersonalLibraryForDetail = false
    }
    val contentRoute = when {
        showNowPlaying && nowPlayingSong != null -> MusicContentRoute.NowPlaying
        openedPlaylist != null -> MusicContentRoute.PlaylistDetail
        openedArtist != null -> MusicContentRoute.ArtistDetail
        openedAlbum != null -> MusicContentRoute.AlbumDetail
        !openedGenreName.isNullOrBlank() -> MusicContentRoute.GenreDetail
        openedHomeCollection != null -> MusicContentRoute.HomeCollectionDetail
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
                if (openedPlaylist != null) {
                    TopAppBar(
                        title = { Text(openedPlaylist.name, style = MaterialTheme.typography.headlineSmall) },
                        navigationIcon = {
                            IconButton(onClick = { closeDetailRoute() }) {
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
                } else if (openedArtist != null) {
                    TopAppBar(
                        title = { Text(openedArtist.name, style = MaterialTheme.typography.headlineSmall) },
                        navigationIcon = {
                            IconButton(onClick = { closeDetailRoute() }) {
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
                } else if (openedAlbum != null) {
                    TopAppBar(
                        title = { Text(openedAlbum.title, style = MaterialTheme.typography.headlineSmall) },
                        navigationIcon = {
                            IconButton(onClick = { closeDetailRoute() }) {
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
                } else if (!openedGenreName.isNullOrBlank()) {
                    TopAppBar(
                        title = { Text(openedGenreName.orEmpty(), style = MaterialTheme.typography.headlineSmall) },
                        navigationIcon = {
                            IconButton(onClick = { closeDetailRoute() }) {
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
                } else if (openedHomeCollection != null) {
                    TopAppBar(
                        title = { Text(openedHomeCollection?.title.orEmpty(), style = MaterialTheme.typography.headlineSmall) },
                        navigationIcon = {
                            IconButton(onClick = { closeDetailRoute() }) {
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
                            IconButton(onClick = {
                                activeLibrarySection = LibrarySection.Overview
                                usePersonalLibraryForDetail = false
                            }) {
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
                modifier = Modifier
                    .fillMaxSize(),
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
                CompositionLocalProvider(LocalBottomBarContentPadding provides bottomBarContentPadding) {
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
                            onAddSongToPlaylist = { song -> pendingSongForPlaylist = song },
                            onShareSong = viewModel::shareSong
                        )
                    }
                }

                MusicContentRoute.PlaylistDetail -> {
                    openedPlaylist?.let { playlist ->
                        PlaylistDetailScreen(
                            modifier = Modifier.padding(paddingValues),
                            playlist = playlist,
                            onPlaySong = { viewModel.playOrToggleSong(it) },
                            onAddSongs = { addSongsTargetPlaylistId = it.id },
                            onAddSongToPlaylist = { song -> pendingSongForPlaylist = song },
                            onDownloadSong = viewModel::downloadSong,
                            onShareSong = viewModel::shareSong,
                            onEditPlaylist = { playlistEditTargetId = it.id },
                            onSharePlaylist = { viewModel.sharePlaylist(it) },
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
                            onShareSong = viewModel::shareSong,
                            onShareCollection = { viewModel.shareArtist(artist) },
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
                            onShareSong = viewModel::shareSong,
                            onShareCollection = { viewModel.shareAlbum(album) },
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
                        onShareSong = viewModel::shareSong,
                        onCollapseBottomBar = { if (!compactBottomBar) compactBottomBar = true },
                        onExpandBottomBar = { if (compactBottomBar) compactBottomBar = false }
                    )
                }

                MusicContentRoute.HomeCollectionDetail -> {
                    openedHomeCollection?.let { collection ->
                        HomeCollectionDetailScreen(
                            modifier = Modifier.padding(paddingValues),
                            collection = collection,
                            songs = uiState.songs,
                            artists = uiState.artists,
                            albums = uiState.albums,
                            onPlaySong = { viewModel.playOrToggleSong(it) },
                            onAddSongToPlaylist = { song -> pendingSongForPlaylist = song },
                            onDownloadSong = viewModel::downloadSong,
                            onShareSong = viewModel::shareSong,
                            onArtistClick = { openArtist(it, personalLibrary = false) },
                            onAlbumClick = { openAlbum(it, personalLibrary = false) },
                            onCollapseBottomBar = { if (!compactBottomBar) compactBottomBar = true },
                            onExpandBottomBar = { if (compactBottomBar) compactBottomBar = false }
                        )
                    }
                }

                MusicContentRoute.Home -> HomeTab(
                    modifier = Modifier.padding(paddingValues),
                    uiState = uiState,
                    authUser = uiState.authUser,
                    onRetry = viewModel::refreshData,
                    onPlaySong = { viewModel.playOrToggleSong(it) },
                    onAddSongToPlaylist = { song -> pendingSongForPlaylist = song },
                    onDownloadSong = viewModel::downloadSong,
                    onShareSong = viewModel::shareSong,
                    onArtistClick = { openArtist(it, personalLibrary = false) },
                    onAlbumClick = { openAlbum(it, personalLibrary = false) },
                    onOpenCollection = { openHomeCollection(it) },
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
                    onShareSong = viewModel::shareSong,
                    onArtistClick = { openArtist(it, personalLibrary = false) },
                    onAlbumClick = { openAlbum(it, personalLibrary = false) },
                    onGenreClick = { openGenre(it, personalLibrary = false) },
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
                    onShareSong = viewModel::shareSong,
                    onArtistClick = { openArtist(it, personalLibrary = false) },
                    onAlbumClick = { openAlbum(it, personalLibrary = false) },
                    onGenreClick = { openGenre(it, personalLibrary = false) },
                    onAccountClick = accountAction,
                    onCollapseBottomBar = { if (!compactBottomBar) compactBottomBar = true },
                    onExpandBottomBar = { if (compactBottomBar) compactBottomBar = false }
                )

                MusicContentRoute.Library -> LibraryTab(
                    modifier = Modifier.padding(paddingValues),
                    activeSection = activeLibrarySection,
                    albums = libraryAlbums,
                    artists = libraryArtists,
                    songs = librarySongs,
                    playlists = uiState.playlists,
                    downloadedSongIds = uiState.downloadedSongIds,
                    authUser = uiState.authUser,
                    onPlaySong = { viewModel.playOrToggleSong(it) },
                    onAddSongToPlaylist = { song -> pendingSongForPlaylist = song },
                    onDownloadSong = viewModel::downloadSong,
                    onShareSong = viewModel::shareSong,
                    onPlaylistClick = { openPlaylist(it) },
                    onArtistClick = { openArtist(it, personalLibrary = true) },
                    onAlbumClick = { openAlbum(it, personalLibrary = true) },
                    onGenreClick = { openGenre(it, personalLibrary = true) },
                    onSectionChange = {
                        activeLibrarySection = it
                        openedPlaylistId = null
                        openedArtistId = null
                        openedAlbumId = null
                        openedGenreName = null
                        openedHomeCollection = null
                        usePersonalLibraryForDetail = false
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
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    onLogin = openAuthenticationActivity,
                    onEditProfile = { showAccountDialog = true },
                    onOpenAdmin = openAdminActivity,
                    onShareProfile = { viewModel.shareCurrentProfile() },
                    onLogout = logoutToGuestMode,
                    onCollapseBottomBar = { if (!compactBottomBar) compactBottomBar = true },
                    onExpandBottomBar = { if (compactBottomBar) compactBottomBar = false }
                )
                    }
                }
            }

            if (!showNowPlaying) {
                AppBottomBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onSizeChanged { bottomBarHeightPx = it.height },
                    selectedTab = selectedTab,
                    compact = compactBottomBar && hasNowPlayingSong,
                    song = nowPlayingSong,
                    isPlaying = uiState.isPlayerRunning,
                    onOpenNowPlaying = { showNowPlaying = true },
                    onPlayPause = viewModel::togglePlayback,
                    onNext = viewModel::playNextSong,
                    onTabSelected = { selectTab(it) },
                    onExpand = { compactBottomBar = false },
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
                    openedHomeCollection = null
                    usePersonalLibraryForDetail = true
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
                openedHomeCollection = null
                usePersonalLibraryForDetail = true
                activeLibrarySection = LibrarySection.Playlists
            }
        )
    }

    uiState.playlists.firstOrNull { it.id == addSongsTargetPlaylistId }?.let { playlist ->
        AddSongsToPlaylistDialog(
            playlist = playlist,
            songs = uiState.songs,
            onDismiss = { addSongsTargetPlaylistId = null },
            onAddSong = { song ->
                viewModel.addSongToPlaylist(song, playlist.id)
                openedPlaylistId = playlist.id
                openedHomeCollection = null
                selectedTab = MusicTab.Library
                activeLibrarySection = LibrarySection.Playlists
                usePersonalLibraryForDetail = true
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
            onLogout = logoutToGuestMode
        )
    }
}
