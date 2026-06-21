package com.mniroy.osmo.demo.protocol.payload

object CommandIds {
    const val CMD_SET_COMMON: Int = 0x00
    const val CMD_SET_CAMERA: Int = 0x1D

    const val VERSION_QUERY: Int = 0x00
    const val KEY_REPORT: Int = 0x11
    const val REBOOT: Int = 0x16
    const val GPS_PUSH: Int = 0x17
    const val CONNECTION: Int = 0x19
    const val POWER_MODE: Int = 0x1A

    const val RECORD_CONTROL: Int = 0x03
    const val CAMERA_MODE_SWITCH: Int = 0x04
    const val CAMERA_STATUS_SUBSCRIPTION: Int = 0x05
    const val CAMERA_STATUS_PUSH: Int = 0x02
    const val NEW_CAMERA_STATUS_PUSH: Int = 0x06
}
