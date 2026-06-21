package com.mniroy.osmo.demo.app.identity

import android.content.Context
import kotlin.random.Random

class ControllerIdentityStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun controllerDeviceId(): Long {
        val existing = preferences.getLong(KEY_CONTROLLER_DEVICE_ID, 0L)
        if (existing != 0L) return existing

        val generated = Random.nextLong(1L, 0x1_0000_0000L)
        preferences.edit().putLong(KEY_CONTROLLER_DEVICE_ID, generated).apply()
        return generated
    }

    fun nextVerifyCode(): Int = Random.nextInt(0, 10_000)

    fun controllerMacAddressBytes(): ByteArray {
        val existing = preferences.getString(KEY_CONTROLLER_MAC_ADDRESS, null)
        if (existing != null) {
            return parseMac(existing)
        }

        val generated = ByteArray(6).also { bytes ->
            Random.nextBytes(bytes)
            bytes[0] = (((bytes[0].toInt() and 0xFC) or 0x02) and 0xFF).toByte()
        }
        preferences.edit().putString(KEY_CONTROLLER_MAC_ADDRESS, formatMac(generated)).apply()
        return generated
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun parseMac(value: String): ByteArray {
        return value.split(":")
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    private fun formatMac(bytes: ByteArray): String {
        return bytes.joinToString(":") { byte -> "%02X".format(byte.toInt() and 0xFF) }
    }

    private companion object {
        private const val PREFERENCES_NAME = "controller_identity"
        private const val KEY_CONTROLLER_DEVICE_ID = "controller_device_id"
        private const val KEY_CONTROLLER_MAC_ADDRESS = "controller_mac_address"
    }
}
