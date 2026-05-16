package com.example.cinderssoul.ui.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cinderssoul.ui.components.AppleMusicPageHeader
import com.example.cinderssoul.ui.components.BrowseCategoryTile
import com.example.cinderssoul.ui.components.CircleCoverImage
import com.example.cinderssoul.ui.components.DiscoverGenreTile
import com.example.cinderssoul.ui.home.HomeHorizontalCardSection
import com.example.cinderssoul.ui.home.HomeSongGridSection
import com.example.cinderssoul.ui.components.LibraryEntryRow
import com.example.cinderssoul.ui.components.LibraryImageTile
import com.example.cinderssoul.ui.components.LibraryMediaRow
import com.example.cinderssoul.ui.components.SearchArtistResultRow
import com.example.cinderssoul.ui.components.SearchEmptyState
import com.example.cinderssoul.ui.components.SectionTitle
import com.example.cinderssoul.ui.components.SongRow
import com.example.cinderssoul.ui.components.itemCountText
import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.models.User
import com.example.cinderssoul.ui.app.AppleMusicRed
import com.example.cinderssoul.ui.app.HomeCollapsedItemLimit

@Composable
internal fun SearchTab(
    modifier: Modifier,
    query: String,
    songs: List<Song>,
    artists: List<Artist>,
    albums: List<Album>,
    authUser: User?,
    onQueryChange: (String) -> Unit,
    onPlaySong: (Song) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onDownloadSong: (Song) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onGenreClick: (String) -> Unit,
    onAccountClick: () -> Unit,
    onCollapseBottomBar: () -> Unit,
    onExpandBottomBar: () -> Unit
) {
    val listState = rememberLazyListState()
    val trimmedQuery = query.trim()
    val normalizedQuery = trimmedQuery.lowercase()
    val genreGroups = songs
        .mapNotNull { song -> song.genre?.trim()?.takeIf { it.isNotBlank() } }
        .groupingBy { it }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
    val browseCategories = genreGroups
        .ifEmpty { listOf("Songs" to songs.size) }
        .take(8)
        .chunked(2)
    val matchedArtists = if (trimmedQuery.isBlank()) {
        emptyList()
    } else {
        artists.filter { artist ->
            artist.name.lowercase().contains(normalizedQuery) ||
                artist.bio.orEmpty().lowercase().contains(normalizedQuery)
        }
    }
    val matchedArtistIds = matchedArtists.map { it.id }.toSet()
    val matchedAlbums = if (trimmedQuery.isBlank()) {
        emptyList()
    } else {
        albums.filter { album ->
            album.title.lowercase().contains(normalizedQuery) ||
                album.artist?.name.orEmpty().lowercase().contains(normalizedQuery) ||
                album.artistId in matchedArtistIds
        }
    }
    val matchedAlbumIds = matchedAlbums.map { it.id }.toSet()
    val matchingGenres = if (trimmedQuery.isBlank()) {
        emptyList()
    } else {
        genreGroups.filter { (genre, _) -> genre.lowercase().contains(normalizedQuery) }
    }
    val matchedSongs = if (trimmedQuery.isBlank()) {
        songs
    } else {
        songs.filter { song ->
            song.title.lowercase().contains(normalizedQuery) ||
                song.artistName.lowercase().contains(normalizedQuery) ||
                song.albumTitle.lowercase().contains(normalizedQuery) ||
                song.genre.orEmpty().lowercase().contains(normalizedQuery) ||
                song.lyrics.orEmpty().lowercase().contains(normalizedQuery) ||
                song.artistId in matchedArtistIds ||
                song.albumId in matchedAlbumIds
        }
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        AppleMusicPageHeader(
            title = "Search",
            subtitle = "Find songs, albums, and artists",
            addHorizontalPadding = false,
            showAccountAction = true,
            currentUser = authUser,
            onAccountClick = onAccountClick
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Artists, songs, lyrics, and more") },
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
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(Modifier.height(18.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (trimmedQuery.isBlank()) {
                item { SectionTitle("Browse Categories", addHorizontalPadding = false) }
                itemsIndexed(
                    browseCategories,
                    key = { rowIndex, row -> "search-category-row-$rowIndex-${row.joinToString("-") { it.first }}" }
                ) { rowIndex, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEachIndexed { index, category ->
                            BrowseCategoryTile(
                                title = category.first,
                                subtitle = "${category.second} songs",
                                colorIndex = index + rowIndex * 2,
                                onClick = { onQueryChange(category.first) }
                            )
                        }
                        if (row.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
                item { SectionTitle("Recently Added", addHorizontalPadding = false) }
                itemsIndexed(
                    matchedSongs.take(10),
                    key = { index, song -> "search-recent-$index-${song.id}" }
                ) { _, song ->
                    SongRow(
                        song = song,
                        onClick = { onPlaySong(song) },
                        onPlay = { onPlaySong(song) },
                        onAddToPlaylist = { onAddSongToPlaylist(song) },
                        onDownload = { onDownloadSong(song) },
                        showBackground = false
                    )
                }
            } else {
                if (matchedArtists.isEmpty() && matchedSongs.isEmpty() && matchedAlbums.isEmpty() && matchingGenres.isEmpty()) {
                    item {
                        SearchEmptyState(query = trimmedQuery)
                    }
                } else {
                    if (matchedArtists.isNotEmpty()) {
                        item { SectionTitle("Artists", addHorizontalPadding = false) }
                        itemsIndexed(
                            matchedArtists,
                            key = { index, artist -> "search-artist-$index-${artist.id}" }
                        ) { _, artist ->
                            val songsCount = songs.count { it.artistId == artist.id }
                            SearchArtistResultRow(
                                artist = artist,
                                subtitle = itemCountText(songsCount, "song"),
                                onClick = { onArtistClick(artist) }
                            )
                        }
                    }

                    if (matchingGenres.isNotEmpty()) {
                        item { SectionTitle("Genres", addHorizontalPadding = false) }
                        itemsIndexed(
                            matchingGenres,
                            key = { index, item -> "search-genre-$index-${item.first}" }
                        ) { index, (genre, count) ->
                            DiscoverGenreTile(
                                modifier = Modifier.fillMaxWidth(),
                                title = genre,
                                subtitle = itemCountText(count, "song"),
                                colorIndex = index,
                                onClick = { onGenreClick(genre) }
                            )
                        }
                    }

                    if (matchedSongs.isNotEmpty()) {
                        item { SectionTitle("Songs", addHorizontalPadding = false) }
                        itemsIndexed(
                            matchedSongs,
                            key = { index, song -> "search-result-$index-${song.id}" }
                        ) { _, song ->
                            SongRow(
                                song = song,
                                onClick = { onPlaySong(song) },
                                onPlay = { onPlaySong(song) },
                                onAddToPlaylist = { onAddSongToPlaylist(song) },
                                onDownload = { onDownloadSong(song) },
                                showBackground = false
                            )
                        }
                    }

                    if (matchedAlbums.isNotEmpty()) {
                        item { SectionTitle("Albums", addHorizontalPadding = false) }
                        itemsIndexed(
                            matchedAlbums,
                            key = { index, album -> "search-album-$index-${album.id}" }
                        ) { _, album ->
                            LibraryMediaRow(
                                title = album.title,
                                subtitle = album.artist?.name ?: "Album",
                                imageUrl = album.coverUrl,
                                fallbackIcon = Icons.Rounded.Album,
                                onClick = { onAlbumClick(album) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DiscoverTab(
    modifier: Modifier,
    songs: List<Song>,
    artists: List<Artist>,
    albums: List<Album>,
    authUser: User?,
    onPlaySong: (Song) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onDownloadSong: (Song) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onGenreClick: (String) -> Unit,
    onAccountClick: () -> Unit,
    onCollapseBottomBar: () -> Unit,
    onExpandBottomBar: () -> Unit
) {
    val listState = rememberLazyListState()
    val genreGroups = songs
        .mapNotNull { it.genre?.trim()?.takeIf { value -> value.isNotBlank() } }
        .groupingBy { it }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
    val topArtists = artists.sortedByDescending { artist -> songs.count { it.artistId == artist.id } }
    val freshSongs = songs.take(24)

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
            .statusBarsPadding()
            .background(Color.Black)
            .padding(top = 2.dp),
        state = listState,
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            AppleMusicPageHeader(
                title = "Discover",
                subtitle = "Explore albums, artists, and genres",
                showAccountAction = true,
                currentUser = authUser,
                onAccountClick = onAccountClick
            )
        }

        if (genreGroups.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Genres")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            genreGroups.take(12),
                            key = { index, item -> "discover-genre-$index-${item.first}" }
                        ) { index, (genre, count) ->
                            DiscoverGenreTile(
                                modifier = Modifier.width(154.dp),
                                title = genre,
                                subtitle = itemCountText(count, "song"),
                                colorIndex = index,
                                onClick = { onGenreClick(genre) }
                            )
                        }
                    }
                }
            }
        }

        item {
            HomeHorizontalCardSection(
                title = "Albums",
                items = albums.take(HomeCollapsedItemLimit),
                expanded = true,
                onToggleExpanded = {},
                key = { index, album -> "discover-album-$index-${album.id}" }
            ) { album ->
                val songsCount = songs.count { it.albumId == album.id }
                LibraryImageTile(
                    title = album.title,
                    subtitle = album.artist?.name ?: itemCountText(songsCount, "song"),
                    imageUrl = album.coverUrl,
                    onClick = { onAlbumClick(album) }
                )
            }
        }

        item {
            HomeHorizontalCardSection(
                title = "Artists",
                items = topArtists.take(HomeCollapsedItemLimit),
                expanded = true,
                onToggleExpanded = {},
                key = { index, artist -> "discover-artist-$index-${artist.id}" }
            ) { artist ->
                val songsCount = songs.count { it.artistId == artist.id }
                LibraryImageTile(
                    title = artist.name,
                    subtitle = itemCountText(songsCount, "song"),
                    imageUrl = artist.avatarUrl,
                    onClick = { onArtistClick(artist) }
                )
            }
        }

        item {
            HomeSongGridSection(
                title = "New Music",
                songs = freshSongs,
                expanded = true,
                onToggleExpanded = {},
                keyPrefix = "discover-song",
                onPlaySong = onPlaySong,
                onAddSongToPlaylist = onAddSongToPlaylist,
                onDownloadSong = onDownloadSong
            )
        }
    }
}

@Composable
internal fun ProfileTab(
    modifier: Modifier,
    user: User?,
    isLoading: Boolean,
    message: String?,
    onLogin: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onCollapseBottomBar: () -> Unit,
    onExpandBottomBar: () -> Unit
) {
    val listState = rememberLazyListState()

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
            .statusBarsPadding()
            .background(Color.Black)
            .padding(horizontal = 20.dp, vertical = 2.dp),
        state = listState,
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            AppleMusicPageHeader(
                title = "Profile",
                subtitle = user?.email ?: "Sign in to keep your library synced",
                addHorizontalPadding = false
            )
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (user?.avatarUrl.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = "Profile",
                        tint = AppleMusicRed,
                        modifier = Modifier.size(112.dp)
                    )
                } else {
                    CircleCoverImage(
                        imageUrl = user?.avatarUrl,
                        title = user?.displayName ?: "Profile",
                        modifier = Modifier.size(112.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = user?.displayName?.takeIf { it.isNotBlank() } ?: "Guest listener",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = user?.email ?: "No account connected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!message.isNullOrBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleMusicRed
                    )
                }
                if (isLoading) {
                    Spacer(Modifier.height(14.dp))
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }
            }
        }

        if (user == null) {
            item {
                TextButton(onClick = onLogin) {
                    Text("Sign in")
                }
            }
        } else {
            item {
                Column {
                    LibraryEntryRow(
                        title = "Edit profile",
                        sub = "Name and avatar",
                        icon = Icons.Rounded.Edit,
                        onClick = onEditProfile
                    )
                    LibraryEntryRow(
                        title = "Log out",
                        sub = user.email,
                        icon = Icons.Rounded.Close,
                        onClick = onLogout
                    )
                }
            }
        }
    }
}
