package com.example.cinderssoul.ui.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.isActive

@Composable
internal fun ArtworkBackground(imageUrl: String?, title: String) {
    val colorScheme = MaterialTheme.colorScheme
    val isLightTheme = colorScheme.background.luminance() > 0.5f
    val imageAlpha = if (isLightTheme) 0.46f else 0.58f
    val scrimColors = if (isLightTheme) {
        listOf(
            colorScheme.background.copy(alpha = 0.74f),
            colorScheme.surface.copy(alpha = 0.62f),
            Color.Black.copy(alpha = 0.16f),
            colorScheme.background.copy(alpha = 0.80f)
        )
    } else {
        listOf(
            colorScheme.background.copy(alpha = 0.86f),
            colorScheme.background.copy(alpha = 0.74f),
            colorScheme.surface.copy(alpha = 0.78f),
            colorScheme.background.copy(alpha = 0.90f)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
        )
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(16.dp)
                    .graphicsLayer(alpha = imageAlpha)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        scrimColors
                    )
                )
        )
    }
}

@Composable
internal fun RotatingArtwork(
    imageUrl: String?,
    title: String,
    isPlaying: Boolean,
    speedMultiplier: Float
) {
    val colorScheme = MaterialTheme.colorScheme
    val isLightTheme = colorScheme.background.luminance() > 0.5f
    val recordBorderColor = if (isLightTheme) {
        Color.Black.copy(alpha = 0.32f)
    } else {
        colorScheme.outline.copy(alpha = 0.36f)
    }
    val recordBrush = Brush.radialGradient(
        if (isLightTheme) {
            listOf(
                Color(0xFF30343A),
                Color(0xFF171B22),
                Color(0xFF07090D)
            )
        } else {
            listOf(
                colorScheme.surfaceVariant.copy(alpha = 0.92f),
                colorScheme.surface.copy(alpha = 0.96f),
                colorScheme.background
            )
        }
    )
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isPlaying, speedMultiplier) {
        if (!isPlaying) return@LaunchedEffect
        while (isActive) {
            val speed = speedMultiplier.coerceIn(0.35f, 3.2f)
            val duration = (12_000f / speed).toInt().coerceIn(1_200, 24_000)
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = tween(durationMillis = duration, easing = LinearEasing)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(332.dp)
                .graphicsLayer(rotationZ = rotation.value)
                .clip(CircleShape)
                .background(recordBrush)
                .border(1.dp, recordBorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(276.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Album,
                        contentDescription = title,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(72.dp)
                    )
                }
            } else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(276.dp)
                        .clip(CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (isLightTheme) Color(0xFF090B0F) else colorScheme.background)
                    .border(1.dp, recordBorderColor, CircleShape)
            )
        }
    }
}
