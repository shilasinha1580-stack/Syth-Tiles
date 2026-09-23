package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SynthColorScheme = darkColorScheme(
  primary = NeonMagenta,
  onPrimary = Color.White,
  primaryContainer = Color(0xFF4A0033),
  onPrimaryContainer = Color(0xFFFFD9E8),
  secondary = NeonCyan,
  onSecondary = Color(0xFF00201E),
  secondaryContainer = Color(0xFF004F4B),
  onSecondaryContainer = Color(0xFF73FAF0),
  tertiary = NeonAmber,
  onTertiary = Color(0xFF261900),
  tertiaryContainer = Color(0xFF553B00),
  onTertiaryContainer = Color(0xFFFFDF9E),
  background = SynthBackground,
  onBackground = Color(0xFFF1E6FF),
  surface = SynthSurface,
  onSurface = Color(0xFFF1E6FF),
  surfaceVariant = SynthSurfaceElevated,
  onSurfaceVariant = Color(0xFFD4C2EB),
  outline = SynthSurfaceBorder
)

@Composable
fun SynthTilesTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = SynthColorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  SynthTilesTheme(content = content)
}

