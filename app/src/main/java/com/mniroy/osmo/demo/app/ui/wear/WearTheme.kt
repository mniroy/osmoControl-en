package com.mniroy.osmo.demo.app.ui.wear

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Colors

// Osmo Wear palette — always dark (watch screens are AMOLED)
internal val WearColors = Colors(
    primary = Color(0xFF4FC3F7),           // sky-blue accent
    primaryVariant = Color(0xFF0288D1),
    secondary = Color(0xFF81C784),         // green for GPS ready
    secondaryVariant = Color(0xFF388E3C),
    error = Color(0xFFEF5350),             // red for recording
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onError = Color.White,
    background = Color(0xFF000000),
    onBackground = Color(0xFFECEFF1),
    surface = Color(0xFF0D1117),
    onSurface = Color(0xFFECEFF1),
)

// Semantic tokens used across screens
internal val RecordingRed  = Color(0xFFEF5350)
internal val GpsReady      = Color(0xFFCEFF00) // Changed to LimeAccent
internal val GpsNoFix      = Color(0xFF546E7A) // Dimmer gray
internal val GpsOff        = Color(0xFF333333) // Dark Track
internal val SpeedAccent   = Color(0xFFFFFFFF) // White
internal val DimText       = Color(0xFF888888)

// New Theme Colors
internal val LimeAccent    = Color(0xFFCEFF00)
internal val DarkTrack     = Color(0xFF333333)

@Composable
fun WearAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = WearColors,
        content = content,
    )
}
