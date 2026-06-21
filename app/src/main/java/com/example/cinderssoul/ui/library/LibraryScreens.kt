package com.example.cinderssoul.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Playlist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.models.User
import com.example.cinderssoul.ui.app.AppleMusicRed
import com.example.cinderssoul.ui.app.LibrarySection
import com.example.cinderssoul.ui.app.LocalBottomBarContentPadding
import com.example.cinderssoul.ui.app.RecentLibraryItem
import com.example.cinderssoul.ui.components.AppleMusicPageHeader
import com.example.cinderssoul.ui.components.CoverImage
import com.example.cinderssoul.ui.components.LibraryCommandRow
import com.example.cinderssoul.ui.components.LibraryMediaRow
import com.example.cinderssoul.ui.components.LibrarySectionsBlock
import com.example.cinderssoul.ui.components.PlaylistEntryRow
import com.example.cinderssoul.ui.components.RecentAddedGridTile
import com.example.cinderssoul.ui.components.SectionTitle
import com.example.cinderssoul.ui.components.SongRow
import com.example.cinderssoul.ui.components.itemCountText

private val LibraryTextFieldShape = RoundedCornerShape(28.dp)

private enum class PlaylistSortMode(val label: String) {
    Custom("Custom"),
    Title("Title"),
    Artist("Artist"),
    Duration("Duration")
}

@Composable
internal fun LibraryTab(
    modifier: Modifier,
    activeSection: LibrarySection,
    albums: List<Album>,
    artists: List<Artist>,
    songs: List<Song>,
    playlists: List<Playlist>,
    downloadedSongIds: Set<Int>,
    authUser: User?,
    onPlaySong: (Song) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onDownloadSong: (Song) -> Unit,
    onShareSong: (Song) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onGenreClick: (String) -> Unit,
    onSectionChange: (LibrarySection) -> Unit,
    onCreatePlaylist: () -> Unit,
    onAccountClick: () -> Unit,
    onCollapseBottomBar: () -> Unit,
    onExpandBottomBar: () -> Unit
) {
    val listState = rememberLazyListState()
    val bottomContentPadding = LocalBottomBarContentPadding.current + 15.dp
    LaunchedEffect(listState) {
        var lastIndex = 0
        var lastOffset = 0
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val down = index > lastIndex || (index == lastIndex && offset > lastOffset + 3)
                val up = index < lastIndex || (index == lastIndex && offset + 6 < lastOffset)
                val enoughOffset = index > 0 || offset > 96
                if (down && enoughOffset) onCollapseBottomBar()
                val nearTop = index == 0 && offset <= 20
                if (nearTop && (up || lastIndex > 0 || lastOffset > 40)) onExpandBottomBar()
                lastIndex = index
                lastOffset = offset
            }
    }
    LaunchedEffect(activeSection) {
        listState.scrollToItem(0)
    }

    val recentAlbums = albums.take(8)
    val recentPlaylists = playlists.take(8)
    val recentAdded = buildList<RecentLibraryItem> {
        val maxSize = maxOf(recentAlbums.size, recentPlaylists.size)
        for (index in 0 until maxSize) {
            recentPlaylists.getOrNull(index)?.let { add(RecentLibraryItem.PlaylistItem(it)) }
            recentAlbums.getOrNull(index)?.let { add(RecentLibraryItem.AlbumItem(it)) }
        }
    }.take(10)
    val recentRows = recentAdded.chunked(2)
    val genreGroups = songs
        .mapNotNull { it.genre?.trim()?.takeIf { value -> value.isNotBlank() } }
        .groupingBy { it }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
    val downloadedSongs = songs.filter {
        it.id in downloadedSongIds || it.audioUrl.startsWith("file://") || it.audioUrl.startsWith("content://")
    }

    LazyColumn(
        modifier = modifier
            .then(if (activeSection == LibrarySection.Overview) Modifier.statusBarsPadding() else Modifier)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                horizontal = 20.dp,
                vertical = if (activeSection == LibrarySection.Overview) 2.dp else 10.dp
        ),
        state = listState,
        contentPadding = PaddingValues(bottom = bottomContentPadding),
        verticalArrangement = if (activeSection == LibrarySection.Overview) {
            Arrangement.spacedBy(18.dp)
        } else {
            Arrangement.spacedBy(2.dp)
        }
    ) {
        if (activeSection == LibrarySection.Overview) {
            item {
                AppleMusicPageHeader(
                    title = "Library",
                    subtitle = "${songs.size} songs - ${playlists.size} playlists",
                    addHorizontalPadding = false,
                    showAccountAction = true,
                    currentUser = authUser,
                    onAccountClick = onAccountClick
                )
                Spacer(Modifier.height(20.dp))
                LibrarySectionsBlock(
                    activeSection = activeSection,
                    playlistsCount = playlists.size,
                    artistsCount = artists.size,
                    albumsCount = albums.size,
                    songsCount = songs.size,
                    genresCount = genreGroups.size,
                    downloadedCount = downloadedSongs.size,
                    onSectionChange = onSectionChange
                )
            }
        }

        when (activeSection) {
            LibrarySection.Overview -> {
                item { SectionTitle("Recently Added", addHorizontalPadding = false) }
                if (recentRows.isEmpty()) {
                    item {
                        Text(
                            text = "No recent items",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    itemsIndexed(
                        recentRows,
                        key = { index, row -> "overview-recent-row-$index-${row.joinToString("-") { it.stableKey }}" }
                    ) { _, row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { item ->
                                RecentAddedGridTile(
                                    item = item,
                                    onPlaylistClick = onPlaylistClick,
                                    onAlbumClick = onAlbumClick
                                )
                            }
                            if (row.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            LibrarySection.Playlists -> {
                item {
                    LibraryCommandRow(
                        title = "New Playlist...",
                        icon = Icons.Rounded.Add,
                        onClick = onCreatePlaylist
                    )
                }

                if (playlists.isEmpty()) {
                    item {
                        Text(
                            text = "No playlists available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    itemsIndexed(playlists, key = { index, playlist -> "playlist-$index-${playlist.id}" }) { _, playlist ->
                        PlaylistEntryRow(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) }
                        )
                    }
                }
            }

            LibrarySection.Artists -> {
                if (artists.isEmpty()) {
                    item {
                        Text(
                            text = "No artists available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    itemsIndexed(artists, key = { index, artist -> "artist-$index-${artist.id}" }) { _, artist ->
                        val artistSongs = songs.filter { it.artistId == artist.id }
                        LibraryMediaRow(
                            title = artist.name,
                            subtitle = itemCountText(artistSongs.size, "song"),
                            imageUrl = artist.avatarUrl,
                            fallbackIcon = Icons.Rounded.AccountCircle,
                            circleArtwork = true,
                            onClick = { onArtistClick(artist) }
                        )
                    }
                }
            }

            LibrarySection.Albums -> {
                if (albums.isEmpty()) {
                    item {
                        Text(
                            text = "No albums available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    itemsIndexed(albums, key = { index, album -> "album-$index-${album.id}" }) { _, album ->
                        LibraryMediaRow(
                            title = album.title,
                            subtitle = album.artist?.name ?: "Unknown artist",
                            imageUrl = album.coverUrl,
                            fallbackIcon = Icons.Rounded.Album,
                            onClick = { onAlbumClick(album) }
                        )
                    }
                }
            }

            LibrarySection.Songs -> {
                itemsIndexed(songs, key = { index, song -> "library-song-$index-${song.id}" }) { _, song ->
                    SongRow(
                        song = song,
                        onClick = { onPlaySong(song) },
                        onPlay = { onPlaySong(song) },
                        onAddToPlaylist = { onAddSongToPlaylist(song) },
                        onDownload = { onDownloadSong(song) },
                        onShare = { onShareSong(song) },
                        showBackground = false
                    )
                }
            }

            LibrarySection.Genres -> {
                if (genreGroups.isEmpty()) {
                    item {
                        Text(
                            text = "No genres available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    itemsIndexed(genreGroups, key = { index, item -> "genre-$index-${item.first}" }) { _, item ->
                        val (genre, count) = item
                        LibraryMediaRow(
                            title = genre,
                            subtitle = itemCountText(count, "song"),
                            imageUrl = null,
                            fallbackIcon = Icons.Rounded.Category,
                            onClick = { onGenreClick(genre) }
                        )
                    }
                }
            }

            LibrarySection.Downloaded -> {
                if (downloadedSongs.isEmpty()) {
                    item {
                        Text(
                            text = "No downloaded songs yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    itemsIndexed(
                        downloadedSongs,
                        key = { index, song -> "downloaded-song-$index-${song.id}" }
                    ) { _, song ->
                        SongRow(
                            song = song,
                            onClick = { onPlaySong(song) },
                            onPlay = { onPlaySong(song) },
                            onAddToPlaylist = { onAddSongToPlaylist(song) },
                            onDownload = { onDownloadSong(song) },
                            onShare = { onShareSong(song) },
                            showBackground = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PlaylistDetailScreen(
    modifier: Modifier = Modifier,
    playlist: Playlist,
    onPlaySong: (Song) -> Unit,
    onAddSongs: (Playlist) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onDownloadSong: (Song) -> Unit,
    onShareSong: (Song) -> Unit,
    onEditPlaylist: (Playlist) -> Unit,
    onSharePlaylist: (Playlist) -> Unit,
    onDownloadPlaylist: (Playlist) -> Int,
    onDeleteSong: (Song) -> Unit = {},
    onDeletePlaylist: (Playlist) -> Unit = {},
    onCollapseBottomBar: () -> Unit,
    onExpandBottomBar: () -> Unit
) {
    val listState = rememberLazyListState()
    val bottomContentPadding = LocalBottomBarContentPadding.current + 15.dp
    var query by rememberSaveable(playlist.id) { mutableStateOf("") }
    var sortMode by rememberSaveable(playlist.id) { mutableStateOf(PlaylistSortMode.Custom) }
    val normalizedQuery = query.trim().lowercase()
    val filteredSongs = if (normalizedQuery.isBlank()) {
        playlist.songs
    } else {
        playlist.songs.filter { song ->
            song.title.lowercase().contains(normalizedQuery) ||
                song.artistName.lowercase().contains(normalizedQuery) ||
                song.albumTitle.lowercase().contains(normalizedQuery) ||
                song.genre.orEmpty().lowercase().contains(normalizedQuery)
        }
    }
    val visibleSongs = when (sortMode) {
        PlaylistSortMode.Custom -> filteredSongs
        PlaylistSortMode.Title -> filteredSongs.sortedBy { it.title.lowercase() }
        PlaylistSortMode.Artist -> filteredSongs.sortedWith(compareBy<Song> { it.artistName.lowercase() }.thenBy { it.title.lowercase() })
        PlaylistSortMode.Duration -> filteredSongs.sortedBy { it.duration }
    }
    LaunchedEffect(listState) {
        var lastIndex = 0
        var lastOffset = 0
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val down = index > lastIndex || (index == lastIndex && offset > lastOffset + 3)
                val up = index < lastIndex || (index == lastIndex && offset + 6 < lastOffset)
                val enoughOffset = index > 0 || offset > 96
                if (down && enoughOffset) onCollapseBottomBar()
                val nearTop = index == 0 && offset <= 20
                if (nearTop && (up || lastIndex > 0 || lastOffset > 40)) onExpandBottomBar()
                lastIndex = index
                lastOffset = offset
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        state = listState,
        contentPadding = PaddingValues(bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            val subtitle = buildString {
                append(itemCountText(playlist.songs.size, "song"))
                playlist.description
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { append(" - ").append(it) }
            }
            LibraryDetailHeader(
                coverUrl = playlist.coverUrl,
                title = playlist.name,
                subtitle = subtitle,
                playEnabled = playlist.songs.isNotEmpty(),
                onPlayClick = { playlist.songs.firstOrNull()?.let(onPlaySong) }
            ) {
                if (playlist.songs.isNotEmpty()) {
                    LibraryCircularAction(
                        icon = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle playlist",
                        onClick = { playlist.songs.shuffled().firstOrNull()?.let(onPlaySong) }
                    )
                }
                LibraryCircularAction(
                    icon = Icons.Rounded.Add,
                    contentDescription = "Add songs",
                    onClick = { onAddSongs(playlist) }
                )
                var showPlaylistActions by rememberSaveable(playlist.id) { mutableStateOf(false) }
                Box {
                    LibraryCircularAction(
                        icon = Icons.Rounded.MoreVert,
                        contentDescription = "More playlist actions",
                        onClick = { showPlaylistActions = true }
                    )
                    DropdownMenu(
                        expanded = showPlaylistActions,
                        onDismissRequest = { showPlaylistActions = false }
                    ) {
                        if (playlist.id > 0) {
                            DropdownMenuItem(
                                text = { Text("Edit playlist")},
                                onClick = { onEditPlaylist(playlist) }           ,
                                leadingIcon = {
                                    Icon(Icons.Rounded.Edit, contentDescription = null)
                                }
//                                icon = Icons.Rounded.Edit,
//                                contentDescription = "Edit playlist",

                            )
                        }
                        if (playlist.id > 0 && playlist.isPublic) {
                            DropdownMenuItem(
                                text = { Text("Share playlist") },
                                onClick = {
                                    showPlaylistActions = false
                                    onSharePlaylist(playlist)
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Share, contentDescription = null)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Download playlist") },
                            onClick = {
                                showPlaylistActions = false
                                onDownloadPlaylist(playlist)
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.DownloadDone, contentDescription = null)
                            }
                        )
                        if (playlist.id > 0) {
                            DropdownMenuItem(
                                text = { Text("Delete playlist") },
                                onClick = {
                                    showPlaylistActions = false
                                    onDeletePlaylist(playlist)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = null,
                                        tint = AppleMusicRed
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        if (playlist.songs.isEmpty()) {
            item {
                Text(
                    text = "This playlist has no songs yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            item {
                PlaylistDetailTools(
                    query = query,
                    onQueryChange = { query = it },
                    sortMode = sortMode,
                    onSortModeChange = { sortMode = it },
                    totalCount = playlist.songs.size,
                    visibleCount = visibleSongs.size
                )
            }

            if (visibleSongs.isEmpty()) {
                item {
                    Text(
                        text = "No songs match your search.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            itemsIndexed(
                visibleSongs,
                key = { index, song -> "playlist-song-${playlist.id}-$index-${song.id}" }
            ) { _, song ->
                SongRow(
                    song = song,
                    onClick = { onPlaySong(song) },
                    onPlay = { onPlaySong(song) },
                    onAddToPlaylist = { onAddSongToPlaylist(song) },
                    onDownload = { onDownloadSong(song) },
                    onShare = { onShareSong(song) },
                    onDelete = { onDeleteSong(song) },
                    showBackground = false
                )
            }
        }
    }
}

@Composable
private fun PlaylistDetailTools(
    query: String,
    onQueryChange: (String) -> Unit,
    sortMode: PlaylistSortMode,
    onSortModeChange: (PlaylistSortMode) -> Unit,
    totalCount: Int,
    visibleCount: Int
) {
    var showSortMenu by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = { Text("Search in playlist") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear playlist search"
                        )
                    }
                }
            },
            shape = LibraryTextFieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = AppleMusicRed
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (visibleCount == totalCount) {
                    itemCountText(totalCount, "song")
                } else {
                    "$visibleCount of ${itemCountText(totalCount, "song")}"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { showSortMenu = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Sort,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(sortMode.label)
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    PlaylistSortMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.label) },
                            onClick = {
                                onSortModeChange(mode)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SongCollectionDetailScreen(
    modifier: Modifier = Modifier,
    coverUrl: String?,
    title: String,
    subtitle: String,
    songs: List<Song>,
    emptyText: String,
    onPlaySong: (Song) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onDownloadSong: (Song) -> Unit,
    onShareSong: (Song) -> Unit,
    onShareCollection: (() -> Unit)? = null,
    onCollapseBottomBar: () -> Unit,
    onExpandBottomBar: () -> Unit
) {
    val listState = rememberLazyListState()
    val bottomContentPadding = LocalBottomBarContentPadding.current + 15.dp
    LaunchedEffect(listState) {
        var lastIndex = 0
        var lastOffset = 0
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val down = index > lastIndex || (index == lastIndex && offset > lastOffset + 3)
                val up = index < lastIndex || (index == lastIndex && offset + 6 < lastOffset)
                val enoughOffset = index > 0 || offset > 96
                if (down && enoughOffset) onCollapseBottomBar()
                val nearTop = index == 0 && offset <= 20
                if (nearTop && (up || lastIndex > 0 || lastOffset > 40)) onExpandBottomBar()
                lastIndex = index
                lastOffset = offset
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        state = listState,
        contentPadding = PaddingValues(bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            LibraryDetailHeader(
                coverUrl = coverUrl,
                title = title,
                subtitle = subtitle,
                playEnabled = songs.isNotEmpty(),
                onPlayClick = { songs.firstOrNull()?.let(onPlaySong) }
            ) {
                if (onShareCollection != null) {
                    LibraryCircularAction(
                        icon = Icons.Rounded.Share,
                        contentDescription = "Share",
                        onClick = onShareCollection
                    )
                }
            }
        }

        if (songs.isEmpty()) {
            item {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            itemsIndexed(
                songs,
                key = { index, song -> "collection-song-$title-$index-${song.id}" }
            ) { _, song ->
                SongRow(
                    song = song,
                    onClick = { onPlaySong(song) },
                    onPlay = { onPlaySong(song) },
                    onAddToPlaylist = { onAddSongToPlaylist(song) },
                    onDownload = { onDownloadSong(song) },
                    onShare = { onShareSong(song) },
                    showBackground = false
                )
            }
        }
    }
}

@Composable
internal fun LibraryDetailHeader(
    coverUrl: String?,
    title: String,
    subtitle: String,
    playEnabled: Boolean,
    onPlayClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppleMusicRed.copy(alpha = 0.30f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CoverImage(
            imageUrl = coverUrl,
            title = title,
            modifier = Modifier.size(218.dp)
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = title,
            fontSize = 28.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LibraryFilledAction(
                modifier = Modifier.weight(1f),
                enabled = playEnabled,
                onClick = onPlayClick
            )
            actions()
        }
    }
}

@Composable
internal fun LibraryFilledAction(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (enabled) AppleMusicRed else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
    val foregroundColor = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .height(44.dp)
            .clip(MaterialTheme.shapes.large)
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = null,
            tint = foregroundColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Play",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = foregroundColor
        )
    }
}

@Composable
internal fun LibraryCircularAction(
    icon: ImageVector,
    contentDescription: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f), CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isDestructive) AppleMusicRed else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
        )
    }
}
