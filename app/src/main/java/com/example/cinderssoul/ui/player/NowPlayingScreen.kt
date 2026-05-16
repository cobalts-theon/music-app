package com.example.cinderssoul.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cinderssoul.MusicUiState
import com.example.cinderssoul.RepeatUiMode
import com.example.cinderssoul.models.Song
import com.example.cinderssoul.ui.components.formatTime
import kotlin.math.roundToLong

@Composable
internal fun NowPlayingScreen(
    modifier: Modifier = Modifier,
    uiState: MusicUiState,
    onDismiss: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBy: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleLike: () -> Unit,
    onAddSongToPlaylist: (Song) -> Unit
) {
    val song = uiState.currentSong ?: return
    val duration = uiState.durationMs.takeIf { it > 0L } ?: 1L

    var isDraggingProgress by remember(song.id) { mutableStateOf(false) }
    var dragProgress by remember(song.id) { mutableFloatStateOf(0f) }
    var sheetOffsetY by remember(song.id) { mutableFloatStateOf(0f) }

    val sliderValue = if (isDraggingProgress) {
        dragProgress
    } else {
        uiState.positionMs.toFloat().coerceIn(0f, duration.toFloat())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(translationY = sheetOffsetY)
    ) {
        ArtworkBackground(song.coverUrl, song.title)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 52.dp, height = 6.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                        .pointerInput(song.id) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { _, dragAmount ->
                                    sheetOffsetY = (sheetOffsetY + dragAmount).coerceAtLeast(0f)
                                },
                                onDragEnd = {
                                    if (sheetOffsetY > 150f) onDismiss() else sheetOffsetY = 0f
                                },
                                onDragCancel = { sheetOffsetY = 0f }
                            )
                        }
                )
            }

            RotatingArtwork(
                imageUrl = song.coverUrl,
                title = song.title,
                isPlaying = uiState.isPlayerRunning,
                speedMultiplier = uiState.spinSpeedMultiplier
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${song.artistName} • ${song.albumTitle}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onToggleLike, modifier = Modifier.size(50.dp)) {
                    Icon(
                        imageVector = if (uiState.isCurrentLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (uiState.isCurrentLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(34.dp)
                    )
                }
                IconButton(onClick = { onAddSongToPlaylist(song) }, modifier = Modifier.size(50.dp)) {
                    Icon(
                        Icons.AutoMirrored.Rounded.PlaylistAdd,
                        contentDescription = "Add to playlist",
                        modifier = Modifier.size(34.dp)
                    )
                }
                IconButton(onClick = onCycleRepeat, modifier = Modifier.size(50.dp)) {
                    Icon(
                        imageVector = if (uiState.repeatMode == RepeatUiMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = if (uiState.repeatMode != RepeatUiMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Column {
                Slider(
                    value = sliderValue,
                    onValueChange = {
                        isDraggingProgress = true
                        dragProgress = it
                    },
                    onValueChangeFinished = {
                        onSeekTo(dragProgress.roundToLong())
                        isDraggingProgress = false
                    },
                    valueRange = 0f..duration.toFloat()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(if (isDraggingProgress) dragProgress.roundToLong() else uiState.positionMs))
                    Text(formatTime(uiState.durationMs))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(38.dp))
                }
                IconButton(onClick = { onSeekBy(-10_000L) }) {
                    Icon(Icons.Rounded.Replay10, contentDescription = "Rewind 10s", modifier = Modifier.size(38.dp))
                }
                IconButton(onClick = onPlayPause, modifier = Modifier.size(86.dp)) {
                    Icon(
                        imageVector = if (uiState.isPlayerRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play Pause",
                        modifier = Modifier.size(66.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onSeekBy(10_000L) }) {
                    Icon(Icons.Rounded.Forward10, contentDescription = "Forward 10s", modifier = Modifier.size(38.dp))
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next", modifier = Modifier.size(38.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.VolumeDown,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                Slider(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),
                    value = uiState.volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f
                )
                Text(
                    text = "${(uiState.volume * 100).roundToLong()}%",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
