package com.mniroy.osmo.demo.protocol.frame

data class DjiFrame(
    val version: Int = 0,
    val cmdType: Int,
    val enc: Int = 0,
    val seq: Int,
    val cmdSet: Int,
    val cmdId: Int,
    val payload: ByteArray,
)

data class DecodedDjiFrame(
    val frame: DjiFrame,
    val crc16: Int,
    val crc32: Long,
)
