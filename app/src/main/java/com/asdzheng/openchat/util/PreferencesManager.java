package com.asdzheng.openchat.util;

import android.content.Context;
import android.content.SharedPreferences;


import com.asdzheng.openchat.App;
import com.asdzheng.openchat.R;

import androidx.preference.PreferenceManager;

public class PreferencesManager {

    private SharedPreferences sharedPrefs;

    public static String getOpenAIAPIKey() {
        return getPrefs().getString("pref_openai_api_key", "");
    }

    public static void setOpenAIAPIKey(String key) {
         getPrefs().edit().putString("pref_openai_api_key", key.trim()).apply();
    }


    public static String getOpenAIModel() {
        return getPrefs().getString("pref_openai_model", App.getContext().getString(R.string.default_openai_model));
    }

    public static String getOpenAITemperature() {
        return getPrefs().getString("pref_openai_temperature", "0.2");
    }

    public static boolean isOpenAIEcho() {
        return getPrefs().getBoolean("pref_openai_echo", false);
    }

    public static SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(App.getContext());
    }

    public static SharedPreferences getPrefs(String key) {
        return App.getContext().getSharedPreferences(key, Context.MODE_PRIVATE);
    }

}
