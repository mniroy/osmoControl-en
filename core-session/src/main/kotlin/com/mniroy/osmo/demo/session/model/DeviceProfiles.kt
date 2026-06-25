package com.mniroy.osmo.demo.session.model

private const val ACTION_4_DEVICE_ID = 0xFF33L
private const val ACTION_5_PRO_DEVICE_ID = 0xFF44L
private const val ACTION_6_DEVICE_ID = 0xFF55L
private const val OSMO_360_DEVICE_ID = 0xFF66L

private val ACTION_WORKBENCH_MODES = linkedSetOf(0x00, 0x01, 0x02, 0x05, 0x0A, 0x28, 0x34)
private val OSMO_360_WORKBENCH_MODES = linkedSetOf(0x01, 0x05, 0x38, 0x3A, 0x3C, 0x3F, 0x41, 0x43, 0x44, 0x4A)

private val ACTION_CAPABILITIES = DeviceCapabilities(
    supportsWorkbench = true,
    supportsRecordKey = true,
    supportsDirectRecord = true,
    supportsModeSwitch = true,
    supportedModes = ACTION_WORKBENCH_MODES,
    supportsGpsPush = true,
    supportsSleep = true,
    supportsWake = true,
    supportsWakeAndSnapshot = true,
    supportsVersionQuery = true,
    supportsStateSubscribe = true,
    supportsReboot = true,
    supportsQsKey = true,
    supportsSnapshotKey = false,
    supportsDebugConsole = true,
    statusPresentationStyle = StatusPresentationStyle.ACTION_STANDARD,
)

private val OSMO_360_CAPABILITIES = ACTION_CAPABILITIES.copy(
    supportedModes = OSMO_360_WORKBENCH_MODES,
    statusPresentationStyle = StatusPresentationStyle.OSMO_360,
)

private val DEBUG_ONLY_CAPABILITIES = DeviceCapabilities(
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
    supportsStateSubscribe = false,
    supportsReboot = false,
    supportsQsKey = false,
    supportsSnapshotKey = false,
    supportsDebugConsole = true,
    statusPresentationStyle = StatusPresentationStyle.DEBUG_ONLY,
)

// DJI RSDK devices whose camera family wasn't identified — GPS push still works on real hardware.
// supportsWorkbench=true is needed so debugOnlyProfile=false, which unblocks the GPS action gate.
private val RSDK_UNKNOWN_CAPABILITIES = DEBUG_ONLY_CAPABILITIES.copy(
    supportsWorkbench = true,
    supportsGpsPush = true,
)

private val UNKNOWN_CAPABILITIES = DEBUG_ONLY_CAPABILITIES.copy(
    supportsVersionQuery = false,
    supportsDebugConsole = false,
)

fun inferSessionDevice(
    name: String,
    macAddress: String,
    manufacturerData: ByteArray? = null,
): SessionDevice {
    val normalized = name.trim().lowercase()
    val hasRsdkSignature = hasDjiRsdkManufacturerSignature(manufacturerData)
    val identity = when {
        "pocket 3" in normalized || "pocket3" in normalized -> ScanIdentity(
            deviceId = 0L,
            protocolFamily = ProtocolFamily.POCKET3_DUML,
            cameraFamily = CameraFamily.POCKET_3,
            workbenchSupported = false,
        )
        "action 4" in normalized || "action4" in normalized -> ScanIdentity(
            deviceId = ACTION_4_DEVICE_ID,
            protocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
            cameraFamily = CameraFamily.ACTION_4,
            workbenchSupported = true,
        )
        "action 5" in normalized || "action5" in normalized -> ScanIdentity(
            deviceId = ACTION_5_PRO_DEVICE_ID,
            protocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
            cameraFamily = CameraFamily.ACTION_5_PRO,
            workbenchSupported = true,
        )
        "action 6" in normalized || "action6" in normalized -> ScanIdentity(
            deviceId = ACTION_6_DEVICE_ID,
            protocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
            cameraFamily = CameraFamily.ACTION_6,
            workbenchSupported = true,
        )
        "osmo 360" in normalized || normalized.contains("360") -> ScanIdentity(
            deviceId = OSMO_360_DEVICE_ID,
            protocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
            cameraFamily = CameraFamily.OSMO_360,
            workbenchSupported = true,
        )
        hasRsdkSignature || normalized.contains("osmo") || normalized.contains("action") -> ScanIdentity(
            deviceId = 0L,
            protocolFamily = ProtocolFamily.DJI_RSDK_ACTION,
            cameraFamily = CameraFamily.UNKNOWN,
            workbenchSupported = false,
        )
        else -> ScanIdentity(
            deviceId = 0L,
            protocolFamily = ProtocolFamily.UNKNOWN,
            cameraFamily = CameraFamily.UNKNOWN,
            workbenchSupported = false,
        )
    }

    return SessionDevice(
        name = name.ifBlank { "DJI Camera" },
        macAddress = macAddress,
        deviceId = identity.deviceId,
        inferredProtocolFamily = identity.protocolFamily,
        inferredCameraFamily = identity.cameraFamily,
        workbenchSupported = identity.workbenchSupported,
    )
}

fun resolveConnectedProfile(
    device: SessionDevice?,
    handshakeDeviceId: Long? = device?.deviceId,
    productId: String? = null,
): DeviceProfile? {
    if (device == null) return null

    val normalizedProductId = productId?.trim()?.takeIf(String::isNotEmpty)
    val resolvedFamily = when (handshakeDeviceId) {
        ACTION_4_DEVICE_ID -> CameraFamily.ACTION_4
        ACTION_5_PRO_DEVICE_ID -> CameraFamily.ACTION_5_PRO
        ACTION_6_DEVICE_ID -> CameraFamily.ACTION_6
        OSMO_360_DEVICE_ID -> CameraFamily.OSMO_360
        else -> device.inferredCameraFamily
    }
    val resolvedProtocolFamily = when {
        resolvedFamily == CameraFamily.POCKET_3 -> ProtocolFamily.POCKET3_DUML
        resolvedFamily != CameraFamily.UNKNOWN -> ProtocolFamily.DJI_RSDK_ACTION
        device.inferredProtocolFamily != ProtocolFamily.UNKNOWN -> device.inferredProtocolFamily
        else -> ProtocolFamily.UNKNOWN
    }
    val capabilities = capabilitiesFor(
        protocolFamily = resolvedProtocolFamily,
        cameraFamily = resolvedFamily,
    )
    val fallbackDisplayName = when {
        normalizedProductId != null -> normalizedProductId
        device.name.isNotBlank() -> device.name
        else -> displayNameFor(resolvedFamily)
    }

    return DeviceProfile(
        protocolFamily = resolvedProtocolFamily,
        cameraFamily = resolvedFamily,
        deviceId = handshakeDeviceId?.takeIf { it != 0L },
        productId = normalizedProductId,
        displayName = fallbackDisplayName,
        capabilities = capabilities,
    )
}

private fun capabilitiesFor(
    protocolFamily: ProtocolFamily,
    cameraFamily: CameraFamily,
): DeviceCapabilities {
    return when {
        protocolFamily == ProtocolFamily.POCKET3_DUML -> DEBUG_ONLY_CAPABILITIES
        cameraFamily == CameraFamily.ACTION_4 ||
            cameraFamily == CameraFamily.ACTION_5_PRO ||
            cameraFamily == CameraFamily.ACTION_6 -> ACTION_CAPABILITIES
        cameraFamily == CameraFamily.OSMO_360 -> OSMO_360_CAPABILITIES
        protocolFamily == ProtocolFamily.DJI_RSDK_ACTION -> RSDK_UNKNOWN_CAPABILITIES
        else -> UNKNOWN_CAPABILITIES
    }
}

private fun displayNameFor(cameraFamily: CameraFamily): String {
    return when (cameraFamily) {
        CameraFamily.ACTION_4 -> "Osmo Action 4"
        CameraFamily.ACTION_5_PRO -> "Osmo Action 5 Pro"
        CameraFamily.ACTION_6 -> "Osmo Action 6"
        CameraFamily.OSMO_360 -> "Osmo 360"
        CameraFamily.POCKET_3 -> "Osmo Pocket 3"
        CameraFamily.UNKNOWN -> "DJI Camera"
    }
}

private fun hasDjiRsdkManufacturerSignature(manufacturerData: ByteArray?): Boolean {
    return manufacturerData != null &&
        manufacturerData.size >= 5 &&
        (manufacturerData[0].toInt() and 0xFF) == 0xAA &&
        (manufacturerData[1].toInt() and 0xFF) == 0x08 &&
        (manufacturerData[4].toInt() and 0xFF) == 0xFA
}

private data class ScanIdentity(
    val deviceId: Long,
    val protocolFamily: ProtocolFamily,
    val cameraFamily: CameraFamily,
    val workbenchSupported: Boolean,
)
