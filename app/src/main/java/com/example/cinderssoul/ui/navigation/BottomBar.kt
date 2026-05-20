package com.example.cinderssoul.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cinderssoul.ui.app.AppleMusicRed
import com.example.cinderssoul.ui.components.CoverImage
import com.example.cinderssoul.ui.components.MiniPlayer
import com.example.cinderssoul.ui.app.MusicTab
import com.example.cinderssoul.models.Song

internal val BottomTabs = listOf(
    MusicTab.Home,
    MusicTab.Search,
    MusicTab.Discover,
    MusicTab.Library
)

internal fun labelForTab(tab: MusicTab): String = when (tab) {
    MusicTab.Home -> "Home"
    MusicTab.Search -> "Search"
    MusicTab.Discover -> "Discover"
    MusicTab.Library -> "Library"
    MusicTab.Profile -> "Profile"
}

internal fun iconForTab(tab: MusicTab): ImageVector = when (tab) {
    MusicTab.Home -> Icons.Rounded.Home
    MusicTab.Search -> Icons.Rounded.Search
    MusicTab.Discover -> Icons.Rounded.Explore
    MusicTab.Library -> Icons.Rounded.LibraryMusic
    MusicTab.Profile -> Icons.Rounded.AccountCircle
}

@Composable
internal fun AppBottomBar(
    modifier: Modifier = Modifier,
    selectedTab: MusicTab,
    compact: Boolean,
    song: Song?,
    isPlaying: Boolean,
    onOpenNowPlaying: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onTabSelected: (MusicTab) -> Unit,
    onExpand: () -> Unit
) {
    val bottomScrim = MaterialTheme.colorScheme.background
    val barBackground = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        bottomScrim.copy(alpha = 0.16f),
                        bottomScrim.copy(alpha = 0.50f),
                        bottomScrim
                    )
                )
            )
            .padding(top = 38.dp)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp)
            .padding(bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = song != null,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight / 2 },
                animationSpec = tween(durationMillis = 280)
            ) + fadeIn(animationSpec = tween(durationMillis = 220)),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight / 2 },
                animationSpec = tween(durationMillis = 220)
            ) + fadeOut(animationSpec = tween(durationMillis = 180))
        ) {
            if (compact && song != null) {
                CompactCurrentSongBar(
                    song = song,
                    currentTab = selectedTab,
                    isPlaying = isPlaying,
                    onOpen = onOpenNowPlaying,
                    onPlayPause = onPlayPause,
                    onExpand = onExpand,
                    onSearch = { onTabSelected(MusicTab.Search) }
                )
            } else {
                MiniPlayer(
                    song = song,
                    isPlaying = isPlaying,
                    onOpen = onOpenNowPlaying,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    showNextButton = true
                )
            }
        }

        AnimatedVisibility(
            visible = !compact || song == null,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight / 2 },
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(animationSpec = tween(durationMillis = 220)),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 230)
            ) + fadeOut(animationSpec = tween(durationMillis = 180))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(barBackground)
                    .border(1.dp, borderColor, RoundedCornerShape(50.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomTabs.forEach { tab ->
                    BottomTabButton(
                        modifier = Modifier.weight(1f),
                        tab = tab,
                        selected = selectedTab == tab,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun BottomTabButton(
    modifier: Modifier = Modifier,
    tab: MusicTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val iconTint = if (selected) AppleMusicRed else MaterialTheme.colorScheme.onSurfaceVariant
    val itemBackground = if (selected) {
        AppleMusicRed.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
    }
    val itemBorder = if (selected) {
        AppleMusicRed.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    }

    Column(
        modifier = modifier
            .padding(horizontal = 3.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(itemBackground)
            .border(1.dp, itemBorder, RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = iconForTab(tab),
            contentDescription = labelForTab(tab),
            modifier = Modifier.size(27.dp),
            tint = iconTint
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = labelForTab(tab),
            style = MaterialTheme.typography.labelSmall,
            color = iconTint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun CompactCurrentSongBar(
    song: Song,
    currentTab: MusicTab,
    isPlaying: Boolean,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onExpand: () -> Unit,
    onSearch: () -> Unit
) {
    Row(
        modifier = Modifier

            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AppleMusicRed.copy(alpha = 0.3f))
                .border(1.dp, AppleMusicRed.copy(alpha = 0.42f), CircleShape)
                .clickable(onClick = onExpand),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconForTab(currentTab),
                contentDescription = labelForTab(currentTab),
                tint = AppleMusicRed,
                modifier = Modifier.size(23.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                .border(1.dp, AppleMusicRed.copy(alpha = 0.42f), RoundedCornerShape(50.dp))
                .clickable(onClick = onOpen)
                .padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(8.dp))
            CoverImage(imageUrl = song.coverUrl, title = song.title, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.56f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.42f), CircleShape)
                .clickable(onClick = onSearch),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}
