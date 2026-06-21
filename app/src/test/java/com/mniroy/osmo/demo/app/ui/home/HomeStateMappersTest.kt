package com.mniroy.osmo.demo.app.ui.home

import com.mniroy.osmo.demo.session.log.SessionLogEntry
import com.mniroy.osmo.demo.session.model.LogCategory
import com.mniroy.osmo.demo.session.model.HandshakeStage
import com.mniroy.osmo.demo.session.model.CameraFamily
import com.mniroy.osmo.demo.session.model.DeviceCapabilities
import com.mniroy.osmo.demo.session.model.DeviceProfile
import com.mniroy.osmo.demo.session.model.ProtocolFamily
import com.mniroy.osmo.demo.session.model.SessionDevice
import com.mniroy.osmo.demo.session.model.SessionStatus
import com.mniroy.osmo.demo.session.model.SessionTransportMode
import com.mniroy.osmo.demo.session.model.StatusPresentationStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeStateMappersTest {

    @Test
    fun `not connected disables workbench controls and shows reason`() {
        val state = DebugHomeState(
            sessionStatus = SessionStatus(mode = SessionTransportMode.FAKE),
        )

        val model = mapWorkbenchUiModel(state)

        assertEquals("Device Not Connected", model.connectionSummary)
        assertFalse(model.recordActionEnabled)
        assertEquals("Please connect device first", model.recordActionDisabledReason)
        assertFalse(model.gpsActionEnabled)
        assertEquals("Please connect device first", model.gpsActionDisabledReason)
        assertFalse(model.modeSwitchEnabled)
        assertEquals("Please connect device first", model.modeSwitchBlockedReason)
    }

    @Test
    fun `protocol ready enables record mode and gps quick actions`() {
        val state = DebugHomeState(
            sessionStatus = SessionStatus(
                mode = SessionTransportMode.FAKE,
                connectedDevice = SessionDevice(name = "Cam", macAddress = "00:00", deviceId = 1L),
                protocolReady = true,
            ),
        )

        val model = mapWorkbenchUiModel(state)

        assertTrue(model.recordActionEnabled)
        assertNull(model.recordActionDisabledReason)
        assertTrue(model.gpsActionEnabled)
        assertNull(model.gpsActionDisabledReason)
        assertTrue(model.modeSwitchEnabled)
        assertNull(model.modeSwitchBlockedReason)
    }

    @Test
    fun `protocol ready with debug only profile keeps workbench actions blocked`() {
        val debugOnlyProfile = DeviceProfile(
            protocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
            cameraFamily = CameraFamily.UNKNOWN,
            deviceId = null,
            productId = null,
            displayName = "Unknown Camera",
            capabilities = DeviceCapabilities(
                supportsWorkbench = false,
                supportsRecordKey = false,
                supportsDirectRecord = false,
                supportsModeSwitch = false,
                supportedModes = emptySet(),
                supportsGpsPush = false,
                supportsSleep = false,
                supportsWake = false,
                supportsWakeAndSnapshot = false,
                supportsVersionQuery = true,
                supportsDebugConsole = true,
                statusPresentationStyle = StatusPresentationStyle.DEBUG_ONLY,
            ),
        )
        val state = DebugHomeState(
            sessionStatus = SessionStatus(
                mode = SessionTransportMode.FAKE,
                connectedDevice = SessionDevice(name = "Cam", macAddress = "00:00", deviceId = 1L),
                connectedProfile = debugOnlyProfile,
                protocolReady = true,
            ),
        )

        val model = mapWorkbenchUiModel(state)

        assertFalse(model.recordActionEnabled)
        assertEquals("Current device only supports debug control", model.recordActionDisabledReason)
        assertFalse(model.gpsActionEnabled)
        assertEquals("Current device only supports debug control", model.gpsActionDisabledReason)
        assertFalse(model.modeSwitchEnabled)
        assertEquals("Current device only supports debug control", model.modeSwitchBlockedReason)
    }

    @Test
    fun `debug only profile hides workbench quick action sections`() {
        val debugOnlyProfile = DeviceProfile(
            protocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
            cameraFamily = CameraFamily.UNKNOWN,
            deviceId = null,
            productId = null,
            displayName = "Unknown Camera",
            capabilities = DeviceCapabilities(
                supportsWorkbench = false,
                supportsRecordKey = false,
                supportsDirectRecord = false,
                supportsModeSwitch = false,
                supportedModes = emptySet(),
                supportsGpsPush = false,
                supportsSleep = false,
                supportsWake = false,
                supportsWakeAndSnapshot = false,
                supportsVersionQuery = true,
                supportsDebugConsole = true,
                statusPresentationStyle = StatusPresentationStyle.DEBUG_ONLY,
            ),
        )
        val model = mapWorkbenchUiModel(
            DebugHomeState(
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = SessionDevice(name = "Cam", macAddress = "00:00", deviceId = 1L),
                    connectedProfile = debugOnlyProfile,
                    protocolReady = true,
                ),
            ),
        )

        assertFalse(model.showModeQuickAction)
        assertFalse(model.showGpsQuickAction)
        assertFalse(model.showDeviceActionsQuickAction)
        assertTrue(model.modeOptions.isEmpty())
        assertFalse(model.deviceActionsUiModel.showSleepAction)
        assertFalse(model.deviceActionsUiModel.showVersionAction)
        assertFalse(model.deviceActionsUiModel.showQsAction)
        assertFalse(model.deviceActionsUiModel.showSnapshotAction)
    }

    @Test
    fun `osmo 360 profile exposes extended workbench mode options`() {
        val osmo360Profile = DeviceProfile(
            protocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
            cameraFamily = CameraFamily.OSMO_360,
            deviceId = 0xFF66,
            productId = "OSMO360",
            displayName = "Osmo 360",
            capabilities = DeviceCapabilities(
                supportsWorkbench = true,
                supportsRecordKey = true,
                supportsDirectRecord = true,
                supportsModeSwitch = true,
                supportedModes = linkedSetOf(0x01, 0x05, 0x38, 0x3F),
                supportsGpsPush = true,
                supportsSleep = true,
                supportsWake = true,
                supportsWakeAndSnapshot = true,
                supportsVersionQuery = true,
                supportsDebugConsole = true,
                statusPresentationStyle = StatusPresentationStyle.OSMO_360,
            ),
        )
        val model = mapWorkbenchUiModel(
            DebugHomeState(
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = SessionDevice(name = "Osmo 360", macAddress = "00:00", deviceId = 0xFF66),
                    connectedProfile = osmo360Profile,
                    protocolReady = true,
                ),
            ),
        )

        assertTrue(model.showModeQuickAction)
        assertEquals(listOf(0x01, 0x05, 0x38, 0x3F), model.modeOptions.map(WorkbenchModeOptionUiModel::mode))
        assertEquals(listOf("Video", "Photo", "360 Panorama Video", "360 Panorama Photo"), model.modeOptions.map(WorkbenchModeOptionUiModel::label))
    }

    @Test
    fun `action profile exposes expanded workbench mode options`() {
        val actionProfile = DeviceProfile(
            protocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
            cameraFamily = CameraFamily.ACTION_5_PRO,
            deviceId = 0xFF44,
            productId = "OA5P",
            displayName = "Osmo Action 5 Pro",
            capabilities = DeviceCapabilities(
                supportsWorkbench = true,
                supportsRecordKey = true,
                supportsDirectRecord = true,
                supportsModeSwitch = true,
                supportedModes = linkedSetOf(0x00, 0x01, 0x02, 0x05, 0x0A, 0x28, 0x34),
                supportsGpsPush = true,
                supportsSleep = true,
                supportsWake = true,
                supportsWakeAndSnapshot = true,
                supportsVersionQuery = true,
                supportsDebugConsole = true,
                statusPresentationStyle = StatusPresentationStyle.ACTION_STANDARD,
            ),
        )
        val model = mapWorkbenchUiModel(
            DebugHomeState(
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = SessionDevice(name = "Osmo Action 5 Pro", macAddress = "00:00", deviceId = 0xFF44),
                    connectedProfile = actionProfile,
                    protocolReady = true,
                ),
            ),
        )

        assertEquals(
            listOf(0x00, 0x01, 0x02, 0x05, 0x0A, 0x28, 0x34),
            model.modeOptions.map(WorkbenchModeOptionUiModel::mode),
        )
        assertEquals(
            listOf("Slow Motion", "Video", "Timelapse", "Photo", "Motionlapse", "Night Mode", "ActiveTrack"),
            model.modeOptions.map(WorkbenchModeOptionUiModel::label),
        )
    }

    @Test
    fun `device actions hide wake buttons when wake capability is absent`() {
        val limitedProfile = DeviceProfile(
            protocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
            cameraFamily = CameraFamily.ACTION_5_PRO,
            deviceId = 0xFF44,
            productId = "OA5P",
            displayName = "Osmo Action 5 Pro",
            capabilities = DeviceCapabilities(
                supportsWorkbench = true,
                supportsRecordKey = true,
                supportsDirectRecord = true,
                supportsModeSwitch = true,
                supportedModes = linkedSetOf(0x01, 0x05),
                supportsGpsPush = true,
                supportsSleep = true,
                supportsWake = false,
                supportsWakeAndSnapshot = false,
                supportsVersionQuery = true,
                supportsQsKey = true,
                supportsSnapshotKey = true,
                supportsDebugConsole = true,
                statusPresentationStyle = StatusPresentationStyle.ACTION_STANDARD,
            ),
        )
        val model = mapWorkbenchUiModel(
            DebugHomeState(
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = SessionDevice(name = "Osmo Action 5 Pro", macAddress = "00:00", deviceId = 0xFF44),
                    connectedProfile = limitedProfile,
                    protocolReady = true,
                    handshakeStage = HandshakeStage.COMPLETED,
                ),
            ),
        )

        assertTrue(model.showDeviceActionsQuickAction)
        assertTrue(model.deviceActionsUiModel.showSleepAction)
        assertFalse(model.deviceActionsUiModel.showWakeAction)
        assertFalse(model.deviceActionsUiModel.showWakeAndSnapshotAction)
        assertTrue(model.deviceActionsUiModel.showVersionAction)
        assertTrue(model.deviceActionsUiModel.showQsAction)
        assertFalse(model.deviceActionsUiModel.showSnapshotAction)
    }

    @Test
    fun `wake and snapshot workbench action only appears while sleeping`() {
        val profile = DeviceProfile(
            protocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
            cameraFamily = CameraFamily.ACTION_5_PRO,
            deviceId = 0xFF44,
            productId = "OA5P",
            displayName = "Osmo Action 5 Pro",
            capabilities = DeviceCapabilities(
                supportsWorkbench = true,
                supportsRecordKey = true,
                supportsDirectRecord = true,
                supportsModeSwitch = true,
                supportedModes = linkedSetOf(0x01, 0x05),
                supportsGpsPush = true,
                supportsSleep = true,
                supportsWake = true,
                supportsWakeAndSnapshot = true,
                supportsVersionQuery = true,
                supportsQsKey = true,
                supportsSnapshotKey = true,
                supportsDebugConsole = true,
                statusPresentationStyle = StatusPresentationStyle.ACTION_STANDARD,
            ),
        )

        val awakeModel = mapWorkbenchUiModel(
            DebugHomeState(
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = SessionDevice(name = "Osmo Action 5 Pro", macAddress = "00:00", deviceId = 0xFF44),
                    connectedProfile = profile,
                    protocolReady = true,
                    handshakeStage = HandshakeStage.COMPLETED,
                    sleeping = false,
                ),
            ),
        )
        val sleepingModel = mapWorkbenchUiModel(
            DebugHomeState(
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = SessionDevice(name = "Osmo Action 5 Pro", macAddress = "00:00", deviceId = 0xFF44),
                    connectedProfile = profile,
                    protocolReady = true,
                    handshakeStage = HandshakeStage.COMPLETED,
                    sleeping = true,
                    wakeAdvertisingSupported = true,
                ),
            ),
        )

        assertFalse(awakeModel.deviceActionsUiModel.showWakeAndSnapshotAction)
        assertFalse(awakeModel.deviceActionsUiModel.showSnapshotAction)
        assertTrue(sleepingModel.deviceActionsUiModel.showWakeAndSnapshotAction)
        assertFalse(sleepingModel.deviceActionsUiModel.showSnapshotAction)
    }

    @Test
    fun `sleeping session surfaces sleep state and disables non wake actions`() {
        val profile = DeviceProfile(
            protocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
            cameraFamily = CameraFamily.ACTION_5_PRO,
            deviceId = 0xFF44,
            productId = "OA5P",
            displayName = "Osmo Action 5 Pro",
            capabilities = DeviceCapabilities(
                supportsWorkbench = true,
                supportsRecordKey = true,
                supportsDirectRecord = true,
                supportsModeSwitch = true,
                supportedModes = linkedSetOf(0x01, 0x05),
                supportsGpsPush = true,
                supportsSleep = true,
                supportsWake = true,
                supportsWakeAndSnapshot = true,
                supportsVersionQuery = true,
                supportsQsKey = true,
                supportsSnapshotKey = true,
                supportsDebugConsole = true,
                statusPresentationStyle = StatusPresentationStyle.ACTION_STANDARD,
            ),
        )
        val device = SessionDevice(name = "SleepCam", macAddress = "AA:BB:CC:DD:EE:44", deviceId = 0xFF44)
        val state = DebugHomeState(
            selectedMode = SessionTransportMode.FAKE,
            sessionStatus = SessionStatus(
                mode = SessionTransportMode.FAKE,
                connectedDevice = device,
                connectedProfile = profile,
                protocolReady = true,
                handshakeStage = HandshakeStage.COMPLETED,
                sleeping = true,
                wakeAdvertisingSupported = true,
            ),
            discoveredDevices = listOf(device),
        )

        val model = mapWorkbenchUiModel(state)
        val cardModel = mapWorkbenchConnectionCardUiModel(state)
        val sheetModel = mapWorkbenchConnectionSheetUiModel(state)

        assertEquals("Device Sleeping", model.connectionSummary)
        assertFalse(model.recordActionEnabled)
        assertEquals("Device Sleeping", model.recordActionDisabledReason)
        assertFalse(model.gpsActionEnabled)
        assertEquals("Device Sleeping", model.gpsActionDisabledReason)
        assertFalse(model.modeSwitchEnabled)
        assertEquals("Device Sleeping", model.modeSwitchBlockedReason)
        assertFalse(model.deviceActionsUiModel.deviceActionsEnabled)
        assertTrue(model.deviceActionsUiModel.wakeActionsEnabled)
        assertEquals("Device Sleeping", primaryActionStatusText(state))
        assertEquals("Device Sleeping", cardModel.statusCopy)
        assertEquals("Device is sleeping, can be woken up", cardModel.supportingCopy)
        assertEquals("Sleeping", sheetModel.deviceRows.first().statusLabel)
        assertEquals("Device has slept, can be woken up", sheetModel.banner?.message)
    }

    @Test
    fun `connected but not ready blocks actions with handshake reason`() {
        val state = DebugHomeState(
            sessionStatus = SessionStatus(
                mode = SessionTransportMode.FAKE,
                connectedDevice = SessionDevice(name = "Cam", macAddress = "00:00", deviceId = 1L),
                protocolReady = false,
            ),
        )

        val model = mapWorkbenchUiModel(state)

        assertEquals("Device Connected", model.connectionSummary)
        assertFalse(model.recordActionEnabled)
        assertEquals("Device Preparing", model.recordActionDisabledReason)
        assertFalse(model.gpsActionEnabled)
        assertEquals("Device Preparing", model.gpsActionDisabledReason)
        assertFalse(model.modeSwitchEnabled)
        assertEquals("Device Preparing", model.modeSwitchBlockedReason)
    }

    @Test
    fun `scanning blocks actions with scanning reason`() {
        val state = DebugHomeState(
            sessionStatus = SessionStatus(
                mode = SessionTransportMode.FAKE,
                scanning = true,
                protocolReady = false,
            ),
        )

        val model = mapWorkbenchUiModel(state)

        assertEquals("Searching for devices", model.connectionSummary)
        assertFalse(model.recordActionEnabled)
        assertEquals("Searching for devices", model.recordActionDisabledReason)
        assertFalse(model.gpsActionEnabled)
        assertEquals("Searching for devices", model.gpsActionDisabledReason)
        assertFalse(model.modeSwitchEnabled)
        assertEquals("Searching for devices", model.modeSwitchBlockedReason)
    }

    @Test
    fun `recording state disables mode switch with explicit reason`() {
        val state = DebugHomeState(
            sessionStatus = SessionStatus(
                mode = SessionTransportMode.FAKE,
                connectedDevice = SessionDevice(name = "Cam", macAddress = "00:00", deviceId = 1L),
                protocolReady = true,
            ),
            cameraStatus = com.mniroy.osmo.demo.session.model.CameraStatusSnapshot(recording = true),
        )

        val model = mapWorkbenchUiModel(state)

        assertFalse(model.modeSwitchEnabled)
        assertEquals("Recording", model.modeSwitchBlockedReason)
    }

    @Test
    fun `photo mode primary action copy uses photo semantics`() {
        val snapshot = com.mniroy.osmo.demo.session.model.CameraStatusSnapshot(
            mode = 0x05,
            modeLabel = "Photo",
        )

        assertTrue(isPhotoCaptureMode(snapshot))
        assertEquals("Photo", primaryActionButtonLabel(snapshot, enabled = true))
        assertEquals("Photo", primaryActionContentDescription(snapshot))
    }

    @Test
    fun `photo mode active state does not surface stop recording copy`() {
        val snapshot = com.mniroy.osmo.demo.session.model.CameraStatusSnapshot(
            mode = 0x05,
            modeLabel = "Photo",
            recording = true,
        )

        assertEquals("Taking Photo", primaryActionButtonLabel(snapshot, enabled = true))
        assertEquals("Taking Photo", primaryActionContentDescription(snapshot))
    }

    @Test
    fun `status overview hides default camera snapshot until protocol ready`() {
        val state = DebugHomeState(
            sessionStatus = SessionStatus(mode = SessionTransportMode.FAKE),
        )

        val model = mapWorkbenchUiModel(state)

        assertEquals(
            listOf("--:--", "--:--", "--", "--", "Waiting for device status", "Waiting for device status"),
            model.statusOverviewItems.map(StatusOverviewItem::value),
        )
    }

    @Test
    fun `status overview items keep the merged 6 metric cockpit summary`() {
        val state = DebugHomeState(
            sessionStatus = SessionStatus(
                mode = SessionTransportMode.FAKE,
                connectedDevice = SessionDevice(name = "Cam", macAddress = "00:00", deviceId = 1L),
                protocolReady = true,
                gpsAutoPushEnabled = true,
                gpsAutoPushHz = 10,
            ),
            cameraStatus = com.mniroy.osmo.demo.session.model.CameraStatusSnapshot(
                recording = true,
                recordTimeSeconds = 95,
                remainTimeSeconds = 610,
                remainCapacityMb = 2048,
                batteryPercent = 82,
                videoResolution = 1,
                fpsIndex = 5,
                eisMode = 1,
            ),
        )

        val model = mapWorkbenchUiModel(state)

        assertEquals(
            listOf("Recording Time", "Remaining Time", "Remaining Capacity", "Battery", "Resolution / FPS", "Stabilization / GPS"),
            model.statusOverviewItems.map(StatusOverviewItem::title),
        )
        assertEquals("01:35", model.statusOverviewItems[0].value)
        assertEquals("10:10", model.statusOverviewItems[1].value)
        assertEquals("2048MB", model.statusOverviewItems[2].value)
        assertEquals("82%", model.statusOverviewItems[3].value)
        assertEquals("4K / 60fps", model.statusOverviewItems[4].value)
        assertEquals("RockSteady / 10Hz", model.statusOverviewItems[5].value)
    }

    @Test
    fun `gps detail items expose altitude speed and bearing`() {
        val items = mapGpsDetailItems(
            SessionStatus(
                mode = SessionTransportMode.FAKE,
                lastGpsAltitudeMeters = 18.5,
                lastGpsSpeedMps = 12.5f,
                lastGpsBearingDegrees = 137.2f,
            ),
        )

        assertEquals(
            listOf("Location Status", "Location Source", "Altitude", "Horizontal Accuracy", "Speed", "Bearing"),
            items.map(StatusOverviewItem::title),
        )
        assertEquals(
            listOf("No Location", "None", "18.5m", "None", "45.0km/h", "137°"),
            items.map(StatusOverviewItem::value),
        )
    }

    @Test
    fun `real mode permission denial exposes request or settings CTA`() {
        val requestCta = mapPermissionCta(
            DebugHomeState(
                selectedMode = SessionTransportMode.REAL,
                prerequisites = RealModePrerequisites(
                    bluetoothPermissionsGranted = false,
                    locationPermissionGranted = false,
                    requiresSettingsAction = false,
                ),
            ),
        )

        assertEquals(PermissionAction.REQUEST, requestCta?.action)
        assertEquals("Grant Permission", requestCta?.label)

        val settingsCta = mapPermissionCta(
            DebugHomeState(
                selectedMode = SessionTransportMode.REAL,
                prerequisites = RealModePrerequisites(
                    bluetoothPermissionsGranted = false,
                    locationPermissionGranted = false,
                    requiresSettingsAction = true,
                ),
            ),
        )

        assertEquals(PermissionAction.OPEN_SETTINGS, settingsCta?.action)
        assertEquals("Go to Settings", settingsCta?.label)
    }

    @Test
    fun `permission CTA null when not in real mode`() {
        val cta = mapPermissionCta(DebugHomeState(selectedMode = SessionTransportMode.FAKE))
        assertNull(cta)
    }

    @Test
    fun `permission CTA null when permissions already granted`() {
        val cta = mapPermissionCta(
            DebugHomeState(
                selectedMode = SessionTransportMode.REAL,
                prerequisites = RealModePrerequisites(
                    bluetoothPermissionsGranted = true,
                    locationPermissionGranted = true,
                ),
            ),
        )
        assertNull(cta)
    }

    @Test
    fun `recent events only keep the newest 3 supported items`() {
        val events = mapRecentEvents(
            listOf(
                SessionLogEntry(LogCategory.TX, "ignored", timestampMillis = 1L),
                SessionLogEntry(LogCategory.STATE, "Handshake started", timestampMillis = 2L),
                SessionLogEntry(LogCategory.ERROR, "GPS failed", timestampMillis = 3L),
                SessionLogEntry(LogCategory.STATE, "Connected", timestampMillis = 4L),
                SessionLogEntry(LogCategory.STATE, "Recording", timestampMillis = 5L),
            ),
        )

        assertEquals(3, events.size)
        assertEquals("Recording", events[0].message)
        assertEquals("Device Connected", events[1].message)
        assertEquals("GPS failed", events[2].message)
    }

    @Test
    fun `derived properties reflect raw state`() {
        val state = DebugHomeState(
            logs = listOf(SessionLogEntry(LogCategory.STATE, "Connected", timestampMillis = 10L)),
            selectedMode = SessionTransportMode.REAL,
            prerequisites = RealModePrerequisites(
                bluetoothPermissionsGranted = false,
                locationPermissionGranted = false,
            ),
        )

        assertEquals("Device Not Connected", state.workbenchUiModel.connectionSummary)
        assertFalse(state.workbenchUiModel.recordActionEnabled)
        assertEquals(1, state.workbenchUiModel.recentEvents.size)
        assertEquals("Device Connected", state.workbenchUiModel.recentEvents.first().message)
        assertEquals(PermissionAction.REQUEST, state.permissionCta?.action)
    }

    @Test
    fun `connection card idle state shows idle copy`() {
        val model = mapWorkbenchConnectionCardUiModel(
            DebugHomeState(
                selectedMode = SessionTransportMode.FAKE,
                sessionStatus = SessionStatus(mode = SessionTransportMode.FAKE),
            ),
        )

        assertEquals(WorkbenchConnectionPhase.IDLE, model.phase)
        assertEquals("Connect Device", model.statusCopy)
        assertEquals("Connect", model.primaryActionLabel)
        assertEquals("Discover and connect device via top panel", model.supportingCopy)
    }

    @Test
    fun `connection card scanning state shows scanning copy`() {
        val model = mapWorkbenchConnectionCardUiModel(
            DebugHomeState(
                selectedMode = SessionTransportMode.FAKE,
                sessionStatus = SessionStatus(mode = SessionTransportMode.FAKE, scanning = true),
            ),
        )

        assertEquals(WorkbenchConnectionPhase.SCANNING, model.phase)
        assertEquals("Searching for devices", model.statusCopy)
        assertEquals("View", model.primaryActionLabel)
        assertEquals("Looking for nearby devices", model.supportingCopy)
    }

    @Test
    fun `connection card connecting and preparing states emit in progress copy`() {
        val device = SessionDevice(name = "Camera", macAddress = "AA:BB:CC:DD:EE:FF", deviceId = 1L)

        val connectingState = DebugHomeState(
            selectedMode = SessionTransportMode.FAKE,
            sessionStatus = SessionStatus(
                mode = SessionTransportMode.FAKE,
                connectedDevice = device,
                handshakeStage = HandshakeStage.REQUEST_SENT,
            ),
        )
        val connectingModel = mapWorkbenchConnectionCardUiModel(connectingState)

        assertEquals(WorkbenchConnectionPhase.CONNECTING, connectingModel.phase)
        assertEquals("Connecting to device", connectingModel.statusCopy)
        assertEquals("Processing", connectingModel.primaryActionLabel)
        assertEquals("Establishing connection with device", connectingModel.supportingCopy)

        val preparingState = connectingState.copy(
            sessionStatus = SessionStatus(
                mode = SessionTransportMode.FAKE,
                connectedDevice = device,
                handshakeStage = HandshakeStage.CAMERA_CONFIRMATION_RECEIVED,
            ),
        )
        val preparingModel = mapWorkbenchConnectionCardUiModel(preparingState)

        assertEquals(WorkbenchConnectionPhase.PREPARING, preparingModel.phase)
        assertEquals("Device connected, preparing control", preparingModel.statusCopy)
        assertEquals("Processing", preparingModel.primaryActionLabel)
        assertEquals("Waiting for device to confirm protocol", preparingModel.supportingCopy)
    }

    @Test
    fun `connection card ready state shows device name and ready copy`() {
        val device = SessionDevice(name = "DroneCam", macAddress = "AA:BB:CC:DD:EE:01", deviceId = 2L)
        val model = mapWorkbenchConnectionCardUiModel(
            DebugHomeState(
                selectedMode = SessionTransportMode.FAKE,
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = device,
                    protocolReady = true,
                    handshakeStage = HandshakeStage.COMPLETED,
                ),
            ),
        )

        assertEquals(WorkbenchConnectionPhase.READY, model.phase)
        assertEquals("DroneCam", model.statusCopy)
        assertEquals("Connected", model.primaryActionLabel)
        assertEquals("Bluetooth connected, control available", model.supportingCopy)
    }

    @Test
    fun `connection card failure state surfaces error copy`() {
        val model = mapWorkbenchConnectionCardUiModel(
            DebugHomeState(
                selectedMode = SessionTransportMode.FAKE,
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    latestError = "Connection timed out",
                ),
            ),
        )

        assertEquals(WorkbenchConnectionPhase.FAILURE, model.phase)
        assertEquals("Connection Incomplete", model.statusCopy)
        assertEquals("Retry", model.primaryActionLabel)
        assertEquals("Connection timed out · Retryable", model.supportingCopy)
    }

    @Test
    fun `connection card permission issue shows permission copy before errors`() {
        val state = DebugHomeState(
            selectedMode = SessionTransportMode.REAL,
            sessionStatus = SessionStatus(
                mode = SessionTransportMode.REAL,
                latestError = "Low energy off",
            ),
            prerequisites = RealModePrerequisites(
                bluetoothPermissionsGranted = false,
                locationPermissionGranted = false,
            ),
        )
        val model = mapWorkbenchConnectionCardUiModel(state)

        assertEquals("Requires Bluetooth/Location permissions", model.supportingCopy)
    }

    @Test
    fun `connection card permission action uses CTA label`() {
        val state = DebugHomeState(
            selectedMode = SessionTransportMode.REAL,
            prerequisites = RealModePrerequisites(
                bluetoothPermissionsGranted = false,
                locationPermissionGranted = false,
                requiresSettingsAction = true,
            ),
        )
        val model = mapWorkbenchConnectionCardUiModel(state)

        assertEquals("Go to Settings", model.primaryActionLabel)
        assertEquals(WorkbenchConnectionCardPrimaryAction.PERMISSION, model.primaryAction)
        assertEquals(PermissionAction.OPEN_SETTINGS, model.permissionAction)
    }

    @Test
    fun `connection sheet scanning state shows neutral banner and stop scan action`() {
        val model = mapWorkbenchConnectionSheetUiModel(
            DebugHomeState(
                selectedMode = SessionTransportMode.FAKE,
                sessionStatus = SessionStatus(mode = SessionTransportMode.FAKE, scanning = true),
            ),
        )

        assertEquals(WorkbenchConnectionPhase.SCANNING, model.phase)
        assertEquals("Stop Scan", model.primaryActionLabel)
        assertEquals(WorkbenchConnectionPrimaryAction.STOP_SCAN, model.primaryAction)
        assertEquals(WorkbenchConnectionBannerType.NEUTRAL, model.banner?.type)
        assertEquals("Updating nearby devices", model.banner?.message)
    }

    @Test
    fun `connection sheet ready state shows success banner device rows and disconnect action`() {
        val device = SessionDevice(name = "ReadyCam", macAddress = "AA:BB:CC:DD:EE:22", deviceId = 3L)
        val model = mapWorkbenchConnectionSheetUiModel(
            DebugHomeState(
                selectedMode = SessionTransportMode.FAKE,
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = device,
                    protocolReady = true,
                    handshakeStage = HandshakeStage.COMPLETED,
                ),
                discoveredDevices = listOf(device),
            ),
        )

        assertEquals(WorkbenchConnectionPhase.READY, model.phase)
        assertEquals("Disconnect", model.primaryActionLabel)
        assertEquals(WorkbenchConnectionBannerType.SUCCESS, model.banner?.type)
        assertEquals("Current device is ready for control", model.banner?.message)
        assertEquals(1, model.deviceRows.size)
        val row = model.deviceRows.first()
        assertEquals(device.deviceId, row.deviceId)
        assertEquals("ReadyCam", row.name)
        assertEquals(WorkbenchConnectionBannerType.SUCCESS, model.banner?.type)
        assertEquals("Connected", row.statusLabel)
        assertTrue(row.isSelected)
        assertEquals(device.deviceId, model.selectedDeviceId)
        assertEquals(WorkbenchConnectionPrimaryAction.DISCONNECT, model.primaryAction)
        assertEquals("Disconnect", model.primaryActionLabel)
    }

    @Test
    fun `connection sheet failure banner exposes retry scan copy`() {
        val model = mapWorkbenchConnectionSheetUiModel(
            DebugHomeState(
                selectedMode = SessionTransportMode.FAKE,
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    latestError = "BLE error",
                ),
            ),
        )

        assertEquals(WorkbenchConnectionBannerType.ERROR, model.banner?.type)
        assertEquals("Rescan", model.banner?.actionLabel)
        assertEquals("BLE error", model.banner?.message)
    }

    @Test
    fun `connection sheet filters devices by name and reports counts`() {
        val first = SessionDevice(name = "Osmo Action 5", macAddress = "AA:BB:CC:DD:EE:11", deviceId = 1L)
        val second = SessionDevice(name = "Pocket 3", macAddress = "AA:BB:CC:DD:EE:22", deviceId = 2L)

        val model = mapWorkbenchConnectionSheetUiModel(
            DebugHomeState(
                selectedMode = SessionTransportMode.FAKE,
                sessionStatus = SessionStatus(mode = SessionTransportMode.FAKE),
                discoveredDevices = listOf(first, second),
                deviceFilterQuery = "action",
            ),
        )

        assertEquals("action", model.filterQuery)
        assertEquals(1, model.filteredDeviceCount)
        assertEquals(2, model.totalDeviceCount)
        assertEquals(listOf(first.deviceId), model.deviceRows.map(WorkbenchConnectionDeviceRowUiModel::deviceId))
    }

    @Test
    fun `workbench connection sheet hides unsupported devices`() {
        val supported = SessionDevice(name = "Osmo Action 5", macAddress = "AA:BB:CC:DD:EE:11", deviceId = 1L)
        val unsupported = SessionDevice(
            name = "Pocket 3",
            macAddress = "AA:BB:CC:DD:EE:22",
            deviceId = 2L,
            workbenchSupported = false,
        )

        val model = mapWorkbenchConnectionSheetUiModel(
            DebugHomeState(
                selectedMode = SessionTransportMode.FAKE,
                destination = HomeDestination.WORKBENCH,
                sessionStatus = SessionStatus(mode = SessionTransportMode.FAKE),
                discoveredDevices = listOf(unsupported, supported),
            ),
        )

        assertEquals(1, model.filteredDeviceCount)
        assertEquals(2, model.totalDeviceCount)
        assertEquals(listOf(supported.deviceId), model.deviceRows.map(WorkbenchConnectionDeviceRowUiModel::deviceId))
    }

    @Test
    fun `workbench connection sheet keeps ambiguous dji rsdk devices visible for handshake`() {
        val supported = SessionDevice(name = "Osmo Action 5", macAddress = "AA:BB:CC:DD:EE:11", deviceId = 1L)
        val ambiguousRsdk = SessionDevice(
            name = "DJI Camera",
            macAddress = "AA:BB:CC:DD:EE:33",
            deviceId = 0L,
            inferredProtocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
            workbenchSupported = false,
        )

        val model = mapWorkbenchConnectionSheetUiModel(
            DebugHomeState(
                selectedMode = SessionTransportMode.FAKE,
                destination = HomeDestination.WORKBENCH,
                sessionStatus = SessionStatus(mode = SessionTransportMode.FAKE),
                discoveredDevices = listOf(ambiguousRsdk, supported),
            ),
        )

        assertEquals(2, model.filteredDeviceCount)
        assertEquals(2, model.totalDeviceCount)
        assertEquals(
            listOf(ambiguousRsdk.deviceId, supported.deviceId),
            model.deviceRows.map(WorkbenchConnectionDeviceRowUiModel::deviceId),
        )
    }

    @Test
    fun `connection sheet filter matches compact mac query`() {
        val device = SessionDevice(name = "Osmo Action 5", macAddress = "AA:BB:CC:DD:EE:11", deviceId = 1L)

        val model = mapWorkbenchConnectionSheetUiModel(
            DebugHomeState(
                selectedMode = SessionTransportMode.FAKE,
                sessionStatus = SessionStatus(mode = SessionTransportMode.FAKE),
                discoveredDevices = listOf(device),
                deviceFilterQuery = "ccddee11",
            ),
        )

        assertEquals(1, model.deviceRows.size)
        assertEquals(device.macAddress, model.deviceRows.single().deviceKey)
    }

    @Test
    fun `connection sheet row shows connecting status even when ids diverge but mac matches`() {
        val candidate = SessionDevice(name = "Candidate", macAddress = "AA:BB:CC:99:11", deviceId = 11L)
        val connectedDevice = SessionDevice(name = "Candidate", macAddress = candidate.macAddress, deviceId = 999L)
        val model = mapWorkbenchConnectionSheetUiModel(
            DebugHomeState(
                selectedMode = SessionTransportMode.FAKE,
                selectedConnectionDeviceId = candidate.deviceId,
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = connectedDevice,
                    handshakeStage = HandshakeStage.REQUEST_SENT,
                ),
                discoveredDevices = listOf(candidate),
            ),
        )

        val row = model.deviceRows.first()
        assertTrue(row.isSelected)
        assertEquals("Connecting", row.statusLabel)
        assertEquals(candidate.deviceId, model.selectedDeviceId)
    }

    @Test
    fun `connection sheet connecting phase uses processing action`() {
        val device = SessionDevice(name = "PendingCam", macAddress = "AA:BB:CC:DD:EE:44", deviceId = 5L)
        val model = mapWorkbenchConnectionSheetUiModel(
            DebugHomeState(
                selectedMode = SessionTransportMode.FAKE,
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = device,
                    handshakeStage = HandshakeStage.REQUEST_SENT,
                ),
                discoveredDevices = listOf(device),
            ),
        )

        assertEquals(WorkbenchConnectionPrimaryAction.PROCESSING, model.primaryAction)
        assertEquals("Processing", model.primaryActionLabel)
        assertEquals(device.deviceId, model.selectedDeviceId)
        assertEquals("Connecting", model.deviceRows.first().statusLabel)
    }

    @Test
    fun `connection sheet preparing phase uses processing action`() {
        val device = SessionDevice(name = "PrepareCam", macAddress = "AA:BB:CC:DD:EE:55", deviceId = 6L)
        val model = mapWorkbenchConnectionSheetUiModel(
            DebugHomeState(
                selectedMode = SessionTransportMode.FAKE,
                sessionStatus = SessionStatus(
                    mode = SessionTransportMode.FAKE,
                    connectedDevice = device,
                    handshakeStage = HandshakeStage.CAMERA_CONFIRMATION_RECEIVED,
                ),
                discoveredDevices = listOf(device),
            ),
        )

        assertEquals(WorkbenchConnectionPrimaryAction.PROCESSING, model.primaryAction)
        assertEquals("Processing", model.primaryActionLabel)
        assertEquals(device.deviceId, model.selectedDeviceId)
        assertEquals("Preparing", model.deviceRows.first().statusLabel)
    }

    @Test
    fun `connection sheet permission banner overrides failure banner`() {
        val state = DebugHomeState(
            selectedMode = SessionTransportMode.REAL,
            sessionStatus = SessionStatus(
                mode = SessionTransportMode.REAL,
                latestError = "BLE disabled",
            ),
            prerequisites = RealModePrerequisites(
                bluetoothPermissionsGranted = false,
                locationPermissionGranted = false,
            ),
        )

        val model = mapWorkbenchConnectionSheetUiModel(state)

        assertEquals(WorkbenchConnectionBannerType.PERMISSION, model.banner?.type)
        assertEquals("Requires Bluetooth/Location permissions", model.banner?.message)
        assertEquals("Authorize", model.banner?.actionLabel)
    }

    @Test
    fun `connection sheet permission banner records request action semantics`() {
        val state = DebugHomeState(
            selectedMode = SessionTransportMode.REAL,
            prerequisites = RealModePrerequisites(
                bluetoothPermissionsGranted = false,
                locationPermissionGranted = false,
                requiresSettingsAction = false,
            ),
        )
        val model = mapWorkbenchConnectionSheetUiModel(state)

        assertEquals(WorkbenchConnectionPrimaryAction.PERMISSION, model.primaryAction)
        assertEquals("Grant Permission", model.primaryActionLabel)
        assertEquals("Requires Bluetooth/Location permissions", model.banner?.message)
        assertEquals("Authorize", model.banner?.actionLabel)
        assertEquals(PermissionAction.REQUEST, model.banner?.permissionAction)
    }

    @Test
    fun `connection sheet selected device exposes connect action`() {
        val device = SessionDevice(name = "SelectCam", macAddress = "AA:BB:CC:DD:EE:33", deviceId = 4L)
        val state = DebugHomeState(
            selectedMode = SessionTransportMode.FAKE,
            sessionStatus = SessionStatus(mode = SessionTransportMode.FAKE),
            discoveredDevices = listOf(device),
            selectedConnectionDeviceId = device.deviceId,
        )
        val model = mapWorkbenchConnectionSheetUiModel(state)

        assertEquals("Connect Device", model.primaryActionLabel)
        assertEquals(device.deviceId, model.selectedDeviceId)
        assertEquals(1, model.deviceRows.size)
        val row = model.deviceRows.first()
        assertTrue(row.isSelected)
        assertEquals("Selected", row.statusLabel)
    }

    @Test
    fun `derived properties expose new connection models`() {
        val state = DebugHomeState(
            selectedMode = SessionTransportMode.FAKE,
            sessionStatus = SessionStatus(mode = SessionTransportMode.FAKE),
        )

        assertEquals(mapWorkbenchConnectionCardUiModel(state), state.workbenchConnectionCardUiModel)
        assertEquals(mapWorkbenchConnectionSheetUiModel(state), state.workbenchConnectionSheetUiModel)
    }
}
