package com.alliot.osmo.demo.app.preferences

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

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        private const val PREFERENCES_NAME = "app_preferences"
        private const val KEY_DARK_THEME_ENABLED = "dark_theme_enabled"
    }
}
