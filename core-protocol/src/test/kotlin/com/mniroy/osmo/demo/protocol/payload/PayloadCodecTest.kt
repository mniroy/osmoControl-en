package com.mniroy.osmo.demo.protocol.payload

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class PayloadCodecTest {
    @Test
    fun connection_payload_encodes_expected_shape() {
        val payload = ConnectionRequestPayload(
            deviceId = 0xFF44,
            macAddress = byteArrayOf(0x12, 0x33, 0x44, 0x55, 0x66),
            verifyMode = 1,
            verifyData = 0x1234,
        )

        val encoded = PayloadCodec.encode(CommandIds.CMD_SET_COMMON, CommandIds.CONNECTION, payload)
        assertEquals(35, encoded.size)
        assertEquals(CommandIds.CMD_SET_COMMON.toByte(), encoded[0])
        assertEquals(CommandIds.CONNECTION.toByte(), encoded[1])
        assertEquals(0x44.toByte(), encoded[2])
        assertEquals(0xFF.toByte(), encoded[3])
    }

    @Test
    fun gps_payload_round_trips_through_encoder_prefix() {
        val payload = GpsDataPayload(
            yearMonthDay = 20260327,
            hourMinuteSecond = 163000,
            longitudeE7 = 1214737000,
            latitudeE7 = 312304000,
            heightMm = 15000,
            speedNorthCmps = 0f,
            speedEastCmps = 0f,
            speedDownCmps = 0f,
            verticalAccuracyMm = 500,
            horizontalAccuracyMm = 500,
            speedAccuracyCmps = 20,
            satelliteCount = 12,
        )

        val encoded = PayloadCodec.encode(CommandIds.CMD_SET_COMMON, CommandIds.GPS_PUSH, payload)
        assertArrayEquals(byteArrayOf(0x00, 0x17), encoded.copyOfRange(0, 2))
        assertEquals(50, encoded.size)
    }
}
