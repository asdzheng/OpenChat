package com.asdzheng.openchat.ui

import android.os.Bundle
import androidx.preference.Preference
import com.asdzheng.openchat.R
import com.asdzheng.openchat.util.PreferencesManager
import com.bluewhaleyt.component.preferences.CustomPreferenceFragment

class SettingsFragment : CustomPreferenceFragment(), Preference.OnPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey)
        super.onCreatePreferences(savedInstanceState, rootKey)
        init()
    }

    private fun init() {
        findPreference<Preference>("pref_openai_temperature")!!.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val key = preference.key
        if ("pref_openai_temperature" == key) {
            when (newValue) {
                getString(R.string.temp_creative) -> {
                    PreferencesManager.openAITemperatureValue = PreferencesManager.TEMP_CREATIVE
                }
                getString(R.string.temp_precise) -> {
                    PreferencesManager.openAITemperatureValue = PreferencesManager.TEMP_PRECISE
                }
                else -> {
                    PreferencesManager.openAITemperatureValue = PreferencesManager.TEMP_BALANCE
                }
            }
        }
        return true
    }
}