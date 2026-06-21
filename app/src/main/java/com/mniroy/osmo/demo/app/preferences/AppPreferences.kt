package com.mniroy.osmo.demo.app.preferences

import android.content.Context

class AppPreferences(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isDarkThemeEnabled(defaultValue: Boolean): Boolean {
        return preferences.getBoolean(KEY_DARK_THEME_ENABLED, defaultValue)
    }

    fun setDarkThemeEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_DARK_THEME_ENABLED, enabled).apply()
    }

    fun getLastConnectedDevice(): Pair<String, String>? {
        val mac = preferences.getString(KEY_LAST_CONNECTED_MAC, null)
        val name = preferences.getString(KEY_LAST_CONNECTED_NAME, null)
        return if (mac != null && name != null) Pair(mac, name) else null
    }

    fun setLastConnectedDevice(macAddress: String?, name: String?) {
        if (macAddress == null || name == null) {
            preferences.edit().remove(KEY_LAST_CONNECTED_MAC).remove(KEY_LAST_CONNECTED_NAME).apply()
        } else {
            preferences.edit()
                .putString(KEY_LAST_CONNECTED_MAC, macAddress)
                .putString(KEY_LAST_CONNECTED_NAME, name)
                .apply()
        }
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        private const val PREFERENCES_NAME = "app_preferences"
        private const val KEY_DARK_THEME_ENABLED = "dark_theme_enabled"
        private const val KEY_LAST_CONNECTED_MAC = "last_connected_mac"
        private const val KEY_LAST_CONNECTED_NAME = "last_connected_name"
    }
}
