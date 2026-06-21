package com.mniroy.osmo.demo.protocol.frame

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class DjiFrameCodecTest {
    @Test
    fun encode_matches_documented_mode_switch_example() {
        val frame = DjiFrame(
            cmdType = 0x01,
            seq = 0x0005,
            cmdSet = 0x1D,
            cmdId = 0x04,
            payload = byteArrayOf(0x00, 0x00, 0x33, 0xFF.toByte(), 0x0A, 0x01, 0x47, 0x39, 0x36),
        )

        val encoded = DjiFrameCodec.encode(frame)
        assertArrayEquals(
            byteArrayOf(
                0xAA.toByte(), 0x1B, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x05, 0x00, 0x57, 0xEE.toByte(),
                0x1D, 0x04, 0x00, 0x00, 0x33, 0xFF.toByte(), 0x0A, 0x01, 0x47, 0x39, 0x36,
                0xF4.toByte(), 0xFA.toByte(), 0xE1.toByte(), 0xD0.toByte(),
            ),
            encoded,
        )
    }

    @Test
    fun decode_round_trip_returns_original_fields() {
        val frame = DjiFrame(cmdType = 0x01, seq = 2, cmdSet = 0x00, cmdId = 0x1A, payload = byteArrayOf(0x03))
        val decoded = DjiFrameCodec.decode(DjiFrameCodec.encode(frame))

        assertEquals(frame.cmdSet, decoded.frame.cmdSet)
        assertEquals(frame.cmdId, decoded.frame.cmdId)
        assertArrayEquals(frame.payload, decoded.frame.payload)
    }
}
