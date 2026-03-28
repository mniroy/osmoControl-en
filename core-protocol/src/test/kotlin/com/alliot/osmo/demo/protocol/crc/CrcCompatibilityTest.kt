package com.alliot.osmo.demo.protocol.crc

import org.junit.Assert.assertEquals
import org.junit.Test

class CrcCompatibilityTest {
    @Test
    fun crc16_matches_mode_switch_example_header() {
        val bytes = byteArrayOf(
            0xAA.toByte(), 0x1B, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x05, 0x00,
        )

        assertEquals(0xEE57, Crc16.calculate(bytes))
    }

    @Test
    fun crc32_matches_mode_switch_example_frame_body() {
        val bytes = byteArrayOf(
            0xAA.toByte(), 0x1B, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x05, 0x00, 0x57, 0xEE.toByte(),
            0x1D, 0x04, 0x00, 0x00, 0x33, 0xFF.toByte(), 0x0A, 0x01, 0x47, 0x39, 0x36,
        )

        assertEquals(0xD0E1FAF4L, Crc32.calculate(bytes))
    }
}
