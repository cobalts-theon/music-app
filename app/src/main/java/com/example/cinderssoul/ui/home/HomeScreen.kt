package com.example.cinderssoul.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cinderssoul.MusicUiState
import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.models.User
import com.example.cinderssoul.ui.app.HomeCollapsedItemLimit
import com.example.cinderssoul.ui.components.AppleMusicPageHeader
import com.example.cinderssoul.ui.components.CoverImage
import com.example.cinderssoul.ui.components.FeaturedSongCard
import com.example.cinderssoul.ui.components.LibraryImageTile

@Composable
internal fun HomeTab(
    modifier: Modifier,
    uiState: MusicUiState,
    authUser: User?,
    onRetry: () -> Unit,
    onPlaySong: (Song) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onDownloadSong: (Song) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onAccountClick: () -> Unit,
    onCollapseBottomBar: () -> Unit,
    onExpandBottomBar: () -> Unit
) {
    val listState = rememberLazyListState()
    var madeForYouExpanded by rememberSaveable { mutableStateOf(false) }
    var artistsExpanded by rememberSaveable { mutableStateOf(false) }
    var albumsExpanded by rememberSaveable { mutableStateOf(false) }
    var recentlyAddedExpanded by rememberSaveable { mutableStateOf(false) }
    var mostPlayedExpanded by rememberSaveable { mutableStateOf(false) }

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
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.errorMessage != null && uiState.songs.isEmpty() -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.ErrorOutline, contentDescription = null)
                    Spacer(Modifier.height(8.dp))
                    Text(text = uiState.errorMessage, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Tap to retry",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onRetry)
                    )
                }
            }
        }
        else -> {
            val featuredSong = uiState.currentSong
                ?: uiState.songs.maxByOrNull { it.playCount }
                ?: uiState.songs.firstOrNull()
            val madeForYouSongs = uiState.songs
            val recentlyAddedSongs = uiState.songs
            val mostPlayedSongs = uiState.songs.sortedByDescending { it.playCount }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 2.dp),
                state = listState,
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    AppleMusicPageHeader(
                        title = "Listen Now",
                        subtitle = "Cinder's Soul",
                        showAccountAction = true,
                        currentUser = authUser,
                        onAccountClick = onAccountClick
                    )
                }
                item {
                    FeaturedSongCard(song = featuredSong, onPlaySong = onPlaySong)
                }
                item {
                    HomeHorizontalCardSection(
                        title = "Made For You",
                        items = madeForYouSongs,
                        expanded = madeForYouExpanded,
                        onToggleExpanded = { madeForYouExpanded = !madeForYouExpanded },
                        key = { index, song -> "home-made-$index-${song.id}" }
                    ) { song ->
                        LibraryImageTile(
                            title = song.title,
                            subtitle = song.artistName,
                            imageUrl = song.coverUrl,
                            onClick = { onPlaySong(song) }
                        )
                    }
                }
                item {
                    HomeHorizontalCardSection(
                        title = "Artists You Follow",
                        items = uiState.artists,
                        expanded = artistsExpanded,
                        onToggleExpanded = { artistsExpanded = !artistsExpanded },
                        key = { index, artist -> "home-artist-$index-${artist.id}" }
                    ) { artist ->
                        val songsCount = uiState.songs.count { it.artistId == artist.id }
                        LibraryImageTile(
                            title = artist.name,
                            subtitle = "$songsCount songs",
                            imageUrl = artist.avatarUrl,
                            onClick = { onArtistClick(artist) }
                        )
                    }
                }
                item {
                    HomeHorizontalCardSection(
                        title = "New Albums",
                        items = uiState.albums,
                        expanded = albumsExpanded,
                        onToggleExpanded = { albumsExpanded = !albumsExpanded },
                        key = { index, album -> "home-album-$index-${album.id}" }
                    ) { album ->
                        val songsCount = uiState.songs.count { it.albumId == album.id }
                        LibraryImageTile(
                            title = album.title,
                            subtitle = "$songsCount songs",
                            imageUrl = album.coverUrl,
                            onClick = { onAlbumClick(album) }
                        )
                    }
                }
                item {
                    HomeSongGridSection(
                        title = "Recently Added",
                        songs = recentlyAddedSongs,
                        expanded = recentlyAddedExpanded,
                        onToggleExpanded = { recentlyAddedExpanded = !recentlyAddedExpanded },
                        keyPrefix = "home-new",
                        onPlaySong = onPlaySong,
                        onAddSongToPlaylist = onAddSongToPlaylist,
                        onDownloadSong = onDownloadSong
                    )
                }
                item {
                    HomeSongGridSection(
                        title = "Most Played",
                        songs = mostPlayedSongs,
                        expanded = mostPlayedExpanded,
                        onToggleExpanded = { mostPlayedExpanded = !mostPlayedExpanded },
                        keyPrefix = "home-most-played",
                        onPlaySong = onPlaySong,
                        onAddSongToPlaylist = onAddSongToPlaylist,
                        onDownloadSong = onDownloadSong
                    )
                }
            }
        }
    }

}

@Composable
internal fun <T> HomeHorizontalCardSection(
    title: String,
    items: List<T>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    key: (Int, T) -> Any,
    itemContent: @Composable (T) -> Unit
) {
    if (items.isEmpty()) return

    val rowState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = rowState)
    val visibleItems = if (expanded) items else items.take(HomeCollapsedItemLimit)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HomeSectionHeader(
            title = title,
            itemCount = items.size,
            expanded = expanded,
            onToggleExpanded = onToggleExpanded
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            state = rowState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(
                visibleItems,
                key = { index, item -> key(index, item) }
            ) { _, item ->
                itemContent(item)
            }
        }
    }

}

@Composable
internal fun HomeSongGridSection(
    title: String,
    songs: List<Song>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    keyPrefix: String,
    onPlaySong: (Song) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onDownloadSong: (Song) -> Unit
) {
    if (songs.isEmpty()) return

    val gridState = rememberLazyGridState()
    val flingBehavior = rememberSnapFlingBehavior(lazyGridState = gridState)
    val visibleSongs = if (expanded) songs else songs.take(HomeCollapsedItemLimit)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HomeSectionHeader(
            title = title,
            itemCount = songs.size,
            expanded = expanded,
            onToggleExpanded = onToggleExpanded
        )
        LazyHorizontalGrid(
            rows = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(304.dp),
            state = gridState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            gridItemsIndexed(
                visibleSongs,
                key = { index, song -> "$keyPrefix-$index-${song.id}" }
            ) { _, song ->
                HomeSongGridTile(
                    song = song,
                    onClick = { onPlaySong(song) },
                    onPlay = { onPlaySong(song) },
                    onAddToPlaylist = { onAddSongToPlaylist(song) },
                    onDownload = { onDownloadSong(song) }
                )
            }
        }
    }
}

@Composable
internal fun HomeSectionHeader(
    title: String,
    itemCount: Int,
    expanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )
        if (itemCount > HomeCollapsedItemLimit) {
            TextButton(onClick = onToggleExpanded) {
                Text(if (expanded) "Less" else "See All")
            }
        }
    }
}

@Composable
internal fun HomeSongGridTile(
    song: Song,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDownload: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .width(304.dp)
            .height(70.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(imageUrl = song.coverUrl, title = song.title, modifier = Modifier.size(52.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artistName} • ${song.albumTitle}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box {
            IconButton(onClick = { showActions = true }, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Actions",
                    modifier = Modifier.size(24.dp)
                )
            }
            DropdownMenu(expanded = showActions, onDismissRequest = { showActions = false }) {
                DropdownMenuItem(
                    text = { Text("Play") },
                    onClick = {
                        showActions = false
                        onPlay()
                    },
                    leadingIcon = { Icon(Icons.Rounded.PlayArrow, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Add to playlist") },
                    onClick = {
                        showActions = false
                        onAddToPlaylist()
                    },
                    leadingIcon = { Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Download") },
                    onClick = {
                        showActions = false
                        onDownload()
                    },
                    leadingIcon = { Icon(Icons.Rounded.DownloadDone, contentDescription = null) }
                )
            }
        }
    }
}
