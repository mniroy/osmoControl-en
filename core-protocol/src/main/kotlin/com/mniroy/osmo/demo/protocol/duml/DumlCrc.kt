package com.mniroy.osmo.demo.protocol.duml

object DumlCrc {
    fun crc8(data: ByteArray): Int = reflectedCrc(
        data = data,
        width = 8,
        poly = 0x31,
        init = 0x77,
    )

    fun crc16(data: ByteArray): Int = reflectedCrc(
        data = data,
        width = 16,
        poly = 0x1021,
        init = 0x3692,
    )

    private fun reflectedCrc(
        data: ByteArray,
        width: Int,
        poly: Int,
        init: Int,
    ): Int {
        val mask = if (width == 32) -1 else (1 shl width) - 1
        val reflectedPoly = reflect(poly, width)
        var crc = init and mask

        for (byte in data) {
            crc = crc xor (byte.toInt() and 0xFF)
            repeat(8) {
                crc = if ((crc and 1) != 0) {
                    (crc ushr 1) xor reflectedPoly
                } else {
                    crc ushr 1
                }
            }
            crc = crc and mask
        }

        return crc and mask
    }

    private fun reflect(value: Int, width: Int): Int {
        var input = value
        var result = 0
        repeat(width) {
            result = (result shl 1) or (input and 1)
            input = input ushr 1
        }
        return result
    }
}
