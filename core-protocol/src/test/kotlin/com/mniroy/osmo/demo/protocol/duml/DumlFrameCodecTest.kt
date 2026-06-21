package com.mniroy.osmo.demo.protocol.duml

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class DumlFrameCodecTest {
    @Test
    fun encode_builds_reference_pairing_message() {
        val frame = DumlFrame.request(
            target = DumlTargets.APP_TO_WIFI,
            messageId = 0x8092,
            cmdSet = DumlCmdSet.WIFI,
            cmdId = DumlWifiCmd.SET_PAIRING_PIN,
            payload = DumlStringCodec.pack("001749319286102") + DumlStringCodec.pack("5160"),
        )

        assertArrayEquals(
            hex("552204ea020780924007450f30303137343933313932383631303204353136302e42"),
            DumlFrameCodec.encode(frame),
        )
    }

    @Test
    fun decode_round_trip_returns_original_fields() {
        val frame = DumlFrame.request(
            target = DumlTargets.APP_TO_GIMBAL,
            messageId = 0x0102,
            cmdSet = DumlCmdSet.GIMBAL,
            cmdId = DumlGimbalCmd.POSITION_TELEMETRY,
            payload = hex("85ff05002c030100123456"),
        )

        val decoded = DumlFrameCodec.decode(DumlFrameCodec.encode(frame))

        assertEquals(frame.target, decoded.target)
        assertEquals(frame.messageId, decoded.messageId)
        assertEquals(frame.flags, decoded.flags)
        assertEquals(frame.cmdSet, decoded.cmdSet)
        assertEquals(frame.cmdId, decoded.cmdId)
        assertArrayEquals(frame.payload, decoded.payload)
    }

    private fun hex(value: String): ByteArray {
        require(value.length % 2 == 0) { "Hex string must have even length." }
        return ByteArray(value.length / 2) { index ->
            value.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }
}
