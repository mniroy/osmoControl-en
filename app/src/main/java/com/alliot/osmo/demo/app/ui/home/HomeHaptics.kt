package com.alliot.osmo.demo.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

enum class HomeHapticKind {
    PRIMARY,
    SECONDARY,
    NAVIGATION,
    DANGER,
}

fun homeHapticFeedbackType(kind: HomeHapticKind): HapticFeedbackType = when (kind) {
    HomeHapticKind.PRIMARY, HomeHapticKind.DANGER -> HapticFeedbackType.LongPress
    HomeHapticKind.SECONDARY, HomeHapticKind.NAVIGATION -> HapticFeedbackType.TextHandleMove
}

@Composable
fun rememberHapticClick(
    kind: HomeHapticKind,
    onClick: () -> Unit,
): () -> Unit {
    val haptics = LocalHapticFeedback.current
    return remember(haptics, kind, onClick) {
        {
            haptics.performHapticFeedback(homeHapticFeedbackType(kind))
            onClick()
        }
    }
}
