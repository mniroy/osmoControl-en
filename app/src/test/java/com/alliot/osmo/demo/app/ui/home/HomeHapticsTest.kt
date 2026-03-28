package com.alliot.osmo.demo.app.ui.home

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeHapticsTest {

    @Test
    fun `primary and danger actions use stronger haptic feedback`() {
        assertEquals(HapticFeedbackType.LongPress, homeHapticFeedbackType(HomeHapticKind.PRIMARY))
        assertEquals(HapticFeedbackType.LongPress, homeHapticFeedbackType(HomeHapticKind.DANGER))
    }

    @Test
    fun `navigation and secondary actions use lighter haptic feedback`() {
        assertEquals(HapticFeedbackType.TextHandleMove, homeHapticFeedbackType(HomeHapticKind.NAVIGATION))
        assertEquals(HapticFeedbackType.TextHandleMove, homeHapticFeedbackType(HomeHapticKind.SECONDARY))
    }
}
