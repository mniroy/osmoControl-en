package com.alliot.osmo.demo.session.fake

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeSessionControllerTest {
    @Test
    fun fake_session_completes_handshake_and_toggles_recording() = runBlocking {
        val controller = FakeSessionController()
        controller.startScan()
        val device = controller.devices.value.first()
        controller.connect(device)
        controller.toggleRecording()

        assertTrue(controller.status.value.protocolReady)
        assertTrue(controller.cameraStatus.value.recording)
        assertEquals(device, controller.status.value.connectedDevice)
    }
}
