package com.alliot.osmo.demo.protocol.duml

data class DumlUnpackedString(
    val value: String,
    val bytesRead: Int,
)

object DumlStringCodec {
    fun pack(value: String): ByteArray {
        val utf8 = value.toByteArray(Charsets.UTF_8)
        require(utf8.size <= 0xFF) { "String too long for DUML packing." }
        return byteArrayOf(utf8.size.toByte()) + utf8
    }

    fun unpack(bytes: ByteArray, offset: Int = 0): DumlUnpackedString {
        require(offset in 0..bytes.size) { "Offset out of bounds." }
        if (offset == bytes.size) {
            return DumlUnpackedString("", 0)
        }
        val length = bytes[offset].toInt() and 0xFF
        require(offset + 1 + length <= bytes.size) { "Truncated DUML string." }
        return DumlUnpackedString(
            value = bytes.copyOfRange(offset + 1, offset + 1 + length).toString(Charsets.UTF_8),
            bytesRead = 1 + length,
        )
    }
}
