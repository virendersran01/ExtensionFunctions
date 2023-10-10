package com.virtualstudios.extensionfunctions.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.reflect.KProperty


class SettingsManager(context: Context) {
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    var isNotificationsEnabled: Boolean by BooleanPreferenceDelegate(
        preferences, "notifications_enabled", true
    )
}

class BooleanPreferenceDelegate(
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Boolean
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        preferences.edit {
            putBoolean(key, value)
        }
    }
}