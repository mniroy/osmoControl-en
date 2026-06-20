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

private const val MODE_SWITCH_BLOCKED_RECORDING = "Recording"
private const val MODE_SWITCH_BLOCKED_NOT_CONNECTED = "Please connect device first"
private const val MODE_SWITCH_BLOCKED_SCANNING = "Searching for devices"
private const val MODE_SWITCH_BLOCKED_HANDSHAKE = "Device Preparing"
private const val MODE_SWITCH_BLOCKED_SLEEPING = "Device Sleeping"
private const val MODE_SWITCH_BLOCKED_UNSUPPORTED = "Current device only supports debug control"
private const val RECORD_BLOCKED_NOT_CONNECTED = "Please connect device first"
private const val RECORD_BLOCKED_SCANNING = "Searching for devices"
private const val RECORD_BLOCKED_HANDSHAKE = "Device Preparing"
private const val RECORD_BLOCKED_SLEEPING = "Device Sleeping"
private const val RECORD_BLOCKED_UNSUPPORTED = "Current device only supports debug control"
private const val GPS_BLOCKED_NOT_CONNECTED = "Please connect device first"
private const val GPS_BLOCKED_SCANNING = "Searching for devices"
private const val GPS_BLOCKED_HANDSHAKE = "Device Preparing"
private const val GPS_BLOCKED_SLEEPING = "Device Sleeping"
private const val GPS_BLOCKED_UNSUPPORTED = "Current device only supports debug control"
private const val CAMERA_MODE_PHOTO = 0x05

internal fun isPhotoCaptureMode(snapshot: CameraStatusSnapshot): Boolean = snapshot.mode == CAMERA_MODE_PHOTO

internal fun primaryActionButtonLabel(snapshot: CameraStatusSnapshot, enabled: Boolean): String {
    return when {
        !enabled -> "Not Ready"
        isPhotoCaptureMode(snapshot) && snapshot.recording -> "Taking Photo"
        isPhotoCaptureMode(snapshot) -> "Photo"
        snapshot.recording -> "Stop"
        else -> "Record"
    }
}

internal fun primaryActionContentDescription(snapshot: CameraStatusSnapshot): String {
    return when {
        isPhotoCaptureMode(snapshot) && snapshot.recording -> "Taking Photo"
        isPhotoCaptureMode(snapshot) -> "Photo"
        snapshot.recording -> "Stop Recording"
        else -> "Start Recording"
    }
}

internal fun primaryActionStatusText(state: DebugHomeState): String {
    val snapshot = state.cameraStatus
    return when {
        isPhotoCaptureMode(snapshot) && snapshot.recording -> "Taking Photo"
        snapshot.recording -> "Recording"
        state.sessionStatus.sleeping -> "Device Sleeping"
        state.sessionStatus.protocolReady -> "Control Ready"
        state.sessionStatus.connectedDevice != null -> "Device Preparing"
        state.sessionStatus.scanning -> "Searching"
        else -> "Standby"
    }
}

internal fun primaryActionHelperText(state: DebugHomeState): String {
    val snapshot = state.cameraStatus
    val workbench = state.workbenchUiModel
    return when {
        isPhotoCaptureMode(snapshot) && snapshot.recording -> "Taking photo, please wait for the device to return to preview state."
        snapshot.recording -> "Recording in progress, mode switching is locked."
        state.sessionStatus.sleeping -> "Device has slept, please wake it up first."
        workbench.recordActionDisabledReason != null -> workbench.recordActionDisabledReason
        workbench.modeSwitchBlockedReason != null -> "Switch Mode: ${workbench.modeSwitchBlockedReason}"
        else -> "Primary controls are centered, suitable for default remote control path."
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
        sleeping -> "Device Sleeping"
        protocolReady -> "Control is Ready"
        connected -> "Device Connected"
        status.scanning -> "Searching for devices"
        else -> "Device Not Connected"
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
        0x00 -> "Slow Motion"
        0x01 -> "Video"
        0x02 -> "Timelapse"
        0x05 -> "Photo"
        0x0A -> "Motionlapse"
        0x28 -> "Night Mode"
        0x34 -> "ActiveTrack"
        0x38 -> "360 Panorama Video"
        0x3A -> "360 Timelapse"
        0x3C -> "360 Selfie"
        0x3F -> "360 Panorama Photo"
        0x41 -> "360 Ultra Wide"
        0x43 -> "360 Stop Motion"
        0x44 -> "360 Night Mode"
        0x4A -> "Single Lens Night"
        else -> "Mode 0x${mode.toString(16)}"
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
    val label = if (prereq.requiresSettingsAction) "Go to Settings" else "Grant Permission"

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
            StatusOverviewItem(title = "Recording Time", value = "--:--"),
            StatusOverviewItem(title = "Remaining Time", value = "--:--"),
            StatusOverviewItem(title = "Remaining Capacity", value = "--"),
            StatusOverviewItem(title = "Battery", value = "--"),
            StatusOverviewItem(title = "Resolution / FPS", value = "Waiting for device status"),
            StatusOverviewItem(title = "Stabilization / GPS", value = "Waiting for device status"),
        )
    }
    if (session.sleeping) {
        return listOf(
            StatusOverviewItem(title = "Recording Time", value = "--:--"),
            StatusOverviewItem(title = "Remaining Time", value = "--:--"),
            StatusOverviewItem(title = "Remaining Capacity", value = "--"),
            StatusOverviewItem(title = "Battery", value = "--"),
            StatusOverviewItem(title = "Resolution / FPS", value = "Device Sleeping"),
            StatusOverviewItem(title = "Stabilization / GPS", value = "Device Sleeping"),
        )
    }

    val gpsValue = if (session.gpsAutoPushEnabled) {
        "${session.gpsAutoPushHz}Hz"
    } else {
        "Standby"
    }

    return listOf(
        StatusOverviewItem(
            title = "Recording Time",
            value = if (camera.recording) formatDuration(camera.recordTimeSeconds.toLong()) else "--:--",
        ),
        StatusOverviewItem(
            title = "Remaining Time",
            value = formatDuration(camera.remainTimeSeconds),
        ),
        StatusOverviewItem(
            title = "Remaining Capacity",
            value = "${camera.remainCapacityMb}MB",
        ),
        StatusOverviewItem(
            title = "Battery",
            value = "${camera.batteryPercent}%",
        ),
        StatusOverviewItem(
            title = "Resolution / FPS",
            value = "${resolutionLabel(camera.videoResolution)} / ${fpsLabel(camera.fpsIndex)}fps",
        ),
        StatusOverviewItem(
            title = "Stabilization / GPS",
            value = "${eisLabel(camera.eisMode)} / $gpsValue",
        ),
    )
}

internal fun mapGpsDetailItems(status: SessionStatus): List<StatusOverviewItem> {
    return listOf(
        StatusOverviewItem(
            title = "Location Status",
            value = gpsStatusLabel(status),
        ),
        StatusOverviewItem(
            title = "Location Source",
            value = gpsProviderLabel(status.lastGpsProvider),
        ),
        StatusOverviewItem(
            title = "Altitude",
            value = status.lastGpsAltitudeMeters?.let { String.format(Locale.US, "%.1fm", it) } ?: "None",
        ),
        StatusOverviewItem(
            title = "Horizontal Accuracy",
            value = status.lastGpsAccuracyMeters?.let { String.format(Locale.US, "%.0fm", it) } ?: "None",
        ),
        StatusOverviewItem(
            title = "Speed",
            value = status.lastGpsSpeedMps?.let { String.format(Locale.US, "%.1fkm/h", it * 3.6f) } ?: "None",
        ),
        StatusOverviewItem(
            title = "Bearing",
            value = status.lastGpsBearingDegrees?.let { "${normalizeBearingDegrees(it)}°" } ?: "None",
        ),
    )
}

private fun gpsStatusLabel(status: SessionStatus): String {
    return when {
        status.lastGpsCoordinate == null -> "No Location"
        status.gpsSignalLocked -> "Real-time Location"
        else -> "Cached Location"
    }
}

private fun gpsProviderLabel(provider: String?): String {
    return when (provider) {
        null -> "None"
        "gps" -> "GPS"
        "network" -> "Network"
        "passive" -> "Passive"
        "manual" -> "Manual"
        "manual-fake" -> "Simulate"
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
        WorkbenchConnectionPhase.IDLE -> "Connect Device"
        WorkbenchConnectionPhase.SCANNING -> "Searching for devices"
        WorkbenchConnectionPhase.CONNECTING -> "Connecting to device"
        WorkbenchConnectionPhase.PREPARING -> "Device connected, preparing control"
        WorkbenchConnectionPhase.READY -> if (sleeping) "Device Sleeping" else state.sessionStatus.connectedDevice?.name ?: "Device Connected"
        WorkbenchConnectionPhase.FAILURE -> "Connection Incomplete"
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
        WorkbenchConnectionCardPrimaryAction.CONNECT -> "Connect"
        WorkbenchConnectionCardPrimaryAction.VIEW -> "View"
        WorkbenchConnectionCardPrimaryAction.PROCESSING -> "Processing"
        WorkbenchConnectionCardPrimaryAction.READY -> if (sleeping) "Sleeping" else "Connected"
        WorkbenchConnectionCardPrimaryAction.RETRY -> "Retry"
        WorkbenchConnectionCardPrimaryAction.PERMISSION -> permissionCta?.label ?: "Authorize"
    }
    val supportingCopy = when {
        state.permissionCta != null -> "Requires Bluetooth/Location permissions"
        sleeping -> "Device is sleeping, can be woken up"
        phase == WorkbenchConnectionPhase.SCANNING -> "Looking for nearby devices"
        phase == WorkbenchConnectionPhase.CONNECTING -> "Establishing connection with device"
        phase == WorkbenchConnectionPhase.PREPARING -> "Waiting for device to confirm protocol"
        phase == WorkbenchConnectionPhase.READY -> "Bluetooth connected, control available"
        phase == WorkbenchConnectionPhase.FAILURE -> resolveFailureSupportingCopy(state)
        else -> "Discover and connect device via top panel"
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
        WorkbenchConnectionPrimaryAction.START_SCAN -> "Start Scan"
        WorkbenchConnectionPrimaryAction.STOP_SCAN -> "Stop Scan"
        WorkbenchConnectionPrimaryAction.CONNECT_DEVICE -> "Connect Device"
        WorkbenchConnectionPrimaryAction.DISCONNECT -> "Disconnect"
        WorkbenchConnectionPrimaryAction.PROCESSING -> "Processing"
        WorkbenchConnectionPrimaryAction.RETRY -> "Rescan"
        WorkbenchConnectionPrimaryAction.PERMISSION -> state.permissionCta?.label ?: "Authorize"
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
    val cause = state.lastUiError ?: state.sessionStatus.latestError ?: "Connection Incomplete"
    return "$cause · Retryable"
}

private fun mapConnectionBanner(state: DebugHomeState, phase: WorkbenchConnectionPhase): WorkbenchConnectionBannerUiModel? {
    val errorText = state.lastUiError ?: state.sessionStatus.latestError
    val permissionCta = state.permissionCta
    val sleeping = state.sessionStatus.sleeping && state.sessionStatus.connectedDevice != null
    return when {
        permissionCta != null -> WorkbenchConnectionBannerUiModel(
            message = "Requires Bluetooth/Location permissions",
            actionLabel = "Authorize",
            type = WorkbenchConnectionBannerType.PERMISSION,
            permissionAction = permissionCta.action,
        )
        sleeping -> WorkbenchConnectionBannerUiModel(
            message = "Device has slept, can be woken up",
            actionLabel = null,
            type = WorkbenchConnectionBannerType.SUCCESS,
        )
        phase == WorkbenchConnectionPhase.SCANNING -> WorkbenchConnectionBannerUiModel(
            message = "Updating nearby devices",
            actionLabel = null,
            type = WorkbenchConnectionBannerType.NEUTRAL,
        )
        phase == WorkbenchConnectionPhase.CONNECTING -> WorkbenchConnectionBannerUiModel(
            message = "Establishing connection with device",
            actionLabel = null,
            type = WorkbenchConnectionBannerType.NEUTRAL,
        )
        phase == WorkbenchConnectionPhase.PREPARING -> WorkbenchConnectionBannerUiModel(
            message = "Waiting for device to confirm protocol",
            actionLabel = null,
            type = WorkbenchConnectionBannerType.NEUTRAL,
        )
        phase == WorkbenchConnectionPhase.READY -> WorkbenchConnectionBannerUiModel(
            message = "Current device is ready for control",
            actionLabel = null,
            type = WorkbenchConnectionBannerType.SUCCESS,
        )
        phase == WorkbenchConnectionPhase.FAILURE -> WorkbenchConnectionBannerUiModel(
            message = errorText ?: "Connection Incomplete",
            actionLabel = "Rescan",
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
        if (status.sleeping) return "Sleeping"
        return when (status.handshakeStage) {
            HandshakeStage.CAMERA_CONFIRMATION_RECEIVED -> "Preparing"
            HandshakeStage.REQUEST_SENT -> "Connecting"
            else -> if (status.protocolReady) "Connected" else "Connecting"
        }
    }
    val matchesSelected = (selectedDeviceId != null && device.deviceId == selectedDeviceId) ||
        (selectedDeviceMac != null && device.macAddress == selectedDeviceMac)
    if (matchesSelected) {
        return "Selected"
    }
    return null
}

private fun localizeRecentEventMessage(message: String): String {
    val normalized = message.trim().removeSuffix(".")
    return when (normalized) {
        "Bluetooth connected",
        "Connected" -> "Device Connected"
        "Protocol ready" -> "Control is Ready"
        "Recording" -> "Recording"
        "Standby" -> "Standby"
        "Handshake started" -> "Starting Device Preparation"
        "Fake handshake completed" -> "Device Preparation Complete"
        "Disconnected" -> "Connection Disconnected"
        else -> message
    }
}
