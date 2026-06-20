package com.alliot.osmo.demo.app.ui.home

import com.alliot.osmo.demo.session.log.SessionLogEntry
import com.alliot.osmo.demo.session.model.CameraFamily
import com.alliot.osmo.demo.session.model.DeviceCapabilities
import com.alliot.osmo.demo.session.model.DeviceProfile
import com.alliot.osmo.demo.session.model.LogCategory
import com.alliot.osmo.demo.session.model.ProtocolFamily
import com.alliot.osmo.demo.session.model.SessionDevice
import com.alliot.osmo.demo.session.model.SessionStatus
import com.alliot.osmo.demo.session.model.SessionTransportMode
import com.alliot.osmo.demo.session.model.StatusPresentationStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugConsoleModelsTest {

    @Test
    fun `manual hex validation trims spaces and normalizes case`() {
        val result = validateManualHexInput(" aa 0b 1c ")

        assertTrue(result.isValid)
        assertEquals("AA0B1C", result.normalizedHex)
        assertEquals(null, result.errorMessage)
    }

    @Test
    fun `manual hex validation rejects invalid characters`() {
        val result = validateManualHexInput("0G 12")

        assertFalse(result.isValid)
        assertEquals(null, result.normalizedHex)
        assertEquals("Only hexadecimal characters and spaces are supported", result.errorMessage)
    }

    @Test
    fun `manual hex validation rejects odd length payload`() {
        val result = validateManualHexInput("ABC")

        assertFalse(result.isValid)
        assertEquals(null, result.normalizedHex)
        assertEquals("HEX must be entered in complete byte pairs", result.errorMessage)
    }

    @Test
    fun `grouped logs keep error expanded by default and honor clear cutoff`() {
        val grouped = groupDebugLogs(
            logs = listOf(
                SessionLogEntry(category = LogCategory.BLE, message = "ble", timestampMillis = 10L),
                SessionLogEntry(category = LogCategory.ERROR, message = "error", timestampMillis = 20L),
                SessionLogEntry(category = LogCategory.STATE, message = "state", timestampMillis = 30L),
            ),
            clearedAtMillis = 10L,
        )

        assertEquals(listOf(LogCategory.ERROR, LogCategory.STATE), grouped.map { it.category })
        assertTrue(grouped.first { it.category == LogCategory.ERROR }.expandedByDefault)
        assertFalse(grouped.first { it.category == LogCategory.STATE }.expandedByDefault)
        assertEquals(listOf("error"), grouped.first { it.category == LogCategory.ERROR }.entries.map { it.message })
    }

    @Test
    fun `debug only profile keeps restricted preset commands in debug console`() {
        val model = mapDebugConsoleUiModel(
            DebugHomeState(
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = SessionDevice(name = "Pocket 3", macAddress = "00:11", deviceId = 0L),
                    connectedProfile = debugProfile(
                        cameraFamily = CameraFamily.POCKET_3,
                        displayName = "Osmo Pocket 3",
                        capabilities = baseCapabilities(
                            supportsWorkbench = false,
                            supportsVersionQuery = true,
                            supportsDebugConsole = true,
                            statusPresentationStyle = StatusPresentationStyle.DEBUG_ONLY,
                        ),
                    ),
                    protocolReady = true,
                ),
            ),
        )

        assertEquals("Current device is not on the Workbench compatibility list, only verified debug commands are available.", model.capabilityNotice)
        assertEquals(listOf("Query Version"), model.coreActions.map(DebugConsoleActionUiModel::label))
        assertTrue(model.modeActions.isEmpty())
        assertTrue(model.keyActions.isEmpty())
        assertFalse(model.showGpsSection)
    }

    @Test
    fun `osmo 360 profile exposes extended debug mode actions`() {
        val model = mapDebugConsoleUiModel(
            DebugHomeState(
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = SessionDevice(name = "Osmo 360", macAddress = "22:33", deviceId = 0xFF66),
                    connectedProfile = debugProfile(
                        cameraFamily = CameraFamily.OSMO_360,
                        displayName = "Osmo 360",
                        capabilities = baseCapabilities(
                            supportedModes = linkedSetOf(0x01, 0x05, 0x38, 0x3F),
                            supportsWorkbench = true,
                            supportsModeSwitch = true,
                            supportsDirectRecord = true,
                            supportsRecordKey = true,
                            supportsGpsPush = true,
                            supportsSleep = true,
                            supportsWake = true,
                            supportsWakeAndSnapshot = true,
                            supportsVersionQuery = true,
                            supportsStateSubscribe = true,
                            supportsReboot = true,
                            supportsQsKey = true,
                            supportsSnapshotKey = true,
                            supportsDebugConsole = true,
                            statusPresentationStyle = StatusPresentationStyle.OSMO_360,
                        ),
                    ),
                    protocolReady = true,
                ),
            ),
        )

        assertEquals(listOf("Video", "Photo", "360 Panorama Video", "360 Panorama Photo"), model.modeActions.map(DebugConsoleActionUiModel::label))
        assertTrue(model.showGpsSection)
    }

    @Test
    fun `action profile exposes expanded debug mode actions`() {
        val model = mapDebugConsoleUiModel(
            DebugHomeState(
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = SessionDevice(name = "Osmo Action 5 Pro", macAddress = "22:33", deviceId = 0xFF44),
                    connectedProfile = debugProfile(
                        cameraFamily = CameraFamily.ACTION_5_PRO,
                        displayName = "Osmo Action 5 Pro",
                        capabilities = baseCapabilities(
                            supportedModes = linkedSetOf(0x00, 0x01, 0x02, 0x05, 0x0A, 0x28, 0x34),
                            supportsWorkbench = true,
                            supportsModeSwitch = true,
                            supportsDirectRecord = true,
                            supportsRecordKey = true,
                            supportsGpsPush = true,
                            supportsSleep = true,
                            supportsWake = true,
                            supportsWakeAndSnapshot = true,
                            supportsVersionQuery = true,
                            supportsStateSubscribe = true,
                            supportsReboot = true,
                            supportsQsKey = true,
                            supportsDebugConsole = true,
                            statusPresentationStyle = StatusPresentationStyle.ACTION_STANDARD,
                        ),
                    ),
                    protocolReady = true,
                ),
            ),
        )

        assertEquals(
            listOf("Slow Motion", "Video", "Timelapse", "Photo", "Motionlapse", "Night Mode", "ActiveTrack"),
            model.modeActions.map(DebugConsoleActionUiModel::label),
        )
    }

    @Test
    fun `snapshot key is not exposed and wake snapshot appears only while sleeping`() {
        val capabilities = baseCapabilities(
            supportedModes = linkedSetOf(0x01, 0x05),
            supportsWorkbench = true,
            supportsModeSwitch = true,
            supportsDirectRecord = true,
            supportsRecordKey = true,
            supportsGpsPush = true,
            supportsSleep = true,
            supportsWake = true,
            supportsWakeAndSnapshot = true,
            supportsVersionQuery = true,
            supportsStateSubscribe = true,
            supportsReboot = true,
            supportsQsKey = true,
            supportsSnapshotKey = true,
            supportsDebugConsole = true,
            statusPresentationStyle = StatusPresentationStyle.ACTION_STANDARD,
        )

        val awakeModel = mapDebugConsoleUiModel(
            DebugHomeState(
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = SessionDevice(name = "Osmo Action 5 Pro", macAddress = "22:33", deviceId = 0xFF44),
                    connectedProfile = debugProfile(
                        cameraFamily = CameraFamily.ACTION_5_PRO,
                        displayName = "Osmo Action 5 Pro",
                        capabilities = capabilities,
                    ),
                    protocolReady = true,
                    sleeping = false,
                ),
            ),
        )
        val sleepingModel = mapDebugConsoleUiModel(
            DebugHomeState(
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = SessionDevice(name = "Osmo Action 5 Pro", macAddress = "22:33", deviceId = 0xFF44),
                    connectedProfile = debugProfile(
                        cameraFamily = CameraFamily.ACTION_5_PRO,
                        displayName = "Osmo Action 5 Pro",
                        capabilities = capabilities,
                    ),
                    protocolReady = true,
                    sleeping = true,
                    wakeAdvertisingSupported = true,
                ),
            ),
        )

        assertEquals(listOf("Key Record", "QS Key"), awakeModel.keyActions.map(DebugConsoleActionUiModel::label))
        assertTrue(awakeModel.auxiliaryActions.isEmpty())
        assertEquals(listOf("Key Record", "QS Key"), sleepingModel.keyActions.map(DebugConsoleActionUiModel::label))
        assertEquals(listOf("Wake and Snap"), sleepingModel.auxiliaryActions.map(DebugConsoleActionUiModel::label))
    }

    @Test
    fun `direct record debug action uses shoot record key copy`() {
        val model = mapDebugConsoleUiModel(
            DebugHomeState(
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = SessionDevice(name = "Osmo Action 5 Pro", macAddress = "22:33", deviceId = 0xFF44),
                    connectedProfile = debugProfile(
                        cameraFamily = CameraFamily.ACTION_5_PRO,
                        displayName = "Osmo Action 5 Pro",
                        capabilities = baseCapabilities(
                            supportsWorkbench = true,
                            supportsDirectRecord = true,
                            supportsDebugConsole = true,
                            statusPresentationStyle = StatusPresentationStyle.ACTION_STANDARD,
                        ),
                    ),
                    protocolReady = true,
                ),
            ),
        )

        assertTrue(model.coreActions.any { it.actionType == DebugConsoleActionType.TOGGLE_RECORD && it.label == "Record Key" })
    }

    @Test
    fun `unsupported profile removes preset debug commands and keeps fallback notice`() {
        val model = mapDebugConsoleUiModel(
            DebugHomeState(
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = SessionDevice(name = "Unknown Device", macAddress = "44:55", deviceId = 0L),
                    connectedProfile = debugProfile(
                        cameraFamily = CameraFamily.UNKNOWN,
                        displayName = "Unknown Device",
                        capabilities = baseCapabilities(
                            supportsWorkbench = false,
                            supportsDebugConsole = false,
                            supportsVersionQuery = false,
                            statusPresentationStyle = StatusPresentationStyle.DEBUG_ONLY,
                        ),
                    ),
                    protocolReady = true,
                ),
            ),
        )

        assertEquals("Current device has no preset debug commands, use logs and manual messaging instead.", model.capabilityNotice)
        assertTrue(model.coreActions.isEmpty())
        assertTrue(model.modeActions.isEmpty())
        assertTrue(model.keyActions.isEmpty())
        assertFalse(model.showGpsSection)
    }

    @Test
    fun `device rows classify scan results by support level`() {
        val rows = mapDebugConsoleDeviceRows(
            DebugHomeState(
                discoveredDevices = listOf(
                    SessionDevice(
                        name = "Osmo Action 5 Pro",
                        macAddress = "00:11",
                        deviceId = 0xFF44,
                        inferredCameraFamily = CameraFamily.ACTION_5_PRO,
                        workbenchSupported = true,
                    ),
                    SessionDevice(
                        name = "DJI Mystery",
                        macAddress = "22:33",
                        deviceId = 0L,
                        inferredCameraFamily = CameraFamily.UNKNOWN,
                        inferredProtocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
                        workbenchSupported = false,
                    ),
                ),
            ),
        )

        assertEquals(listOf("Workbench Compatible", "Restricted Debug"), rows.map(DebugConsoleDeviceRowUiModel::capabilityLabel))
    }

    @Test
    fun `connected profile overrides device row capability label`() {
        val device = SessionDevice(
            name = "Unknown Device",
            macAddress = "44:55",
            deviceId = 0L,
            inferredProtocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
            workbenchSupported = false,
        )
        val rows = mapDebugConsoleDeviceRows(
            DebugHomeState(
                discoveredDevices = listOf(device),
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = device,
                    connectedProfile = debugProfile(
                        cameraFamily = CameraFamily.UNKNOWN,
                        displayName = "Unknown Device",
                        capabilities = baseCapabilities(
                            supportsWorkbench = false,
                            supportsDebugConsole = false,
                            statusPresentationStyle = StatusPresentationStyle.DEBUG_ONLY,
                        ),
                    ),
                ),
            ),
        )

        assertEquals("Log / Manual Only", rows.single().capabilityLabel)
    }

    @Test
    fun `connected session summary prefers resolved profile name and capability copy`() {
        val state = DebugHomeState(
            sessionStatus = SessionStatus(
                mode = SessionTransportMode.FAKE,
                connectedDevice = SessionDevice(name = "DJI Device", macAddress = "66:77", deviceId = 0L),
                connectedProfile = debugProfile(
                    cameraFamily = CameraFamily.POCKET_3,
                    displayName = "Osmo Pocket 3",
                    capabilities = baseCapabilities(
                        supportsWorkbench = false,
                        supportsVersionQuery = true,
                        supportsDebugConsole = true,
                        statusPresentationStyle = StatusPresentationStyle.DEBUG_ONLY,
                    ),
                ),
            ),
        )

        assertEquals("Osmo Pocket 3", debugConsoleConnectedDeviceName(state))
        assertEquals("Restricted Debug", debugConsoleConnectedCapabilityLabel(state))
    }
}

private fun debugProfile(
    cameraFamily: CameraFamily,
    displayName: String,
    capabilities: DeviceCapabilities,
): DeviceProfile {
    return DeviceProfile(
        protocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
        cameraFamily = cameraFamily,
        deviceId = null,
        productId = null,
        displayName = displayName,
        capabilities = capabilities,
    )
}

private fun baseCapabilities(
    supportsWorkbench: Boolean,
    supportsRecordKey: Boolean = false,
    supportsDirectRecord: Boolean = false,
    supportsModeSwitch: Boolean = false,
    supportedModes: Set<Int> = emptySet(),
    supportsGpsPush: Boolean = false,
    supportsSleep: Boolean = false,
    supportsWake: Boolean = false,
    supportsWakeAndSnapshot: Boolean = false,
    supportsVersionQuery: Boolean = false,
    supportsStateSubscribe: Boolean = false,
    supportsReboot: Boolean = false,
    supportsQsKey: Boolean = false,
    supportsSnapshotKey: Boolean = false,
    supportsDebugConsole: Boolean,
    statusPresentationStyle: StatusPresentationStyle,
): DeviceCapabilities {
    return DeviceCapabilities(
        supportsWorkbench = supportsWorkbench,
        supportsRecordKey = supportsRecordKey,
        supportsDirectRecord = supportsDirectRecord,
        supportsModeSwitch = supportsModeSwitch,
        supportedModes = supportedModes,
        supportsGpsPush = supportsGpsPush,
        supportsSleep = supportsSleep,
        supportsWake = supportsWake,
        supportsWakeAndSnapshot = supportsWakeAndSnapshot,
        supportsVersionQuery = supportsVersionQuery,
        supportsStateSubscribe = supportsStateSubscribe,
        supportsReboot = supportsReboot,
        supportsQsKey = supportsQsKey,
        supportsSnapshotKey = supportsSnapshotKey,
        supportsDebugConsole = supportsDebugConsole,
        statusPresentationStyle = statusPresentationStyle,
    )
}
