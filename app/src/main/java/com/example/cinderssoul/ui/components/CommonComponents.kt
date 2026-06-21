package com.example.cinderssoul.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.cinderssoul.models.Album
import com.example.cinderssoul.models.Artist
import com.example.cinderssoul.models.Playlist
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.models.User
import com.example.cinderssoul.ui.app.AppleMusicRed
import com.example.cinderssoul.ui.app.LibrarySection
import com.example.cinderssoul.ui.app.RecentLibraryItem
import java.util.Locale

private val WhiteCardBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))

@Composable
internal fun AppleMusicPageHeader(
    title: String,
    subtitle: String,
    addHorizontalPadding: Boolean = true,
    showAccountAction: Boolean = false,
    currentUser: User? = null,
    onAccountClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (addHorizontalPadding) 16.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 34.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (showAccountAction) {
            Spacer(Modifier.width(12.dp))
            TopBarAccountAction(user = currentUser, onClick = onAccountClick)
        }
    }
}

@Composable
internal fun FeaturedSongCard(song: Song?, onPlaySong: (Song) -> Unit) {
    if (song == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(WhiteCardBorder, MaterialTheme.shapes.large)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                shape = MaterialTheme.shapes.large
            )
            .clickable { onPlaySong(song) }
            .padding(14.dp)
    ) {
        Text(
            text = "TOP PICK",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoverImage(
                imageUrl = song.coverUrl,
                title = song.title,
                modifier = Modifier.size(118.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.artistName} • ${song.albumTitle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Play",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
internal fun RowScope.BrowseCategoryTile(
    title: String,
    subtitle: String,
    colorIndex: Int,
    onClick: () -> Unit
) {
    val colors = listOf(
        Color(0xFFC0392B),
        Color(0xFF8E44AD),
        Color(0xFF2874A6),
        Color(0xFF148F77),
        Color(0xFFD35400),
        Color(0xFF7D6608)
    )
    val backgroundColor = colors[colorIndex % colors.size]

    Box(
        modifier = Modifier
            .weight(1f)
            .height(92.dp)
            .border(WhiteCardBorder, MaterialTheme.shapes.medium)
            .background(backgroundColor, MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.86f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun SearchEmptyState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(34.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "No results for \"$query\"",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
internal fun SearchArtistResultRow(
    artist: Artist,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 2.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleCoverImage(
                imageUrl = artist.avatarUrl,
                title = artist.name,
                modifier = Modifier.size(78.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.64f),
                modifier = Modifier.size(24.dp)
            )
        }
        LibraryRowDivider(modifier = Modifier.padding(start = 94.dp))
    }
}

@Composable
internal fun DiscoverGenreTile(
    title: String,
    subtitle: String,
    colorIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = GenreCardColors[colorIndex % GenreCardColors.size]
    val imageModel = remember(title) { genreCardAssetModel(title) }

    Box(
        modifier = modifier
            .height(104.dp)
            .clip(MaterialTheme.shapes.medium)
            .border(WhiteCardBorder, MaterialTheme.shapes.medium)
            .background(cardColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomStart
    ) {
        AsyncImage(
            model = imageModel,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(cardColor.copy(alpha = 0.46f), BlendMode.Softlight),
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardColor.copy(alpha = 0.42f))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.04f),
                            Color.Black.copy(alpha = 0.56f)
                        )
                    )
                )
        )
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.86f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val GenreCardColors = listOf(
    Color(0xFFC0392B),
    Color(0xFF2874A6),
    Color(0xFF148F77),
    Color(0xFF8E44AD),
    Color(0xFFD35400),
    Color(0xFF6C7A89)
)

private const val GenreCardAssetPath = "file:///android_asset/genre_cards/"

private fun genreCardAssetModel(title: String): String {
    val normalized = title.trim().lowercase(Locale.US)
    val fileName = when {
        normalized.contains("electronic") ||
            normalized.contains("edm") ||
            normalized.contains("dance") -> "electronic.jpg"
        normalized.contains("hip") ||
            normalized.contains("rap") -> "hip_hop.png"
        normalized.contains("alternative") ||
            normalized.contains("indie") -> "alternative.png"
        normalized.contains("rock") -> "rock.png"
        normalized.contains("r&b") ||
            normalized.contains("rnb") ||
            normalized.contains("soul") -> "r_and_b.png"
        normalized.contains("jazz") -> "jazz.jpg"
        normalized.contains("chill") ||
            normalized.contains("lofi") -> "chill.jpg"
        normalized.contains("pop") ||
            normalized.contains("v-pop") ||
            normalized.contains("vpop") -> "pop.jpg"
        normalized.contains("ost") -> "OST.jpg"

        else -> "default.png"
    }

    return GenreCardAssetPath + fileName
}

@Composable
internal fun SongRow(
    song: Song,
    onClick: () -> Unit,
    onPlay: (() -> Unit)? = null,
    onAddToPlaylist: (() -> Unit)? = null,
    onDownload: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    showBackground: Boolean = true
) {
    var showActions by remember { mutableStateOf(false) }

    val rowModifier = if (showBackground) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(WhiteCardBorder, MaterialTheme.shapes.small)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick)
            .padding(10.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoverImage(imageUrl = song.coverUrl, title = song.title, modifier = Modifier.size(56.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = "${song.artistName} • ${song.albumTitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box {
                IconButton(onClick = { showActions = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Actions",
                        modifier = Modifier.size(28.dp)
                    )
                }
                DropdownMenu(expanded = showActions, onDismissRequest = { showActions = false }) {
                    DropdownMenuItem(
                        text = { Text("Play") },
                        onClick = {
                            showActions = false
                            (onPlay ?: onClick).invoke()
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        }
                    )
                    if (onAddToPlaylist != null) {
                        DropdownMenuItem(
                            text = { Text("Add to playlist") },
                            onClick = {
                                showActions = false
                                onAddToPlaylist.invoke()
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = null)
                            }
                        )
                    }
                    if (onDownload != null) {
                        DropdownMenuItem(
                            text = { Text("Download") },
                            onClick = {
                                showActions = false
                                onDownload.invoke()
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.DownloadDone, contentDescription = null)
                            }
                        )
                    }
                    if (onShare != null) {
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = {
                                showActions = false
                                onShare.invoke()
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.Share, contentDescription = null)
                            }
                        )
                    }
                    if (onDelete != null) {
                        DropdownMenuItem(
                            text = { Text("Remove from playlist") },
                            onClick = {
                                showActions = false
                                onDelete.invoke()
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
        if (!showBackground) {
            LibraryRowDivider(modifier = Modifier.padding(start = 82.dp, end = 16.dp))
        }
    }
}

@Composable
internal fun MiniPlayer(
    modifier: Modifier = Modifier,
    song: Song?,
    isPlaying: Boolean,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    showNextButton: Boolean
) {
    if (song == null) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f), RoundedCornerShape(50.dp))
            .clickable(onClick = onOpen)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(8.dp))
        CoverImage(imageUrl = song.coverUrl, title = song.title, modifier = Modifier.size(35.dp))
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artistName, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onPlayPause) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(35.dp)
            )
        }
        if (showNextButton) {
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next song",
                    modifier = Modifier.size(35.dp)
                )
            }
        }
    }
}

@Composable
internal fun CoverImage(imageUrl: String?, title: String, modifier: Modifier = Modifier) {
    if (imageUrl.isNullOrBlank()) {
        Box(
            modifier = modifier
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Album, contentDescription = title)
        }
        return
    }

    AsyncImage(
        model = imageUrl,
        contentDescription = title,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(MaterialTheme.shapes.small).border(WhiteCardBorder, MaterialTheme.shapes.small)
    )
}

@Composable
internal fun SectionTitle(text: String, addHorizontalPadding: Boolean = true) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = if (addHorizontalPadding) Modifier.padding(horizontal = 16.dp) else Modifier
    )
}

@Composable
internal fun TopBarAccountAction(user: User?, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        if (user?.avatarUrl.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = "Account",
                modifier = Modifier.size(40.dp)
            )
        } else {
            AsyncImage(
                model = user?.avatarUrl,
                contentDescription = "Account",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), CircleShape)
            )
        }
    }
}

@Composable
internal fun LibrarySectionsBlock(
    activeSection: LibrarySection,
    playlistsCount: Int,
    artistsCount: Int,
    albumsCount: Int,
    songsCount: Int,
    genresCount: Int,
    downloadedCount: Int,
    onSectionChange: (LibrarySection) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        LibraryEntryRow(
            title = "Playlists",
            sub = itemCountText(playlistsCount, "playlist"),
            icon = Icons.Rounded.LibraryMusic,
            isActive = activeSection == LibrarySection.Playlists,
            onClick = { onSectionChange(LibrarySection.Playlists) }
        )
        LibraryEntryRow(
            title = "Artists",
            sub = itemCountText(artistsCount, "artist"),
            icon = Icons.Rounded.AccountCircle,
            isActive = activeSection == LibrarySection.Artists,
            onClick = { onSectionChange(LibrarySection.Artists) }
        )
        LibraryEntryRow(
            title = "Albums",
            sub = itemCountText(albumsCount, "album"),
            icon = Icons.Rounded.Album,
            isActive = activeSection == LibrarySection.Albums,
            onClick = { onSectionChange(LibrarySection.Albums) }
        )
        LibraryEntryRow(
            title = "Songs",
            sub = itemCountText(songsCount, "song"),
            icon = Icons.Rounded.MusicNote,
            isActive = activeSection == LibrarySection.Songs,
            onClick = { onSectionChange(LibrarySection.Songs) }
        )
        LibraryEntryRow(
            title = "Genres",
            sub = itemCountText(genresCount, "genre"),
            icon = Icons.Rounded.Category,
            isActive = activeSection == LibrarySection.Genres,
            onClick = { onSectionChange(LibrarySection.Genres) }
        )
        LibraryEntryRow(
            title = "Downloaded",
            sub = itemCountText(downloadedCount, "song"),
            icon = Icons.Rounded.DownloadDone,
            isActive = activeSection == LibrarySection.Downloaded,
            onClick = { onSectionChange(LibrarySection.Downloaded) }
        )
    }
}

@Composable
internal fun LibraryCommandRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    LibraryEntryRow(
        title = title,
        sub = null,
        icon = icon,
        onClick = onClick
    )
}

@Composable
internal fun PlaylistEntryRow(
    playlist: Playlist,
    onClick: () -> Unit
) {
    LibraryMediaRow(
        title = playlist.name,
        subtitle = itemCountText(playlist.songs.size, "song"),
        imageUrl = playlist.coverUrl,
        fallbackIcon = Icons.Rounded.LibraryMusic,
        onClick = onClick
    )
}

@Composable
internal fun RowScope.RecentAddedGridTile(
    item: RecentLibraryItem,
    onPlaylistClick: (Playlist) -> Unit,
    onAlbumClick: (Album) -> Unit
) {
    val modifier = Modifier
        .weight(1f)
        .clickable {
            when (item) {
                is RecentLibraryItem.PlaylistItem -> onPlaylistClick(item.playlist)
                is RecentLibraryItem.AlbumItem -> onAlbumClick(item.album)
            }
        }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f), MaterialTheme.shapes.medium)
                .padding(3.dp)
        ) {
            when (item) {
                is RecentLibraryItem.PlaylistItem -> CoverImage(
                    imageUrl = item.playlist.coverUrl,
                    title = item.playlist.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(WhiteCardBorder, MaterialTheme.shapes.small)
                )

                is RecentLibraryItem.AlbumItem -> CoverImage(
                    imageUrl = item.album.coverUrl,
                    title = item.album.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(WhiteCardBorder, MaterialTheme.shapes.small)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        when (item) {
            is RecentLibraryItem.PlaylistItem -> {
                Text(
                    text = item.playlist.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Playlist • ${item.playlist.songs.size} tracks",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            is RecentLibraryItem.AlbumItem -> {
                Text(
                    text = item.album.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.album.artist?.name ?: "Album",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun LibraryImageTile(
    title: String,
    subtitle: String,
    imageUrl: String?,
    onClick: () -> Unit
) {
    val tileSize = 148.dp

    Column(
        modifier = Modifier
            .width(tileSize)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(tileSize)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(3.dp)
        ) {
            CoverImage(
                imageUrl = imageUrl,
                title = title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.small)
                    .border(WhiteCardBorder, MaterialTheme.shapes.small)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun LibraryMediaRow(
    title: String,
    subtitle: String,
    imageUrl: String?,
    fallbackIcon: ImageVector,
    circleArtwork: Boolean = false,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (circleArtwork) {
                CircleCoverImage(
                    imageUrl = imageUrl,
                    title = title,
                    modifier = Modifier.size(56.dp)
                )
            } else if (imageUrl.isNullOrBlank()) {
                LibraryIconTile(icon = fallbackIcon, contentDescription = title, modifier = Modifier.size(56.dp))
            } else {
                CoverImage(imageUrl = imageUrl, title = title, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.64f),
                modifier = Modifier.size(24.dp)
            )
        }
        LibraryRowDivider(modifier = Modifier.padding(start = 68.dp))
    }
}

@Composable
internal fun LibraryIconTile(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .border(WhiteCardBorder, MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppleMusicRed,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
internal fun CircleCoverImage(imageUrl: String?, title: String, modifier: Modifier = Modifier) {
    if (imageUrl.isNullOrBlank()) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.AccountCircle, contentDescription = title, tint = AppleMusicRed)
        }
        return
    }

    AsyncImage(
        model = imageUrl,
        contentDescription = title,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(CircleShape).border(WhiteCardBorder, MaterialTheme.shapes.small),
    )
}

@Composable
internal fun LibraryRowDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    )
}

@Composable
internal fun LibraryEntryRow(
    title: String,
    sub: String?,
    icon: ImageVector,
    isActive: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val iconColor = AppleMusicRed
    val accentColor = MaterialTheme.colorScheme.onSurface
    val subColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.74f)

    val rowModifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(27.dp),
                tint = iconColor
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (!sub.isNullOrBlank()) {
                Text(
                    text = sub,
                    style = MaterialTheme.typography.labelMedium,
                    color = subColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.64f),
                modifier = Modifier.size(24.dp)
            )
        }
        LibraryRowDivider(modifier = Modifier.padding(start = 39.dp))
    }
}

internal fun itemCountText(count: Int, singular: String): String {
    val suffix = if (count == 1) singular else "${singular}s"
    return "$count $suffix"
}

internal fun formatTime(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L) / 1000L).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
