package com.mniroy.osmo.demo.protocol.duml

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DumlPayloadCodecTest {
    @Test
    fun set_pairing_pin_payload_encodes_identifier_and_pin() {
        val payload = DumlPayloadCodec.encode(
            DumlCmdSet.WIFI,
            DumlWifiCmd.SET_PAIRING_PIN,
            SetPairingPinPayload(
                identifier = "001749319286102",
                pin = "5160",
            ),
        )

        assertArrayEquals(
            hex("0f3030313734393331393238363130320435313630"),
            payload,
        )
    }

    @Test
    fun pairing_status_payload_decodes_already_paired_state() {
        val payload = DumlPayloadCodec.decode(
            DumlCmdSet.WIFI,
            DumlWifiCmd.SET_PAIRING_PIN,
            DumlFlags.RESPONSE,
            byteArrayOf(0x00, 0x01),
        ) as PairingStatusPayload

        assertEquals(PairingStatus.ALREADY_PAIRED, payload.status)
        assertEquals(0x00, payload.rawStatusPrefix)
    }

    @Test
    fun pairing_status_payload_decodes_approval_required_state() {
        val payload = DumlPayloadCodec.decode(
            DumlCmdSet.WIFI,
            DumlWifiCmd.SET_PAIRING_PIN,
            DumlFlags.RESPONSE,
            byteArrayOf(0x00, 0x02),
        ) as PairingStatusPayload

        assertEquals(PairingStatus.APPROVAL_REQUIRED, payload.status)
    }

    @Test
    fun pairing_approved_payload_decodes_confirmation_byte() {
        val payload = DumlPayloadCodec.decode(
            DumlCmdSet.WIFI,
            DumlWifiCmd.PAIRING_APPROVED,
            DumlFlags.REQUEST,
            byteArrayOf(0x01),
        ) as PairingApprovedPayload

        assertTrue(payload.approved)
    }

    @Test
    fun gimbal_telemetry_payload_decodes_known_fields_and_preserves_tail() {
        val payload = DumlPayloadCodec.decode(
            DumlCmdSet.GIMBAL,
            DumlGimbalCmd.POSITION_TELEMETRY,
            DumlFlags.NOTIFY,
            hex("85ff05002c0321f09abc"),
        ) as GimbalTelemetryPayload

        assertEquals(-123, payload.pitchTenths)
        assertEquals(5, payload.rollTenths)
        assertEquals(812, payload.yawTenths)
        assertEquals(0x21, payload.modeFlags)
        assertEquals(0xF0, payload.rollAdjust)
        assertArrayEquals(hex("9abc"), payload.tail)
    }

    private fun hex(value: String): ByteArray {
        require(value.length % 2 == 0) { "Hex string must have even length." }
        return ByteArray(value.length / 2) { index ->
            value.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }
}
