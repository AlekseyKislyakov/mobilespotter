package com.example.mobile_spotter.data.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesStorage @Inject constructor(
    context: Context
) {

    companion object {
        const val PREF_FILE_NAME = "pref_file"

        private const val KEY_ACCESS_TOKEN = "accessToken"
        private const val KEY_IS_SIGNED_IN = "isSignedIn"
        private const val KEY_CITY_ID = "cityId"
        private const val KEY_LAST_CATEGORIES_UPDATE = "lastCategoriesUpdate"
        private const val KEY_IS_CITY_SELECTED = "isCitySelected"
        private const val KEY_CRYPTO_MANAGER_IV = "cryptoManagerIv"
        private const val KEY_IS_X_TESTER_HEADER_ENABLED = "isXTestersHeaderEnabled"
    }

    private val pref: SharedPreferences

    init {
        pref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    }


    var isSignedIn: Boolean
        get() = pref.getBoolean(KEY_IS_SIGNED_IN, false)
        set(value) {
            pref.edit().putBoolean(KEY_IS_SIGNED_IN, value).apply()
        }

    var cityId: String?
        get() = pref.getString(KEY_CITY_ID, null)
        set(cityId) {
            pref.edit().putString(KEY_CITY_ID, cityId).apply()
        }

    var lastCategoriesUpdate: Long
        get() = pref.getLong(KEY_LAST_CATEGORIES_UPDATE, 0)
        set(value) {
            pref.edit().putLong(KEY_LAST_CATEGORIES_UPDATE, value).apply()


            @SuppressLint("ApplySharedPref")
            suspend fun clear() = withContext(Dispatchers.IO) {
                pref.edit().clear().commit()
            }
        }
}
