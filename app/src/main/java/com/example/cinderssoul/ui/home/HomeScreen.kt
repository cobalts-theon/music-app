package com.example.cinderssoul.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapPosition
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
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cinderssoul.MusicUiState
import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.models.User
import com.example.cinderssoul.ui.app.HomeCollection
import com.example.cinderssoul.ui.app.HomeCollapsedItemLimit
import com.example.cinderssoul.ui.app.HomeSongGridCollapsedItemLimit
import com.example.cinderssoul.ui.app.HomeSongGridRows
import com.example.cinderssoul.ui.app.LocalBottomBarContentPadding
import com.example.cinderssoul.ui.components.AppleMusicPageHeader
import com.example.cinderssoul.ui.components.CoverImage
import com.example.cinderssoul.ui.components.FeaturedSongCard
import com.example.cinderssoul.ui.components.LibraryImageTile
import com.example.cinderssoul.ui.components.LibraryMediaRow
import com.example.cinderssoul.ui.components.SongRow
import com.example.cinderssoul.ui.components.itemCountText

private val HomeGridTileBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))

@Composable
internal fun HomeTab(
    modifier: Modifier,
    uiState: MusicUiState,
    authUser: User?,
    onRetry: () -> Unit,
    onPlaySong: (Song) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onDownloadSong: (Song) -> Unit,
    onShareSong: (Song) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onOpenCollection: (HomeCollection) -> Unit,
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
                contentPadding = PaddingValues(bottom = bottomContentPadding),
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
                        onSeeAll = { onOpenCollection(HomeCollection.MadeForYou) },
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
                        onSeeAll = { onOpenCollection(HomeCollection.Artists) },
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
                        onSeeAll = { onOpenCollection(HomeCollection.Albums) },
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
                        onSeeAll = { onOpenCollection(HomeCollection.RecentlyAdded) },
                        keyPrefix = "home-new",
                        onPlaySong = onPlaySong,
                        onAddSongToPlaylist = onAddSongToPlaylist,
                        onDownloadSong = onDownloadSong,
                        onShareSong = onShareSong
                    )
                }
                item {
                    HomeSongGridSection(
                        title = "Most Played",
                        songs = mostPlayedSongs,
                        onSeeAll = { onOpenCollection(HomeCollection.MostPlayed) },
                        keyPrefix = "home-most-played",
                        onPlaySong = onPlaySong,
                        onAddSongToPlaylist = onAddSongToPlaylist,
                        onDownloadSong = onDownloadSong,
                        onShareSong = onShareSong
                    )
                }
            }
        }
    }

}

@Composable
internal fun HomeCollectionDetailScreen(
    modifier: Modifier,
    collection: HomeCollection,
    songs: List<Song>,
    artists: List<Artist>,
    albums: List<Album>,
    onPlaySong: (Song) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onDownloadSong: (Song) -> Unit,
    onShareSong: (Song) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onCollapseBottomBar: () -> Unit,
    onExpandBottomBar: () -> Unit
) {
    val listState = rememberLazyListState()
    val bottomContentPadding = LocalBottomBarContentPadding.current + 15.dp
    val collectionSongs = when (collection) {
        HomeCollection.MadeForYou,
        HomeCollection.RecentlyAdded -> songs
        HomeCollection.MostPlayed -> songs.sortedByDescending { it.playCount }
        HomeCollection.Artists,
        HomeCollection.Albums -> emptyList()
    }
    val itemCount = when (collection) {
        HomeCollection.Artists -> artists.size
        HomeCollection.Albums -> albums.size
        else -> collectionSongs.size
    }
    val itemName = when (collection) {
        HomeCollection.Artists -> "artist"
        HomeCollection.Albums -> "album"
        else -> "song"
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
            AppleMusicPageHeader(
                title = collection.title,
                subtitle = itemCountText(itemCount, itemName)
            )
        }

        when (collection) {
            HomeCollection.Artists -> {
                if (artists.isEmpty()) {
                    item {
                        Text(
                            text = "No artists available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    itemsIndexed(
                        artists,
                        key = { index, artist -> "home-detail-artist-$index-${artist.id}" }
                    ) { _, artist ->
                        val songsCount = songs.count { it.artistId == artist.id }
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            LibraryMediaRow(
                                title = artist.name,
                                subtitle = itemCountText(songsCount, "song"),
                                imageUrl = artist.avatarUrl,
                                fallbackIcon = Icons.Rounded.AccountCircle,
                                circleArtwork = true,
                                onClick = { onArtistClick(artist) }
                            )
                        }
                    }
                }
            }

            HomeCollection.Albums -> {
                if (albums.isEmpty()) {
                    item {
                        Text(
                            text = "No albums available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    itemsIndexed(
                        albums,
                        key = { index, album -> "home-detail-album-$index-${album.id}" }
                    ) { _, album ->
                        val songsCount = songs.count { it.albumId == album.id }
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            LibraryMediaRow(
                                title = album.title,
                                subtitle = album.artist?.name ?: itemCountText(songsCount, "song"),
                                imageUrl = album.coverUrl,
                                fallbackIcon = Icons.Rounded.Album,
                                onClick = { onAlbumClick(album) }
                            )
                        }
                    }
                }
            }

            else -> {
                if (collectionSongs.isEmpty()) {
                    item {
                        Text(
                            text = "No songs available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    itemsIndexed(
                        collectionSongs,
                        key = { index, song -> "home-detail-song-${collection.name}-$index-${song.id}" }
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
internal fun <T> HomeHorizontalCardSection(
    title: String,
    items: List<T>,
    onSeeAll: (() -> Unit)? = null,
    key: (Int, T) -> Any,
    itemContent: @Composable (T) -> Unit
) {
    if (items.isEmpty()) return

    val rowState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(
        lazyListState = rowState,
        snapPosition = SnapPosition.Start
    )
    val visibleItems = if (onSeeAll == null) items else items.take(HomeCollapsedItemLimit)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HomeSectionHeader(
            title = title,
            itemCount = items.size,
            collapsedItemLimit = HomeCollapsedItemLimit,
            onSeeAll = onSeeAll
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
    onSeeAll: (() -> Unit)? = null,
    keyPrefix: String,
    onPlaySong: (Song) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onDownloadSong: (Song) -> Unit,
    onShareSong: (Song) -> Unit
) {
    if (songs.isEmpty()) return

    val gridState = rememberLazyGridState()
    val flingBehavior = rememberSnapFlingBehavior(
        lazyGridState = gridState,
        snapPosition = SnapPosition.Start
    )
    val visibleSongs = if (onSeeAll == null) songs else songs.take(HomeSongGridCollapsedItemLimit)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HomeSectionHeader(
            title = title,
            itemCount = songs.size,
            collapsedItemLimit = HomeSongGridCollapsedItemLimit,
            onSeeAll = onSeeAll
        )
        LazyHorizontalGrid(
            rows = GridCells.Fixed(HomeSongGridRows),
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
                    onDownload = { onDownloadSong(song) },
                    onShare = { onShareSong(song) }
                )
            }
        }
    }
}

@Composable
internal fun HomeSectionHeader(
    title: String,
    itemCount: Int,
    collapsedItemLimit: Int,
    onSeeAll: (() -> Unit)?
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
        if (onSeeAll != null && itemCount > collapsedItemLimit) {
            TextButton(onClick = onSeeAll) {
                Text("See All")
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
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .width(304.dp)
            .height(70.dp)
            .clip(MaterialTheme.shapes.small)
            .border(HomeGridTileBorder, MaterialTheme.shapes.small)
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
                DropdownMenuItem(
                    text = { Text("Share") },
                    onClick = {
                        showActions = false
                        onShare()
                    },
                    leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = null) }
                )
            }
        }
    }
}
