package com.alliot.osmo.demo.protocol.payload

sealed interface ProtocolPayload

data object EmptyPayload : ProtocolPayload

data class VersionResponsePayload(
    val ackResult: Int,
    val productId: String,
    val sdkVersion: String,
) : ProtocolPayload

data class KeyReportPayload(
    val keyCode: Int,
    val mode: Int,
    val keyValue: Int,
) : ProtocolPayload

data class RebootPayload(
    val deviceId: Long,
) : ProtocolPayload

data class RebootResponsePayload(
    val deviceId: Long,
    val retCode: Int,
) : ProtocolPayload

data class GpsDataPayload(
    val yearMonthDay: Int,
    val hourMinuteSecond: Int,
    val longitudeE7: Int,
    val latitudeE7: Int,
    val heightMm: Int,
    val speedNorthCmps: Float,
    val speedEastCmps: Float,
    val speedDownCmps: Float,
    val verticalAccuracyMm: Long,
    val horizontalAccuracyMm: Long,
    val speedAccuracyCmps: Long,
    val satelliteCount: Long,
) : ProtocolPayload

data class ConnectionRequestPayload(
    val deviceId: Long,
    val macAddress: ByteArray,
    val firmwareVersion: Long = 0,
    val connectionIndex: Int = 0,
    val verifyMode: Int = 0,
    val verifyData: Int = 0,
) : ProtocolPayload

data class ConnectionResponsePayload(
    val deviceId: Long,
    val retCode: Int,
    val cameraIndex: Long = 0,
) : ProtocolPayload

data class CameraConnectionConfirmationPayload(
    val deviceId: Long,
    val macAddress: ByteArray,
    val firmwareVersion: Long,
    val verifyMode: Int,
    val verifyData: Int,
) : ProtocolPayload

data class RecordControlPayload(
    val deviceId: Long,
    val recordControl: Int,
) : ProtocolPayload

data class AckPayload(
    val retCode: Int,
    val reserved: ByteArray = ByteArray(0),
) : ProtocolPayload

data class CameraModeSwitchPayload(
    val deviceId: Long,
    val mode: Int,
) : ProtocolPayload

data class CameraStatusSubscriptionPayload(
    val pushMode: Int,
    val pushFreq: Int,
) : ProtocolPayload

data class CameraStatusPayload(
    val cameraMode: Int,
    val cameraStatus: Int,
    val videoResolution: Int,
    val fpsIndex: Int,
    val eisMode: Int,
    val recordTimeSeconds: Int,
    val photoRatio: Int = 0,
    val realTimeCountdownSeconds: Int = 0,
    val timelapseIntervalDeciSeconds: Int = 0,
    val timelapseDurationSeconds: Int = 0,
    val remainCapacityMb: Long,
    val remainPhotoCount: Long,
    val remainTimeSeconds: Long,
    val userMode: Int,
    val powerMode: Int,
    val cameraModeNextFlag: Int = 0,
    val temperatureState: Int = 0,
    val photoCountdownMilliseconds: Long = 0,
    val loopRecordSeconds: Int = 0,
    val batteryPercent: Int,
) : ProtocolPayload

data class NewCameraStatusPayload(
    val modeName: String,
    val modeParameters: String,
) : ProtocolPayload

data class PowerModePayload(
    val powerMode: Int,
) : ProtocolPayload
