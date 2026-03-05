package com.pdfwizard.pro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryBlueContainer,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    onSurface = OnSurface
)

private val DarkColors = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryBlue,
    secondary = Secondary,
    background = Color(0xFF1A1C1E),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE6E1E5)
)

@Composable
fun PdfWizardTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
