package com.alliot.osmo.demo.app.ui.home

import com.alliot.osmo.demo.session.log.SessionLogEntry
import com.alliot.osmo.demo.session.model.CameraStatusSnapshot
import com.alliot.osmo.demo.session.model.HandshakeStage
import com.alliot.osmo.demo.session.model.LogCategory
import com.alliot.osmo.demo.session.model.ProtocolFamily
import com.alliot.osmo.demo.session.model.SessionDevice
import com.alliot.osmo.demo.session.model.SessionStatus
import com.alliot.osmo.demo.session.model.SessionTransportMode
import java.util.Locale

private const val MODE_SWITCH_BLOCKED_RECORDING = "录制中"
private const val MODE_SWITCH_BLOCKED_NOT_CONNECTED = "请先连接设备"
private const val MODE_SWITCH_BLOCKED_SCANNING = "正在搜索设备"
private const val MODE_SWITCH_BLOCKED_HANDSHAKE = "设备准备中"
private const val MODE_SWITCH_BLOCKED_SLEEPING = "设备已休眠"
private const val MODE_SWITCH_BLOCKED_UNSUPPORTED = "当前设备仅支持调试控制"
private const val RECORD_BLOCKED_NOT_CONNECTED = "请先连接设备"
private const val RECORD_BLOCKED_SCANNING = "正在搜索设备"
private const val RECORD_BLOCKED_HANDSHAKE = "设备准备中"
private const val RECORD_BLOCKED_SLEEPING = "设备已休眠"
private const val RECORD_BLOCKED_UNSUPPORTED = "当前设备仅支持调试控制"
private const val GPS_BLOCKED_NOT_CONNECTED = "请先连接设备"
private const val GPS_BLOCKED_SCANNING = "正在搜索设备"
private const val GPS_BLOCKED_HANDSHAKE = "设备准备中"
private const val GPS_BLOCKED_SLEEPING = "设备已休眠"
private const val GPS_BLOCKED_UNSUPPORTED = "当前设备仅支持调试控制"
private const val CAMERA_MODE_PHOTO = 0x05

internal fun isPhotoCaptureMode(snapshot: CameraStatusSnapshot): Boolean = snapshot.mode == CAMERA_MODE_PHOTO

internal fun primaryActionButtonLabel(snapshot: CameraStatusSnapshot, enabled: Boolean): String {
    return when {
        !enabled -> "未就绪"
        isPhotoCaptureMode(snapshot) && snapshot.recording -> "拍照中"
        isPhotoCaptureMode(snapshot) -> "拍照"
        snapshot.recording -> "停止"
        else -> "录制"
    }
}

internal fun primaryActionContentDescription(snapshot: CameraStatusSnapshot): String {
    return when {
        isPhotoCaptureMode(snapshot) && snapshot.recording -> "拍照中"
        isPhotoCaptureMode(snapshot) -> "拍照"
        snapshot.recording -> "停止录制"
        else -> "开始录制"
    }
}

internal fun primaryActionStatusText(state: DebugHomeState): String {
    val snapshot = state.cameraStatus
    return when {
        isPhotoCaptureMode(snapshot) && snapshot.recording -> "拍照中"
        snapshot.recording -> "录制中"
        state.sessionStatus.sleeping -> "设备休眠"
        state.sessionStatus.protocolReady -> "控制就绪"
        state.sessionStatus.connectedDevice != null -> "设备准备中"
        state.sessionStatus.scanning -> "搜索中"
        else -> "待命"
    }
}

internal fun primaryActionHelperText(state: DebugHomeState): String {
    val snapshot = state.cameraStatus
    val workbench = state.workbenchUiModel
    return when {
        isPhotoCaptureMode(snapshot) && snapshot.recording -> "拍照进行中，请等待设备返回预览状态。"
        snapshot.recording -> "录制进行中，模式切换已锁定。"
        state.sessionStatus.sleeping -> "设备已休眠，请先唤醒。"
        workbench.recordActionDisabledReason != null -> workbench.recordActionDisabledReason
        workbench.modeSwitchBlockedReason != null -> "模式切换：${workbench.modeSwitchBlockedReason}"
        else -> "主操作保持在页面中心，适合默认遥控路径。"
    }
}

fun mapWorkbenchUiModel(state: DebugHomeState): WorkbenchUiModel {
    val status = state.sessionStatus
    val connected = status.connectedDevice != null
    val protocolReady = status.protocolReady
    val sleeping = status.sleeping
    val recording = state.cameraStatus.recording
    val capabilities = status.connectedProfile?.capabilities
    val debugOnlyProfile = capabilities != null && !capabilities.supportsWorkbench

    val connectionSummary = when {
        sleeping -> "设备已休眠"
        protocolReady -> "控制已就绪"
        connected -> "设备已连接"
        status.scanning -> "正在搜索设备"
        else -> "未连接设备"
    }

    val recordActionEnabled = protocolReady && !sleeping && (capabilities?.supportsRecordKey ?: true) && !debugOnlyProfile
    val recordActionDisabledReason = when {
        recordActionEnabled -> null
        debugOnlyProfile -> RECORD_BLOCKED_UNSUPPORTED
        sleeping -> RECORD_BLOCKED_SLEEPING
        connected -> RECORD_BLOCKED_HANDSHAKE
        status.scanning -> RECORD_BLOCKED_SCANNING
        else -> RECORD_BLOCKED_NOT_CONNECTED
    }

    val gpsActionEnabled = protocolReady && !sleeping && (capabilities?.supportsGpsPush ?: true) && !debugOnlyProfile
    val gpsActionDisabledReason = when {
        gpsActionEnabled -> null
        debugOnlyProfile -> GPS_BLOCKED_UNSUPPORTED
        sleeping -> GPS_BLOCKED_SLEEPING
        connected -> GPS_BLOCKED_HANDSHAKE
        status.scanning -> GPS_BLOCKED_SCANNING
        else -> GPS_BLOCKED_NOT_CONNECTED
    }

    val modeSwitchEnabled = protocolReady && !sleeping && !recording && (capabilities?.supportsModeSwitch ?: true) && !debugOnlyProfile
    val modeSwitchBlockedReason = when {
        recording -> MODE_SWITCH_BLOCKED_RECORDING
        debugOnlyProfile -> MODE_SWITCH_BLOCKED_UNSUPPORTED
        sleeping -> MODE_SWITCH_BLOCKED_SLEEPING
        !protocolReady && connected -> MODE_SWITCH_BLOCKED_HANDSHAKE
        !protocolReady && status.scanning -> MODE_SWITCH_BLOCKED_SCANNING
        !protocolReady -> MODE_SWITCH_BLOCKED_NOT_CONNECTED
        else -> null
    }

    val modeOptions = mapWorkbenchModeOptions(capabilities?.supportedModes)
    val deviceActionsUiModel = mapWorkbenchDeviceActionsUiModel(state, debugOnlyProfile)
    val showModeQuickAction = !debugOnlyProfile && modeOptions.isNotEmpty()
    val showGpsQuickAction = !debugOnlyProfile && (capabilities?.supportsGpsPush ?: true)
    val showDeviceActionsQuickAction = deviceActionsUiModel.showSleepAction ||
        deviceActionsUiModel.showWakeAction ||
        deviceActionsUiModel.showWakeAndSnapshotAction ||
        deviceActionsUiModel.showVersionAction ||
        deviceActionsUiModel.showQsAction ||
        deviceActionsUiModel.showSnapshotAction

    return WorkbenchUiModel(
        connectionSummary = connectionSummary,
        recordActionEnabled = recordActionEnabled,
        recordActionDisabledReason = recordActionDisabledReason,
        gpsActionEnabled = gpsActionEnabled,
        gpsActionDisabledReason = gpsActionDisabledReason,
        modeSwitchEnabled = modeSwitchEnabled,
        modeSwitchBlockedReason = modeSwitchBlockedReason,
        showModeQuickAction = showModeQuickAction,
        showGpsQuickAction = showGpsQuickAction,
        showDeviceActionsQuickAction = showDeviceActionsQuickAction,
        modeOptions = modeOptions,
        deviceActionsUiModel = deviceActionsUiModel,
        statusOverviewItems = mapStatusOverviewItems(state),
        recentEvents = mapRecentEvents(state.logs),
    )
}

private fun mapWorkbenchModeOptions(supportedModes: Set<Int>?): List<WorkbenchModeOptionUiModel> {
    val modes = supportedModes?.toList() ?: listOf(0x01, 0x05)
    return modes.map { mode ->
        WorkbenchModeOptionUiModel(
            mode = mode,
            label = workbenchModeLabel(mode),
        )
    }
}

internal fun workbenchModeLabel(mode: Int): String {
    return when (mode) {
        0x00 -> "慢动作"
        0x01 -> "视频"
        0x02 -> "静止延时"
        0x05 -> "拍照"
        0x0A -> "运动延时"
        0x28 -> "夜景"
        0x34 -> "人物跟随"
        0x38 -> "360 全景视频"
        0x3A -> "360 延时"
        0x3C -> "360 自拍"
        0x3F -> "360 全景照片"
        0x41 -> "360 超广角"
        0x43 -> "360 定格"
        0x44 -> "360 夜景"
        0x4A -> "单镜夜景"
        else -> "模式 0x${mode.toString(16)}"
    }
}

private fun mapWorkbenchDeviceActionsUiModel(
    state: DebugHomeState,
    debugOnlyProfile: Boolean,
): WorkbenchDeviceActionsUiModel {
    val session = state.sessionStatus
    val capabilities = session.connectedProfile?.capabilities
    val busy = state.busyAction != null
    val controlReady =
        session.connectedDevice != null &&
            session.protocolReady &&
            session.handshakeStage == HandshakeStage.COMPLETED
    val deviceActionsEnabled = controlReady && !busy && !session.sleeping
    val wakeActionsEnabled = !busy && session.connectedDevice != null && session.wakeAdvertisingSupported

    if (debugOnlyProfile) {
        return WorkbenchDeviceActionsUiModel(
            showSleepAction = false,
            showWakeAction = false,
            showWakeAndSnapshotAction = false,
            showVersionAction = false,
            showQsAction = false,
            showSnapshotAction = false,
            deviceActionsEnabled = false,
            wakeActionsEnabled = false,
        )
    }

    return WorkbenchDeviceActionsUiModel(
        showSleepAction = (capabilities?.supportsSleep ?: true) && !session.sleeping,
        showWakeAction = capabilities?.supportsWake ?: true,
        showWakeAndSnapshotAction = (capabilities?.supportsWakeAndSnapshot ?: true) && session.sleeping,
        showVersionAction = capabilities?.supportsVersionQuery ?: true,
        showQsAction = capabilities?.supportsQsKey ?: true,
        showSnapshotAction = false,
        deviceActionsEnabled = deviceActionsEnabled,
        wakeActionsEnabled = wakeActionsEnabled,
    )
}

fun mapPermissionCta(state: DebugHomeState): PermissionCta? {
    if (state.selectedMode != SessionTransportMode.REAL) return null

    val prereq = state.prerequisites
    val missing = !prereq.bluetoothPermissionsGranted || !prereq.locationPermissionGranted
    if (!missing) return null

    val action = if (prereq.requiresSettingsAction) PermissionAction.OPEN_SETTINGS else PermissionAction.REQUEST
    val label = if (prereq.requiresSettingsAction) "前往设置" else "开启权限"

    return PermissionCta(label = label, action = action)
}

fun mapRecentEvents(logs: List<SessionLogEntry>): List<RecentEvent> {
    val supported = logs.filter { entry ->
        entry.category == LogCategory.STATE || entry.category == LogCategory.ERROR
    }
    return supported
        .sortedByDescending { it.timestampMillis }
        .take(3)
        .map { entry ->
            RecentEvent(
                message = localizeRecentEventMessage(entry.message),
                timestampMillis = entry.timestampMillis,
            )
        }
}

private fun mapStatusOverviewItems(state: DebugHomeState): List<StatusOverviewItem> {
    val camera = state.cameraStatus
    val session = state.sessionStatus
    val statusReady = session.protocolReady
    if (!statusReady) {
        return listOf(
            StatusOverviewItem(title = "录制时长", value = "--:--"),
            StatusOverviewItem(title = "剩余时长", value = "--:--"),
            StatusOverviewItem(title = "剩余容量", value = "--"),
            StatusOverviewItem(title = "电量", value = "--"),
            StatusOverviewItem(title = "分辨率 / 帧率", value = "等待设备状态"),
            StatusOverviewItem(title = "增稳 / GPS", value = "等待设备状态"),
        )
    }
    if (session.sleeping) {
        return listOf(
            StatusOverviewItem(title = "录制时长", value = "--:--"),
            StatusOverviewItem(title = "剩余时长", value = "--:--"),
            StatusOverviewItem(title = "剩余容量", value = "--"),
            StatusOverviewItem(title = "电量", value = "--"),
            StatusOverviewItem(title = "分辨率 / 帧率", value = "设备已休眠"),
            StatusOverviewItem(title = "增稳 / GPS", value = "设备已休眠"),
        )
    }

    val gpsValue = if (session.gpsAutoPushEnabled) {
        "${session.gpsAutoPushHz}Hz"
    } else {
        "待机"
    }

    return listOf(
        StatusOverviewItem(
            title = "录制时长",
            value = if (camera.recording) formatDuration(camera.recordTimeSeconds.toLong()) else "--:--",
        ),
        StatusOverviewItem(
            title = "剩余时长",
            value = formatDuration(camera.remainTimeSeconds),
        ),
        StatusOverviewItem(
            title = "剩余容量",
            value = "${camera.remainCapacityMb}MB",
        ),
        StatusOverviewItem(
            title = "电量",
            value = "${camera.batteryPercent}%",
        ),
        StatusOverviewItem(
            title = "分辨率 / 帧率",
            value = "${resolutionLabel(camera.videoResolution)} / ${fpsLabel(camera.fpsIndex)}fps",
        ),
        StatusOverviewItem(
            title = "增稳 / GPS",
            value = "${eisLabel(camera.eisMode)} / $gpsValue",
        ),
    )
}

internal fun mapGpsDetailItems(status: SessionStatus): List<StatusOverviewItem> {
    return listOf(
        StatusOverviewItem(
            title = "定位状态",
            value = gpsStatusLabel(status),
        ),
        StatusOverviewItem(
            title = "定位来源",
            value = gpsProviderLabel(status.lastGpsProvider),
        ),
        StatusOverviewItem(
            title = "海拔",
            value = status.lastGpsAltitudeMeters?.let { String.format(Locale.US, "%.1fm", it) } ?: "暂无",
        ),
        StatusOverviewItem(
            title = "水平精度",
            value = status.lastGpsAccuracyMeters?.let { String.format(Locale.US, "%.0fm", it) } ?: "暂无",
        ),
        StatusOverviewItem(
            title = "速度",
            value = status.lastGpsSpeedMps?.let { String.format(Locale.US, "%.1fkm/h", it * 3.6f) } ?: "暂无",
        ),
        StatusOverviewItem(
            title = "方向角",
            value = status.lastGpsBearingDegrees?.let { "${normalizeBearingDegrees(it)}°" } ?: "暂无",
        ),
    )
}

private fun gpsStatusLabel(status: SessionStatus): String {
    return when {
        status.lastGpsCoordinate == null -> "暂无定位"
        status.gpsSignalLocked -> "实时定位"
        else -> "缓存定位"
    }
}

private fun gpsProviderLabel(provider: String?): String {
    return when (provider) {
        null -> "暂无"
        "gps" -> "GPS"
        "network" -> "网络"
        "passive" -> "被动"
        "manual" -> "手动"
        "manual-fake" -> "模拟"
        else -> provider
    }
}

private fun normalizeBearingDegrees(value: Float): Int {
    val normalized = ((value % 360f) + 360f) % 360f
    return normalized.toInt()
}

fun mapWorkbenchConnectionCardUiModel(state: DebugHomeState): WorkbenchConnectionCardUiModel {
    val phase = determineConnectionPhase(state)
    val permissionCta = state.permissionCta
    val sleeping = state.sessionStatus.sleeping && state.sessionStatus.connectedDevice != null
    val statusCopy = when (phase) {
        WorkbenchConnectionPhase.IDLE -> "连接设备"
        WorkbenchConnectionPhase.SCANNING -> "正在搜索设备"
        WorkbenchConnectionPhase.CONNECTING -> "正在连接设备"
        WorkbenchConnectionPhase.PREPARING -> "设备已连接，正在准备控制"
        WorkbenchConnectionPhase.READY -> if (sleeping) "设备已休眠" else state.sessionStatus.connectedDevice?.name ?: "已连接设备"
        WorkbenchConnectionPhase.FAILURE -> "连接未完成"
    }
    val primaryAction = when {
        permissionCta != null -> WorkbenchConnectionCardPrimaryAction.PERMISSION
        phase == WorkbenchConnectionPhase.READY -> WorkbenchConnectionCardPrimaryAction.READY
        phase == WorkbenchConnectionPhase.CONNECTING || phase == WorkbenchConnectionPhase.PREPARING -> WorkbenchConnectionCardPrimaryAction.PROCESSING
        phase == WorkbenchConnectionPhase.SCANNING -> WorkbenchConnectionCardPrimaryAction.VIEW
        phase == WorkbenchConnectionPhase.FAILURE -> WorkbenchConnectionCardPrimaryAction.RETRY
        else -> WorkbenchConnectionCardPrimaryAction.CONNECT
    }
    val primaryActionLabel = when (primaryAction) {
        WorkbenchConnectionCardPrimaryAction.CONNECT -> "连接"
        WorkbenchConnectionCardPrimaryAction.VIEW -> "查看"
        WorkbenchConnectionCardPrimaryAction.PROCESSING -> "处理中"
        WorkbenchConnectionCardPrimaryAction.READY -> if (sleeping) "已休眠" else "已连接"
        WorkbenchConnectionCardPrimaryAction.RETRY -> "重试"
        WorkbenchConnectionCardPrimaryAction.PERMISSION -> permissionCta?.label ?: "授权"
    }
    val supportingCopy = when {
        state.permissionCta != null -> "需要蓝牙/定位权限"
        sleeping -> "设备休眠中，可执行唤醒"
        phase == WorkbenchConnectionPhase.SCANNING -> "正在查找附近设备"
        phase == WorkbenchConnectionPhase.CONNECTING -> "正在与设备建立连接"
        phase == WorkbenchConnectionPhase.PREPARING -> "等待设备确认协议"
        phase == WorkbenchConnectionPhase.READY -> "蓝牙已连接，控制可用"
        phase == WorkbenchConnectionPhase.FAILURE -> resolveFailureSupportingCopy(state)
        else -> "通过顶部面板发现并连接设备"
    }

    return WorkbenchConnectionCardUiModel(
        phase = phase,
        statusCopy = statusCopy,
        primaryActionLabel = primaryActionLabel,
        supportingCopy = supportingCopy,
        primaryAction = primaryAction,
        permissionAction = permissionCta?.action,
    )
}

fun mapWorkbenchConnectionSheetUiModel(state: DebugHomeState): WorkbenchConnectionSheetUiModel {
    val phase = determineConnectionPhase(state)
    val banner = mapConnectionBanner(state, phase)
    val visibleDevices = state.discoveredDevices.filter { device ->
        state.destination != HomeDestination.WORKBENCH || shouldShowInWorkbench(device)
    }
    val filteredDevices = filterDiscoveredDevices(visibleDevices, state.deviceFilterQuery)
    val candidateId = state.selectedConnectionDeviceId
    val candidateMac = state.discoveredDevices.firstOrNull { it.deviceId == candidateId }?.macAddress
    val connectedDevice = state.sessionStatus.connectedDevice
    val connectedDeviceId = connectedDevice?.deviceId
    val connectedMac = connectedDevice?.macAddress
    val candidateVisible = candidateId != null && filteredDevices.any { it.deviceId == candidateId }
    val hasCandidate = candidateVisible && connectedDeviceId != candidateId
    val selectedDeviceId = when {
        hasCandidate -> candidateId
        connectedDeviceId != null -> connectedDeviceId
        else -> null
    }
    val primaryAction = determineSheetPrimaryAction(state, phase, hasCandidate)
    val primaryActionLabel = when (primaryAction) {
        WorkbenchConnectionPrimaryAction.START_SCAN -> "开始扫描"
        WorkbenchConnectionPrimaryAction.STOP_SCAN -> "停止扫描"
        WorkbenchConnectionPrimaryAction.CONNECT_DEVICE -> "连接设备"
        WorkbenchConnectionPrimaryAction.DISCONNECT -> "断开连接"
        WorkbenchConnectionPrimaryAction.PROCESSING -> "处理中"
        WorkbenchConnectionPrimaryAction.RETRY -> "重新扫描"
        WorkbenchConnectionPrimaryAction.PERMISSION -> state.permissionCta?.label ?: "授权"
    }
    val selectedDeviceMac = candidateMac ?: connectedMac
    val deviceRows = mapConnectionDeviceRows(
        devices = filteredDevices,
        selectedDeviceId = selectedDeviceId,
        selectedDeviceMac = selectedDeviceMac,
        connectedDeviceId = connectedDeviceId,
        connectedDeviceMac = connectedMac,
        status = state.sessionStatus,
    )

    return WorkbenchConnectionSheetUiModel(
        phase = phase,
        banner = banner,
        filterQuery = state.deviceFilterQuery,
        filteredDeviceCount = filteredDevices.size,
        totalDeviceCount = state.discoveredDevices.size,
        deviceRows = deviceRows,
        primaryActionLabel = primaryActionLabel,
        primaryAction = primaryAction,
        selectedDeviceId = selectedDeviceId,
    )
}

private fun shouldShowInWorkbench(device: SessionDevice): Boolean {
    return device.workbenchSupported || device.inferredProtocolFamily == ProtocolFamily.DJI_RSDK_ACTION
}

internal fun filterDiscoveredDevices(
    devices: List<SessionDevice>,
    query: String,
): List<SessionDevice> {
    val normalizedQuery = query.trim().lowercase()
    if (normalizedQuery.isBlank()) return devices
    val compactQuery = normalizedQuery.replace(":", "").replace("-", "")
    return devices.filter { device ->
        val normalizedMac = device.macAddress.lowercase()
        val compactMac = normalizedMac.replace(":", "").replace("-", "")
        device.name.lowercase().contains(normalizedQuery) ||
            normalizedMac.contains(normalizedQuery) ||
            compactMac.contains(compactQuery)
    }
}

private fun determineSheetPrimaryAction(
    state: DebugHomeState,
    phase: WorkbenchConnectionPhase,
    hasCandidate: Boolean,
): WorkbenchConnectionPrimaryAction {
    if (state.permissionCta != null) return WorkbenchConnectionPrimaryAction.PERMISSION
    return when {
        phase == WorkbenchConnectionPhase.READY -> WorkbenchConnectionPrimaryAction.DISCONNECT
        phase == WorkbenchConnectionPhase.CONNECTING || phase == WorkbenchConnectionPhase.PREPARING -> WorkbenchConnectionPrimaryAction.PROCESSING
        hasCandidate -> WorkbenchConnectionPrimaryAction.CONNECT_DEVICE
        phase == WorkbenchConnectionPhase.SCANNING -> WorkbenchConnectionPrimaryAction.STOP_SCAN
        phase == WorkbenchConnectionPhase.FAILURE -> WorkbenchConnectionPrimaryAction.RETRY
        else -> WorkbenchConnectionPrimaryAction.START_SCAN
    }
}

private fun determineConnectionPhase(state: DebugHomeState): WorkbenchConnectionPhase {
    val status = state.sessionStatus
    val hasError = state.lastUiError != null || status.latestError != null
    val handshakeRejected = status.handshakeStage == HandshakeStage.REJECTED
    return when {
        status.protocolReady && status.connectedDevice != null -> WorkbenchConnectionPhase.READY
        handshakeRejected -> WorkbenchConnectionPhase.FAILURE
        status.connectedDevice != null -> when (status.handshakeStage) {
            HandshakeStage.CAMERA_CONFIRMATION_RECEIVED -> WorkbenchConnectionPhase.PREPARING
            HandshakeStage.REQUEST_SENT -> WorkbenchConnectionPhase.CONNECTING
            else -> WorkbenchConnectionPhase.CONNECTING
        }
        status.scanning -> WorkbenchConnectionPhase.SCANNING
        hasError -> WorkbenchConnectionPhase.FAILURE
        else -> WorkbenchConnectionPhase.IDLE
    }
}

private fun resolveFailureSupportingCopy(state: DebugHomeState): String {
    val cause = state.lastUiError ?: state.sessionStatus.latestError ?: "连接未完成"
    return "$cause · 可重试"
}

private fun mapConnectionBanner(state: DebugHomeState, phase: WorkbenchConnectionPhase): WorkbenchConnectionBannerUiModel? {
    val errorText = state.lastUiError ?: state.sessionStatus.latestError
    val permissionCta = state.permissionCta
    val sleeping = state.sessionStatus.sleeping && state.sessionStatus.connectedDevice != null
    return when {
        permissionCta != null -> WorkbenchConnectionBannerUiModel(
            message = "需要蓝牙/定位权限",
            actionLabel = "授权",
            type = WorkbenchConnectionBannerType.PERMISSION,
            permissionAction = permissionCta.action,
        )
        sleeping -> WorkbenchConnectionBannerUiModel(
            message = "设备已休眠，可执行唤醒",
            actionLabel = null,
            type = WorkbenchConnectionBannerType.SUCCESS,
        )
        phase == WorkbenchConnectionPhase.SCANNING -> WorkbenchConnectionBannerUiModel(
            message = "正在更新附近设备",
            actionLabel = null,
            type = WorkbenchConnectionBannerType.NEUTRAL,
        )
        phase == WorkbenchConnectionPhase.CONNECTING -> WorkbenchConnectionBannerUiModel(
            message = "正在与设备建立连接",
            actionLabel = null,
            type = WorkbenchConnectionBannerType.NEUTRAL,
        )
        phase == WorkbenchConnectionPhase.PREPARING -> WorkbenchConnectionBannerUiModel(
            message = "等待设备确认协议",
            actionLabel = null,
            type = WorkbenchConnectionBannerType.NEUTRAL,
        )
        phase == WorkbenchConnectionPhase.READY -> WorkbenchConnectionBannerUiModel(
            message = "当前设备已可控制",
            actionLabel = null,
            type = WorkbenchConnectionBannerType.SUCCESS,
        )
        phase == WorkbenchConnectionPhase.FAILURE -> WorkbenchConnectionBannerUiModel(
            message = errorText ?: "连接未完成",
            actionLabel = "重新扫描",
            type = WorkbenchConnectionBannerType.ERROR,
        )
        else -> null
    }
}

private fun mapConnectionDeviceRows(
    devices: List<SessionDevice>,
    selectedDeviceId: Long?,
    selectedDeviceMac: String?,
    connectedDeviceId: Long?,
    connectedDeviceMac: String?,
    status: SessionStatus,
): List<WorkbenchConnectionDeviceRowUiModel> {
    return devices.map { device ->
        WorkbenchConnectionDeviceRowUiModel(
            deviceKey = device.macAddress,
            deviceId = device.deviceId,
            name = device.name,
            macSuffix = device.macAddress.takeLast(4),
            statusLabel = mapDeviceStatusLabel(
                device = device,
                connectedDeviceId = connectedDeviceId,
                connectedDeviceMac = connectedDeviceMac,
                status = status,
                selectedDeviceId = selectedDeviceId,
                selectedDeviceMac = selectedDeviceMac,
            ),
            isSelected = device.deviceId == selectedDeviceId ||
                (selectedDeviceMac != null && device.macAddress == selectedDeviceMac),
        )
    }
}

private fun mapDeviceStatusLabel(
    device: SessionDevice,
    connectedDeviceId: Long?,
    connectedDeviceMac: String?,
    status: SessionStatus,
    selectedDeviceId: Long?,
    selectedDeviceMac: String?,
): String? {
    val matchesConnected = connectedDeviceId != null && (
        device.deviceId == connectedDeviceId ||
            (connectedDeviceMac != null && device.macAddress == connectedDeviceMac)
        )
    if (matchesConnected) {
        if (status.sleeping) return "已休眠"
        return when (status.handshakeStage) {
            HandshakeStage.CAMERA_CONFIRMATION_RECEIVED -> "准备中"
            HandshakeStage.REQUEST_SENT -> "连接中"
            else -> if (status.protocolReady) "已连接" else "连接中"
        }
    }
    val matchesSelected = (selectedDeviceId != null && device.deviceId == selectedDeviceId) ||
        (selectedDeviceMac != null && device.macAddress == selectedDeviceMac)
    if (matchesSelected) {
        return "已选择"
    }
    return null
}

private fun localizeRecentEventMessage(message: String): String {
    val normalized = message.trim().removeSuffix(".")
    return when (normalized) {
        "Bluetooth connected",
        "Connected" -> "设备已连接"
        "Protocol ready" -> "控制已就绪"
        "Recording" -> "录制中"
        "Standby" -> "待命"
        "Handshake started" -> "开始准备设备"
        "Fake handshake completed" -> "设备准备完成"
        "Disconnected" -> "连接已断开"
        else -> message
    }
}
