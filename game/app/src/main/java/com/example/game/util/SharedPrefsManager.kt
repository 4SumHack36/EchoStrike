package com.example.game.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPrefsManager(context: Context) {

    private val sharedPrefsName = "SensorMonitorSharedPrefs"
    private val prefs: SharedPreferences =
        context.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)

    fun saveInt(key: String, value: Int) {
        prefs.edit {
            putInt(key, value)
        }
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return prefs.getInt(key, defaultValue)
    }

    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit {
            putBoolean(key, value)
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun saveString(key: String, value: String) {
        prefs.edit {
            putString(key, value)
        }
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return prefs.getString(key, defaultValue) ?: ""
    }

    fun deleteString(key: String) {
        prefs.edit {
            remove(key)
        }
    }

    fun deleteBoolean(key: String) {
        prefs.edit {
            remove(key)
        }
    }

    fun deleteInt(key: String) {
        prefs.edit {
            remove(key)
        }
    }
}