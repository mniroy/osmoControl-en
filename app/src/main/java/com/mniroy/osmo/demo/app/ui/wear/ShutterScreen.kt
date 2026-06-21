package com.mniroy.osmo.demo.app.ui.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.mniroy.osmo.demo.app.ui.home.DebugHomeState
import com.mniroy.osmo.demo.app.ui.home.formatDuration
import com.mniroy.osmo.demo.app.ui.home.isPhotoCaptureMode

/**
 * Main Wear OS screen: Minimalist timer and stats with a large clickable area.
 */
@Composable
fun ShutterScreen(
    state: DebugHomeState,
    onToggleRecord: () -> Unit,
) {
    val camera = state.cameraStatus
    val session = state.sessionStatus
    val photoMode = isPhotoCaptureMode(camera)
    val recording = camera.recording && !photoMode
    val controlReady = session.protocolReady && !session.sleeping

    val modeLabel = when {
        photoMode -> "PHOTO"
        else -> camera.modeName.ifBlank { camera.modeLabel }.uppercase()
    }

    // Root container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Mode Label (Top green text)
            Text(
                text = modeLabel,
                color = if (recording) RecordingRed else LimeAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(8.dp))

            // Main Timer / Ready State
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(
                        enabled = true,
                        role = Role.Button,
                        onClick = onToggleRecord
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (recording) {
                    Text(
                        text = formatDuration(camera.recordTimeSeconds.toLong()),
                        color = Color.White,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-1).sp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            // Outer raised bevel ring
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFB0B0B0))
                                ),
                                shape = CircleShape
                            )
                            .padding(5.dp) // Ring thickness
                            // Inner concave face
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0xFFD0D0D0), Color(0xFFFFFFFF))
                                ),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Bottom stats (Battery, SD, GPS horizontally)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${camera.batteryPercent}%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "SD ${formatDuration(camera.remainTimeSeconds.toLong())}",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                )
                
                if (session.gpsSignalLocked) {
                    Text(
                        text = "GPS",
                        color = LimeAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (session.gpsAutoPushEnabled) {
                    Text(
                        text = "GPS",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
