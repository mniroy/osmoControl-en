package com.mniroy.osmo.demo.app.ui.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Text
import com.mniroy.osmo.demo.app.ui.home.DebugHomeState
import com.mniroy.osmo.demo.app.ui.home.formatDuration
import com.mniroy.osmo.demo.app.ui.home.fpsLabel
import com.mniroy.osmo.demo.app.ui.home.resolutionLabel

/**
 * Status overview screen — battery, recording time, remaining space, resolution.
 */
@Composable
fun StatusScreen(state: DebugHomeState) {
    val camera = state.cameraStatus
    val session = state.sessionStatus
    val listState = rememberScalingLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Text(
                    text = "STATUS",
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = LimeAccent,
                    textAlign = TextAlign.Center,
                )
            }

            if (!session.protocolReady) {
                item {
                    Text(
                        text = "Waiting for\ndevice…",
                        fontSize = 12.sp,
                        color = DimText,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                // Battery
                item {
                    StatusRow(
                        label = "Battery",
                        value = "${camera.batteryPercent}%",
                        valueColor = when {
                            camera.batteryPercent > 50 -> GpsReady
                            camera.batteryPercent > 20 -> GpsNoFix
                            else -> RecordingRed
                        },
                    )
                }

                // Recording time
                item {
                    StatusRow(
                        label = "Rec Time",
                        value = if (camera.recording) {
                            formatDuration(camera.recordTimeSeconds.toLong())
                        } else "--:--",
                        valueColor = if (camera.recording) RecordingRed else DimText,
                    )
                }

                // Remaining time
                item {
                    StatusRow(
                        label = "Remaining",
                        value = formatDuration(camera.remainTimeSeconds),
                        valueColor = Color(0xFFECEFF1),
                    )
                }

                // Remaining MB
                item {
                    StatusRow(
                        label = "Space",
                        value = "${camera.remainCapacityMb} MB",
                        valueColor = Color(0xFFECEFF1),
                    )
                }

                // Resolution / FPS
                item {
                    StatusRow(
                        label = "Res / FPS",
                        value = "${resolutionLabel(camera.videoResolution)} / ${fpsLabel(camera.fpsIndex)}fps",
                        valueColor = SpeedAccent,
                    )
                }

                // GPS Hz
                item {
                    StatusRow(
                        label = "GPS Sync",
                        value = if (session.gpsAutoPushEnabled) {
                            "${session.gpsAutoPushHz}Hz"
                        } else "Off",
                        valueColor = if (session.gpsAutoPushEnabled) GpsReady else DimText,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
    valueColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1117), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = DimText,
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
        )
    }
}
