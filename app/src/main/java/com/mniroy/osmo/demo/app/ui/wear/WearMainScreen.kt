package com.mniroy.osmo.demo.app.ui.wear

import android.view.KeyEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.unit.dp
import androidx.compose.animation.Crossfade
import androidx.activity.compose.BackHandler
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.CircularProgressIndicator
import com.mniroy.osmo.demo.app.ui.home.DebugHomeState
import com.mniroy.osmo.demo.session.model.SessionDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.scrollBy
import kotlin.math.abs
import kotlin.math.min

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
                        val coroutineScope = rememberCoroutineScope()
                        val focusRequester = remember { FocusRequester() }
                        val verticalPagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 3 })
                        
                        val rawModeOptions = state.workbenchUiModel.modeOptions
                        val allowedModes = setOf(0x05, 0x01, 0x02, 0x0A)
                        val modeOptions = remember(rawModeOptions) { rawModeOptions.filter { it.mode in allowedModes } }
                        
                        val currentMode = state.cameraStatus.mode
                        val safeSize = modeOptions.size.takeIf { it > 0 } ?: 1
                        val initialIndex = modeOptions.indexOfFirst { it.mode == currentMode }.takeIf { it >= 0 } ?: 0
                        val initialPage = remember { (Int.MAX_VALUE / 2) / safeSize * safeSize + initialIndex }
                        val horizontalPagerState = androidx.compose.foundation.pager.rememberPagerState(
                            initialPage = initialPage,
                            pageCount = { Int.MAX_VALUE }
                        )

                        // Sync pager with external mode changes
                        LaunchedEffect(currentMode, modeOptions) {
                            if (modeOptions.isNotEmpty()) {
                                val targetIndex = modeOptions.indexOfFirst { it.mode == currentMode }
                                if (targetIndex >= 0) {
                                    val currentMod = horizontalPagerState.currentPage % modeOptions.size
                                    if (currentMod != targetIndex) {
                                        // find shortest path to target
                                        var diff = targetIndex - currentMod
                                        if (diff > modeOptions.size / 2) diff -= modeOptions.size
                                        else if (diff < -modeOptions.size / 2) diff += modeOptions.size
                                        horizontalPagerState.animateScrollToPage(horizontalPagerState.currentPage + diff)
                                    }
                                }
                            }
                        }

                        // Trigger mode switch when pager settles on a new page
                        LaunchedEffect(horizontalPagerState.settledPage) {
                            if (modeOptions.isNotEmpty() && state.workbenchUiModel.modeSwitchEnabled && !state.cameraStatus.recording) {
                                val selectedMode = modeOptions[horizontalPagerState.settledPage % modeOptions.size].mode
                                if (selectedMode != currentMode) {
                                    onSwitchMode(selectedMode)
                                }
                            }
                        }

                        // Debounce state for rotary
                        var lastScrollTime by remember { mutableStateOf(0L) }
                        var accumulatedScroll by remember { mutableStateOf(0f) }

                        Box(modifier = Modifier.fillMaxSize()) {
                            val watchBattery = rememberBatteryLevel()
                            val cameraBattery = state.cameraStatus.batteryPercent / 100f
                            
                            androidx.wear.compose.material.CircularProgressIndicator(
                                progress = watchBattery,
                                modifier = Modifier.fillMaxSize().padding(2.dp),
                                indicatorColor = Color(0xFF2196F3),
                                trackColor = Color.DarkGray,
                                strokeWidth = 3.dp
                            )
                            
                            androidx.wear.compose.material.CircularProgressIndicator(
                                progress = cameraBattery,
                                modifier = Modifier.fillMaxSize().padding(6.dp),
                                indicatorColor = LimeAccent,
                                trackColor = Color.DarkGray,
                                strokeWidth = 3.dp
                            )

                            androidx.compose.foundation.pager.VerticalPager(
                                state = verticalPagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                if (page == 0) {
                                // Main Shutter Screen with HorizontalPager for modes
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .onRotaryScrollEvent { event ->
                                            val now = System.currentTimeMillis()
                                            if (now - lastScrollTime > 300) {
                                                accumulatedScroll = 0f
                                            }
                                            lastScrollTime = now
                                            accumulatedScroll += event.verticalScrollPixels
                                            
                                            val threshold = 30f
                                            if (abs(accumulatedScroll) > threshold) {
                                                if (modeOptions.isNotEmpty() && state.workbenchUiModel.modeSwitchEnabled && !state.cameraStatus.recording) {
                                                    val currentIndex = horizontalPagerState.currentPage
                                                    val nextIndex = if (accumulatedScroll > 0) {
                                                        currentIndex + 1
                                                    } else {
                                                        currentIndex - 1
                                                    }
                                                    coroutineScope.launch {
                                                        horizontalPagerState.animateScrollToPage(nextIndex)
                                                    }
                                                }
                                                accumulatedScroll = 0f
                                            }
                                            true
                                        }
                                        .focusRequester(focusRequester)
                                        .focusable()
                                ) {
                                    LaunchedEffect(Unit) {
                                        focusRequester.requestFocus()
                                    }
                                    
                                    androidx.compose.foundation.pager.HorizontalPager(
                                        state = horizontalPagerState,
                                        modifier = Modifier.fillMaxSize()
                                    ) { page ->
                                        val pageModeLabel = if (modeOptions.isNotEmpty()) {
                                            val label = modeOptions[page % modeOptions.size].label
                                            if (label.isBlank()) modeOptions[page % modeOptions.size].mode.toString() else label
                                        } else null
                                        ShutterScreen(state = state, onToggleRecord = onToggleRecord, targetModeLabel = pageModeLabel)
                                    }
                                }
                            } else if (page == 1) {
                                // Sleep / Wake Screen
                                val sleeping = state.sessionStatus.sleeping
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Button(
                                        onClick = { if (sleeping) onWake() else onSleep() },
                                        modifier = Modifier.size(80.dp),
                                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E1E1E))
                                    ) {
                                        Text(
                                            text = if (sleeping) "WAKE" else "SLEEP",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            } else {
                                // Settings Big Button Screen
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Button(
                                        onClick = { showSettings = true },
                                        modifier = Modifier.size(80.dp),
                                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E1E1E))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = Color.White,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } // end Box
                    } // end else
                } // end WearAppTheme
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
