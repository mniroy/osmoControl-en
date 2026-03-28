package com.alliot.osmo.demo.app.identity

import android.content.Context

class PairedCameraStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isPaired(macAddress: String): Boolean {
        return preferences.getBoolean(keyFor(macAddress), false)
    }

    fun markPaired(macAddress: String) {
        preferences.edit().putBoolean(keyFor(macAddress), true).apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun keyFor(macAddress: String): String = macAddress.trim().uppercase()

    private companion object {
        private const val PREFERENCES_NAME = "paired_cameras"
    }
}
