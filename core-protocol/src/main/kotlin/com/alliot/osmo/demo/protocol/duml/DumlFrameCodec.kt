package com.alliot.osmo.demo.protocol.duml

import com.alliot.osmo.demo.protocol.util.ByteOrder

object DumlFrameCodec {
    fun encode(frame: DumlFrame): ByteArray {
        val totalLength = DumlConstants.MIN_FRAME_SIZE + frame.payload.size
        val buffer = ByteArray(totalLength)

        buffer[0] = DumlConstants.SOF.toByte()
        buffer[1] = (totalLength and 0xFF).toByte()
        buffer[2] = (((frame.version and 0x3F) shl 2) or ((totalLength ushr 8) and 0x03)).toByte()
        buffer[3] = DumlCrc.crc8(buffer.copyOfRange(0, 3)).toByte()
        ByteOrder.writeU16(frame.target, buffer, 4)
        writeU16Be(frame.messageId, buffer, 6)
        buffer[8] = frame.flags.toByte()
        buffer[9] = frame.cmdSet.toByte()
        buffer[10] = frame.cmdId.toByte()
        frame.payload.copyInto(buffer, destinationOffset = DumlConstants.HEADER_SIZE)

        val crc16Offset = totalLength - DumlConstants.CRC16_SIZE
        ByteOrder.writeU16(DumlCrc.crc16(buffer.copyOfRange(0, crc16Offset)), buffer, crc16Offset)
        return buffer
    }

    fun decode(bytes: ByteArray): DumlDecodedFrame {
        require(bytes.size >= DumlConstants.MIN_FRAME_SIZE) { "Frame too short" }
        require((bytes[0].toInt() and 0xFF) == DumlConstants.SOF) { "Invalid SOF" }

        val totalLength = (bytes[1].toInt() and 0xFF) or ((bytes[2].toInt() and 0x03) shl 8)
        require(totalLength == bytes.size) { "Frame length mismatch" }

        val crc8 = bytes[3].toInt() and 0xFF
        val expectedCrc8 = DumlCrc.crc8(bytes.copyOfRange(0, 3))
        require(crc8 == expectedCrc8) { "CRC8 mismatch" }

        val crc16 = ByteOrder.u16(bytes, bytes.size - 2)
        val expectedCrc16 = DumlCrc.crc16(bytes.copyOfRange(0, bytes.size - 2))
        require(crc16 == expectedCrc16) { "CRC16 mismatch" }

        val target = ByteOrder.u16(bytes, 4)
        return DumlDecodedFrame(
            target = target,
            sender = target and 0xFF,
            receiver = (target ushr 8) and 0xFF,
            messageId = u16Be(bytes, 6),
            flags = bytes[8].toInt() and 0xFF,
            cmdSet = bytes[9].toInt() and 0xFF,
            cmdId = bytes[10].toInt() and 0xFF,
            payload = bytes.copyOfRange(DumlConstants.HEADER_SIZE, bytes.size - DumlConstants.CRC16_SIZE),
            version = (bytes[2].toInt() and 0xFF) ushr 2,
            crc8 = crc8,
            crc16 = crc16,
        )
    }

    private fun writeU16Be(value: Int, dest: ByteArray, offset: Int) {
        dest[offset] = ((value ushr 8) and 0xFF).toByte()
        dest[offset + 1] = (value and 0xFF).toByte()
    }

    private fun u16Be(bytes: ByteArray, offset: Int): Int {
        return ((bytes[offset].toInt() and 0xFF) shl 8) or (bytes[offset + 1].toInt() and 0xFF)
    }
}
