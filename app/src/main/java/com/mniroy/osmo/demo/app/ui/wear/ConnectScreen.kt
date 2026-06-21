package com.mniroy.osmo.demo.app.ui.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import com.mniroy.osmo.demo.app.ui.home.DebugHomeState
import com.mniroy.osmo.demo.app.ui.home.WorkbenchConnectionPhase
import com.mniroy.osmo.demo.session.model.SessionDevice

/**
 * BLE connection screen shown when no device is connected.
 * Auto-scans on entry; lists discovered devices; connects on tap.
 */
@Composable
fun ConnectScreen(
    state: DebugHomeState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnect: (SessionDevice) -> Unit,
    onDisconnect: () -> Unit,
    onPermissionAction: () -> Unit,
) {
    val sheet = state.workbenchConnectionSheetUiModel
    val session = state.sessionStatus
    val phase = sheet.phase

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Text(
                text = "CONNECT",
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = DimText,
                textAlign = TextAlign.Center,
            )

            // Phase status
            val statusText = when (phase) {
                WorkbenchConnectionPhase.SCANNING -> "Scanning…"
                WorkbenchConnectionPhase.CONNECTING -> "Connecting…"
                WorkbenchConnectionPhase.PREPARING -> "Preparing…"
                WorkbenchConnectionPhase.READY -> session.connectedDevice?.name ?: "Connected"
                WorkbenchConnectionPhase.FAILURE -> "Connection failed"
                WorkbenchConnectionPhase.IDLE -> "Ready to scan"
            }
            val color = when (phase) {
                WorkbenchConnectionPhase.READY -> GpsReady
                WorkbenchConnectionPhase.FAILURE -> RecordingRed
                else -> SpeedAccent
            }
            Text(
                text = statusText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            // Permission CTA
            val permCta = state.permissionCta
            if (permCta != null) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onPermissionAction,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = GpsNoFix.copy(alpha = 0.8f),
                    ),
                ) {
                    Text(
                        text = permCta.label,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Scan / Stop / Disconnect button
            if (permCta == null) {
                val (label, bgColor) = when (phase) {
                    WorkbenchConnectionPhase.READY ->
                        "Disconnect" to RecordingRed.copy(alpha = 0.8f)
                    WorkbenchConnectionPhase.SCANNING ->
                        "Stop Scan" to DimText.copy(alpha = 0.4f)
                    WorkbenchConnectionPhase.FAILURE ->
                        "Retry Scan" to SpeedAccent.copy(alpha = 0.8f)
                    WorkbenchConnectionPhase.CONNECTING,
                    WorkbenchConnectionPhase.PREPARING ->
                        "Cancel" to RecordingRed.copy(alpha = 0.8f)
                    else ->
                        "Start Scan" to SpeedAccent.copy(alpha = 0.8f)
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        when (phase) {
                            WorkbenchConnectionPhase.READY,
                            WorkbenchConnectionPhase.CONNECTING,
                            WorkbenchConnectionPhase.PREPARING -> onDisconnect()
                            WorkbenchConnectionPhase.SCANNING -> onStopScan()
                            else -> onStartScan()
                        }
                    },
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(backgroundColor = bgColor),
                ) {
                    Text(text = label, fontSize = 12.sp)
                }
            }

            // Discovered device list
            val devices = state.discoveredDevices
            if (devices.isNotEmpty()) {
                devices.forEach { device ->
                    val isConnected = session.connectedDevice?.macAddress == device.macAddress
                    DeviceRow(
                        device = device,
                        isConnected = isConnected,
                        enabled = phase == WorkbenchConnectionPhase.SCANNING ||
                            phase == WorkbenchConnectionPhase.IDLE,
                        onClick = { onConnect(device) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(
    device: SessionDevice,
    isConnected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (isConnected) GpsReady.copy(alpha = 0.18f) else Color(0xFF0D1117)
    val textColor = if (isConnected) GpsReady else Color(0xFFECEFF1)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(12.dp))
            .then(
                if (enabled && !isConnected) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else Modifier,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = device.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
            )
            Text(
                text = device.macAddress.takeLast(8),
                fontSize = 9.sp,
                color = DimText,
            )
        }
    }
}
