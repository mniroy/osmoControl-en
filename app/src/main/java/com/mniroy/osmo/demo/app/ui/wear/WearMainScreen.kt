package com.mniroy.osmo.demo.app.ui.wear

import android.view.KeyEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import kotlinx.coroutines.delay
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WearMainScreen(
    state: DebugHomeState,
    onToggleRecord: () -> Unit,
    onQsClick: () -> Unit,
    onSwitchMode: (Int) -> Unit,
    onSetGpsAutoPushEnabled: (Boolean) -> Unit,
    onSetGpsAutoPushFrequencyHz: (Int) -> Unit,
    onSleep: () -> Unit,
    onWake: () -> Unit,
    onRequestVersion: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnect: (SessionDevice) -> Unit,
    onDisconnect: () -> Unit,
    onPermissionAction: () -> Unit,
) {
    val connected = state.sessionStatus.protocolReady ||
        state.sessionStatus.connectedDevice != null

    val batteryProgress = (state.cameraStatus.batteryPercent / 100f).coerceIn(0f, 1f)
    val recording = state.cameraStatus.recording && !com.mniroy.osmo.demo.app.ui.home.isPhotoCaptureMode(state.cameraStatus)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()

                val strokeWidthPx = 5.dp.toPx()
                val paddingPx = 6.dp.toPx()
                val color = if (recording) RecordingRed else LimeAccent
                val trackColor = DarkTrack

                val diameter = min(size.width, size.height) - 2 * paddingPx - strokeWidthPx
                val sizeOffset = Offset(paddingPx + strokeWidthPx / 2, paddingPx + strokeWidthPx / 2)
                val arcSize = Size(diameter, diameter)

                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = sizeOffset,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )

                drawArc(
                    color = color,
                    startAngle = 270f,
                    sweepAngle = batteryProgress * 360f,
                    useCenter = false,
                    topLeft = sizeOffset,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
            .background(Color.Black)
    ) {
        Crossfade(targetState = connected, label = "RootNav") { isConnected ->
            if (isConnected) {
                WearAppTheme {
                    var showSettings by remember { mutableStateOf(false) }

                    if (showSettings) {
                        SettingsListScreen(
                            state = state,
                            onClose = { showSettings = false },
                            onSwitchMode = onSwitchMode,
                            onSetGpsAutoPushEnabled = onSetGpsAutoPushEnabled,
                            onSetGpsAutoPushFrequencyHz = onSetGpsAutoPushFrequencyHz,
                            onSleep = onSleep,
                            onWake = onWake,
                            onRequestVersion = onRequestVersion,
                            onDisconnect = onDisconnect
                        )
                    } else {
                        val scrollState = rememberScrollState()
                        val focusRequester = remember { FocusRequester() }
                        val coroutineScope = rememberCoroutineScope()
                        
                        // Debounce state for rotary
                        var lastScrollTime by remember { mutableStateOf(0L) }
                        var accumulatedScroll by remember { mutableStateOf(0f) }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .onRotaryScrollEvent { event ->
                                    // Mode Selection via Bezel
                                    val now = System.currentTimeMillis()
                                    if (now - lastScrollTime > 300) {
                                        accumulatedScroll = 0f
                                    }
                                    lastScrollTime = now
                                    accumulatedScroll += event.verticalScrollPixels

                                    val threshold = 50f
                                    if (abs(accumulatedScroll) > threshold) {
                                        val modeOptions = state.workbenchUiModel.modeOptions
                                        val currentMode = state.cameraStatus.mode
                                        if (modeOptions.isNotEmpty() && state.workbenchUiModel.modeSwitchEnabled && !state.cameraStatus.recording) {
                                            val currentIndex = modeOptions.indexOfFirst { it.mode == currentMode }.takeIf { it >= 0 } ?: 0
                                            if (accumulatedScroll > 0 && currentIndex < modeOptions.size - 1) {
                                                onSwitchMode(modeOptions[currentIndex + 1].mode)
                                            } else if (accumulatedScroll < 0 && currentIndex > 0) {
                                                onSwitchMode(modeOptions[currentIndex - 1].mode)
                                            }
                                        }
                                        accumulatedScroll = 0f
                                    }
                                    true // Consume the event so it doesn't scroll the screen vertically
                                }
                                .focusRequester(focusRequester)
                                .focusable()
                        ) {
                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // First "page" is ShutterScreen
                                Box(modifier = Modifier.fillParentMaxSize()) {
                                    ShutterScreen(state = state, onToggleRecord = onToggleRecord)
                                }
                                
                                // Spacer to separate shutter from settings
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Settings button at the bottom
                                Button(
                                    onClick = { showSettings = true },
                                    modifier = Modifier.size(48.dp),
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E1E1E))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            } else {
                WearAppTheme {
                    ConnectScreen(
                        state = state,
                        onStartScan = onStartScan,
                        onStopScan = onStopScan,
                        onConnect = onConnect,
                        onDisconnect = onDisconnect,
                        onPermissionAction = onPermissionAction,
                    )
                }
            }
        }
    }
}
