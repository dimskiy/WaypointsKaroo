package de.dimskiy.waypoints.platform.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import de.dimskiy.waypoints.R

@Composable
fun getDarkColorScheme() = darkColorScheme(
    primary = colorResource(R.color.Primary),
    secondary = colorResource(R.color.Secondary),
    tertiary = colorResource(R.color.Tertiary),
    background = colorResource(R.color.Background),
    surface = colorResource(R.color.Surface),
    onPrimary = colorResource(R.color.OnPrimary)
)

@Composable
fun getLightColorScheme() = lightColorScheme(
    primary = colorResource(R.color.Primary),
    secondary = colorResource(R.color.Secondary),
    tertiary = colorResource(R.color.Tertiary),
    background = colorResource(R.color.Background),
    surface = colorResource(R.color.Surface),
    onPrimary = colorResource(R.color.OnPrimary)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> getDarkColorScheme()
        else -> getLightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}