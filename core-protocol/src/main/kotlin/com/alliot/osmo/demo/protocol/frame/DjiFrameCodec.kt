package com.alliot.osmo.demo.protocol.frame

import com.alliot.osmo.demo.protocol.crc.Crc16
import com.alliot.osmo.demo.protocol.crc.Crc32
import com.alliot.osmo.demo.protocol.util.ByteOrder

object DjiFrameCodec {
    private const val SOF = 0xAA
    private const val HEADER_SIZE = 12
    private const val CRC32_SIZE = 4

    fun encode(frame: DjiFrame): ByteArray {
        val totalLength = HEADER_SIZE + 2 + frame.payload.size + CRC32_SIZE
        val buffer = ByteArray(totalLength)
        buffer[0] = SOF.toByte()
        ByteOrder.writeU16(((frame.version and 0x3F) shl 10) or totalLength, buffer, 1)
        buffer[3] = frame.cmdType.toByte()
        buffer[4] = frame.enc.toByte()
        buffer[5] = 0
        buffer[6] = 0
        buffer[7] = 0
        ByteOrder.writeU16(frame.seq, buffer, 8)

        val crc16 = Crc16.calculate(buffer.copyOfRange(0, 10))
        ByteOrder.writeU16(crc16, buffer, 10)

        buffer[12] = frame.cmdSet.toByte()
        buffer[13] = frame.cmdId.toByte()
        frame.payload.copyInto(buffer, destinationOffset = 14)

        val crc32 = Crc32.calculate(buffer.copyOfRange(0, totalLength - CRC32_SIZE))
        ByteOrder.writeI32(crc32.toInt(), buffer, totalLength - CRC32_SIZE)
        return buffer
    }

    fun decode(bytes: ByteArray): DecodedDjiFrame {
        require(bytes.size >= 16) { "Frame too short" }
        require((bytes[0].toInt() and 0xFF) == SOF) { "Invalid SOF" }

        val verLength = ByteOrder.u16(bytes, 1)
        val expectedLength = verLength and 0x03FF
        require(expectedLength == bytes.size) { "Frame length mismatch" }

        val crc16 = ByteOrder.u16(bytes, 10)
        val calculatedCrc16 = Crc16.calculate(bytes.copyOfRange(0, 10))
        require(crc16 == calculatedCrc16) { "CRC16 mismatch" }

        val crc32 = ByteOrder.u32(bytes, bytes.size - 4)
        val calculatedCrc32 = Crc32.calculate(bytes.copyOfRange(0, bytes.size - 4))
        require(crc32 == calculatedCrc32) { "CRC32 mismatch" }

        val cmdSet = bytes[12].toInt() and 0xFF
        val cmdId = bytes[13].toInt() and 0xFF
        val payload = if (bytes.size > 18) bytes.copyOfRange(14, bytes.size - 4) else ByteArray(0)

        return DecodedDjiFrame(
            frame = DjiFrame(
                version = verLength ushr 10,
                cmdType = bytes[3].toInt() and 0xFF,
                enc = bytes[4].toInt() and 0xFF,
                seq = ByteOrder.u16(bytes, 8),
                cmdSet = cmdSet,
                cmdId = cmdId,
                payload = payload,
            ),
            crc16 = crc16,
            crc32 = crc32,
        )
    }
}
