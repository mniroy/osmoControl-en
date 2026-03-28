package com.alliot.osmo.demo.protocol.duml

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class DumlStringCodecTest {
    @Test
    fun pack_prefixes_utf8_length() {
        assertArrayEquals(
            byteArrayOf(4, '5'.code.toByte(), '1'.code.toByte(), '6'.code.toByte(), '0'.code.toByte()),
            DumlStringCodec.pack("5160"),
        )
    }

    @Test
    fun unpack_reads_length_prefixed_string() {
        val packed = byteArrayOf(4, 'l'.code.toByte(), 'o'.code.toByte(), 'v'.code.toByte(), 'e'.code.toByte())
        val unpacked = DumlStringCodec.unpack(packed, 0)

        assertEquals("love", unpacked.value)
        assertEquals(5, unpacked.bytesRead)
    }
}
