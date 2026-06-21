package com.mniroy.osmo.demo.app.ui.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import com.mniroy.osmo.demo.app.ui.home.DebugHomeState
import java.util.Locale

/**
 * GPS 10Hz screen.
 * Shows GPS auto-push toggle, lock state, and last known speed.
 */
@Composable
fun GpsScreen(
    state: DebugHomeState,
    onSetGpsAutoPushEnabled: (Boolean) -> Unit,
    onSetGpsAutoPushFrequencyHz: (Int) -> Unit,
) {
    val session = state.sessionStatus
    val gpsEnabled = session.gpsAutoPushEnabled
    val gpsReady = state.workbenchUiModel.gpsActionEnabled
    val locked = session.gpsSignalLocked
    val hz = session.gpsAutoPushHz

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header
            Text(
                text = "GPS",
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = LimeAccent,
                textAlign = TextAlign.Center,
            )

            // GPS lock indicator
            GpsStatusIndicator(locked = locked, enabled = gpsEnabled)

            // Toggle chip
            ToggleChip(
                modifier = Modifier.fillMaxWidth(),
                checked = gpsEnabled,
                onCheckedChange = { if (gpsReady || gpsEnabled) onSetGpsAutoPushEnabled(it) },
                label = {
                    Text(
                        text = if (gpsEnabled) "${hz}Hz Auto Sync" else "GPS Off",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                secondaryLabel = {
                    Text(
                        text = if (gpsEnabled) {
                            if (locked) "Signal Locked" else "Acquiring…"
                        } else {
                            state.workbenchUiModel.gpsActionDisabledReason ?: "Tap to enable"
                        },
                        fontSize = 10.sp,
                        color = DimText,
                    )
                },
                toggleControl = {
                    Switch(
                        checked = gpsEnabled,
                        enabled = gpsReady || gpsEnabled,
                    )
                },
                colors = ToggleChipDefaults.toggleChipColors(
                    checkedStartBackgroundColor = GpsReady.copy(alpha = 0.20f),
                    checkedEndBackgroundColor = GpsReady.copy(alpha = 0.10f),
                    uncheckedStartBackgroundColor = Color(0xFF0D1117),
                    uncheckedEndBackgroundColor = Color(0xFF0D1117),
                ),
            )

            // Set 10Hz quick action
            if (gpsEnabled && gpsReady) {
                HzChip(
                    label = "Set 10Hz",
                    selected = hz == 10,
                    onClick = { onSetGpsAutoPushFrequencyHz(10) },
                )
            }

            // Speed + coordinate
            val speedKmh = session.lastGpsSpeedMps?.let { it * 3.6f }
            if (speedKmh != null) {
                Text(
                    text = String.format(Locale.US, "%.1f km/h", speedKmh),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpeedAccent,
                )
            }
            val coord = session.lastGpsCoordinate
            if (coord != null) {
                Text(
                    text = coord,
                    fontSize = 9.sp,
                    color = DimText,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun GpsStatusIndicator(locked: Boolean, enabled: Boolean) {
    val color = when {
        !enabled -> GpsOff
        locked -> GpsReady
        else -> GpsNoFix
    }
    val label = when {
        !enabled -> "GPS Off"
        locked -> "Signal Locked"
        else -> "No Fix"
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun HzChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ToggleChip(
        modifier = Modifier.fillMaxWidth(),
        checked = selected,
        onCheckedChange = { if (!selected) onClick() },
        label = {
            Text(text = label, fontSize = 12.sp)
        },
        toggleControl = {},
        colors = ToggleChipDefaults.toggleChipColors(
            checkedStartBackgroundColor = SpeedAccent.copy(alpha = 0.22f),
            checkedEndBackgroundColor = SpeedAccent.copy(alpha = 0.10f),
            uncheckedStartBackgroundColor = Color(0xFF0D1117),
            uncheckedEndBackgroundColor = Color(0xFF0D1117),
        ),
    )
}
