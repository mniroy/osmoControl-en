package com.mniroy.osmo.demo.app.ui.home

import com.mniroy.osmo.demo.session.log.SessionLogEntry
import com.mniroy.osmo.demo.session.model.DeviceCapabilities
import com.mniroy.osmo.demo.session.model.LogCategory
import com.mniroy.osmo.demo.session.model.ProtocolFamily
import com.mniroy.osmo.demo.session.model.SessionDevice
import com.mniroy.osmo.demo.session.model.SessionTransportMode
import java.util.Locale

data class ManualHexValidation(
    val normalizedHex: String?,
    val errorMessage: String?,
) {
    val isValid: Boolean
        get() = normalizedHex != null && errorMessage == null
}

data class DebugLogGroup(
    val category: LogCategory,
    val entries: List<SessionLogEntry>,
    val expandedByDefault: Boolean,
)

enum class DebugConsoleActionType {
    VERSION_QUERY,
    REBOOT,
    TOGGLE_RECORD,
    SUBSCRIBE_STATUS,
    SLEEP,
    WAKE,
    SWITCH_MODE,
    WAKE_AND_SNAPSHOT,
    RECORD_KEY,
    QS_KEY,
    SNAPSHOT_KEY,
}

data class DebugConsoleActionUiModel(
    val actionType: DebugConsoleActionType,
    val label: String,
    val enabled: Boolean,
    val mode: Int? = null,
)

data class DebugConsoleUiModel(
    val capabilityNotice: String?,
    val coreActions: List<DebugConsoleActionUiModel>,
    val modeActions: List<DebugConsoleActionUiModel>,
    val auxiliaryActions: List<DebugConsoleActionUiModel>,
    val keyActions: List<DebugConsoleActionUiModel>,
    val showGpsSection: Boolean,
    val gpsControlsEnabled: Boolean,
)

data class DebugConsoleDeviceRowUiModel(
    val name: String,
    val macAddress: String,
    val capabilityLabel: String,
    val isConnected: Boolean,
)

fun validateManualHexInput(input: String): ManualHexValidation {
    val compact = input.trim().replace("\\s+".toRegex(), "")
    if (compact.isEmpty()) {
        return ManualHexValidation(
            normalizedHex = null,
            errorMessage = "Please enter HEX command",
        )
    }
    if (!compact.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
        return ManualHexValidation(
            normalizedHex = null,
            errorMessage = "Only hexadecimal characters and spaces are supported",
        )
    }
    if (compact.length % 2 != 0) {
        return ManualHexValidation(
            normalizedHex = null,
            errorMessage = "HEX must be entered in complete byte pairs",
        )
    }
    return ManualHexValidation(
        normalizedHex = compact.uppercase(Locale.US),
        errorMessage = null,
    )
}

fun groupDebugLogs(
    logs: List<SessionLogEntry>,
    clearedAtMillis: Long = Long.MIN_VALUE,
): List<DebugLogGroup> {
    val visibleLogs = logs.filter { it.timestampMillis > clearedAtMillis }
    val orderedCategories = listOf(LogCategory.ERROR) + LogCategory.entries.filterNot { it == LogCategory.ERROR }
    return orderedCategories.mapNotNull { category ->
        val entries = visibleLogs
            .filter { it.category == category }
            .sortedByDescending { it.timestampMillis }
        if (entries.isEmpty()) {
            null
        } else {
            DebugLogGroup(
                category = category,
                entries = entries,
                expandedByDefault = category == LogCategory.ERROR,
            )
        }
    }
}

fun mapDebugConsoleUiModel(state: DebugHomeState): DebugConsoleUiModel {
    val session = state.sessionStatus
    val capabilities = session.connectedProfile?.capabilities
    val busy = state.busyAction != null
    val canSendProtocol = state.selectedMode == SessionTransportMode.FAKE || session.protocolReady
    val baseControlEnabled = !busy && canSendProtocol
    val wakeEnabled = !busy && canWakeFromDebugConsole(state)
    val presetCommandsAvailable = capabilities?.supportsDebugConsole ?: true

    if (!presetCommandsAvailable) {
        return DebugConsoleUiModel(
            capabilityNotice = "Current device has no preset debug commands, use logs and manual messaging instead.",
            coreActions = emptyList(),
            modeActions = emptyList(),
            auxiliaryActions = emptyList(),
            keyActions = emptyList(),
            showGpsSection = false,
            gpsControlsEnabled = false,
        )
    }

    val coreActions = buildList {
        addIfSupported(DebugConsoleActionType.VERSION_QUERY, "Query Version", capabilities, DeviceCapabilities::supportsVersionQuery, baseControlEnabled)
        addIfSupported(DebugConsoleActionType.REBOOT, "Reboot Camera", capabilities, DeviceCapabilities::supportsReboot, baseControlEnabled)
        addIfSupported(DebugConsoleActionType.TOGGLE_RECORD, "Record Key", capabilities, DeviceCapabilities::supportsDirectRecord, baseControlEnabled)
        addIfSupported(DebugConsoleActionType.SUBSCRIBE_STATUS, "Subscribe Status", capabilities, DeviceCapabilities::supportsStateSubscribe, baseControlEnabled)
        addIfSupported(DebugConsoleActionType.SLEEP, "Sleep", capabilities, DeviceCapabilities::supportsSleep, baseControlEnabled)
        addIfSupported(DebugConsoleActionType.WAKE, "Wake", capabilities, DeviceCapabilities::supportsWake, wakeEnabled)
    }
    val modeActions = buildList {
        if (capabilities == null || capabilities.supportsModeSwitch) {
            val supportedModes = capabilities?.supportedModes?.takeIf { it.isNotEmpty() } ?: linkedSetOf(0x01, 0x05)
            supportedModes.forEach { mode ->
                add(
                    DebugConsoleActionUiModel(
                        actionType = DebugConsoleActionType.SWITCH_MODE,
                        label = workbenchModeLabel(mode),
                        enabled = baseControlEnabled,
                        mode = mode,
                    ),
                )
            }
        }
    }
    val auxiliaryActions = buildList {
        if (session.sleeping && (capabilities == null || capabilities.supportsWakeAndSnapshot)) {
            add(
                DebugConsoleActionUiModel(
                    actionType = DebugConsoleActionType.WAKE_AND_SNAPSHOT,
                    label = "Wake and Snap",
                    enabled = wakeEnabled,
                ),
            )
        }
    }
    val keyActions = buildList {
        addIfSupported(DebugConsoleActionType.RECORD_KEY, "Key Record", capabilities, DeviceCapabilities::supportsRecordKey, baseControlEnabled)
        addIfSupported(DebugConsoleActionType.QS_KEY, "QS Key", capabilities, DeviceCapabilities::supportsQsKey, baseControlEnabled)
    }
    val showGpsSection = capabilities?.supportsGpsPush ?: true
    val capabilityNotice = when {
        capabilities == null -> null
        !capabilities.supportsWorkbench -> "Current device is not on the Workbench compatibility list, only verified debug commands are available."
        else -> null
    }

    return DebugConsoleUiModel(
        capabilityNotice = capabilityNotice,
        coreActions = coreActions,
        modeActions = modeActions,
        auxiliaryActions = auxiliaryActions,
        keyActions = keyActions,
        showGpsSection = showGpsSection,
        gpsControlsEnabled = baseControlEnabled,
    )
}

fun mapDebugConsoleDeviceRows(state: DebugHomeState): List<DebugConsoleDeviceRowUiModel> {
    val connectedMac = state.sessionStatus.connectedDevice?.macAddress
    val connectedProfile = state.sessionStatus.connectedProfile
    return state.discoveredDevices.map { device ->
        val isConnected = device.macAddress == connectedMac
        val capabilityLabel = if (isConnected && connectedProfile != null) {
            capabilityLabelFor(connectedProfile.capabilities)
        } else {
            capabilityLabelFor(device)
        }
        DebugConsoleDeviceRowUiModel(
            name = device.name,
            macAddress = device.macAddress,
            capabilityLabel = capabilityLabel,
            isConnected = isConnected,
        )
    }
}

fun debugConsoleConnectedDeviceName(state: DebugHomeState): String {
    return state.sessionStatus.connectedProfile?.displayName
        ?: state.sessionStatus.connectedDevice?.name
        ?: "None"
}

fun debugConsoleConnectedCapabilityLabel(state: DebugHomeState): String {
    val capabilities = state.sessionStatus.connectedProfile?.capabilities ?: return "Unrecognized"
    return capabilityLabelFor(capabilities)
}

private fun canWakeFromDebugConsole(state: DebugHomeState): Boolean {
    val session = state.sessionStatus
    return state.selectedMode == SessionTransportMode.FAKE ||
        (session.connectedDevice != null &&
            state.prerequisites.bluetoothPermissionsGranted &&
            state.sessionStatus.bluetoothEnabled &&
            state.sessionStatus.wakeAdvertisingSupported)
}

private fun capabilityLabelFor(capabilities: DeviceCapabilities): String {
    return when {
        capabilities.supportsWorkbench -> "Workbench Compatible"
        capabilities.supportsDebugConsole -> "Restricted Debug"
        else -> "Log / Manual Only"
    }
}

private fun capabilityLabelFor(device: SessionDevice): String {
    return when {
        device.workbenchSupported -> "Workbench Compatible"
        device.inferredProtocolFamily != ProtocolFamily.UNKNOWN -> "Restricted Debug"
        else -> "Log / Manual Only"
    }
}

private fun MutableList<DebugConsoleActionUiModel>.addIfSupported(
    actionType: DebugConsoleActionType,
    label: String,
    capabilities: DeviceCapabilities?,
    predicate: (DeviceCapabilities) -> Boolean,
    enabled: Boolean,
) {
    if (capabilities == null || predicate(capabilities)) {
        add(
            DebugConsoleActionUiModel(
                actionType = actionType,
                label = label,
                enabled = enabled,
            ),
        )
    }
}
