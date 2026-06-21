package com.mniroy.osmo.demo.protocol.payload

import com.mniroy.osmo.demo.protocol.util.ByteOrder
import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN
import kotlin.math.min

object PayloadCodec {
    fun encode(cmdSet: Int, cmdId: Int, payload: ProtocolPayload): ByteArray {
        val body = when {
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.VERSION_QUERY && payload is EmptyPayload -> ByteArray(0)
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.KEY_REPORT && payload is KeyReportPayload -> {
                ByteArray(4).also {
                    it[0] = payload.keyCode.toByte()
                    it[1] = payload.mode.toByte()
                    ByteOrder.writeU16(payload.keyValue, it, 2)
                }
            }
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.REBOOT && payload is RebootPayload -> {
                ByteBuffer.allocate(8).order(LITTLE_ENDIAN).apply {
                    putInt(payload.deviceId.toInt())
                    repeat(4) { put(0) }
                }.array()
            }
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.GPS_PUSH && payload is GpsDataPayload -> {
                ByteBuffer.allocate(48).order(LITTLE_ENDIAN).apply {
                    putInt(payload.yearMonthDay)
                    putInt(payload.hourMinuteSecond)
                    putInt(payload.longitudeE7)
                    putInt(payload.latitudeE7)
                    putInt(payload.heightMm)
                    putFloat(payload.speedNorthCmps)
                    putFloat(payload.speedEastCmps)
                    putFloat(payload.speedDownCmps)
                    putInt(payload.verticalAccuracyMm.toInt())
                    putInt(payload.horizontalAccuracyMm.toInt())
                    putInt(payload.speedAccuracyCmps.toInt())
                    putInt(payload.satelliteCount.toInt())
                }.array()
            }
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.CONNECTION && payload is ConnectionRequestPayload -> {
                ByteBuffer.allocate(33).order(LITTLE_ENDIAN).apply {
                    putInt(payload.deviceId.toInt())
                    put(payload.macAddress.size.toByte())
                    repeat(16) { index ->
                        put(payload.macAddress.getOrNull(index) ?: 0)
                    }
                    putInt(payload.firmwareVersion.toInt())
                    put(payload.connectionIndex.toByte())
                    put(payload.verifyMode.toByte())
                    putShort(payload.verifyData.toShort())
                    repeat(4) { put(0) }
                }.array()
            }
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.CONNECTION && payload is CameraConnectionConfirmationPayload -> {
                ByteBuffer.allocate(33).order(LITTLE_ENDIAN).apply {
                    putInt(payload.deviceId.toInt())
                    put(payload.macAddress.size.toByte())
                    repeat(16) { index ->
                        put(payload.macAddress.getOrNull(index) ?: 0)
                    }
                    putInt(payload.firmwareVersion.toInt())
                    put(0)
                    put(payload.verifyMode.toByte())
                    putShort(payload.verifyData.toShort())
                    repeat(4) { put(0) }
                }.array()
            }
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.CONNECTION && payload is ConnectionResponsePayload -> {
                ByteBuffer.allocate(9).order(LITTLE_ENDIAN).apply {
                    putInt(payload.deviceId.toInt())
                    put(payload.retCode.toByte())
                    putInt(payload.cameraIndex.toInt())
                }.array()
            }
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.POWER_MODE && payload is PowerModePayload -> byteArrayOf(payload.powerMode.toByte())
            cmdSet == CommandIds.CMD_SET_CAMERA && cmdId == CommandIds.RECORD_CONTROL && payload is RecordControlPayload -> {
                ByteBuffer.allocate(9).order(LITTLE_ENDIAN).apply {
                    putInt(payload.deviceId.toInt())
                    put(payload.recordControl.toByte())
                    repeat(4) { put(0) }
                }.array()
            }
            cmdSet == CommandIds.CMD_SET_CAMERA && cmdId == CommandIds.CAMERA_MODE_SWITCH && payload is CameraModeSwitchPayload -> {
                ByteBuffer.allocate(9).order(LITTLE_ENDIAN).apply {
                    putInt(payload.deviceId.toInt())
                    put(payload.mode.toByte())
                    repeat(4) { put(0) }
                }.array()
            }
            cmdSet == CommandIds.CMD_SET_CAMERA && cmdId == CommandIds.CAMERA_STATUS_SUBSCRIPTION && payload is CameraStatusSubscriptionPayload -> {
                byteArrayOf(
                    payload.pushMode.toByte(),
                    payload.pushFreq.toByte(),
                    0,
                    0,
                    0,
                    0,
                )
            }
            cmdSet == CommandIds.CMD_SET_CAMERA && cmdId == CommandIds.CAMERA_STATUS_PUSH && payload is CameraStatusPayload -> {
                ByteBuffer.allocate(38).order(LITTLE_ENDIAN).apply {
                    put(payload.cameraMode.toByte())
                    put(payload.cameraStatus.toByte())
                    put(payload.videoResolution.toByte())
                    put(payload.fpsIndex.toByte())
                    put(payload.eisMode.toByte())
                    putShort(payload.recordTimeSeconds.toShort())
                    put(0)
                    put(payload.photoRatio.toByte())
                    putShort(payload.realTimeCountdownSeconds.toShort())
                    putShort(payload.timelapseIntervalDeciSeconds.toShort())
                    putShort(payload.timelapseDurationSeconds.toShort())
                    putInt(payload.remainCapacityMb.toInt())
                    putInt(payload.remainPhotoCount.toInt())
                    putInt(payload.remainTimeSeconds.toInt())
                    put(payload.userMode.toByte())
                    put(payload.powerMode.toByte())
                    put(payload.cameraModeNextFlag.toByte())
                    put(payload.temperatureState.toByte())
                    putInt(payload.photoCountdownMilliseconds.toInt())
                    putShort(payload.loopRecordSeconds.toShort())
                    put(payload.batteryPercent.toByte())
                }.array()
            }
            payload is AckPayload -> byteArrayOf(payload.retCode.toByte()) + payload.reserved
            else -> error("Unsupported payload: cmdSet=${cmdSet.toString(16)}, cmdId=${cmdId.toString(16)}, payload=$payload")
        }

        return byteArrayOf(cmdSet.toByte(), cmdId.toByte()) + body
    }

    fun decode(cmdSet: Int, cmdId: Int, bytes: ByteArray, isCommandFrame: Boolean = false): ProtocolPayload {
        return when {
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.VERSION_QUERY -> {
                val ack = if (bytes.size >= 2) ByteOrder.u16(bytes, 0) else 0
                val product = if (bytes.size >= 18) bytes.copyOfRange(2, 18).decodeToString().trimEnd('\u0000') else ""
                val version = if (bytes.size > 18) bytes.copyOfRange(18, bytes.size).decodeToString().trimEnd('\u0000') else ""
                VersionResponsePayload(ack, product, version)
            }
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.KEY_REPORT -> {
                KeyReportPayload(
                    keyCode = bytes.getOrElse(0) { 0 }.toInt() and 0xFF,
                    mode = bytes.getOrElse(1) { 0 }.toInt() and 0xFF,
                    keyValue = if (bytes.size >= 4) ByteOrder.u16(bytes, 2) else 0,
                )
            }
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.REBOOT && bytes.size >= 5 -> {
                RebootResponsePayload(
                    deviceId = ByteOrder.u32(bytes, 0),
                    retCode = bytes[4].toInt() and 0xFF,
                )
            }
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.GPS_PUSH -> AckPayload(bytes.firstOrNull()?.toInt()?.and(0xFF) ?: 0)
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.CONNECTION && isCommandFrame && bytes.size >= 33 -> {
                val macLength = bytes[4].toInt() and 0xFF
                CameraConnectionConfirmationPayload(
                    deviceId = ByteOrder.u32(bytes, 0),
                    macAddress = bytes.copyOfRange(5, 5 + macLength.coerceAtMost(16)),
                    firmwareVersion = ByteOrder.u32(bytes, 21),
                    verifyMode = bytes[26].toInt() and 0xFF,
                    verifyData = ByteOrder.u16(bytes, 27),
                )
            }
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.CONNECTION && bytes.size >= 5 -> {
                ConnectionResponsePayload(
                    deviceId = ByteOrder.u32(bytes, 0),
                    retCode = bytes[4].toInt() and 0xFF,
                    cameraIndex = if (bytes.size >= 9) ByteOrder.u32(bytes, 5) else 0,
                )
            }
            cmdSet == CommandIds.CMD_SET_COMMON && cmdId == CommandIds.POWER_MODE -> AckPayload(bytes.firstOrNull()?.toInt()?.and(0xFF) ?: 0)
            cmdSet == CommandIds.CMD_SET_CAMERA && cmdId == CommandIds.RECORD_CONTROL -> AckPayload(bytes.firstOrNull()?.toInt()?.and(0xFF) ?: 0, bytes.drop(1).toByteArray())
            cmdSet == CommandIds.CMD_SET_CAMERA && cmdId == CommandIds.CAMERA_MODE_SWITCH -> AckPayload(bytes.firstOrNull()?.toInt()?.and(0xFF) ?: 0, bytes.drop(1).toByteArray())
            cmdSet == CommandIds.CMD_SET_CAMERA && cmdId == CommandIds.CAMERA_STATUS_SUBSCRIPTION -> decode(CommandIds.CMD_SET_CAMERA, CommandIds.CAMERA_STATUS_PUSH, bytes)
            cmdSet == CommandIds.CMD_SET_CAMERA && cmdId == CommandIds.CAMERA_STATUS_PUSH && bytes.size >= 38 -> {
                CameraStatusPayload(
                    cameraMode = bytes[0].toInt() and 0xFF,
                    cameraStatus = bytes[1].toInt() and 0xFF,
                    videoResolution = bytes[2].toInt() and 0xFF,
                    fpsIndex = bytes[3].toInt() and 0xFF,
                    eisMode = bytes[4].toInt() and 0xFF,
                    recordTimeSeconds = ByteOrder.u16(bytes, 5),
                    photoRatio = bytes[8].toInt() and 0xFF,
                    realTimeCountdownSeconds = ByteOrder.u16(bytes, 9),
                    timelapseIntervalDeciSeconds = ByteOrder.u16(bytes, 11),
                    timelapseDurationSeconds = ByteOrder.u16(bytes, 13),
                    remainCapacityMb = ByteOrder.u32(bytes, 15),
                    remainPhotoCount = ByteOrder.u32(bytes, 19),
                    remainTimeSeconds = ByteOrder.u32(bytes, 23),
                    userMode = bytes[27].toInt() and 0xFF,
                    powerMode = bytes[28].toInt() and 0xFF,
                    cameraModeNextFlag = bytes[29].toInt() and 0xFF,
                    temperatureState = bytes[30].toInt() and 0xFF,
                    photoCountdownMilliseconds = ByteOrder.u32(bytes, 31),
                    loopRecordSeconds = ByteOrder.u16(bytes, 35),
                    batteryPercent = bytes[37].toInt() and 0xFF,
                )
            }
            cmdSet == CommandIds.CMD_SET_CAMERA && cmdId == CommandIds.NEW_CAMERA_STATUS_PUSH -> {
                val modeNameLength = bytes.getOrElse(1) { 0 }.toInt() and 0xFF
                val modeNameStart = 2
                val modeNameEnd = min(bytes.size, modeNameStart + modeNameLength)
                val modeName = bytes.copyOfRange(modeNameStart, modeNameEnd).decodeToString()
                val parameterTypeIndex = min(bytes.size, modeNameEnd)
                val parameterLength = bytes.getOrElse(parameterTypeIndex + 1) { 0 }.toInt() and 0xFF
                val parameterStart = min(bytes.size, parameterTypeIndex + 2)
                val parameterEnd = min(bytes.size, parameterStart + parameterLength)
                val parameters = bytes.copyOfRange(parameterStart, parameterEnd).decodeToString()
                NewCameraStatusPayload(modeName, parameters)
            }
            else -> AckPayload(bytes.firstOrNull()?.toInt()?.and(0xFF) ?: 0, bytes.drop(1).toByteArray())
        }
    }
}
