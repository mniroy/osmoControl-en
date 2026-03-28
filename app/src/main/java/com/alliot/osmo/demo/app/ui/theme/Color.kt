package com.alliot.osmo.demo.app.ui.theme

import androidx.compose.ui.graphics.Color

val DayBackground = Color(0xFFF8FAFC)
val DaySurface = Color(0xFFFFFFFF)
val DaySurfaceElevated = Color(0xFFE2E8F0)
val DayForeground = Color(0xFF0F172A)
val DayForegroundMuted = Color(0xFF475569)
val DayOutline = Color(0xFFCBD5E1)

val NightBackground = Color(0xFF020617)
val NightSurface = Color(0xFF0F172A)
val NightSurfaceElevated = Color(0xFF1E293B)
val NightForeground = Color(0xFFF8FAFC)
val NightForegroundMuted = Color(0xFFCBD5E1)
val NightOutline = Color(0xFF334155)

val AccentBlueStrong = Color(0xFF0369A1)
val AccentBlue = Color(0xFF38BDF8)
val ActionInk = Color(0xFF020617)
val SignalReady = Color(0xFF22C55E)
val SignalWarn = Color(0xFFF59E0B)
val SignalDanger = Color(0xFFEF4444)

@Deprecated("Use SignalReady for ready/connected indicators", ReplaceWith("SignalReady"))
val SignalGreen = SignalReady

@Deprecated("Use SignalDanger for error/recording indicators", ReplaceWith("SignalDanger"))
val SignalRed = SignalDanger
