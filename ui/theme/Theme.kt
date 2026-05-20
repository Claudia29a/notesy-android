package com.example.notesy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NotesyLightColorScheme = lightColorScheme(
    primary = NotesyNavy,
    onPrimary = NotesyWhite,
    secondary = NotesyYellowCard,
    onSecondary = NotesyTextPrimary,
    tertiary = NotesyBlueFab,
    onTertiary = NotesyWhite,

    background = NotesyBackground,
    onBackground = NotesyTextPrimary,

    surface = NotesySurface,
    onSurface = NotesyTextPrimary,

    surfaceVariant = NotesyBlueSheet,
    onSurfaceVariant = NotesyTextSecondary,

    outline = NotesyNavyLight
)

private val NotesyDarkColorScheme = darkColorScheme(
    primary = NotesyNavy,
    onPrimary = NotesyWhite,
    secondary = NotesyYellowCard,
    onSecondary = NotesyTextPrimary,
    tertiary = NotesyBlueFab,
    onTertiary = NotesyWhite,

    background = NotesyBackground,
    onBackground = NotesyTextPrimary,

    surface = NotesySurface,
    onSurface = NotesyTextPrimary,

    surfaceVariant = NotesyBlueSheet,
    onSurfaceVariant = NotesyTextSecondary,

    outline = NotesyNavyLight
)

@Composable
fun NotesyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NotesyLightColorScheme,
        typography = Typography,
        content = content
    )
}