package com.mniroy.osmo.demo.app.ui.home

import com.mniroy.osmo.demo.session.log.SessionLogEntry
import com.mniroy.osmo.demo.session.model.CameraStatusSnapshot
import com.mniroy.osmo.demo.session.model.SessionDevice
import com.mniroy.osmo.demo.session.model.SessionStatus
import com.mniroy.osmo.demo.session.model.SessionTransportMode

data class RealModePrerequisites(
    val bluetoothPermissionsGranted: Boolean = false,
    val locationPermissionGranted: Boolean = false,
    val requiresSettingsAction: Boolean = false,
)

data class DebugHomeState(
    val destination: HomeDestination = HomeDestination.WORKBENCH,
    val debugConsoleLogsRequest: Int = 0,
    val openSheet: HomeSheet? = null,
    val selectedMode: SessionTransportMode = SessionTransportMode.REAL,
    val sessionStatus: SessionStatus = SessionStatus(mode = SessionTransportMode.REAL),
    val selectedConnectionDeviceId: Long? = null,
    val deviceFilterQuery: String = "",
    val cameraStatus: CameraStatusSnapshot = CameraStatusSnapshot(),
    val discoveredDevices: List<SessionDevice> = emptyList(),
    val logs: List<SessionLogEntry> = emptyList(),
    val manualHex: String = "",
    val busyAction: String? = null,
    val lastUiError: String? = null,
    val prerequisites: RealModePrerequisites = RealModePrerequisites(),
) {
    val workbenchUiModel: WorkbenchUiModel by lazy(LazyThreadSafetyMode.NONE) {
        mapWorkbenchUiModel(this)
    }

    val permissionCta: PermissionCta? by lazy(LazyThreadSafetyMode.NONE) {
        mapPermissionCta(this)
    }

    val workbenchConnectionCardUiModel: WorkbenchConnectionCardUiModel by lazy(LazyThreadSafetyMode.NONE) {
        mapWorkbenchConnectionCardUiModel(this)
    }

    val workbenchConnectionSheetUiModel: WorkbenchConnectionSheetUiModel by lazy(LazyThreadSafetyMode.NONE) {
        mapWorkbenchConnectionSheetUiModel(this)
    }

    val debugConsoleUiModel: DebugConsoleUiModel by lazy(LazyThreadSafetyMode.NONE) {
        mapDebugConsoleUiModel(this)
    }
}
