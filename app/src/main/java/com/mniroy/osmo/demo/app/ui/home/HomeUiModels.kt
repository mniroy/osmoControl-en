package com.mniroy.osmo.demo.app.ui.home

enum class PermissionAction {
    REQUEST,
    OPEN_SETTINGS,
}

data class PermissionCta(
    val label: String,
    val action: PermissionAction,
)

data class RecentEvent(
    val message: String,
    val timestampMillis: Long,
)

data class StatusOverviewItem(
    val title: String,
    val value: String,
)

data class WorkbenchModeOptionUiModel(
    val mode: Int,
    val label: String,
)

data class WorkbenchDeviceActionsUiModel(
    val showSleepAction: Boolean,
    val showWakeAction: Boolean,
    val showWakeAndSnapshotAction: Boolean,
    val showVersionAction: Boolean,
    val showQsAction: Boolean,
    val showSnapshotAction: Boolean,
    val deviceActionsEnabled: Boolean,
    val wakeActionsEnabled: Boolean,
)

data class WorkbenchUiModel(
    val connectionSummary: String,
    val recordActionEnabled: Boolean,
    val recordActionDisabledReason: String?,
    val gpsActionEnabled: Boolean,
    val gpsActionDisabledReason: String?,
    val modeSwitchEnabled: Boolean,
    val modeSwitchBlockedReason: String?,
    val showModeQuickAction: Boolean,
    val showGpsQuickAction: Boolean,
    val showDeviceActionsQuickAction: Boolean,
    val modeOptions: List<WorkbenchModeOptionUiModel>,
    val deviceActionsUiModel: WorkbenchDeviceActionsUiModel,
    val statusOverviewItems: List<StatusOverviewItem>,
    val recentEvents: List<RecentEvent>,
)

enum class WorkbenchConnectionPhase {
    IDLE,
    SCANNING,
    CONNECTING,
    PREPARING,
    READY,
    FAILURE,
}

enum class WorkbenchConnectionBannerType {
    NEUTRAL,
    SUCCESS,
    ERROR,
    PERMISSION,
}

data class WorkbenchConnectionCardUiModel(
    val phase: WorkbenchConnectionPhase,
    val statusCopy: String,
    val primaryActionLabel: String,
    val supportingCopy: String,
    val primaryAction: WorkbenchConnectionCardPrimaryAction,
    val permissionAction: PermissionAction?,
)

data class WorkbenchConnectionBannerUiModel(
    val message: String,
    val actionLabel: String?,
    val type: WorkbenchConnectionBannerType,
    val permissionAction: PermissionAction? = null,
)

data class WorkbenchConnectionDeviceRowUiModel(
    val deviceKey: String,
    val deviceId: Long,
    val name: String,
    val macSuffix: String,
    val statusLabel: String?,
    val isSelected: Boolean,
)

data class WorkbenchConnectionSheetUiModel(
    val phase: WorkbenchConnectionPhase,
    val banner: WorkbenchConnectionBannerUiModel?,
    val filterQuery: String,
    val filteredDeviceCount: Int,
    val totalDeviceCount: Int,
    val deviceRows: List<WorkbenchConnectionDeviceRowUiModel>,
    val primaryActionLabel: String,
    val primaryAction: WorkbenchConnectionPrimaryAction,
    val selectedDeviceId: Long?,
)

enum class WorkbenchConnectionPrimaryAction {
    START_SCAN,
    STOP_SCAN,
    CONNECT_DEVICE,
    DISCONNECT,
    PROCESSING,
    RETRY,
    PERMISSION,
}

enum class WorkbenchConnectionCardPrimaryAction {
    CONNECT,
    VIEW,
    PROCESSING,
    READY,
    RETRY,
    PERMISSION,
}
