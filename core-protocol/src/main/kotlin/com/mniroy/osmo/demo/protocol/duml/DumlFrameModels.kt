package com.mniroy.osmo.demo.protocol.duml

data class DumlFrame(
    val target: Int,
    val messageId: Int,
    val flags: Int,
    val cmdSet: Int,
    val cmdId: Int,
    val payload: ByteArray = ByteArray(0),
    val version: Int = DumlConstants.VERSION,
) {
    companion object {
        fun request(
            target: Int,
            messageId: Int,
            cmdSet: Int,
            cmdId: Int,
            payload: ByteArray = ByteArray(0),
        ): DumlFrame = DumlFrame(
            target = target,
            messageId = messageId,
            flags = DumlFlags.REQUEST,
            cmdSet = cmdSet,
            cmdId = cmdId,
            payload = payload,
        )
    }
}

data class DumlDecodedFrame(
    val target: Int,
    val sender: Int,
    val receiver: Int,
    val messageId: Int,
    val flags: Int,
    val cmdSet: Int,
    val cmdId: Int,
    val payload: ByteArray,
    val version: Int,
    val crc8: Int,
    val crc16: Int,
)
