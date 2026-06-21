package com.mniroy.osmo.demo.app.ui.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import com.mniroy.osmo.demo.app.ui.home.DebugHomeState

enum class SettingsDestination {
    MENU, MODE, GPS, ACTIONS, STATUS
}

@Composable
fun SettingsListScreen(
    state: DebugHomeState,
    onClose: () -> Unit,
    onSwitchMode: (Int) -> Unit,
    onSetGpsAutoPushEnabled: (Boolean) -> Unit,
    onSetGpsAutoPushFrequencyHz: (Int) -> Unit,
    onSleep: () -> Unit,
    onWake: () -> Unit,
    onRequestVersion: () -> Unit,
    onDisconnect: () -> Unit,
) {
    var destination by remember { mutableStateOf(SettingsDestination.MENU) }

    when (destination) {
        SettingsDestination.MENU -> {
            val listState = rememberScalingLazyListState()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                ScalingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item {
                        Text(
                            text = "SETTINGS",
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            color = LimeAccent,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        )
                    }
                    item {
                        SettingsChip(label = "Mode Selection", onClick = { destination = SettingsDestination.MODE })
                    }
                    item {
                        SettingsChip(label = "GPS", onClick = { destination = SettingsDestination.GPS })
                    }
                    item {
                        SettingsChip(label = "Status", onClick = { destination = SettingsDestination.STATUS })
                    }
                    item {
                        SettingsChip(label = "Actions & Disconnect", onClick = { destination = SettingsDestination.ACTIONS })
                    }
                    item {
                        SettingsChip(label = "Back to Camera", onClick = onClose, backgroundColor = Color(0xFF1E1E1E))
                    }
                }
            }
        }
        SettingsDestination.MODE -> {
            Box(modifier = Modifier.fillMaxSize()) {
                ModeSelectScreen(state = state, onSwitchMode = onSwitchMode)
                BackButton(onClick = { destination = SettingsDestination.MENU })
            }
        }
        SettingsDestination.GPS -> {
            Box(modifier = Modifier.fillMaxSize()) {
                GpsScreen(
                    state = state,
                    onSetGpsAutoPushEnabled = onSetGpsAutoPushEnabled,
                    onSetGpsAutoPushFrequencyHz = onSetGpsAutoPushFrequencyHz
                )
                BackButton(onClick = { destination = SettingsDestination.MENU })
            }
        }
        SettingsDestination.ACTIONS -> {
            Box(modifier = Modifier.fillMaxSize()) {
                DeviceActionsScreen(
                    state = state,
                    onSleep = onSleep,
                    onWake = onWake,
                    onRequestVersion = onRequestVersion,
                    onNavigateToConnect = { 
                        onClose()
                        onDisconnect()
                    }
                )
                BackButton(onClick = { destination = SettingsDestination.MENU })
            }
        }
        SettingsDestination.STATUS -> {
            Box(modifier = Modifier.fillMaxSize()) {
                StatusScreen(state = state)
                BackButton(onClick = { destination = SettingsDestination.MENU })
            }
        }
    }
}

@Composable
private fun SettingsChip(label: String, onClick: () -> Unit, backgroundColor: Color = Color(0xFF0D1117)) {
    Chip(
        onClick = onClick,
        colors = ChipDefaults.chipColors(backgroundColor = backgroundColor),
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = label,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        androidx.wear.compose.material.CompactButton(
            onClick = onClick,
            colors = androidx.wear.compose.material.ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E1E1E))
        ) {
            Text("Back", fontSize = 10.sp)
        }
    }
}
