package com.alliot.osmo.demo.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.alliot.osmo.demo.session.model.HandshakeStage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkbenchSheets(
    state: DebugHomeState,
    isLandscape: Boolean,
    onDismiss: () -> Unit,
    onSwitchMode: (Int) -> Unit,
    onSetGpsAutoPushEnabled: (Boolean) -> Unit,
    onSetGpsAutoPushFrequencyHz: (Int) -> Unit,
    onSetGpsLocationRequestFrequencyHz: (Int) -> Unit,
    onPushSampleGps: () -> Unit,
    onSleep: () -> Unit,
    onWake: () -> Unit,
    onWakeAndSnapshot: () -> Unit,
    onRequestVersion: () -> Unit,
    onSendQsKeyClick: () -> Unit,
    onSendSnapshotKeyClick: () -> Unit,
    onPermissionAction: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onUpdateDeviceFilterQuery: (String) -> Unit,
    onSelectConnectionDevice: (Long) -> Unit,
    onConfirmConnection: () -> Unit,
    onDisconnect: () -> Unit,
    onRetryConnectionScan: () -> Unit,
    onNavigateToDebugConsole: () -> Unit,
) {
    val openSheet = state.openSheet ?: return
    val content: @Composable () -> Unit = {
        when (openSheet) {
            HomeSheet.MODE -> ModeSheet(state = state, onSwitchMode = onSwitchMode)
            HomeSheet.GPS -> GpsSheet(
                state = state,
                onSetGpsAutoPushEnabled = onSetGpsAutoPushEnabled,
                onSetGpsAutoPushFrequencyHz = onSetGpsAutoPushFrequencyHz,
                onSetGpsLocationRequestFrequencyHz = onSetGpsLocationRequestFrequencyHz,
                onPushSampleGps = onPushSampleGps,
                onPermissionAction = onPermissionAction,
            )
            HomeSheet.DEVICE_ACTIONS -> DeviceActionsSheet(
                state = state,
                onSleep = onSleep,
                onWake = onWake,
                onWakeAndSnapshot = onWakeAndSnapshot,
                onRequestVersion = onRequestVersion,
                onSendQsKeyClick = onSendQsKeyClick,
                onSendSnapshotKeyClick = onSendSnapshotKeyClick,
            )
            HomeSheet.CONNECTION -> ConnectionSheet(
                model = state.workbenchConnectionSheetUiModel,
                onStartScan = onStartScan,
                onStopScan = onStopScan,
                onDeviceFilterQueryChange = onUpdateDeviceFilterQuery,
                onSelectDevice = onSelectConnectionDevice,
                onConfirmConnection = onConfirmConnection,
                onDisconnect = onDisconnect,
                onRetryConnectionScan = onRetryConnectionScan,
                onPermissionAction = onPermissionAction,
                onNavigateToDebugConsole = onNavigateToDebugConsole,
                onDismiss = onDismiss,
            )
        }
    }

    if (isLandscape) {
        Dialog(onDismissRequest = onDismiss) {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    content()
                }
            }
        }
    } else {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ModeSheet(
    state: DebugHomeState,
    onSwitchMode: (Int) -> Unit,
) {
    val modeOptions = state.workbenchUiModel.modeOptions
    Text("Switch Mode", style = MaterialTheme.typography.titleLarge)
    Text(
        text = state.workbenchUiModel.modeSwitchBlockedReason ?: if (modeOptions.isNotEmpty()) {
            "Current device supports ${modeOptions.size} modes."
        } else {
            "Current device does not support mode switching."
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (modeOptions.isEmpty()) {
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        modeOptions.chunked(2).forEach { rowModes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowModes.forEach { option ->
                    HomeFilledButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onSwitchMode(option.mode) },
                        enabled = state.workbenchUiModel.modeSwitchEnabled,
                    ) {
                        Text(option.label)
                    }
                }
                if (rowModes.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GpsSheet(
    state: DebugHomeState,
    onSetGpsAutoPushEnabled: (Boolean) -> Unit,
    onSetGpsAutoPushFrequencyHz: (Int) -> Unit,
    onSetGpsLocationRequestFrequencyHz: (Int) -> Unit,
    onPushSampleGps: () -> Unit,
    onPermissionAction: () -> Unit,
) {
    val busy = state.busyAction != null
    val gpsControlsEnabled = state.workbenchUiModel.gpsActionEnabled && !busy
    Text("GPS", style = MaterialTheme.typography.titleLarge)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Auto Sync")
            Text(
                text = if (state.sessionStatus.gpsAutoPushEnabled) {
                    "Continuous push at ${state.sessionStatus.gpsAutoPushHz}Hz"
                } else {
                    state.workbenchUiModel.gpsActionDisabledReason ?: "Closed"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = state.sessionStatus.gpsAutoPushEnabled,
            onCheckedChange = { if (gpsControlsEnabled) onSetGpsAutoPushEnabled(it) },
            enabled = gpsControlsEnabled,
        )
    }
    SummaryPill(
        title = "Latest Coordinates",
        value = state.sessionStatus.lastGpsCoordinate ?: "No Location",
    )
    Text(
        text = state.sessionStatus.lastGpsResult ?: "Waiting for first location",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    mapGpsDetailItems(state.sessionStatus).chunked(2).forEach { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            rowItems.forEach { item ->
                SummaryPill(
                    modifier = Modifier.weight(1f),
                    title = item.title,
                    value = item.value,
                )
            }
            if (rowItems.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HomeOutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { if (gpsControlsEnabled) onSetGpsAutoPushFrequencyHz(10) },
            enabled = gpsControlsEnabled,
        ) {
            Text("Sync 10Hz")
        }
        HomeOutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { if (gpsControlsEnabled) onSetGpsLocationRequestFrequencyHz(1) },
            enabled = gpsControlsEnabled,
        ) {
            Text("Location 1Hz")
        }
    }
    HomeFilledButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onPushSampleGps,
        enabled = gpsControlsEnabled,
    ) {
        Text("Push Mock GPS")
    }
    val permissionCta = state.permissionCta
    if (permissionCta != null) {
        HomeOutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onPermissionAction,
        ) {
            Text(permissionCta.label)
        }
    }
}

@Composable
private fun DeviceActionsSheet(
    state: DebugHomeState,
    onSleep: () -> Unit,
    onWake: () -> Unit,
    onWakeAndSnapshot: () -> Unit,
    onRequestVersion: () -> Unit,
    onSendQsKeyClick: () -> Unit,
    onSendSnapshotKeyClick: () -> Unit,
) {
    Text("Device Actions", style = MaterialTheme.typography.titleLarge)
    Text(
        text = "Low frequency or high risk operations are grouped in sub-panels to avoid being adjacent to the main record button.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val model = state.workbenchUiModel.deviceActionsUiModel
    val visibleActions = buildList<@Composable () -> Unit> {
        if (model.showSleepAction) {
            add {
                HomeFilledButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSleep,
                    kind = HomeHapticKind.DANGER,
                    enabled = model.deviceActionsEnabled,
                ) { Text("Sleep") }
            }
        }
        if (model.showWakeAction) {
            add {
                HomeFilledButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onWake,
                    enabled = model.wakeActionsEnabled,
                ) { Text("Wake") }
            }
        }
        if (model.showVersionAction) {
            add {
                HomeOutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRequestVersion,
                    enabled = model.deviceActionsEnabled,
                ) { Text("Version") }
            }
        }
        if (model.showWakeAndSnapshotAction) {
            add {
                HomeOutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onWakeAndSnapshot,
                    enabled = model.wakeActionsEnabled,
                ) { Text("Wake and Snap") }
            }
        }
        if (model.showQsAction) {
            add {
                HomeOutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSendQsKeyClick,
                    enabled = model.deviceActionsEnabled,
                ) { Text("QS") }
            }
        }
        if (model.showSnapshotAction) {
            add {
                HomeOutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSendSnapshotKeyClick,
                    enabled = model.deviceActionsEnabled,
                ) { Text("Sleep Quick Snapshot") }
            }
        }
    }
    if (visibleActions.isEmpty()) {
        Text(
            text = "Current device does not support Workbench actions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    visibleActions.chunked(2).forEach { rowActions ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            rowActions.forEach { action ->
                Box(modifier = Modifier.weight(1f)) { action() }
            }
            if (rowActions.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ConnectionSheet(
    model: WorkbenchConnectionSheetUiModel,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceFilterQueryChange: (String) -> Unit,
    onSelectDevice: (Long) -> Unit,
    onConfirmConnection: () -> Unit,
    onDisconnect: () -> Unit,
    onRetryConnectionScan: () -> Unit,
    onPermissionAction: () -> Unit,
    onNavigateToDebugConsole: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Connect Device", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = connectionSheetSubtitle(model.phase),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        model.banner?.let { banner ->
            ConnectionBanner(
                banner = banner,
                onPermissionAction = onPermissionAction,
                onRetryConnectionScan = onRetryConnectionScan,
            )
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = model.filterQuery,
            onValueChange = onDeviceFilterQueryChange,
            label = { Text("Filter Devices") },
            placeholder = { Text("Search by Name or MAC") },
            singleLine = true,
            supportingText = {
                val copy = if (model.filterQuery.isBlank()) {
                    "Found ${model.totalDeviceCount} devices"
                } else {
                    "Matched ${model.filteredDeviceCount} / ${model.totalDeviceCount}"
                }
                Text(copy)
            },
        )

        val permissionBlocked = model.banner?.type == WorkbenchConnectionBannerType.PERMISSION
        val showEmptyState = model.deviceRows.isEmpty() &&
            model.phase == WorkbenchConnectionPhase.FAILURE &&
            !permissionBlocked
        val showFilteredEmptyState = model.deviceRows.isEmpty() &&
            model.filterQuery.isNotBlank() &&
            model.totalDeviceCount > 0 &&
            !permissionBlocked
        when {
            permissionBlocked -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Scanning will start after Bluetooth/Location permissions are granted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            showFilteredEmptyState -> ConnectionSheetEmptyState(
                title = "No matching devices",
                detail = "Adjust filter terms, or keep scanning to find more devices",
                hint = "Supports matching by device name and MAC address",
            )
            showEmptyState -> ConnectionSheetEmptyState()
            else -> LazyColumn(
                modifier = Modifier.heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items = model.deviceRows, key = WorkbenchConnectionDeviceRowUiModel::deviceKey) { row ->
                    ConnectionDeviceRow(
                        row = row,
                        enabled = model.primaryAction != WorkbenchConnectionPrimaryAction.PROCESSING,
                        onSelect = { onSelectDevice(row.deviceId) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val primaryEnabled = model.primaryAction != WorkbenchConnectionPrimaryAction.PROCESSING &&
            (model.primaryAction != WorkbenchConnectionPrimaryAction.CONNECT_DEVICE || model.selectedDeviceId != null)

        HomeFilledButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = {
                when (model.primaryAction) {
                    WorkbenchConnectionPrimaryAction.START_SCAN -> onStartScan()
                    WorkbenchConnectionPrimaryAction.STOP_SCAN -> onStopScan()
                    WorkbenchConnectionPrimaryAction.CONNECT_DEVICE -> onConfirmConnection()
                    WorkbenchConnectionPrimaryAction.DISCONNECT -> onDisconnect()
                    WorkbenchConnectionPrimaryAction.PROCESSING -> Unit
                    WorkbenchConnectionPrimaryAction.RETRY -> onRetryConnectionScan()
                    WorkbenchConnectionPrimaryAction.PERMISSION -> onPermissionAction()
                }
            },
            enabled = primaryEnabled,
        ) {
            Text(model.primaryActionLabel, style = MaterialTheme.typography.titleMedium)
        }

        HomeOutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onDismiss()
                onNavigateToDebugConsole()
            },
            kind = HomeHapticKind.NAVIGATION,
        ) {
            Text("Go to Debug Console")
        }
    }
}

@Composable
private fun ConnectionSheetEmptyState() {
    ConnectionSheetEmptyState(
        title = "No devices found yet",
        detail = "Make sure the device is turned on and close to the phone",
        hint = "Use the action below to rescan",
    )
}

@Composable
private fun ConnectionSheetEmptyState(
    title: String,
    detail: String,
    hint: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConnectionDeviceRow(
    row: WorkbenchConnectionDeviceRowUiModel,
    enabled: Boolean,
    onSelect: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = rememberHapticClick(
                    kind = HomeHapticKind.NAVIGATION,
                    onClick = onSelect,
                ),
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (row.isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = row.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                row.statusLabel?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = "Suffix ${row.macSuffix}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ConnectionBanner(
    banner: WorkbenchConnectionBannerUiModel,
    onPermissionAction: () -> Unit,
    onRetryConnectionScan: () -> Unit,
) {
    val backgroundColor = when (banner.type) {
        WorkbenchConnectionBannerType.PERMISSION,
        WorkbenchConnectionBannerType.ERROR -> MaterialTheme.colorScheme.errorContainer
        WorkbenchConnectionBannerType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when (banner.type) {
        WorkbenchConnectionBannerType.PERMISSION,
        WorkbenchConnectionBannerType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        WorkbenchConnectionBannerType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val action = when {
        banner.permissionAction != null -> onPermissionAction
        banner.type == WorkbenchConnectionBannerType.ERROR -> onRetryConnectionScan
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = banner.message,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
        )
        banner.actionLabel?.let { label ->
            action?.let {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = contentColor,
                    modifier = Modifier.clickable(
                        onClick = rememberHapticClick(kind = HomeHapticKind.SECONDARY, onClick = it),
                    ),
                )
            }
        }
    }
}

private fun connectionSheetSubtitle(phase: WorkbenchConnectionPhase): String {
    return when (phase) {
        WorkbenchConnectionPhase.IDLE -> "Select a nearby device to start connecting"
        WorkbenchConnectionPhase.SCANNING -> "Updating nearby devices"
        WorkbenchConnectionPhase.CONNECTING -> "Establishing connection with device"
        WorkbenchConnectionPhase.PREPARING -> "Device connected, completing preparation"
        WorkbenchConnectionPhase.READY -> "Current device is ready for control"
        WorkbenchConnectionPhase.FAILURE -> "Connection Incomplete"
    }
}
