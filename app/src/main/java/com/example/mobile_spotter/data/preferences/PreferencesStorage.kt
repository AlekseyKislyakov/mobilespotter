package com.example.mobile_spotter.data.preferences

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PreferencesStorage @Inject constructor(
    application: Application
) {

    companion object {
        const val PREF_FILE_NAME = "pref_file"

        private const val KEY_DEVICE_ID = "KEY_DEVICE_ID"
        private const val KEY_USER_ID = "KEY_USER_ID"
        private const val KEY_PUBLIC_ACCOUNT = "KEY_PUBLIC_ACCOUNT"
        private const val KEY_TOKEN = "KEY_TOKEN"
    }

    private val pref: SharedPreferences

    init {
        pref = application.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    }

    var userId: Int?
        get() = pref.getInt(KEY_USER_ID, 0)
        set(userId) {
            pref.edit().putInt(KEY_USER_ID, userId ?: 0).apply()
        }

    var deviceId: Int?
        get() = pref.getInt(KEY_DEVICE_ID, -1)
        set(deviceId) {
            pref.edit().putInt(KEY_DEVICE_ID, deviceId ?: -1).apply()
        }

    var publicAccount: Boolean?
        get() = pref.getBoolean(KEY_PUBLIC_ACCOUNT, false)
        set(publicAccount) {
            pref.edit().putBoolean(KEY_PUBLIC_ACCOUNT, publicAccount ?: false).apply()
        }

    var token: String?
        get() = pref.getString(KEY_TOKEN, "keddva5rd")
        set(token) {
            pref.edit().putString(KEY_TOKEN, token).apply()
        }
}
