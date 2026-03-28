package com.alliot.osmo.demo.session.fake

import com.alliot.osmo.demo.session.model.CameraStatusSnapshot
import com.alliot.osmo.demo.session.model.SessionDevice

object FakeCameraScript {
    val devices = listOf(
        SessionDevice(name = "Osmo Action 5 Pro", macAddress = "AA:BB:CC:DD:EE:01", deviceId = 0xFF44),
        SessionDevice(name = "Osmo 360", macAddress = "AA:BB:CC:DD:EE:02", deviceId = 0xFF66),
    )

    fun initialStatus(device: SessionDevice): CameraStatusSnapshot {
        return CameraStatusSnapshot(
            mode = 0x01,
            state = 0x01,
            recording = false,
            powerMode = 0,
            batteryPercent = 87,
            remainTimeSeconds = 3_600,
            detail = "${device.name} ready",
            lastPushCommandId = "1D02",
            lastPushSummary = "Fake status initialized",
        )
    }
}
