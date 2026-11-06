package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue80,
    onPrimary = Color(0xFF001E2F),
    primaryContainer = Color(0xFF004D74),
    onPrimaryContainer = Color(0xFFC2E7FF),
    secondary = SlateGrey80,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = CobaltBlue80,
    onTertiary = Color(0xFF082F49),
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBlue40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = SlateGrey40,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = Color(0xFF1E293B),
    tertiary = CobaltBlue40,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = LightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // false to preserve intentional Grey & Blue aesthetic
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
