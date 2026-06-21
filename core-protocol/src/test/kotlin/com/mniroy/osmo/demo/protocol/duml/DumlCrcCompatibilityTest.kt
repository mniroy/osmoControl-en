package com.mniroy.osmo.demo.protocol.duml

import org.junit.Assert.assertEquals
import org.junit.Test

class DumlCrcCompatibilityTest {
    @Test
    fun crc8_matches_pocket3_reference_header() {
        val header = byteArrayOf(0x55, 0x22, 0x04)
        assertEquals(0xEA, DumlCrc.crc8(header))
    }

    @Test
    fun crc16_matches_pocket3_reference_frame_without_trailer() {
        val bytes = hex("552204ea020780924007450f3030313734393331393238363130320435313630")
        assertEquals(0x422E, DumlCrc.crc16(bytes))
    }

    private fun hex(value: String): ByteArray {
        require(value.length % 2 == 0) { "Hex string must have even length." }
        return ByteArray(value.length / 2) { index ->
            value.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }
}
