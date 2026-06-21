package com.example.cinderssoul.ui.player

import android.content.Context
import android.graphics.Color.alpha
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.dispose
import coil3.compose.AsyncImage
import coil3.load
import coil3.request.allowHardware
import com.example.cinderssoul.R
import eightbitlab.com.blurview.BlurTarget
import eightbitlab.com.blurview.BlurView
import kotlinx.coroutines.isActive
import android.graphics.Color as AndroidColor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource

@Composable
internal fun ArtworkBackground(imageUrl: String?, title: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                ArtworkBlurBackgroundView(context).apply {
                    bind(imageUrl = imageUrl, title = title)
                }
            },
            update = { view ->
                view.bind(imageUrl = imageUrl, title = title)
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.45f)
                        )
                    )
                )
        )
    }
}

private const val ArtworkBlurScaleFactor = 12f
private const val ArtworkBlurRadius = 25f
private const val ArtworkBackgroundColor = 0xFF101014.toInt()

private class ArtworkBlurBackgroundView(context: Context) : FrameLayout(context) {
    private var currentImageUrl: String? = null

    private val blurTarget = BlurTarget(context).apply {
        setBackgroundColor(ArtworkBackgroundColor)
        clipChildren = false
        clipToPadding = false
        disableTouchAndFocus()
    }
    private val imageView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        setBackgroundColor(ArtworkBackgroundColor)
        disableTouchAndFocus()
    }
    private val blurView = BlurView(context).apply {
        setupWith(blurTarget, ArtworkBlurScaleFactor, true)
            .setBlurRadius(ArtworkBlurRadius)
            .setOverlayColor(AndroidColor.TRANSPARENT)
            .setBlurAutoUpdate(true)
        disableTouchAndFocus()
    }

    init {
        setBackgroundColor(ArtworkBackgroundColor)
        clipChildren = false
        clipToPadding = false
        disableTouchAndFocus()

        val matchParent = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        addView(blurTarget, matchParent)
        blurTarget.addView(imageView, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        addView(blurView, LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    fun bind(imageUrl: String?, title: String) {
        imageView.contentDescription = title
        val normalizedUrl = imageUrl?.takeIf { it.isNotBlank() }
        if (currentImageUrl == normalizedUrl) return

        currentImageUrl = normalizedUrl
        if (normalizedUrl == null) {
            imageView.dispose()
            imageView.setImageDrawable(null)
            blurView.setBlurEnabled(false)
        } else {
            blurView.setBlurEnabled(true)
            imageView.load(normalizedUrl) {
                allowHardware(false)
            }
        }
    }

    override fun onDetachedFromWindow() {
        imageView.dispose()
        super.onDetachedFromWindow()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean = false

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean = false
}

private fun View.disableTouchAndFocus() {
    isClickable = false
    isLongClickable = false
    isFocusable = false
    isFocusableInTouchMode = false
    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
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
    val rotation = remember { Animatable(0f) }
    val artworkSize = animateDpAsState(
        targetValue = if (isPlaying) 200.dp else 300.dp,
        animationSpec = tween(durationMillis = 520),
        label = "rotating-artwork-size"
    ).value

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
                .size(300.dp)
                .dropShadow(CircleShape) {
                    color = Color.Black.copy(alpha = 0.25f)
                    radius = 8.dp.toPx()
                    offset = Offset(4.dp.toPx(), 4.dp.toPx())
                }
                .clip(CircleShape)
                .background(Color(0xFF07080C))
                .paint(
                    painter = painterResource(id = R.drawable.vinyldisc),
                    contentScale = ContentScale.Crop,
                )
                .border(1.dp, recordBorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(artworkSize)
                    .graphicsLayer(rotationZ = rotation.value)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
//                    Icon(
//                        imageVector = Icons.Rounded.Album,
//                        contentDescription = title,
//                        tint = colorScheme.onSurfaceVariant,
//                        modifier = Modifier.size(72.dp)
//                    )
                    }
                } else {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
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

@Preview(showBackground = true, name = "Artwork Background Light")
@Composable
private fun ArtworkBackgroundLightPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ArtworkBackground(imageUrl = null, title = "Sample Song")
        }
    }
}

@Preview(showBackground = true, name = "Artwork Background Dark", backgroundColor = 0xFF121212)
@Composable
private fun ArtworkBackgroundDarkPreview() {
    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(background = Color(0xFF121212))) {
        Box(modifier = Modifier.fillMaxSize()) {
            ArtworkBackground(imageUrl = R.drawable.authentication_background3.toString(), title = "Sample Song")
        }
    }
}

@Preview(showBackground = true, name = "Full Player Artwork Preview")
@Composable
private fun FullPlayerArtworkPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            ArtworkBackground(imageUrl = null, title = "Sample Song")
            androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                RotatingArtwork(
                    imageUrl = null,
                    title = "Sample Song",
                    isPlaying = true,
                    speedMultiplier = 1f
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
