package com.asdzheng.openchat.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.asdzheng.openchat.App
import com.asdzheng.openchat.R

class PreferencesManager {
    private val sharedPrefs: SharedPreferences? = null

    companion object {
        @JvmStatic
        val openAIAPIKey: String?
            get() = prefs.getString("pref_openai_api_key", "")

        @JvmStatic
        fun setOpenAIAPIKey(key: String) {
            prefs.edit().putString("pref_openai_api_key", key.trim { it <= ' ' }).apply()
        }

        val openAIModel: String?
            get() = prefs.getString(
                "pref_openai_model",
                App.getContext().getString(R.string.default_openai_model)
            )
        val openAITemperature: String?
            get() = prefs.getString(
                "pref_openai_temperature",
                App.getContext().getString(R.string.temp_balance)
            )
        var openAITemperatureValue: Int
            get() = prefs.getInt("pref_openai_temperature_value", TEMP_BALANCE)
            set(temp) {
                prefs.edit().putInt("pref_openai_temperature_value", temp).apply()
            }
        val isOpenAIEcho: Boolean
            get() = prefs.getBoolean("pref_openai_echo", false)
        val prefs: SharedPreferences
            get() = PreferenceManager.getDefaultSharedPreferences(App.getContext())

        fun getPrefs(key: String?): SharedPreferences {
            return App.getContext().getSharedPreferences(key, Context.MODE_PRIVATE)
        }

        fun getTemperatureValue(): Float {
            return when (openAITemperatureValue) {
                TEMP_CREATIVE -> 0.8f
                TEMP_PRECISE -> 0.2f
                else -> 0.5f
            }
        }

        const val TEMP_BALANCE = 0
        const val TEMP_CREATIVE = 1
        const val TEMP_PRECISE = 2

    }
}