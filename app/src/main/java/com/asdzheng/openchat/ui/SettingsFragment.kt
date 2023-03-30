package com.asdzheng.openchat.ui;

import android.os.Bundle;

import com.asdzheng.openchat.R;
import com.bluewhaleyt.component.preferences.CustomPreferenceFragment;

import androidx.annotation.Nullable;

public class SettingsFragment extends CustomPreferenceFragment {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey);
        super.onCreatePreferences(savedInstanceState, rootKey);
        init();
    }

    private void init() {

    }

}
