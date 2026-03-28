package com.alliot.osmo.demo.protocol.crc

object Crc32 {
    private val table = IntArray(256).also { values ->
        var index = 0
        while (index < 256) {
            var current = index
            repeat(8) {
                current = if ((current and 1) != 0) {
                    (current ushr 1) xor 0xEDB88320.toInt()
                } else {
                    current ushr 1
                }
            }
            values[index] = current
            index++
        }
    }

    fun calculate(data: ByteArray): Long {
        var crc = 0x00003AA3
        for (byte in data) {
            val tableIndex = (crc xor (byte.toInt() and 0xFF)) and 0xFF
            crc = table[tableIndex] xor (crc ushr 8)
        }
        return crc.toLong() and 0xFFFF_FFFFL
    }
}
