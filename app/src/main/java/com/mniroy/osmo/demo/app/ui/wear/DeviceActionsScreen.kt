package com.mniroy.osmo.demo.app.ui.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.Text
import com.mniroy.osmo.demo.app.ui.home.DebugHomeState

/**
 * Device actions screen: Sleep, Wake, Version request.
 */
@Composable
fun DeviceActionsScreen(
    state: DebugHomeState,
    onSleep: () -> Unit,
    onWake: () -> Unit,
    onRequestVersion: () -> Unit,
    onNavigateToConnect: () -> Unit,
) {
    val model = state.workbenchUiModel.deviceActionsUiModel
    val deviceName = state.sessionStatus.connectedDevice?.name ?: "No Device"
    val listState = rememberScalingLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Text(
                    text = "DEVICE",
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = LimeAccent,
                    textAlign = TextAlign.Center,
                )
            }

            item {
                Text(
                    text = deviceName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (state.sessionStatus.protocolReady) SpeedAccent else DimText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Busy action label
            val busy = state.busyAction
            if (busy != null) {
                item {
                    Text(
                        text = "⏳ $busy",
                        fontSize = 10.sp,
                        color = GpsNoFix,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            if (model.showSleepAction) {
                item {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onSleep,
                        enabled = model.deviceActionsEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = RecordingRed.copy(alpha = 0.80f),
                        ),
                    ) {
                        Text(
                            text = "Sleep",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            if (model.showWakeAction) {
                item {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onWake,
                        enabled = model.wakeActionsEnabled,
                    ) {
                        Text(text = "Wake", fontSize = 13.sp)
                    }
                }
            }

            if (model.showVersionAction) {
                item {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onRequestVersion,
                        enabled = model.deviceActionsEnabled,
                    ) {
                        Text(text = "Version", fontSize = 13.sp)
                    }
                }
            }

            // Version result
            val version = state.sessionStatus.latestVersion
            if (version != null) {
                item {
                    Text(
                        text = "v$version",
                        fontSize = 10.sp,
                        color = DimText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    )
                }
            }

            item {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    onClick = onNavigateToConnect,
                ) {
                    Text(text = "Manage Connection", fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }

            if (!model.showSleepAction && !model.showWakeAction && !model.showVersionAction) {
                item {
                    Text(
                        text = "No actions\navailable",
                        fontSize = 12.sp,
                        color = DimText,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
