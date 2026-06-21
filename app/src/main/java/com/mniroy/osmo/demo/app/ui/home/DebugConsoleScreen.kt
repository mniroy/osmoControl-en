package com.mniroy.osmo.demo.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mniroy.osmo.demo.session.log.SessionLogEntry
import com.mniroy.osmo.demo.session.model.HandshakeStage
import com.mniroy.osmo.demo.session.model.LogCategory
import com.mniroy.osmo.demo.session.model.SessionDevice
import com.mniroy.osmo.demo.session.model.SessionTransportMode

@Composable
fun DebugConsoleScreen(
    state: DebugHomeState,
    listState: LazyListState,
    onModeSelected: (SessionTransportMode) -> Unit,
    onVerifyModeSelected: (Int) -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onUpdateDeviceFilterQuery: (String) -> Unit,
    onDisconnect: () -> Unit,
    onConnect: (SessionDevice) -> Unit,
    onVersion: () -> Unit,
    onReboot: () -> Unit,
    onToggleRecord: () -> Unit,
    onSwitchMode: (Int) -> Unit,
    onSubscribe: () -> Unit,
    onSleep: () -> Unit,
    onWake: () -> Unit,
    onWakeAndSnapshot: () -> Unit,
    onRecordKeyClick: () -> Unit,
    onQsKeyClick: () -> Unit,
    onSnapshotKeyClick: () -> Unit,
    onPushGps: () -> Unit,
    onSetGpsFrequency: (Int) -> Unit,
    onSetLocationRequestFrequency: (Int) -> Unit,
    onUpdateManualHex: (String) -> Unit,
    onSendManual: () -> Unit,
    onSetGpsSyncEnabled: (Boolean) -> Unit,
    onPermissionAction: () -> Unit = {},
) {
    val clipboardManager = LocalClipboardManager.current
    var logsClearedAtMillis by rememberSaveable { mutableLongStateOf(Long.MIN_VALUE) }
    val logGroups = remember(state.logs, logsClearedAtMillis) {
        groupDebugLogs(state.logs, logsClearedAtMillis)
    }
    val debugConsoleUiModel = state.debugConsoleUiModel

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            DebugSessionSection(
                state = state,
                onModeSelected = onModeSelected,
                onVerifyModeSelected = onVerifyModeSelected,
                onStartScan = onStartScan,
                onStopScan = onStopScan,
                onDisconnect = onDisconnect,
                onPermissionAction = onPermissionAction,
            )
        }
        item {
            DebugDeviceListSection(
                state = state,
                onFilterChange = onUpdateDeviceFilterQuery,
                onConnect = onConnect,
            )
        }
        item {
            DebugCommandsSection(
                state = state,
                uiModel = debugConsoleUiModel,
                onVersion = onVersion,
                onReboot = onReboot,
                onToggleRecord = onToggleRecord,
                onSwitchMode = onSwitchMode,
                onSubscribe = onSubscribe,
                onSleep = onSleep,
                onWake = onWake,
                onWakeAndSnapshot = onWakeAndSnapshot,
                onRecordKeyClick = onRecordKeyClick,
                onQsKeyClick = onQsKeyClick,
                onSnapshotKeyClick = onSnapshotKeyClick,
                onPushGps = onPushGps,
                onSetGpsFrequency = onSetGpsFrequency,
                onSetLocationRequestFrequency = onSetLocationRequestFrequency,
                onSetGpsSyncEnabled = onSetGpsSyncEnabled,
            )
        }
        item {
            DebugStatusSection(state = state)
        }
        item {
            DebugLogsSection(
                groups = logGroups,
                onClearLogs = {
                    logsClearedAtMillis = state.logs.maxOfOrNull(SessionLogEntry::timestampMillis) ?: Long.MIN_VALUE
                },
                onCopyRecent = {
                    val recent = logGroups.flatMap { group ->
                        group.entries.take(10).map { entry ->
                            "[${entry.category.name}] ${entry.message}${entry.hex?.let { " | $it" } ?: ""}"
                        }
                    }
                    clipboardManager.setText(AnnotatedString(recent.joinToString("\n")))
                },
            )
        }
        item {
            DebugManualCommandSection(
                value = state.manualHex,
                onValueChange = onUpdateManualHex,
                onSend = onSendManual,
            )
        }
    }
}

@Composable
private fun DebugSessionSection(
    state: DebugHomeState,
    onModeSelected: (SessionTransportMode) -> Unit,
    onVerifyModeSelected: (Int) -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDisconnect: () -> Unit,
    onPermissionAction: () -> Unit,
) {
    val isBusy = state.busyAction != null
    val permissionsReady = state.prerequisites.bluetoothPermissionsGranted && state.prerequisites.locationPermissionGranted
    val canScan = state.selectedMode == SessionTransportMode.FAKE || (permissionsReady && state.sessionStatus.bluetoothEnabled)
    val permissionCta = state.permissionCta

    HomeSectionCard(title = "Transport & Session") {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SessionTransportMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = state.selectedMode == mode,
                    onClick = rememberHapticClick(
                        kind = HomeHapticKind.NAVIGATION,
                        onClick = { onModeSelected(mode) },
                    ),
                    enabled = !isBusy,
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = SessionTransportMode.entries.size),
                ) {
                    Text(if (mode == SessionTransportMode.FAKE) "Simulate" else "Real Device")
                }
            }
        }
        if (state.selectedMode == SessionTransportMode.REAL && permissionCta != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Real device mode prerequisites not met, need to resolve permissions or system settings first.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                HomeFilledButton(onClick = onPermissionAction) {
                    Text(permissionCta.label)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SummaryPill(modifier = Modifier.weight(1f), title = "Connect", value = debugConnectionSummary(state))
            SummaryPill(modifier = Modifier.weight(1f), title = "Handshake", value = debugHandshakeLabel(state.sessionStatus.handshakeStage))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SummaryPill(modifier = Modifier.weight(1f), title = "Device", value = debugConsoleConnectedDeviceName(state))
            SummaryPill(
                modifier = Modifier.weight(1f),
                title = "Capability",
                value = debugConsoleConnectedCapabilityLabel(state),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SummaryPill(
                modifier = Modifier.weight(1f),
                title = "Broadcast Wake",
                value = if (state.sessionStatus.wakeAdvertisingSupported) "Supported" else "Not Supported",
            )
            SummaryPill(
                modifier = Modifier.weight(1f),
                title = "Protocol Family",
                value = state.sessionStatus.connectedProfile?.protocolFamily?.name ?: "-",
            )
        }
        if (state.selectedMode == SessionTransportMode.REAL) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HomeOutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onVerifyModeSelected(0) },
                    enabled = !isBusy && state.sessionStatus.handshakeVerifyMode != 0,
                ) {
                    Text("Paired(0)")
                }
                HomeOutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onVerifyModeSelected(1) },
                    enabled = !isBusy && state.sessionStatus.handshakeVerifyMode != 1,
                ) {
                    Text("Pairing(1)")
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HomeFilledButton(modifier = Modifier.weight(1f), onClick = onStartScan, enabled = !isBusy && !state.sessionStatus.scanning && canScan) {
                Text("Scan")
            }
            HomeOutlinedButton(modifier = Modifier.weight(1f), onClick = onStopScan, enabled = !isBusy && state.sessionStatus.scanning) {
                Text("Stop")
            }
            HomeOutlinedButton(modifier = Modifier.weight(1f), onClick = onDisconnect, enabled = !isBusy && state.sessionStatus.connectedDevice != null) {
                Text("Disconnect")
            }
        }
    }
}

@Composable
private fun DebugDeviceListSection(
    state: DebugHomeState,
    onFilterChange: (String) -> Unit,
    onConnect: (SessionDevice) -> Unit,
) {
    val isBusy = state.busyAction != null
    val deviceRows = remember(state.discoveredDevices, state.sessionStatus.connectedDevice, state.sessionStatus.connectedProfile) {
        mapDebugConsoleDeviceRows(state)
    }
    val filteredDevices = remember(deviceRows, state.deviceFilterQuery, state.discoveredDevices) {
        val filteredMacs = filterDiscoveredDevices(state.discoveredDevices, state.deviceFilterQuery)
            .map(SessionDevice::macAddress)
            .toSet()
        deviceRows.filter { it.macAddress in filteredMacs }
    }
    HomeSectionCard(title = "Device List") {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.deviceFilterQuery,
            onValueChange = onFilterChange,
            label = { Text("Filter Devices") },
            placeholder = { Text("Search by Name or MAC") },
            singleLine = true,
            supportingText = {
                val summary = if (state.deviceFilterQuery.isBlank()) {
                    "Found ${state.discoveredDevices.size} devices"
                } else {
                    "Matched ${filteredDevices.size} / ${state.discoveredDevices.size}"
                }
                Text(summary)
            },
        )
        if (state.discoveredDevices.isEmpty()) {
            Text(
                text = when {
                    state.sessionStatus.scanning -> "Scanning..."
                    state.selectedMode == SessionTransportMode.REAL && state.permissionCta != null -> "Complete permissions and system prerequisites first"
                    else -> "No Devices"
                },
                style = MaterialTheme.typography.bodySmall,
            )
        } else if (filteredDevices.isEmpty()) {
            Text(
                text = "No matching devices",
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            filteredDevices.forEach { device ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(device.name, fontWeight = FontWeight.Medium)
                        Text(
                            text = "${device.macAddress} · ${device.capabilityLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HomeFilledButton(
                        onClick = {
                            state.discoveredDevices.firstOrNull { it.macAddress == device.macAddress }?.let(onConnect)
                        },
                        enabled = !isBusy && !device.isConnected,
                    ) {
                        Text(if (device.isConnected) "Connected" else "Connect")
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugCommandsSection(
    state: DebugHomeState,
    uiModel: DebugConsoleUiModel,
    onVersion: () -> Unit,
    onReboot: () -> Unit,
    onToggleRecord: () -> Unit,
    onSwitchMode: (Int) -> Unit,
    onSubscribe: () -> Unit,
    onSleep: () -> Unit,
    onWake: () -> Unit,
    onWakeAndSnapshot: () -> Unit,
    onRecordKeyClick: () -> Unit,
    onQsKeyClick: () -> Unit,
    onSnapshotKeyClick: () -> Unit,
    onPushGps: () -> Unit,
    onSetGpsFrequency: (Int) -> Unit,
    onSetLocationRequestFrequency: (Int) -> Unit,
    onSetGpsSyncEnabled: (Boolean) -> Unit,
) {
    val onDebugAction = { action: DebugConsoleActionUiModel ->
        handleDebugAction(
            action = action,
            onVersion = onVersion,
            onReboot = onReboot,
            onToggleRecord = onToggleRecord,
            onSubscribe = onSubscribe,
            onSleep = onSleep,
            onWake = onWake,
            onWakeAndSnapshot = onWakeAndSnapshot,
            onRecordKeyClick = onRecordKeyClick,
            onQsKeyClick = onQsKeyClick,
            onSnapshotKeyClick = onSnapshotKeyClick,
            onSwitchMode = onSwitchMode,
        )
    }

    HomeSectionCard(title = "Commands & GPS") {
        uiModel.capabilityNotice?.let { notice ->
            Text(
                text = notice,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (uiModel.coreActions.isNotEmpty()) {
            DebugButtonGrid(
                actions = uiModel.coreActions,
                onAction = onDebugAction,
            )
        }

        if (uiModel.modeActions.isNotEmpty()) {
            DebugButtonGrid(
                actions = uiModel.modeActions,
                onAction = onDebugAction,
            )
        }

        if (uiModel.auxiliaryActions.isNotEmpty()) {
            DebugButtonGrid(
                actions = uiModel.auxiliaryActions,
                onAction = onDebugAction,
            )
        }

        if (uiModel.keyActions.isNotEmpty()) {
            DebugButtonGrid(
                actions = uiModel.keyActions,
                onAction = onDebugAction,
            )
        }

        if (uiModel.showGpsSection) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SummaryPill(modifier = Modifier.weight(1f), title = "GPS Auto Sync", value = if (state.sessionStatus.gpsAutoPushEnabled) "Turn On" else "Close")
                SummaryPill(modifier = Modifier.weight(1f), title = "Location Request", value = "${state.sessionStatus.gpsLocationRequestHz}Hz")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Continuous Sync")
                Switch(
                    checked = state.sessionStatus.gpsAutoPushEnabled,
                    onCheckedChange = onSetGpsSyncEnabled,
                    enabled = uiModel.gpsControlsEnabled,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf(1, 2, 5, 10).forEach { hz ->
                    HomeOutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onSetGpsFrequency(hz) },
                        enabled = uiModel.gpsControlsEnabled,
                    ) {
                        Text("${hz}Hz")
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf(1, 2, 5).forEach { hz ->
                    HomeOutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onSetLocationRequestFrequency(hz) },
                        enabled = uiModel.gpsControlsEnabled,
                    ) {
                        Text("Location ${hz}Hz")
                    }
                }
            }
            HomeFilledButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onPushGps,
                enabled = uiModel.gpsControlsEnabled,
            ) {
                Text("Push Mock GPS")
            }
        }

        if (
            uiModel.coreActions.isEmpty() &&
            uiModel.modeActions.isEmpty() &&
            uiModel.auxiliaryActions.isEmpty() &&
            uiModel.keyActions.isEmpty() &&
            !uiModel.showGpsSection
        ) {
            Text(
                text = "No verified preset debug commands available, please use log and manual messaging.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DebugStatusSection(state: DebugHomeState) {
    HomeSectionCard(title = "Parsing Status") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SummaryPill(modifier = Modifier.weight(1f), title = "Last Push", value = state.cameraStatus.lastPushCommandId)
            SummaryPill(modifier = Modifier.weight(1f), title = "Recording", value = if (state.cameraStatus.recording) "Yes" else "No")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SummaryPill(modifier = Modifier.weight(1f), title = "Mode", value = state.cameraStatus.modeName.ifBlank { state.cameraStatus.modeLabel })
            SummaryPill(modifier = Modifier.weight(1f), title = "Status", value = state.cameraStatus.stateLabel)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SummaryPill(modifier = Modifier.weight(1f), title = "Record Result", value = state.sessionStatus.lastRecordResult ?: "-")
            SummaryPill(modifier = Modifier.weight(1f), title = "Wake Result", value = state.sessionStatus.lastWakeResult ?: "-")
        }
        Text(
            text = state.cameraStatus.lastPushSummary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DebugLogsSection(
    groups: List<DebugLogGroup>,
    onClearLogs: () -> Unit,
    onCopyRecent: () -> Unit,
) {
    HomeSectionCard(title = "Log") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HomeOutlinedButton(modifier = Modifier.weight(1f), onClick = onClearLogs) {
                Text("Clear Display")
            }
            HomeOutlinedButton(modifier = Modifier.weight(1f), onClick = onCopyRecent, enabled = groups.isNotEmpty()) {
                Text("Copy Latest Log")
            }
        }
        if (groups.isEmpty()) {
            Text("No Logs", style = MaterialTheme.typography.bodySmall)
        } else {
            groups.forEach { group ->
                var expanded by rememberSaveable(group.category.name) { mutableStateOf(group.expandedByDefault) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "${group.category.name} (${group.entries.size}) ${if (expanded) "Collapse" else "Expand"}",
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (expanded) {
                        group.entries.take(20).forEach { entry ->
                            Text(
                                text = "[${entry.category.name}] ${entry.message}${entry.hex?.let { " | $it" } ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugManualCommandSection(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val validation = remember(value) { validateManualHexInput(value) }
    val showError = value.isNotBlank() && !validation.isValid

    HomeSectionCard(title = "Manual Command") {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            label = { Text("HEX") },
            isError = showError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            supportingText = {
                if (showError) {
                    Text(validation.errorMessage.orEmpty())
                } else {
                    Text("Supports space separation, will be normalized automatically before sending.")
                }
            },
        )
        HomeFilledButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val normalized = validation.normalizedHex ?: return@HomeFilledButton
                onValueChange(normalized)
                onSend()
            },
            enabled = validation.isValid,
        ) {
            Text("Send")
        }
    }
}

@Composable
private fun DebugButtonGrid(
    actions: List<DebugConsoleActionUiModel>,
    onAction: (DebugConsoleActionUiModel) -> Unit,
) {
    actions.chunked(3).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            row.forEach { action ->
                HomeOutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onAction(action) },
                    enabled = action.enabled,
                ) {
                    Text(action.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

private fun handleDebugAction(
    action: DebugConsoleActionUiModel,
    onVersion: () -> Unit,
    onReboot: () -> Unit,
    onToggleRecord: () -> Unit,
    onSubscribe: () -> Unit,
    onSleep: () -> Unit,
    onWake: () -> Unit,
    onWakeAndSnapshot: () -> Unit,
    onRecordKeyClick: () -> Unit,
    onQsKeyClick: () -> Unit,
    onSnapshotKeyClick: () -> Unit,
    onSwitchMode: (Int) -> Unit,
) {
    when (action.actionType) {
        DebugConsoleActionType.VERSION_QUERY -> onVersion()
        DebugConsoleActionType.REBOOT -> onReboot()
        DebugConsoleActionType.TOGGLE_RECORD -> onToggleRecord()
        DebugConsoleActionType.SUBSCRIBE_STATUS -> onSubscribe()
        DebugConsoleActionType.SLEEP -> onSleep()
        DebugConsoleActionType.WAKE -> onWake()
        DebugConsoleActionType.SWITCH_MODE -> onSwitchMode(action.mode ?: return)
        DebugConsoleActionType.WAKE_AND_SNAPSHOT -> onWakeAndSnapshot()
        DebugConsoleActionType.RECORD_KEY -> onRecordKeyClick()
        DebugConsoleActionType.QS_KEY -> onQsKeyClick()
        DebugConsoleActionType.SNAPSHOT_KEY -> onSnapshotKeyClick()
    }
}

private fun debugConnectionSummary(state: DebugHomeState): String = when {
    state.sessionStatus.protocolReady -> "Protocol Connected"
    state.sessionStatus.connectedDevice != null -> "Bluetooth Connected"
    state.sessionStatus.scanning -> "Scanning"
    else -> "Not Connected"
}

private fun debugHandshakeLabel(stage: HandshakeStage): String = when (stage) {
    HandshakeStage.IDLE -> "Idle"
    HandshakeStage.REQUEST_SENT -> "Request Sent"
    HandshakeStage.CAMERA_CONFIRMATION_RECEIVED -> "Camera Confirmed"
    HandshakeStage.COMPLETED -> "Complete"
    HandshakeStage.REJECTED -> "Rejected"
}
