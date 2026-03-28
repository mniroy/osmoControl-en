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
    Text("模式切换", style = MaterialTheme.typography.titleLarge)
    Text(
        text = state.workbenchUiModel.modeSwitchBlockedReason ?: if (modeOptions.isNotEmpty()) {
            "当前设备支持 ${modeOptions.size} 种模式。"
        } else {
            "当前设备未开放模式切换。"
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
            Text("自动同步")
            Text(
                text = if (state.sessionStatus.gpsAutoPushEnabled) {
                    "${state.sessionStatus.gpsAutoPushHz}Hz 持续推送"
                } else {
                    state.workbenchUiModel.gpsActionDisabledReason ?: "已关闭"
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
        title = "最新坐标",
        value = state.sessionStatus.lastGpsCoordinate ?: "暂无定位",
    )
    Text(
        text = state.sessionStatus.lastGpsResult ?: "等待首次定位",
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
            Text("同步 10Hz")
        }
        HomeOutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { if (gpsControlsEnabled) onSetGpsLocationRequestFrequencyHz(1) },
            enabled = gpsControlsEnabled,
        ) {
            Text("定位 1Hz")
        }
    }
    HomeFilledButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onPushSampleGps,
        enabled = gpsControlsEnabled,
    ) {
        Text("推送示例 GPS")
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
    Text("设备动作", style = MaterialTheme.typography.titleLarge)
    Text(
        text = "把低频或高风险操作收进次级面板，避免与主录制按钮相邻。",
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
                ) { Text("休眠") }
            }
        }
        if (model.showWakeAction) {
            add {
                HomeFilledButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onWake,
                    enabled = model.wakeActionsEnabled,
                ) { Text("唤醒") }
            }
        }
        if (model.showVersionAction) {
            add {
                HomeOutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRequestVersion,
                    enabled = model.deviceActionsEnabled,
                ) { Text("版本") }
            }
        }
        if (model.showWakeAndSnapshotAction) {
            add {
                HomeOutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onWakeAndSnapshot,
                    enabled = model.wakeActionsEnabled,
                ) { Text("唤醒并快拍") }
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
                ) { Text("休眠快拍") }
            }
        }
    }
    if (visibleActions.isEmpty()) {
        Text(
            text = "当前设备未开放 Workbench 设备动作。",
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
                Text("连接设备", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = connectionSheetSubtitle(model.phase),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "关闭")
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
            label = { Text("筛选设备") },
            placeholder = { Text("按名称或 MAC 搜索") },
            singleLine = true,
            supportingText = {
                val copy = if (model.filterQuery.isBlank()) {
                    "已发现 ${model.totalDeviceCount} 台设备"
                } else {
                    "匹配 ${model.filteredDeviceCount} / ${model.totalDeviceCount}"
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
                    text = "完成蓝牙/定位授权后即可扫描设备。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            showFilteredEmptyState -> ConnectionSheetEmptyState(
                title = "没有匹配的设备",
                detail = "调整筛选词，或继续扫描等待更多设备出现",
                hint = "支持按设备名和 MAC 地址匹配",
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
            Text("前往调试台")
        }
    }
}

@Composable
private fun ConnectionSheetEmptyState() {
    ConnectionSheetEmptyState(
        title = "暂未发现设备",
        detail = "确认设备已开机并靠近手机",
        hint = "使用底部操作重新扫描",
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
                text = "尾号 ${row.macSuffix}",
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
        WorkbenchConnectionPhase.IDLE -> "选择附近设备开始连接"
        WorkbenchConnectionPhase.SCANNING -> "正在更新附近设备"
        WorkbenchConnectionPhase.CONNECTING -> "正在与设备建立连接"
        WorkbenchConnectionPhase.PREPARING -> "设备已连接，正在完成准备"
        WorkbenchConnectionPhase.READY -> "当前设备已可控制"
        WorkbenchConnectionPhase.FAILURE -> "连接未完成"
    }
}
