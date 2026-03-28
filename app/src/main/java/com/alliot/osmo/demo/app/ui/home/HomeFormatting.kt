package com.alliot.osmo.demo.app.ui.home

internal fun formatDuration(seconds: Long): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val remainder = safeSeconds % 60
    return "%02d:%02d".format(minutes, remainder)
}

internal fun resolutionLabel(index: Int): String = when (index) {
    1 -> "4K"
    2 -> "2.7K"
    3 -> "1080P"
    else -> "Auto"
}

internal fun fpsLabel(index: Int): Int = when (index) {
    1 -> 24
    2 -> 25
    3 -> 30
    4 -> 50
    5 -> 60
    6 -> 100
    7 -> 120
    else -> 30
}

internal fun eisLabel(mode: Int): String = when (mode) {
    1 -> "RockSteady"
    2 -> "HorizonBalancing"
    3 -> "HorizonSteady"
    else -> "标准"
}
