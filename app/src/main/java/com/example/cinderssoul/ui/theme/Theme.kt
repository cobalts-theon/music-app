package com.example.cinderssoul.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Crimson,
    onPrimary = Color.White,
    secondary = Mist,
    onSecondary = Ink,
    tertiary = Silver,
    background = Color(0xFF06090E),
    onBackground = Mist,
    surface = Color(0xFF0D141E),
    onSurface = Mist,
    surfaceVariant = Color(0xFF131D2B),
    onSurfaceVariant = Mist.copy(alpha = 0.88f),
    outline = Silver.copy(alpha = 0.45f)
)

private val LightColorScheme = lightColorScheme(
    primary = Crimson,
    onPrimary = Color.White,
    secondary = Ink,
    onSecondary = Color.White,
    tertiary = Silver,
    background = Parchment,
    onBackground = Ink,
    surface = Mist,
    onSurface = Ink,
    surfaceVariant = Silver,
    onSurfaceVariant = Ink.copy(alpha = 0.85f),
    outline = Silver
)

private val CindersSoulShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun CindersSoulTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    val activity = LocalContext.current.findActivity()

    if (!view.isInEditMode && activity != null) {
        SideEffect {
            activity.window.statusBarColor = colorScheme.background.toArgb()
            activity.window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(activity.window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = CindersSoulShapes,
        content = content
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
